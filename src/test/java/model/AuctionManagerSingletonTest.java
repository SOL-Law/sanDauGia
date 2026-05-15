package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AuctionManagerSingletonTest {

    @Test
    void testSingleton() {

        AuctionManager a =
                AuctionManager.getInstance();

        AuctionManager b =
                AuctionManager.getInstance();

        assertSame(a, b);
    }
}