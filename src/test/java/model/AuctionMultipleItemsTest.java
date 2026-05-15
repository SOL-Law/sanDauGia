package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AuctionMultipleItemsTest {

    @Test
    void testMultipleItemsAdded() {

        AuctionManager manager =
                AuctionManager.getInstance();

        manager.addItem(
                "A",
                100,
                "",
                60
        );

        manager.addItem(
                "B",
                200,
                "",
                60
        );

        String data =
                manager.getAllItems();

        assertTrue(data.contains("A"));
        assertTrue(data.contains("B"));
    }
}