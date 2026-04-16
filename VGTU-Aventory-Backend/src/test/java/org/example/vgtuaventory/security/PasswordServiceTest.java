package org.example.vgtuaventory.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordServiceTest {

    private final PasswordService passwordService = new PasswordService();

    @Test
    void hashPassword_shouldReturnDifferentValueThanInput() {
        String rawPassword = "password123";

        String hashedPassword = passwordService.hashPassword(rawPassword);

        assertNotNull(hashedPassword);
        assertNotEquals(rawPassword, hashedPassword);
    }

    @Test
    void verifyPassword_shouldReturnTrueForCorrectPassword() {
        String rawPassword = "password123";

        String hashedPassword = passwordService.hashPassword(rawPassword);

        boolean result = passwordService.verifyPassword(rawPassword, hashedPassword);

        assertTrue(result);
    }

    @Test
    void verifyPassword_shouldReturnFalseForIncorrectPassword() {
        String rawPassword = "password123";
        String wrongPassword = "wrongPassword";

        String hashedPassword = passwordService.hashPassword(rawPassword);

        boolean result = passwordService.verifyPassword(wrongPassword, hashedPassword);

        assertFalse(result);
    }
}