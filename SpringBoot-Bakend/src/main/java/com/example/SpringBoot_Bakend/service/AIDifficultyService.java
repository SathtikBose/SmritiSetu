package com.example.SpringBoot_Bakend.service;

import com.example.SpringBoot_Bakend.entities.*;
import com.example.SpringBoot_Bakend.repository.DifficultyLogRepository;
import com.example.SpringBoot_Bakend.repository.LevelAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AIDifficultyService {
    
    private final DifficultyLogRepository difficultyLogRepository;
    private final LevelAttemptRepository levelAttemptRepository;

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
        
        // Rule-Based Logic (as requested by PRD Phase 1 for bounded safety)
        if (avgTime < 30 && avgHints < 1) {
            if (currentDiff < 10) { // Max difficulty cap
                newDiff = currentDiff + 1;
                reasoning = String.format("Patient completed tasks very quickly (avg %.1fs) with minimal hints. Increasing difficulty safely to stimulate cognitive function.", avgTime);
                decision = "INCREASE";
            } else {
                reasoning = "Patient is performing excellently at maximum difficulty. No changes needed.";
            }
        } else if (avgTime > 90 || avgHints >= 2 || avgTries >= 3) {
            if (currentDiff > 1) { // Min difficulty cap
                newDiff = currentDiff - 1;
                reasoning = String.format("Patient struggled with recent tasks (avg %.1fs, %.1f hints). Decreasing difficulty boundedly to reduce cognitive load and prevent anxiety.", avgTime, avgHints);
                decision = "DECREASE";
            } else {
                reasoning = "Patient is experiencing difficulty, but is already at the lowest safe setting.";
            }
        } else {
            reasoning = "Patient's performance is stable and appropriate for their current cognitive state. Holding current difficulty.";
        }
        
        progress.setCurrentDifficulty(newDiff);
        
        // Prepare JSONB Features for the Database Log
        Map<String, Object> features = new HashMap<>();
        features.put("avg_time_sec", avgTime);
        features.put("avg_hints", avgHints);
        features.put("avg_tries", avgTries);
        features.put("current_difficulty", currentDiff);
        
        DifficultyLog log = DifficultyLog.builder()
            .user(progress.getUser())
            .game(progress.getGame())
            .inputFeatures(features) // Seamlessly mapped to JSONB!
            .aiDecision(decision)
            .difficultyDelta((float) (newDiff - currentDiff))
            .reasoningText(reasoning)
            .build();
            
        return difficultyLogRepository.save(log);
    }
}
