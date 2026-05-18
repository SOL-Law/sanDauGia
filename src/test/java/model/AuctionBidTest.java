package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AuctionBidTest {

    @Test
    void testPlaceBidSuccess() {

        AuctionManager manager =
                AuctionManager.getInstance();

        manager.addItem(
                "Laptop",
                1000,
                "",
                60,
                "Laptop gaming",
                "Asus"
        );

        boolean result =
                manager.placeBid(
                        "Laptop",
                        2000,
                        "thuan"
                );

        assertTrue(result);
    }

    @Test
    void testPlaceBidFailLowPrice() {

        AuctionManager manager =
                AuctionManager.getInstance();

        manager.addItem(
                "Phone",
                5000,
                "",
                60,
                "Điện thoại",
                "Samsung"
        );

        boolean result =
                manager.placeBid(
                        "Phone",
                        1000,
                        "abc"
                );

        assertFalse(result);
    }
}