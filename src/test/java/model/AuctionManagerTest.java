package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AuctionManagerTest {

    @Test
    void testAddItem() {

        AuctionManager manager =
                AuctionManager.getInstance();

        manager.addItem(
                "iPhone 15",
                15000000,
                "",
                60
        );

        String items =
                manager.getAllItems();

        assertNotNull(items);

        assertTrue(
                items.contains("iPhone 15")
        );
    }
}