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
            String insertSql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertSql);

            insertStmt.setString(1, username);
            insertStmt.setString(2, password);
            insertStmt.setString(3, "BIDDER"); // mặc định

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
    // ==========================================
    // LẤY SỐ DƯ TÀI KHOẢN (GET BALANCE)
    // ==========================================
    public static double getBalance(String username) {
        double balance = 0.0;

        try (Connection conn = server.util.DBConnection.getConnection()) {
            // Lệnh SQL: Tìm vào bảng users, lấy ra cột balance của cái thằng có tên trùng với username
            String sql = "SELECT balance FROM users WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();

            // Nếu tìm thấy thằng đó trong DB, lôi tiền của nó ra
            if (rs.next()) {
                balance = rs.getDouble("balance");
            }

        } catch (Exception e) {
            System.out.println("Lỗi khi lấy số dư từ Database: " + e.getMessage());
        }

        return balance;
    }
    // ==========================================
    // NẠP TIỀN VÀO TÀI KHOẢN
    // ==========================================
    public static boolean deposit(String username, double amount) {
        try (Connection conn = server.util.DBConnection.getConnection()) {
            // Lệnh SQL: Lấy số dư cũ CỘNG THÊM số tiền mới
            String sql = "UPDATE users SET balance = balance + ? WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setDouble(1, amount);
            stmt.setString(2, username);

            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Lỗi bơm tiền: " + e.getMessage());
            return false;
        }
    }
    // ==========================================
    // TRỪ TIỀN NGƯỜI THẮNG ĐẤU GIÁ
    // ==========================================
    public static boolean payForItem(String username, double amount) {
        try (Connection conn = server.util.DBConnection.getConnection()) {
            String sql = "UPDATE users SET balance = balance - ? WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setDouble(1, amount);
            stmt.setString(2, username);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            return false;
        }
    }
}