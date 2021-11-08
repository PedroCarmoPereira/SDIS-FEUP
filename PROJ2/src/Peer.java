import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.math.BigInteger;

public class Peer extends ChordNode  implements RMI {
    
    public static Registry registry = null;
    public static RMI stub = null;
    public static int REP_DEGREE = 3;

    private static ScheduledExecutorService threadPool;

    private String id;

    public Peer(String id, int myPort, String ip, String port) {
        super(myPort, ip, port);
        this.id = id;
    }

    public Peer(String id, int myPort) {
        super(myPort);
        this.id = id;
    }

    public void backup(String path) throws RemoteException {

        if(!this.testNetwork())
            return;

        String fileName = FileHandler.getFileName(path);

        for(int i = 0; i < Peer.REP_DEGREE; i++){

            BigInteger fileId = FileHandler.getId(fileName + i);
            BackupRequest br = new BackupRequest(fileId, path);

            Lookup nl = new Lookup(fileId);
            int next_node = nl.getNextNode();

            if (next_node == -1) 
                System.out.println(MyUtils.ANSI_RED + "File " + fileId + " should belong to me..." + MyUtils.ANSI_RESET);
            else 
                SocketFunctions.createSocketAndSend(ChordNode.getFingerTable().get(next_node).getInternetIp(), ChordNode.getFingerTable().get(next_node).getPort(), br); 
    
        }
    }

    public void restore(String fileName) throws RemoteException {

        if(!this.testNetwork())
            return;

        for(int i = 0; i < Peer.REP_DEGREE; i++){
            BigInteger fileId = FileHandler.getId(fileName + i);
            RestoreRequest rr = new RestoreRequest(fileId, fileName);

            Lookup nl = new Lookup(fileId);
            int next_node = nl.getNextNode();

            if (next_node == -1) {
                System.out.println(MyUtils.ANSI_YELLOW + "File " + fileId + " should belong to me... Checking in storage folder." + MyUtils.ANSI_RESET);
                Restore r = new Restore(fileName, fileId);
                r.receive();
            }
            else 
                SocketFunctions.createSocketAndSend(ChordNode.getFingerTable().get(next_node).getInternetIp(), ChordNode.getFingerTable().get(next_node).getPort(), rr); 
    
        }
    }

    public void delete(String fileName) throws RemoteException {

        for(int i = 0; i < Peer.REP_DEGREE; i++){

            BigInteger fileId = FileHandler.getId(fileName + i);
            Delete d = new Delete(fileName, fileId);

            if(!this.testNetwork())
                continue;

            Lookup nl = new Lookup(fileId);
            int next_node = nl.getNextNode();

            if (next_node != -1)
                SocketFunctions.createSocketAndSend(ChordNode.getFingerTable().get(next_node).getInternetIp(), ChordNode.getFingerTable().get(next_node).getPort(), d);     
        }

    }

    public void stick() throws RemoteException{
        return;
    }

    private boolean testNetwork() {
        if (Peer.getFingerTable() == null) {
            System.out.println(MyUtils.ANSI_RED + "No peers on network" + MyUtils.ANSI_RESET);
            return false;
        }
        return true;
    }
    
    /**
     * @return the threadPool
     */
    public static ScheduledExecutorService getThreadPool() {
        return threadPool;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param threadPool the threadPool to set
     */
    public static void setThreadPool() {
        Peer.threadPool = Executors.newScheduledThreadPool(500);
    }

    public static void startRMI(String peerID, int RMIport, Peer peer) {
        try {
            Peer.stub = (RMI) UnicastRemoteObject.exportObject((Remote) peer, 0);
            try {
                Peer.registry = LocateRegistry.createRegistry(RMIport);
            } catch (RemoteException e) {
                Peer.registry = LocateRegistry.getRegistry(RMIport);
            }

            Peer.registry.bind(peerID, stub);
        } catch (Exception e) {
            System.err.println("Peer exception: " + e.toString());
            e.printStackTrace();
        }
    }

    public void stickRMI(int port){
        try {
            final Registry registry = LocateRegistry.getRegistry("localhost", port);
            RMI peer = (RMI) registry.lookup(this.getId());
            peer.stick();

        } catch (Exception e) {
            System.err.println("RMI not sticking: " + e.toString());
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        Peer peer;
        String peerID;
        int RMIport;
        int myPort;

        switch (args.length) {
            case 3:
                peerID = args[0];
                RMIport = Integer.parseInt(args[1]);
                myPort = Integer.parseInt(args[2]);
                peer = new Peer(peerID, myPort);
                break;

            case 5:
                peerID = args[0];
                RMIport = Integer.parseInt(args[1]);
                myPort = Integer.parseInt(args[2]);
                String ip = args[3];
                String port = args[4];
                peer = new Peer(peerID, myPort, ip, port);  
                break;

            default:
                System.out.println(MyUtils.ANSI_RED + "\nERROR:" + MyUtils.ANSI_YELLOW + " Incorrect args." + MyUtils.ANSI_RESET);
                System.out.println("\tCreator peer => Peer <peerId> <RMIport>.");
                System.out.println("\tJoining peer => Peer <peerId> <RMIport> <networkIp> <networkPort>.\n");
                return;
        }
        
        Peer.startRMI(peerID, RMIport, peer);
        peer.stickRMI(RMIport);
        //fazer um mini testApp
        
        /* System.out.println("My Id:" + ChordNode.getChordInfo().getId());
        for(int i = 1; i <= ChordNodeInfo.getM(); i++) System.out.println("FT[" + i + "]: " + ChordNode.ftIndex(i)); */

        /* --------------------Shut down code--------------------------- */
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.out.println(MyUtils.ANSI_RESET + "\nShutting down ...");
                if (ChordNode.getChordInfo().getPredecessor() != null && SocketFunctions.testConnection(ChordNode.getChordInfo().getPredecessor().getInternetIp(), ChordNode.getChordInfo().getPredecessor().getPort()))
                    SocketFunctions.createSocketAndSend(ChordNode.getChordInfo().getPredecessor().getInternetIp(), ChordNode.getChordInfo().getPredecessor().getPort(), new LeavePredecessor(ChordNode.getFingerTable().get(0)));

                if (ChordNode.getChordInfo().getSuccessor() != null && SocketFunctions.testConnection(ChordNode.getChordInfo().getSuccessor().getInternetIp(), ChordNode.getChordInfo().getSuccessor().getPort()))
                    SocketFunctions.createSocketAndSend(ChordNode.getChordInfo().getSuccessor().getInternetIp(), ChordNode.getChordInfo().getSuccessor().getPort(), new LeaveSuccessor());
                
                Peer.getThreadPool().shutdownNow();
            }
        });
        /* ------------------------------------------------------------ */

    }
}