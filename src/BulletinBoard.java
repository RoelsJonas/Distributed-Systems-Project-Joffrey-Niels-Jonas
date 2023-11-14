import java.util.HashMap;
import java.util.Map;

public class BulletinBoard implements BulletinBoardIF{
    private  Map<String, Message>[] board;
    // TODO PUBLIC HASHING FUNCTION (IS THIS NECESSARY AS WE USE A HASHMAP?)

    // CONSTRUCTORS
    public BulletinBoard(int n) {
        board = new Map[n];
        for(int i = 0; i < n; i++) board[i] = new HashMap<>();
    }

    //FUNCTIONS
    // Add a message to the bulletin board, to map at index index, with key tag and value value
    public void add(int index, Message value, String tag) {
        board[index].put(tag, value);
    }

    // Get a message from map at index index with the key being the hash of b
    public Message get(int index, String b) {
        // TODO GET IMPLEMENTEREN
        return null;
    }
}
