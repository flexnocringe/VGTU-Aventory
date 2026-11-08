package org.example.vgtuaventory.service;


import lombok.RequiredArgsConstructor;
import org.example.vgtuaventory.exception.AccountLockedException;
import org.example.vgtuaventory.exception.BadRequestException;
import org.example.vgtuaventory.exception.InvalidCredentialsException;
import org.example.vgtuaventory.model.User;
import org.example.vgtuaventory.model.UserRole;
import org.example.vgtuaventory.repository.UserRepository;
import org.example.vgtuaventory.security.PasswordService;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PASSWORD_PATTERN =
        Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,}$");

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final TokenService tokenService;
    private final LoginAttemptService loginAttempts;

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
            throw new BadRequestException("Email and password must not be empty");
        }

        String normalizedEmail = email.trim();
        String loginKey = normalizedEmail.toLowerCase(Locale.ROOT);

        loginAttempts.assertNotLocked(loginKey);

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail).orElse(null);

        if (user == null || !passwordService.verifyPassword(password, user.getPassword())) {
            if (loginAttempts.recordFailure(loginKey)) {
                throw new AccountLockedException();
            }
            throw new InvalidCredentialsException();
        }

        loginAttempts.reset(loginKey);

        return user;
    }

    public User register(String email, String password) {
        if (email == null || password == null || email.isBlank() || password.isBlank()) {
            throw new BadRequestException("Email and password must not be empty");
        }

        String normalizedEmail = email.trim();

        if (!EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            throw new BadRequestException("Email format is invalid");
        }

        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new BadRequestException("Password must be at least 8 characters and contain at least one letter and one number");
        }

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new BadRequestException("Email already exists");
        }

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPassword(passwordService.hashPassword(password));
        user.setRole(UserRole.SELLER);

        return userRepository.save(user);
    }
}
