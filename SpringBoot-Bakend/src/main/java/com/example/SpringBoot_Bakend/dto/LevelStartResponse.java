package com.example.SpringBoot_Bakend.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;
import java.util.UUID;

@Data @Builder
public class LevelStartResponse {
    private UUID gameId;
    private String gameType;
    private Integer level;
    private Integer difficulty;
    private String language;
    private Map<String, Object> levelConfig;
}
