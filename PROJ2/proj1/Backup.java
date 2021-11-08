import java.io.*;
import java.nio.file.*;

class Backup {

    public static int MAX_CHUNK_SIZE = 64000;
    private byte[] file;
    private byte[][] chunks;
    private FileHash fileHash;
    private String filePath;
    private int chunksN;
    private String PeerId;
    private int repDegree;

    private void readFile() {
        Path file = Paths.get(this.filePath);
        try {
            this.file = Files.readAllBytes(file);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void createChunks() {
        this.chunksN = (this.file.length / MAX_CHUNK_SIZE) + 1;

        boolean modul = false;
        if (this.file.length % MAX_CHUNK_SIZE == 0)
            modul = true;

        this.chunks = new byte[this.chunksN][];
        int start = 0;
        for (int i = 0; i < this.chunksN; i++) {

            if (modul && i == this.chunksN - 1) {
                this.chunks[i] = null;
                break;
            }

            if (start + MAX_CHUNK_SIZE > this.file.length) {
                this.chunks[i] = new byte[this.file.length - start];
                System.arraycopy(this.file, start, this.chunks[i], 0, this.file.length - start);
            } else {
                this.chunks[i] = new byte[MAX_CHUNK_SIZE];
                System.arraycopy(this.file, start, this.chunks[i], 0, MAX_CHUNK_SIZE);
            }
            start += MAX_CHUNK_SIZE;
        }
    }

    public int getNumberChunks() {
        return this.chunksN;
    }

    public String getFileId() {
        return this.fileHash.getFileId();
    }

    public String getPath() {
        return this.filePath;
    }

    public Message getMessage(int chunk, String type) throws IOException {
        Message message = new Message();

        if (type.toUpperCase().equals("PUTCHUNK"))
            message.putChunkMessage("1.0", this.PeerId, this.getFileId(), String.valueOf(chunk),
                    String.valueOf(this.repDegree), chunks[chunk]);

        return message;
    }

    public Backup(String PeerId, String path, int repDegree) {
        this.PeerId = PeerId;
        this.filePath = path;
        this.repDegree = repDegree;
        this.readFile();
        this.createChunks();
        this.fileHash = new FileHash(path);

        Peer.getState().addInitiatedFileIds(this.filePath, this.fileHash.getFileId());
        Peer.getState().addInitiatedDesiredRep(this.filePath, this.repDegree);
        Peer.getState().cleanFileRecord(this.filePath);

    }

}