package com.example.SpringBoot_Bakend.controllers;

import com.example.SpringBoot_Bakend.dto.ContentResponse;
import com.example.SpringBoot_Bakend.dto.LevelStartResponse;
import com.example.SpringBoot_Bakend.entities.User;
import com.example.SpringBoot_Bakend.service.GameContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController @RequiredArgsConstructor
public class ContentController {
    private final GameContentService gameContentService;
    @GetMapping("/game/{gameId}/level/{level}/start")
    public ResponseEntity<LevelStartResponse> start(@AuthenticationPrincipal User user, @PathVariable UUID gameId, @PathVariable Integer level) {
        return ResponseEntity.ok(gameContentService.startLevel(user, gameId, level));
    }
    @GetMapping("/content/{lang}/{gameId}")
    public ResponseEntity<ContentResponse> content(@PathVariable String lang, @PathVariable String gameId) { return ResponseEntity.ok(gameContentService.content(lang, gameId)); }
}
