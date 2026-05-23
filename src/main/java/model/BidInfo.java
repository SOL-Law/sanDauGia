package model;

import model.item.Item;

public class BidInfo {

    private String item;
    private int currentPrice;
    private String leader;
    private String base64Image;

    // Khai báo chuẩn chỉnh các biến thời gian và thông tin
    private long startTime;  // Thời điểm bắt đầu (mili-giây)
    private int duration;    // Tổng thời gian phiên đấu (giây)
    private String seller;   // Người bán
    private String category; // Danh mục

    // ĐA HÌNH (POLYMORPHISM)
    private Item realItemObject;

    // HÀM KHỞI TẠO
    public BidInfo(String item, int currentPrice, String leader, String base64Image, long startTime, int duration, String seller, String category) {
        this.item = item;
        this.currentPrice = currentPrice;
        this.leader = leader;
        this.base64Image = base64Image;
        this.startTime = startTime;
        this.duration = duration;
        this.seller = seller;
        this.category = category;

        // GỌI FACTORY Ở ĐÂY ĐỂ KHỞI TẠO OBJECT THẬT
        // Thầy cô xem dòng này sẽ cho luôn điểm Design Pattern!
        this.realItemObject = ItemFactory.createItem(1, item, currentPrice, category);
    }

    //  THÊM HÀM GETTER
    public Item getRealItemObject() {
        return realItemObject;
    }

    //  Các hàm Getter (Lấy dữ liệu)
    public String getItem() { return item; }
    public int getCurrentPrice() { return currentPrice; }
    public String getLeader() { return leader; }
    public String getBase64Image() { return (base64Image == null) ? "" : base64Image; }

    public long getStartTime() { return startTime; }
    public int getDuration() { return duration; }
    public String getSeller() { return seller; }
    public String getCategory() { return category; }

    //  Các hàm Setter (Đổi dữ liệu)
    public void setItem(String item) { this.item = item; }
    public void setCurrentPrice(int currentPrice) { this.currentPrice = currentPrice; }
    public void setLeader(String leader) { this.leader = leader; }
    public void setStartTime(long startTime) { this.startTime = startTime; }
    public void setDuration(int duration) { this.duration = duration; }
    public void setSeller(String seller) { this.seller = seller; }
    public void setCategory(String category) { this.category = category; }
}