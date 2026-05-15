package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AuctionSingletonTest {

    @Test
    void testSingletonInstance() {

        AuctionManager a =
                AuctionManager.getInstance();

        AuctionManager b =
                AuctionManager.getInstance();

        assertSame(a, b);
    }
}