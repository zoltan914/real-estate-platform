package com.devtiro.realestate.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyViewingConfirmRequest {

    @NotBlank(message = "Viewing ID is required")
    private String propertyViewingId;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}
