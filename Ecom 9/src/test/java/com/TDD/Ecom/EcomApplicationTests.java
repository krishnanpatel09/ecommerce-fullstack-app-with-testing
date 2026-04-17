package com.TDD.Ecom;

import com.TDD.Ecom.dto.SignupDto;
import com.TDD.Ecom.model.User;
import com.TDD.Ecom.repo.UserRepo;
import com.TDD.Ecom.service.AuthService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.properties")
class EcomApplicationTests {

	@MockBean
	private UserRepo userRepo;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Autowired
	private AuthService authService;

	private User testUser;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		testUser = new User();
		testUser.setUsername("testuser");
		testUser.setEmail("test@example.com");
		testUser.setPassword("EncodedPassword123");
		testUser.setRole("USER");

		when(passwordEncoder.encode(any(String.class))).thenReturn("EncodedPassword123");
	}

	@Test
	void testSuccessfulRegistration() {
		SignupDto signupDto = new SignupDto("JohnDoe", "john.doe@example.com", "StrongPass123!", "USER");
		Map<String, String> response = authService.signup(signupDto);
		assertNotNull(response);
		assertEquals("User Account Created successfully", response.get("message"));
		assertEquals("USER", response.get("role"));
	}

	@Test
	void testRegistrationFailsForEmptyEmail() {
		SignupDto signupDto = new SignupDto("JohnDoe", "", "StrongPass123!", "USER");
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			authService.signup(signupDto);
		});
		assertEquals("Email cannot be empty", exception.getMessage());
	}

	@Test
	void testRegistrationFailsForInvalidEmailFormat() {
		SignupDto signupDto = new SignupDto("JohnDoe", "invalid-email", "StrongPass123!", "USER");
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			authService.signup(signupDto);
		});
		assertEquals("Invalid email format", exception.getMessage());
	}

	@Test
	void testRegistrationFailsForEmptyPassword() {
		SignupDto signupDto = new SignupDto("JohnDoe", "john.doe@example.com", "", "USER");
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			authService.signup(signupDto);
		});
		assertEquals("Password cannot be empty", exception.getMessage());
	}

	@Test
	void testUserSignup() {
		SignupDto signupDto = new SignupDto();
		signupDto.setEmail("test@example.com");
		signupDto.setUserName("testuser");
		signupDto.setPassword("Password123");
		signupDto.setRole("USER");

		Map<String, String> response = authService.signup(signupDto);
		assertNotNull(response);
		assertEquals("User Account Created successfully", response.get("message"));
		assertEquals("USER", response.get("role"));
	}

	@Test
	void testSuccessfulUserRegistration() {
		SignupDto signupDto = new SignupDto();
		signupDto.setEmail("test@example.com");
		signupDto.setUserName("testuser");
		signupDto.setPassword("Password123");
		signupDto.setRole("USER");

		when(userRepo.findByEmail(signupDto.getEmail())).thenReturn(Optional.empty());
		when(userRepo.findByUsername(signupDto.getUserName())).thenReturn(Optional.empty());
		when(userRepo.save(any(User.class))).thenReturn(testUser);

		Map<String, String> response = authService.signup(signupDto);

		assertNotNull(response);
		assertEquals("User Account Created successfully", response.get("message"));
		assertEquals("USER", response.get("role"));
	}

	@Test
	void testRegistrationWithEmptyEmail() {
		SignupDto signupDto = new SignupDto();
		signupDto.setUserName("testuser");
		signupDto.setPassword("Password123");
		signupDto.setRole("USER");

		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			authService.signup(signupDto);
		});

		assertEquals("Email cannot be empty", exception.getMessage());
	}

	@Test
	void testRegistrationWithInvalidEmailFormat() {
		SignupDto signupDto = new SignupDto();
		signupDto.setEmail("invalid-email");
		signupDto.setUserName("testuser");
		signupDto.setPassword("Password123");
		signupDto.setRole("USER");

		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			authService.signup(signupDto);
		});

		assertEquals("Invalid email format", exception.getMessage());
	}

	@Test
	void testRegistrationWithEmptyPassword() {
		SignupDto signupDto = new SignupDto();
		signupDto.setEmail("test@example.com");
		signupDto.setUserName("testuser");
		signupDto.setRole("USER");

		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			authService.signup(signupDto);
		});

		assertEquals("Password cannot be empty", exception.getMessage());
	}

	@Test
	void testRegistrationWithExistingEmail() {
		SignupDto signupDto = new SignupDto();
		signupDto.setEmail("test@example.com");
		signupDto.setUserName("testuser");
		signupDto.setPassword("Password123"); // ✅ Must be valid
		signupDto.setRole("USER");

		when(userRepo.findByEmail(signupDto.getEmail())).thenReturn(Optional.of(testUser));

		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			authService.signup(signupDto);
		});

		assertEquals("Email already exists", exception.getMessage());
	}

}
