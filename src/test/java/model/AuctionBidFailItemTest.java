package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AuctionBidFailItemTest {

    @Test
    void testBidItemNotFound() {

        AuctionManager manager =
                AuctionManager.getInstance();

        boolean result =
                manager.placeBid(
                        "Không tồn tại",
                        1000,
                        "thuan"
                );

        assertFalse(result);
    }
}