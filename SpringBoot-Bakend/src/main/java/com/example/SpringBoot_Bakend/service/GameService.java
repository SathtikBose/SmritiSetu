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
    private final UserRepository userRepository;
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

        // Calculate XP (Elderly patients are rewarded with +15 XP and +200 coins per level)
        int xpEarned = Math.max(15 - request.getHintsUsed() * 2, 5);
        int coinsEarned = 200;
        
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
        
        // Update user state directly
        int currentCoins = user.getCoins() != null ? user.getCoins() : 1000;
        int currentTotalXp = user.getTotalXp() != null ? user.getTotalXp() : 1450;
        int currentMonthlyXp = user.getMonthlyLeagueXp() != null ? user.getMonthlyLeagueXp() : 0;
        
        user.setCoins(currentCoins + coinsEarned);
        user.setTotalXp(currentTotalXp + xpEarned);
        user.setMonthlyLeagueXp(currentMonthlyXp + xpEarned);
        
        // Compute League Tier
        int newMonthlyXp = user.getMonthlyLeagueXp();
        String tierName = "Bronze Division";
        if (newMonthlyXp >= 900) {
            tierName = "Diamond Division";
        } else if (newMonthlyXp >= 675) {
            tierName = "Platinum Division";
        } else if (newMonthlyXp >= 450) {
            tierName = "Gold Division";
        } else if (newMonthlyXp >= 225) {
            tierName = "Silver Division";
        }
        user.setLeagueTier(tierName);

        // Progress level counter
        int completedLevel = progress.getCurrentLevel();
        int nextLevel = completedLevel + 1;
        progress.setCurrentLevel(nextLevel);
        progress.setLastPlayed(LocalDateTime.now());
        
        // Update User highest level tracker
        if (game.getGameType() != null && "pattern".equalsIgnoreCase(game.getGameType().name())) {
            if (user.getHighestUnlockedPatternLevel() == null || nextLevel > user.getHighestUnlockedPatternLevel()) {
                user.setHighestUnlockedPatternLevel(nextLevel);
            }
        } else {
            if (user.getHighestUnlockedLevel() == null || nextLevel > user.getHighestUnlockedLevel()) {
                user.setHighestUnlockedLevel(nextLevel);
            }
        }
        userRepository.save(user);

        // Update LeagueStatus entity for backward compatibility
        LeagueStatus league = leagueRepository.findByUserId(user.getId())
                .orElseGet(() -> leagueRepository.save(LeagueStatus.builder().user(user).build()));
        league.setTotalXp(user.getTotalXp());
        league.setCurrentLeague(tierName);
        leagueRepository.save(league);
        
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
                .newLeague(tierName)
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
