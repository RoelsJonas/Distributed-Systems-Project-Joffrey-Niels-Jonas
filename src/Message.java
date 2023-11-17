import java.io.Serializable;
import java.security.AlgorithmParameters;
import java.util.Arrays;

public class Message implements Serializable {
    private byte[] bytes;
    private byte[] ap;

    public Message(byte[] bytes) {
        this.bytes = bytes;
    }

    @Override
    public String toString() {
        return "Message{" +
                "bytes=" + Arrays.toString(bytes) +
                ", ap=" + Arrays.toString(ap) +
                '}';
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
