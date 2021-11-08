import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class ChordNode {
    private Receiver receiver;
    private static ChordNodeInfo chordInfo;
    private static ArrayList<ChordNodeInfo> fingerTable;

    public ChordNode(int myPort, String ip, String port) {
        Peer.setThreadPool();
        chordInfo = new ChordNodeInfo(myPort);
        chordInfo.printAddressInfo();
        this.addReceiver();
        this.askToJoin(ip, port);
        fingerTable = null;
        Peer.getThreadPool().scheduleAtFixedRate(new Stabilize(), 0, 3, TimeUnit.SECONDS);
    }

    public ChordNode(int myPort) {
        Peer.setThreadPool();
        chordInfo = new ChordNodeInfo(myPort);
        chordInfo.printAddressInfo();
        this.addReceiver();
        fingerTable = null;
        Peer.getThreadPool().scheduleAtFixedRate(new Stabilize(), 0, 3, TimeUnit.SECONDS);
        System.err.println(MyUtils.ANSI_GREEN + "\nPeer ready\tID:" + chordInfo.getId() + MyUtils.ANSI_RESET);
    }

    /**
     * sends message to network to join
     */
    public void askToJoin(String ip, String port) {
        Join join = new Join(chordInfo.getInternetIp(), chordInfo.getPort(), chordInfo.getId());
        if(!SocketFunctions.testConnection(ip, Integer.parseInt(port))){
            System.out.println(MyUtils.ANSI_RED + "Failed connection to server." + MyUtils.ANSI_RESET);
            System.exit(0);
        }
        else
            SocketFunctions.createSocketAndSend(ip, Integer.parseInt(port), join);
    }

    /**
     * create a receiver server for the chord 
     */
    public void addReceiver() {
        this.receiver = new Receiver(chordInfo.getLocalIp(), chordInfo.getPort());
        Peer.getThreadPool().execute(this.receiver);
    }

    /*************************************
     * GETTERS
     **************************************/

    /**
     * @return the chordInfo
     */
    public static ChordNodeInfo getChordInfo() {
        return chordInfo;
    }

    /**
     * @return the fingerTable
     */
    public static ArrayList<ChordNodeInfo> getFingerTable() {
        return fingerTable;
    }

    public static void setIndexFT(int i, ChordNodeInfo newnode){
        fingerTable.set(i, newnode);
    }

    public static void initFT(ChordNodeInfo info){
        fingerTable = new ArrayList<ChordNodeInfo>(Collections.nCopies(ChordNodeInfo.getM(), info));
    }

    public static BigInteger ftIndex(int i){
        Integer sum = (int) Math.pow(2, i - 1);
        Integer mod = (int) Math.pow(2, ChordNodeInfo.getM());
        return chordInfo.getId().add(new BigInteger(sum.toString())).mod(new BigInteger(mod.toString()));
    }

    public static void wipeFT(){
        fingerTable = null;
    }
}