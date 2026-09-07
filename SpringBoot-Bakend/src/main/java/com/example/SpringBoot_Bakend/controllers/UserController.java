package com.example.SpringBoot_Bakend.controllers;

import com.example.SpringBoot_Bakend.dto.UpdateProfileRequest;
import com.example.SpringBoot_Bakend.dto.UserProfileResponse;
import com.example.SpringBoot_Bakend.entities.User;
import com.example.SpringBoot_Bakend.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/user") @RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> profile(@AuthenticationPrincipal User user) { return ResponseEntity.ok(toResponse(user)); }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(@AuthenticationPrincipal User user, @Valid @RequestBody UpdateProfileRequest request) {
        user.setName(request.getName());
        if (request.getPreferredLanguage() != null) user.setPreferredLanguage(request.getPreferredLanguage());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getGender() != null) user.setGender(request.getGender());
        if (request.getAge() != null) user.setAge(request.getAge());
        if (request.getAvatarUri() != null) user.setAvatarUri(request.getAvatarUri());
        return ResponseEntity.ok(toResponse(userRepository.save(user)));
    }

    @PostMapping("/perks/use")
    public ResponseEntity<?> usePerk(@AuthenticationPrincipal User user, @RequestBody java.util.Map<String, String> body) {
        String perkType = body.get("perkType");
        if (perkType == null) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "perkType is required"));
        }

        String typeUpper = perkType.toUpperCase();
        int remaining;

        if (typeUpper.contains("HINT")) {
            if (user.getHintsCount() <= 0) {
                return ResponseEntity.badRequest().body(java.util.Map.of("error", "No Hint perks in inventory."));
            }
            user.setHintsCount(user.getHintsCount() - 1);
            remaining = user.getHintsCount();
        } else if (typeUpper.contains("PEEK") || typeUpper.contains("SHOW_AGAIN")) {
            if (user.getShowAgainCount() <= 0) {
                return ResponseEntity.badRequest().body(java.util.Map.of("error", "No Peek/Show Again perks in inventory."));
            }
            user.setShowAgainCount(user.getShowAgainCount() - 1);
            remaining = user.getShowAgainCount();
        } else if (typeUpper.contains("SKIP")) {
            if (user.getSkipLevelCount() <= 0) {
                return ResponseEntity.badRequest().body(java.util.Map.of("error", "No Skip Level perks in inventory."));
            }
            user.setSkipLevelCount(user.getSkipLevelCount() - 1);
            remaining = user.getSkipLevelCount();
        } else {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "Unknown perk type: " + perkType));
        }

        userRepository.save(user);
        return ResponseEntity.ok(java.util.Map.of("success", true, "remainingCount", remaining));
    }

    private UserProfileResponse toResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .role(user.getRole())
                .preferredLanguage(user.getPreferredLanguage())
                .patientLinkCode(user.getPatientLinkCode())
                .linkedPatientCode(user.getLinkedPatientCode())
                .coins(user.getCoins())
                .hintsCount(user.getHintsCount())
                .skipLevelCount(user.getSkipLevelCount())
                .showAgainCount(user.getShowAgainCount())
                .totalXp(user.getTotalXp())
                .monthlyLeagueXp(user.getMonthlyLeagueXp())
                .leagueTier(user.getLeagueTier())
                .phone(user.getPhone())
                .gender(user.getGender())
                .age(user.getAge())
                .avatarUri(user.getAvatarUri())
                .highestUnlockedLevel(user.getHighestUnlockedLevel())
                .highestUnlockedPatternLevel(user.getHighestUnlockedPatternLevel())
                .streakDays(user.getStreakDays())
                .lastActiveDate(user.getLastActiveDate())
                .build();
    }
}
