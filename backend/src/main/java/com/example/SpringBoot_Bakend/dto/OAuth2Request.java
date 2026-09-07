package com.example.SpringBoot_Bakend.dto;

import com.example.SpringBoot_Bakend.entities.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OAuth2Request {
    private String idToken; // Token received from Google / Firebase on frontend
    private String email; // Optional direct email from Firebase Auth user
    private String name; // Optional display name from Firebase Auth user
    private String firebaseUid; // Firebase User UID
    private String patientCode; // Optional patient link code for Caregivers
    private Role role; // Optional: PATIENT or CAREGIVER
}

