import java.util.HashMap;
import java.util.Map;

public interface BulletinBoardIF {


    public void add(int index, Message value, String tag);

    public Message get(int index, String b);
}
