import java.io.IOException;
import java.math.BigInteger;

public class BackupRequest implements java.io.Serializable{
	private static final long serialVersionUID = 1L;

	private BigInteger id;
	private ChordNodeInfo sender;
	private String path;

	public BackupRequest(BigInteger id, String path){
		this.id = id;
		this.sender = ChordNode.getChordInfo();
		this.path = path;
	}

	public BigInteger getId() {
		return id;
	}

	private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
        stream.writeObject(sender);
		stream.writeObject(id);
		stream.writeObject(path);
    }

    private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
        sender = (ChordNodeInfo) stream.readObject();
		id = (BigInteger) stream.readObject();
		path = (String) stream.readObject();
	}
	
	public void receive(){
		Lookup nl = new Lookup(id);
		int next_node = nl.getNextNode();
		if (next_node == -1) SocketFunctions.createSocketAndSend(sender.getInternetIp(), sender.getPort(), new BackupRequestResponse(this.id, this.path));
		else SocketFunctions.createSocketAndSend(ChordNode.getFingerTable().get(next_node).getInternetIp(), ChordNode.getFingerTable().get(next_node).getPort(), this); 
	}
	
}