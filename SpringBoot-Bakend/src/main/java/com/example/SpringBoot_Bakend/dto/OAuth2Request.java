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
    private String idToken; // Token received from Google on the frontend
    private Role role; // Optional: specify role during first-time OAuth registration
}
