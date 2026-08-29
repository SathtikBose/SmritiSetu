package com.example.SpringBoot_Bakend.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "league_status")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeagueStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Builder.Default
    @Column(name = "current_league", nullable = false)
    private String currentLeague = "BRONZE"; // BRONZE, SILVER, GOLD, PLATINUM

    @Builder.Default
    @Column(name = "total_xp", nullable = false)
    private Integer totalXp = 0;
}
