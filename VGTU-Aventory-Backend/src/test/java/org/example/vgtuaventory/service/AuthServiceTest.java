package org.example.vgtuaventory.service;

import org.example.vgtuaventory.model.User;
import org.example.vgtuaventory.model.UserRole;
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

    // TC1 good auth
    @Test
    void authenticate_shouldReturnToken_whenCredentialsValid() {

        User user = new User();
        user.setEmail("test@test.com");
        user.setPassword("hashedPassword");

        when(userRepository.findByEmailIgnoreCase("test@test.com"))
                .thenReturn(Optional.of(user));

        when(passwordService.verifyPassword("password123", "hashedPassword"))
                .thenReturn(true);

        when(tokenService.generateToken(user))
                .thenReturn("token123");

        String result = authService.authenticate("test@test.com", "password123");

        assertNotNull(result);
        assertEquals("token123", result);
    }

    // TC2 failed auth, bad psw
    @Test
    void authenticate_shouldThrowException_whenPasswordIncorrect() {

        User user = new User();
        user.setEmail("test@test.com");
        user.setPassword("hashedPassword");

        when(userRepository.findByEmailIgnoreCase("test@test.com"))
                .thenReturn(Optional.of(user));

        when(passwordService.verifyPassword("password123", "hashedPassword"))
                .thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                authService.authenticate("test@test.com", "password123"));

        assertEquals("Invalid credentials", ex.getMessage());
    }

    // TC2 failed auth user not found
    @Test
    void authenticate_shouldThrowException_whenUserNotFound() {

        when(userRepository.findByEmailIgnoreCase("test@test.com"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                authService.authenticate("test@test.com", "password123"));
    }

    // TC3 input validation
    @Test
    void authenticate_shouldThrowException_whenInputInvalid() {

        assertThrows(IllegalArgumentException.class, () ->
                authService.authenticate("", "password123"));

        assertThrows(IllegalArgumentException.class, () ->
                authService.authenticate("test@test.com", ""));

        assertThrows(IllegalArgumentException.class, () ->
                authService.authenticate(null, "password123"));
    }

    @Test
    void register_shouldCreateUser_whenInputValid() {
        User saved = new User();
        saved.setId(1);
        saved.setEmail("test@test.com");
        saved.setPassword("hashed");
        saved.setRole(UserRole.SELLER);

        when(userRepository.existsByEmailIgnoreCase("test@test.com")).thenReturn(false);
        when(passwordService.hashPassword("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(saved);

        User result = authService.register("test@test.com", "password123");

        assertEquals("test@test.com", result.getEmail());
        assertEquals(UserRole.SELLER, result.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldThrowException_whenEmailAlreadyExists() {
        when(userRepository.existsByEmailIgnoreCase("test@test.com")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.register("test@test.com", "password123"));

        assertEquals("Email already exists", ex.getMessage());
    }

    @Test
    void register_shouldThrowException_whenEmailFormatInvalid() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.register("bad-email", "password123"));

        assertEquals("Email format is invalid", ex.getMessage());
    }

    @Test
    void register_shouldThrowException_whenPasswordTooWeak() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.register("test@test.com", "password"));

        assertEquals("Password must be at least 8 characters and contain at least one letter and one number", ex.getMessage());
    }
}