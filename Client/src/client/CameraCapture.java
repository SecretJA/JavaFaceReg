package client;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import java.io.*;
import java.net.Socket;
import javax.imageio.ImageIO;

public class CameraCapture extends JFrame {
    private JLabel cameraScreen;
    private VideoCapture capture;
    private Mat frame;
    private boolean capturing;
    private BufferedImage capturedImage;

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public CameraCapture() {
        cameraScreen = new JLabel();
        JButton captureButton = new JButton("Capture");

        captureButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                capturing = false;
                capturedImage = matToBufferedImage(frame);
                sendImageToServer(capturedImage);
            }
        });

        setLayout(new BorderLayout());
        add(cameraScreen, BorderLayout.CENTER);
        add(captureButton, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setVisible(true);

        capture = new VideoCapture(0);
        frame = new Mat();
        capturing = true;

        new Thread(() -> {
            while (capturing) {
                capture.read(frame);
                if (!frame.empty()) {
                    ImageIcon image = new ImageIcon(matToBufferedImage(frame));
                    cameraScreen.setIcon(image);
                }
            }
        }).start();
    }

    private BufferedImage matToBufferedImage(Mat mat) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (mat.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        mat.get(0, 0, ((DataBufferByte) image.getRaster().getDataBuffer()).getData());
        return image;
    }

    private void sendImageToServer(BufferedImage image) {
        String serverAddress = "localhost";
        int port = 12345;

        try (Socket socket = new Socket(serverAddress, port);
             OutputStream outputStream = socket.getOutputStream();
             InputStream inputStream = socket.getInputStream()) {

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();

            ObjectOutputStream oos = new ObjectOutputStream(outputStream);
            oos.writeObject("findFace");
            oos.writeObject(imageBytes);

            // Đọc kết quả từ server
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String result;
            StringBuilder resultBuilder = new StringBuilder();
            while ((result = reader.readLine()) != null) {
                resultBuilder.append(result).append("\n");
            }

            JOptionPane.showMessageDialog(this, resultBuilder.toString(), "Kết quả từ server", JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CameraCapture::new);
    }
}
