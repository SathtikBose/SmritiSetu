package com.example.SpringBoot_Bakend.service;

import com.example.SpringBoot_Bakend.dto.ContentResponse;
import com.example.SpringBoot_Bakend.dto.LevelStartResponse;
import com.example.SpringBoot_Bakend.entities.Game;
import com.example.SpringBoot_Bakend.entities.GameProgress;
import com.example.SpringBoot_Bakend.entities.User;
import com.example.SpringBoot_Bakend.repository.GameProgressRepository;
import com.example.SpringBoot_Bakend.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service @RequiredArgsConstructor
public class GameContentService {
    private final GameRepository gameRepository;
    private final GameProgressRepository progressRepository;

    public LevelStartResponse startLevel(User user, UUID gameId, Integer requestedLevel) {
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new IllegalArgumentException("Game not found."));
        GameProgress progress = progressRepository.findByUserIdAndGameId(user.getId(), gameId)
                .orElseGet(() -> progressRepository.save(GameProgress.builder().user(user).game(game).build()));
        if (requestedLevel != null && !requestedLevel.equals(progress.getCurrentLevel())) throw new IllegalArgumentException("Start the current unlocked level only.");
        int count = Math.min(8, 2 + progress.getCurrentDifficulty());
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("itemCount", count);
        config.put("difficulty", progress.getCurrentDifficulty());
        config.put("timeLimitSeconds", 0); // no pressure timers for elderly users
        config.put("audioPrompt", localizedPrompt(user.getPreferredLanguage(), game.getType()));
        return LevelStartResponse.builder().gameId(gameId).gameType(game.getType()).level(progress.getCurrentLevel())
                .difficulty(progress.getCurrentDifficulty()).language(user.getPreferredLanguage()).levelConfig(config).build();
    }

    public ContentResponse content(String language, String gameId) {
        if (!language.equals("en") && !language.equals("hi") && !language.equals("as")) throw new IllegalArgumentException("Supported languages are en, hi and as.");
        Game game = gameRepository.findById(UUID.fromString(gameId)).orElseThrow(() -> new IllegalArgumentException("Game not found."));
        return ContentResponse.builder().language(language).gameId(gameId).content(Map.of("prompt", localizedPrompt(language, game.getType()), "type", game.getType())).build();
    }

    private String localizedPrompt(String language, String type) {
        Map<String, String> english = Map.of("memory_match", "Find the matching familiar pictures.", "sequence_recall", "Watch carefully, then repeat the sequence.", "daily_reasoning", "Choose what comes next in this everyday routine.");
        Map<String, String> hindi = Map.of("memory_match", "मिलते-जुलते परिचित चित्र चुनें।", "sequence_recall", "ध्यान से देखें, फिर क्रम दोहराएँ।", "daily_reasoning", "रोज़मर्रा की दिनचर्या का अगला कदम चुनें।");
        Map<String, String> assamese = Map.of("memory_match", "একে ধৰণৰ চিনাকি ছবিবোৰ বিচাৰক।", "sequence_recall", "মনোযোগে চাওক, তাৰ পিছত ক্ৰমটো পুনৰ কৰক।", "daily_reasoning", "দৈনন্দিন কামৰ পৰৱৰ্তী পদক্ষেপ বাছনি কৰক।");
        return (language.equals("hi") ? hindi : language.equals("as") ? assamese : english).getOrDefault(type, "Let us play together.");
    }
}
