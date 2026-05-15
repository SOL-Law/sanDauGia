package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AuctionManagerAddItemTest {

    @Test
    void testAddItem() {

        AuctionManager manager =
                AuctionManager.getInstance();

        manager.addItem(
                "Laptop Gaming",
                20000000,
                "",
                60
        );

        String items =
                manager.getAllItems();

        assertTrue(
                items.contains("Laptop Gaming")
        );
    }
}