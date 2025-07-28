package server;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import javax.imageio.ImageIO;

public class FaceRecognitionServer {
    private static final String API_KEY = "IKnZDsMB0rWSZj8bH_cA09IQ5nS6ccYp";
    private static final String API_SECRET = "tukoKax4KYh6AsFfT5mxUUtAvAg4TDOL";
    private static final String ENDPOINT = "https://api-us.faceplusplus.com/facepp/v3/compare";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/mysql?zeroDateTimeBehavior=CONVERT_TO_NULL";
    private static final String USER = "root";
    private static final String PASSWORD = "1234";

    public static void main(String[] args) {
        int port = 12345; // Port mà server sẽ lắng nghe
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server đang chạy...");

            while (true) {
                try (Socket socket = serverSocket.accept()) {
                    System.out.println("New client connected");

                    // Đọc thông điệp từ client
                    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                    String message = (String) ois.readObject();
                    System.out.println("Received message: " + message);

                    // Xử lý thông điệp
                    String responseMessage = "";
                    if (message.equals("addFace")) {
                        String ten = (String) ois.readObject();
                        String tuoi = (String) ois.readObject();
                        String diaChi = (String) ois.readObject();
                        byte[] imageBytes = (byte[]) ois.readObject();
                        responseMessage = addFace(ten, tuoi, diaChi, imageBytes);
                    } else if (message.equals("findFace")) {
                        byte[] imageBytes = (byte[]) ois.readObject();
                        responseMessage = findFace(imageBytes);
                    } else {
                        responseMessage = "Unknown command received";
                        System.out.println(responseMessage);
                    }

                    // Gửi phản hồi lại client
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    writer.write(responseMessage);
                    writer.newLine();
                    writer.flush();
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println("Client connection error: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }

    private static String addFace(String ten, String tuoi, String diaChi, byte[] imageBytes) {
        System.out.println("Executing addFace function...");
        System.out.println("Name: " + ten);
        System.out.println("Age: " + tuoi);
        System.out.println("Address: " + diaChi);
        // Lưu ảnh vào thư mục và lưu thông tin vào cơ sở dữ liệu
        try {
            String imagePath = saveImage(imageBytes);
            saveToDatabase(ten, tuoi, diaChi, imagePath);
            System.out.println("Dữ liệu đã được lưu vào cơ sở dữ liệu.");
            return "Dữ liệu đã được lưu thành công";
        } catch (IOException e) {
            e.printStackTrace();
            return "Lỗi khi lưu dữ liệu";
        }
    }

    private static String findFace(byte[] imageBytes) {
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageBytes);
            BufferedImage image = ImageIO.read(byteArrayInputStream);

            File outputfile = new File("images/captured_image.jpg");
            ImageIO.write(image, "jpg", outputfile);

            System.out.println("Hình ảnh đã được lưu thành công.");

            // So sánh ảnh với cơ sở dữ liệu và lấy kết quả
            return compareImageWithDatabase(outputfile);

        } catch (IOException e) {
            e.printStackTrace();
            return "Lỗi khi xử lý hình ảnh";
        }
    }

    private static String compareImageWithDatabase(File image1) {
        File folder = new File("G:\\DoAnFinal\\Server\\imageData");

        File[] files = folder.listFiles();
        double maxConfidence = 0.0;
        String bestMatchImage = "";

        if (files != null) {
            for (File image2 : files) {
                if (image2.isFile() && isImageFile(image2)) {
                    try {
                        HttpClient httpClient = HttpClients.createDefault();
                        HttpPost request = new HttpPost(ENDPOINT);

                        FileBody fileBody1 = new FileBody(image1, ContentType.DEFAULT_BINARY);
                        FileBody fileBody2 = new FileBody(image2, ContentType.DEFAULT_BINARY);

                        HttpEntity entity = MultipartEntityBuilder.create()
                                .addTextBody("api_key", API_KEY)
                                .addTextBody("api_secret", API_SECRET)
                                .addPart("image_file1", fileBody1)
                                .addPart("image_file2", fileBody2)
                                .build();

                        request.setEntity(entity);

                        HttpResponse response = httpClient.execute(request);
                        HttpEntity responseEntity = response.getEntity();

                        if (responseEntity != null) {
                            String responseString = EntityUtils.toString(responseEntity);
                            System.out.println("Response from Face++ API: " + responseString);

                            JSONObject jsonResponse = new JSONObject(responseString);

                            if (jsonResponse.has("confidence")) {
                                double confidence = jsonResponse.getDouble("confidence");

                                if (confidence > maxConfidence) {
                                    maxConfidence = confidence;
                                    bestMatchImage = image2.getName();
                                }
                            } else {
                                System.out.println("Không tìm thấy trường confidence trong phản hồi từ API.");
                            }
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        StringBuilder result = new StringBuilder();
        result.append("Best match image: ").append(bestMatchImage)
                .append("\nWith similarity percentage: ").append(maxConfidence).append("%\n");

        try (Connection connection = getConnection()) {
            String userQuery = "SELECT * FROM users WHERE image_directory_path LIKE ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(userQuery)) {
                preparedStatement.setString(1, "%" + bestMatchImage + "%");
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        int id = resultSet.getInt("id");
                        String name = resultSet.getString("name");
                        int age = resultSet.getInt("age");
                        String address = resultSet.getString("address");
                        result.append("User Info: ID: ").append(id).append(", Name: ").append(name)
                                .append(", Age: ").append(age).append(", Address: ").append(address);
                    } else {
                        result.append("Không tìm thấy thông tin người dùng cho hình ảnh này.");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result.toString();
    }

    private static boolean isImageFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".gif");
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASSWORD);
    }

    private static String saveImage(byte[] imageBytes) throws IOException {
        String directoryPath = "imageData";
        Path directory = Paths.get(directoryPath);
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }

        int imageCount = directory.toFile().list().length + 1;
        String imagePath = directoryPath + File.separator + imageCount + ".jpg";
        try (FileOutputStream fos = new FileOutputStream(imagePath)) {
            fos.write(imageBytes);
        }
        return imagePath;
    }

    private static void saveToDatabase(String ten, String tuoi, String diaChi, String imagePath) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String sql = "INSERT INTO users (name, age, address, image_directory_path) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, ten);
            pstmt.setInt(2, Integer.parseInt(tuoi));
            pstmt.setString(3, diaChi);
            pstmt.setString(4, imagePath);

            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                int id = rs.getInt(1);
                System.out.println("Người dùng mới được thêm vào với ID: " + id);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
