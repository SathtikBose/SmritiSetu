package com.example.SpringBoot_Bakend.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class LevelAttemptRequest {
    private UUID gameId;
    private String gameName;
    private Integer level;
    private Integer timeTakenSec;
    private Long timeTakenMs;
    private Integer triesCount;
    private Integer totalCards;
    private Integer hintsUsed; // Legacy/General hints
    private Integer idleHintsCount; // Inactivity auto-hints
    private Integer perkHintsCount; // Purchased hint perks used
    private Integer peekUsedCount; // Peek perk used
    private Integer previewTimeSec; // Preview duration
    private Integer previewDurationSec;
    private Integer patternLength;
    private String difficulty;
    private Boolean skipped = false;
    private Boolean syncedOffline = false;

    public Integer getEffectiveHintsUsed() {
        if (hintsUsed != null) return hintsUsed;
        int total = 0;
        if (idleHintsCount != null) total += idleHintsCount;
        if (perkHintsCount != null) total += perkHintsCount;
        return total;
    }

    public Integer getEffectiveTimeTakenSec() {
        if (timeTakenSec != null) return timeTakenSec;
        if (timeTakenMs != null) return (int) (timeTakenMs / 1000);
        return 30;
    }

    public Integer getEffectiveTriesCount() {
        if (triesCount != null) return triesCount;
        return 1;
    }
}
