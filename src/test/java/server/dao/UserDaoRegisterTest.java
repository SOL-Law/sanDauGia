package server.dao;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserDaoRegisterTest {

    @Test
    void testRegister() {

        String username =
                "user_test_" + System.currentTimeMillis();

        boolean result =
                UserDao.register(
                        username,
                        "123456",
                        "USER"
                );

        assertTrue(result);
    }
}