import java.io.IOException;
import java.io.Serializable;

public class PossiblePredecessor implements Serializable {
    private static final long serialVersionUID = 1L;
    
    ChordNodeInfo sender;

    public PossiblePredecessor(){
        this.sender = ChordNode.getChordInfo();
    }

	private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
        stream.writeObject(sender);
    }

    private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
        sender = (ChordNodeInfo) stream.readObject();
    }
    
    public void receive(){

        if(ChordNode.getChordInfo().getPredecessor() != null &&
            !SocketFunctions.testConnection(ChordNode.getChordInfo().getPredecessor().getInternetIp(), ChordNode.getChordInfo().getPredecessor().getPort())){
                ChordNode.getChordInfo().setPredecessor(null);

            }
        

        if(ChordNode.getChordInfo().getPredecessor() == null ||
        (sender.getId().compareTo(ChordNode.getChordInfo().getId()) < 0 && sender.getId().compareTo(ChordNode.getChordInfo().getPredecessor().getId()) > 0) ||
        (sender.getId().compareTo(ChordNode.getChordInfo().getId()) > 0 && sender.getId().compareTo(ChordNode.getChordInfo().getPredecessor().getId()) > 0 && ChordNode.getChordInfo().getPredecessor().getId().compareTo(ChordNode.getChordInfo().getId()) > 0) ||
        (sender.getId().compareTo(ChordNode.getChordInfo().getId()) < 0 && sender.getId().compareTo(ChordNode.getChordInfo().getPredecessor().getId()) < 0 && ChordNode.getChordInfo().getId().compareTo(ChordNode.getChordInfo().getPredecessor().getId()) < 0)
        ) ChordNode.getChordInfo().setPredecessor(sender);


    }
}