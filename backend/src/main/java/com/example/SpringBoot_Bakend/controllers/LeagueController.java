package com.example.SpringBoot_Bakend.controllers;

import com.example.SpringBoot_Bakend.dto.LeagueStatusResponse;
import com.example.SpringBoot_Bakend.entities.User;
import com.example.SpringBoot_Bakend.repository.LeagueStatusRepository;
import com.example.SpringBoot_Bakend.repository.UserRepository;
import com.example.SpringBoot_Bakend.service.DailyStreakService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequiredArgsConstructor
public class LeagueController {
    private final LeagueStatusRepository repository;
    private final UserRepository userRepository;
    private final DailyStreakService dailyStreakService;

    @GetMapping({"/league/status", "/api/v1/league/status"})
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

        LocalDate today = LocalDate.now();
        YearMonth currentYearMonth = YearMonth.from(today);
        int daysUntilReset = Math.max(1, currentYearMonth.lengthOfMonth() - today.getDayOfMonth() + 1);
        String seasonName = today.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US));
        int effectiveStreak = dailyStreakService.getEffectiveStreak(user, today);

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
                .seasonName(seasonName)
                .daysUntilMonthlyReset(daysUntilReset)
                .streakDays(effectiveStreak)
                .build());
    }

    @GetMapping({"/league/leaderboard", "/api/v1/league/leaderboard"})
    public ResponseEntity<Map<String, Object>> getLeaderboard(
            @RequestParam(defaultValue = "Bronze Division") String tier) {
        LocalDate today = LocalDate.now();
        String seasonName = today.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US));

        List<User> topUsers = userRepository.findAll().stream()
                .filter(u -> u.getRole() != null && u.getRole().name().equals("PATIENT"))
                .sorted((a, b) -> Integer.compare(
                        b.getMonthlyLeagueXp() != null ? b.getMonthlyLeagueXp() : 0,
                        a.getMonthlyLeagueXp() != null ? a.getMonthlyLeagueXp() : 0))
                .limit(20)
                .toList();

        List<Map<String, Object>> standings = new ArrayList<>();
        int rank = 1;
        for (User u : topUsers) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("rank", rank++);
            entry.put("userId", u.getId());
            entry.put("userName", u.getName());
            entry.put("monthlyXp", u.getMonthlyLeagueXp() != null ? u.getMonthlyLeagueXp() : 0);
            entry.put("avatarUri", u.getAvatarUri());
            entry.put("streak", u.getStreakDays() != null ? u.getStreakDays() : 0);
            entry.put("leagueTier", u.getLeagueTier());
            standings.add(entry);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("tier", tier);
        result.put("season", seasonName);
        result.put("standings", standings);

        return ResponseEntity.ok(result);
    }
}
