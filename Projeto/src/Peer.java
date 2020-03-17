import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.net.InetAddress;
import java.io.IOException;

public class Peer implements RMI {

    private String id;
    private MDB MDB;

    public Peer(String id) throws IOException {
        this.id = id;

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
        this.MDB = new MDB("224.0.0.0", 4444); // HARDCODED

        executor.execute(this.MDB);
    }

    public void backup(String path, int repDegree) {
        Backup backup = new Backup();
        for(int i = 0; i < backup.getNumberChunks(); i++){
            this.MDB.send(backup.getMessage(i)); //envia mensagem por MDB multicast data channel
        }
    }

    public static void main(String args[]) {

        if (args.length < 2) {
            System.out.println("Input error.");
            System.out.println("Usage: Peer <port number> <Peer Id>");
            return;
        }

        try {
            Peer obj = new Peer(args[1]);
            RMI stub = (RMI) UnicastRemoteObject.exportObject((Remote) obj, 0);

            Registry registry = LocateRegistry.createRegistry(Integer.parseInt(args[0]));
            registry.bind(args[1], stub);

            System.out.println("System IP Address : " + InetAddress.getLocalHost());
            System.err.println("Peer ready\n");
        } catch (Exception e) {
            System.err.println("Peer exception: " + e.toString());
            e.printStackTrace();
        }


        /* --------------------Shut down code--------------------------- */
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                    System.out.println("\nShutting down ...");
            }
        });
    }
}