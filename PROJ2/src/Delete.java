import java.io.*;
import java.math.BigInteger;

class Delete implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private ChordNodeInfo sender;
    private BigInteger fileId;
    private String fileName;

    public Delete(String fileName, BigInteger fileId) {
        this.sender = ChordNode.getChordInfo();
        this.fileId = fileId;
        this.fileName = fileName;
        if(FileHandler.deleteFileFromStorage(this.fileId + "-" + this.fileName)){
            System.out.println(MyUtils.ANSI_GREEN + "File '" + this.fileName + "' with id " + this.fileId + " deleted"
            + "\n" + MyUtils.ANSI_RESET);
        }
    }

    public void receive() {
        Lookup lookup = new Lookup(this.fileId);
        int nn = lookup.getNextNode();
        if (nn == -1) {
            if(FileHandler.deleteFileFromStorage(this.fileId + "-" + this.fileName)){
                System.out.println(MyUtils.ANSI_GREEN + "File '" + this.fileName + "' with id " + this.fileId + " deleted"
                + "\n" + MyUtils.ANSI_RESET);
                SocketFunctions.createSocketAndSend(sender.getInternetIp(), sender.getPort(),
                MyUtils.ANSI_GREEN + "File '" + this.fileName + "' with id " + this.fileId + " deleted"
                    + "\n" + MyUtils.ANSI_RESET);
            }
        }
        
        else SocketFunctions.createSocketAndSend(Peer.getFingerTable().get(nn).getInternetIp(), Peer.getFingerTable().get(nn).getPort(), this);
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
        stream.writeObject(fileId);
        stream.writeObject(fileName);
    }

    private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
        sender = (ChordNodeInfo) stream.readObject();
        fileId = (BigInteger) stream.readObject();
        fileName = (String) stream.readObject();
    }
}