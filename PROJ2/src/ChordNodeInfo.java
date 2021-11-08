import java.math.BigInteger;
import java.security.MessageDigest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.URL;

public class ChordNodeInfo implements Serializable{
    private static final long serialVersionUID = 1L;

    public static int m = 10; // Vamos deixar baixinho para já

    private BigInteger id;
    private String internetIp;
    private String localIp;
    private int port;
    private ChordNodeInfo predecessor;

    public ChordNodeInfo(int myPort) {
        this.findInternetIp();
        this.findLocalIp();
        this.port = myPort;
        this.createId();
        predecessor = null;
    }

    public ChordNodeInfo(BigInteger id, String inetIP, int port, ChordNodeInfo c){
        this.id = id;
        this.internetIp = inetIP;
        this.port = port;
        this.localIp = "";
        predecessor = c;
    }

    private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
		stream.writeObject(id);
		stream.writeObject(internetIp);
        stream.writeObject(port);
        
        stream.writeObject(predecessor);
    }

    private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
        id = (BigInteger) stream.readObject();
        internetIp = (String) stream.readObject();
        port = (int) stream.readObject();

        predecessor = (ChordNodeInfo) stream.readObject();



	}

    private void createId() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte adressToHash[] = (this.internetIp + this.port).getBytes();

            this.id = new BigInteger(1, digest.digest(adressToHash)).mod(BigInteger.valueOf(2).pow(m));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void findInternetIp() {
        BufferedReader in = null;
        try {
            URL whatismyip = new URL("http://checkip.amazonaws.com");
            in = new BufferedReader(new InputStreamReader(
                    whatismyip.openStream()));
            this.internetIp = in.readLine();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private void findLocalIp() {
        try{
            final DatagramSocket socket = new DatagramSocket();
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            this.localIp = socket.getLocalAddress().getHostAddress();
            socket.close();
        } catch(Exception e){
            e.printStackTrace();
        }
        
    }

    public void printAddressInfo() {
        System.out.println("Ip: " + this.internetIp + "\tPort: " + this.port);
    }

    /*************************************
     * GETTERS
     **************************************/

    /**
     * @return the id
     */
    public BigInteger getId() {
        return id;
    }

    /**
     * @return the internetIp
     */
    public String getInternetIp() {
        return internetIp;
    }

    /**
     * @return the localIp
     */
    public String getLocalIp() {
        return localIp;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @return the m
     */
    public static int getM() {
        return m;
    }

    /**
     * @return the predecessor
     */
    public ChordNodeInfo getPredecessor() {
        return predecessor;
    }

    /**
     * @return the predecessor
     */
    public ChordNodeInfo getSuccessor() {
        if(ChordNode.getFingerTable() == null)
            return null;
        return ChordNode.getFingerTable().get(0);
    }

    /**
     * @param predecessor the predecessor to set
     */
    public void setPredecessor(ChordNodeInfo predecessor) {
        this.predecessor = predecessor;
    }

}