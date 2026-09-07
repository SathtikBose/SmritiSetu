package com.example.SpringBoot_Bakend.dto;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data @Builder
public class GameResponse {
    private UUID id;
    private String name;
    private String type;
}
