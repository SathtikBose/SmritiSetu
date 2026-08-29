package com.example.SpringBoot_Bakend.service;

import com.example.SpringBoot_Bakend.dto.LevelAttemptRequest;
import com.example.SpringBoot_Bakend.dto.LevelAttemptResponse;
import com.example.SpringBoot_Bakend.entities.*;
import com.example.SpringBoot_Bakend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GameService {
    private final GameRepository gameRepository;
    private final GameProgressRepository progressRepository;
    private final LevelAttemptRepository attemptRepository;
    private final LeagueStatusRepository leagueRepository;
    private final AIDifficultyService aiDifficultyService;

    @Transactional
    public LevelAttemptResponse processLevelAttempt(User user, LevelAttemptRequest request) {
        Game game = gameRepository.findById(request.getGameId())
                .orElseThrow(() -> new RuntimeException("Game not found!"));
        
        // Find or create progress profile for this specific game
        GameProgress progress = progressRepository.findByUserIdAndGameId(user.getId(), game.getId())
                .orElseGet(() -> progressRepository.save(GameProgress.builder()
                        .user(user)
                        .game(game)
                        .currentLevel(1)
                        .currentDifficulty(1)
                        .build()));

        // Calculate XP (Elderly patients are never penalized, they just gain less XP if they use hints)
        int xpEarned = Math.max(10 - request.getHintsUsed() * 2, 2);
        
        // Save the telemetry attempt
        LevelAttempt attempt = LevelAttempt.builder()
                .progress(progress)
                .timeTakenSec(request.getTimeTakenSec())
                .hintsUsed(request.getHintsUsed())
                .triesCount(request.getTriesCount())
                .xpEarned(xpEarned)
                .playedAt(LocalDateTime.now())
                .syncedOffline(request.getSyncedOffline() != null ? request.getSyncedOffline() : false)
                .build();
        attemptRepository.save(attempt);
        
        // Update League / Gamification
        LeagueStatus league = leagueRepository.findByUserId(user.getId())
                .orElseGet(() -> leagueRepository.save(LeagueStatus.builder().user(user).build()));
        
        league.setTotalXp(league.getTotalXp() + xpEarned);
        
        // Simple League Promotions
        if (league.getTotalXp() > 500 && league.getCurrentLeague().equals("BRONZE")) {
            league.setCurrentLeague("SILVER");
        } else if (league.getTotalXp() > 1500 && league.getCurrentLeague().equals("SILVER")) {
            league.setCurrentLeague("GOLD");
        }
        leagueRepository.save(league);
        
        // Progress level counter
        int completedLevel = progress.getCurrentLevel();
        progress.setCurrentLevel(completedLevel + 1);
        progress.setLastPlayed(LocalDateTime.now());
        
        // Every 5th level, run the Rule-Based AI Difficulty Engine
        String aiReasoning = null;
        if (completedLevel % 5 == 0) {
            DifficultyLog log = aiDifficultyService.evaluateAndAdjustDifficulty(progress);
            if (log != null) aiReasoning = log.getReasoningText();
        }
        
        progressRepository.save(progress);
        
        return LevelAttemptResponse.builder()
                .xpEarned(xpEarned)
                .newDifficulty(progress.getCurrentDifficulty())
                .nextLevel(progress.getCurrentLevel())
                .newLeague(league.getCurrentLeague())
                .aiReasoningMessage(aiReasoning)
                .build();
    }

    @Transactional
    public java.util.List<LevelAttemptResponse> processBulkLevelAttempts(User user, java.util.List<LevelAttemptRequest> requests) {
        return requests.stream()
                .map(req -> processLevelAttempt(user, req))
                .collect(java.util.stream.Collectors.toList());
    }
}
