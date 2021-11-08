import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMI extends Remote {
    void backup(String path) throws RemoteException;
    void restore(String path) throws RemoteException;
    void delete(String path) throws RemoteException;
    void stick() throws RemoteException;
}