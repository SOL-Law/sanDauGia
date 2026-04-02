package server.dao;

public class AuctionDao {

    // 1. Hàm giả lập: Luôn báo là phiên đấu giá đang mở
    public static boolean isAuctionRunning(int auctionId) {
        return true;
    }

    // 2. Hàm giả lập: Mặc định giá món hàng đang là 50 VNĐ
    public static double getCurrentPrice(int auctionId) {
        return 50.0;
    }

    // 3. Hàm giả lập: In ra màn hình thay vì lưu thật xuống DB
    public static void placeBid(int auctionId, int userId, double bidPrice) {
        System.out.println("💾 [DATABASE GIẢ LẬP]: Nhận lệnh lưu DB - User " + userId + " đặt giá " + bidPrice);
    }
}