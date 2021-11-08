import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

class Receiver extends Thread {

    private ServerSocket server;
    private String ip;
    private int port;

    public Receiver(String ip, int port) {
        this.ip = ip;
        this.port = port;
        try {
            InetAddress addr = InetAddress.getByName(this.ip);
            this.server = SocketFunctions.createTCPListenerSocket(this.port, addr, 0);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while(true){
            try {
                Socket privateSocket = this.server.accept();
                Object obj = SocketFunctions.socketListenerTCP(privateSocket);
                Peer.getThreadPool().execute(new ProcessReceived(obj));
            } catch (IOException e) {}   
        }
	}
}
