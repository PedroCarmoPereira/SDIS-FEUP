import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.PriorityQueue;

class compareFileRepDeg implements Comparator<Map.Entry<String, ArrayList<Integer>>>{

	public int compare(Map.Entry<String, ArrayList<Integer>> a1, Map.Entry<String, ArrayList<Integer>> a2){
		return a2.getValue().get(1) - a1.getValue().get(1);

	}
}

public class Reclaim {

	private long sizeToReclaimInKB;
	private PriorityQueue<String> filesToDelete;

	public Reclaim(){
		this.sizeToReclaimInKB = Peer.getState().calculateUsedSpace()/1000 - Peer.getState().getMaxSpace();
		System.out.println("Using: " + Peer.getState().calculateUsedSpace()/1000 + " KB");
		System.out.println("Allowed: " + Peer.getState().getMaxSpace() + " KB");
		this.filesToDelete = new PriorityQueue<String>();
	}

	public long getSizeToReclaim(){
		return sizeToReclaimInKB;
	}

	public PriorityQueue<String> getFilesToDelete(){
		return filesToDelete;
	}

	public void calculateFilesToDelete(){

		ArrayList<Map.Entry<String, ArrayList<Integer>>> l = new ArrayList<Map.Entry<String, ArrayList<Integer>>>();

		long toReclaim = this.sizeToReclaimInKB;
		for (Map.Entry<String, ArrayList<Integer>> entry : Peer.getState().getChunksStored().entrySet()) {
			int actualRepDeg = entry.getValue().get(1);
			if (Peer.getState().getChunksDesiredRepDeg().containsKey(entry.getKey())){
				int desiredRepDeg = Peer.getState().getChunksDesiredRepDeg().get(entry.getKey());
				if (desiredRepDeg < actualRepDeg){
					this.filesToDelete.add(entry.getKey());
					toReclaim -= entry.getValue().get(0)/1000;
				}

				else l.add(entry);
			}

			else continue;
			
			if (toReclaim <= 0) break;
		}

		l.sort(new compareFileRepDeg());

		int i = 0;
		while(toReclaim > 0 && i < l.size()){
			this.filesToDelete.add(l.get(i).getKey());
			toReclaim -= l.get(i).getValue().get(0)/1000;
			i++;
		}

		System.out.println("Free Space on Disk, after RECLAIM: " + (0 - toReclaim) + " KB");

	}

}