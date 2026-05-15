package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AuctionAddItemTest {

    @Test
    void testAddItem() {

        AuctionManager manager =
                AuctionManager.getInstance();

        String before =
                manager.getAllItems();

        manager.addItem(
                "Tablet",
                3000,
                "",
                60
        );

        String after =
                manager.getAllItems();

        assertNotEquals(before, after);
    }
}