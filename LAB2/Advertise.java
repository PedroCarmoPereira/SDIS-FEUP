import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

class Advertise implements Runnable {
    private MulticastSocket socket;
    private InetAddress group;
    private byte[] buf;
    private int mcast_port;
    private String mcast_addr;
    private int port;

    public Advertise(MulticastSocket socket, InetAddress group, String mcast_addr, int mcast_port, int port) {
        this.socket = socket;
        this.group = group;
        this.mcast_port = mcast_port;
        this.mcast_addr = mcast_addr;
        this.port = port;
    }

    @Override
    public void run(){
        try {
            buf =  String.valueOf(this.port).getBytes();

            DatagramPacket packet = new DatagramPacket(buf, buf.length, group, this.mcast_port);
            socket.send(packet);
        } catch (IOException ex) {
            System.out.println("IO error: " + ex.getMessage());
            return;
        }

        System.out.println("multicast: " + this.mcast_addr + " " + this.mcast_port + ": <srvc_addr> " + this.port);
    }
}