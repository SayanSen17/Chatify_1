package bibek;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Client {

    private JFrame frame;
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new Client();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public Client() throws IOException {
        initialize();
        setupServer();
    }

    private void initialize() {
        frame = new JFrame("Chat Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());

        messageField = new JTextField();
        bottomPanel.add(messageField, BorderLayout.CENTER);

        sendButton = new JButton("Send");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        bottomPanel.add(sendButton, BorderLayout.EAST);

        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private void setupServer() throws IOException {
        serverSocket = new ServerSocket(6000);
        chatArea.append("Server started. Waiting for clients...\n");

        clientSocket = serverSocket.accept();
        chatArea.append("Client connected: " + clientSocket.getInetAddress().getHostAddress() + "\n");

        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        startListening();
    }

    private void startListening() {
        new Thread(() -> {
            try {
                while (true) {
                    String message = in.readLine();
                    if (message == null) {
                        break;
                    }
                    chatArea.append("Client: " + message + "\n");
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
