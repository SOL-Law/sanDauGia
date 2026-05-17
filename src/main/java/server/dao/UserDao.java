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

                System.out.println("️ Login OK: " + username + " | Role: " + role);
                return role; //  trả role
            }

        } catch (Exception e) {
            System.out.println("Lỗi Database: " + e.getMessage());
        }

        return null; // login fail
    }

    // ==========================================
    // REGISTER
    // ==========================================
    public static boolean register(String username, String password, String role) {
        try (java.sql.Connection conn = server.util.DBConnection.getConnection()) {

            // check trùng username
            String checkSql = "SELECT * FROM users WHERE username = ?";
            java.sql.PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, username);

            java.sql.ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                return false;
            }

            // insert user với role được người dùng chọn
            String insertSql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
            java.sql.PreparedStatement insertStmt = conn.prepareStatement(insertSql);

            insertStmt.setString(1, username);
            insertStmt.setString(2, password);
            insertStmt.setString(3, role);

            int rows = insertStmt.executeUpdate();

            if (rows > 0) {
                System.out.println("🗄 Tạo user: " + username + " | Quyền: " + role);
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
    // ==========================================
    // LẤY ID TÀI KHOẢN TỪ DATABASE
    // ==========================================
    public static String getUserId(String username) {
        try (java.sql.Connection conn = server.util.DBConnection.getConnection()) {
            String sql = "SELECT id FROM users WHERE username = ?";
            java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            java.sql.ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return String.valueOf(rs.getInt("id"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "N/A";
    }

    // ==========================================
    // CẬP NHẬT TÊN VÀ MẬT KHẨU (ĐỒNG BỘ MỌI BẢNG)
    // ==========================================
    public static boolean updateProfile(String oldUser, String newUser, String newPass) {
        try (java.sql.Connection conn = server.util.DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Bật chế độ Cập nhật đồng loạt (Tránh lỗi đứt gánh giữa chừng)

            // 1. Cập nhật bảng Users (Đổi pass nếu có nhập)
            String sql1 = newPass.isEmpty()
                    ? "UPDATE users SET username = ? WHERE username = ?"
                    : "UPDATE users SET username = ?, password = ? WHERE username = ?";
            java.sql.PreparedStatement stmt1 = conn.prepareStatement(sql1);
            stmt1.setString(1, newUser);
            if(newPass.isEmpty()) {
                stmt1.setString(2, oldUser);
            } else {
                stmt1.setString(2, newPass);
                stmt1.setString(3, oldUser);
            }
            stmt1.executeUpdate();

            // 2. Nếu thực sự có đổi Tên, phải đổi luôn tên ở các bảng khác để không bị lỗi lịch sử
            if (!oldUser.equals(newUser)) {
                // Đổi tên người dẫn đầu
                java.sql.PreparedStatement s2 = conn.prepareStatement("UPDATE items SET highest_bidder = ? WHERE highest_bidder = ?");
                s2.setString(1, newUser); s2.setString(2, oldUser); s2.executeUpdate();

                // Đổi tên người đăng bán
                java.sql.PreparedStatement s3 = conn.prepareStatement("UPDATE items SET seller_name = ? WHERE seller_name = ?");
                s3.setString(1, newUser); s3.setString(2, oldUser); s3.executeUpdate();

                // Đổi tên lịch sử đặt giá
                java.sql.PreparedStatement s4 = conn.prepareStatement("UPDATE bids SET username = ? WHERE username = ?");
                s4.setString(1, newUser); s4.setString(2, oldUser); s4.executeUpdate();
            }

            conn.commit(); // Chốt lưu tất cả!
            return true;
        } catch (Exception e) {
            System.out.println("Lỗi cập nhật Profile: " + e.getMessage());
            return false;
        }
    }
}