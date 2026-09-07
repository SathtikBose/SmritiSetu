package com.example.SpringBoot_Bakend.repository;

import com.example.SpringBoot_Bakend.entities.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, UUID> {
    List<Reminder> findAllByUserId(UUID userId);
}
