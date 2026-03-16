package org.example.vgtuaventory.service;

import org.example.vgtuaventory.model.User;
import org.example.vgtuaventory.repository.UserRepository;
import org.example.vgtuaventory.security.PasswordService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordService passwordService;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthService authService;

    @Test
    void authenticate_shouldReturnToken_whenCredentialsValid() {

        User user = new User();
        user.setEmail("test@test.com");
        user.setPassword("hashedPassword");

        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(user));

        when(passwordService.verifyPassword("password123", "hashedPassword"))
                .thenReturn(true);

        when(tokenService.generateToken(user))
                .thenReturn("token123");

        String result = authService.authenticate("test@test.com", "password123");

        assertEquals("token123", result);
    }

    @Test
    void authenticate_shouldThrowException_whenUserNotFound() {

        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                authService.authenticate("test@test.com", "password123"));
    }

    @Test
    void authenticate_shouldThrowException_whenPasswordIncorrect() {

        User user = new User();
        user.setPassword("hashedPassword");

        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(user));

        when(passwordService.verifyPassword("password123", "hashedPassword"))
                .thenReturn(false);

        assertThrows(RuntimeException.class, () ->
                authService.authenticate("test@test.com", "password123"));
    }
}