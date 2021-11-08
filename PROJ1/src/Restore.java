import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class Restore {

	private String filePath;
	private String finalName = "";
	private byte[][] chunks;
	private FileHash fileHash;
	private int chunkN;
	private ArrayList<Integer> chunksMissing;
	//TODO: ENHANCEMENT, UMA ARRAY COM PORTAS TCP UMA POR CHUNK, E NA VERSÃO 2 DO PROTOCOLO, OS PEERS EM VEZ DE MANDAREM PARA O MDR MANDAM POR TCP

	public Restore(String PeerId, String filePath) {
		this.filePath = filePath;
		boolean had = false;
		if (Peer.getState().getChunksStored().containsKey(filePath)) {
			ArrayList<String> tmp = Peer.getFileNameIdTable().get(filePath);
			this.fileHash = new FileHash(filePath, tmp.get(tmp.size() - 1));
			had = true;
		}

		else
			this.fileHash = new FileHash(filePath);

		this.chunkN = (int) this.fileHash.getFileAttributes().size() / Backup.MAX_CHUNK_SIZE + 1;
		this.chunks = new byte[this.chunkN][];
		this.chunksMissing = new ArrayList<Integer>();

		for (int i = 0; i < this.chunkN; i++)
			this.chunksMissing.add(i);

		if (had) loadStoredChunks();
		
		String tmp = "";
		for(int i = this.filePath.length() - 1; i >= 0 && this.filePath.charAt(i) != '/'; i--)
			tmp += this.filePath.charAt(i);

		StringBuilder b = new StringBuilder(tmp);
		b = b.reverse();

		this.finalName = b.toString();
	}

	public int getChunkN() {
		return chunkN;
	}

	public FileHash getFileHash() {
		return fileHash;
	}

	public ArrayList<Integer> getMissingChunks() {
		return chunksMissing;
	}

	public byte[][] getChunks() {
		return chunks;
	}

	public void setChunks(int index, byte[] body){
		this.chunks[index] = Arrays.copyOf(body, body.length);
	}

	public String getFinalName(){
		return this.finalName;
	}

	public void loadStoredChunks(){
		System.out.println("Fetching locally stored chunks...");
		if (Peer.getChunksBackedUpPerFileId().containsKey(this.fileHash.getFileId())){
			ArrayList<String> l = Peer.getChunksBackedUpPerFileId().get(this.fileHash.getFileId());
			for (String cns : l){
				for (int i = 0; i < this.chunksMissing.size(); i++)
					if(this.chunksMissing.get(i) == Integer.parseInt(cns)){
						String f = Peer.getChunkStorage().getPath() + "/CN" + cns + "FID" + this.fileHash.getFileId();
						Path p = Paths.get(f);
						try{
						byte [] body = Files.readAllBytes(p);
						this.setChunks(Integer.parseInt(cns), body);
						this.chunksMissing.remove(i);
						break;
						}catch(IOException e){
							System.out.println(MyUtils.ANSI_RED + "\nError failed to read chunk file: " + MyUtils.ANSI_RESET + f);
							e.printStackTrace();
						}
					}
			}
		}

	}

	public void writeFile() {

		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

			for (int i = 0; i < chunks.length; i++) outputStream.write(chunks[i]);

			File f = new File(Peer.getRestoreStorage() +  "/" + this.finalName);
			if(!f.exists()) f.createNewFile();

			byte fullFile[] = outputStream.toByteArray();
			OutputStream out = new FileOutputStream(f);
			out.write(fullFile);
			out.close();
			
		} catch (Exception e) {
			System.out.println(MyUtils.ANSI_RED + "\nError writting file: " + MyUtils.ANSI_RESET);
			e.printStackTrace();
		}
	}
}