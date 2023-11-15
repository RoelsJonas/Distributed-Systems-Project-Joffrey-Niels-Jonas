import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

public interface BulletinBoardIF  extends Remote {


    public void add(int index, byte[] value, String tag) throws RemoteException;

    public byte[] get(int index, String b) throws RemoteException;
}
