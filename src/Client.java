import java.util.HashMap;
import java.util.Map;

public class Client {
    private Map<String, Contact> contacts;
    private final String name;


    // CONSTRUCTORS
    public Client(String name) {
        this.name = name;
        contacts = new HashMap<>();

        // TODO INITIALIZE RMI TO BULLETINBOARD
    }

    // FUNCTIONS
    // Send a message to a contact
    public void send(String contact, String message) {
        // TODO IMPLEMENT SEE PAPER FIGURE 2
    }

    // Receive a message from a contact
    public String receive(String contact) {
        // TODO IMPLEMENT SEE PAPER FIGURE 2
        return null;
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
