package com.devtiro.realestate.controller;

import com.devtiro.realestate.domain.dto.*;
import com.devtiro.realestate.domain.entities.PropertyType;
import com.devtiro.realestate.domain.entities.User;
import com.devtiro.realestate.services.PropertyListingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/listings")
@RequiredArgsConstructor
public class PropertyListingController {

    private final PropertyListingService propertyListingService;
    private final CacheManager cacheManager;

    @PostMapping
    public ResponseEntity<PropertyListingResponseDto> createListing(
            @Valid @RequestBody PropertyListingCreateRequest request,
            @AuthenticationPrincipal User agent) throws IOException {
        return ResponseEntity.ok(propertyListingService.createPropertyListing(request, agent));
    }

    @GetMapping
    public ResponseEntity<Page<PropertyListingResponseDto>> getAllListings(
            @PageableDefault(
                    size = 20,
                    page = 0,
                    sort = "createdDate",
                    direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(propertyListingService.getAllListings(pageable));
    }

    @PostMapping("/{propertyListingId}/photos")
    public ResponseEntity<PropertyListingResponseDto> uploadPhotos(
            @Valid @RequestBody List<PhotoDto> photos,
            @PathVariable String propertyListingId,
            @AuthenticationPrincipal User agent
    ) {
        return ResponseEntity.ok(propertyListingService.uploadPhotos(photos, agent.getId(), propertyListingId));
    }

    @GetMapping("/{propertyListingId}")
    public ResponseEntity<PropertyListingResponseDto> getPropertyListingById(
            @PathVariable String propertyListingId
    ) {
        return ResponseEntity.ok(propertyListingService.getPropertyListingById(propertyListingId));
    }

    @PutMapping("/{propertyListingId}")
    public ResponseEntity<PropertyListingResponseDto> updatePropertyListing(
            @RequestBody PropertyListingUpdateRequest request,
            @PathVariable String propertyListingId,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(propertyListingService.updatePropertyListing(request, user.getId(), propertyListingId));
    }

    @PatchMapping("/{propertyListingId}/status")
    public ResponseEntity<PropertyListingResponseDto> updatePropertyListingStatus(
            @Valid @RequestBody PropertyListingStatusUpdateRequest request,
            @PathVariable String propertyListingId,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(propertyListingService.updatePropertyListingStatus(request, user.getId(), propertyListingId));
    }

    @DeleteMapping("/{propertyListingId}")
    public ResponseEntity<Void> deletePropertyListing(
        @PathVariable String propertyListingId,
        @AuthenticationPrincipal User user
    ) {
        propertyListingService.deletePropertyListing(user.getId(), propertyListingId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my-listings")
    public ResponseEntity<Page<PropertyListingResponseDto>> getMyListings(
            @PageableDefault(
                    size = 20,
                    page = 0,
                    sort = "createdDate",
                    direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal User user
    ) {
        log.info("requesting my-listing endpoint with user id: {}, email: {}", user.getId(), user.getEmail());
        log.info("In cache the id: {}", cacheManager.getCache("usersByEmail").get(user.getEmail(), User.class).getId());
        return ResponseEntity.ok(propertyListingService.getMyPropertyListings(user.getId(), pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<PropertySearchResponseDto> searchProperties(
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String zipCode,
            @RequestParam(required = false) String neighborhood,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Double distance,
            @RequestParam(required = false) PropertyType propertyType,
            @RequestParam(required = false) Integer minBedrooms,
            @RequestParam(required = false) Integer maxBedrooms,
            @RequestParam(required = false) Integer minBathrooms,
            @RequestParam(required = false) Integer maxBathrooms,
            @RequestParam(required = false) BigDecimal minSquareFeet,
            @RequestParam(required = false) BigDecimal maxSquareFeet,
            @RequestParam(required = false) Boolean hasGarage,
            @RequestParam(required = false) Boolean hasPool,
            @RequestParam(required = false) Boolean hasGarden,
            @RequestParam(required = false, defaultValue = "dateCreated") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {

        // Build GeoLocationDto if latitude and longitude are provided
        GeoLocationDto location = null;
        if (latitude != null && longitude != null) {
            location = GeoLocationDto.builder()
                    .lat(latitude)
                    .lon(longitude)
                    .build();
        }

        SearchCriteriaDto criteria = SearchCriteriaDto.builder()
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .city(city)
                .zipCode(zipCode)
                .neighborhood(neighborhood)
                .state(state)
                .location(location)
                .distance(distance)
                .propertyType(propertyType)
                .minBedrooms(minBedrooms)
                .maxBedrooms(maxBedrooms)
                .minBathrooms(minBathrooms)
                .maxBathrooms(maxBathrooms)
                .minSquareFeet(minSquareFeet)
                .maxSquareFeet(maxSquareFeet)
                .hasGarage(hasGarage)
                .hasPool(hasPool)
                .hasGarden(hasGarden)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .page(page)
                .size(size)
                .build();

        return ResponseEntity.ok(propertyListingService.searchProperties(criteria));
    }
}
