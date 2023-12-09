
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
    public Map<String, Contact> contacts;
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

        storeRecoveryData();

    }

    // Receive a message from a contact
    public String receive(String contactName) {
        Contact contact = contacts.getOrDefault(contactName, null);
        if(contact == null) return null; // if the contact is not found we return null

        // get the encrypted message from the board
        try{
            Message m = board.get(contact.getIdxReceive(), contact.getTagReceive());
            if(m == null) return null; 

            byte[] encryptedBytes = m.getBytes();
            if(encryptedBytes == null) return null; // if the message is null it doesn't exist, so we return null as well

            // TODO DECRYPT MESSAGE
            GCMParameterSpec ivSpec = new GCMParameterSpec(128, m.getAp());
            cipher.init(Cipher.DECRYPT_MODE, contact.getKeyReceive(),ivSpec);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            if(decryptedBytes == null) return null; 

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
        } catch (Exception e) {
            //e.printStackTrace();
        }

        storeRecoveryData();

        return null;
    }

   public void storeRecoveryData() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("RecoveryData/Client_" + name))) {
            //writer.write("contactname;idxReceive;idxSend;tagSend;tagReceive");
            for(Map.Entry<String, Contact> entry : contacts.entrySet()) {
                String contactName = entry.getKey();
                Contact contact = entry.getValue();

                int idxSend = contact.getIdxSend();
                int idxReceive = contact.getIdxReceive();
                byte[] tagSend = contact.getTagSend();
                byte[] tagReceive = contact.getTagReceive();
                SecretKey keySend = contact.getKeySend();
                SecretKey keyReceive = contact.getKeyReceive();

                writer.write(contactName + ";");
                writer.write(idxSend + ";");
                writer.write(idxReceive + ";");
                writer.write(Arrays.toString(tagSend) + ";");
                writer.write(Arrays.toString(tagReceive) + ";");
                writer.write(Base64.getEncoder().encodeToString(keySend.getEncoded()) + ";");
                writer.write(Base64.getEncoder().encodeToString(keyReceive.getEncoded()) + ";");
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void recoverClient() {
        try (BufferedReader reader = new BufferedReader(new FileReader("RecoveryData/Client_" + name))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(";");
                String contactName = data[0];
                int idxSend = Integer.parseInt(data[1]);
                int idxReceive = Integer.parseInt(data[2]);
                byte[] tagSend = parseByteArray(data[3]);
                byte[] tagReceive = parseByteArray(data[4]);
                SecretKey keySend = new SecretKeySpec(Base64.getDecoder().decode(data[5]), "AES");
                SecretKey keyReceive = new SecretKeySpec(Base64.getDecoder().decode(data[6]), "AES");

                Contact contact = contacts.get(contactName);
                contact.setIdxSend(idxSend);
                contact.setIdxReceive(idxReceive);
                contact.setTagSend(tagSend);
                contact.setTagReceive(tagReceive);
                contact.setKeySend(keySend);
                contact.setKeyReceive(keyReceive);
            }
        } catch (IOException e) {
            System.out.println("No recovery file found for client " + name + ".");
        }
    }

    private byte[] parseByteArray(String data) {
        String[] bytes = data.substring(1, data.length() - 1).split(", ");
        byte[] byteArray = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            byteArray[i] = Byte.parseByte(bytes[i]);
        }
        return byteArray;
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
