
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.AlgorithmParameters;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class Client {
    private Map<String, Contact> contacts;
    private final String name;
    private BulletinBoardIF board;
    private final SecureRandom srng;
    private MessageDigest secureHash;
    private Cipher cipher;

    // CONSTRUCTORS
    public Client(String name) {
        this.name = name;
        contacts = new HashMap<>();
        srng = new SecureRandom();

        // initialize the hash object
        try {
            secureHash = MessageDigest.getInstance(BulletinBoardIF.SECUREHASHINGALOGRITHM);
        } catch (Exception e) {e.printStackTrace();}

        // Connect to bulletin board
        try {
            Registry myRegistry = LocateRegistry.getRegistry("localhost", Main.PORT);
            board = (BulletinBoardIF) myRegistry.lookup(Main.SERVERNAME);
        } catch (Exception e) {e.printStackTrace();}

        try {
            cipher = Cipher.getInstance("AES/GCM/NOPADDING");
        } catch(Exception e) {e.printStackTrace();}

    }

    // FUNCTIONS
    // Send a message to a contact
    public void send(String contactName, String message) {
        Contact contact = contacts.getOrDefault(contactName, null);
        if(contact == null) return;

        int idxNew = srng.nextInt(Main.N);
        byte[] tagNew = new byte[Main.TAG_LENGTH];
        srng.nextBytes(tagNew);

        // get the message, index and tag as separate byte arrays
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buff = ByteBuffer.allocate(Main.IDX_LENGTH);
        buff.putInt(idxNew);
        byte[] idxBytes = buff.array();

        // combine the 3 byte arrays
        byte[] combinedBytes = new byte[messageBytes.length + idxBytes.length + tagNew.length];
        System.arraycopy(messageBytes, 0, combinedBytes, 0, messageBytes.length);
        System.arraycopy(idxBytes, 0, combinedBytes, messageBytes.length, idxBytes.length);
        System.arraycopy(tagNew, 0, combinedBytes, messageBytes.length + idxBytes.length, tagNew.length);

        byte[] tagDigest = secureHash.digest(contact.getTagSend());


        try {
            byte[] iv = new byte[12];
            srng.nextBytes(iv);
            GCMParameterSpec ivSpec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.ENCRYPT_MODE, contact.getKeySend(), ivSpec);
            byte[] encryptedBytes = cipher.doFinal(combinedBytes);


            // Try to add the encrypted message to the board if succeeded set tags to new tags and derive the next key
            board.add(contact.getIdxSend(), new Message(encryptedBytes, iv), tagDigest);
            contact.setIdxSend(idxNew);
            contact.setTagSend(tagNew);
            contact.deriveNextKeySend();
        } catch(Exception e) {e.printStackTrace();}



    }

    // Receive a message from a contact
    public String receive(String contactName) {
        Contact contact = contacts.getOrDefault(contactName, null);
        if(contact == null) return null; // if the contact is not found we return null

        // get the encrypted message from the board
        try{
            Message m = board.get(contact.getIdxReceive(), contact.getTagReceive());
            byte[] encryptedBytes = m.getBytes();
            if(encryptedBytes == null) return null; // if the message is null it doesn't exist, so we return null as well

            // TODO DECRYPT MESSAGE
            GCMParameterSpec ivSpec = new GCMParameterSpec(128, m.getAp());
            cipher.init(Cipher.DECRYPT_MODE, contact.getKeyReceive(),ivSpec);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

            // if the message decryption was unsuccessful we return null as the message didn't come from the right sender
//            if(decryptedUnsuccessful) return null;

            // extract the different parts of the encrypted message
            byte[] messageBytes = new byte[decryptedBytes.length - Main.TAG_LENGTH - Main.IDX_LENGTH];
            System.arraycopy(decryptedBytes, 0, messageBytes, 0, messageBytes.length);
            byte[] idxBytes = new byte[Main.IDX_LENGTH];
            System.arraycopy(decryptedBytes, messageBytes.length, idxBytes, 0, idxBytes.length);
            byte[] tagBytes = new byte[Main.TAG_LENGTH];
            System.arraycopy(decryptedBytes, messageBytes.length + idxBytes.length, tagBytes, 0, tagBytes.length);

            // set the new tag and index
            contact.setTagReceive(tagBytes);
            ByteBuffer buff = ByteBuffer.allocate(Main.IDX_LENGTH);
            buff.put(idxBytes);
            buff.rewind();
            contact.setIdxReceive(buff.getInt());

            // derive the next key
            contact.deriveNextKeyReceive();

            // return the message as a string
            return new String(messageBytes);
        } catch (Exception e) {e.printStackTrace();}

        return null;
    }


    // GETTERS AND SETTERS
    public String getName() {
        return name;
    }

    public Contact getContact(String name) {
        return contacts.getOrDefault(name, null);
    }

    public void addContact(String name, Contact contact) {
        contacts.put(name, contact);
    }
}
