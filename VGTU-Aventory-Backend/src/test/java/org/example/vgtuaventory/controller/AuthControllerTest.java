package org.example.vgtuaventory.controller;

import org.example.vgtuaventory.model.User;
import org.example.vgtuaventory.model.UserRole;
import org.example.vgtuaventory.service.AuthService;
import org.example.vgtuaventory.service.TokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthController authController;

    @Test
    void login_shouldReturnTokenAndUserData_whenCredentialsAreValid() {
        User user = new User();
        user.setId(7);
        user.setEmail("test@test.com");
        user.setRole(UserRole.SELLER);

        when(authService.login("test@test.com", "password123"))
                .thenReturn(new AuthService.LoginResult("token-123", user));

        ResponseEntity<?> response = authController.login(new AuthController.LoginRequest("test@test.com", "password123"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(AuthController.LoginResponse.class, response.getBody());

        AuthController.LoginResponse body = (AuthController.LoginResponse) response.getBody();
        assertNotNull(body);
        assertEquals("token-123", body.token());
        assertEquals(7, body.id());
        assertEquals("test@test.com", body.email());
        assertEquals("SELLER", body.role());
    }

    @Test
    void login_shouldReturnLocked_whenAccountIsBlocked() {
        when(authService.login("test@test.com", "password123"))
                .thenThrow(new IllegalStateException("Account is temporarily blocked. Try again later."));

        ResponseEntity<?> response = authController.login(new AuthController.LoginRequest("test@test.com", "password123"));

        assertEquals(HttpStatus.LOCKED, response.getStatusCode());
        assertEquals("Account is temporarily blocked. Try again later.", response.getBody());
    }

    @Test
    void logout_shouldRevokeCurrentToken_whenAuthorizationHeaderPresent() {
        ResponseEntity<?> response = authController.logout("Bearer token-123");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(tokenService).revokeToken("token-123");
    }

    @Test
    void logout_shouldReturnUnauthorized_whenAuthorizationHeaderMissing() {
        ResponseEntity<?> response = authController.logout(null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Unauthorized", response.getBody());
    }
}