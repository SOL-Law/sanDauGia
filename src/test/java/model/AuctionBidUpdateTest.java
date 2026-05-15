package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AuctionBidUpdateTest {

    @Test
    void testBidUpdatePrice() {

        AuctionManager manager =
                AuctionManager.getInstance();

        manager.addItem(
                "Camera",
                1000,
                "",
                60
        );

        boolean result =
                manager.placeBid(
                        "Camera",
                        5000,
                        "thuan"
                );

        assertTrue(result);

        String data =
                manager.getAllItems();

        assertTrue(data.contains("5000"));
    }
}