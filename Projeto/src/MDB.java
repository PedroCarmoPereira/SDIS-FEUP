import java.util.*;
import java.net.*;
import java.io.*;


class MDB implements Runnable {

    String address;
    int port;

    public MDB(String address, int port) throws SocketException {
        this.address = address;
        this.port = port;
    }

    public void send(String msg) {
        try{
            InetAddress group = InetAddress.getByName(this.address);
            DatagramSocket socket = new DatagramSocket();
            byte[] b = msg.getBytes();
            DatagramPacket request = new DatagramPacket(b, b.length, group, this.port);
            socket.send(request);
        }
        catch (SocketTimeoutException ex) {
            System.out.println("Timeout error: " + ex.getMessage());
            ex.printStackTrace();
        } catch (IOException ex) {
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Override
    public void run() {

        byte[] buff = new byte[66000];

        try {
            MulticastSocket socket = new MulticastSocket(this.port);
            InetAddress group = InetAddress.getByName(this.address);
            socket.joinGroup(group);

            while (true) {
                DatagramPacket recieved = new DatagramPacket(buff, buff.length);
                socket.receive(recieved);
                String recievedData = new String(recieved.getData());
                System.out.println(recievedData);
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }

    }
}