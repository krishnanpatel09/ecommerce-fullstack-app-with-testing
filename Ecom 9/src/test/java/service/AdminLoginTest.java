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

class AdminLoginTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    private JwtTokenProvider jwtTokenProvider;  // Using a regular mock

    @InjectMocks
    private AuthService authService;

    private User adminUser;
    private LoginDto loginDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Manually creating a mock for JwtTokenProvider
        jwtTokenProvider = new FakeJwtTokenProvider();

        // Injecting the mock into AuthService manually
        authService = new AuthService(userRepo, passwordEncoder, jwtTokenProvider);

        adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("encodedPassword");
        adminUser.setRole("ADMIN");

        loginDto = new LoginDto();
        loginDto.setEmail("admin@example.com");
        loginDto.setPassword("Admin123");

        when(passwordEncoder.matches(any(String.class), any(String.class))).thenReturn(true);
    }

    // Fake JwtTokenProvider for testing
    static class FakeJwtTokenProvider extends JwtTokenProvider {
        @Override
        public String generateToken(User user) {
            return "mocked-jwt-token"; // Return a fake token
        }
    }

    @Test
    void adminLogin_WithValidCredentials_ShouldReturnSuccessResponse() {
        when(userRepo.findByEmail(loginDto.getEmail())).thenReturn(Optional.of(adminUser));

        Map<String, String> response = authService.login(loginDto);

        assertNotNull(response);
        assertEquals("mocked-jwt-token", response.get("token"));
        assertEquals("ADMIN", response.get("role"));
    }

    @Test
    void adminLogin_WithInvalidEmail_ShouldThrowException() {
        when(userRepo.findByEmail(loginDto.getEmail())).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.login(loginDto);
        });

        assertEquals("User not found with email: admin@example.com", exception.getMessage());  // Updated message
    }


    @Test
    void adminLogin_WithInvalidPassword_ShouldThrowException() {
        when(userRepo.findByEmail(loginDto.getEmail())).thenReturn(Optional.of(adminUser));
        when(passwordEncoder.matches(any(String.class), any(String.class))).thenReturn(false);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.login(loginDto);
        });

        assertEquals("Invalid password", exception.getMessage()); // Update to match actual message
    }


    @Test
    void adminLogin_WithNonAdminUser_ShouldReturnUserRole() {
        User regularUser = new User();
        regularUser.setUsername("user");
        regularUser.setEmail("user@example.com");
        regularUser.setPassword("encodedPassword");
        regularUser.setRole("USER");

        when(userRepo.findByEmail(loginDto.getEmail())).thenReturn(Optional.of(regularUser));

        Map<String, String> response = authService.login(loginDto);

        assertNotNull(response);
        assertEquals("USER", response.get("role"));
        assertEquals("mocked-jwt-token", response.get("token"));
    }
}
