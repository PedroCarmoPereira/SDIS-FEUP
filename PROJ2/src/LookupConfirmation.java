import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;

public class LookupConfirmation implements Serializable{
	private static final long serialVersionUID = 1L;
	
	ChordNodeInfo successor;
	ChordNodeInfo requestor;
	BigInteger id;

	public LookupConfirmation(BigInteger id, ChordNodeInfo requestor){
		this.id = id;
		this.requestor = requestor;
		this.successor = ChordNode.getChordInfo();
	}

	private void writeObject(java.io.ObjectOutputStream stream) throws IOException {

		stream.writeObject(successor);
		stream.writeObject(requestor);
		stream.writeObject(id);
    }

    private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {

		successor = (ChordNodeInfo) stream.readObject();
		requestor =( ChordNodeInfo) stream.readObject();

		id = (BigInteger) stream.readObject();
	}

	public void receive(){
		if (requestor.getId().compareTo(ChordNode.getChordInfo().getId()) == 0){
			for(int i = 2; i <= ChordNodeInfo.getM(); i++){
				if(ChordNode.ftIndex(i).compareTo(this.id) == 0){
					ChordNode.setIndexFT(i - 1, this.successor);
				}
			}
		}
		else SocketFunctions.createSocketAndSend(requestor.getInternetIp(), requestor.getPort(), new JoinConfirmation(successor, this.id));
	}

}