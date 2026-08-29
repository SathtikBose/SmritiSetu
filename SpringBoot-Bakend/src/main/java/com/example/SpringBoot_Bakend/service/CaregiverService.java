package com.example.SpringBoot_Bakend.service;

import com.example.SpringBoot_Bakend.dto.DifficultyLogResponse;
import com.example.SpringBoot_Bakend.dto.PatientProgressResponse;
import com.example.SpringBoot_Bakend.entities.CaregiverLink;
import com.example.SpringBoot_Bakend.entities.DifficultyLog;
import com.example.SpringBoot_Bakend.entities.GameProgress;
import com.example.SpringBoot_Bakend.entities.LeagueStatus;
import com.example.SpringBoot_Bakend.repository.CaregiverLinkRepository;
import com.example.SpringBoot_Bakend.repository.DifficultyLogRepository;
import com.example.SpringBoot_Bakend.repository.GameProgressRepository;
import com.example.SpringBoot_Bakend.repository.LeagueStatusRepository;
import com.example.SpringBoot_Bakend.repository.UserRepository;
import com.example.SpringBoot_Bakend.entities.Role;
import com.example.SpringBoot_Bakend.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CaregiverService {
    private final CaregiverLinkRepository linkRepository;
    private final GameProgressRepository progressRepository;
    private final LeagueStatusRepository leagueRepository;
    private final DifficultyLogRepository difficultyLogRepository;
    private final UserRepository userRepository;

    public void linkPatient(UUID caregiverId, UUID patientId) {
        if (caregiverId.equals(patientId)) throw new IllegalArgumentException("A caregiver cannot link to their own account.");
        User patient = userRepository.findById(patientId).orElseThrow(() -> new IllegalArgumentException("Patient not found."));
        if (patient.getRole() != Role.PATIENT) throw new IllegalArgumentException("The linked account must have the PATIENT role.");
        if (linkRepository.findByPatientId(patientId).isPresent() && !linkRepository.existsByCaregiverIdAndPatientId(caregiverId, patientId)) {
            throw new IllegalStateException("This patient is already linked to another caregiver in the demo.");
        }
        if (!linkRepository.existsByCaregiverIdAndPatientId(caregiverId, patientId)) {
            User caregiver = userRepository.findById(caregiverId).orElseThrow();
            linkRepository.save(CaregiverLink.builder().caregiver(caregiver).patient(patient).build());
        }
    }

    public void verifyLink(UUID caregiverId, UUID patientId) {
        List<CaregiverLink> links = linkRepository.findByCaregiverId(caregiverId);
        boolean isLinked = links.stream().anyMatch(link -> link.getPatient().getId().equals(patientId));
        if (!isLinked) {
            throw new SecurityException("Caregiver is not linked to this patient's data.");
        }
    }

    public PatientProgressResponse getPatientProgress(UUID caregiverId, UUID patientId) {
        verifyLink(caregiverId, patientId);

        List<GameProgress> progressList = progressRepository.findAllByUserId(patientId);
        LeagueStatus leagueStatus = leagueRepository.findByUserId(patientId).orElse(null);

        List<PatientProgressResponse.GameProgressDto> gameProgressDtos = progressList.stream()
                .map(p -> PatientProgressResponse.GameProgressDto.builder()
                        .gameName(p.getGame().getName())
                        .currentLevel(p.getCurrentLevel())
                        .currentDifficulty(p.getCurrentDifficulty())
                        .lastPlayed(p.getLastPlayed() != null ? p.getLastPlayed().toString() : "Never")
                        .build())
                .collect(Collectors.toList());

        String patientName = progressList.isEmpty() ? "Patient" : progressList.get(0).getUser().getName();

        return PatientProgressResponse.builder()
                .patientName(patientName)
                .currentLeague(leagueStatus != null ? leagueStatus.getCurrentLeague() : "BRONZE")
                .totalXp(leagueStatus != null ? leagueStatus.getTotalXp() : 0)
                .gameProgress(gameProgressDtos)
                .build();
    }

    public List<DifficultyLogResponse> getDifficultyLogs(UUID caregiverId, UUID patientId) {
        verifyLink(caregiverId, patientId);

        // Fetch logs descending so newest AI decisions are first
        List<DifficultyLog> logs = difficultyLogRepository.findAllByUserIdOrderByCreatedAtDesc(patientId);

        return logs.stream()
                .map(log -> DifficultyLogResponse.builder()
                        .gameName(log.getGame().getName())
                        .aiDecision(log.getAiDecision())
                        .reasoningText(log.getReasoningText()) // Provides the transparency!
                        .loggedAt(log.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
