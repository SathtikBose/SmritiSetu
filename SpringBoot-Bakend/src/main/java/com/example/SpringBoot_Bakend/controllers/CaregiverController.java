package com.example.SpringBoot_Bakend.controllers;

import com.example.SpringBoot_Bakend.dto.DifficultyLogResponse;
import com.example.SpringBoot_Bakend.dto.PatientProgressResponse;
import com.example.SpringBoot_Bakend.entities.User;
import com.example.SpringBoot_Bakend.service.CaregiverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/caregiver")
@RequiredArgsConstructor
public class CaregiverController {

    private final CaregiverService caregiverService;

    @PostMapping("/patient/{patientId}/link")
    public ResponseEntity<Void> linkPatient(@AuthenticationPrincipal User caregiver, @PathVariable UUID patientId) {
        caregiverService.linkPatient(caregiver.getId(), patientId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/patient/{patientId}/progress")
    public ResponseEntity<PatientProgressResponse> getPatientProgress(
            @AuthenticationPrincipal User caregiver,
            @PathVariable UUID patientId) {
        
        // This endpoint requires ROLE_CAREGIVER (enforced in SecurityConfig)
        return ResponseEntity.ok(caregiverService.getPatientProgress(caregiver.getId(), patientId));
    }

    @GetMapping("/patient/{patientId}/difficulty-log")
    public ResponseEntity<List<DifficultyLogResponse>> getPatientDifficultyLogs(
            @AuthenticationPrincipal User caregiver,
            @PathVariable UUID patientId) {
        
        // This endpoint requires ROLE_CAREGIVER (enforced in SecurityConfig)
        return ResponseEntity.ok(caregiverService.getDifficultyLogs(caregiver.getId(), patientId));
    }
}
