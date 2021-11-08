import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class State implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // backup initiated related
    private LinkedHashMap<String, String> initiatedFileIds = new LinkedHashMap<String, String>();
    private LinkedHashMap<String, Integer> initiatedDesiredRep = new LinkedHashMap<String, Integer>();
    private LinkedHashMap<String, LinkedHashMap<String, Integer>> initiatedChunks = new LinkedHashMap<String, LinkedHashMap<String, Integer>>();

    // chunks stored
    private Map<String, ArrayList<String>> chunksPeersStored = new HashMap<String, ArrayList<String>>();// Key = chunk, Value = lista de peers que guardaram o chunk
    private LinkedHashMap<String, ArrayList<Integer>> chunksStored = new LinkedHashMap<String, ArrayList<Integer>>();//tabela

    private Map<String, Integer> chunksDesiredRepDeg = new HashMap<String, Integer>();// Key = chunk; value = desired rep deg of chunk

    // storage capacity
    private long maxSpace;

    public State() {
        
    }

    public void cleanFileRecord(String pathName) {
        this.initiatedChunks.remove(pathName);
    }

    public void removeChunksDesiredRepDeg(String fileId) {
        ArrayList<String> aux = new ArrayList<String>();

        for (Map.Entry<String, Integer> entry : this.chunksDesiredRepDeg.entrySet()) {
            if(entry.getKey().endsWith(fileId))
                aux.add(entry.getKey());
        }

        for (int i = 0; i < aux.size();i++){
            this.chunksDesiredRepDeg.remove(aux.get(i));
        }
    }

    public void removeChunksPeersStored(String fileId) {
        ArrayList<String> aux = new ArrayList<String>();

        for (Map.Entry<String, ArrayList<String>> entry : this.chunksPeersStored.entrySet()) {
            if(entry.getKey().endsWith(fileId))
                aux.add(entry.getKey());
        }

        for (int i = 0; i < aux.size();i++){
            this.chunksPeersStored.remove(aux.get(i));
        }
    }

    public void removeChunksStored(String fileId) {
        ArrayList<String> aux = new ArrayList<String>();

        for (Map.Entry<String, ArrayList<Integer>> entry : this.chunksStored.entrySet()) {
            if(entry.getKey().endsWith(fileId))
                aux.add(entry.getKey());
        }

        for (int i = 0; i < aux.size();i++){
            this.chunksStored.remove(aux.get(i));
        }
    }

    public Map<String, Integer> getChunksDesiredRepDeg(){
        return this.chunksDesiredRepDeg;
    }

    public void addChunksDesiredRepDeg(String chunkId, int repDeg) {
        this.chunksDesiredRepDeg.put(chunkId, repDeg);
    }

    public void addInitiatedFileIds(String pathName, String fileId) {
        this.initiatedFileIds.put(pathName, fileId);
    }

    public void removeInitiatedFileIds(String pathName){
        this.initiatedFileIds.remove(pathName);
    }

    public void addInitiatedDesiredRep(String pathName, int desiredRep) {
        this.initiatedDesiredRep.put(pathName, desiredRep);
    }

    public void removeInitiatedDesiredRep(String pathName){
        this.initiatedDesiredRep.remove(pathName);
    }

    public void addInitiatedChunks(String pathName, String chunkId, int perceivedRep) {
        if (this.initiatedChunks.containsKey(pathName)) {
            this.initiatedChunks.get(pathName).put(chunkId, perceivedRep);
        } else {
            LinkedHashMap<String, Integer> auxMap = new LinkedHashMap<String, Integer>();
            auxMap.put(chunkId, perceivedRep);
            this.initiatedChunks.put(pathName, auxMap);
        }
    }

    public void removeInitiatedChunks(String pathName){
        this.initiatedChunks.remove(pathName);
    }

    public void addChunksStored(String chunkId, int size, int perceivedRep) {
        ArrayList<Integer> auxList = new ArrayList<Integer>();
        auxList.add(size);
        auxList.add(perceivedRep);
        this.chunksStored.put(chunkId, auxList);
    }

    public void setMaxSpace(long maxSpace) {
        this.maxSpace = maxSpace;
    }

    public LinkedHashMap<String, ArrayList<Integer>> getChunksStored() {
        return this.chunksStored;
    }

    public void removeChunkStored(String cn){
        this.chunksStored.remove(cn);
    }

    public Map<String, ArrayList<String>> getChunksPeersStored() {
        return this.chunksPeersStored;
    }

    public long getMaxSpace() {
        return this.maxSpace;
    }

    private void writeObject(java.io.ObjectOutputStream stream) throws IOException {

        stream.writeObject(initiatedFileIds);
        stream.writeObject(initiatedDesiredRep);
        stream.writeObject(initiatedChunks);
        stream.writeObject(chunksPeersStored);
        stream.writeObject(chunksStored);
        stream.writeObject(chunksDesiredRepDeg);
        stream.writeLong(maxSpace);

    }

    private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {

        initiatedFileIds = (LinkedHashMap<String, String>) stream.readObject();
        initiatedDesiredRep = (LinkedHashMap<String, Integer>) stream.readObject();
        initiatedChunks = (LinkedHashMap<String, LinkedHashMap<String, Integer>>) stream.readObject();
        chunksPeersStored = (HashMap<String, ArrayList<String>>) stream.readObject();
        chunksStored = (LinkedHashMap<String, ArrayList<Integer>>) stream.readObject();
        chunksDesiredRepDeg = (HashMap<String, Integer>) stream.readObject();
        maxSpace = stream.readLong();

    }

    public long calculateUsedSpace(){
        long usedSpace = 0;

        for(ArrayList<Integer> vals : this.chunksStored.values())
            usedSpace += vals.get(0);

        return usedSpace;
    }

    @Override
    public String toString() {
        String result = "";
        long usedSpace = 0;
        result += MyUtils.ANSI_CYAN + "\nBack-ups initiated by this peer:" + MyUtils.ANSI_RESET;
        for (Map.Entry<String, String> entry1 : this.initiatedFileIds.entrySet()) {
            result += MyUtils.ANSI_CYAN + "\nPathName: " + MyUtils.ANSI_RESET + entry1.getKey() + "\n";
            result += MyUtils.ANSI_CYAN + "FileId: " + MyUtils.ANSI_RESET + entry1.getValue() + "\n";
            result += MyUtils.ANSI_CYAN + "Desired replication degree: " + MyUtils.ANSI_RESET
                    + this.initiatedDesiredRep.get(entry1.getKey()) + "\n";
            result += MyUtils.ANSI_YELLOW + "\n\tChunk Id\t\t\t\t\t\t\t\tReplication degree\n" + MyUtils.ANSI_RESET;

            for (Map.Entry<String, Integer> entry2 : this.initiatedChunks.get(entry1.getKey()).entrySet()) {
                if (entry2.getValue() >= this.initiatedDesiredRep.get(entry1.getKey()))
                    result += "\t" + entry2.getKey() + "\t" + MyUtils.ANSI_GREEN + entry2.getValue()
                            + MyUtils.ANSI_RESET + "\n";
                else
                    result += "\t" + entry2.getKey() + "\t" + MyUtils.ANSI_RED + entry2.getValue() + MyUtils.ANSI_RESET
                            + "\n";
            }
        }

        result += MyUtils.ANSI_CYAN + "\nChunks stored in this peer:\n" + MyUtils.ANSI_RESET;
        result += MyUtils.ANSI_YELLOW + "\n\tChunk Id\t\t\t\t\t\t\t\tSize(Kb)\tReplication degree\n" + MyUtils.ANSI_RESET;

        for (Map.Entry<String, ArrayList<Integer>> entry2 : this.chunksStored.entrySet()) {
            usedSpace += entry2.getValue().get(0);
            result += "\t" + entry2.getKey() + "\t" + (float) (entry2.getValue().get(0) / 1000.0) + "\t\t"
                    + entry2.getValue().get(1) + "\n";
        }

        if (this.maxSpace == -1)
            result += MyUtils.ANSI_CYAN + "\nPeer total space: " + MyUtils.ANSI_RESET + "uncapped\n";
        else
            result += MyUtils.ANSI_CYAN + "\nPeer total space: " + MyUtils.ANSI_RESET + this.maxSpace + " Kb \n";

        result += MyUtils.ANSI_CYAN + "Peer used space: " + MyUtils.ANSI_RESET + (float) usedSpace / 1000.0 + " Kb \n";

        return result;
    }
}