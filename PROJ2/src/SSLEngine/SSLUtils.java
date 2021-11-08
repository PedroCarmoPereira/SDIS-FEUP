import java.io.*;


public class SSLUtils {
    
    // Retorna uma SSLServer
    public static SSLServer createSSLServer(String hostAddress, int port) {
        SSLServer returnSv = null; 
        try{
            returnSv = new SSLServer("TLSv1.2", hostAddress, port);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return returnSv;
    }

    // Retorna uma SSLClient já conectada, se conseguir...
    public static SSLClient createSSLClient(String destinationIP, int destinationPort) {
        SSLClient client = null;

        
        try {
            client = new SSLClient("TLSv1.2", destinationIP, destinationPort);
            client.connectToSv();
        } catch (Exception e) {
            return null;
        }
        
        return client;
    }

    // Começa um loop que retorna objetos recebidos da SSLServer, meter o handle aqui dentro
    public static Object sslServerListener(SSLServer server) {
        Object returnObj = null;
        try {
            returnObj = server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return returnObj;
    }

    // Recebe uma SSLClient e um objeto para enviar
    public static void sendObjectToSSLServer(SSLClient client, Object obj) {
        try {
            client.writeObject(client.getSocketChannel(), client.getEngine(), obj);
            client.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Converter Objetos para byte[]
    public static byte[] serialize(Object obj) {
        ByteArrayOutputStream out = null;

        try {
            out = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(out);
            os.writeObject(obj);
            return out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }

    // ao contrário
    public static Object deserialize(byte[] data) {
        Object returnObj = null;


        try {
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            ObjectInputStream is = new ObjectInputStream(in);
            returnObj = is.readObject();
        } catch( Exception e) {
            e.printStackTrace();
        }

        return returnObj;
    }
}