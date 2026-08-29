package com.example.SpringBoot_Bakend.dto;

import lombok.Data;
import java.util.UUID;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Data
public class LevelAttemptRequest {
    @NotNull private UUID gameId;
    @NotNull @Min(0) private Integer timeTakenSec;
    @NotNull @Min(0) private Integer hintsUsed;
    @NotNull @Min(1) private Integer triesCount;
    private Boolean syncedOffline; // Will be true if the React Native app buffered it while offline
}
