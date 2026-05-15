package model;

public class BidInfo {

    private String item;
    private int currentPrice;
    private String leader;
    private String base64Image;
    private int remainingTime; // MỚI THÊM: Lưu thời gian còn lại

    // ✅ Constructor
    public BidInfo(String item, int currentPrice, String leader, String base64Image, int remainingTime) {
        this.item = item;
        this.currentPrice = currentPrice;
        this.leader = leader;
        this.base64Image = base64Image;
        this.remainingTime = remainingTime;
    }

    // ✅ Getter
    public String getItem() { return item; }
    public int getCurrentPrice() { return currentPrice; }
    public String getLeader() { return leader; }
    public String getBase64Image() { return (base64Image == null) ? "" : base64Image; }
    public int getRemainingTime() { return remainingTime; } // MỚI THÊM

    // ✅ Setter
    public void setCurrentPrice(int currentPrice) { this.currentPrice = currentPrice; }
    public void setLeader(String leader) { this.leader = leader; }
    public void setRemainingTime(int time) { this.remainingTime = time; } // MỚI THÊM
}