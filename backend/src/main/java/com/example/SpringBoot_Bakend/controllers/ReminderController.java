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
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;

    @GetMapping("/caregiver/patient/{patientId}/reminders")
    public ResponseEntity<List<ReminderResponse>> getReminders(
            @AuthenticationPrincipal User caregiver,
            @PathVariable UUID patientId) {
        return ResponseEntity.ok(reminderService.getPatientReminders(caregiver.getId(), patientId));
    }

    @PostMapping("/caregiver/patient/{patientId}/reminders")
    public ResponseEntity<ReminderResponse> createReminder(
            @AuthenticationPrincipal User caregiver,
            @PathVariable UUID patientId,
            @Valid @RequestBody ReminderRequest request) {
        return ResponseEntity.ok(reminderService.createReminder(caregiver.getId(), patientId, request));
    }

    @PutMapping("/caregiver/patient/{patientId}/reminders/{reminderId}")
    public ResponseEntity<ReminderResponse> updateReminder(
            @AuthenticationPrincipal User caregiver,
            @PathVariable UUID patientId,
            @PathVariable UUID reminderId,
            @Valid @RequestBody ReminderRequest request) {
        return ResponseEntity.ok(reminderService.updateReminder(caregiver.getId(), patientId, reminderId, request));
    }

    @PatchMapping("/caregiver/patient/{patientId}/reminders/{reminderId}/toggle")
    public ResponseEntity<ReminderResponse> toggleReminder(
            @AuthenticationPrincipal User caregiver,
            @PathVariable UUID patientId,
            @PathVariable UUID reminderId) {
        return ResponseEntity.ok(reminderService.toggleReminder(caregiver.getId(), patientId, reminderId));
    }

    @DeleteMapping("/caregiver/patient/{patientId}/reminders/{reminderId}")
    public ResponseEntity<Void> deleteReminder(
            @AuthenticationPrincipal User caregiver,
            @PathVariable UUID patientId,
            @PathVariable UUID reminderId) {
        reminderService.deleteReminder(caregiver.getId(), patientId, reminderId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping({"/user/reminders", "/patient/reminders"})
    public ResponseEntity<List<ReminderResponse>> getOwnReminders(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(reminderService.getOwnReminders(user.getId()));
    }
}
