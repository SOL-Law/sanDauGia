package server.util;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {
    public static Connection getConnection() {
        try {
            // Lấy từ MYSQL_PUBLIC_URL của Railway
            String host = "mainline.proxy.rlwy.net";
            String port = "17571";
            String dbName = "railway";
            String user = "root";
            String pass = "DVWWYfCJZheQAAqasoyZxXgGXFeLteFl";

            // Lắp ráp lại thành URL chuẩn của Java JDBC
            String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName;

            Connection conn = DriverManager.getConnection(url, user, pass);
            System.out.println("✅ Ngon! Ống hút đã thông lên CLOUD DATABASE!");
            return conn;
        } catch (Exception e) {
            System.out.println("❌ Oẳng! Lỗi kết nối Cloud rồi!");
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        getConnection(); // Chạy thử cái này xem có ra chữ Xanh không nhé!
    }
}