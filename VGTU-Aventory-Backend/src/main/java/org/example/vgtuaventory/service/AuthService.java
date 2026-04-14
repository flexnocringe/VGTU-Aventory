package org.example.vgtuaventory.service;


import lombok.RequiredArgsConstructor;
import org.example.vgtuaventory.model.User;
import org.example.vgtuaventory.repository.UserRepository;
import org.example.vgtuaventory.security.PasswordService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final TokenService tokenService;

    public String authenticate(String email, String password){
        if(email == null || password == null || email.isBlank() || password.isBlank()) {
            throw new IllegalArgumentException("Email and password must not be empty");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        boolean valid = passwordService.verifyPassword(password, user.getPassword());

        if (!valid) {
            throw new RuntimeException("Invalid credentials");
        }

        return tokenService.generateToken(user);
    }
}
