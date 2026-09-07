package com.example.SpringBoot_Bakend.dto;

import com.example.SpringBoot_Bakend.entities.Role;
import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data @Builder
public class UserProfileResponse {
    private UUID id;
    private String username;
    private String name;
    private Role role;
    private String preferredLanguage;
    private String patientLinkCode;
    private String linkedPatientCode;
    private Integer coins;
    private Integer hintsCount;
    private Integer skipLevelCount;
    private Integer showAgainCount;
    private Integer totalXp;
    private Integer monthlyLeagueXp;
    private String leagueTier;
    private String phone;
    private String gender;
    private Integer age;
    private String avatarUri;
    private Integer highestUnlockedLevel;
    private Integer highestUnlockedPatternLevel;
    private Integer streakDays;
    private String lastActiveDate;
}
