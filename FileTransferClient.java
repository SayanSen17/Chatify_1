package bibek;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class FileTransferClient {

    private JFrame frame;
    private JTextArea chatArea;
    private JButton chooseFileButton;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new FileTransferClient();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public FileTransferClient() throws IOException {
        initialize();
        setupClient();
        startListening();
    }

    private void initialize() {
        frame = new JFrame("File Transfer Client");
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
                    if (message.equals("FILE_REQUEST")) {
                        receiveFile();
                    } else {
                        chatArea.append("Server: " + message + "\n");
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

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(fileData);
            objectOutputStream.flush();

            chatArea.append("File sent: " + file.getName() + "\n");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receiveFile() {
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
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

