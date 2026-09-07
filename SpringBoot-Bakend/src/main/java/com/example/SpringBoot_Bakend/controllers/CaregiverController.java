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

    @PostMapping("/link-by-code")
    public ResponseEntity<?> linkPatientByCode(@AuthenticationPrincipal User caregiver, @RequestBody java.util.Map<String, String> request) {
        String linkCode = request.get("linkCode");
        if (linkCode == null || linkCode.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "linkCode is required"));
        }
        User patient = caregiverService.linkPatientByCode(caregiver.getId(), linkCode);
        return ResponseEntity.ok(java.util.Map.of(
                "success", true,
                "message", "Patient linked successfully",
                "patient", java.util.Map.of(
                        "id", patient.getId(),
                        "name", patient.getName(),
                        "linkCode", patient.getPatientLinkCode() != null ? patient.getPatientLinkCode() : "",
                        "leagueTier", patient.getLeagueTier(),
                        "totalXp", patient.getTotalXp()
                )
        ));
    }

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
