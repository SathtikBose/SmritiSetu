package com.example.SpringBoot_Bakend.service;

import com.example.SpringBoot_Bakend.entities.User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class DailyStreakService {

    public int recordDailyActivity(User user, LocalDate today) {
        String lastDateStr = user.getLastActiveDate();
        int currentStreak = user.getStreakDays() != null ? user.getStreakDays() : 0;
        int newStreak;

        if (lastDateStr == null || lastDateStr.isBlank()) {
            newStreak = 1;
        } else {
            try {
                LocalDate lastDate = LocalDate.parse(lastDateStr);
                long daysDiff = ChronoUnit.DAYS.between(lastDate, today);

                if (daysDiff == 0) {
                    newStreak = Math.max(1, currentStreak);
                } else if (daysDiff == 1) {
                    newStreak = Math.max(0, currentStreak) + 1;
                } else {
                    newStreak = 1;
                }
            } catch (Exception e) {
                newStreak = 1;
            }
        }

        user.setStreakDays(newStreak);
        user.setLastActiveDate(today.toString());
        return newStreak;
    }

    public int getEffectiveStreak(User user, LocalDate today) {
        if (user.getLastActiveDate() == null || user.getLastActiveDate().isBlank()) return 0;
        try {
            LocalDate lastDate = LocalDate.parse(user.getLastActiveDate());
            long daysDiff = ChronoUnit.DAYS.between(lastDate, today);
            return (daysDiff <= 1) ? (user.getStreakDays() != null ? user.getStreakDays() : 0) : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}
