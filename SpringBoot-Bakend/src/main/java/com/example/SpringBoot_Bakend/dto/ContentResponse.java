package com.example.SpringBoot_Bakend.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data @Builder
public class ContentResponse {
    private String language;
    private String gameId;
    private Map<String, String> content;
}
