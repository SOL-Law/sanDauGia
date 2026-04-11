package model;

public class BidInfo {

    private String item;
    private int currentPrice;
    private String leader;

    // ✅ Constructor đúng
    public BidInfo(String item, int currentPrice, String leader) {
        this.item = item;
        this.currentPrice = currentPrice;
        this.leader = leader;
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

    // ✅ Setter
    public void setCurrentPrice(int currentPrice) {
        this.currentPrice = currentPrice;
    }

    public void setLeader(String leader) {
        this.leader = leader;
    }
}