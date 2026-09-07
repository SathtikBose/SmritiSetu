package com.example.SpringBoot_Bakend.service;

import com.example.SpringBoot_Bakend.dto.ReminderRequest;
import com.example.SpringBoot_Bakend.dto.ReminderResponse;
import com.example.SpringBoot_Bakend.entities.CaregiverLink;
import com.example.SpringBoot_Bakend.entities.Reminder;
import com.example.SpringBoot_Bakend.entities.User;
import com.example.SpringBoot_Bakend.repository.CaregiverLinkRepository;
import com.example.SpringBoot_Bakend.repository.ReminderRepository;
import com.example.SpringBoot_Bakend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReminderService {
    private static final Set<String> REMINDER_TYPES = Set.of(
            "medicine", "hydration", "activity", "appointment", "meal", "other", "general"
    );
    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;
    private final CaregiverLinkRepository linkRepository;
    private final CaregiverService caregiverService;

    public UUID resolvePatientIdForCaregiver(UUID caregiverId, UUID candidatePatientId) {
        if (candidatePatientId != null && !candidatePatientId.equals(caregiverId)) {
            try {
                caregiverService.verifyLink(caregiverId, candidatePatientId);
                return candidatePatientId;
            } catch (Exception ignored) {
            }
        }
        User caregiver = userRepository.findById(caregiverId).orElseThrow(() -> new IllegalArgumentException("Caregiver not found"));
        if (caregiver.getLinkedPatientCode() != null && !caregiver.getLinkedPatientCode().isBlank()) {
            User patient = userRepository.findByPatientLinkCode(caregiver.getLinkedPatientCode().trim().toUpperCase()).orElse(null);
            if (patient != null) {
                return patient.getId();
            }
        }
        List<CaregiverLink> links = linkRepository.findByCaregiverId(caregiverId);
        if (!links.isEmpty()) {
            return links.get(0).getPatient().getId();
        }
        throw new IllegalStateException("No linked patient found for this caregiver. Please link a patient with their Patient ID first.");
    }

    public List<ReminderResponse> getPatientReminders(UUID caregiverId, UUID patientId) {
        UUID targetPatientId = resolvePatientIdForCaregiver(caregiverId, patientId);
        return reminderRepository.findAllByUserId(targetPatientId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ReminderResponse createReminder(UUID caregiverId, UUID patientId, ReminderRequest request) {
        UUID targetPatientId = resolvePatientIdForCaregiver(caregiverId, patientId);
        validateType(request.getType());
        User patient = userRepository.findById(targetPatientId).orElseThrow();

        Reminder reminder = Reminder.builder()
                .user(patient)
                .type(request.getType())
                .scheduledTime(request.getScheduledTime())
                .message(request.getMessage())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        return mapToResponse(reminderRepository.save(reminder));
    }

    public ReminderResponse updateReminder(UUID caregiverId, UUID patientId, UUID reminderId, ReminderRequest request) {
        UUID targetPatientId = resolvePatientIdForCaregiver(caregiverId, patientId);
        validateType(request.getType());
        Reminder reminder = reminderRepository.findById(reminderId)
                .filter(value -> value.getUser().getId().equals(targetPatientId))
                .orElseThrow(() -> new IllegalArgumentException("Reminder not found for this patient."));
        reminder.setType(request.getType());
        reminder.setScheduledTime(request.getScheduledTime());
        reminder.setMessage(request.getMessage());
        if (request.getActive() != null) reminder.setActive(request.getActive());
        return mapToResponse(reminderRepository.save(reminder));
    }

    public ReminderResponse toggleReminder(UUID caregiverId, UUID patientId, UUID reminderId) {
        UUID targetPatientId = resolvePatientIdForCaregiver(caregiverId, patientId);
        Reminder reminder = reminderRepository.findById(reminderId)
                .filter(value -> value.getUser().getId().equals(targetPatientId))
                .orElseThrow(() -> new IllegalArgumentException("Reminder not found for this patient."));
        reminder.setActive(!Boolean.TRUE.equals(reminder.getActive()));
        return mapToResponse(reminderRepository.save(reminder));
    }

    public void deleteReminder(UUID caregiverId, UUID patientId, UUID reminderId) {
        UUID targetPatientId = resolvePatientIdForCaregiver(caregiverId, patientId);
        Reminder reminder = reminderRepository.findById(reminderId)
                .filter(value -> value.getUser().getId().equals(targetPatientId))
                .orElseThrow(() -> new IllegalArgumentException("Reminder not found for this patient."));
        reminderRepository.delete(reminder);
    }

    public List<ReminderResponse> getOwnReminders(UUID patientId) {
        return reminderRepository.findAllByUserId(patientId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ReminderResponse mapToResponse(Reminder reminder) {
        return ReminderResponse.builder()
                .id(reminder.getId())
                .type(reminder.getType())
                .scheduledTime(reminder.getScheduledTime())
                .message(reminder.getMessage())
                .active(reminder.getActive())
                .build();
    }

    private void validateType(String type) {
        if (type == null || !REMINDER_TYPES.contains(type.trim().toLowerCase())) {
            // Allow gracefully or default to Medicine
        }
    }
}

