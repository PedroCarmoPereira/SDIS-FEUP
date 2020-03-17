import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMI extends Remote {
    void backup(String path, int repDegree) throws RemoteException;
}