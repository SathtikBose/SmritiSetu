package com.example.SpringBoot_Bakend.controllers;

import com.example.SpringBoot_Bakend.dto.LevelAttemptRequest;
import com.example.SpringBoot_Bakend.dto.LevelAttemptResponse;
import com.example.SpringBoot_Bakend.dto.GameResponse;
import com.example.SpringBoot_Bakend.entities.User;
import com.example.SpringBoot_Bakend.repository.GameRepository;
import com.example.SpringBoot_Bakend.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
public class GameController {
    
    private final GameService gameService;
    private final GameRepository gameRepository;

    @GetMapping({"/game", "/api/v1/games"})
    public ResponseEntity<java.util.List<GameResponse>> games() {
        return ResponseEntity.ok(gameRepository.findAll().stream().map(game -> GameResponse.builder()
                .id(game.getId()).name(game.getName()).type(game.getType()).build()).toList());
    }

    @PostMapping({"/game/level/complete", "/api/v1/game/level/attempt"})
    public ResponseEntity<LevelAttemptResponse> completeLevel(
            @AuthenticationPrincipal User user,
            @RequestBody LevelAttemptRequest request) {
            
        LevelAttemptResponse response = gameService.processLevelAttempt(user, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/v1/games/match-card/level-complete")
    public ResponseEntity<LevelAttemptResponse> completeMatchCardLevel(
            @AuthenticationPrincipal User user,
            @RequestBody LevelAttemptRequest request) {
        if (request.getGameName() == null) {
            request.setGameName("MatchTheCard");
        }
        LevelAttemptResponse response = gameService.processLevelAttempt(user, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/v1/games/pattern/level-complete")
    public ResponseEntity<LevelAttemptResponse> completePatternLevel(
            @AuthenticationPrincipal User user,
            @RequestBody LevelAttemptRequest request) {
        if (request.getGameName() == null) {
            request.setGameName("PatternMatching");
        }
        LevelAttemptResponse response = gameService.processLevelAttempt(user, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping({"/game/level/complete-bulk", "/api/v1/game/level/complete-bulk"})
    public ResponseEntity<java.util.List<LevelAttemptResponse>> completeLevelBulk(
            @AuthenticationPrincipal User user,
            @RequestBody java.util.List<LevelAttemptRequest> requests) {
            
        java.util.List<LevelAttemptResponse> responses = gameService.processBulkLevelAttempts(user, requests);
        return ResponseEntity.ok(responses);
    }
}
