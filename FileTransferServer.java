package bibek;

	
	import javax.swing.*;
	import java.awt.*;
	import java.awt.event.ActionEvent;
	import java.awt.event.ActionListener;
	import java.io.*;
	import java.net.ServerSocket;
	import java.net.Socket;

	public class FileTransferServer {

	    private JFrame frame;
	    private JTextArea chatArea;
	    private JButton chooseFileButton;

	    private ServerSocket serverSocket;
	    private Socket clientSocket;
	    private PrintWriter out;
	    private BufferedReader in;

	    public static void main(String[] args) {
	        SwingUtilities.invokeLater(() -> {
	            try {
	                new FileTransferServer();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        });
	    }

	    public FileTransferServer() throws IOException {
	        initialize();
	        setupServer();
	    }

	    private void initialize() {
	        frame = new JFrame("File Transfer Server");
	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        frame.setSize(400, 300);
	        frame.setLayout(new BorderLayout());

	        chatArea = new JTextArea();
	        chatArea.setEditable(false);
	        JScrollPane scrollPane = new JScrollPane(chatArea);
	        frame.add(scrollPane, BorderLayout.CENTER);

	        chooseFileButton = new JButton("Choose File");
	        chooseFileButton.addActionListener(new ActionListener() {
	            @Override
	            public void actionPerformed(ActionEvent e) {
	                sendFile();
	            }
	        });
	        frame.add(chooseFileButton, BorderLayout.SOUTH);

	        frame.setVisible(true);
	    }

	    private void setupServer() throws IOException {
	        serverSocket = new ServerSocket(12345);
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
	                    if (message.equals("FILE_REQUEST")) {
	                        receiveFile();
	                    } else {
	                        chatArea.append("Client: " + message + "\n");
	                    }
	                }
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }).start();
	    }

	    private void sendFile() {
	        JFileChooser fileChooser = new JFileChooser();
	        int result = fileChooser.showOpenDialog(frame);
	        if (result == JFileChooser.APPROVE_OPTION) {
	            File selectedFile = fileChooser.getSelectedFile();
	            out.println("FILE_REQUEST");
	            sendFile(selectedFile);
	        }
	    }

	    private void sendFile(File file) {
	        try (FileInputStream fileInputStream = new FileInputStream(file);
	             BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream)) {

	            byte[] fileData = new byte[(int) file.length()];
	            bufferedInputStream.read(fileData, 0, fileData.length);

	            ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
	            objectOutputStream.writeObject(fileData);
	            objectOutputStream.flush();

	            chatArea.append("File sent: " + file.getName() + "\n");

	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }

	    private void receiveFile() {
	        try {
	            ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
	            byte[] fileData = (byte[]) objectInputStream.readObject();

	            JFileChooser fileChooser = new JFileChooser();
	            int result = fileChooser.showSaveDialog(frame);

	            if (result == JFileChooser.APPROVE_OPTION) {
	                File selectedFile = fileChooser.getSelectedFile();
	                try (FileOutputStream fileOutputStream = new FileOutputStream(selectedFile);
	                     BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)) {

	                    bufferedOutputStream.write(fileData, 0, fileData.length);
	                    bufferedOutputStream.flush();
	                    chatArea.append("File received: " + selectedFile.getName() + "\n");

	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
	            }
	        } catch (IOException | ClassNotFoundException e) {
	            e.printStackTrace();
	        }
	    }
	}



