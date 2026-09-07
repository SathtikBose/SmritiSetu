package com.example.SpringBoot_Bakend.service;

import com.example.SpringBoot_Bakend.dto.LevelAttemptRequest;
import com.example.SpringBoot_Bakend.dto.LevelAttemptResponse;
import com.example.SpringBoot_Bakend.entities.*;
import com.example.SpringBoot_Bakend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
    private final DailyStreakService dailyStreakService;

    @Transactional
    public LevelAttemptResponse processLevelAttempt(User user, LevelAttemptRequest request) {
        // Resolve Game by ID or fallback by name/type
        Game game = null;
        if (request.getGameId() != null) {
            game = gameRepository.findById(request.getGameId()).orElse(null);
        }
        if (game == null) {
            String name = request.getGameName() != null ? request.getGameName() : "MatchTheCard";
            game = gameRepository.findByName(name).orElseGet(() -> {
                Game g = Game.builder()
                        .name(name)
                        .type("COGNITIVE")
                        .build();
                return gameRepository.save(g);
            });
        }
        final Game targetGame = game;
        
        // Find or create progress profile for this specific game
        GameProgress progress = progressRepository.findByUserIdAndGameId(user.getId(), targetGame.getId())
                .orElseGet(() -> progressRepository.save(GameProgress.builder()
                        .user(user)
                        .game(targetGame)
                        .currentLevel(request.getLevel() != null ? request.getLevel() : 1)
                        .currentDifficulty(1)
                        .build()));

        int hintsUsed = request.getEffectiveHintsUsed();
        int timeTaken = request.getEffectiveTimeTakenSec();
        int tries = request.getEffectiveTriesCount();

        // Calculate XP (Elderly patients earn +15 XP and +200 coins per level)
        int xpEarned = Math.max(15 - hintsUsed * 2, 5);
        int coinsEarned = 200;
        
        // Save the telemetry attempt
        LevelAttempt attempt = LevelAttempt.builder()
                .progress(progress)
                .timeTakenSec(timeTaken)
                .hintsUsed(hintsUsed)
                .triesCount(tries)
                .xpEarned(xpEarned)
                .playedAt(LocalDateTime.now())
                .syncedOffline(request.getSyncedOffline() != null ? request.getSyncedOffline() : false)
                .build();
        attemptRepository.save(attempt);
        
        // Update user streak & rewards
        dailyStreakService.recordDailyActivity(user, LocalDate.now());

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
        int completedLevel = request.getLevel() != null ? request.getLevel() : progress.getCurrentLevel();
        int nextLevel = completedLevel + 1;
        progress.setCurrentLevel(Math.max(progress.getCurrentLevel(), nextLevel));
        progress.setLastPlayed(LocalDateTime.now());
        
        // Update User highest level tracker
        boolean isPattern = (game.getName() != null && game.getName().toLowerCase().contains("pattern"))
                || (request.getPatternLength() != null)
                || (request.getGameName() != null && request.getGameName().toLowerCase().contains("pattern"));

        if (isPattern) {
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
        
        // Every 5th level, run AI Difficulty Engine
        String aiReasoning = null;
        String predictedDifficulty = "NORMAL";
        if (completedLevel % 5 == 0) {
            DifficultyLog log = aiDifficultyService.evaluateAndAdjustDifficulty(progress);
            if (log != null) {
                aiReasoning = log.getReasoningText();
                predictedDifficulty = log.getAiDecision();
            }
        }
        
        progressRepository.save(progress);
        
        return LevelAttemptResponse.builder()
                .passed(true)
                .xpEarned(xpEarned)
                .coinsEarned(coinsEarned)
                .totalCoins(user.getCoins())
                .totalXp(user.getTotalXp())
                .monthlyLeagueXp(user.getMonthlyLeagueXp())
                .leagueTier(tierName)
                .newLeague(tierName)
                .highestUnlockedLevel(user.getHighestUnlockedLevel())
                .highestUnlockedPatternLevel(user.getHighestUnlockedPatternLevel())
                .nextUnlockedLevel(isPattern ? user.getHighestUnlockedPatternLevel() : user.getHighestUnlockedLevel())
                .nextLevel(nextLevel)
                .newDifficulty(progress.getCurrentDifficulty())
                .predictedDifficulty(predictedDifficulty)
                .difficultyReasoning(aiReasoning)
                .aiReasoningMessage(aiReasoning)
                .streakDays(user.getStreakDays())
                .build();
    }

    @Transactional
    public java.util.List<LevelAttemptResponse> processBulkLevelAttempts(User user, java.util.List<LevelAttemptRequest> requests) {
        return requests.stream()
                .map(req -> processLevelAttempt(user, req))
                .collect(java.util.stream.Collectors.toList());
    }
}
