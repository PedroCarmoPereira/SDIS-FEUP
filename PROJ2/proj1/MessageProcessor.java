import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

class MessageProcessor extends Thread {

	protected Message msg;
	public String threadId;
	public Boolean send = true;
	public static HashMap<String, MessageProcessor> threads = new HashMap<String, MessageProcessor>();

	public MessageProcessor(byte[] receivedData) {
		this.msg = new Message(receivedData);
	}

	public String getThreadId() {
		return this.threadId;
	}

	public void dontSend() {
		this.send = false;
	}

	public Message getMessage() {
		return msg;
	}

	@Override
	public void run() {
		String fileName;
		String chunkName;
		if (!Peer.getId().equals(this.msg.getSenderId())) // checks if it is the same peer receiving
			switch (this.msg.getMsgType()) {
				case "PUTCHUNK":
					chunkName = "CN" + this.msg.getChunkNo() + "FID" + this.msg.getFileId();
					if (threads.containsKey("REMOVED" + chunkName))
						threads.get("REMOVED" + chunkName).dontSend();
					int aux = Peer.storeChunk(this.msg);
					if (aux >= 0 || aux == -2) {
						try {
							Message confirmation = new Message();
							confirmation.storedMessage("1.0", Peer.getId(), this.msg.getFileId(),
									this.msg.getChunkNo());
							Random randomDelay = new Random();
							Thread.sleep(randomDelay.nextInt(400));
							Peer.getThreadExecutor().execute(new MessageSender(ChannelType.CONTROL, confirmation));
							// Peer.getMC().send(confirmation.getMessage());
							System.out.println(MyUtils.ANSI_CYAN + "\nSENT: " + MyUtils.ANSI_RESET
									+ confirmation.getStringHeader());

							// add chunk and desired rep deg to map
							Peer.getState().addChunksDesiredRepDeg(chunkName, Integer.parseInt(this.msg.getRepDeg()));
						} catch (IOException ex) {
							System.err.println(MyUtils.ANSI_RED + "Error creating stored message: " + MyUtils.ANSI_RESET
									+ ex.toString());
							ex.printStackTrace();
						} catch (InterruptedException e) {
							System.err.println(MyUtils.ANSI_RED + "Error creating random delay: " + MyUtils.ANSI_RESET
									+ e.toString());
							e.printStackTrace();
						}
					}
					break;
				case "STORED":
					String key = this.msg.getFileId();
					// Verifica se já existe chunks no array de informação de chunks guardados
					if (Peer.getState().getChunksPeersStored()
							.containsKey("CN" + this.msg.getChunkNo() + "FID" + this.msg.getFileId())) {
						ArrayList<String> auxList = Peer.getState().getChunksPeersStored()
								.get("CN" + this.msg.getChunkNo() + "FID" + this.msg.getFileId());

						// Se peer não existe nos ids de peers que guardaram o chunk, então adiciona
						if (auxList.equals(null) || !(auxList.contains(this.msg.getSenderId()))) {
							auxList.add(this.msg.getSenderId());
							Peer.getState().getChunksPeersStored()
									.put("CN" + this.msg.getChunkNo() + "FID" + this.msg.getFileId(), auxList);
						}
					} else {
						// cria nova correspondencia para o chunk guardado (no peer que mandou a
						// mensagem)
						ArrayList<String> auxList = new ArrayList<String>();
						auxList.add(this.msg.getSenderId());
						Peer.getState().getChunksPeersStored()
								.put("CN" + this.msg.getChunkNo() + "FID" + this.msg.getFileId(), auxList);
					}

					// atualiza valor de stored em chunks stored
					if (Peer.getState().getChunksStored()
							.containsKey("CN" + this.msg.getChunkNo() + "FID" + this.msg.getFileId()))
						Peer.getState().addChunksStored("CN" + this.msg.getChunkNo() + "FID" + this.msg.getFileId(),
								Peer.getState().getChunksStored()
										.get("CN" + this.msg.getChunkNo() + "FID" + this.msg.getFileId()).get(0),
								Peer.getState().getChunksPeersStored()
										.get("CN" + this.msg.getChunkNo() + "FID" + this.msg.getFileId()).size());

					if (Peer.getBackupFiles().containsKey(key)) {
						if (!Peer.getBackupFiles().get(key).get(Integer.parseInt(this.msg.getChunkNo()))
								.contains(this.msg.getSenderId())) {
							System.out.println(MyUtils.ANSI_GREEN + "\nRECEIVED: " + MyUtils.ANSI_RESET
									+ this.msg.getStringHeader());
							Peer.getBackupFiles().get(key).get(Integer.parseInt(this.msg.getChunkNo()))
									.add(this.msg.getSenderId());
						}
					}
					break;
				case "DELETE":
					if (Peer.deleteChunks(this.msg) > 0) {
						System.out.println(MyUtils.ANSI_GREEN + "\nAll chunks with fileId " + this.msg.getFileId()
								+ " deleted successfully" + MyUtils.ANSI_RESET);
					} else {
						System.out.println(MyUtils.ANSI_RED + "\nError deleting local chunks" + MyUtils.ANSI_RESET);
					}
					break;

				case "GETCHUNK":

					fileName = Peer.getChunkStorage().getPath() + "/CN" + this.msg.getChunkNo() + "FID"
							+ this.msg.getFileId();
					this.threadId = fileName;
					File file = new File(fileName);

					if (file.exists()) {
						Path path = Paths.get(fileName);
						byte[] body;
						Message chunk = new Message();
						try {
							body = Files.readAllBytes(path);
							chunk.chunkMessage("1.0", Peer.getId(), this.msg.getFileId(), this.msg.getChunkNo(), body);
							Random randomDelay = new Random();
							int ttw = randomDelay.nextInt(400);
							threads.put(fileName, this);
							Thread.sleep(ttw);
							if (this.send) {
								Peer.getThreadExecutor().execute(new MessageSender(ChannelType.RESTORE, chunk));
								// Peer.getMDR().send(chunk.getMessage());
								System.out.println(
										MyUtils.ANSI_CYAN + "\nSENT: " + MyUtils.ANSI_RESET + chunk.getStringHeader());
							} else {
								System.out.println(MyUtils.ANSI_YELLOW + "\nCHUNK " + this.msg.getChunkNo()
										+ " SENT BY ANOTHER PEER" + MyUtils.ANSI_RESET);
								Thread.currentThread().interrupt();
							}
							threads.remove(fileName);
						} catch (InterruptedException e) {
							System.err.println(MyUtils.ANSI_RED + "\nError creating random delay: " + MyUtils.ANSI_RESET
									+ e.toString());
							e.printStackTrace();
						} catch (IOException e) {
							System.err.println(
									MyUtils.ANSI_RED + "\nError reading chunk: " + MyUtils.ANSI_RESET + e.toString());
							e.printStackTrace();
						}
					}
					break;

				case "CHUNK":
					fileName = Peer.getChunkStorage().getPath() + "/CN" + this.msg.getChunkNo() + "FID"
							+ this.msg.getFileId();
					if (threads.containsKey(fileName))
						threads.get(fileName).dontSend();
					System.out.println(
							MyUtils.ANSI_GREEN + "\nRECEIVED: " + MyUtils.ANSI_RESET + this.msg.getStringHeader());
					break;

				case "REMOVED":
					System.out.println(
							MyUtils.ANSI_GREEN + "\nRECEIVED: " + MyUtils.ANSI_RESET + this.msg.getStringHeader());
					chunkName = "CN" + this.msg.getChunkNo() + "FID" + this.msg.getFileId();
					threads.put("REMOVED" + chunkName, this);
					if (Peer.getState().getChunksPeersStored().containsKey(chunkName))
						Peer.getState().getChunksPeersStored().get(chunkName).remove(this.msg.getSenderId());

					if (Peer.getState().getChunksStored().containsKey(chunkName))
						Peer.getState().addChunksStored(chunkName,
								Peer.getState().getChunksStored().get(chunkName).get(0),
								Peer.getState().getChunksStored().get(chunkName).get(1) - 1);

					if (Peer.getState().getChunksStored().containsKey(chunkName)
							&& Peer.getState().getChunksDesiredRepDeg().get(chunkName) > Peer.getState()
									.getChunksStored().get(chunkName).get(1)) {
						Path path = Paths.get("./p2pstorage/" + chunkName);
						byte[] body;
						Message m = new Message();
						try {
							body = Files.readAllBytes(path);
							m.putChunkMessage("1.0", Peer.getId(), this.msg.getFileId(), this.msg.getChunkNo(),
									Peer.getState().getChunksDesiredRepDeg().get(chunkName).toString(), body);
							Random randomDelay = new Random();
							int ttw = randomDelay.nextInt(400);
							Thread.sleep(ttw);
							if (this.send) {
								Peer.getThreadExecutor().execute(new MessageSender(ChannelType.BACKUP, m));
								// Peer.getMDB().send(m.getMessage());
								System.out.println(
										MyUtils.ANSI_CYAN + "\nSENT: " + MyUtils.ANSI_RESET + m.getStringHeader());
							} else {
								System.out.println(MyUtils.ANSI_YELLOW + "\nPUTCHUNK " + this.msg.getChunkNo()
										+ " SENT BY ANOTHER PEER" + MyUtils.ANSI_RESET);
								Thread.currentThread().interrupt();
							}

						} catch (InterruptedException e) {
							System.err.println(MyUtils.ANSI_RED + "\nError creating random delay: " + MyUtils.ANSI_RESET
									+ e.toString());
							e.printStackTrace();
						} catch (IOException e) {
							System.err.println(
									MyUtils.ANSI_RED + "\nError reading chunk: " + MyUtils.ANSI_RESET + e.toString());
							e.printStackTrace();
						}
					}
					threads.remove("REMOVED" + chunkName);
					break;
				default:
					System.out.println(this.msg.getMsgType());
					break;
			}
	}
}
