import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Map;
import javax.swing.*;

public class Main {
    // CONSTANTS
    public static final int N = 256;                                    // Amount of maps in the bulletin board
    public static final int PORT = 1099;                                // Port on which the server runs
    public static final String SERVERNAME = "BulletinBoard";            // Name of the server
    public static final int TAG_LENGTH = 8;                             // Amount of bytes in the tag
    public static final int IDX_LENGTH = 4;                             // Amount of bytes in the index
    public static final int KEY_LENGTH = 16;

    private static ArrayList<Client> users = new ArrayList<>();
    private static ArrayList<JButton> buttons = new ArrayList<>();

    private static final SecureRandom srng = new SecureRandom();

    public static void main(String[] args) throws AccessException, RemoteException, NoSuchAlgorithmException, NotBoundException {
        BulletinBoard.startServer();

        Client c1 = new Client("Joffrey");
        Client c2 = new Client("Niels");
        Client c3 = new Client("Jonas");

        bumpUsers(c1);
        users.add(c1);

        bumpUsers(c2);
        users.add(c2);

        bumpUsers(c3);
        users.add(c3);

        ChatUI client1UI = new ChatUI(users, c1);
        ChatUI client2UI = new ChatUI(users, c2);
        ChatUI client3UI = new ChatUI(users, c3);

        c1.storeRecoveryData();
        c2.storeRecoveryData();
        c3.storeRecoveryData();

        // crash client c1
        try {
            Thread.sleep(10000); // Delay of 10 seconds
            crashAndRecoverClient(c1, client1UI);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Crash the server
        try {
            Thread.sleep(1000); // Delay of 10 seconds
            crashAndRecoverServer();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void crashAndRecoverClient(Client c, ChatUI oldClient1UI) {
        System.out.println("CRASHED CLIENT " + c.getName());
        users.remove(c);
        oldClient1UI.crash();
        try {
            Thread.sleep(2000); 
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        Client c1 = new Client("Joffrey");
        bumpUsers(c1);
        users.add(c1);
        ChatUI newClient1UI = new ChatUI(users, c1);

        c1.recoverClient();
    }

    private static void crashAndRecoverServer() throws AccessException, RemoteException, NotBoundException, NoSuchAlgorithmException {
        System.out.println("CRASHED SERVER");

       BulletinBoard.stopServer();

        try {
            Thread.sleep(2000); 
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        BulletinBoard.startServer();
        BulletinBoard.recoverBoard();
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

    public static void bumpUsers(Client client) {
        for (Client otherClient: users) {
            bump(otherClient, client);
        }
    }
}