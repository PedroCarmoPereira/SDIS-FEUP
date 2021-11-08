import java.io.IOException;
import java.io.Serializable;

public class GetPredecessor implements Serializable {
  private static final long serialVersionUID = 1L;
  
  ChordNodeInfo sender;

  public GetPredecessor() {
    this.sender = ChordNode.getChordInfo();
  }

  private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
    stream.writeObject(sender);
  }

  private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
    sender = (ChordNodeInfo) stream.readObject();
  }

  public void receive() {
    SocketFunctions.createSocketAndSend(sender.getInternetIp(), sender.getPort(), new PossibleSuccessor());
  }
}