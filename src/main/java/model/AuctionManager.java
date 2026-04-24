package model;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class AuctionManager {

    private static AuctionManager instance;

    // thread-safe storage
    private final Map<Integer, BidInfo> items =
            new ConcurrentHashMap<>();

    // auto-increment ID
    private final AtomicInteger idCounter =
            new AtomicInteger(0);

    // 🔥 FIX QUAN TRỌNG: mặc định phải false
    private boolean isRunning = false;

    private AuctionManager() {

        // dữ liệu mẫu ban đầu
        addItem("Laptop", 100);
        addItem("Phone", 200);
        addItem("Watch", 300);

        System.out.println("✅ AuctionManager initialized");
        printAllItems();
    }

    // =========================
    // SINGLETON
    // =========================
    public static synchronized AuctionManager getInstance() {

        if (instance == null) {

            instance =
                    new AuctionManager();

        }

        return instance;
    }

    // =========================
    // PLACE BID
    // =========================
    public synchronized boolean placeBid(
            String itemName,
            int price,
            String user
    ) {

        if (!isRunning) {

            System.out.println("❌ BID FAIL (session chưa start)");

            return false;
        }

        for (BidInfo bid : items.values()) {

            if (bid.getItem().equals(itemName)) {

                if (price > bid.getCurrentPrice()) {

                    bid.setCurrentPrice(price);

                    bid.setLeader(user);

                    System.out.println(
                            "💰 BID SUCCESS: "
                                    + itemName
                                    + " -> "
                                    + price
                                    + " ("
                                    + user
                                    + ")"
                    );

                    return true;
                }

                System.out.println(
                        "❌ BID FAIL (price too low)"
                );

                return false;
            }
        }

        System.out.println(
                "❌ BID FAIL (item not found)"
        );

        return false;
    }

    // =========================
    // ADD ITEM (UPLOAD)
    // =========================
    public synchronized void addItem(
            String name,
            int startPrice
    ) {

        int newId =
                idCounter.incrementAndGet();

        items.put(

                newId,

                new BidInfo(
                        name,
                        startPrice,
                        "none"
                )
        );

        System.out.println(
                "🆕 ADD ITEM: "
                        + name
                        + " | "
                        + startPrice
        );
    }

    // =========================
    // GET ALL ITEMS (CLIENT UI)
    // =========================
    public synchronized String getAllItems() {

        StringBuilder sb =
                new StringBuilder();

        for (BidInfo b : items.values()) {

            sb.append(
                            b.getItem()
                    )
                    .append("|")

                    .append(
                            b.getCurrentPrice()
                    )

                    .append("|")

                    .append(
                            b.getLeader()
                    )

                    .append(";");
        }

        return sb.toString();
    }

    // =========================
    // START NEW SESSION
    // =========================
    public synchronized void startNewSession() {

        isRunning = true;

        System.out.println(
                "🟢 START NEW SESSION"
        );

        for (BidInfo bid : items.values()) {

            bid.setLeader("none");

            // nếu muốn reset giá:
            // bid.setCurrentPrice(bid.getStartPrice());
        }
    }

    // =========================
    // END SESSION
    // =========================
    public synchronized void endAuction() {

        isRunning = false;

        System.out.println(
                "🔴 AUCTION ENDED"
        );
    }

    // =========================
    // CHECK STATUS
    // =========================
    public synchronized boolean isRunning() {

        return isRunning;

    }

    // =========================
    // DEBUG PRINT
    // =========================
    public synchronized void printAllItems() {

        System.out.println(
                "===== ITEM LIST ====="
        );

        for (BidInfo bid : items.values()) {

            System.out.println(

                    bid.getItem()

                            + " | "

                            + bid.getCurrentPrice()

                            + " | "

                            + bid.getLeader()
            );
        }
    }
}