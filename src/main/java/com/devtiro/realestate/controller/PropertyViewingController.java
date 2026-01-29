package com.devtiro.realestate.controller;

import com.devtiro.realestate.domain.dto.*;
import com.devtiro.realestate.domain.entities.User;
import com.devtiro.realestate.services.PropertyViewingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/viewings")
@RequiredArgsConstructor
public class PropertyViewingController {

    private final PropertyViewingService propertyViewingService;

    @GetMapping("/user")
    public ResponseEntity<List<PropertyViewingResponseDto>> getAllUserViewings(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(propertyViewingService.getAllUserViewings(user.getId()));
    }

    @GetMapping("/agent")
    public ResponseEntity<List<PropertyViewingResponseDto>> getAllAgentViewings(
            @AuthenticationPrincipal User agent
    ) {
        return ResponseEntity.ok(propertyViewingService.getAllAgentViewings(agent.getId()));
    }

    @GetMapping("/user/scheduled")
    public ResponseEntity<List<PropertyViewingResponseDto>> getAllUserConfirmedViewings(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(propertyViewingService.getAllUserConfirmedViewings(user.getId()));
    }

    @GetMapping("/agent/scheduled")
    public ResponseEntity<List<PropertyViewingResponseDto>> getAllAgentConfirmedViewings(
            @AuthenticationPrincipal User agent
    ) {
        return ResponseEntity.ok(propertyViewingService.getAllAgentConfirmedViewings(agent.getId()));
    }

    @PostMapping("/user/request-viewing")
    public ResponseEntity<PropertyViewingResponseDto> requestViewing(
            @Valid @RequestBody PropertyViewingRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(propertyViewingService.requestViewing(request, user));
    }

    @PatchMapping("/agent/confirm-viewing")
    public ResponseEntity<PropertyViewingResponseDto> confirmViewing(
            @Valid @RequestBody PropertyViewingConfirmRequest request,
            @AuthenticationPrincipal User agent
    ) {
        return ResponseEntity.ok(propertyViewingService.confirmViewing(request, agent));
    }


    @PutMapping("/{viewingId}/reschedule")
    public ResponseEntity<PropertyViewingResponseDto> rescheduleViewing(
            @PathVariable String viewingId,
            @Valid @RequestBody PropertyViewingRescheduleRequest request,
            @AuthenticationPrincipal User userPrincipal
    ) {
        return ResponseEntity.ok(propertyViewingService.rescheduleViewing(viewingId, request, userPrincipal));
    }


    @PutMapping("/{viewingId}/cancel")
    public ResponseEntity<PropertyViewingResponseDto> cancelViewing(
            @PathVariable String viewingId,
            @Valid @RequestBody PropertyViewingCancelRequest request,
            @AuthenticationPrincipal User userPrincipal
    ) {
        return ResponseEntity.ok(propertyViewingService.cancelViewing(viewingId, request, userPrincipal));
    }

    @PutMapping("/{viewingId}/status")
    public ResponseEntity<PropertyViewingResponseDto> updateViewingStatus(
            @PathVariable String viewingId,
            @Valid @RequestBody PropertyViewingStatusUpdateRequest request,
            @AuthenticationPrincipal User userPrincipal
    ) {
        return ResponseEntity.ok(propertyViewingService.updateViewingStatus(viewingId, request, userPrincipal));
    }

}
