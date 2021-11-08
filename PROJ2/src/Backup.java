import java.io.*;
import java.math.BigInteger;

class Backup implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private ChordNodeInfo sender;
    private byte[] file;
    private String filePath;
    private BigInteger fileId;
    private String fileName;

    public Backup(String path, BigInteger fileId) {
        this.sender = ChordNode.getChordInfo();
        this.filePath = path;
        this.fileId = fileId;
        this.fileName = FileHandler.getFileName(path);
        this.file = FileHandler.readFile(path);
    }

    public void receive() {
        Lookup lookup = new Lookup(this.fileId);
        int nn = lookup.getNextNode();
        if (nn == -1) {
            if (FileHandler.fileExistsInStorage(this.fileId.toString()))
                SocketFunctions.createSocketAndSend(sender.getInternetIp(), sender.getPort(),
                        MyUtils.ANSI_RED + "File id " + this.fileId + " already exists\n" + MyUtils.ANSI_RESET);
            else {
                if (FileHandler.saveFile(this.fileId + "-" + this.fileName, this.file)) {
                    System.out.println(MyUtils.ANSI_GREEN + "File '" + this.fileName + "' backup successful with id "
                            + this.fileId + MyUtils.ANSI_RESET);
                    SocketFunctions.createSocketAndSend(sender.getInternetIp(), sender.getPort(),
                            MyUtils.ANSI_GREEN + "File '" + this.fileName + "' backup successful with id " + this.fileId
                                    + "\n" + MyUtils.ANSI_RESET);
                } else
                    SocketFunctions.createSocketAndSend(sender.getInternetIp(), sender.getPort(),
                            MyUtils.ANSI_RED + "Unable to backup file '" + this.fileName + "', id: " + this.fileId + "\n"
                                    + MyUtils.ANSI_RESET);
            }
        }
        
        else SocketFunctions.createSocketAndSend(Peer.getFingerTable().get(nn).getInternetIp(), Peer.getFingerTable().get(nn).getPort(), this);
    }


    /**
     * @return the file
     */
    public byte[] getFile() {
        return file;
    }

    /**
     * @return the fileId
     */
    public BigInteger getFileId() {
        return fileId;
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
        stream.writeObject(sender);
        stream.writeObject(file);
        stream.writeObject(filePath);
        stream.writeObject(fileId);
        stream.writeObject(fileName);
    }

    private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
        sender = (ChordNodeInfo) stream.readObject();
        file = (byte[]) stream.readObject();
        filePath = (String) stream.readObject();
        fileId = (BigInteger) stream.readObject();
        fileName = (String) stream.readObject();
    }
}