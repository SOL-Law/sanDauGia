package server.dao;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserDaoLoginSuccessTest {

    @Test
    void testLoginSuccess() {

        String role =
                UserDao.login(
                        "trongle",
                        "123456"
                );

        assertNotNull(role);
    }
}