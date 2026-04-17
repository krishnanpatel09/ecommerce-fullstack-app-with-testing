package service;

import com.TDD.Ecom.dto.SignupDto;
import com.TDD.Ecom.model.User;
import com.TDD.Ecom.repo.UserRepo;
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

class AdminServiceTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User adminUser;
    private SignupDto signupDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("encodedPassword");
        adminUser.setRole("ADMIN");

        signupDto = new SignupDto();
        signupDto.setUserName("newadmin");
        signupDto.setEmail("newadmin@example.com");
        signupDto.setPassword("Admin123");
        signupDto.setRole("ADMIN"); // Even if provided, AuthService may override to "USER"

        when(passwordEncoder.encode(any(String.class))).thenReturn("encodedPassword");
    }

    @Test
    void adminSignup_WithValidData_ShouldCreateAdmin() {
        // Simulate that the email and username are not taken
        when(userRepo.findByEmail(signupDto.getEmail())).thenReturn(Optional.empty());
        when(userRepo.findByUsername(signupDto.getUserName())).thenReturn(Optional.empty());

        // When saving, verify that the user’s role is set to "USER" (as per AuthService default behavior)
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            assertEquals("USER", savedUser.getRole());
            return savedUser;
        });

        Map<String, String> response = authService.signup(signupDto);

        assertNotNull(response);
        assertEquals("User Account Created successfully", response.get("message"));
        assertEquals("USER", response.get("role"));
        verify(userRepo).save(any(User.class));
    }

    @Test
    void adminSignup_WithExistingEmail_ShouldThrowException() {
        when(userRepo.findByEmail(signupDto.getEmail())).thenReturn(Optional.of(adminUser));

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            authService.signup(signupDto);
        });

        assertEquals("Email already exists", thrown.getMessage());
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void adminSignup_WithExistingUsername_ShouldThrowException() {
        // Assume email is not taken but username is taken
        when(userRepo.findByEmail(signupDto.getEmail())).thenReturn(Optional.empty());
        when(userRepo.findByUsername(signupDto.getUserName())).thenReturn(Optional.of(adminUser));

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            authService.signup(signupDto);
        });
        assertEquals("Username already exists", thrown.getMessage());
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void regularUserAttemptingAdminAccess_ShouldReturnFalse() {
        User regularUser = new User();
        regularUser.setEmail("user@example.com");
        regularUser.setRole("USER");

        when(userRepo.findByEmail("user@example.com")).thenReturn(Optional.of(regularUser));

        assertFalse(authService.isAdmin("user@example.com"));
    }

    @Test
    void adminUserCheck_ShouldReturnTrue() {
        when(userRepo.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));

        assertTrue(authService.isAdmin("admin@example.com"));
    }

    @Test
    void isAdmin_UserNotFound_ShouldReturnFalse() {
        // If no user is found with the given email, then isAdmin should return false.
        when(userRepo.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        assertFalse(authService.isAdmin("nonexistent@example.com"));
    }
}
