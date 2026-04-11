package model;

import java.util.*;

public class AuctionManager {

    private static AuctionManager instance;

    // 🔥 dùng Map với id
    private Map<Integer, BidInfo> items = new HashMap<>();

    private boolean isRunning = true;

    private AuctionManager() {
        items.put(1, new BidInfo("Laptop", 100, "none"));
        items.put(2, new BidInfo("Phone", 200, "none"));
        items.put(3, new BidInfo("Watch", 300, "none"));
    }

    public static synchronized AuctionManager getInstance() {
        if (instance == null) {
            instance = new AuctionManager();
        }
        return instance;
    }

    // =========================
    // PLACE BID (THREAD-SAFE)
    // =========================
    public synchronized boolean placeBid(String itemName, int price, String user) {

        if (!isRunning) return false;

        for (BidInfo bid : items.values()) {

            if (bid.getItem().equals(itemName)) {

                if (price > bid.getCurrentPrice()) {
                    bid.setCurrentPrice(price);
                    bid.setLeader(user);
                    return true;
                }
            }
        }
        return false;
    }

    // =========================
    // 🔥 ADD ITEM (SELLER)
    // =========================
    public synchronized void addItem(String name, int startPrice) {

        int newId = items.size() + 1;

        items.put(newId, new BidInfo(name, startPrice, "none"));

        System.out.println("🆕 Thêm sản phẩm: " + name + " | Giá: " + startPrice);
    }

    // =========================
    // GET DATA
    // =========================
    public synchronized String getAllItems() {
        StringBuilder sb = new StringBuilder();

        for (BidInfo b : items.values()) {
            sb.append(b.getItem()).append("|")
                    .append(b.getCurrentPrice()).append("|")
                    .append(b.getLeader()).append(";");
        }

        return sb.toString();
    }

    // =========================
    // KẾT THÚC PHIÊN
    // =========================
    public synchronized void endAuction() {
        isRunning = false;
    }

    public synchronized boolean isRunning() {
        return isRunning;
    }
}