package bibek;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MultiClientChatServer {

    private JFrame frame;
    private JTextArea chatArea;

    private ServerSocket serverSocket;
    private List<ClientHandler> clients;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new MultiClientChatServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public MultiClientChatServer() throws IOException {
        initialize();
        setupServer();
    }

    private void initialize() {
        frame = new JFrame("Multi-Client Chat Server");
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

    private class ClientHandler implements Runnable {

        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket clientSocket) throws IOException {
            this.clientSocket = clientSocket;
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        }

        @Override
        public void run() {
            try {
                while (true) {
                    String message = in.readLine();
                    if (message == null) {
                        break;
                    }
                    chatArea.append("Client: " + message + "\n");
                    broadcastMessage(message, this);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                chatArea.append("Client disconnected: " + clientSocket.getInetAddress().getHostAddress() + "\n");
                clients.remove(this);
                broadcastMessage("Client disconnected", this);
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }
    }
}

