package com.example.SpringBoot_Bakend.controllers;

import com.example.SpringBoot_Bakend.entities.User;
import com.example.SpringBoot_Bakend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/shop")
@RequiredArgsConstructor
public class ShopController {

    private final UserRepository userRepository;

    @GetMapping("/items")
    public ResponseEntity<List<Map<String, Object>>> getShopItems() {
        return ResponseEntity.ok(List.of(
                Map.of(
                        "id", "PERK_HINT",
                        "name", "Extra Hint",
                        "costCoins", 1000,
                        "description", "Auto-highlights unmatched card pair or gives pattern sequence guidance"
                ),
                Map.of(
                        "id", "PERK_PEEK",
                        "name", "Peek (Show Again)",
                        "costCoins", 800,
                        "description", "Re-reveals hidden cards for 4 seconds"
                ),
                Map.of(
                        "id", "PERK_SKIP",
                        "name", "Skip Level",
                        "costCoins", 2000,
                        "description", "Instantly completes level and awards full XP & coins"
                )
        ));
    }

    @PostMapping("/buy")
    public ResponseEntity<?> buyPerk(@AuthenticationPrincipal User user, @RequestBody Map<String, String> request) {
        String perkType = request.get("perkType");
        if (perkType == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "perkType is required"));
        }

        int cost;
        switch (perkType.toUpperCase()) {
            case "HINT":
            case "PERK_HINT":
                cost = 1000;
                if (user.getCoins() < cost) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Insufficient coins"));
                }
                user.setCoins(user.getCoins() - cost);
                user.setHintsCount(user.getHintsCount() + 1);
                break;

            case "PEEK":
            case "SHOW_AGAIN":
            case "PERK_PEEK":
                cost = 800;
                if (user.getCoins() < cost) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Insufficient coins"));
                }
                user.setCoins(user.getCoins() - cost);
                user.setShowAgainCount(user.getShowAgainCount() + 1);
                break;

            case "SKIP":
            case "SKIP_LEVEL":
            case "PERK_SKIP":
                cost = 2000;
                if (user.getCoins() < cost) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Insufficient coins"));
                }
                user.setCoins(user.getCoins() - cost);
                user.setSkipLevelCount(user.getSkipLevelCount() + 1);
                break;

            default:
                return ResponseEntity.badRequest().body(Map.of("error", "Unknown perk type: " + perkType));
        }

        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "remainingCoins", user.getCoins(),
                "hintsCount", user.getHintsCount(),
                "showAgainCount", user.getShowAgainCount(),
                "skipLevelCount", user.getSkipLevelCount()
        ));
    }
}
