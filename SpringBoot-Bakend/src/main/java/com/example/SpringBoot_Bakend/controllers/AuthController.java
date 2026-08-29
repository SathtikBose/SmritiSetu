package com.example.SpringBoot_Bakend.controllers;

import com.example.SpringBoot_Bakend.dto.AuthRequest;
import com.example.SpringBoot_Bakend.dto.AuthResponse;
import com.example.SpringBoot_Bakend.dto.OAuth2Request;
import com.example.SpringBoot_Bakend.dto.RegisterRequest;
import com.example.SpringBoot_Bakend.entities.AuthProvider;
import com.example.SpringBoot_Bakend.entities.Role;
import com.example.SpringBoot_Bakend.entities.User;
import com.example.SpringBoot_Bakend.repository.UserRepository;
import com.example.SpringBoot_Bakend.security.JwtUtil;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String googleClientId;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        // Create user via LOCAL auth
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .role(request.getRole() != null ? request.getRole() : Role.PATIENT)
                .authProvider(AuthProvider.LOCAL)
                .preferredLanguage(request.getPreferredLanguage() != null ? request.getPreferredLanguage() : "en")
                .build();

        userRepository.save(user);

        // Generate token
        String jwtToken = jwtUtil.generateToken(user);

        return ResponseEntity.ok(AuthResponse.builder()
                .token(jwtToken)
                .userId(user.getId())
                .role(user.getRole())
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow();

        String jwtToken = jwtUtil.generateToken(user);

        return ResponseEntity.ok(AuthResponse.builder()
                .token(jwtToken)
                .userId(user.getId())
                .role(user.getRole())
                .build());
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody OAuth2Request request) {
        try {
            if (googleClientId.isBlank()) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Google login is not configured.");
            }
            // Setup Verifier
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), new GsonFactory())
                    .setAudience(java.util.Collections.singletonList(googleClientId))
                    .build();

            // Verify Token
            GoogleIdToken idToken = verifier.verify(request.getIdToken());
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                
                // Get profile info from payload
                String email = payload.getEmail(); // We'll use email as the username
                String name = (String) payload.get("name");

                // Check if user exists in our database
                Optional<User> optionalUser = userRepository.findByUsername(email);
                User user;

                if (optionalUser.isPresent()) {
                    user = optionalUser.get();
                } else {
                    // Register the new user via GOOGLE auth
                    user = User.builder()
                            .username(email)
                            .password(null) // No password for OAuth users
                            .name(name)
                            .role(request.getRole() != null ? request.getRole() : Role.PATIENT)
                            .authProvider(AuthProvider.GOOGLE)
                            .preferredLanguage("en")
                            .build();
                    
                    user = userRepository.save(user);
                }

                // Generate our custom JWT token
                String jwtToken = jwtUtil.generateToken(user);

                return ResponseEntity.ok(AuthResponse.builder()
                        .token(jwtToken)
                        .userId(user.getId())
                        .role(user.getRole())
                        .build());
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid ID token.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error verifying token: " + e.getMessage());
        }
    }
}
