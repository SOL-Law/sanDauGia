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
                60,
                "Khác",
                "None"
        );

        manager.addItem(
                "B",
                200,
                "",
                60,
                "Khác",
                "None"
        );

        String data =
                manager.getAllItems();

        assertTrue(data.contains("A"));
        assertTrue(data.contains("B"));
    }
}