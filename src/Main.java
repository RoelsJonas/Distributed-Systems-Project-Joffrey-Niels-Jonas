import javax.crypto.SecretKey;

public class Main {
    // CONSTANTS
    private static final int N = 16;


    public static void main(String[] args) {
        BulletinBoard bulletinBoard = new BulletinBoard(N); // Generate a new bulletin board with capacity N

        // Create some example clients
        Client c1 = new Client("Joffrey");
        Client c2 = new Client("Niels");
        Client c3 = new Client("Jonas");

        // Exchange contact information between clients
        bump(c1, c2);
        bump(c1, c3);
        bump(c2, c3);
    }


    // Function to add 2 contacts to each others contact book (exchange keys and state information)
    private static void bump(Client c1, Client c2) {
        // TODO IMPLEMENT GENERATION!
        // GENERATE 2 KEYS K12 and K21;
        SecretKey k12 = null;
        SecretKey k21 = null;

        // GENERATE 2 RANDOM TAGS
        String tag12 = null;
        String tag21 = null;

        // GENERATE 2 RANDOM INDEXES
        int idx12 = 0;
        int idx21 = 0;

        c1.addContact(c2.getName(), new Contact(c2.getName(), tag12, tag21, idx12, idx21, k12, k21));
        c2.addContact(c1.getName(), new Contact(c1.getName(), tag21, tag12, idx21, idx12, k21, k12));
    }
}