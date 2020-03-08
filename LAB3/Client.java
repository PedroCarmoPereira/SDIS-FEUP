import java.io.*;
import java.net.*;

class Client{

    public static void print_usage(){
        System.out.println("Usage: Client <host> <port> <oper> <opernd*>");
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
        try{ port = Integer.parseInt(args[1]);}
        catch (NumberFormatException ex){
            System.out.println("Invalid port number");
            return;
        }

        String req_string;
        if(args[2].equals("lookup")) req_string = args[2] + " " + args[3];
        else if (args[2].equals("register") && args.length == 5) req_string = args[2] + " " + args[3] + " " + args[4];
        else{
            System.out.println("Invalid Operation");
            print_usage();
            return;
        }
        try {
            InetAddress address = InetAddress.getByName(hostname);
            DatagramSocket socket = new DatagramSocket();
            byte[] b = req_string.getBytes();
            DatagramPacket request = new DatagramPacket(b, b.length, address, port);
            socket.send(request);

            byte[] buffer = new byte[512];
            DatagramPacket response = new DatagramPacket(buffer, buffer.length);
            socket.receive(response);

            String resp_data = new String(buffer);
            System.out.println("Client: " + req_string + ": " + resp_data);

        } catch (SocketTimeoutException ex) {
            System.out.println("Timeout error: " + ex.getMessage());
            ex.printStackTrace();
        } catch (IOException ex) {
            System.out.println("Client error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}