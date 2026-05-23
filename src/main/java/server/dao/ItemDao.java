package server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ItemDao {

    // ==========================================
    // 1. LƯU SẢN PHẨM MỚI ( ĐÃ THÊM NGƯỜI BÁN)
    // ==========================================
    // Đã thêm sellerName và category
    public static boolean insertItem(String name, int startPrice, String imageBase64, String sellerName, String category) {
        try (Connection conn = server.util.DBConnection.getConnection()) {
            String sql = "INSERT INTO items (name, current_price, highest_bidder, image_base64, status, seller_name, category) VALUES (?, ?, 'None', ?, 'ACTIVE', ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setInt(2, startPrice);
            stmt.setString(3, imageBase64);
            stmt.setString(4, sellerName);
            stmt.setString(5, category);   // Lưu thêm Danh mục
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
        try (java.sql.Connection conn = server.util.DBConnection.getConnection()) {
            //  BƯỚC 1: Đã thêm highest_bidder vào câu lệnh SQL
            String sql = "SELECT name, current_price, image_base64, seller_name, category, highest_bidder FROM items WHERE status = 'ACTIVE'";
            java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
            java.sql.ResultSet rs = stmt.executeQuery();

            model.AuctionManager manager = model.AuctionManager.getInstance();
            int count = 0;
            while (rs.next()) {
                String name = rs.getString("name");
                int price = rs.getInt("current_price");
                String image = rs.getString("image_base64");
                String seller = rs.getString("seller_name");
                String category = rs.getString("category");

                //  BƯỚC 2: Hứng tên người dẫn đầu từ DB
                String leader = rs.getString("highest_bidder");
                if (leader == null || leader.trim().isEmpty()) leader = "None";

                //  BƯỚC 3: Dùng hàm mới để truyền cả Leader lên RAM
                manager.loadItemFromDB(name, price, image, 3600, seller, category, leader);
                manager.startAuctionTimer(name, 3600); // Mặc định cho sống 1 tiếng (3600s)
                count++;
            }
            System.out.println(" Đã tải thành công " + count + " sản phẩm từ Database lên Server!");
        } catch (Exception e) {
            System.out.println("Lỗi tải danh sách sản phẩm: " + e.getMessage());
        }
    }
    // ==========================================
    // 3. KẾT THÚC PHIÊN VÀ CHUYỂN TIỀN (TRỪ MUA, CỘNG BÁN)
    // ==========================================
    public static void endAuctionAndSettlePayment(String itemName) {
        try (java.sql.Connection conn = server.util.DBConnection.getConnection()) {

            // Lấy thông tin Món đồ: Ai đang dẫn đầu, Giá bao nhiêu, Ai là người đăng bán?
            String sqlInfo = "SELECT highest_bidder, current_price, seller_name FROM items WHERE name = ?";
            java.sql.PreparedStatement stmtInfo = conn.prepareStatement(sqlInfo);
            stmtInfo.setString(1, itemName);
            java.sql.ResultSet rs = stmtInfo.executeQuery();

            if (rs.next()) {
                String winner = rs.getString("highest_bidder");
                int price = rs.getInt("current_price");
                String seller = rs.getString("seller_name");

                // Nếu có người mua (không phải là 'None')
                if (winner != null && !winner.equals("None")) {

                    // A. TRỪ TIỀN NGƯỜI THẮNG
                    String sqlDeduct = "UPDATE users SET balance = balance - ? WHERE username = ?";
                    java.sql.PreparedStatement stmtDeduct = conn.prepareStatement(sqlDeduct);
                    stmtDeduct.setInt(1, price);
                    stmtDeduct.setString(2, winner);
                    stmtDeduct.executeUpdate();

                    // B. CỘNG TIỀN NGƯỜI BÁN
                    if (seller != null && !seller.isEmpty()) {
                        String sqlAdd = "UPDATE users SET balance = balance + ? WHERE username = ?";
                        java.sql.PreparedStatement stmtAdd = conn.prepareStatement(sqlAdd);
                        stmtAdd.setInt(1, price);
                        stmtAdd.setString(2, seller);
                        stmtAdd.executeUpdate();
                    }

                    System.out.println(" Thanh toán thành công: [" + winner + "] đã trả " + price + " cho [" + seller + "]");

                    // Chuyển trạng thái món đồ thành ĐÃ THANH TOÁN (PAID)
                    String sqlStatus = "UPDATE items SET status = 'PAID' WHERE name = ?";
                    java.sql.PreparedStatement stmtStatus = conn.prepareStatement(sqlStatus);
                    stmtStatus.setString(1, itemName);
                    stmtStatus.executeUpdate();
                } else {
                    // Nếu không có ai mua thì đổi trạng thái thành HỦY (CANCELED) hoặc FINISHED
                    String sqlStatus = "UPDATE items SET status = 'CANCELED' WHERE name = ?";
                    java.sql.PreparedStatement stmtStatus = conn.prepareStatement(sqlStatus);
                    stmtStatus.setString(1, itemName);
                    stmtStatus.executeUpdate();
                }
            }
        } catch (Exception e) {
            System.out.println("Lỗi thanh toán kết thúc phiên: " + e.getMessage());
        }
    }

    // ==========================================
    // 4. LƯU LỊCH SỬ VÀ CẬP NHẬT GIÁ MỚI NHẤT
    // ==========================================
    public static boolean insertBidHistory(String itemName, String username, int price) {
        try (java.sql.Connection conn = server.util.DBConnection.getConnection()) {
            String sql1 = "INSERT INTO bids (item_id, username, bid_amount) VALUES ((SELECT id FROM items WHERE name = ? LIMIT 1), ?, ?)";
            java.sql.PreparedStatement stmt1 = conn.prepareStatement(sql1);
            stmt1.setString(1, itemName);
            stmt1.setString(2, username);
            stmt1.setInt(3, price);
            stmt1.executeUpdate();

            String sql2 = "UPDATE items SET current_price = ?, highest_bidder = ? WHERE name = ?";
            java.sql.PreparedStatement stmt2 = conn.prepareStatement(sql2);
            stmt2.setInt(1, price);
            stmt2.setString(2, username);
            stmt2.setString(3, itemName);
            stmt2.executeUpdate();

            return true;
        } catch (Exception e) {
            System.out.println("Lỗi lưu lịch sử và cập nhật giá: " + e.getMessage());
            return false;
        }
    }

    // ==========================================
    // 5. LẤY LỊCH SỬ CỦA RIÊNG MỘT NGƯỜI
    // ==========================================
    public static String getPersonalHistory(String username) {
        StringBuilder sb = new StringBuilder();
        try (java.sql.Connection conn = server.util.DBConnection.getConnection()) {
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

                if (time != null && time.length() > 19) {
                    time = time.substring(0, 19);
                }
                sb.append(String.format("[%s] Bạn đã trả giá:\n %s -> %d VNĐ\n\n", time, itemName, price));
            }
            if (sb.length() == 0) return "Bạn chưa tham gia đấu giá món đồ nào!";
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

    // ==========================================
    // 7. LẤY DỮ LIỆU VẼ BIỂU ĐỒ (LINE CHART)
    // ==========================================
    public static String getChartData(String itemName) {
        StringBuilder sb = new StringBuilder();
        try (java.sql.Connection conn = server.util.DBConnection.getConnection()) {
            String sql = "SELECT bid_amount, bid_time FROM bids b JOIN items i ON b.item_id = i.id WHERE i.name = ? ORDER BY bid_time ASC";
            java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, itemName);
            java.sql.ResultSet rs = stmt.executeQuery();

            boolean hasBid = false;
            while (rs.next()) {
                hasBid = true;
                int price = rs.getInt("bid_amount");
                String time = rs.getString("bid_time");
                if (time != null && time.length() >= 19) time = time.substring(11, 19);
                sb.append(time).append("-").append(price).append(",");
            }
            if (!hasBid) {
                String sql2 = "SELECT current_price FROM items WHERE name = ?";
                java.sql.PreparedStatement stmt2 = conn.prepareStatement(sql2);
                stmt2.setString(1, itemName);
                java.sql.ResultSet rs2 = stmt2.executeQuery();
                if(rs2.next()){
                    sb.append("Bắt đầu-").append(rs2.getInt("current_price")).append(",");
                }
            }
        } catch (Exception e) {
            System.out.println("Lỗi đọc data biểu đồ: " + e.getMessage());
        }
        return sb.toString();
    }

    // ==========================================
    // 8. XÓA SẢN PHẨM (Xóa lịch sử bid để tránh khóa ngoại)
    // ==========================================
    public static boolean deleteItem(String itemName) {
        try (java.sql.Connection conn = server.util.DBConnection.getConnection()) {
            String sqlBids = "DELETE FROM bids WHERE item_id = (SELECT id FROM items WHERE name = ? LIMIT 1)";
            java.sql.PreparedStatement stmtBids = conn.prepareStatement(sqlBids);
            stmtBids.setString(1, itemName);
            stmtBids.executeUpdate();

            String sqlItem = "DELETE FROM items WHERE name = ?";
            java.sql.PreparedStatement stmtItem = conn.prepareStatement(sqlItem);
            stmtItem.setString(1, itemName);
            return stmtItem.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Lỗi SQL khi xóa sản phẩm: " + e.getMessage());
            return false;
        }
    }

    // ==========================================
    // 9. CHỈ SỬA TÊN SẢN PHẨM
    // ==========================================
    public static boolean updateItemDetails(String oldName, String newName) {
        try (java.sql.Connection conn = server.util.DBConnection.getConnection()) {
            // Câu lệnh SQL giờ chỉ UPDATE mỗi cột name
            String sql = "UPDATE items SET name = ? WHERE name = ?";
            java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, newName);
            stmt.setString(2, oldName);

            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Lỗi SQL khi sửa tên sản phẩm: " + e.getMessage());
            return false;
        }
    }
    // ==========================================
    // 10. KHÔI PHỤC ĐỒ VẬT (Dành riêng cho ADMIN)
    // ==========================================
    public static java.util.Map<String, String> getItemForRestore(String itemName) {
        java.util.Map<String, String> info = new java.util.HashMap<>();
        try (java.sql.Connection conn = server.util.DBConnection.getConnection()) {
            //  Móc thêm highest_bidder
            String sql = "SELECT current_price, image_base64, seller_name, category, highest_bidder FROM items WHERE name = ?";
            java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, itemName);
            java.sql.ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                info.put("price", String.valueOf(rs.getInt("current_price")));
                info.put("image", rs.getString("image_base64"));
                info.put("seller", rs.getString("seller_name"));
                info.put("category", rs.getString("category"));

                //  Lấy tên người dẫn đầu
                String leader = rs.getString("highest_bidder");
                info.put("leader", (leader == null || leader.trim().isEmpty()) ? "None" : leader);

                java.sql.PreparedStatement stmt2 = conn.prepareStatement("UPDATE items SET status = 'ACTIVE' WHERE name = ?");
                stmt2.setString(1, itemName);
                stmt2.executeUpdate();
            }
        } catch (Exception e) {}
        return info;
    }
}