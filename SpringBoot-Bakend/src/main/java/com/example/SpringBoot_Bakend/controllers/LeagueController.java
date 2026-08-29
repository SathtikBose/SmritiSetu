package com.example.SpringBoot_Bakend.controllers;

import com.example.SpringBoot_Bakend.dto.LeagueStatusResponse;
import com.example.SpringBoot_Bakend.entities.LeagueStatus;
import com.example.SpringBoot_Bakend.entities.User;
import com.example.SpringBoot_Bakend.repository.LeagueStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/league") @RequiredArgsConstructor
public class LeagueController {
    private final LeagueStatusRepository repository;
    @GetMapping("/status")
    public ResponseEntity<LeagueStatusResponse> status(@AuthenticationPrincipal User user) {
        LeagueStatus status = repository.findByUserId(user.getId()).orElse(LeagueStatus.builder().user(user).build());
        int next = switch (status.getCurrentLeague()) { case "BRONZE" -> 501; case "SILVER" -> 1501; case "GOLD" -> 3001; default -> status.getTotalXp(); };
        return ResponseEntity.ok(LeagueStatusResponse.builder().currentLeague(status.getCurrentLeague()).totalXp(status.getTotalXp()).xpToNextLeague(Math.max(0, next - status.getTotalXp())).build());
    }
}
