package com.example.SpringBoot_Bakend.service;

import com.example.SpringBoot_Bakend.entities.*;
import com.example.SpringBoot_Bakend.repository.DifficultyLogRepository;
import com.example.SpringBoot_Bakend.repository.LevelAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AIDifficultyService {
    
    private final DifficultyLogRepository difficultyLogRepository;
    private final LevelAttemptRepository levelAttemptRepository;
    private final CognitiveAiService cognitiveAiService;

    public DifficultyLog evaluateAndAdjustDifficulty(GameProgress progress) {
        // Fetch last 10 attempts (we look at recent trends)
        List<LevelAttempt> recentAttempts = levelAttemptRepository.findTop10ByProgressIdOrderByPlayedAtDesc(progress.getId());
        
        if (recentAttempts.size() < 5) return null; // Not enough data yet
        
        // Take the 5 most recent attempts
        List<LevelAttempt> last5 = recentAttempts.subList(0, 5);
        
        double avgTime = last5.stream().mapToInt(LevelAttempt::getTimeTakenSec).average().orElse(0.0);
        double avgHints = last5.stream().mapToInt(LevelAttempt::getHintsUsed).average().orElse(0.0);
        double avgTries = last5.stream().mapToInt(LevelAttempt::getTriesCount).average().orElse(0.0);
        
        int currentDiff = progress.getCurrentDifficulty();
        int newDiff = currentDiff;
        String reasoning = "";
        String decision = "HOLD";

        // Try AI Microservice prediction first
        List<CognitiveAiService.LevelMetric> metrics = new ArrayList<>();
        for (int i = 0; i < last5.size(); i++) {
            LevelAttempt att = last5.get(i);
            metrics.add(CognitiveAiService.LevelMetric.builder()
                    .level(i + 1)
                    .time_taken_ms((long) att.getTimeTakenSec() * 1000)
                    .tries_count(att.getTriesCount())
                    .total_cards(4 + (i * 2))
                    .idle_hints_triggered(att.getHintsUsed())
                    .perk_hints_used(0)
                    .difficulty(currentDiff <= 3 ? "EASY" : (currentDiff <= 6 ? "NORMAL" : "HARD"))
                    .build());
        }

        User user = progress.getUser();
        CognitiveAiService.PredictionRequest req = CognitiveAiService.PredictionRequest.builder()
                .user_id(user != null ? user.getId().toString() : "anonymous")
                .user_age(user != null && user.getAge() != null ? user.getAge() : 68)
                .game_name(progress.getGame() != null ? progress.getGame().getName() : "MatchTheCard")
                .last_5_levels(metrics)
                .build();

        CognitiveAiService.PredictionResponse aiRes = cognitiveAiService.predictDifficulty(req);

        if (aiRes != null && aiRes.getPredicted_difficulty() != null) {
            String pred = aiRes.getPredicted_difficulty().toUpperCase();
            if (pred.contains("HARD") || pred.contains("INCREASE")) {
                newDiff = Math.min(10, currentDiff + 1);
                decision = "INCREASE";
            } else if (pred.contains("EASY") || pred.contains("DECREASE")) {
                newDiff = Math.max(1, currentDiff - 1);
                decision = "DECREASE";
            } else {
                newDiff = currentDiff;
                decision = "HOLD";
            }
            reasoning = aiRes.getClinical_rationale() != null ? aiRes.getClinical_rationale() :
                    "AI Model evaluated cognitive performance index: " + aiRes.getCognitive_performance_index();
        } else {
            // Deterministic Rule-Based Fallback
            if (avgTime < 30 && avgHints < 1) {
                if (currentDiff < 10) {
                    newDiff = currentDiff + 1;
                    reasoning = String.format("Patient completed tasks quickly (avg %.1fs) with minimal hints. Increasing difficulty safely to stimulate cognitive function.", avgTime);
                    decision = "INCREASE";
                } else {
                    reasoning = "Patient is performing excellently at maximum difficulty. No changes needed.";
                }
            } else if (avgTime > 90 || avgHints >= 2 || avgTries >= 3) {
                if (currentDiff > 1) {
                    newDiff = currentDiff - 1;
                    reasoning = String.format("Patient struggled with recent tasks (avg %.1fs, %.1f hints). Decreasing difficulty to reduce cognitive load.", avgTime, avgHints);
                    decision = "DECREASE";
                } else {
                    reasoning = "Patient is experiencing difficulty, but is already at the lowest safe setting.";
                }
            } else {
                reasoning = "Patient's performance is stable and appropriate for their current cognitive state. Holding current difficulty.";
            }
        }
        
        progress.setCurrentDifficulty(newDiff);
        
        // Prepare JSONB Features for the Database Log
        Map<String, Object> features = new HashMap<>();
        features.put("avg_time_sec", avgTime);
        features.put("avg_hints", avgHints);
        features.put("avg_tries", avgTries);
        features.put("current_difficulty", currentDiff);
        if (aiRes != null && aiRes.getCognitive_performance_index() != null) {
            features.put("cpi", aiRes.getCognitive_performance_index());
            features.put("fatigue_detected", aiRes.getFatigue_detected());
        }
        
        DifficultyLog log = DifficultyLog.builder()
            .user(progress.getUser())
            .game(progress.getGame())
            .inputFeatures(features)
            .aiDecision(decision)
            .difficultyDelta((float) (newDiff - currentDiff))
            .reasoningText(reasoning)
            .build();
            
        return difficultyLogRepository.save(log);
    }
}
