package server.dao;

import server.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDao {

    // ==========================================
    // 1. HÀM ĐĂNG NHẬP (Kết nối MySQL)
    // ==========================================
    public static boolean login(String username, String password) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, username);
            pst.setString(2, password);

            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                System.out.println("🗄️ [DATABASE]: Đã xác minh tài khoản: " + username);
                return true;
            }
        } catch (Exception e) {
            System.out.println("Lỗi Database: " + e.getMessage());
        }
        return false;
    }

    // ==========================================
    // 2. HÀM ĐĂNG KÝ (Kết nối MySQL)
    // ==========================================
    public static boolean register(String username, String password) {
        try (Connection conn = DBConnection.getConnection()) {
            // Bước 1: Kiểm tra xem tên đăng nhập đã ai dùng chưa
            String checkSql = "SELECT * FROM users WHERE username = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                return false; // Tên đã tồn tại trong MySQL -> Từ chối!
            }

            // Bước 2: Nếu chưa ai dùng, lưu tài khoản mới vào MySQL
            String insertSql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertSql);
            insertStmt.setString(1, username);
            insertStmt.setString(2, password);
            insertStmt.setString(3, "newuser@email.com"); // Email mặc định

            int rows = insertStmt.executeUpdate();
            if (rows > 0) {
                System.out.println("🗄️ [DATABASE]: Đã tạo tài khoản mới: " + username);
                return true;
            }
        } catch (Exception e) {
            System.out.println("Lỗi Database khi đăng ký: " + e.getMessage());
        }
        return false;
    }
}