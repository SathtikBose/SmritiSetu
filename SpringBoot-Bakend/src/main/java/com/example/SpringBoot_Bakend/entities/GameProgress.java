package com.example.SpringBoot_Bakend.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "game_progress")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Builder.Default
    @Column(name = "current_level", nullable = false)
    private Integer currentLevel = 1;

    @Builder.Default
    @Column(name = "current_difficulty", nullable = false)
    private Integer currentDifficulty = 1;

    @Column(name = "last_played")
    private LocalDateTime lastPlayed;
}
