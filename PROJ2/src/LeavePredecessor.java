import java.io.IOException;
import java.io.Serializable;

public class LeavePredecessor implements Serializable{
	private static final long serialVersionUID = 1L;
	
	ChordNodeInfo newSucessor;

	public LeavePredecessor(ChordNodeInfo ns){
		newSucessor = ns;
	}

	
	private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
		stream.writeObject(newSucessor);
    }

    private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
		newSucessor = (ChordNodeInfo) stream.readObject();
	}
	
	public void receive(){
		if (newSucessor.getId().compareTo(ChordNode.getChordInfo().getId()) != 0 && SocketFunctions.testConnection(newSucessor.getInternetIp(), newSucessor.getPort()))
			ChordNode.setIndexFT(0, newSucessor);
		else if (newSucessor.getId().compareTo(ChordNode.getChordInfo().getId()) == 0) {
			ChordNode.wipeFT();
			ChordNode.getChordInfo().setPredecessor(null);
		}
	}
}