import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

class Message {

    private String CRLF;
    private String version;
    private String type;
    private String senderId;
    private String fileId;
    private String chunkNo;
    private String repDeg;
    private byte[] header;
    private byte[] body;
    private byte[] fullMessage;

    public Message() {
        this.CRLF = "\r\n";
    }

    public Message(byte[] msg) {
        this.CRLF = "\r\n";
        int i;
        for (i = 0; i < msg.length - 4; i++) {
            if (msg[i] == 0xD && msg[i + 1] == 0xA && msg[i + 2] == 0xD && msg[i + 3] == 0xA)
                break;
        }

        this.header = Arrays.copyOfRange(msg, 0, i);
        this.body = Arrays.copyOfRange(msg, i + 4, msg.length);

        String fullHeader = new String(this.header);
        String[] headerParts = fullHeader.split(" ");
        this.version = headerParts[0];
        this.type = headerParts[1];
        this.senderId = headerParts[2];
        this.fileId = headerParts[3];

        switch (this.type) {
            case "PUTCHUNK":
                this.chunkNo = headerParts[4];
                this.repDeg = headerParts[5];
                break;
            case "STORED":
                this.chunkNo = headerParts[4];
                this.repDeg = null;
                break;
            case "GETCHUNK":
                this.chunkNo = headerParts[4];
                this.repDeg = null;
                break;
            case "CHUNK":
                this.chunkNo = headerParts[4];
                this.repDeg = null;
                break;
            case "DELETE":
                this.chunkNo = null;
                this.repDeg = null;
                break;
            case "REMOVED":
                this.chunkNo = headerParts[4];
                this.repDeg = null;
                break;
            default:
                break;
        }

    }

    public void putChunkMessage(String version, String senderId, String fileId, String chunkNo, String replicationDeg,
            byte[] body) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        String header = version + " PUTCHUNK " + senderId + " " + fileId + " " + chunkNo + " " + replicationDeg + " "
                + CRLF + CRLF;
        this.type = "PUTCHUNK";
        this.version = version;
        this.senderId = senderId;
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.repDeg = replicationDeg;
        this.header = header.getBytes();
        this.body = body;

        outputStream.write(this.header);
        if (this.body != null) outputStream.write(this.body);

        this.fullMessage = outputStream.toByteArray();
    }

    public void chunkMessage(String version, String senderId, String fileId, String chunkNo,
            byte[] body) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        String header = version + " CHUNK " + senderId + " " + fileId + " " + chunkNo + " " + CRLF + CRLF;
        this.type = "CHUNK";
        this.version = version;
        this.senderId = senderId;
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.repDeg = null;
        this.header = header.getBytes();
        this.body = body;

        outputStream.write(this.header);
        outputStream.write(this.body);

        this.fullMessage = outputStream.toByteArray();
    }

    public void storedMessage(String version, String senderId, String fileId, String chunkNo) throws IOException {

        String header = version + " STORED " + senderId + " " + fileId + " " + chunkNo + " " + CRLF + CRLF;
        this.type = "STORED";
        this.version = version;
        this.senderId = senderId;
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.header = header.getBytes();
        this.repDeg = null;
        this.body = null;

        this.fullMessage = Arrays.copyOf(this.header, this.header.length);
    }

    public void getChunkMessage(String version, String senderId, String fileId, String chunkNo) throws IOException {
        String header = version + " GETCHUNK " + senderId + " " + fileId + " " + chunkNo + " " + CRLF + CRLF;
        this.type = "GETCHUNK";
        this.version = version;
        this.senderId = senderId;
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.header = header.getBytes();
        this.repDeg = null;
        this.body = null;

        this.fullMessage = Arrays.copyOf(this.header, this.header.length);
    }

    public void deleteMessage(String version, String senderId, String fileId) throws IOException {
        String header = version + " DELETE " + senderId + " " + fileId + " " + CRLF + CRLF;
        this.type = "DELETE";
        this.version = version;
        this.senderId = senderId;
        this.fileId = fileId;
        this.header = header.getBytes();
        this.chunkNo = null;
        this.repDeg = null;
        this.body = null;

        this.fullMessage = Arrays.copyOf(this.header, this.header.length);
    }

    public void removedMessage(String version, String senderId, String fileId, String chunkNo){
        String header = version + " REMOVED " + senderId + " " + fileId + " " + chunkNo + " " + CRLF + CRLF;
        this.type = "REMOVED";
        this.version = version;
        this.senderId = senderId;
        this.fileId = fileId;
        this.header = header.getBytes();
        this.chunkNo = chunkNo;
        this.repDeg = null;
        this.body = null;

        this.fullMessage = Arrays.copyOf(this.header, this.header.length);
    }

    public String getStringHeader() {
        String header = new String(this.header);
        header = header.replace("\n", "").replace("\r", "");
        return header;
    }

    public byte[] getHeader() {
        return this.header;
    }

    public byte[] getBody() {
        return this.body;
    }

    public byte[] getMessage() {
        return this.fullMessage;
    }

    public String getVersion() {
        return this.version;
    }

    public String getMsgType() {
        return this.type;
    }

    public String getSenderId() {
        return this.senderId;
    }

    public String getFileId() {
        return this.fileId;
    }

    public String getChunkNo() {
        return this.chunkNo;
    }

    public String getRepDeg() {
        return this.repDeg;
    }

}