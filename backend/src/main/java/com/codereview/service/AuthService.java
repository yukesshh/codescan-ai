package com.codereview.service;

import com.codereview.dto.AuthDTO;
import com.codereview.model.User;
import com.codereview.repository.UserRepository;
import com.codereview.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private CustomUserDetailsService userDetailsService;

    public AuthDTO.AuthResponse register(AuthDTO.RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername()))
            throw new RuntimeException("Username already taken");
        if (userRepository.existsByEmail(request.getEmail()))
            throw new RuntimeException("Email already registered");

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .build();

        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtUtil.generateToken(userDetails);

        return AuthDTO.AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .userId(user.getId())
                .build();
    }

    public AuthDTO.AuthResponse login(AuthDTO.LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtUtil.generateToken(userDetails);

        return AuthDTO.AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .userId(user.getId())
                .build();
    }
}