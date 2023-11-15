import java.io.Serializable;
import java.security.AlgorithmParameters;

public class Message implements Serializable {
    private byte[] bytes;
    private byte[] ap;

    public Message(byte[] bytes) {
        this.bytes = bytes;
    }

    public Message(byte[] bytes, byte[] ap) {
        this.bytes = bytes;
        this.ap = ap;
    }

    public byte[] getAp() {
        return ap;
    }

    public byte[] getBytes() {
        return bytes;
    }
}
