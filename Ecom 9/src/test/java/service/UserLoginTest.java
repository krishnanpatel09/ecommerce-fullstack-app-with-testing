package service;

import com.TDD.Ecom.dto.LoginDto;
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
import static org.mockito.Mockito.*;

class UserLoginTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private LoginDto loginDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);  // Initializes the mocks

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole("USER");

        loginDto = new LoginDto();
        loginDto.setEmail("test@example.com");
        loginDto.setPassword("Password123");

        when(passwordEncoder.matches(any(String.class), any(String.class))).thenReturn(true);

        // Manually inject FakeJwtTokenProvider into authService
        JwtTokenProvider fakeJwtTokenProvider = new FakeJwtTokenProvider();
        authService = new AuthService(userRepo, passwordEncoder, fakeJwtTokenProvider);  // Inject fake provider
    }

    // Fake JwtTokenProvider for testing
    static class FakeJwtTokenProvider extends JwtTokenProvider {
        @Override
        public String generateToken(User user) {
            return "fake-jwt-token"; // Return a fake token for tests
        }
    }

    @Test
    void userLogin_WithValidCredentials_ShouldReturnSuccessResponse() {
        // Arrange: simulate finding the user by email
        when(userRepo.findByEmail(loginDto.getEmail())).thenReturn(Optional.of(testUser));

        // Act
        Map<String, String> response = authService.login(loginDto);

        // Assert
        assertNotNull(response);
        assertEquals("fake-jwt-token", response.get("token"));  // Check if token is returned
        assertEquals("USER", response.get("role"));  // Check if the role is correctly returned
    }

    @Test
    void userLogin_WithInvalidEmail_ShouldThrowException() {
        when(userRepo.findByEmail(loginDto.getEmail())).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> authService.login(loginDto));

        assertEquals("User not found with email: test@example.com", exception.getMessage());
    }

    @Test
    void userLogin_WithInvalidPassword_ShouldThrowException() {
        // Arrange: simulate finding user but password does not match
        when(userRepo.findByEmail(loginDto.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(any(String.class), any(String.class))).thenReturn(false);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> authService.login(loginDto));

        assertEquals("Invalid password", exception.getMessage());
    }

    @Test
    void userLogin_WithEmptyEmail_ShouldThrowException() {
        // Arrange: simulate empty email scenario
        loginDto.setEmail("");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> authService.login(loginDto));

        assertEquals("Email cannot be empty", exception.getMessage());
    }

    @Test
    void userLogin_WithEmptyPassword_ShouldThrowException() {
        // Arrange: simulate empty password scenario
        loginDto.setPassword("");
        // Make sure the user is found even if password is blank
        when(userRepo.findByEmail(loginDto.getEmail())).thenReturn(Optional.of(testUser));
        // The password match check should now fail
        when(passwordEncoder.matches(eq(""), any(String.class))).thenReturn(false);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> authService.login(loginDto));

        assertEquals("Invalid password", exception.getMessage());
    }
}
