import javax.crypto.SecretKey;

public class Contact {
    private final String name;
    private String tagSend;
    private String tagReceive;
    private int idxSend;
    private int idxReceive;

    private SecretKey keySend;

    private SecretKey keyReceive;


    // CONSTRUCTORS


    public Contact(String name, String tagSend, String tagReceive, int idxSend, int idxReceive, SecretKey keySend, SecretKey keyReceive) {
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

    public String getTagSend() {
        return tagSend;
    }

    public void setTagSend(String tagSend) {
        this.tagSend = tagSend;
    }

    public String getTagReceive() {
        return tagReceive;
    }

    public void setTagReceive(String tagReceive) {
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

    public SecretKey getKeyReceive() {
        return keyReceive;
    }

    public void deriveNextKeySend() {
        // TODO KEYDERAVATION
    }

    public void deriveNextKeyReceive() {
        // TODO KEYDERAVATION
    }
}