package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class ImageClient extends JFrame {

    public ImageClient() {
        setTitle("Image Client");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 400);

        JPanel panel = new JPanel();
        add(panel);
        placeComponents(panel);

        setVisible(true);
    }

    private void placeComponents(JPanel panel) {
        panel.setLayout(null);

        JLabel label = new JLabel("Select an image to send:");
        label.setBounds(10, 20, 200, 25);
        panel.add(label);

        JButton selectButton = new JButton("Select Image");
        selectButton.setBounds(10, 50, 150, 25);
        panel.add(selectButton);

        JLabel fileLabel = new JLabel("");
        fileLabel.setBounds(10, 80, 300, 25);
        panel.add(fileLabel);

        JButton sendButton = new JButton("Send Image");
        sendButton.setBounds(10, 110, 150, 25);
        sendButton.setEnabled(false);
        panel.add(sendButton);

        JLabel imageLabel = new JLabel();
        imageLabel.setBounds(10, 140, 550, 200); // Adjust size as needed
        panel.add(imageLabel);

        selectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    fileLabel.setText("Selected: " + selectedFile.getName());
                    sendButton.setEnabled(true);

                    // Display the selected image
                    ImageIcon imageIcon = new ImageIcon(selectedFile.getAbsolutePath());
                    Image image = imageIcon.getImage().getScaledInstance(imageLabel.getWidth(), imageLabel.getHeight(), Image.SCALE_SMOOTH);
                    imageLabel.setIcon(new ImageIcon(image));

                    sendButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            sendImage(selectedFile);
                        }
                    });
                }
            }
        });
    }

    private void sendImage(File imageFile) {
        String serverAddress = "localhost"; // Server address
        int port = 12345; // Server port

        try (Socket socket = new Socket(serverAddress, port);
             FileInputStream fileInputStream = new FileInputStream(imageFile);
             OutputStream outputStream = socket.getOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {

            // Send the command "findFace" to the server
            objectOutputStream.writeObject("findFace");

            // Convert the image file to byte array
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            byte[] imageBytes = byteArrayOutputStream.toByteArray();

            // Send the image bytes to the server
            objectOutputStream.writeObject(imageBytes);

            // Receive response from the server
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String response;
            StringBuilder responseBuilder = new StringBuilder();
            while ((response = reader.readLine()) != null) {
                responseBuilder.append(response).append("\n");
            }

            // Display the server response
            JOptionPane.showMessageDialog(this, responseBuilder.toString(), "Server Response", JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException e) {
            System.err.println("Error sending image: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ImageClient::new);
    }
}
