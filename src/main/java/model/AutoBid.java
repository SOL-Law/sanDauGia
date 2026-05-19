package model;

public class AutoBid {
    private String username;
    private String itemName;
    private int maxBid;
    private int increment;
    private long registeredTime;

    public AutoBid(String username, String itemName, int maxBid, int increment) {
        this.username = username;
        this.itemName = itemName;
        this.maxBid = maxBid;
        this.increment = increment;
        this.registeredTime = System.currentTimeMillis(); // Để PriorityQueue phân định ai trước ai sau
    }

    public String getUsername() { return username; }
    public String getItemName() { return itemName; }
    public int getMaxBid() { return maxBid; }
    public int getIncrement() { return increment; }
    public long getRegisteredTime() { return registeredTime; }
}