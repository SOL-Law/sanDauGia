package server.dao;

import server.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDao {

    // ==========================================
    // LOGIN (TRẢ VỀ ROLE)
    // ==========================================
    public static String login(String username, String password) {
        try (Connection conn = DBConnection.getConnection()) {

            String sql = "SELECT role FROM users WHERE username = ? AND password = ?";
            PreparedStatement pst = conn.prepareStatement(sql);

            pst.setString(1, username);
            pst.setString(2, password);

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");

                System.out.println("🗄️ Login OK: " + username + " | Role: " + role);
                return role; // 🔥 trả role
            }

        } catch (Exception e) {
            System.out.println("Lỗi Database: " + e.getMessage());
        }

        return null; // login fail
    }

    // ==========================================
    // REGISTER
    // ==========================================
    public static boolean register(String username, String password) {
        try (Connection conn = DBConnection.getConnection()) {

            // check trùng username
            String checkSql = "SELECT * FROM users WHERE username = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, username);

            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                return false;
            }

            // insert user (role mặc định BIDDER)
            String insertSql = "INSERT INTO users (username, password, email, role) VALUES (?, ?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertSql);

            insertStmt.setString(1, username);
            insertStmt.setString(2, password);
            insertStmt.setString(3, "newuser@email.com");
            insertStmt.setString(4, "BIDDER"); // 🔥 mặc định

            int rows = insertStmt.executeUpdate();

            if (rows > 0) {
                System.out.println("🗄️ Tạo user: " + username);
                return true;
            }

        } catch (Exception e) {
            System.out.println("Lỗi Database khi đăng ký: " + e.getMessage());
        }

        return false;
    }
}