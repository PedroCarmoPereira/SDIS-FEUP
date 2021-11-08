import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;


public class Up implements Serializable {
	BigInteger searcher;
	HashMap<String, byte[]> files;

	public Up(BigInteger searcher){
		this.searcher = searcher;
		this.files = FileHandler.getStoredFiles("<=", searcher);
		FileHandler.deleteFilesFromStorage(this.files);
	}
	
	private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
		stream.writeObject(searcher);
		stream.writeObject(files);
		
    }

    private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
		searcher = (BigInteger) stream.readObject();
		files = (HashMap<String, byte[]>) stream.readObject();
	}
	
	public void receive(){
		if(files.size() > 0)
			System.out.println(MyUtils.ANSI_CYAN + "Joining network... Received files:" + MyUtils.ANSI_RESET);
		for (Map.Entry<String, byte[]> file : files.entrySet()) {
			FileHandler.saveFile(file.getKey(), file.getValue());
			System.out.println(MyUtils.ANSI_CYAN + "   -> " + file.getKey() + MyUtils.ANSI_RESET);
		}

		System.out.println(MyUtils.ANSI_GREEN + "\nPeer ready\tID:" + ChordNode.getChordInfo().getId() + MyUtils.ANSI_RESET);
		
	}
}