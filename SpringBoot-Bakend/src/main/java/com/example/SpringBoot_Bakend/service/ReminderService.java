package com.example.SpringBoot_Bakend.service;

import com.example.SpringBoot_Bakend.dto.ReminderRequest;
import com.example.SpringBoot_Bakend.dto.ReminderResponse;
import com.example.SpringBoot_Bakend.entities.Reminder;
import com.example.SpringBoot_Bakend.entities.User;
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
    private static final Set<String> REMINDER_TYPES = Set.of("medicine", "hydration", "activity", "appointment");
    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;
    private final CaregiverService caregiverService;

    public List<ReminderResponse> getPatientReminders(UUID caregiverId, UUID patientId) {
        caregiverService.verifyLink(caregiverId, patientId);
        return reminderRepository.findAllByUserId(patientId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ReminderResponse createReminder(UUID caregiverId, UUID patientId, ReminderRequest request) {
        caregiverService.verifyLink(caregiverId, patientId);
        validateType(request.getType());
        User patient = userRepository.findById(patientId).orElseThrow();

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
        caregiverService.verifyLink(caregiverId, patientId);
        validateType(request.getType());
        Reminder reminder = reminderRepository.findById(reminderId)
                .filter(value -> value.getUser().getId().equals(patientId))
                .orElseThrow(() -> new IllegalArgumentException("Reminder not found for this patient."));
        reminder.setType(request.getType());
        reminder.setScheduledTime(request.getScheduledTime());
        reminder.setMessage(request.getMessage());
        if (request.getActive() != null) reminder.setActive(request.getActive());
        return mapToResponse(reminderRepository.save(reminder));
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
        if (!REMINDER_TYPES.contains(type.toLowerCase())) throw new IllegalArgumentException("Reminder type must be medicine, hydration, activity or appointment.");
    }
}
