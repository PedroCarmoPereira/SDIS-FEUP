import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMI extends Remote {
    int backup(String path, int repDegree) throws RemoteException;
    int restore(String path) throws RemoteException;
    int reclaim(long kb) throws RemoteException;
    int delete(String path) throws RemoteException;
    State state() throws RemoteException;
}