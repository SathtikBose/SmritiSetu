package com.example.SpringBoot_Bakend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @NotBlank private String name;
    @NotBlank private String preferredLanguage;
}
