import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;

public class SocketFunctions {

    // Returns a ServerSocket, ready to accept incoming link requests, if timeout ==
    // 0, doesn't set timeout
    public static ServerSocket createTCPListenerSocket(int port, InetAddress addr, int timeout) {
        ServerSocket serverSocket = null;
        try {
            if (addr == null)
                addr = InetAddress.getByName("127.0.0.1");
            serverSocket = new ServerSocket(port, 1, addr);
            if (timeout != 0)
                serverSocket.setSoTimeout(timeout);

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Server is listening on port " + port + " and address " + addr);

        return serverSocket;
    }

    // Returns a Socket, ready to send objects to destPort and destAddr, if timeout
    // == 0, doesn't set timeout
    public static Socket createTCPSenderSocket(InetAddress destinationAddr, int destinationPort, int timeout) {
        Socket socket = null;
        try {
            InetSocketAddress endPoint = new InetSocketAddress(destinationAddr, destinationPort);
            socket = new Socket();
            socket.connect(endPoint, timeout);
            return socket;
        } catch (IOException e) {
            System.out.println(
                    "Host: " + destinationAddr.toString().substring(1) + ":" + destinationPort + " unreachable");
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("here");
            System.out.println(
                    "Host: " + destinationAddr.toString().substring(1) + ":" + destinationPort + " unreachable");
            return null;
        }
    }

    // Receives a Socket to listen, returns the received object
    public static Object socketListenerTCP(Socket socket) {
        ObjectInputStream inStream;
        Object received = null;
        try {
            inStream = new ObjectInputStream(socket.getInputStream());
            received = inStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return received;
    }

    // Sends objToSend to TCP socket
    public static void socketSender(Socket socket, Object objToSend) {
        ObjectOutputStream outputStream;
        try {
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(objToSend);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createSocketAndSend(String destinationIp, int destinationPort, Object objToSend, int timeout) {
        try {
            InetAddress destinationAddr = InetAddress.getByName(destinationIp);
            Socket socket = createTCPSenderSocket(destinationAddr, destinationPort, timeout);
            if (socket != null) {
                socketSender(socket, objToSend);
                closeSocket(socket);
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    // same but without timeout
    public static void createSocketAndSend(String destinationIp, int destinationPort, Object objToSend) {
        try {
            InetAddress destinationAddr = InetAddress.getByName(destinationIp);
            Socket socket = createTCPSenderSocket(destinationAddr, destinationPort, 0);
            if (socket != null) {
                socketSender(socket, objToSend);
                closeSocket(socket);
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public static boolean testConnection(String destinationIp, int destinationPort) {
        try {
            InetAddress destinationAddr = InetAddress.getByName(destinationIp);
            Socket s = createTCPSenderSocket(destinationAddr, destinationPort, 3000);
            if (s != null) {
                socketSender(s, "");
                closeSocket(s);
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Necessário?
    public static void closeSocket(Socket socket) {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
