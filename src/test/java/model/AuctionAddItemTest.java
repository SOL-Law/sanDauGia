package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AuctionAddItemTest {

    @Test
    void testAddItem() {

        AuctionManager manager =
                AuctionManager.getInstance();

        manager.addItem(
                "Camera",
                1000,
                "",
                60,
                "Điện tử",
                "Canon"
        );

        String data =
                manager.getAllItems();

        assertTrue(data.contains("Camera"));
    }
}