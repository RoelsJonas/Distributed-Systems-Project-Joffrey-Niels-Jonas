import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class BulletinBoard extends UnicastRemoteObject implements BulletinBoardIF{
    private static Map<String, Message>[] board;
    private static MessageDigest secureHash;

    // CONSTRUCTORS
    public BulletinBoard(int n) throws RemoteException, NoSuchAlgorithmException {
        super();
        board = new Map[n];
        for(int i = 0; i < n; i++) board[i] = new HashMap<>();
        try {
            secureHash = MessageDigest.getInstance(BulletinBoardIF.SECUREHASHINGALOGRITHM);
        } catch (Exception e) {e.printStackTrace();}
    }

    //FUNCTIONS
    // Add a message to the bulletin board, to map at index, with key tag and value
    public void add(int index, Message value, byte[] tag) throws RemoteException {
//        System.out.println("Adding message: " + new String(value.getBytes()) + " at: " + index + ", with tag: " + Arrays.toString(tag));
        board[index].put(Arrays.toString(tag), value);

        storeRecoveryData();
    }

    // Get a message from map at index with the key being the hash of b
    public Message get(int index, byte[] b) throws RemoteException {
        // get the hash of the given tag
        byte[] tag = secureHash.digest(b);
//        System.out.println("Getting message at: " + index + ", with tag: " + Arrays.toString(tag));
        // return null if no message with that tag is found
        // else remove the message from the board and return that
        Message res = null;
        try{
            res = board[index].remove(Arrays.toString(tag));
//            System.out.println(new String(res.getBytes()));
        } catch (Exception e) {e.printStackTrace();}

        storeRecoveryData();
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

    public static void storeRecoveryData() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("RecoveryData/Board"))) {
            for (int index = 0; index < board.length; index++) {
                for (Map.Entry<String, Message> entry : board[index].entrySet()) {
                    String tag = entry.getKey();
                    Message message = entry.getValue();

                    byte[] bytes = message.getBytes();
                    byte[] ap = message.getAp();

                    writer.write(index + ";"); 
                    writer.write(tag + ";");
                    writer.write(Arrays.toString(bytes) + ";"); 
                    writer.write(Arrays.toString(ap) + ";");
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void recoverBoard() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader("RecoveryData/Board"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(";");
                int index = Integer.parseInt(data[0]);
                String tag = data[1];
                byte[] bytes = parseByteArray(data[2]);
                byte[] ap = parseByteArray(data[3]);

                Message message = new Message(bytes, ap);
                board[index].put(tag, message);
            }
        } catch (IOException e) {
           throw new IOException("No recovery data found");
        }
    }

    private static byte[] parseByteArray(String data) {
        String[] bytes = data.substring(1, data.length() - 1).split(", ");
        byte[] byteArray = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            byteArray[i] = Byte.parseByte(bytes[i]);
        }
        return byteArray;
    }
}
