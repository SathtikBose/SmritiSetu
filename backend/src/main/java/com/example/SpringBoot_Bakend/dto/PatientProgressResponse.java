package com.example.SpringBoot_Bakend.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class PatientProgressResponse {
    private String patientName;
    private String currentLeague;
    private Integer totalXp;
    private List<GameProgressDto> gameProgress;
    
    @Data
    @Builder
    public static class GameProgressDto {
        private String gameName;
        private Integer currentLevel;
        private Integer currentDifficulty;
        private String lastPlayed;
    }
}
