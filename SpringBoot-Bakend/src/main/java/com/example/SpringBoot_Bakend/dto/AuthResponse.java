package com.example.SpringBoot_Bakend.dto;

import com.example.SpringBoot_Bakend.entities.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private UUID userId;
    private String name;
    private String username;
    private Role role;
    private String patientLinkCode;
    private String linkedPatientCode;
    private Integer coins;
    private Integer hintsCount;
    private Integer skipLevelCount;
    private Integer showAgainCount;
    private Integer totalXp;
    private Integer monthlyLeagueXp;
    private String leagueTier;
    private Integer highestUnlockedLevel;
    private Integer highestUnlockedPatternLevel;
    private String phone;
    private String gender;
    private Integer age;
    private String avatarUri;
    private String preferredLanguage;
    private Integer streakDays;
    private String lastActiveDate;
}
