package com.devtiro.realestate.domain.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
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
public class PropertyViewingRequest {

    @NotBlank(message = "Listing ID is required")
    private String propertyListingId;

    @NotNull(message = "Scheduled date and time is required")
    @Future(message = "Scheduled date must be in the future")
    private LocalDateTime scheduledDateTime;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}
