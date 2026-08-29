package com.example.SpringBoot_Bakend.repository;

import com.example.SpringBoot_Bakend.entities.LevelAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LevelAttemptRepository extends JpaRepository<LevelAttempt, UUID> {
    List<LevelAttempt> findTop10ByProgressIdOrderByPlayedAtDesc(UUID progressId);
    long countByProgressId(UUID progressId);
}
