package org.example.vgtuaventory.service;


import lombok.RequiredArgsConstructor;
import org.example.vgtuaventory.model.User;
import org.example.vgtuaventory.model.UserRole;
import org.example.vgtuaventory.repository.UserRepository;
import org.example.vgtuaventory.security.PasswordService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(5);
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PASSWORD_PATTERN =
        Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,}$");

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final TokenService tokenService;
    private final ConcurrentHashMap<String, Integer> failedLoginAttempts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Instant> lockedUntil = new ConcurrentHashMap<>();

    public record LoginResult(String token, User user) {
    }

    public String authenticate(String email, String password){
        return login(email, password).token();
    }

    public LoginResult login(String email, String password) {
        User user = authenticateUser(email, password);
        return new LoginResult(tokenService.generateToken(user), user);
    }

    private User authenticateUser(String email, String password) {
        if(email == null || password == null || email.isBlank() || password.isBlank()) {
            throw new IllegalArgumentException("Email and password must not be empty");
        }

        String normalizedEmail = email.trim();
        String loginKey = normalizedEmail.toLowerCase(Locale.ROOT);
        Instant now = Instant.now();

        Instant blockedUntil = lockedUntil.get(loginKey);
        if (blockedUntil != null && blockedUntil.isAfter(now)) {
            throw new IllegalStateException("Account is temporarily blocked. Try again later.");
        }

        if (blockedUntil != null && !blockedUntil.isAfter(now)) {
            lockedUntil.remove(loginKey);
            failedLoginAttempts.remove(loginKey);
        }

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> handleFailedLogin(loginKey));

        boolean valid = passwordService.verifyPassword(password, user.getPassword());

        if (!valid) {
            throw handleFailedLogin(loginKey);
        }

        failedLoginAttempts.remove(loginKey);
        lockedUntil.remove(loginKey);

        return user;
    }

    private RuntimeException handleFailedLogin(String loginKey) {
        int attempts = failedLoginAttempts.merge(loginKey, 1, Integer::sum);

        if (attempts >= MAX_FAILED_LOGIN_ATTEMPTS) {
            lockedUntil.put(loginKey, Instant.now().plus(LOCK_DURATION));
            failedLoginAttempts.remove(loginKey);
            throw new IllegalStateException("Account is temporarily blocked. Try again later.");
        }

        return new RuntimeException("Invalid credentials");
    }

    public User register(String email, String password) {
        if (email == null || password == null || email.isBlank() || password.isBlank()) {
            throw new IllegalArgumentException("Email and password must not be empty");
        }

        String normalizedEmail = email.trim();

        if (!EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            throw new IllegalArgumentException("Email format is invalid");
        }

        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException("Password must be at least 8 characters and contain at least one letter and one number");
        }

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPassword(passwordService.hashPassword(password));
        user.setRole(UserRole.SELLER);

        return userRepository.save(user);
    }

    public void changePassword(String token, String currentPassword, String newPassword) {
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("Unauthorized");
        }

        if (currentPassword == null || newPassword == null || currentPassword.isBlank() || newPassword.isBlank()) {
            throw new IllegalArgumentException("Current password and new password must not be empty");
        }

        if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
            throw new IllegalArgumentException("Password must be at least 8 characters and contain at least one letter and one number");
        }

        Integer userId = tokenService.getUserIdForToken(token)
                .orElseThrow(() -> new IllegalStateException("Unauthorized"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Unauthorized"));

        if (!passwordService.verifyPassword(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        user.setPassword(passwordService.hashPassword(newPassword));
        userRepository.save(user);
        tokenService.revokeToken(token);
    }
}
