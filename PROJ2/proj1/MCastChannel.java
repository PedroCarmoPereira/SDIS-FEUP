import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.*;

enum ChannelType {
    BACKUP,
    CONTROL,
    RESTORE,
    DEFAULT
}

class MCastChannel implements Runnable {

    int port;
    String address;
    ChannelType usage;
    public static ArrayList<Message> log = new ArrayList<Message>();

    public MCastChannel(String address, int port) throws SocketException {
        this.address = address;
        this.port = port;
        this.usage = ChannelType.DEFAULT ;
    }

    public MCastChannel(String address, ChannelType usage, int port) throws SocketException {
        this.address = address;
        this.port = port;
        this.usage = usage;
    }

    public void send(byte[] msg) {
        try {
            InetAddress group = InetAddress.getByName(this.address);
            DatagramSocket socket = new DatagramSocket();
            DatagramPacket request = new DatagramPacket(msg, msg.length, group, this.port);
            socket.send(request);
        } catch (SocketTimeoutException ex) {
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
            System.out.println("Connected to MCast Channel on: " + InetAddress.getByName(this.address) + ":" + this.port);
            while (true) {
                DatagramPacket recieved = new DatagramPacket(buff, buff.length);
                socket.receive(recieved);
                byte[] recievedData = Arrays.copyOf(buff, recieved.getLength());
                if(this.usage == ChannelType.RESTORE) log.add(new Message(recievedData));
                Peer.getThreadExecutor().execute(new MessageProcessor(recievedData));
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }

    }
}   