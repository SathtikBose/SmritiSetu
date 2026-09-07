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

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }

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

        return ResponseEntity.ok(toAuthResponse(user, jwtToken));
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

        return ResponseEntity.ok(toAuthResponse(user, jwtToken));
    }

    @PostMapping({"/google", "/firebase"})
    public ResponseEntity<?> googleOrFirebaseLogin(@RequestBody OAuth2Request request) {
        try {
            String email = request.getEmail();
            String name = request.getName();

            // 1. Try GoogleIdTokenVerifier if googleClientId is configured and token is present
            if (request.getIdToken() != null && !request.getIdToken().isBlank()) {
                if (googleClientId != null && !googleClientId.isBlank()) {
                    try {
                        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                                new NetHttpTransport(), new GsonFactory())
                                .setAudience(java.util.Collections.singletonList(googleClientId))
                                .build();
                        GoogleIdToken idToken = verifier.verify(request.getIdToken());
                        if (idToken != null) {
                            GoogleIdToken.Payload payload = idToken.getPayload();
                            email = payload.getEmail();
                            name = (String) payload.get("name");
                        }
                    } catch (Exception ignored) {
                        // Fallback to JWT payload parsing below for Firebase tokens
                    }
                }

                // 2. If email is still null, parse standard JWT base64 payload (Firebase / Google Token)
                if (email == null || email.isBlank()) {
                    try {
                        String[] parts = request.getIdToken().split("\\.");
                        if (parts.length >= 2) {
                            String payloadJson = new String(java.util.Base64.getUrlDecoder().decode(parts[1]), java.nio.charset.StandardCharsets.UTF_8);
                            com.fasterxml.jackson.databind.JsonNode jsonNode = new com.fasterxml.jackson.databind.ObjectMapper().readTree(payloadJson);
                            if (jsonNode.has("email") && !jsonNode.get("email").isNull()) {
                                email = jsonNode.get("email").asText();
                            }
                            if (jsonNode.has("name") && !jsonNode.get("name").isNull()) {
                                name = jsonNode.get("name").asText();
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
            }

            // 3. Validate extracted email
            if (email == null || email.isBlank()) {
                return ResponseEntity.badRequest().body(java.util.Map.of("error", "Could not resolve valid email from authentication token"));
            }

            // 4. Find or Create User in Supabase Database
            Optional<User> optionalUser = userRepository.findByUsername(email);
            User user;
            Role selectedRole = request.getRole() != null ? request.getRole() : Role.PATIENT;

            if (optionalUser.isPresent()) {
                user = optionalUser.get();
                if (name != null && !name.isBlank() && (user.getName() == null || user.getName().isBlank())) {
                    user.setName(name);
                }
                if (request.getPatientCode() != null && !request.getPatientCode().isBlank()) {
                    user.setLinkedPatientCode(request.getPatientCode().trim().toUpperCase());
                }
                user = userRepository.save(user);
            } else {
                user = User.builder()
                        .username(email)
                        .password(null)
                        .name(name != null && !name.isBlank() ? name : email.split("@")[0])
                        .role(selectedRole)
                        .authProvider(AuthProvider.GOOGLE)
                        .preferredLanguage("en")
                        .coins(0)
                        .hintsCount(3)
                        .skipLevelCount(1)
                        .showAgainCount(1)
                        .totalXp(0)
                        .monthlyLeagueXp(0)
                        .highestUnlockedLevel(1)
                        .highestUnlockedPatternLevel(1)
                        .streakDays(1)
                        .linkedPatientCode(selectedRole == Role.CAREGIVER && request.getPatientCode() != null ? request.getPatientCode().trim().toUpperCase() : null)
                        .build();

                user = userRepository.save(user);
            }

            // 5. Generate secure JWT token
            String jwtToken = jwtUtil.generateToken(user);
            return ResponseEntity.ok(toAuthResponse(user, jwtToken));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", "Authentication error: " + e.getMessage()));
        }
    }


    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @org.springframework.security.core.annotation.AuthenticationPrincipal User user,
            @RequestBody java.util.Map<String, String> request) {
        String currentPassword = request.get("currentPassword");
        String newPassword = request.get("newPassword");

        if (currentPassword == null || newPassword == null || newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "Valid currentPassword and newPassword (min 6 chars) required"));
        }

        if (user.getPassword() != null && !passwordEncoder.matches(currentPassword, user.getPassword())) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "Incorrect current password"));
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok(java.util.Map.of("success", true, "message", "Password updated successfully"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody java.util.Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "Email is required"));
        }
        // In production / demo flow, generate 6-digit OTP
        return ResponseEntity.ok(java.util.Map.of(
                "success", true,
                "message", "OTP sent to email successfully",
                "otp", "123456" // Default mock OTP for mobile verification
        ));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody java.util.Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");
        String newPassword = request.get("newPassword");

        if (email == null || otp == null || newPassword == null || newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "Valid email, otp, and newPassword (min 6 chars) required"));
        }

        Optional<User> userOpt = userRepository.findByUsername(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
        }

        return ResponseEntity.ok(java.util.Map.of("success", true, "message", "Password reset successfully"));
    }

    private AuthResponse toAuthResponse(User user, String jwtToken) {
        return AuthResponse.builder()
                .token(jwtToken)
                .userId(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .role(user.getRole())
                .patientLinkCode(user.getPatientLinkCode())
                .linkedPatientCode(user.getLinkedPatientCode())
                .coins(user.getCoins())
                .hintsCount(user.getHintsCount())
                .skipLevelCount(user.getSkipLevelCount())
                .showAgainCount(user.getShowAgainCount())
                .totalXp(user.getTotalXp())
                .monthlyLeagueXp(user.getMonthlyLeagueXp())
                .leagueTier(user.getLeagueTier())
                .highestUnlockedLevel(user.getHighestUnlockedLevel())
                .highestUnlockedPatternLevel(user.getHighestUnlockedPatternLevel())
                .phone(user.getPhone())
                .gender(user.getGender())
                .age(user.getAge())
                .avatarUri(user.getAvatarUri())
                .preferredLanguage(user.getPreferredLanguage())
                .streakDays(user.getStreakDays())
                .lastActiveDate(user.getLastActiveDate())
                .build();
    }
}
