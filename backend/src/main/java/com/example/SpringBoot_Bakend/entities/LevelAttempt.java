package com.example.SpringBoot_Bakend.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "level_attempts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LevelAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "progress_id", nullable = false)
    private GameProgress progress;

    @Column(name = "time_taken_sec", nullable = false)
    private Integer timeTakenSec;

    @Column(name = "hints_used", nullable = false)
    private Integer hintsUsed;

    @Column(name = "tries_count", nullable = false)
    private Integer triesCount;

    @Column(name = "xp_earned", nullable = false)
    private Integer xpEarned;

    @Column(name = "played_at", nullable = false)
    private LocalDateTime playedAt;

    @Builder.Default
    @Column(name = "synced_offline", nullable = false)
    private Boolean syncedOffline = false;
}
