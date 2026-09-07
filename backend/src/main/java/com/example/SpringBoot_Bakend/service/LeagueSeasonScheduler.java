package com.example.SpringBoot_Bakend.service;

import com.example.SpringBoot_Bakend.entities.User;
import com.example.SpringBoot_Bakend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class LeagueSeasonScheduler {

    private final UserRepository userRepository;

    // Triggered at 00:00:00 on the 1st day of every month
    @Scheduled(cron = "0 0 0 1 * ?")
    @Transactional
    public void executeMonthlySeasonReset() {
        String currentMonthKey = YearMonth.now().toString(); // e.g. "2026-10"
        log.info("Executing monthly league season reset for: {}", currentMonthKey);

        List<User> users = userRepository.findAll();
        for (User user : users) {
            user.setMonthlyLeagueXp(0);
            user.setLeagueTier("Bronze Division");
            user.setLastSeasonResetMonth(currentMonthKey);
        }
        userRepository.saveAll(users);
        log.info("Successfully reset monthly league progress for {} users.", users.size());
    }
}
