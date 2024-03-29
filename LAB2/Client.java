import java.io.*;
import java.net.*;

class Client {

    public static void print_usage() {
        System.out.println("Usage: Client <mcast_addr> <mcast_port> <oper> <opernd*>");
        System.out.println("<oper>: lookup -> <opernd> : <dns_name>");
        System.out.println("<oper>: register -> <opernd> : <dns_name> && <ip_addr>");
    }

    public static void main(String[] args) {
        if (args.length < 4) {
            print_usage();
            return;
        }

        String hostname = args[0];
        int port;
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            System.out.println("Invalid port number");
            return;
        }

        String req_string;
        if (args[2].equals("lookup"))
            req_string = args[2] + " " + args[3];
        else if (args[2].equals("register") && args.length == 5)
            req_string = args[2] + " " + args[3] + " " + args[4];
        else {
            System.out.println("Invalid Operation");
            print_usage();
            return;
        }
        try {

            MulticastSocket socket = new MulticastSocket(port);
            InetAddress group = InetAddress.getByName(hostname);
            socket.joinGroup(group);
            byte[] buf = new byte[512];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            String received = new String(packet.getData(), 0, packet.getLength());
            
            byte[] b = received.getBytes();
            DatagramPacket request = new DatagramPacket(b, b.length, group, Integer.parseInt(received));
            socket.send(request);

            byte[] buffer = new byte[512];
            DatagramPacket response = new DatagramPacket(buffer, buffer.length);
            socket.receive(response);

            String resp_data = new String(buffer);
            System.out.println("Client: " + req_string + ": " + resp_data);

            socket.leaveGroup(group);
            socket.close();
            
        } catch (SocketTimeoutException ex) {
            System.out.println("Timeout error: " + ex.getMessage());
            ex.printStackTrace();
        } catch (IOException ex) {
            System.out.println("Client error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}