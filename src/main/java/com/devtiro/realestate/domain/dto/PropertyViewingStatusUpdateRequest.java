package com.devtiro.realestate.domain.dto;

import com.devtiro.realestate.domain.entities.ViewingStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyViewingStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private ViewingStatus status;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
}
