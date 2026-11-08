package org.example.vgtuaventory.controller;

import org.example.vgtuaventory.exception.GlobalExceptionHandler;
import org.example.vgtuaventory.model.User;
import org.example.vgtuaventory.model.UserRole;
import org.example.vgtuaventory.repository.UserRepository;
import org.example.vgtuaventory.security.PasswordService;
import org.example.vgtuaventory.service.AuthService;
import org.example.vgtuaventory.service.LoginAttemptService;
import org.example.vgtuaventory.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Charakterizaciniai AuthController testai (2 ND, parasyti pries klaidu apdorojimo pertvarkyma).
 * Tikrinama tik isorine elgsena: HTTP kodas ir atsakymo turinys. Naudojamas tikras AuthService,
 * imituojamos tik repozitorija ir slaptazodziu servisas, todel testai nepriklauso nuo to,
 * kokia isimtimi servisas pranesa apie klaida.
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private static final String LOCKED_MESSAGE = "Account is temporarily blocked. Try again later.";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordService passwordService;

    private TokenService tokenService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService();
        Clock clock = Clock.fixed(Instant.parse("2026-11-08T10:00:00Z"), ZoneOffset.UTC);
        AuthService authService = new AuthService(userRepository, passwordService, tokenService, new LoginAttemptService(clock));
        AuthController controller = new AuthController(authService, tokenService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ---------- pagalbiniai metodai ----------

    private static User user(int id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setPassword("hash");
        user.setRole(UserRole.SELLER);
        return user;
    }

    private static MockHttpServletRequestBuilder json(String url, String email, String password) {
        String body = String.format("{\"email\":%s,\"password\":%s}",
                email == null ? "null" : "\"" + email + "\"",
                password == null ? "null" : "\"" + password + "\"");
        return post(url).contentType(MediaType.APPLICATION_JSON).content(body);
    }

    // ---------- login ----------

    @Test
    void login_shouldReturnTokenAndUserData_whenCredentialsAreValid() throws Exception {
        when(userRepository.findByEmailIgnoreCase("test@test.com")).thenReturn(Optional.of(user(7, "test@test.com")));
        when(passwordService.verifyPassword("password123", "hash")).thenReturn(true);

        mockMvc.perform(json("/auth/login", "test@test.com", "password123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.role").value("SELLER"));
    }

    @Test
    void login_shouldReturnBadRequest_whenPasswordIsBlank() throws Exception {
        mockMvc.perform(json("/auth/login", "test@test.com", " "))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email and password must not be empty"));
    }

    @Test
    void login_shouldReturnUnauthorized_whenUserNotFound() throws Exception {
        when(userRepository.findByEmailIgnoreCase("nobody@test.com")).thenReturn(Optional.empty());

        mockMvc.perform(json("/auth/login", "nobody@test.com", "password123"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid credentials"));
    }

    @Test
    void login_shouldReturnUnauthorizedFourTimes_thenLocked() throws Exception {
        when(userRepository.findByEmailIgnoreCase("test@test.com")).thenReturn(Optional.of(user(7, "test@test.com")));
        when(passwordService.verifyPassword("wrong-pass1", "hash")).thenReturn(false);

        for (int attempt = 0; attempt < 4; attempt++) {
            mockMvc.perform(json("/auth/login", "test@test.com", "wrong-pass1"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string("Invalid credentials"));
        }

        mockMvc.perform(json("/auth/login", "test@test.com", "wrong-pass1"))
                .andExpect(status().isLocked())
                .andExpect(content().string(LOCKED_MESSAGE));

        mockMvc.perform(json("/auth/login", "test@test.com", "password123"))
                .andExpect(status().isLocked())
                .andExpect(content().string(LOCKED_MESSAGE));
    }

    // ---------- register ----------

    @Test
    void register_shouldReturnCreatedUser_whenInputIsValid() throws Exception {
        when(userRepository.existsByEmailIgnoreCase("new@test.com")).thenReturn(false);
        when(passwordService.hashPassword("password123")).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(11);
            return saved;
        });

        mockMvc.perform(json("/auth/register", "new@test.com", "password123"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(11))
                .andExpect(jsonPath("$.email").value("new@test.com"))
                .andExpect(jsonPath("$.role").value("SELLER"));
    }

    @Test
    void register_shouldReturnBadRequest_whenEmailAlreadyExists() throws Exception {
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(true);

        mockMvc.perform(json("/auth/register", "taken@test.com", "password123"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email already exists"));
    }

    @Test
    void register_shouldReturnBadRequest_whenEmailFormatIsInvalid() throws Exception {
        mockMvc.perform(json("/auth/register", "bad-email", "password123"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email format is invalid"));
    }

    @Test
    void register_shouldReturnBadRequest_whenCredentialsMissing() throws Exception {
        mockMvc.perform(json("/auth/register", null, "password123"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email and password must not be empty"));
    }

    // ---------- logout ----------

    @Test
    void logout_shouldRevokeCurrentToken_whenAuthorizationHeaderPresent() throws Exception {
        String token = tokenService.generateToken(user(7, "test@test.com"));
        assertTrue(tokenService.isTokenValid(token));

        mockMvc.perform(post("/auth/logout").header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assertFalse(tokenService.isTokenValid(token));
    }

    @Test
    void logout_shouldReturnUnauthorized_whenAuthorizationHeaderMissing() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Unauthorized"));
    }

    @Test
    void logout_shouldReturnUnauthorized_whenBearerTokenIsBlank() throws Exception {
        mockMvc.perform(post("/auth/logout").header("Authorization", "Bearer   "))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Unauthorized"));
    }
}
