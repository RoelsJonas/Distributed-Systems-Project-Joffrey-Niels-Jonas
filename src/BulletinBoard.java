import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class BulletinBoard extends UnicastRemoteObject implements BulletinBoardIF{
    private  Map<String, Message>[] board;
    private MessageDigest secureHash;

    // CONSTRUCTORS
    public BulletinBoard(int n) throws RemoteException {
        super();
        board = new Map[n];
        for(int i = 0; i < n; i++) board[i] = new HashMap<>();
        try {
            secureHash = MessageDigest.getInstance(BulletinBoardIF.SECUREHASHINGALOGRITHM);
        } catch (Exception e) {e.printStackTrace();}
    }

    //FUNCTIONS
    // Add a message to the bulletin board, to map at index, with key tag and value
    public void add(int index, byte[] value, byte[] tag) throws RemoteException {
//        System.out.println("Adding message: " + new String(value) + " at: " + index + ", with tag: " + Arrays.toString(tag));
        board[index].put(Arrays.toString(tag), new Message(value));
    }

    // Get a message from map at index with the key being the hash of b
    public byte[] get(int index, byte[] b) throws RemoteException {
        // get the hash of the given tag
        byte[] tag = secureHash.digest(b);
//        System.out.println("Getting message at: " + index + ", with tag: " + Arrays.toString(tag));
        // return null if no message with that tag is found
        // else remove the message from the board and return that
        byte[] res = null;
        try{
            Message m = board[index].remove(Arrays.toString(tag));
            res = m.getBytes();
//            System.out.println(new String(res));
        } catch (Exception e) {e.printStackTrace();}
        return res;
    }

    // Start the RMI server
    public static void startServer() {
        try {
            Registry registry = LocateRegistry.createRegistry(Main.PORT);
            registry.rebind(Main.SERVERNAME, new BulletinBoard(Main.N));
            System.out.println("Server started on port: " + Main.PORT);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
