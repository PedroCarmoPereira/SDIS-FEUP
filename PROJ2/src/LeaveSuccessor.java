import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class LeaveSuccessor implements Serializable{
	private static final long serialVersionUID = 1L;
	
	HashMap<String, byte[]> files;
	BigInteger leaverNodeId;

	public LeaveSuccessor(){
		this.leaverNodeId = ChordNode.getChordInfo().getId();
		this.files = FileHandler.getStoredFiles(">=", new BigInteger("0"));
		FileHandler.deleteFilesFromStorage(this.files);
	}
	
	private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
		stream.writeObject(files);
		stream.writeObject(leaverNodeId);

    }

    private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
		files = (HashMap<String, byte[]>) stream.readObject();
		leaverNodeId = (BigInteger) stream.readObject();
	}
	
	public void receive(){
		ChordNode.getChordInfo().setPredecessor(null);
		if(files.size() == 0){
			System.out.println(MyUtils.ANSI_CYAN + "Node " + this.leaverNodeId + " left... Received no files." + MyUtils.ANSI_RESET);
			return;
		}
		
		System.out.println(MyUtils.ANSI_CYAN + "Node " + this.leaverNodeId + " left... Received files:" + MyUtils.ANSI_RESET);

		for (Map.Entry<String, byte[]> file : files.entrySet()) {
			FileHandler.saveFile(file.getKey(), file.getValue());
			System.out.println(MyUtils.ANSI_CYAN + "   -> " + file.getKey() + MyUtils.ANSI_RESET);
		}

	}
}