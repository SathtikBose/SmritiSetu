package com.example.SpringBoot_Bakend.controllers;

import com.example.SpringBoot_Bakend.dto.LeagueStatusResponse;
import com.example.SpringBoot_Bakend.entities.LeagueStatus;
import com.example.SpringBoot_Bakend.entities.User;
import com.example.SpringBoot_Bakend.repository.LeagueStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/league") @RequiredArgsConstructor
public class LeagueController {
    private final LeagueStatusRepository repository;

    @GetMapping("/status")
    public ResponseEntity<LeagueStatusResponse> status(@AuthenticationPrincipal User user) {
        int monthlyXp = user.getMonthlyLeagueXp() != null ? user.getMonthlyLeagueXp() : 0;
        int totalXp = user.getTotalXp() != null ? user.getTotalXp() : 1450;

        String tierName;
        String nextTierName;
        int nextTierXp;
        int tierMinXp;

        if (monthlyXp < 225) {
            tierName = "Bronze Division";
            nextTierName = "Silver Division";
            tierMinXp = 0;
            nextTierXp = 225;
        } else if (monthlyXp < 450) {
            tierName = "Silver Division";
            nextTierName = "Gold Division";
            tierMinXp = 225;
            nextTierXp = 450;
        } else if (monthlyXp < 675) {
            tierName = "Gold Division";
            nextTierName = "Platinum Division";
            tierMinXp = 450;
            nextTierXp = 675;
        } else if (monthlyXp < 900) {
            tierName = "Platinum Division";
            nextTierName = "Diamond Division";
            tierMinXp = 675;
            nextTierXp = 900;
        } else {
            tierName = "Diamond Division";
            nextTierName = null;
            tierMinXp = 900;
            nextTierXp = 900;
        }

        int xpNeeded = nextTierName != null ? Math.max(0, nextTierXp - monthlyXp) : 0;
        int levelsNeeded = nextTierName != null ? (xpNeeded + 14) / 15 : 0;
        float progressPct = nextTierName != null ? (float)(monthlyXp - tierMinXp) / (float)(nextTierXp - tierMinXp) : 1.0f;

        return ResponseEntity.ok(LeagueStatusResponse.builder()
                .currentLeague(tierName)
                .currentTier(tierName)
                .monthlyXp(monthlyXp)
                .lifetimeTotalXp(totalXp)
                .totalXp(totalXp)
                .nextTier(nextTierName)
                .xpToNextLeague(xpNeeded)
                .xpNeededForNextTier(xpNeeded)
                .levelsNeededForNextTier(levelsNeeded)
                .tierProgressPercentage(progressPct)
                .seasonName("September 2026")
                .daysUntilMonthlyReset(23)
                .streakDays(7)
                .build());
    }
}
