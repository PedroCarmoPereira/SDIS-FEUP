import java.util.*;
import java.io.IOException;
import java.net.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class Server extends Thread {
    private MulticastSocket socket;
    private Map<String, String> dns_map = new HashMap<String, String>();
    private String mcast_addr;
    private int mcast_port;
    private int port;
    private InetAddress group;

    public Map<String, String> getDNSMap() {
        return this.dns_map;
    }

    public Server(int port, String mcast_addr, int mcast_port) throws SocketException, IOException {
        this.mcast_addr = mcast_addr;
        this.mcast_port = mcast_port;
        this.port = port;
        socket = new MulticastSocket(mcast_port);
        this.group = InetAddress.getByName(mcast_addr);
        socket.joinGroup(group);
    }

    public String[] lookup(String dns_name) {
        String[] ret = { dns_name, null };
        if (dns_map.get(dns_name) != null)
            ret[1] = this.dns_map.get(dns_name);
        return ret;
    }

    public int register(String dns_name, String ip_addr) {
        if (this.lookup(dns_name)[1] != null)
            return -1;
        this.dns_map.put(dns_name, ip_addr);
        return this.dns_map.size();
    }

    public void service() throws IOException {

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
        Advertise advertise = new Advertise(this.socket, this.group, this.mcast_addr, this.mcast_port, this.port);
        Task task = new Task(this.port);

        executor.schedule(task, 1, TimeUnit.SECONDS);
        executor.scheduleAtFixedRate(advertise, 1, 1, TimeUnit.SECONDS);
    }

    public static void main(String args[]) {

        if (args.length < 3) {
            System.out.println("Usage: java Server <portno> <mcast_addr> <mcast_port>");
            return;
        }

        int port;
        int mcast_port;

        try {
            port = Integer.parseInt(args[0]);
            mcast_port = Integer.parseInt(args[2]);
        } catch (NumberFormatException ex) {
            System.out.println("Invalid port number");
            return;
        }

        Server self;
        try {
            self = new Server(port, args[1], mcast_port);
            self.service();
        } catch (SocketException ex) {
            System.out.println("Socket error: " + ex.getMessage());
            return;
        } catch (IOException ex) {
            System.out.println("IO error: " + ex.getMessage());
            return;
        }

    }
}