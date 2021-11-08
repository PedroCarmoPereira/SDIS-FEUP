import java.io.IOException;
import java.math.BigInteger;

public class RestoreRequest implements java.io.Serializable{
	private static final long serialVersionUID = 1L;

	private BigInteger id;
	private ChordNodeInfo sender;
	private String fileName;

	public RestoreRequest(BigInteger id, String fileName){
		this.id = id;
		this.sender = ChordNode.getChordInfo();
		this.fileName = fileName;
	}

	public BigInteger getId() {
		return id;
	}

	private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
        stream.writeObject(sender);
		stream.writeObject(id);
		stream.writeObject(fileName);
    }

    private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
        sender = (ChordNodeInfo) stream.readObject();
		id = (BigInteger) stream.readObject();
		fileName = (String) stream.readObject();
	}
	
	public void receive(){
		Lookup nl = new Lookup(id);
		int next_node = nl.getNextNode();
        if (next_node == -1) 
            SocketFunctions.createSocketAndSend(sender.getInternetIp(), sender.getPort(), new Restore(this.fileName, this.id));
        else 
            SocketFunctions.createSocketAndSend(ChordNode.getFingerTable().get(next_node).getInternetIp(), ChordNode.getFingerTable().get(next_node).getPort(), this); 
	}
	
}