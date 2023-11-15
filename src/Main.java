import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

public class Main {
    // CONSTANTS
    public static final int N = 256;                                    // Amount of maps in the bulletin board
    public static final int PORT = 1099;                                // Port on which the server runs
    public static final String SERVERNAME = "BulletinBoard";            // Name of the server
    public static final int TAG_LENGTH = 8;                             // Amount of bytes in the tag
    public static final int IDX_LENGTH = 4;                             // Amount of bytes in the index
    public static final int KEY_LENGTH = 16;

    private static final SecureRandom srng = new SecureRandom();
    public static void main(String[] args) {
        // Start server
        BulletinBoard.startServer();

        // Create some example clients
        Client c1 = new Client("Joffrey");
        Client c2 = new Client("Niels");
        Client c3 = new Client("Jonas");

        // Exchange contact information between clients
        bump(c1, c2);
        bump(c1, c3);
        bump(c2, c3);

        c1.send("Niels", "test");
        String m1 = c2.receive("Joffrey");
        System.out.println(m1);

        c1.send("Niels", "test2");
        String m2 = c2.receive("Joffrey");
        System.out.println(m2);

        c3.send("Joffrey", "Hey");

        c1.send("Niels", "test3");
        String m3 = c2.receive("Joffrey");
        System.out.println(m3);

        c1.send("Niels", "test4");
        String m4 = c2.receive("Joffrey");
        System.out.println(m4);

        String m5 = c1.receive("Jonas");
        System.out.println(m5);
    }


    // Function to add 2 contacts to each other's contact book (exchange keys and state information)
    private static void bump(Client c1, Client c2) {
        // GENERATE 2 KEYS K12 and K21;
        byte[] k12Bytes = new byte[KEY_LENGTH];
        byte[] k21Bytes = new byte[KEY_LENGTH];
        srng.nextBytes(k12Bytes);
        srng.nextBytes(k21Bytes);
        SecretKey k12 = new SecretKeySpec(k12Bytes, "AES");
        SecretKey k21 = new SecretKeySpec(k21Bytes, "AES");

        // GENERATE 2 RANDOM TAGS
        byte[] tag12 = new byte[TAG_LENGTH];
        srng.nextBytes(tag12);
        byte[] tag21 = new byte[TAG_LENGTH];
        srng.nextBytes(tag21);

        // GENERATE 2 RANDOM INDEXES
        int idx12 = srng.nextInt(N);
        int idx21 = srng.nextInt(N);

        c1.addContact(c2.getName(), new Contact(c2.getName(), tag12, tag21, idx12, idx21, k12, k21));
        c2.addContact(c1.getName(), new Contact(c1.getName(), tag21, tag12, idx21, idx12, k21, k12));
    }
}