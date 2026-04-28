package model;

public class BidInfo {

    private String item;
    private int currentPrice;
    private String leader;

    private String base64Image;


    // ✅ Constructor đúng
    public BidInfo(String item, int currentPrice, String leader , String base64Image) {
        this.item = item;
        this.currentPrice = currentPrice;
        this.leader = leader;
        this.base64Image = base64Image;
    }

    // ✅ Getter
    public String getItem() {
        return item;
    }

    public int getCurrentPrice() {
        return currentPrice;
    }

    public String getLeader() {
        return leader;
    }

    public String getBase64Image() { return (base64Image == null) ? "" : base64Image;}

    // ✅ Setter
    public void setCurrentPrice(int currentPrice) {
        this.currentPrice = currentPrice;
    }

    public void setLeader(String leader) {
        this.leader = leader;
    }
}