package com.example.SpringBoot_Bakend.dto;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class ReminderResponse {
    private UUID id;
    private String type;
    private String scheduledTime;
    private String message;
    private Boolean active;
}
