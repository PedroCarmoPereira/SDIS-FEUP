import java.io.File;
import java.io.IOException;
import java.util.List;

class Delete {
    private String peerId;
    private String filePath;
    private String fileId;
    private FileHash fileHash;


    public Delete(String fileId) {
        this.peerId = null;
        this.filePath = null;
        this.fileId = fileId;
        this.fileHash = new FileHash(null, this.fileId);
    }


    public Delete(String peerId, String path) {
        this.peerId = peerId;
        this.filePath = path;
        this.fileHash = new FileHash(this.filePath);
        this.fileId = this.fileHash.getFileId();
    }

    public int deleteChunks() {
        List<File> files = this.fileHash.getLocalChunks();

        

        for (File file : files) {
            if (!file.delete()) {
                System.err.println("Can't remove " + file.getAbsolutePath());
                return -1;
            }
        }

        Peer.getState().removeChunksDesiredRepDeg(this.fileId);
        Peer.getState().removeChunksPeersStored(this.fileId);
        Peer.getState().removeChunksStored(this.fileId);
        return 1;
    }

    public Message getMessage() throws IOException {
        Message message = new Message();
        message.deleteMessage("1.0", this.peerId, this.fileId);
        return message;
    }

}