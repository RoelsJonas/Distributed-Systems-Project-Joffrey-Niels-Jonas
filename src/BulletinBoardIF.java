import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public interface BulletinBoardIF  extends Remote {
    public static final String SECUREHASHINGALOGRITHM = "SHA-256";
    public void add(int index, Message value, byte[] tag) throws RemoteException;

    public Message get(int index, byte[] b) throws RemoteException;
}
