package service;

import com.TDD.Ecom.dto.LoginDto;
import com.TDD.Ecom.dto.SignupDto;
import com.TDD.Ecom.model.User;
import com.TDD.Ecom.repo.UserRepo;
import com.TDD.Ecom.security.JwtTokenProvider;
import com.TDD.Ecom.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    // Replace @Mock with manual implementation
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private SignupDto signupDto;
    private LoginDto loginDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Manually stub jwtTokenProvider
        jwtTokenProvider = new JwtTokenProvider() {
            @Override
            public String generateToken(User user) {
                return "dummy-jwt-token";
            }
        };

        // Rebuild authService with manual JwtTokenProvider
        authService = new AuthService(userRepo, passwordEncoder, jwtTokenProvider);

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole("USER");

        signupDto = new SignupDto();
        signupDto.setUserName("newuser");
        signupDto.setEmail("newuser@example.com");
        signupDto.setPassword("Password123");
        signupDto.setRole("USER");

        loginDto = new LoginDto();
        loginDto.setEmail("test@example.com");
        loginDto.setPassword("password123");

        when(passwordEncoder.encode(any(String.class))).thenReturn("encodedPassword");
        when(passwordEncoder.matches(any(String.class), any(String.class))).thenReturn(true);
    }

    @Test
    void signup_WithValidData_ShouldCreateUser() {
        when(userRepo.findByEmail(signupDto.getEmail())).thenReturn(Optional.empty());
        when(userRepo.findByUsername(signupDto.getUserName())).thenReturn(Optional.empty());
        when(userRepo.save(any(User.class))).thenReturn(testUser);

        Map<String, String> response = authService.signup(signupDto);

        assertNotNull(response);
        assertEquals("User Account Created successfully", response.get("message"));
        assertEquals("USER", response.get("role"));
        verify(userRepo).save(any(User.class));
    }

    @Test
    void signup_WithExistingEmail_ShouldThrowException() {
        when(userRepo.findByEmail(signupDto.getEmail())).thenReturn(Optional.of(testUser));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.signup(signupDto);
        });

        assertEquals("Email already exists", exception.getMessage());
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void login_WithValidCredentials_ShouldReturnSuccessResponse() {
        when(userRepo.findByEmail(loginDto.getEmail())).thenReturn(Optional.of(testUser));

        Map<String, String> response = authService.login(loginDto);

        assertNotNull(response);
        assertEquals("USER", response.get("role"));
        assertEquals("dummy-jwt-token", response.get("token"));
    }

    @Test
    void login_WithInvalidEmail_ShouldThrowException() {
        when(userRepo.findByEmail(loginDto.getEmail())).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.login(loginDto);
        });

        assertEquals("User not found with email: test@example.com", exception.getMessage());
    }

    @Test
    void login_WithInvalidPassword_ShouldThrowException() {
        when(userRepo.findByEmail(loginDto.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(any(String.class), any(String.class))).thenReturn(false);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.login(loginDto);
        });

        assertEquals("Invalid password", exception.getMessage());
    }
}
