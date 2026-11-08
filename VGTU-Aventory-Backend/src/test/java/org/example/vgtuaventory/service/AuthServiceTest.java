package org.example.vgtuaventory.service;

import org.example.vgtuaventory.exception.AccountLockedException;
import org.example.vgtuaventory.exception.BadRequestException;
import org.example.vgtuaventory.model.User;
import org.example.vgtuaventory.model.UserRole;
import org.example.vgtuaventory.repository.UserRepository;
import org.example.vgtuaventory.security.PasswordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
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

    private MutableClock clock;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-11-07T10:00:00Z"));
        authService = new AuthService(userRepository, passwordService, tokenService, new LoginAttemptService(clock));
    }

    /** Test clock that can be moved forward, so time-dependent rules run without waiting. */
    static final class MutableClock extends Clock {
        private Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }

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

        assertThrows(BadRequestException.class, () ->
                authService.authenticate("", "password123"));

        assertThrows(BadRequestException.class, () ->
                authService.authenticate("test@test.com", ""));

        assertThrows(BadRequestException.class, () ->
                authService.authenticate(null, "password123"));
    }

        @Test
        void login_shouldReturnUserAndToken_whenCredentialsValid() {
                User user = new User();
                user.setId(2);
                user.setEmail("test@test.com");
                user.setPassword("hashedPassword");
                user.setRole(UserRole.SELLER);

                when(userRepository.findByEmailIgnoreCase("test@test.com")).thenReturn(Optional.of(user));
                when(passwordService.verifyPassword("password123", "hashedPassword")).thenReturn(true);
                when(tokenService.generateToken(user)).thenReturn("token-abc");

                AuthService.LoginResult result = authService.login("test@test.com", "password123");

                assertEquals("token-abc", result.token());
                assertEquals("test@test.com", result.user().getEmail());
        }

        @Test
        void login_shouldLockAccount_afterFiveFailedAttempts() {
                when(userRepository.findByEmailIgnoreCase("test@test.com")).thenReturn(Optional.empty());

                for (int attempt = 0; attempt < 4; attempt++) {
                        RuntimeException ex = assertThrows(RuntimeException.class,
                                        () -> authService.login("test@test.com", "password123"));
                        assertEquals("Invalid credentials", ex.getMessage());
                }

                AccountLockedException locked = assertThrows(AccountLockedException.class,
                                () -> authService.login("test@test.com", "password123"));

                assertEquals("Account is temporarily blocked. Try again later.", locked.getMessage());
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

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> authService.register("test@test.com", "password123"));

        assertEquals("Email already exists", ex.getMessage());
    }

    @Test
    void register_shouldThrowException_whenEmailFormatInvalid() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> authService.register("bad-email", "password123"));

        assertEquals("Email format is invalid", ex.getMessage());
    }

    @Test
    void register_shouldThrowException_whenPasswordTooWeak() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> authService.register("test@test.com", "password"));

        assertEquals("Password must be at least 8 characters and contain at least one letter and one number", ex.getMessage());
    }

    // HW2: characterization tests that pin the current behaviour before refactoring

    private static final String LOCKED_MESSAGE = "Account is temporarily blocked. Try again later.";

    private static User user(String email, String passwordHash) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordHash);
        return user;
    }

    @Test
    void register_shouldRejectMissingOrBlankCredentials_withoutTouchingRepository() {
        String[][] inputs = {{null, "password123"}, {"test@test.com", null}, {" ", "password123"}, {"test@test.com", " "}};

        for (String[] input : inputs) {
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> authService.register(input[0], input[1]));
            assertEquals("Email and password must not be empty", ex.getMessage());
        }

        verifyNoInteractions(userRepository);
    }

    @Test
    void login_shouldRejectMissingPassword_withoutTouchingRepository() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> authService.login("test@test.com", null));

        assertEquals("Email and password must not be empty", ex.getMessage());
        verifyNoInteractions(userRepository);
    }

    @Test
    void register_shouldTrimEmail_hashPassword_andAssignSellerRole() {
        when(userRepository.existsByEmailIgnoreCase("test@test.com")).thenReturn(false);
        when(passwordService.hashPassword("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = authService.register("  test@test.com  ", "password123");

        assertEquals("test@test.com", result.getEmail());
        assertEquals("hashed", result.getPassword());
        assertEquals(UserRole.SELLER, result.getRole());
    }

    @Test
    void login_shouldLockAfterFiveWrongPasswords_andRejectCorrectPasswordWhileLocked() {
        User user = user("test@test.com", "hash");
        when(userRepository.findByEmailIgnoreCase("test@test.com")).thenReturn(Optional.of(user));
        when(passwordService.verifyPassword("wrong-pass1", "hash")).thenReturn(false);

        for (int attempt = 0; attempt < 4; attempt++) {
            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> authService.login("test@test.com", "wrong-pass1"));
            assertEquals("Invalid credentials", ex.getMessage());
        }

        AccountLockedException locked = assertThrows(AccountLockedException.class,
                () -> authService.login("test@test.com", "wrong-pass1"));
        assertEquals(LOCKED_MESSAGE, locked.getMessage());

        AccountLockedException stillLocked = assertThrows(AccountLockedException.class,
                () -> authService.login("test@test.com", "password123"));
        assertEquals(LOCKED_MESSAGE, stillLocked.getMessage());

        verify(userRepository, times(5)).findByEmailIgnoreCase("test@test.com");
        verify(passwordService, never()).verifyPassword("password123", "hash");
    }

    @Test
    void login_shouldCountFailuresPerNormalizedEmail() {
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());
        String[] spellings = {"  Test@Test.com ", "test@test.com", "TEST@TEST.COM", "test@test.com"};

        for (String email : spellings) {
            RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(email, "password123"));
            assertEquals("Invalid credentials", ex.getMessage());
        }

        AccountLockedException locked = assertThrows(AccountLockedException.class,
                () -> authService.login("Test@test.COM", "password123"));
        assertEquals(LOCKED_MESSAGE, locked.getMessage());
    }

    @Test
    void login_shouldResetFailedAttempts_afterSuccessfulLogin() {
        User user = user("test@test.com", "hash");
        when(userRepository.findByEmailIgnoreCase("test@test.com")).thenReturn(Optional.of(user));
        when(passwordService.verifyPassword("wrong-pass1", "hash")).thenReturn(false);
        when(passwordService.verifyPassword("password123", "hash")).thenReturn(true);
        when(tokenService.generateToken(user)).thenReturn("token");

        for (int attempt = 0; attempt < 4; attempt++) {
            assertThrows(RuntimeException.class, () -> authService.login("test@test.com", "wrong-pass1"));
        }
        assertEquals("token", authService.login("test@test.com", "password123").token());

        for (int attempt = 0; attempt < 4; attempt++) {
            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> authService.login("test@test.com", "wrong-pass1"));
            assertEquals("Invalid credentials", ex.getMessage());
        }
    }

    private void lockAccount(String email) {
        for (int attempt = 0; attempt < 5; attempt++) {
            assertThrows(RuntimeException.class, () -> authService.login(email, "wrong-pass1"));
        }
    }

    @Test
    void login_shouldStayLocked_untilFiveMinutesHavePassed() {
        when(userRepository.findByEmailIgnoreCase("test@test.com")).thenReturn(Optional.empty());
        lockAccount("test@test.com");

        clock.advance(Duration.ofMinutes(5).minusSeconds(1));

        AccountLockedException locked = assertThrows(AccountLockedException.class,
                () -> authService.login("test@test.com", "password123"));
        assertEquals(LOCKED_MESSAGE, locked.getMessage());
    }

    @Test
    void login_shouldUnlockAfterFiveMinutes_andStartCountingFromZero() {
        User user = user("test@test.com", "hash");
        when(userRepository.findByEmailIgnoreCase("test@test.com")).thenReturn(Optional.of(user));
        when(passwordService.verifyPassword("wrong-pass1", "hash")).thenReturn(false);
        when(passwordService.verifyPassword("password123", "hash")).thenReturn(true);
        when(tokenService.generateToken(user)).thenReturn("token");
        lockAccount("test@test.com");

        clock.advance(Duration.ofMinutes(5));

        for (int attempt = 0; attempt < 4; attempt++) {
            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> authService.login("test@test.com", "wrong-pass1"));
            assertEquals("Invalid credentials", ex.getMessage());
        }
        assertEquals("token", authService.login("test@test.com", "password123").token());
    }

    @Test
    void login_shouldLockOnlyTheAccountWithFailedAttempts() {
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());

        for (int attempt = 0; attempt < 4; attempt++) {
            assertThrows(RuntimeException.class, () -> authService.login("first@test.com", "password123"));
        }
        assertThrows(AccountLockedException.class, () -> authService.login("first@test.com", "password123"));

        RuntimeException other = assertThrows(RuntimeException.class,
                () -> authService.login("second@test.com", "password123"));
        assertEquals("Invalid credentials", other.getMessage());
    }
}