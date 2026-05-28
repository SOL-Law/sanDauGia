package server.dao;

import org.junit.jupiter.api.Test;
import server.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;

import static org.junit.jupiter.api.Assertions.*;

public class UserDaoBalanceTest {

    @Test
    void testDepositAndLockedBalance() throws Exception {
        String username = "balanceuser_" + System.nanoTime();
        String itemName = "LockedBalanceItem-" + System.nanoTime();

        assertTrue(UserDao.register(username, "123456", "BIDDER"));
        assertTrue(UserDao.deposit(username, 20000));
        assertEquals(20000.0, UserDao.getBalance(username), 0.1);

        assertTrue(ItemDao.insertItem(itemName, 15000, "", "nguoiban", "Khác"));
        assertTrue(ItemDao.insertBidHistory(itemName, username, 15000));

        assertEquals(15000.0, UserDao.getLockedBalance(username), 0.1);

        assertTrue(ItemDao.deleteItem(itemName));

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE username = ?");
            stmt.setString(1, username);
            stmt.executeUpdate();
        }
    }
}
