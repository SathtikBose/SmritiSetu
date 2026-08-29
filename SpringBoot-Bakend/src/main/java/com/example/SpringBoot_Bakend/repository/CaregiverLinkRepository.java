package com.example.SpringBoot_Bakend.repository;

import com.example.SpringBoot_Bakend.entities.CaregiverLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CaregiverLinkRepository extends JpaRepository<CaregiverLink, UUID> {
    Optional<CaregiverLink> findByPatientId(UUID patientId);
    List<CaregiverLink> findByCaregiverId(UUID caregiverId);
    boolean existsByCaregiverIdAndPatientId(UUID caregiverId, UUID patientId);
}
