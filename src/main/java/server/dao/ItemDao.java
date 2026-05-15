package server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ItemDao {

    // ==========================================
    // 1. LƯU SẢN PHẨM MỚI VÀO DATABASE
    // ==========================================
    public static boolean insertItem(String name, int startPrice, String imageBase64) {
        try (Connection conn = server.util.DBConnection.getConnection()) {
            String sql = "INSERT INTO items (name, current_price, highest_bidder, image_base64, status) VALUES (?, ?, 'None', ?, 'ACTIVE')";
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, name);
            stmt.setInt(2, startPrice);
            stmt.setString(3, imageBase64);

            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Lỗi lưu sản phẩm: " + e.getMessage());
            return false;
        }
    }

    // ==========================================
    // 2. LẤY TOÀN BỘ SẢN PHẨM LÚC BẬT SERVER
    // ==========================================
    public static void loadAllItemsToManager() {
        try (Connection conn = server.util.DBConnection.getConnection()) {
            String sql = "SELECT name, current_price, image_base64 FROM items WHERE status = 'ACTIVE'";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            model.AuctionManager manager = model.AuctionManager.getInstance();
            int count = 0;

            while (rs.next()) {
                String name = rs.getString("name");
                int price = rs.getInt("current_price");
                String image = rs.getString("image_base64");

                manager.addItem(name, price, image);
                count++;
            }
            System.out.println(" Đã tải thành công " + count + " sản phẩm từ Database lên Server!");

        } catch (Exception e) {
            System.out.println("Lỗi tải danh sách sản phẩm: " + e.getMessage());
        }
    }
    // ==========================================
    // KHÓA SỔ SẢN PHẨM SAU KHI HẾT GIỜ
    // ==========================================
    public static boolean finishItem(String itemName) {
        try (Connection conn = server.util.DBConnection.getConnection()) {
            String sql = "UPDATE items SET status = 'FINISHED' WHERE name = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, itemName);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            return false;
        }
    }
    // ==========================================
    // 3. LƯU LỊCH SỬ VÀ CẬP NHẬT GIÁ MỚI NHẤT
    // ==========================================
    public static boolean insertBidHistory(String itemName, String username, int price) {
        try (java.sql.Connection conn = server.util.DBConnection.getConnection()) {

            // BƯỚC 1: Ghi vào bảng lịch sử (bids) như cũ
            String sql1 = "INSERT INTO bids (item_id, username, bid_amount) VALUES ((SELECT id FROM items WHERE name = ? LIMIT 1), ?, ?)";
            java.sql.PreparedStatement stmt1 = conn.prepareStatement(sql1);
            stmt1.setString(1, itemName);
            stmt1.setString(2, username);
            stmt1.setInt(3, price);
            stmt1.executeUpdate();

            // Cập nhật giá mới và người dẫn đầu vào bảng items
            String sql2 = "UPDATE items SET current_price = ?, highest_bidder = ? WHERE name = ?";
            java.sql.PreparedStatement stmt2 = conn.prepareStatement(sql2);
            stmt2.setInt(1, price);
            stmt2.setString(2, username);
            stmt2.setString(3, itemName);
            stmt2.executeUpdate(); // Chạy lệnh Update

            return true;
        } catch (Exception e) {
            System.out.println("Lỗi lưu lịch sử và cập nhật giá: " + e.getMessage());
            return false;
        }
    }
    // ==========================================
    // 4. LẤY LỊCH SỬ GIAO DỊCH TỪ DATABASE
    // ==========================================
    public static String getAuctionHistory() {
        StringBuilder sb = new StringBuilder();
        try (java.sql.Connection conn = server.util.DBConnection.getConnection()) {
            // Câu lệnh SQL móc nối bảng bids và items để lấy tên thật của đồ vật
            String sql = "SELECT i.name, b.username, b.bid_amount, b.bid_time " +
                    "FROM bids b JOIN items i ON b.item_id = i.id " +
                    "ORDER BY b.bid_time DESC LIMIT 50";

            java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
            java.sql.ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String itemName = rs.getString("name");
                String user = rs.getString("username");
                int price = rs.getInt("bid_amount");
                String time = rs.getString("bid_time");

                // Cắt bớt phần mili-giây của time cho đẹp (Ví dụ: 2026-05-15 10:30:00)
                if (time != null && time.length() > 19) {
                    time = time.substring(0, 19);
                }

                sb.append(String.format("[%s] %s %s -> %d VNĐ\n\n", time, user, itemName, price));
            }
        } catch (Exception e) {
            System.out.println("Lỗi đọc lịch sử: " + e.getMessage());
        }
        return sb.toString();
    }
    // ==========================================
    // 5. LẤY LỊCH SỬ CỦA RIÊNG MỘT NGƯỜI
    // ==========================================
    public static String getPersonalHistory(String username) {
        StringBuilder sb = new StringBuilder();
        try (java.sql.Connection conn = server.util.DBConnection.getConnection()) {
            // Dùng WHERE b.username = ? để lọc đúng người đó
            String sql = "SELECT i.name, b.bid_amount, b.bid_time " +
                    "FROM bids b JOIN items i ON b.item_id = i.id " +
                    "WHERE b.username = ? ORDER BY b.bid_time DESC LIMIT 50";

            java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            java.sql.ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String itemName = rs.getString("name");
                int price = rs.getInt("bid_amount");
                String time = rs.getString("bid_time");

                // Cắt đuôi mili-giây cho đẹp
                if (time != null && time.length() > 19) {
                    time = time.substring(0, 19);
                }

                sb.append(String.format("[%s] Bạn đã trả giá:\n %s -> %d VNĐ\n\n", time, itemName, price));
            }

            // Nếu chưa mua gì thì báo cho người ta biết
            if (sb.length() == 0) {
                return "Bạn chưa tham gia đấu giá món đồ nào!";
            }

        } catch (Exception e) {
            System.out.println("Lỗi đọc lịch sử cá nhân: " + e.getMessage());
        }
        return sb.toString();
    }
    // ==========================================
    // 6. CẬP NHẬT TRẠNG THÁI MÓN ĐỒ
    // ==========================================
    public static void updateItemStatus(String itemName, String status) {
        try (java.sql.Connection conn = server.util.DBConnection.getConnection()) {
            String sql = "UPDATE items SET status = ? WHERE name = ?";
            java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, status);
            stmt.setString(2, itemName);
            stmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Lỗi cập nhật trạng thái đồ vật: " + e.getMessage());
        }
    }
}
