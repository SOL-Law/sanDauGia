package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AuctionGetItemsTest {

    @Test
    void testGetAllItemsNotNull() {

        AuctionManager manager =
                AuctionManager.getInstance();

        String data =
                manager.getAllItems();

        assertNotNull(data);
    }
}