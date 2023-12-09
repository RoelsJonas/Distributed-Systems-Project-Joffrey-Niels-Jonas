import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.Mac;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Contact {
    private final String name;
    private byte[] tagSend;
    private byte[] tagReceive;
    private int idxSend;
    private int idxReceive;

    private SecretKey keySend;

    private SecretKey keyReceive;


    // CONSTRUCTORS


    public Contact(String name, byte[] tagSend, byte[] tagReceive, int idxSend, int idxReceive, SecretKey keySend, SecretKey keyReceive) {
        this.name = name;
        this.tagSend = tagSend;
        this.tagReceive = tagReceive;
        this.idxSend = idxSend;
        this.idxReceive = idxReceive;
        this.keySend = keySend;
        this.keyReceive = keyReceive;
    }

    // GETTERS AND SETTERS
    public String getName() {
        return name;
    }

    public byte[] getTagSend() {
        return tagSend;
    }

    public void setTagSend(byte[] tagSend) {
        this.tagSend = tagSend;
    }

    public byte[] getTagReceive() {
        return tagReceive;
    }

    public void setTagReceive(byte[] tagReceive) {
        this.tagReceive = tagReceive;
    }

    public int getIdxSend() {
        return idxSend;
    }

    public void setIdxSend(int idxSend) {
        this.idxSend = idxSend;
    }

    public int getIdxReceive() {
        return idxReceive;
    }

    public void setIdxReceive(int idxReceive) {
        this.idxReceive = idxReceive;
    }

    public SecretKey getKeySend() {
        return keySend;
    }

    public void setKeySend(SecretKey keySend) {
        this.keySend = keySend;
    }

    public SecretKey getKeyReceive() {
        return keyReceive;
    }

    public void setKeyReceive(SecretKey keyReceive) {
        this.keyReceive = keyReceive;
    }

    public void deriveNextKeySend() {
        try {
            SecretKey newKeySend = deriveKey(keySend);
            this.keySend = newKeySend;
        }
        catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    public void deriveNextKeyReceive() {
        try {
            SecretKey newKeyReceive = deriveKey(keyReceive);
            this.keyReceive = newKeyReceive;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    public SecretKey deriveKey(SecretKey currentKey) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        byte[] salt = null; // Zorgt voor extra randomnes bij het afleiden van de key
        byte[] info = null; // Geeft extra context

        mac.init(currentKey);
        byte[] derivedKey = mac.doFinal();
        return new SecretKeySpec(derivedKey, "AES");
    }
}
