package bibek;

	
	import javax.swing.*;
	import java.awt.*;
	import java.awt.event.ActionEvent;
	import java.awt.event.ActionListener;
	import java.io.BufferedReader;
	import java.io.IOException;
	import java.io.InputStreamReader;
	import java.io.PrintWriter;
	import java.net.Socket;

	public class ChatClient {

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
	                new ChatClient();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        });
	    }

	    public ChatClient() throws IOException {
	        initialize();
	        setupClient();
	        startListening();
	    }

	    private void initialize() {
	        frame = new JFrame("Chat Application - Client");
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

	    private void setupClient() throws IOException {
	        socket = new Socket("localhost", 6000);
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

