package server.dao;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserDaoTest {

    @Test
    void testLoginFail() {

        String role =
                UserDao.login(
                        "abcxyz",
                        "123456789"
                );

        assertNull(role);
    }
}