package org.example.vgtuaventory.service;


import lombok.RequiredArgsConstructor;
import org.example.vgtuaventory.model.User;
import org.example.vgtuaventory.model.UserRole;
import org.example.vgtuaventory.repository.UserRepository;
import org.example.vgtuaventory.security.PasswordService;
import org.springframework.stereotype.Service;

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

    public String authenticate(String email, String password){
        if(email == null || password == null || email.isBlank() || password.isBlank()) {
            throw new IllegalArgumentException("Email and password must not be empty");
        }

        User user = userRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        boolean valid = passwordService.verifyPassword(password, user.getPassword());

        if (!valid) {
            throw new RuntimeException("Invalid credentials");
        }

        return tokenService.generateToken(user);
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
}
