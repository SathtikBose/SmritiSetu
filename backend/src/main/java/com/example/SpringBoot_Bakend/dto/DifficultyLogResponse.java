package com.example.SpringBoot_Bakend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DifficultyLogResponse {
    private String gameName;
    private String aiDecision;
    private String reasoningText; // The plain-text explanation for the caregiver
    private LocalDateTime loggedAt;
}
