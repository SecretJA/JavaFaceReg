# Hệ Thống Nhận Diện Khuôn Mặt Thông Minh

## Tổng quan dự án

Hệ thống nhận diện khuôn mặt là một ứng dụng Java được phát triển nhằm giúp xác định danh tính người dùng dựa trên công nghệ nhận diện khuôn mặt. Với mô hình Client-Server, hệ thống cho phép người dùng đăng ký thông tin cá nhân cùng ảnh khuôn mặt và thực hiện nhận diện chính xác thông qua camera hoặc file ảnh. 

Mục tiêu dự án là tạo ra giải pháp nhận diện nhanh chóng, tiện lợi, phù hợp với các ứng dụng thực tế như kiểm soát ra vào, chấm công, quản lý an ninh,...

## Điểm nổi bật

- **Nhận diện thời gian thực** trực tiếp từ camera, cho kết quả nhanh chóng.
- Hỗ trợ nhận diện từ **đa dạng định dạng ảnh** (JPG, PNG, JPEG).
- Sử dụng **Face++ API** - một trong những công nghệ AI nhận diện khuôn mặt hàng đầu thế giới, giúp đảm bảo độ chính xác cao.
- Quản lý và lưu trữ dữ liệu người dùng hiệu quả với **MySQL**.
- **Kiến trúc Client-Server** rõ ràng, dễ dàng mở rộng và bảo trì.
- Giao diện người dùng trực quan, thân thiện được xây dựng bằng **Java Swing** với khả năng hỗ trợ nhiều cửa sổ làm việc đồng thời.

## Cách hoạt động

### Kiến trúc tổng thể

```
┌─────────────────┐    Socket    ┌─────────────────┐    HTTP API    ┌─────────────────┐
│   Client App    │────────────▶ │   Java Server   │────────────▶ │   Face++ API    │
│   (Swing GUI)   │              │   (Port 12345)  │              │   (AI Engine)   │
└─────────────────┘              └─────────────────┘              └─────────────────┘
         │                                │                                │
         ▼                                ▼                                ▼
┌─────────────────┐              ┌─────────────────┐              ┌─────────────────┐
│   Camera/File   │              │   MySQL DB      │              │   AI Models     │
│   Input         │              │   (User Data)   │              │   (Face Detect) │
└─────────────────┘              └─────────────────┘              └─────────────────┘
```

### Quy trình chính

1. **Đăng ký người dùng mới:** 
   Người dùng nhập thông tin cá nhân, chụp ảnh hoặc tải ảnh lên → Gửi server xử lý → Lưu trữ vào cơ sở dữ liệu → Phản hồi kết quả cho người dùng.

2. **Nhận diện khuôn mặt:** 
   Ảnh đầu vào được gửi từ client → Server gọi Face++ API so sánh với ảnh đã lưu trong cơ sở dữ liệu → Trả về kết quả nhận diện chính xác → Hiển thị trên giao diện người dùng.

## Các thành phần chính

- **Client Side:** Giao diện người dùng xây dựng bằng Java Swing, bao gồm các module chính như:
  - `MainFrame.java`: Giao diện chính với các tùy chọn chức năng.
  - `ClientForm.java`: Đăng ký thông tin và chụp ảnh.
  - `CameraCapture.java`: Lấy ảnh từ camera.
  - `ImageClient.java`: Nhận diện hình ảnh từ file.

- **Server Side:** Xử lý logic nhận diện và quản lý dữ liệu người dùng.
  - `FaceRecognitionServer.java`: Server chính nhận và xử lý yêu cầu.
  - Quản lý cơ sở dữ liệu MySQL, xử lý hình ảnh.
  - Tích hợp Face++ API để nhận diện khuôn mặt chính xác.

## Công nghệ sử dụng

- **Backend:** Java, MySQL, OpenCV, Face++ API
- **Frontend:** Java Swing, AWT, Socket Programming
- **External API:** Face++ Compare API, HTTP Client

## Ưu điểm và tiềm năng phát triển

- Sử dụng công nghệ AI tiên tiến kết hợp OpenCV giúp nhận diện nhanh và chính xác.
- Kiến trúc có thể mở rộng, hỗ trợ đa nền tảng (Windows, Linux, macOS).
- Giao diện trực quan, nhiều cửa sổ hỗ trợ đa nhiệm.
- Bảo mật dữ liệu người dùng với hệ quản trị MySQL và quản lý kết nối hiệu quả.
- Có thể phát triển thêm các tính năng bảo mật nâng cao, tích hợp lưu trữ đám mây, ứng dụng di động và giao diện web trong tương lai.

## Ứng dụng thực tế

Hệ thống có thể áp dụng trong nhiều lĩnh vực như:

- Doanh nghiệp: chấm công, kiểm soát ra vào
- Giáo dục: điểm danh sinh viên, quản lý học sinh
- Y tế: xác định bệnh nhân, hồ sơ bệnh án
- An ninh dân cư: kiểm soát cư dân, giám sát an ninh
- Sự kiện: check-in tự động, quản lý vé

---

Hệ thống nhận diện khuôn mặt này không chỉ là dự án học tập mà còn là một giải pháp công nghệ thực tế, mang lại hiệu quả cao và có tiềm năng phát triển trong nhiều lĩnh vực khác nhau.
