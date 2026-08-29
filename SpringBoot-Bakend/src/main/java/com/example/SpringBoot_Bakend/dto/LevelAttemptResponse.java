package com.example.SpringBoot_Bakend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LevelAttemptResponse {
    private Integer xpEarned;
    private Integer newDifficulty;
    private Integer nextLevel;
    private String newLeague;
    private String aiReasoningMessage; // Sent back when the AI adjusts the difficulty (every 5 levels)
}
