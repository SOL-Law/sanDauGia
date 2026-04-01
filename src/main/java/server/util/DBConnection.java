package server.util;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {
    public static Connection getConnection() {
        try {
            // Nhớ sửa 'root' cuối cùng thành mật khẩu MySQL của đại ca
            // 'sanDauGia' là tên Database  tạo trong MySQL Workbench
            String url = "jdbc:mysql://localhost:3306/sanDauGia";
            String user = "root";
            String pass = "123456";

            Connection conn = DriverManager.getConnection(url, user, pass);
            System.out.println("✅ Ngon! Ống hút đã thông xuống Database!");
            return conn;
        } catch (Exception e) {
            System.out.println("❌ Oẳng! Kiểm tra lại mật khẩu hoặc tên DB đi đại ca.");
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        getConnection(); // Chạy thử phát xem có ra chữ Xanh không
    }
}