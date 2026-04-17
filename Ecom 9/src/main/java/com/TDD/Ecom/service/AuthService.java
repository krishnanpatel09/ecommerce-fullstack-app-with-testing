package com.TDD.Ecom.service;

import com.TDD.Ecom.dto.LoginDto;
import com.TDD.Ecom.dto.SignupDto;
import com.TDD.Ecom.model.User;
import com.TDD.Ecom.repo.UserRepo;
import com.TDD.Ecom.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class AuthService {
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public AuthService(UserRepo userRepo, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public Map<String, String> signup(SignupDto signupDto) {
        // Validate email
        if (signupDto.getEmail() == null || signupDto.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }

        if (!isValidEmail(signupDto.getEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }

        Optional<User> existingUserByEmail = userRepo.findByEmail(signupDto.getEmail());
        if (existingUserByEmail.isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Validate username
        Optional<User> existingUserByUsername = userRepo.findByUsername(signupDto.getUserName());
        if (existingUserByUsername.isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Validate password
        if (signupDto.getPassword() == null || signupDto.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        if (signupDto.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }

        if (!signupDto.getPassword().matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter");
        }

        if (!signupDto.getPassword().matches(".*[a-z].*")) {
            throw new IllegalArgumentException("Password must contain at least one lowercase letter");
        }

        if (!signupDto.getPassword().matches(".*\\d.*")) {
            throw new IllegalArgumentException("Password must contain at least one number");
        }

        // Create new user
        User user = new User();
        user.setUsername(signupDto.getUserName());
        user.setEmail(signupDto.getEmail());
        user.setPassword(passwordEncoder.encode(signupDto.getPassword()));
        user.setRole("USER"); // Default role

        userRepo.save(user);

        Map<String, String> response = new HashMap<>();
        response.put("message", "User Account Created successfully");
        response.put("role", user.getRole());
        return response;
    }

    public Map<String, String> login(LoginDto loginDto) {
        if (loginDto.getEmail() == null || loginDto.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        
        String email = loginDto.getEmail().trim().toLowerCase();
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format");
        }

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }

        String token = jwtTokenProvider.generateToken(user);
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("role", user.getRole());
        return response;
    }

    public boolean isEmailRegistered(String email) {
        Optional<User> userOptional = userRepo.findByEmail(email);
        return userOptional.isPresent();
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        email = email.trim().toLowerCase();
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }

    public boolean isAdmin(String email) {
        Optional<User> userOptional = userRepo.findByEmail(email);
        return userOptional.isPresent() && "ADMIN".equals(userOptional.get().getRole());
    }
}
