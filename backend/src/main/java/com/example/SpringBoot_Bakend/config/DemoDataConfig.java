package com.example.SpringBoot_Bakend.config;

import com.example.SpringBoot_Bakend.entities.Game;
import com.example.SpringBoot_Bakend.repository.GameRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DemoDataConfig {
    @Bean CommandLineRunner seedDemoGames(GameRepository games) {
        return args -> { seed(games, "Memory Match", "memory_match"); seed(games, "Sequence Recall", "sequence_recall"); seed(games, "Daily Reasoning", "daily_reasoning"); };
    }
    private void seed(GameRepository games, String name, String type) { if (games.findByName(name).isEmpty()) games.save(Game.builder().name(name).type(type).build()); }
}
