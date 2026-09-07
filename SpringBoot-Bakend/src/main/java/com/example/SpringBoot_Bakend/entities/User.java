package com.example.SpringBoot_Bakend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String username; // Used for login

    @Column
    private String password;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AuthProvider authProvider = AuthProvider.LOCAL;

    @Column(name = "preferred_language")
    @Builder.Default
    private String preferredLanguage = "en"; // default English

    @Column(name = "patient_link_code", length = 10)
    private String patientLinkCode; // Unique code (e.g. "SM-8492")

    @Column(name = "linked_patient_code", length = 10)
    private String linkedPatientCode; // For Caregivers

    @Column(nullable = false)
    @Builder.Default
    private Integer coins = 1000;

    @Column(name = "hints_count", nullable = false)
    @Builder.Default
    private Integer hintsCount = 0;

    @Column(name = "skip_level_count", nullable = false)
    @Builder.Default
    private Integer skipLevelCount = 0;

    @Column(name = "show_again_count", nullable = false)
    @Builder.Default
    private Integer showAgainCount = 0;

    @Column(name = "total_xp", nullable = false)
    @Builder.Default
    private Integer totalXp = 1450;

    @Column(name = "monthly_league_xp", nullable = false)
    @Builder.Default
    private Integer monthlyLeagueXp = 0;

    @Column(name = "league_tier", nullable = false, length = 40)
    @Builder.Default
    private String leagueTier = "Bronze Division";

    @Column(name = "last_season_reset_month", length = 7)
    @Builder.Default
    private String lastSeasonResetMonth = "2026-09";

    @Column
    private String phone;

    @Column
    private String gender;

    @Column
    private Integer age;

    @Column(name = "avatar_uri")
    private String avatarUri;

    @Column(name = "highest_unlocked_level", nullable = false)
    @Builder.Default
    private Integer highestUnlockedLevel = 5;

    @Column(name = "highest_unlocked_pattern_level", nullable = false)
    @Builder.Default
    private Integer highestUnlockedPatternLevel = 5;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.patientLinkCode == null && this.role == Role.PATIENT) {
            this.patientLinkCode = "SM-" + ((int)(Math.random() * 9000) + 1000);
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
