import java.io.IOException;
import java.math.BigInteger;

public class Join implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    //request peer info
    private BigInteger senderNodeId;
    private String senderIp;
    private int senderPort;

    public Join(String senderIp, int senderPort, BigInteger sni){
        this.senderIp = senderIp;
        this.senderPort = senderPort;
        this.senderNodeId = sni;
    }

    private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
        stream.writeObject(senderIp);
        stream.writeObject(senderPort);
        stream.writeObject(senderNodeId);
    }

    private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
        senderIp = (String) stream.readObject();
        senderPort = (int) stream.readObject();
        senderNodeId = (BigInteger) stream.readObject();
    }

    public void receive(){
        System.out.println(MyUtils.ANSI_CYAN + "Received join request from node " + senderNodeId + MyUtils.ANSI_RESET);
        ChordNodeInfo newnode = new ChordNodeInfo(senderNodeId, senderIp, senderPort, null);

        if (newnode.getId().compareTo(ChordNode.getChordInfo().getId()) == 0){
            SocketFunctions.createSocketAndSend(senderIp, senderPort, MyUtils.ANSI_RED + "Join Failed: Id already in use, try changing the port yielded to Chord.\n" + MyUtils.ANSI_RESET);
            return;
        }

        if (ChordNode.getChordInfo().getPredecessor() == null && ChordNode.getFingerTable() == null){
            ChordNode.initFT(newnode);
            ChordNode.getChordInfo().setPredecessor(newnode);
            SocketFunctions.createSocketAndSend(senderIp, senderPort, new JoinConfirmation(ChordNode.getChordInfo(), senderNodeId));
            
            return;
        } else{
            Lookup l = new Lookup(senderNodeId, ChordNode.getChordInfo(), newnode, true);
            l.findSucessor();
        }

        

        /*
        1. PC1 inicia a rede, FT1 e Predecessor1 são NULL
        1.1 Se um PC receber um pedido de join para um id igual ao seu enviar msg de erro
        2. PC2 manda join, PC1 mete-o como Pred e mete-o na FT em todos os indices -> Envia Msg ao PC2 a avisar-lhe que tem de fazer o mm
        3. Chega PC3, pede join a qq um dos pcs
            3.1 Se o Id do PC3 é menor que o de quem recebeu o pedido (PCRecetor PCR), PC3.id < PCR então "chegamos" onde queremos, PCR mete o PC3 como seu predecessor e envia-lhe as infos que ele precisa 
            3.2 Caso contrário, PCR percore a sua FT do index mais alto para o mais baixo até encontrar um que seja PCRFT[i].id <= PC3.id e encaminha o pedido
        */

        //

    }
}
