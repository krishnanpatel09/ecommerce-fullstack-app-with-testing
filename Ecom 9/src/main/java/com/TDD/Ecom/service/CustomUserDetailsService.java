package com.TDD.Ecom.service;

import com.TDD.Ecom.model.User;
import com.TDD.Ecom.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(login)
                .orElseGet(() -> userRepository.findByUsername(login)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found with email/username: " + login)));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail() != null ? user.getEmail() : user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole())
                .build();
    }
} 