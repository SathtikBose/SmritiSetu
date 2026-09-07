package com.example.SpringBoot_Bakend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @NotBlank private String name;
    private String preferredLanguage;
    private String phone;
    private String gender;
    private Integer age;
    private String avatarUri;
}
