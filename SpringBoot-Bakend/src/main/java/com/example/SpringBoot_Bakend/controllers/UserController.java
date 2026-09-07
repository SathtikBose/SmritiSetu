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
                .build();
    }
}
