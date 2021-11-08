import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class PossibleSuccessor implements Serializable {
    private static final long serialVersionUID = 1L;
    
    ChordNodeInfo sender;
    ChordNodeInfo successor;


    public PossibleSuccessor(){
        this.sender = ChordNode.getChordInfo();
        this.successor = ChordNode.getChordInfo().getPredecessor();
    }

	private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
        stream.writeObject(sender);
        stream.writeObject(successor);
    }

    private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
        sender = (ChordNodeInfo) stream.readObject();
        successor = (ChordNodeInfo) stream.readObject();
    }
    
    public void receive(){
        if(successor == null){
            SocketFunctions.createSocketAndSend(ChordNode.getFingerTable().get(0).getInternetIp(), ChordNode.getFingerTable().get(0).getPort(), new PossiblePredecessor());
            return;
        }

        if((successor.getId().compareTo(ChordNode.getChordInfo().getId()) > 0 && successor.getId().compareTo(sender.getId()) < 0) ||
        (successor.getId().compareTo(ChordNode.getChordInfo().getId()) > 0 && successor.getId().compareTo(sender.getId()) > 0 && ChordNode.getChordInfo().getId().compareTo(sender.getId()) > 0) ||
        (successor.getId().compareTo(ChordNode.getChordInfo().getId()) < 0 && successor.getId().compareTo(sender.getId()) < 0 && sender.getId().compareTo(ChordNode.getChordInfo().getId()) < 0)
        ) {
            if (SocketFunctions.testConnection(successor.getInternetIp(), successor.getPort()))
                ChordNode.setIndexFT(0, successor);
        }
        SocketFunctions.createSocketAndSend(ChordNode.getFingerTable().get(0).getInternetIp(), ChordNode.getFingerTable().get(0).getPort(), new PossiblePredecessor());
    }
}