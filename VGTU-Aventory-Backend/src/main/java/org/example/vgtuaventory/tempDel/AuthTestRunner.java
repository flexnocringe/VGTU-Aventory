package org.example.vgtuaventory.tempDel;

import lombok.RequiredArgsConstructor;
import org.example.vgtuaventory.model.User;
import org.example.vgtuaventory.model.UserRole;
import org.example.vgtuaventory.repository.UserRepository;
import org.example.vgtuaventory.security.PasswordService;
import org.example.vgtuaventory.service.AuthService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthTestRunner implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final AuthService authService;

    @Override
    public void run(String... args) {
        String email = "test@test.com";
        String rawPassword = "123456";

        if (userRepository.findByEmail(email).isEmpty()) {
            User user = new User();
            user.setEmail(email);
            user.setPassword(passwordService.hashPassword(rawPassword));
            user.setRole(UserRole.SELLER);
            userRepository.save(user);
            System.out.println("Test user created.");
        }

        try {
            String token = authService.authenticate(email, rawPassword);
            System.out.println("AUTH OK, token: " + token);
        } catch (Exception e) {
            System.out.println("AUTH FAILED: " + e.getMessage());
        }
    }
}
