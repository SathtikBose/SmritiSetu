package com.example.SpringBoot_Bakend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LevelAttemptResponse {
    @Builder.Default
    private Boolean passed = true;
    private Integer xpEarned;
    private Integer coinsEarned;
    private Integer totalCoins;
    private Integer totalXp;
    private Integer monthlyLeagueXp;
    private String leagueTier;
    private String newLeague;
    private Integer highestUnlockedLevel;
    private Integer highestUnlockedPatternLevel;
    private Integer nextUnlockedLevel;
    private Integer nextLevel;
    private Integer newDifficulty;
    private String predictedDifficulty;
    private String difficultyReasoning;
    private String aiReasoningMessage;
    private Integer streakDays;
}
