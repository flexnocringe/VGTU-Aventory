package org.example.vgtuaventory.controller;

import lombok.RequiredArgsConstructor;
import org.example.vgtuaventory.model.User;
import org.example.vgtuaventory.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            AuthService.LoginResult loginResult = authService.login(request.email(), request.password());
            User user = loginResult.user();
            return ResponseEntity.ok(new LoginResponse(
                    loginResult.token(),
                    user.getId(),
                    user.getEmail(),
                    user.getRole().name()
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.LOCKED).body(ex.getMessage());
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            User savedUser = authService.register(request.email(), request.password());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new RegisterResponse(savedUser.getId(), savedUser.getEmail(), savedUser.getRole().name()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    public record RegisterRequest(String email, String password) {
    }

    public record RegisterResponse(int id, String email, String role) {
    }

    public record LoginRequest(String email, String password) {
    }

    public record LoginResponse(String token, int id, String email, String role) {
    }
}
