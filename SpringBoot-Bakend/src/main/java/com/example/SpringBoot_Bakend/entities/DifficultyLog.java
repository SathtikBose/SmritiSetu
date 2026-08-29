package com.example.SpringBoot_Bakend.entities;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "difficulty_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DifficultyLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Type(JsonType.class)
    @Column(name = "input_features", columnDefinition = "jsonb")
    private Map<String, Object> inputFeatures;

    @Column(name = "ai_decision")
    private String aiDecision; // "INCREASE", "DECREASE", "HOLD"

    @Column(name = "difficulty_delta")
    private Float difficultyDelta;

    @Column(name = "reasoning_text", columnDefinition = "TEXT")
    private String reasoningText;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
