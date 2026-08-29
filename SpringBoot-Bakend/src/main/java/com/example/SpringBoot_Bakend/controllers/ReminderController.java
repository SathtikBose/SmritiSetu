package com.example.SpringBoot_Bakend.controllers;

import com.example.SpringBoot_Bakend.dto.ReminderRequest;
import com.example.SpringBoot_Bakend.dto.ReminderResponse;
import com.example.SpringBoot_Bakend.entities.User;
import com.example.SpringBoot_Bakend.service.ReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/caregiver/patient/{patientId}/reminders")
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;

    @GetMapping
    public ResponseEntity<List<ReminderResponse>> getReminders(
            @AuthenticationPrincipal User caregiver,
            @PathVariable UUID patientId) {
        return ResponseEntity.ok(reminderService.getPatientReminders(caregiver.getId(), patientId));
    }

    @PostMapping
    public ResponseEntity<ReminderResponse> createReminder(
            @AuthenticationPrincipal User caregiver,
            @PathVariable UUID patientId,
            @Valid @RequestBody ReminderRequest request) {
        return ResponseEntity.ok(reminderService.createReminder(caregiver.getId(), patientId, request));
    }

    @PutMapping("/{reminderId}")
    public ResponseEntity<ReminderResponse> updateReminder(@AuthenticationPrincipal User caregiver,
            @PathVariable UUID patientId, @PathVariable UUID reminderId, @Valid @RequestBody ReminderRequest request) {
        return ResponseEntity.ok(reminderService.updateReminder(caregiver.getId(), patientId, reminderId, request));
    }
}
