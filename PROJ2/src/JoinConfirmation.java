import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class JoinConfirmation implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	ChordNodeInfo successor;
	BigInteger searcher;

	public JoinConfirmation(ChordNodeInfo succ, BigInteger searcher){
		this.successor = succ;
		this.searcher = searcher;
		
	}

	private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
		stream.writeObject(successor);
		stream.writeObject(searcher);
    }

    private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
		successor = (ChordNodeInfo) stream.readObject();
		searcher = (BigInteger) stream.readObject();
	}
	
	public void receive(){
		 if(successor.getPredecessor() == null || successor.getPredecessor().getId().compareTo(ChordNode.getChordInfo().getId()) == 0)
			Peer.getChordInfo().setPredecessor(successor);
		else
			Peer.getChordInfo().setPredecessor(successor.getPredecessor());
		Peer.initFT(this.successor);

		SocketFunctions.createSocketAndSend(this.successor.getInternetIp(), this.successor.getPort(), new CatchUp());		
	}
}