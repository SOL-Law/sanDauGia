package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AuctionManagerBidFailTest {

    @Test
    void testPlaceBidFail() {

        AuctionManager manager =
                AuctionManager.getInstance();

        manager.addItem(
                "Samsung S24",
                15000000,
                "",
                60
        );

        boolean result =
                manager.placeBid(
                        "Samsung S24",
                        10000000,
                        "abc"
                );

        assertFalse(result);
    }
}