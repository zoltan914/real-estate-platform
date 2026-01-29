package com.devtiro.realestate.domain.dto;

import com.devtiro.realestate.domain.entities.ViewingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyViewingResponseDto {

    private String id;
    private String propertyListingId;
    private String userId;
    private String agentId;
    private String userName;
    private String userEmail;
    private String userPhone;
    private String propertyAddress;
    private String propertyTitle;
    private LocalDateTime scheduledDateTime;
    private ViewingStatus status;
    private String notes;
    private String cancellationReason;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
