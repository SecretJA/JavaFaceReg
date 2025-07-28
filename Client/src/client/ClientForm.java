package client;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgcodecs.Imgcodecs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;
import java.net.Socket;
import org.opencv.core.MatOfByte;

public class ClientForm extends JFrame {
    static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    private JTextField txtTen, txtTuoi, txtDiaChi;
    private JLabel lblImage;
    private JButton btnCapture, btnSend;
    private JPanel cameraPanel;
    private VideoCapture camera;
    private Mat frame;
    private Timer timer;
    private byte[] imageBytes;

    public ClientForm() {
        setTitle("Client Form");
        setLayout(new GridLayout(6, 2));

        txtTen = new JTextField();
        txtTuoi = new JTextField();
        txtDiaChi = new JTextField();
        lblImage = new JLabel();
        btnCapture = new JButton("Chụp Hình");
        btnSend = new JButton("Gửi");
        cameraPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (frame != null) {
                    Image img = toBufferedImage(frame);
                    g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), null);
                }
            }
        };

        add(new JLabel("Tên:"));
        add(txtTen);
        add(new JLabel("Tuổi:"));
        add(txtTuoi);
        add(new JLabel("Địa chỉ:"));
        add(txtDiaChi);
        add(lblImage);
        add(cameraPanel);
        add(btnCapture);
        add(btnSend);

        cameraPanel.setPreferredSize(new Dimension(320, 240));

        btnCapture.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                captureImage();
            }
        });

        btnSend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendDataToServer();
            }
        });

        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        startCamera();
    }

    private void startCamera() {
        camera = new VideoCapture(0);
        if (!camera.isOpened()) {
            System.out.println("Error: Camera is not opened");
            return;
        }
        frame = new Mat();
        timer = new Timer(30, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (camera.read(frame)) {
                    cameraPanel.repaint();
                }
            }
        });
        timer.start();
    }

    private void captureImage() {
        if (frame != null && !frame.empty()) {
            MatOfByte matOfByte = new MatOfByte();
            Imgcodecs.imencode(".jpg", frame, matOfByte);
            imageBytes = matOfByte.toArray();

            // Hiển thị ảnh chụp lên màn hình
            ImageIcon imageIcon = new ImageIcon(imageBytes);
            lblImage.setIcon(imageIcon);
            lblImage.setText("");
        } else {
            System.out.println("Error: Frame is empty");
        }
    }

    private void sendDataToServer() {
        String ten = txtTen.getText();
        String tuoi = txtTuoi.getText();
        String diaChi = txtDiaChi.getText();

        try (Socket socket = new Socket("localhost", 12345);
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Gửi thông điệp addFace trước
            oos.writeObject("addFace");

            // Gửi dữ liệu
            oos.writeObject(ten);
            oos.writeObject(tuoi);
            oos.writeObject(diaChi);
            oos.writeObject(imageBytes);

            // Nhận phản hồi từ server
            String response = reader.readLine();
            JOptionPane.showMessageDialog(this, response);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static Image toBufferedImage(Mat mat) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (mat.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = mat.channels() * mat.cols() * mat.rows();
        byte[] buffer = new byte[bufferSize];
        mat.get(0, 0, buffer);
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(buffer, 0, targetPixels, 0, buffer.length);
        return image;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ClientForm().setVisible(true);
        });
    }
}
