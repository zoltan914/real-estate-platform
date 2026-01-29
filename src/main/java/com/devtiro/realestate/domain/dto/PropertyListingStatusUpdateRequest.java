package com.devtiro.realestate.domain.dto;

import com.devtiro.realestate.domain.entities.PropertyStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyListingStatusUpdateRequest {
    @NotNull(message = "Status is required")
    private PropertyStatus status;
}
