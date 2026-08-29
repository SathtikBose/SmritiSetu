package com.example.SpringBoot_Bakend.dto;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class LeagueStatusResponse {
    private String currentLeague;
    private Integer totalXp;
    private Integer xpToNextLeague;
}
