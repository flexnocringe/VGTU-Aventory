package org.example.vgtuaventory.service;

import org.example.vgtuaventory.model.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTest {

    private final TokenService tokenService = new TokenService();

    @Test
    void generateToken_shouldReturnNonNullToken() {
        User user = new User();

        String token = tokenService.generateToken(user);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void generateToken_shouldReturnDifferentTokens() {
        User user = new User();

        String token1 = tokenService.generateToken(user);
        String token2 = tokenService.generateToken(user);

        assertNotEquals(token1, token2);
    }
}