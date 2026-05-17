package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AuctionManagerTest {

    @Test
    void testDefaultItemsLoaded() {

        AuctionManager manager =
                AuctionManager.getInstance();

        manager.addItem(
                "iPhone 15",
                15000000,
                "",
                60,
                "Điện thoại Apple",
                "Apple"
        );

        manager.addItem(
                "MacBook Air M2",
                25000000,
                "",
                60,
                "Laptop văn phòng",
                "Apple"
        );

        manager.addItem(
                "Đồng hồ thông minh",
                3000000,
                "",
                60,
                "Smart Watch",
                "Samsung"
        );

        String data =
                manager.getAllItems();

        assertNotNull(data);

        assertTrue(
                data.contains("iPhone 15")
        );

        assertTrue(
                data.contains("MacBook Air M2")
        );

        assertTrue(
                data.contains("Đồng hồ thông minh")
        );
    }
}