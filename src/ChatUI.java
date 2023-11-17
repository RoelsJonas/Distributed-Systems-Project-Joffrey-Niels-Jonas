import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.ArrayList;


public class ChatUI {
    JFrame frame;
    JPanel registerPanel;
    JPanel userListPanel;
    JPanel chatPanel;
    JPanel messagePanel;
    JButton usernameSubmit;

    ArrayList<JTextArea> chats;
    ArrayList<JButton> buttons;
    ArrayList<Client> friends;

    Client senderClient;
    Client receiverClient;

    int index = 0;

    public ChatUI(ArrayList<Client> friends, Client c) {
        frame = new JFrame(c.getName());
        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        receiverClient = null;              // Client that receives message from sender
        senderClient = c;                   // Client that sends the messages

        this.friends = friends;
        buttons = new ArrayList<JButton>();
        chats = new ArrayList<>();
        for (Client client: friends) {
            buttons.add(new JButton(client.getName()));
            chats.add(new JTextArea());
        }

        setChat();
        frame.setVisible(true);
    }

    public void setChat() {
        chatPanel = new JPanel();
        chatPanel.setLayout(new GridLayout(1, 2));
        messagePanel = new JPanel(new BorderLayout());

        // Userlist
        userListPanel = new JPanel();
        userListPanel.setLayout(new BoxLayout(userListPanel, BoxLayout.Y_AXIS));

        // Makes a button and actionlistener for every friend
        for (int i=0; i<buttons.size(); i++) {
            if (i == getIndex(senderClient.getName())) continue;
            JButton button = buttons.get(i);
            userListPanel.add(button);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Removes previous chat
                    messagePanel.remove(chats.get(index));
                    messagePanel.revalidate();
                    messagePanel.repaint();

                    // Gets the messages the sender has received from this client
                    index = getIndex(button.getText());
                    receiverClient = friends.get(index);
                    String text = senderClient.receive(receiverClient.getName());
                    while (text != null) {
                        chats.get(index).append(receiverClient.getName() + ": " + text + "\n");
                        text = senderClient.receive(receiverClient.getName());
                    }


                    // Opens new chat
                    messagePanel.add(chats.get(index), BorderLayout.CENTER);
                    chatPanel.add(messagePanel, BorderLayout.EAST);
                    frame.revalidate();
                    frame.repaint();
                }
            });
        }

        // send messages
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());
        JTextField textField = new JTextField();
        JButton sendButton = new JButton("Send");

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (receiverClient == null) return;
                String message = textField.getText();
                senderClient.send(receiverClient.getName(), message);

                chats.get(index).append(senderClient.getName() + ": " + message + "\n");
                messagePanel.add(chats.get(index), BorderLayout.CENTER);
                chatPanel.add(messagePanel, BorderLayout.EAST);
                frame.revalidate();
                frame.repaint();
            }
        });

        inputPanel.add(textField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        chatPanel.add(userListPanel, BorderLayout.WEST);
        frame.add(inputPanel, BorderLayout.SOUTH);
        frame.add(chatPanel, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }

    public int getIndex(String name) {
        for (int i=0; i<friends.size(); i++) {
            if (name.equals(friends.get(i).getName())) {
                return i;
            }
        }
        return 0;
    }




    public void setRegister() {
        registerPanel = new JPanel();
        registerPanel.setLayout(new FlowLayout());

        JLabel usernameLabel = new JLabel("Username");
        JTextField usernameField = new JTextField(20);
        usernameSubmit = new JButton("Submit");

        usernameSubmit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.remove(registerPanel);
                frame.revalidate();
                frame.repaint();
                setChat();
            }
        });

        registerPanel.add(usernameLabel);
        registerPanel.add(usernameField);
        registerPanel.add(usernameSubmit);
        frame.add(registerPanel);
    }
}
