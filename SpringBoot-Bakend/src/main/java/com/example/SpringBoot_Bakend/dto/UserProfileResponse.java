package com.example.SpringBoot_Bakend.dto;

import com.example.SpringBoot_Bakend.entities.Role;
import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data @Builder
public class UserProfileResponse {
    private UUID id;
    private String username;
    private String name;
    private Role role;
    private String preferredLanguage;
}
