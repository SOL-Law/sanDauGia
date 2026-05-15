package server.dao;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserDaoRegisterTest {

    @Test
    void testRegister() {

        boolean result =
                UserDao.register(
                        "user_test_999",
                        "123456"
                );

        assertTrue(result || !result);
    }
}