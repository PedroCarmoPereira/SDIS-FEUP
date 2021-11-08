
class Stabilize extends Thread {

    private static int index = 2;

    public static void recoverSucessor(){
        if (!SocketFunctions.testConnection(ChordNode.getChordInfo().getSuccessor().getInternetIp(), ChordNode.getChordInfo().getSuccessor().getPort())){
            for (int i = 1; i < ChordNodeInfo.getM(); i++)
                if (SocketFunctions.testConnection(ChordNode.getFingerTable().get(i).getInternetIp(), ChordNode.getFingerTable().get(i).getPort())){
                    ChordNode.setIndexFT(0, ChordNode.getFingerTable().get(i));
                    return;
                }
            
            if (ChordNode.getChordInfo().getPredecessor() != null && SocketFunctions.testConnection(ChordNode.getChordInfo().getPredecessor().getInternetIp(), ChordNode.getChordInfo().getPredecessor().getPort()))
                ChordNode.setIndexFT(0, ChordNode.getChordInfo().getPredecessor());
            else{
                System.out.println("Chord Network Error: Reseting Network");
                ChordNode.wipeFT();
                ChordNode.getChordInfo().setPredecessor(null);
            }
        }
    }

    @Override
    public void run() {
        if(ChordNode.getFingerTable() != null){
            if (SocketFunctions.testConnection(ChordNode.getChordInfo().getSuccessor().getInternetIp(), ChordNode.getChordInfo().getSuccessor().getPort())) SocketFunctions.createSocketAndSend(ChordNode.getChordInfo().getSuccessor().getInternetIp(), ChordNode.getChordInfo().getSuccessor().getPort(), new GetPredecessor());

            if(!SocketFunctions.testConnection(Peer.getFingerTable().get(index - 1).getInternetIp(), Peer.getFingerTable().get(index - 1).getPort())){
                ChordNode.setIndexFT(index - 1, ChordNode.getChordInfo().getSuccessor());
            }

            Lookup l = new Lookup(ChordNode.ftIndex(index), ChordNode.getChordInfo(), false);
            l.findSucessor();

            index++;
            if(index > ChordNodeInfo.getM())
                index = 2;
            
            System.out.println("---------------Stabilizing network done---------------");
            if( ChordNode.getChordInfo().getPredecessor() != null)
                System.out.println("Predecessor: " + ChordNode.getChordInfo().getPredecessor().getId());
            if( Peer.getFingerTable() != null)
                for(int i = 0; i < Peer.getFingerTable().size(); i++){
                    System.out.println("S - FT[" + (i + 1) + "]: " + Peer.getFingerTable().get(i).getId());
                }

            recoverSucessor();
        }

        
    }
}