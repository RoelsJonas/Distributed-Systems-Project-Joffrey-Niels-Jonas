import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class BulletinBoard extends UnicastRemoteObject implements BulletinBoardIF{
    private  Map<String, byte[]>[] board;
    // TODO PUBLIC SECURE HASHING FUNCTION

    // CONSTRUCTORS
    public BulletinBoard(int n) throws RemoteException {
        super();
        board = new Map[n];
        for(int i = 0; i < n; i++) board[i] = new HashMap<>();
    }

    //FUNCTIONS
    // Add a message to the bulletin board, to map at index index, with key tag and value value
    public void add(int index, byte[] value, String tag) throws RemoteException {
        board[index].put(tag, value);
    }

    // Get a message from map at index with the key being the hash of b
    public byte[] get(int index, String b) throws RemoteException {
        // TODO GET IMPLEMENTEREN
        // byte[] hash = HASH(B);
        byte[] res = null;
//        try{
//            res = board[index].remove(hash);
//        } catch (Exception e) {e.printStackTrace();}
        return res;
    }

    public static void startServer() {
        try {
            Registry registry = LocateRegistry.createRegistry(Main.PORT);
            registry.rebind(Main.SERVERNAME, new BulletinBoard(Main.N));
        } catch (Exception e) { e.printStackTrace(); }
    }
}
