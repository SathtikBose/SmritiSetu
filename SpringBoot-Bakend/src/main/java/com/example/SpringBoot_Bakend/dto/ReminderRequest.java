package com.example.SpringBoot_Bakend.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class ReminderRequest {
    @NotBlank private String type;
    @NotBlank private String scheduledTime;
    @NotBlank private String message;
    private Boolean active;
}
