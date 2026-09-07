package com.example.SpringBoot_Bakend.dto;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class LeagueStatusResponse {
    private String currentLeague;
    private String currentTier;
    private Integer monthlyXp;
    private Integer lifetimeTotalXp;
    private Integer totalXp;
    private String nextTier;
    private Integer xpToNextLeague;
    private Integer xpNeededForNextTier;
    private Integer levelsNeededForNextTier;
    private Float tierProgressPercentage;
    private String seasonName;
    private Integer daysUntilMonthlyReset;
    private Integer streakDays;
}
