package bibek;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MultiUserChatServer {

    private JFrame frame;
    private JTextArea chatArea;

    private ServerSocket serverSocket;
    private List<ClientHandler> clients;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new MultiUserChatServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public MultiUserChatServer() throws IOException {
        initialize();
        setupServer();
    }

    private void initialize() {
        frame = new JFrame("Multi-User Chat Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        frame.setVisible(true);
    }

    private void setupServer() throws IOException {
        serverSocket = new ServerSocket(12345);
        chatArea.append("Server started. Waiting for clients...\n");

        clients = new ArrayList<>();

        while (true) {
            Socket clientSocket = serverSocket.accept();
            chatArea.append("Client connected: " + clientSocket.getInetAddress().getHostAddress() + "\n");

            ClientHandler clientHandler = new ClientHandler(clientSocket);
            clients.add(clientHandler);
            new Thread(clientHandler).start();
        }
    }

    private void broadcastMessage(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    private void sendPrivateMessage(String message, String recipient, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender && client.getUsername().equals(recipient)) {
                client.sendMessage("Private message from " + sender.getUsername() + ": " + message);
                sender.sendMessage("Private message to " + recipient + ": " + message);
                return;
            }
        }
        sender.sendMessage("User " + recipient + " not found or not online.");
    }

    private class ClientHandler implements Runnable {

        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;

        public ClientHandler(Socket clientSocket) throws IOException {
            this.clientSocket = clientSocket;
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        }

        @Override
        public void run() {
            try {
                out.println("Enter your username:");
                username = in.readLine();
                chatArea.append("User " + username + " connected.\n");

                broadcastMessage("User " + username + " joined the chat.", this);

                while (true) {
                    String message = in.readLine();
                    if (message == null) {
                        break;
                    }
                    if (message.startsWith("/private")) {
                        // Format: /private username message
                        String[] parts = message.split(" ", 3);
                        if (parts.length == 3) {
                            sendPrivateMessage(parts[2], parts[1], this);
                        } else {
                            sendMessage("Invalid private message format. Use: /private username message");
                        }
                    } else {
                        chatArea.append(username + ": " + message + "\n");
                        broadcastMessage(username + ": " + message, this);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                chatArea.append("User " + username + " disconnected.\n");
                clients.remove(this);
                broadcastMessage("User " + username + " left the chat.", this);
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }

        public String getUsername() {
            return username;
        }
    }
}
