package com.example.SpringBoot_Bakend.repository;

import com.example.SpringBoot_Bakend.entities.DifficultyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DifficultyLogRepository extends JpaRepository<DifficultyLog, UUID> {
    List<DifficultyLog> findAllByUserIdOrderByCreatedAtDesc(UUID userId);
}
