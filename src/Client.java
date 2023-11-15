import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;

public class Client {
    private Map<String, Contact> contacts;
    private final String name;
    private BulletinBoardIF board;

    // CONSTRUCTORS
    public Client(String name) {
        this.name = name;
        contacts = new HashMap<>();

        // Connect to bulletin board
        try {
            Registry myRegistry = LocateRegistry.getRegistry("localhost", Main.PORT);
            board = (BulletinBoardIF) myRegistry.lookup(Main.SERVERNAME);
        } catch (Exception e) {e.printStackTrace();}

    }

    // FUNCTIONS
    // Send a message to a contact
    public void send(String contactName, String message) {
        Contact contact = contacts.getOrDefault(contactName, null);
        if(contact == null) return;

        // TODO GENERATE NEW TAG AND INDEX
        int idxNew = 0;
        byte[] tagNew = new byte[Main.TAG_LENGTH];

        // get the message, index and tag as seperate byte arrays
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        BigInteger idx = BigInteger.valueOf(idxNew);
        byte[] idxBytes = idx.toByteArray();

        // combine the 3 byte arrays
        byte[] combinedBytes = new byte[messageBytes.length + idxBytes.length + tagNew.length];
        System.arraycopy(messageBytes, 0, combinedBytes, 0, messageBytes.length);
        System.arraycopy(idxBytes, 0, combinedBytes, messageBytes.length, idxBytes.length);
        System.arraycopy(tagNew, 0, combinedBytes, messageBytes.length + idxBytes.length, tagNew.length);

        // hash the tag TODO
        byte[] tagDigest = new byte[Main.TAG_LENGTH];

        // Try to add the message to the board if succeeded set tags to new tags and derive the next key
        try{
            board.add(contact.getIdxSend(), combinedBytes, new String(tagDigest));
            contact.setIdxSend(idxNew);
            contact.setTagSend(tagNew);
            contact.deriveNextKeySend();
        } catch (Exception e) {e.printStackTrace();}


    }

    // Receive a message from a contact
    public String receive(String contact) {
        // TODO IMPLEMENT SEE PAPER FIGURE 2
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
