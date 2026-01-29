package com.devtiro.realestate.domain.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyViewingRescheduleRequest {

    @NotNull(message = "New scheduled date and time is required")
    @Future(message = "Scheduled date must be in the future")
    private LocalDateTime newScheduledDateTime;

    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;
}
