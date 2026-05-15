package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AuctionManagerEndAuctionTest {

    @Test
    void testEndAuction() {

        AuctionManager manager =
                AuctionManager.getInstance();

        manager.addItem(
                "Tai nghe",
                2000000,
                "",
                60
        );

        manager.endAuction("Tai nghe");

        String items =
                manager.getAllItems();

        assertFalse(
                items.contains("Tai nghe")
        );
    }
}