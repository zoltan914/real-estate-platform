package com.devtiro.realestate.services.impl;

import com.devtiro.realestate.domain.dto.*;
import com.devtiro.realestate.domain.entities.PropertyViewing;
import com.devtiro.realestate.domain.entities.User;
import com.devtiro.realestate.domain.entities.ViewingStatus;
import com.devtiro.realestate.exceptions.TimeSlotOverlapException;
import com.devtiro.realestate.exceptions.UnauthorizedException;
import com.devtiro.realestate.mappers.PropertyViewingMapper;
import com.devtiro.realestate.repositories.PropertyListingRepository;
import com.devtiro.realestate.repositories.PropertyViewingRepository;
import com.devtiro.realestate.services.NotificationService;
import com.devtiro.realestate.services.PropertyViewingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
public class PropertyViewingServiceImpl implements PropertyViewingService {

    @Value("${viewing.schedule.time-limit-in-minutes}")
    private int viewingScheduleTimeLimitInMinutes;

    private final PropertyViewingRepository viewingRepository;
    private final PropertyListingRepository listingRepository;
    private final PropertyViewingMapper viewingMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public PropertyViewingResponseDto requestViewing(PropertyViewingRequest request, User user) {

        String listingId = request.getPropertyListingId();
        var listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        var existingPropertyViewings = viewingRepository.findAllByPropertyListingId(listingId);

        // check double booking
        boolean hasConflict = existingPropertyViewings.stream()
                .filter(v -> v.getStatus().equals(ViewingStatus.CONFIRMED) ||
                        v.getStatus().equals(ViewingStatus.REQUESTED))
                .anyMatch(v -> {
                    var scheduledTime = v.getScheduledDateTime();
                    var requestedTime = request.getScheduledDateTime();
                    return Math.abs(Duration.between(scheduledTime, requestedTime).toMinutes()) < viewingScheduleTimeLimitInMinutes;
                });

        if (hasConflict) {
            throw new TimeSlotOverlapException("Time slot is not available. Please choose another time.");
        }

        PropertyViewing viewing = PropertyViewing.builder()
                .propertyListingId(listingId)
                .userId(user.getId())
                .agentId(listing.getAgentId())
                .agentEmail(listing.getAgentEmail())
                .userName(user.getFirstName() + " " + user.getLastName())
                .userEmail(user.getEmail())
                .userPhone(user.getPhoneNumber())
                .propertyAddress(listing.getStreet() + ", " + listing.getCity() + ", " + listing.getState())
                .propertyTitle(listing.getTitle())
                .scheduledDateTime(request.getScheduledDateTime())
                .status(ViewingStatus.REQUESTED)
                .notes(request.getNotes())
                .build();

        PropertyViewing savedViewing = viewingRepository.save(viewing);

        // Send notification to agent
        notificationService.notifyAgentOnViewingRequest(listing.getAgentEmail(), savedViewing);

        // Send notification to user
        notificationService.notifyUserOnViewingRequest(user.getEmail(), savedViewing);

        return viewingMapper.toPropertyViewingResponseDto(savedViewing);
    }

    @Override
    public List<PropertyViewingResponseDto> getAllUserViewings(String userId) {
        var viewings = viewingRepository.findAllByUserId(userId);

        return viewings.stream()
                .map(viewingMapper::toPropertyViewingResponseDto)
                .toList();
    }

    @Override
    public List<PropertyViewingResponseDto> getAllUserConfirmedViewings(String userId) {
        var viewings = viewingRepository.findAllByUserIdAndStatus(userId, ViewingStatus.CONFIRMED);
        return viewings.stream()
                .map(viewingMapper::toPropertyViewingResponseDto)
                .toList();
    }

    @Override
    public List<PropertyViewingResponseDto> getAllAgentConfirmedViewings(String agentId) {
        var viewings = viewingRepository.findAllByAgentIdAndStatus(agentId, ViewingStatus.CONFIRMED);
        return viewings.stream()
                .map(viewingMapper::toPropertyViewingResponseDto)
                .toList();
    }

    @Override
    public List<PropertyViewingResponseDto> getAllAgentViewings(String agentId) {
        var viewings = viewingRepository.findAllByAgentId(agentId);

        return viewings.stream()
                .map(viewingMapper::toPropertyViewingResponseDto)
                .toList();
    }

    @Override
    public PropertyViewingResponseDto confirmViewing(PropertyViewingConfirmRequest request, User agent) {
        String propertyViewingId = request.getPropertyViewingId();
        var viewing = viewingRepository.findById(propertyViewingId)
                .orElseThrow(() -> new IllegalArgumentException("Viewing not found with ID: " + propertyViewingId));

        if (!viewing.getAgentId().equals(agent.getId())) {
            throw new UnauthorizedException("Only the owner agent can confirm the viewing");
        }

        viewing.setNotes(viewing.getNotes() + "\n" + request.getNotes());
        viewing.setStatus(ViewingStatus.CONFIRMED);

        PropertyViewing savedViewing = viewingRepository.save(viewing);

        notificationService.notifyUserOnConfirmedViewingByAgent(savedViewing, agent);

        return viewingMapper.toPropertyViewingResponseDto(savedViewing);

    }

    @Override
    public PropertyViewingResponseDto rescheduleViewing(String viewingId, PropertyViewingRescheduleRequest request, User userPrincipal) {

        var viewing = viewingRepository.findById(viewingId)
                .orElseThrow(() -> new IllegalArgumentException("Viewing not found with ID: " + viewingId));

        if (!viewing.getUserId().equals(userPrincipal.getId()) &&
                !viewing.getAgentId().equals(userPrincipal.getId())) {
            throw new UnauthorizedException("You are not authorized to reschedule this viewing");
        }
        viewing.setScheduledDateTime(request.getNewScheduledDateTime());
        viewing.setStatus(ViewingStatus.RESCHEDULED);
        viewing.setNotes(viewing.getNotes() + "\nRescheduled: " + request.getReason());

        var updatedViewing = viewingRepository.save(viewing);

        notificationService.notifyViewingRescheduled(viewing, userPrincipal);

        return viewingMapper.toPropertyViewingResponseDto(updatedViewing);
    }

    @Override
    public PropertyViewingResponseDto cancelViewing(String viewingId, PropertyViewingCancelRequest request, User userPrincipal) {
        var viewing = viewingRepository.findById(viewingId)
                .orElseThrow(() -> new IllegalArgumentException("Viewing not found with ID: " + viewingId));

        if (!viewing.getUserId().equals(userPrincipal.getId()) &&
                !viewing.getAgentId().equals(userPrincipal.getId())) {
            throw new UnauthorizedException("You are not authorized to reschedule this viewing");
        }

        viewing.setStatus(ViewingStatus.CANCELLED);
        viewing.setCancellationReason(request.getReason());

        var updatedViewing = viewingRepository.save(viewing);
        log.info("Cancelled viewing with id: {}", viewingId);

        notificationService.notifyViewingCancelled(viewing, userPrincipal);

        return viewingMapper.toPropertyViewingResponseDto(updatedViewing);
    }

    @Override
    public PropertyViewingResponseDto updateViewingStatus(String viewingId, PropertyViewingStatusUpdateRequest request, User userPrincipal) {

        var viewing = viewingRepository.findById(viewingId)
                .orElseThrow(() -> new IllegalArgumentException("Viewing not found with ID: " + viewingId));

        if (!viewing.getAgentId().equals(userPrincipal.getId())) {
            throw new UnauthorizedException("Only the agent can update viewing status");
        }
        viewing.setStatus(request.getStatus());
        if (request.getNotes() != null) {
            viewing.setNotes(viewing.getNotes() + "\n" + request.getNotes());
        }

        var updatedViewing = viewingRepository.save(viewing);
        log.info("Updated viewing status to {} for id: {}", request.getStatus(), viewingId);

        return viewingMapper.toPropertyViewingResponseDto(updatedViewing);
    }

}