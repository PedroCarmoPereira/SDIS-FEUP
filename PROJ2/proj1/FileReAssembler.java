import java.util.ArrayList;
import java.util.concurrent.Callable;

public class FileReAssembler implements Callable<Integer> {

	private Restore restore;

	public FileReAssembler(Restore res){
		restore = res;
	}

	@Override
	public Integer call() {
		try{
		Thread.sleep(5000); // Wait 5 secs before restore
		} catch (InterruptedException ex){
			ex.printStackTrace();
		}
		//EXIT CONDITION OTHER THAN GETTING ALL FILES
		long start = System.currentTimeMillis();
		long curr = System.currentTimeMillis();
		long timeElapsed = curr - start;
		while(!restore.getMissingChunks().isEmpty() && timeElapsed < 30000){
			for(int i = 0; i < restore.getMissingChunks().size(); i++){
				ArrayList<Message> log = Peer.getMDR().log;
				for(int j = 0; j < log.size(); j++){

					if (!log.get(j).getMsgType().equals("CHUNK")){
						log.remove(j);
						break;
					}


					if (log.get(j).getFileId().equals(restore.getFileHash().getFileId()) &&
					 Integer.parseInt(log.get(j).getChunkNo()) == restore.getMissingChunks().get(i)){

						restore.setChunks(restore.getMissingChunks().get(i), log.get(j).getBody());
						log.remove(j);
						restore.getMissingChunks().remove(i);
						break;

					 }
				}
			}
			curr = System.currentTimeMillis();
			timeElapsed = curr - start;
		}
		
		if (timeElapsed >= 5000){
			System.out.println(MyUtils.ANSI_RED + "\nTIMEOUT RESTORE FOR: " + MyUtils.ANSI_RESET + restore.getFinalName() + MyUtils.ANSI_RED + " FAILED" + MyUtils.ANSI_RESET);
			return -1;
		}
		else {
			System.out.println(MyUtils.ANSI_GREEN + "\nREBUILDING: " + MyUtils.ANSI_RESET + restore.getFinalName());
			restore.writeFile();
			System.out.println(MyUtils.ANSI_GREEN + "\nBUILD: " + MyUtils.ANSI_RESET + restore.getFinalName() + MyUtils.ANSI_GREEN + " DONE" + MyUtils.ANSI_RESET);
			return 0;
		}

	}
}