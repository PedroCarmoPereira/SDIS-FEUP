import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.net.InetAddress;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;


public class Peer implements RMI {

    public static Registry registry;

    private static String id;
    private static int MAX_TRIES = 5;
    private static long STORAGE_SPACE_KB; // Expressed in KBytes, -1 if uncapped
    private static ScheduledExecutorService executor;

    private static MCastChannel MC;
    private static MCastChannel MDB;
    private static MCastChannel MDR;

    private static File chunkStorage;
    private static File restoreStorage;
    private static File fileNameIdFile;

    private static Map<String, ArrayList<String>> fileNameIdTable = new HashMap<String, ArrayList<String>>();
    private static Map<String, ArrayList<String>> chunksBackedUpPerFileId = new HashMap<String, ArrayList<String>>();
    private static Map<String, ArrayList<ArrayList<String>>> backupFiles = new HashMap<String, ArrayList<ArrayList<String>>>();

    private static State state;

    public static void readFileNameIdTableFromFile() {
        try {
            FileReader fr = new FileReader(Peer.fileNameIdFile);
            BufferedReader bfr = new BufferedReader(fr);
            String fileName;
            while ((fileName = bfr.readLine()) != null) {
                fileNameIdTable.put(fileName, new ArrayList<String>());
                String fileId;
                while ((fileId = bfr.readLine()) != null) {
                    if (fileId.equals("["))
                        continue;
                    if (fileId.equals("]"))
                        break;
                    fileNameIdTable.get(fileName).add(fileId);
                }

            }

            bfr.close();
            fr.close();

        } catch (IOException e) {
            System.err.println("FileReader Exception: " + e.toString());
            e.printStackTrace();
        }
    }

    public static void writeFileNameIdTableToFile() {
        try {
            FileWriter fw = new FileWriter(Peer.fileNameIdFile);
            BufferedWriter bfw = new BufferedWriter(fw);

            for (Map.Entry<String, ArrayList<String>> entry : Peer.fileNameIdTable.entrySet()) {
                bfw.write(entry.getKey());
                bfw.write("\n[\n");
                for (int i = 0; i < entry.getValue().size(); i++) {
                    bfw.write(entry.getValue().get(i));
                    bfw.write("\n");
                }
                bfw.write("]\n");
            }

            bfw.close();
            fw.close();

        } catch (IOException e) {
            System.err.println("FileWriter Exception: " + e.toString());
            e.printStackTrace();
        }
    }

    public Peer(String id, String MChost, int MCport, String MDBhost, int MDBport, String MDRhost, int MDRport)
            throws IOException {

        try {
            File stateFile = new File("state.txt");
            if (stateFile.exists()) {
                FileInputStream fi = new FileInputStream(stateFile);
                ObjectInputStream oi = new ObjectInputStream(fi);

                Peer.state = (State) oi.readObject();
                oi.close();
                Peer.STORAGE_SPACE_KB = Peer.getState().getMaxSpace();
            } else {
                Peer.state = new State();
                Peer.STORAGE_SPACE_KB = -1;
                Peer.getState().setMaxSpace(Peer.STORAGE_SPACE_KB);
            }

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error reading state file: " + e.toString());
            e.printStackTrace();
        }

        Peer.id = id;

        executor = Executors.newScheduledThreadPool(250);
        MC = new MCastChannel(MChost, ChannelType.CONTROL, MCport);
        MDB = new MCastChannel(MDBhost, ChannelType.BACKUP, MDBport);
        MDR = new MCastChannel(MDRhost, ChannelType.RESTORE, MDRport);

        chunkStorage = new File("./p2pstorage");
        if (!chunkStorage.exists())
            chunkStorage.mkdir();

        registerChunksInDisk();

        restoreStorage = new File("./p2prestored");
        if (!restoreStorage.exists())
            restoreStorage.mkdir();

        fileNameIdFile = new File("./fileNameId.txt");
        if (!fileNameIdFile.exists())
            fileNameIdFile.createNewFile();

        else
            readFileNameIdTableFromFile();

        executor.execute(MDR);
        executor.execute(MDB);
        executor.execute(MC);

    }

    public static MCastChannel getMC() {
        return Peer.MC;
    }

    public static MCastChannel getMDB() {
        return Peer.MDB;
    }

    public static MCastChannel getMDR() {
        return Peer.MDR;
    }

    public static String getId() {
        return Peer.id;
    }

    public static ScheduledExecutorService getThreadExecutor() {
        return executor;
    }

    public static void addFileNameId(Backup backup, String path) {
        if (Peer.fileNameIdTable.containsKey(path) && !Peer.fileNameIdTable.get(path).contains(backup.getFileId()))
            Peer.fileNameIdTable.get(path).add(backup.getFileId());
        else {
            ArrayList<String> list_names = new ArrayList<String>();
            list_names.add(backup.getFileId());
            fileNameIdTable.put(path, list_names);
        }
    }

    public int restore(String path) {

        /*
         * STEP 0: CHECK IF I CAN REBUILD FILE BASED ON MY CHUNKS (DO NOT SEND GETCHUNKS
         * FOR CHUNKS I HAVE)
         *
         * STEP 1: GET FILE ID FROM PATH
         * 
         * STEP 2: GET NUMBER OF CHUNKS TO REQUEST
         * 
         * STEP 3: SEND GETCHUNK MESSAGES TO GET ALL CHUNKS NEEDED
         * 
         * STEP 4: RECEIVE CHUNK MESSAGES AND REASSEMBLE FILE
         */
        try {

            Restore restore = new Restore(Peer.id, path);

            for (Integer i : restore.getMissingChunks()) {
                String cn = String.valueOf(i);
                Message msg = new Message();
                msg.getChunkMessage("1.0", Peer.id, restore.getFileHash().getFileId(), cn);
                Peer.getThreadExecutor().execute(new MessageSender(ChannelType.RESTORE, msg));
                //MDR.send(msg.getMessage());
                System.out.println(MyUtils.ANSI_CYAN + "\nSENT: " + MyUtils.ANSI_RESET + msg.getStringHeader());
            }
            Future<Integer> result = executor.submit(new FileReAssembler(restore));
            return result.get().intValue();
        } catch (Exception e) {
            System.err.println(MyUtils.ANSI_RED + "\nRESTORE EXCEPTION: " + e.toString());
            e.printStackTrace();
            return -2;
        }

    }

    public int backup(String path, int repDegree) {
        try {
            long confirmationInterval = 1000;
            int NoTries = 0;
            Backup backup = new Backup(Peer.id, path, repDegree);
            Peer.backupFiles.put(backup.getFileId(), new ArrayList<ArrayList<String>>());

            boolean recorded_id = false;

            int i = 0;
            while (i < backup.getNumberChunks()) {
                Message msg = backup.getMessage(i, "PUTCHUNK");
                ArrayList<String> auxInitalizer = new ArrayList<String>();
                Peer.backupFiles.get(backup.getFileId()).add(auxInitalizer);
                Peer.getThreadExecutor().execute(new MessageSender(ChannelType.BACKUP, msg));
                //MDB.send(msg.getMessage());
                System.out.println(MyUtils.ANSI_CYAN + "\nSENT: " + MyUtils.ANSI_RESET + msg.getStringHeader());
                Thread.sleep(confirmationInterval);

                Peer.getState().addInitiatedChunks(backup.getPath(), "CN" + i + "FID" + backup.getFileId(),
                        Peer.backupFiles.get(backup.getFileId()).get(i).size());

                if (Peer.backupFiles.get(backup.getFileId()).get(i).size() < repDegree) {
                    NoTries++;
                    if (NoTries >= MAX_TRIES) {
                        System.out.println(MyUtils.ANSI_RED + MyUtils.ANSI_YELLOW_BACKGROUND + "\n   ERROR: Max tries("
                                + MAX_TRIES + ") of chunk " + i + " reached." + MyUtils.ANSI_RESET);
                        return -1;
                    }
                    confirmationInterval = confirmationInterval * 2;
                    System.out.println(MyUtils.ANSI_RED + "\nReplication degree of chunk " + i + " not accomplished."
                            + MyUtils.ANSI_RESET);
                    System.out.println(MyUtils.ANSI_YELLOW + "Trying again with confirmation interval of: "
                            + confirmationInterval / 1000 + " seconds" + MyUtils.ANSI_RESET);

                    if (Peer.backupFiles.get(backup.getFileId()).get(i).size() != 0)
                        recorded_id = true;
                } else {
                    recorded_id = true;
                    confirmationInterval = 1000;
                    NoTries = 0;

                    // add chunk and desired rep deg to map
                    Peer.getState().addChunksDesiredRepDeg("CN" + i + "FID" + backup.getFileId(), repDegree);

                    i++;
                }
            }

            if (recorded_id)
                addFileNameId(backup, path);

        } catch (IOException e) {
            System.err.println("Backup exception: " + e.toString());
            e.printStackTrace();
            return 1;
        } catch (InterruptedException ex) {
            System.err.println("Backup sleep exception: " + ex.toString());
            ex.printStackTrace();
            return 2;
        }

        return 0;
    }

    public int delete(String path) {
        int ret;
        try {
            Delete delete = new Delete(Peer.id, path);
            if (delete.deleteChunks() > 0) {
                System.out.println(MyUtils.ANSI_GREEN + "\nLocal chunks deleted" + MyUtils.ANSI_RESET);
                ret = 0;
                Peer.getState().removeInitiatedChunks(path);
                Peer.getState().removeInitiatedFileIds(path);
                Peer.getState().removeInitiatedDesiredRep(path);
            } else {
                System.out.println(MyUtils.ANSI_RED + "\nError deleting local chunks" + MyUtils.ANSI_RESET);
                ret = -1;
            }
            Message msg = delete.getMessage();
            for (int i = 0; i < MAX_TRIES; i++) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    System.err.println("Delete sleep exception: " + ex.toString());
                    ex.printStackTrace();
                    ret = -2;
                }
                Peer.getThreadExecutor().execute(new MessageSender(ChannelType.CONTROL, msg));
                //MC.send(msg.getMessage());
                System.out.println(MyUtils.ANSI_CYAN + "\nSENT: " + MyUtils.ANSI_RESET + msg.getStringHeader());
            }
        } catch (IOException e) {
            System.err.println("Delete exception: " + e.toString());
            e.printStackTrace();
            ret = -3;
        }
        chunksBackedUpPerFileId.clear();
        registerChunksInDisk();
        return ret;
    }

    public State state() {
        return Peer.state;
    }

    public static State getState() {
        return Peer.state;
    }

    public static Map<String, ArrayList<ArrayList<String>>> getBackupFiles() {
        return Peer.backupFiles;
    }

    public static File getChunkStorage() {
        return chunkStorage;
    }

    public static Map<String, ArrayList<String>> getFileNameIdTable() {
        return fileNameIdTable;
    }

    public static Map<String, ArrayList<String>> getChunksBackedUpPerFileId() {
        return chunksBackedUpPerFileId;
    }

    public static void registerChunkBackedUp(String fileId, String chunkNo) {
        if (chunksBackedUpPerFileId.containsKey(fileId))
            chunksBackedUpPerFileId.get(fileId).add(chunkNo);

        else {
            ArrayList<String> tmp = new ArrayList<String>();
            tmp.add(chunkNo);
            chunksBackedUpPerFileId.put(fileId, tmp);
        }
    }

    public static void registerChunksInDisk() {
        String[] chunks = chunkStorage.list();
        for (String pathname : chunks) {
            String cn = pathname.substring(2, pathname.indexOf("FID"));
            String fid = pathname.substring(pathname.indexOf("FID") + 3);
            registerChunkBackedUp(fid, cn);
        }
    }

    public int reclaim(long kb) {
        System.out.println(MyUtils.ANSI_GREEN + "\nSETTING STORAGE SPACE TO: " + MyUtils.ANSI_RESET + kb + " KBytes");
        STORAGE_SPACE_KB = kb;
        Peer.getState().setMaxSpace(kb);

        Reclaim r = new Reclaim();

        if (r.getSizeToReclaim() > 0 && kb != -1) {
            r.calculateFilesToDelete();
            while (!r.getFilesToDelete().isEmpty()) {
                String chunkFileName = r.getFilesToDelete().remove();
                String cn = chunkFileName.substring(2, chunkFileName.indexOf("FID"));
                String fid = chunkFileName.substring(chunkFileName.indexOf("FID") + 3);
                File f = new File("./p2pstorage/" + chunkFileName);
                if (!f.delete()) {
                    System.err.println("Can't remove " + f.getAbsolutePath());
                    return -1;
                }

                Message removed = new Message();
                removed.removedMessage("1.0", Peer.id, fid, cn);
                Peer.getThreadExecutor().execute(new MessageSender(ChannelType.CONTROL, removed));
                //Peer.getMC().send(removed.getMessage());

                Peer.getState().getChunksStored().remove(chunkFileName);
                Peer.getState().getChunksPeersStored().get(chunkFileName).remove(Peer.id);

                System.out.println(MyUtils.ANSI_CYAN + "\nSENT: " + MyUtils.ANSI_RESET + removed.getStringHeader());
            }
        }

        return 0;
    }

    public static File getRestoreStorage() {
        return restoreStorage;
    }

    public static long getCurrentStorageOcupation() {
        long length = 0;
        for (File file : chunkStorage.listFiles())
            length += file.length();
        return length;
    }

    public static int storeChunk(Message chunk) {

        if (STORAGE_SPACE_KB == -1
                || STORAGE_SPACE_KB * 1000 >= getCurrentStorageOcupation() + chunk.getBody().length) {
            File chunkToStore = new File(
                    chunkStorage.getPath() + "/CN" + chunk.getChunkNo() + "FID" + chunk.getFileId());
            
            if (chunkToStore.exists()) {
                System.out.println(MyUtils.ANSI_GREEN + "\nCHUNK NO: " + MyUtils.ANSI_RESET + chunk.getChunkNo()
                        + MyUtils.ANSI_GREEN + " FOR FID: " + MyUtils.ANSI_RESET + chunk.getFileId()
                        + " ALREADY EXISTS");
                return -2;
            }

            try {
                chunkToStore.createNewFile();
            } catch (IOException e) {
                System.out.println("Error creating chunk file: " + e.toString());
                return -1;
            }
            try (FileOutputStream fos = new FileOutputStream(chunkToStore.getPath())) {
                fos.write(chunk.getBody());
                fos.close();
            } catch (IOException e) {
                System.out.println("Error storing file: " + e.toString());
                return -1;
            }

            // Verifica se já existe chunks no array de informação de chunks guardados
            if (Peer.getState().getChunksPeersStored()
                    .containsKey("CN" + chunk.getChunkNo() + "FID" + chunk.getFileId())) {

                ArrayList<String> auxList = Peer.getState().getChunksPeersStored()
                        .get("CN" + chunk.getChunkNo() + "FID" + chunk.getFileId());
                if (auxList.equals(null) || !(auxList.contains(Peer.getId()))) {
                    auxList.add(Peer.getId());
                    Peer.getState().getChunksPeersStored().put("CN" + chunk.getChunkNo() + "FID" + chunk.getFileId(),
                            auxList);
                }
            } else {
                ArrayList<String> auxList = new ArrayList<String>();
                auxList.add(Peer.getId());
                Peer.getState().getChunksPeersStored().put("CN" + chunk.getChunkNo() + "FID" + chunk.getFileId(),
                        auxList);
            }

            Peer.getState().addChunksStored("CN" + chunk.getChunkNo() + "FID" + chunk.getFileId(),
                    chunk.getBody().length, Peer.getState().getChunksPeersStored()
                            .get("CN" + chunk.getChunkNo() + "FID" + chunk.getFileId()).size());

            registerChunkBackedUp(chunk.getFileId(), chunk.getChunkNo());
            return chunk.getBody().length;
        }

        else
            System.out.println(MyUtils.ANSI_RED + "\nCAN NOT STORE CHUNK NO: " + MyUtils.ANSI_RESET + chunk.getChunkNo()
                    + MyUtils.ANSI_RED + " FOR FID: " + MyUtils.ANSI_RESET + chunk.getFileId() + MyUtils.ANSI_RED
                    + " EXCEEDS MAXIMUM STORAGE CAPACITY" + MyUtils.ANSI_RESET);

        return -1;
    }

    public static int deleteChunks(Message deleteMessage) {
        Delete delete = new Delete(deleteMessage.getFileId());
        if (delete.deleteChunks() > 0)
            return 1;
        return -1;
    }

    public static void main(String args[]) {

        if (args.length < 6) {
            System.out.println("Input error.");
            System.out.println(
                    "Usage: Peer <rmi port number> <Peer Id> <MC host> <MC port> <MDB host> <MDB port> <MDR host> <MDR port>");
            return;
        }

        int RMIport = Integer.parseInt(args[0]);
        String peerID = args[1];
        String MChost = args[2];
        int MCport = Integer.parseInt(args[3]);
        String MDBhost = args[4];
        int MDBport = Integer.parseInt(args[5]);
        String MDRhost = args[6];
        int MDRport = Integer.parseInt(args[7]);

        try {
            Peer obj = new Peer(peerID, MChost, MCport, MDBhost, MDBport, MDRhost, MDRport);
            RMI stub = (RMI) UnicastRemoteObject.exportObject((Remote) obj, 0);
            try {
                Peer.registry = LocateRegistry.createRegistry(RMIport);
            } catch (RemoteException e) {
                Peer.registry = LocateRegistry.getRegistry(RMIport);
            }

            Peer.registry.bind(args[1], stub);

            System.err.println(MyUtils.ANSI_YELLOW + "\nPeer ready" + MyUtils.ANSI_RESET);
            System.out.println("System IP Address :" + InetAddress.getLocalHost());
        } catch (Exception e) {
            System.err.println("Peer exception: " + e.toString());
            e.printStackTrace();
        }

        /* --------------------Shut down code--------------------------- */
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    FileOutputStream f = new FileOutputStream(new File("state.txt"));
                    ObjectOutputStream o = new ObjectOutputStream(f);
                    o.writeObject(Peer.getState());
                    o.close();
                } catch (IOException e) {
                    System.err.println("Error creating state file: " + e.toString());
                    e.printStackTrace();
                }

                System.out.println(MyUtils.ANSI_RESET + "\nShutting down ...");
                writeFileNameIdTableToFile();
            }
        });
    }
}