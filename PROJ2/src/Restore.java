import java.io.*;
import java.math.BigInteger;

class Restore implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private ChordNodeInfo sender;
    private byte[] file;
    private BigInteger fileId;
    private String fileName;

    public Restore(String fileName, BigInteger fileId) {
        this.sender = ChordNode.getChordInfo();
        this.fileId = fileId;
        this.fileName = fileName;
        this.file = FileHandler.readFile("./storage/" + this.fileId + "-" + this.fileName);
    }

    public void receive() {
        Lookup lookup = new Lookup(this.fileId);
        int nn = lookup.getNextNode();
            if(file == null){
                System.out.println(MyUtils.ANSI_RED + "Couldn't find file with id " + this.fileId + MyUtils.ANSI_RESET); 
                return;
            }
            
            if (FileHandler.fileExistsInRestored(this.fileId.toString()))
                System.out.println(MyUtils.ANSI_YELLOW + "File with id " + this.fileId + " was already restored before\n"
                        + MyUtils.ANSI_RESET);
            else {
                if (FileHandler.restoreFile(this.fileId + "-" + this.fileName, this.file)) {
                    System.out.println(MyUtils.ANSI_GREEN + "File '" + this.fileId + "-" + this.fileName + "' restored successful"
                            + MyUtils.ANSI_RESET);
                } else
                    System.out.println(MyUtils.ANSI_RED + "Unable to restore file '" + this.fileName + "', id: "
                            + this.fileId + "\n" + MyUtils.ANSI_RESET);
            }
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
        stream.writeObject(fileId);
        stream.writeObject(fileName);
    }

    private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
        sender = (ChordNodeInfo) stream.readObject();
        file = (byte[]) stream.readObject();
        fileId = (BigInteger) stream.readObject();
        fileName = (String) stream.readObject();
    }
}