package server.util;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {
    public static Connection getConnection() {
        try {
            // URL chuẩn 100% cho Aiven (Dùng sslMode=REQUIRED theo đúng yêu cầu của Aiven)
            String url = "jdbc:mysql://mysql-sandaugia36-database.a.aivencloud.com:17257/defaultdb?sslMode=REQUIRED";

            // Tài khoản
            String user = "avnadmin";

            // Pass lấy chính xác từ ảnh của bạn, KHÔNG CÓ DẤU CÁCH thừa
            String pass = "AVNS_1mTgrMcT9Wj5tWLcFRI";

            Connection conn = DriverManager.getConnection(url, user, pass);
            System.out.println("Ngon! Ống hút đã thông lên AIVEN CLOUD DATABASE!");
            return conn;
        } catch (Exception e) {
            System.out.println("Hỏng! Lỗi kết nối Cloud Aiven rồi!");
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        getConnection();
    }
}