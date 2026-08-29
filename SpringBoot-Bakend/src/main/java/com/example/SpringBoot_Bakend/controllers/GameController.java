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
@RequestMapping("/game")
@RequiredArgsConstructor
public class GameController {
    
    private final GameService gameService;
    private final GameRepository gameRepository;

    @GetMapping
    public ResponseEntity<java.util.List<GameResponse>> games() {
        return ResponseEntity.ok(gameRepository.findAll().stream().map(game -> GameResponse.builder()
                .id(game.getId()).name(game.getName()).type(game.getType()).build()).toList());
    }

    @PostMapping("/level/complete")
    public ResponseEntity<LevelAttemptResponse> completeLevel(
            @AuthenticationPrincipal User user, // Automatically injected from the validated JWT token!
            @Valid @RequestBody LevelAttemptRequest request) {
            
        LevelAttemptResponse response = gameService.processLevelAttempt(user, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/level/complete-bulk")
    public ResponseEntity<java.util.List<LevelAttemptResponse>> completeLevelBulk(
            @AuthenticationPrincipal User user,
            @RequestBody java.util.List<@Valid LevelAttemptRequest> requests) {
            
        java.util.List<LevelAttemptResponse> responses = gameService.processBulkLevelAttempts(user, requests);
        return ResponseEntity.ok(responses);
    }
}
