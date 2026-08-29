package com.example.SpringBoot_Bakend.repository;

import com.example.SpringBoot_Bakend.entities.GameProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GameProgressRepository extends JpaRepository<GameProgress, UUID> {
    Optional<GameProgress> findByUserIdAndGameId(UUID userId, UUID gameId);
    List<GameProgress> findAllByUserId(UUID userId);
}
