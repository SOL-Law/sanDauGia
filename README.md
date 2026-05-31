HỆ THỐNG ĐẤU GIÁ TRỰC TUYẾN (ONLINE AUCTION SYSTEM)
Chào mọi người! Đây là repo chứa source code bài tập lớn môn Lập trình nâng cao của nhóm. Tụi mình xây dựng một sàn đấu giá thời gian thực (real-time) dựa trên mô hình Client-Server.

1. TỔNG QUAN BÀI TOÁN
Hệ thống mô phỏng một sàn đấu giá thực tế, nơi mọi thay đổi về giá đều được "bắn" ngay lập tức đến tất cả người chơi. App chia làm 3 role (quyền) chính:

Người bán (Seller): Quăng sản phẩm lên sàn, set giá khởi điểm, thời gian và ngồi xem thiên hạ đấu giá.

Người mua (Bidder): Vào tranh giá (bid), theo dõi biểu đồ giá chạy real-time và xem lại lịch sử "tiêu tiền".

Quản trị viên (Admin): Nắm quyền to nhất. Quản lý user, xóa/sửa item và có quyền "hồi sinh" các phiên đấu giá đã kết thúc.

Điểm nhấn của project: Có tích hợp bot đấu giá hộ (Auto-Bidding), thuật toán chống nẫng tay trên phút chót (Anti-sniping) và xử lý đồng thời (Thread-safe) cực kỳ chặt chẽ.

2. CÔNG NGHỆ ĐÃ DÙNG
Ngôn ngữ: Java (JDK 17+)

UI/Giao diện: Java Swing (có bọc thêm theme FlatMacDarkLaf cho giao diện Dark Mode nhìn xịn xò như Mac).

Kiến trúc: Client-Server, chia class theo chuẩn MVC.

Giao tiếp mạng: Java Socket & ép kiểu dữ liệu bằng JSON (Gson).

Database: MySQL (Lưu trữ trên đám mây Aiven Cloud).

Build Tool: Maven.

Testing & Deploy: JUnit 5 để viết Unit Test, dùng GitHub Actions cho luồng CI/CD.

3. CẤU TRÚC PROJECT
Project được gom gọn gàng theo form của Maven:

sanDauGia/
├── .github/                  # Chứa script chạy CI/CD (GitHub Actions) tự động test
├── sql/                      # Các script .sql dùng để setup Database từ đầu
├── src/
│   ├── main/java/
│   │   ├── client/           # Code phía Client (UI đăng nhập, phòng đấu giá, lịch sử...)
│   │   ├── frontend/         # Giao diện chính của sàn
│   │   ├── model/            # Các class thực thể (Item, User, Auction, AutoBid...)
│   │   ├── network/          # Đóng gói/mở gói tin (Request/Response)
│   │   └── server/           # Code phía Server (Nhận kết nối, gọi DAO chọc xuống DB)
│   └── test/                 # Chứa các file Unit Test (JUnit 5) kiểm tra logic
├── pom.xml                   # Quản lý thư viện (dependencies)
└── README.md                 # Chính là file bạn đang đọc 
4. HƯỚNG DẪN BUILD & RUN APP
Vì project chạy bằng Maven nên anh em dùng Windows, Linux hay macOS đều chạy chung một lệnh, không lo lỗi môi trường.

Điều kiện: Máy tính đã cài sẵn JDK 17+ và có Maven.

BƯỚC 1: BẬT SERVER
Mở terminal tại thư mục gốc của project (chỗ có file pom.xml) và gõ lệnh:

Bash
mvn compile exec:java -Dexec.mainClass="server.AuctionServer"
(Đợi một chút, khi nào màn hình hiện  SERVER ĐÃ MỞ (Cổng 8888). Đang chờ kết nối... là server đã sẵn sàng).

BƯỚC 2: BẬT CLIENT (NGƯỜI CHƠI)
Mở thêm một cửa sổ terminal mới (tuyệt đối không tắt terminal của server nhé) và gõ lệnh:

Bash
mvn compile exec:java -Dexec.mainClass="client.ui.auth.AuthFrame"
Tips để test chấm điểm: Hãy mở 2-3 terminal rồi chạy lệnh của BƯỚC 2 nhiều lần để giả lập 3 người cùng online vào tranh giá.

5. CÁC TÍNH NĂNG ĐÃ XONG
Tính năng cốt lõi:
[x] Đăng ký, đăng nhập và phân quyền rõ ràng (Admin, Seller, Bidder).

[x] CRUD sản phẩm (Thêm/Sửa/Xóa).

[x] Đấu giá trực tiếp, giá nhảy số tức thời (Real-time) không cần F5.

[x] Tự động khóa phiên khi đồng hồ về 0 và tự trừ tiền/cộng tiền người thắng.

[x] Bắt lỗi mượt mà, hiện pop-up (Toast) thông báo khi nhập sai giá, hết tiền,...

Kỹ thuật nâng cao & Điểm cộng:
[x] Áp dụng Design Patterns: Đã tích hợp Singleton (Cho AuctionManager), Factory Method (Cho ItemFactory), và Observer (Broadcast qua Socket).

[x] Ủy quyền Auto-Bid: Bot tự động giằng co giá thay người chơi dựa trên cấu hình MaxBid và Increment (Dùng cấu trúc PriorityQueue để phân định ai đặt trước).

[x] Concurrent Bidding (An toàn luồng): Đã dùng cơ chế synchronized để khóa chặn (lock), đảm bảo không bị lỗi dữ liệu (Race Condition) khi 10 người cùng bấm đặt giá một lúc.

[x] Anti-sniping: Nếu có thanh niên nào "đợi 3s cuối mới đặt giá", hệ thống sẽ tự cộng thêm 60 giây vào đồng hồ đếm ngược.

[x] Biểu đồ giá (Price Curve): Vẽ Line Chart thể hiện lịch sử giá của món đồ nhảy theo thời gian thực.

[x] Quyền lực của Admin: Có nút đỏ "Khôi phục phiên", hồi sinh các món đồ đã kết thúc về lại sàn đấu (Load lại từ DB lên RAM Server).

6. TÀI LIỆU & VIDEO DEMO
Báo cáo chi tiết (PDF):https://drive.google.com/file/d/15KVo4ksFYFiY8PafVlEbe3239RKN47de/view?usp=sharing

Video chạy thử App:https://youtu.be/wlu2VBUc7yM?si=gxnnJgXEAsDmucxL
