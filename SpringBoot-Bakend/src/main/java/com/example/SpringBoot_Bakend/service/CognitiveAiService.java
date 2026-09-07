package com.example.SpringBoot_Bakend.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class CognitiveAiService {

    private final RestTemplate restTemplate;
    private final String aiBaseUrl;

    public CognitiveAiService(
            RestTemplateBuilder builder,
            @Value("${ai.service.base-url:https://smritisetuai.onrender.com}") String aiBaseUrl) {
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(10))
                .build();
        this.aiBaseUrl = aiBaseUrl.endsWith("/") ? aiBaseUrl.substring(0, aiBaseUrl.length() - 1) : aiBaseUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LevelMetric {
        private Integer level;
        private Long time_taken_ms;
        private Integer tries_count;
        private Integer total_cards;
        private Integer idle_hints_triggered;
        private Integer perk_hints_used;
        private String difficulty;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PredictionRequest {
        private String user_id;
        private Integer user_age;
        private String game_name;
        private List<LevelMetric> last_5_levels;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PredictionResponse {
        private String predicted_difficulty;
        private Double confidence;
        private Integer suggested_timer_seconds;
        private Integer suggested_idle_hint_seconds;
        private Double cognitive_performance_index;
        private Boolean fatigue_detected;
        private String clinical_rationale;
    }

    public PredictionResponse predictDifficulty(PredictionRequest request) {
        try {
            String url = aiBaseUrl + "/predict";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<PredictionRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<PredictionResponse> response = restTemplate.postForEntity(url, entity, PredictionResponse.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.warn("Cognitive AI service at {} is unreachable or returned error: {}", aiBaseUrl, e.getMessage());
        }
        return null;
    }
}
