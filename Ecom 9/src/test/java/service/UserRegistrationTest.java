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

class UserRegistrationTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private SignupDto signupDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

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

        when(passwordEncoder.encode(any(String.class))).thenReturn("encodedPassword");
    }

    @Test
    void userRegistration_WithValidData_ShouldCreateUser() {
        // Arrange: email and username do not already exist
        when(userRepo.findByEmail(signupDto.getEmail())).thenReturn(Optional.empty());
        when(userRepo.findByUsername(signupDto.getUserName())).thenReturn(Optional.empty());
        when(userRepo.save(any(User.class))).thenReturn(testUser);

        // Act
        Map<String, String> response = authService.signup(signupDto);

        // Assert
        assertNotNull(response);
        assertEquals("User Account Created successfully", response.get("message"));
        // Even if signupDto.role is set, the service might default to USER.
        assertEquals("USER", response.get("role"));
        verify(userRepo).save(any(User.class));
    }

    @Test
    void userRegistration_WithExistingEmail_ShouldThrowException() {
        // Arrange: simulate existing email
        when(userRepo.findByEmail(signupDto.getEmail())).thenReturn(Optional.of(testUser));

        // Act
        Exception exception = assertThrows(IllegalArgumentException.class, () -> authService.signup(signupDto));

        // Assert
        assertEquals("Email already exists", exception.getMessage());
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void userRegistration_WithExistingUsername_ShouldThrowException() {
        // Arrange: email is free, but username is taken
        when(userRepo.findByEmail(signupDto.getEmail())).thenReturn(Optional.empty());
        when(userRepo.findByUsername(signupDto.getUserName())).thenReturn(Optional.of(testUser));

        // Act
        Exception exception = assertThrows(IllegalArgumentException.class, () -> authService.signup(signupDto));

        // Assert
        assertEquals("Username already exists", exception.getMessage());
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void userRegistration_WithInvalidRole_ShouldDefaultToUserRole() {
        // Arrange: set an invalid role
        signupDto.setRole("INVALID_ROLE");
        // Simulate that the email and username are not already taken
        when(userRepo.findByEmail(signupDto.getEmail())).thenReturn(Optional.empty());
        when(userRepo.findByUsername(signupDto.getUserName())).thenReturn(Optional.empty());
        // When saving the user, let the service decide the role
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            return savedUser;
        });

        // Act
        Map<String, String> response = authService.signup(signupDto);

        // Assert
        assertNotNull(response);
        assertEquals("User Account Created successfully", response.get("message"));
        // The service should default the role to "USER" even if an invalid role was given.
        assertEquals("USER", response.get("role"));

        verify(userRepo).save(any(User.class));
    }
}
