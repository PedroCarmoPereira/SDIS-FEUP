import java.io.IOException;
import java.io.Serializable;


public class CatchUp implements Serializable {
	ChordNodeInfo sender;

	public CatchUp(){
        this.sender = ChordNode.getChordInfo();
	}
	
	private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
        stream.writeObject(sender);
    }

    private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
        sender = (ChordNodeInfo) stream.readObject();
	}
	
	public void receive(){
		if (SocketFunctions.testConnection(sender.getInternetIp(), sender.getPort()))
			SocketFunctions.createSocketAndSend(sender.getInternetIp(), sender.getPort(), new Up(sender.getId()));
	}
}