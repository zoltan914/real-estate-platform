package com.devtiro.realestate.services;

import com.devtiro.realestate.domain.dto.*;
import com.devtiro.realestate.domain.entities.User;

import java.util.List;

public interface PropertyViewingService {

    PropertyViewingResponseDto requestViewing(PropertyViewingRequest request, User user);

    List<PropertyViewingResponseDto> getAllUserViewings(String userId);
    List<PropertyViewingResponseDto> getAllAgentViewings(String agentId);

    List<PropertyViewingResponseDto> getAllUserConfirmedViewings(String userId);
    List<PropertyViewingResponseDto> getAllAgentConfirmedViewings(String agentId);

    PropertyViewingResponseDto confirmViewing(PropertyViewingConfirmRequest request, User agent);

    PropertyViewingResponseDto rescheduleViewing(String viewingId, PropertyViewingRescheduleRequest request, User userPrincipal);

    PropertyViewingResponseDto cancelViewing(String viewingId, PropertyViewingCancelRequest request, User userPrincipal);

    PropertyViewingResponseDto updateViewingStatus(String viewingId, PropertyViewingStatusUpdateRequest request, User userPrincipal);

}
