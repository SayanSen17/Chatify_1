package bibek;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class MultiClientChatClient {

    private JFrame frame;
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new MultiClientChatClient();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public MultiClientChatClient() throws IOException {
        initialize();
        setupClient();
        startListening();
    }

    private void initialize() {
        frame = new JFrame("Multi-Client Chat Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        messageField = new JTextField();
        frame.add(messageField, BorderLayout.SOUTH);

        sendButton = new JButton("Send");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        frame.add(sendButton, BorderLayout.EAST);

        frame.setVisible(true);
    }

    private void setupClient() throws IOException {
        socket = new Socket("localhost", 12345);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    private void startListening() {
        new Thread(() -> {
            try {
                while (true) {
                    String message = in.readLine();
                    if (message == null) {
                        break;
                    }
                    chatArea.append("Server: " + message + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void sendMessage() {
        String message = messageField.getText();
        if (!message.isEmpty()) {
            out.println(message);
            chatArea.append("You: " + message + "\n");
            messageField.setText("");
        }
    }
}

