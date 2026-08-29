package com.example.SpringBoot_Bakend.repository;

import com.example.SpringBoot_Bakend.entities.LeagueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeagueStatusRepository extends JpaRepository<LeagueStatus, UUID> {
    Optional<LeagueStatus> findByUserId(UUID userId);
}
