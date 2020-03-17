
class Message {

    private String CRLF;
    private String msgOption;
    private String version;
    private String messageType;
    private String senderId;
    private String fileId;
    private String chunkNo;
    private String replicationDeg;
    private String header;
    private String body;

    public Message() {
        this.CRLF =  "\r\n";
    }

    public Message(String msgOption, String version, String messageType, String senderId, String fileId, String chunkNo, String replicationDeg, String body) {
        this.CRLF =  "\r\n";
        this.msgOption = msgOption;
        this.version = version;
        this.messageType = messageType;
        this.senderId = senderId;
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.replicationDeg = replicationDeg;
        this.body = body;
    }

    private void createHeader(){
        this.header = this.version + " " + this.msgOption + " " + this.messageType + " " + this.senderId + " " + this.fileId + " " + this.chunkNo + " " + this.replicationDeg + " " + this.CRLF + this.CRLF;
    }
}