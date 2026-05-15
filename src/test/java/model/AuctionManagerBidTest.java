package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AuctionManagerBidTest {

    @Test
    void testPlaceBidSuccess() {

        AuctionManager manager =
                AuctionManager.getInstance();

        manager.addItem(
                "iPad",
                10000000,
                "",
                60
        );

        boolean result =
                manager.placeBid(
                        "iPad",
                        12000000,
                        "thuan"
                );

        assertTrue(result);

        String items =
                manager.getAllItems();

        assertTrue(
                items.contains("12000000")
        );

        assertTrue(
                items.contains("thuan")
        );
    }
}