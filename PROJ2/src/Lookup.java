import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;

public class Lookup implements Serializable {
	private static final long serialVersionUID = 1L;

	ChordNodeInfo sender;
	ChordNodeInfo requestor;
	BigInteger id;
	boolean isJoin;

	public Lookup(BigInteger id){
		this.sender = ChordNode.getChordInfo();
		this.requestor = null;
		this.id = id;
		this.isJoin = false;
	}

	public Lookup(BigInteger id, ChordNodeInfo sender, boolean isJoin) {
		this.id = id;
		this.sender = sender;
		this.requestor = sender;
		this.isJoin = isJoin;
	}

	public Lookup(BigInteger id, ChordNodeInfo sender, ChordNodeInfo requestor, boolean isJoin) {
		this.id = id;
		this.sender = sender;
		this.requestor = requestor;
		this.isJoin = isJoin;
	}

	private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
		stream.writeObject(sender);
		stream.writeObject(requestor);
		stream.writeObject(id);
	}

	private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
		sender = (ChordNodeInfo) stream.readObject();
		requestor = (ChordNodeInfo) stream.readObject();
		id = (BigInteger) stream.readObject();
	}

	public void findSucessor() {

		int indoce = this.getNextNode();
		switch (indoce) {
			case -1:
				if (ChordNode.getChordInfo().getId().compareTo(sender.getId()) == 0
						&& ChordNode.getChordInfo().getId().compareTo(requestor.getId()) == 0
						&& sender.getId().compareTo(requestor.getId()) == 0) {
					int i = 2;
					if (this.isJoin)
						i = 1;
					for (; i <= ChordNodeInfo.getM(); i++) {
						if (ChordNode.ftIndex(i).compareTo(this.id) == 0) {
							ChordNode.setIndexFT(i - 1, ChordNode.getChordInfo());
						}
					}
				} else if (sender.getId().compareTo(ChordNode.getChordInfo().getId()) == 0)
					SocketFunctions.createSocketAndSend(this.requestor.getInternetIp(), this.requestor.getPort(),
							new JoinConfirmation(ChordNode.getChordInfo(), this.id));
				else
					if (SocketFunctions.testConnection(sender.getInternetIp(), sender.getPort()))
						SocketFunctions.createSocketAndSend(sender.getInternetIp(), sender.getPort(),
							new LookupConfirmation(id, this.requestor));
				break;
			default:
				if (SocketFunctions.testConnection(Peer.getFingerTable().get(indoce).getInternetIp(), Peer.getFingerTable().get(indoce).getPort()))
					SocketFunctions.createSocketAndSend(Peer.getFingerTable().get(indoce).getInternetIp(),
							Peer.getFingerTable().get(indoce).getPort(), this);
				break;

		}

	}

	// returna id da finger table ou -1 caso sejamos nós o sucessor;
	public int getNextNode() {
		// somos nós o sucessor
		if (ChordNode.getChordInfo().getPredecessor() != null && 
		((ChordNode.getChordInfo().getId().compareTo(this.id) == 0 ||
		// caso normal
				(id.compareTo(ChordNode.getChordInfo().getId()) < 0
						&& id.compareTo(ChordNode.getChordInfo().getPredecessor().getId()) > 0))
				||
				// caso dar a volta
				ChordNode.getChordInfo().getPredecessor().getId().compareTo(ChordNode.getChordInfo().getId()) > 0
						&& (ChordNode.getChordInfo().getId().compareTo(id) > 0
								|| ChordNode.getChordInfo().getPredecessor().getId().compareTo(id) < 0))) {
			return -1;
		}
		// não somos nós o sucessor
		else {

			for (int i = 0; i < Peer.getFingerTable().size(); i++) {
				if ((Peer.getFingerTable().get(i).getId().compareTo(id) >= 0
						&& ChordNode.getChordInfo().getId().compareTo(Peer.getFingerTable().get(i).getId()) < 0)
						|| (ChordNode.getChordInfo().getId().compareTo(Peer.getFingerTable().get(i).getId()) > 0
								&& (this.id.compareTo(ChordNode.getChordInfo().getId()) > 0
										|| this.id.compareTo(Peer.getFingerTable().get(i).getId()) <= 0))) {
					if (i == 0) {
						return 0;
					} else {
						while (!SocketFunctions.testConnection(Peer.getFingerTable().get(i - 1).getInternetIp(),
								Peer.getFingerTable().get(i - 1).getPort())) {
							if (i == 1)
								break;
							i--;
						}
						return i - 1;
					}
				}

			}

			return ChordNodeInfo.getM() - 1;
		}
	}

}