package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AuctionManagerAddItemTest {

    @Test
    void testAddNewItem() {

        AuctionManager manager =
                AuctionManager.getInstance();

        manager.addItem(
                "Laptop",
                1500,
                "",
                60,
                "Laptop gaming",
                "Asus"
        );

        String data =
                manager.getAllItems();

        assertTrue(data.contains("Laptop"));
    }
}