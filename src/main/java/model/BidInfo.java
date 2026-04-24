package model;

public class BidInfo {
    private String itemName;
    private int currentPrice;
    private String leader;

    public BidInfo(String itemName, int currentPrice, String leader) {
        this.itemName = itemName;
        this.currentPrice = currentPrice;
        this.leader = leader;
    }

    public String getItemName() {
        return itemName;
    }

    public int getCurrentPrice() {
        return currentPrice;
    }

    public String getLeader() {
        return leader;
    }

    public void setCurrentPrice(int currentPrice) {
        this.currentPrice = currentPrice;
    }

    public void setLeader(String leader) {
        this.leader = leader;
    }

    @Override
    public String toString() {
        return "BidInfo{" +
                "itemName='" + itemName + '\'' +
                ", currentPrice=" + String.format("%,.0f VNĐ", (double) currentPrice) +
                ", leader='" + leader + '\'' +
                '}';
    }
}
