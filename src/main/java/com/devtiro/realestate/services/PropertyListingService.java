package com.devtiro.realestate.services;

import com.devtiro.realestate.domain.dto.*;
import com.devtiro.realestate.domain.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PropertyListingService {

    Page<PropertyListingResponseDto> getAllListings(Pageable pageable);

    PropertyListingResponseDto createPropertyListing(PropertyListingCreateRequest request, User agent);

    PropertyListingResponseDto uploadPhotos(List<PhotoDto> photos, String agentId, String propertyListingId);

    PropertyListingResponseDto getPropertyListingById(String propertyListingId);

    PropertyListingResponseDto updatePropertyListing(PropertyListingUpdateRequest request, String agentId, String propertyListingId);

    PropertyListingResponseDto updatePropertyListingStatus(PropertyListingStatusUpdateRequest request, String agentId, String propertyListingId);

    void deletePropertyListing(String agentId, String propertyListingId);

    Page<PropertyListingResponseDto> getMyPropertyListings(String agentId, Pageable pageable);

    PropertySearchResponseDto searchProperties(SearchCriteriaDto criteria);



}
