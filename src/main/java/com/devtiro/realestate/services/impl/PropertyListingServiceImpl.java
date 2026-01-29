package com.devtiro.realestate.services.impl;

import com.devtiro.realestate.domain.dto.*;
import com.devtiro.realestate.domain.entities.Photo;
import com.devtiro.realestate.domain.entities.PropertyListing;
import com.devtiro.realestate.domain.entities.PropertyStatus;
import com.devtiro.realestate.domain.entities.User;
import com.devtiro.realestate.exceptions.UnauthorizedException;
import com.devtiro.realestate.mappers.PhotoMapper;
import com.devtiro.realestate.mappers.PropertyListingMapper;
import com.devtiro.realestate.repositories.PropertyListingRepository;
import com.devtiro.realestate.services.PropertyListingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.GeoDistanceOrder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PropertyListingServiceImpl implements PropertyListingService {

    private final PropertyListingMapper propertyListingMapper;
    private final PropertyListingRepository propertyListingRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final PhotoMapper photoMapper;

    @Override
    public Page<PropertyListingResponseDto> getAllListings(Pageable pageable) {
        Page<PropertyListing> allListings = propertyListingRepository.findAll(pageable);
        return allListings.map(propertyListingMapper::toPropertyListingResponseDto);
    }

    @Override
    public PropertyListingResponseDto createPropertyListing(PropertyListingCreateRequest request, User agent) {

        PropertyListing propertyListing = propertyListingMapper.toEntity(request);
        propertyListing.setAgentId(agent.getId());
        propertyListing.setAgentName(agent.getFirstName() + " " + agent.getLastName());
        propertyListing.setAgentEmail(agent.getEmail());
        propertyListing.setStatus(PropertyStatus.ACTIVE);

        PropertyListing savedPropertyListing = propertyListingRepository.save(propertyListing);

        PropertyListingResponseDto propertyListingResponseDto = propertyListingMapper.toPropertyListingResponseDto(savedPropertyListing);

        return propertyListingResponseDto;
    }

    @Override
    public PropertyListingResponseDto uploadPhotos(List<PhotoDto> photos, String agentId, String propertyListingId) {

        PropertyListing propertyListing = propertyListingRepository.findById(propertyListingId)
                .orElseThrow(() -> new IllegalArgumentException("Property listing not found with ID: " + propertyListingId));

        if (!propertyListing.getAgentId().equals(agentId)) {
            throw new UnauthorizedException("You are not authorized to update this listing");
        }

        List<Photo> photosList = photoMapper.toEntity(photos);
        LocalDateTime now = LocalDateTime.now();
        photosList.forEach(p -> {
            if (p.getUploadDate() == null) {
                p.setUploadDate(now);
            }
        });

        propertyListing.getPhotos().addAll(photosList);

        PropertyListing savedPropertyListing = propertyListingRepository.save(propertyListing);
        return propertyListingMapper.toPropertyListingResponseDto(savedPropertyListing);
    }

    @Override
    public PropertyListingResponseDto getPropertyListingById(String propertyListingId) {

        PropertyListing propertyListing = propertyListingRepository.findById(propertyListingId)
                .orElseThrow(() -> new IllegalArgumentException("Property listing not found with ID: " + propertyListingId));

        return propertyListingMapper.toPropertyListingResponseDto(propertyListing);
    }

    @Override
    public PropertyListingResponseDto updatePropertyListing(PropertyListingUpdateRequest request, String agentId, String propertyListingId) {

        PropertyListing propertyListing = propertyListingRepository.findById(propertyListingId)
                .orElseThrow(() -> new IllegalArgumentException("Property listing not found with ID: " + propertyListingId));

        if (!propertyListing.getAgentId().equals(agentId)) {
            throw new UnauthorizedException("You are not authorized to update this listing");
        }

        propertyListingMapper.updateEntity(propertyListing, request);

        PropertyListing savedPropertyListing = propertyListingRepository.save(propertyListing);

        log.info("Updated Property Listing with id: {}", propertyListingId);

        return propertyListingMapper.toPropertyListingResponseDto(savedPropertyListing);
    }

    @Override
    public PropertyListingResponseDto updatePropertyListingStatus(PropertyListingStatusUpdateRequest request, String agentId, String propertyListingId) {
        PropertyListing propertyListing = propertyListingRepository.findById(propertyListingId)
                .orElseThrow(() -> new IllegalArgumentException("Property listing not found with ID: " + propertyListingId));

        if (!propertyListing.getAgentId().equals(agentId)) {
            throw new UnauthorizedException("You are not authorized to update this listing");
        }

        propertyListing.setStatus(request.getStatus());

        if (request.getStatus().equals(PropertyStatus.SOLD)) {
            propertyListing.setSoldDate(LocalDateTime.now());
        }

        PropertyListing updatedPropertyListing = propertyListingRepository.save(propertyListing);

        log.info("Updated Property Listing status to {} for id: {}", request.getStatus(), propertyListingId);

        return propertyListingMapper.toPropertyListingResponseDto(updatedPropertyListing);
    }

    @Override
    public void deletePropertyListing(String agentId, String propertyListingId) {
        PropertyListing propertyListing = propertyListingRepository.findById(propertyListingId)
                .orElseThrow(() -> new IllegalArgumentException("Property listing not found with ID: " + propertyListingId));

        if (!propertyListing.getAgentId().equals(agentId)) {
            throw new UnauthorizedException("You are not authorized to update this listing");
        }

        propertyListingRepository.deleteById(propertyListingId);

        log.info("Deleted listing with id: {}", propertyListingId);

    }

    @Override
    public Page<PropertyListingResponseDto> getMyPropertyListings(String agentId, Pageable pageable) {
        Page<PropertyListing> propertyListings = propertyListingRepository.findAllByAgentId(agentId, pageable);

        return propertyListings.map(propertyListingMapper::toPropertyListingResponseDto);
    }


    @Override
    public PropertySearchResponseDto searchProperties(SearchCriteriaDto criteria) {
        log.info("Searching properties with criteria: {}", criteria);

        // Build the search criteria
        Criteria elasticCriteria = buildSearchCriteria(criteria);

        // Build the query with sorting and pagination
        Query query = buildQuery(elasticCriteria, criteria);

        // Execute the search
        SearchHits<PropertyListing> searchHits = elasticsearchOperations.search(query, PropertyListing.class);

        // Convert results to DTOs and calculate distances if geo search
        List<PropertyListingResponseDto> propertyDtos = searchHits.getSearchHits().stream()
                .map(searchHit -> {
                    PropertyListing property = searchHit.getContent();
                    PropertyListingResponseDto dto = propertyListingMapper.toPropertyListingResponseDto(property);

                    // Calculate and set distance if this is a geolocation search
                    if (criteria.getLocation() != null && criteria.getLocation().getLat() != null
                            && criteria.getLocation().getLon() != null && property.getLocation() != null) {
                        double distance = calculateDistance(
                                criteria.getLocation().getLat(),
                                criteria.getLocation().getLon(),
                                property.getLocation().getLat(),
                                property.getLocation().getLon()
                        );
                        dto.setDistanceInKm(Math.round(distance * 100.0) / 100.0); // Round to 2 decimal places
                    }

                    return dto;
                })
                .collect(Collectors.toList());

        // Calculate pagination info
        int page = criteria.getPage() != null ? criteria.getPage() : 0;
        int size = criteria.getSize() != null ? criteria.getSize() : 20;
        long totalElements = searchHits.getTotalHits();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        // Build and return response
        return PropertySearchResponseDto.builder()
                .properties(propertyDtos)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .currentPage(page)
                .pageSize(size)
                .hasNext(page < totalPages - 1)
                .hasPrevious(page > 0)
                .build();
    }

    private Criteria buildSearchCriteria(SearchCriteriaDto criteria) {
        List<Criteria> criteriaList = new ArrayList<>();

        // Price range filter
        if (criteria.getMinPrice() != null) {
            criteriaList.add(Criteria.where("price").greaterThanEqual(criteria.getMinPrice()));
        }
        if (criteria.getMaxPrice() != null) {
            criteriaList.add(Criteria.where("price").lessThanEqual(criteria.getMaxPrice()));
        }

        // Location filters
        if (criteria.getCity() != null && !criteria.getCity().isEmpty()) {
            criteriaList.add(Criteria.where("city").is(criteria.getCity()));
        }
        if (criteria.getZipCode() != null && !criteria.getZipCode().isEmpty()) {
            criteriaList.add(Criteria.where("zipCode").is(criteria.getZipCode()));
        }
        if (criteria.getNeighborhood() != null && !criteria.getNeighborhood().isEmpty()) {
            criteriaList.add(Criteria.where("neighborhood").matches(criteria.getNeighborhood()));
        }
        if (criteria.getState() != null && !criteria.getState().isEmpty()) {
            criteriaList.add(Criteria.where("state").is(criteria.getState()));
        }

        // Property type filter
        if (criteria.getPropertyType() != null) {
            criteriaList.add(Criteria.where("propertyType").is(criteria.getPropertyType()));
        }

        // Bedroom filters
        if (criteria.getMinBedrooms() != null) {
            criteriaList.add(Criteria.where("bedrooms").greaterThanEqual(criteria.getMinBedrooms()));
        }
        if (criteria.getMaxBedrooms() != null) {
            criteriaList.add(Criteria.where("bedrooms").lessThanEqual(criteria.getMaxBedrooms()));
        }

        // Bathroom filters
        if (criteria.getMinBathrooms() != null) {
            criteriaList.add(Criteria.where("bathrooms").greaterThanEqual(criteria.getMinBathrooms()));
        }
        if (criteria.getMaxBathrooms() != null) {
            criteriaList.add(Criteria.where("bathrooms").lessThanEqual(criteria.getMaxBathrooms()));
        }

        // Square footage filters
        if (criteria.getMinSquareFeet() != null) {
            criteriaList.add(Criteria.where("squareFeet").greaterThanEqual(criteria.getMinSquareFeet()));
        }
        if (criteria.getMaxSquareFeet() != null) {
            criteriaList.add(Criteria.where("squareFeet").lessThanEqual(criteria.getMaxSquareFeet()));
        }

        // Feature filters
        if (criteria.getHasGarage() != null && criteria.getHasGarage()) {
            criteriaList.add(Criteria.where("hasGarage").is(true));
        }
        if (criteria.getHasPool() != null && criteria.getHasPool()) {
            criteriaList.add(Criteria.where("hasPool").is(true));
        }
        if (criteria.getHasGarden() != null && criteria.getHasGarden()) {
            criteriaList.add(Criteria.where("hasGarden").is(true));
        }

        // Geolocation filter - radius search
        if (criteria.getLocation() != null && criteria.getLocation().getLat() != null
                && criteria.getLocation().getLon() != null && criteria.getDistance() != null) {

            Point point = new Point(criteria.getLocation().getLon(), criteria.getLocation().getLat());
            Distance distance = new Distance(criteria.getDistance(), Metrics.KILOMETERS);

            criteriaList.add(Criteria.where("location").within(point, distance));

            log.info("Geo search: center ({}, {}), radius {} km",
                    criteria.getLocation().getLat(), criteria.getLocation().getLon(), criteria.getDistance());
        }

        // Only show active listings
        criteriaList.add(Criteria.where("status").is(PropertyStatus.ACTIVE));

        // Combine all criteria with AND
        if (criteriaList.isEmpty()) {
            return new Criteria();
        } else if (criteriaList.size() == 1) {
            return criteriaList.getFirst();
        } else {
            Criteria combinedCriteria = criteriaList.getFirst();
            for (int i = 1; i < criteriaList.size(); i++) {
                combinedCriteria = combinedCriteria.and(criteriaList.get(i));
            }
            return combinedCriteria;
        }
    }

    private Query buildQuery(Criteria criteria, SearchCriteriaDto searchCriteria) {
        CriteriaQuery query = new CriteriaQuery(criteria);

        // Add geo-distance sorting if requested and location is provided
        if ("distance".equalsIgnoreCase(searchCriteria.getSortBy())
                && searchCriteria.getLocation() != null
                && searchCriteria.getLocation().getLat() != null
                && searchCriteria.getLocation().getLon() != null) {

            GeoPoint geoPoint = new GeoPoint(
                    searchCriteria.getLocation().getLat(),
                    searchCriteria.getLocation().getLon()
            );

            Sort.Direction direction = "desc".equalsIgnoreCase(searchCriteria.getSortDirection())
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;

             GeoDistanceOrder geoDistanceOrder = new GeoDistanceOrder("location", geoPoint);
            geoDistanceOrder = geoDistanceOrder.with(direction); // builder pattern
            Sort geoSort = Sort.by(geoDistanceOrder);  // Wrap in Sort.by()
            query.addSort(geoSort);                    // Now compatible!

            log.info("Sorting by distance from ({}, {})",
                    searchCriteria.getLocation().getLat(),
                    searchCriteria.getLocation().getLon());
        } else {
            // Add regular sorting
            Sort sort = buildSort(searchCriteria);
            if (sort != null) {
                query.addSort(sort);
            }
        }

        // Add pagination
        int page = searchCriteria.getPage() != null ? searchCriteria.getPage() : 0;
        int size = searchCriteria.getSize() != null ? searchCriteria.getSize() : 20;
        Pageable pageable = PageRequest.of(page, size);
        query.setPageable(pageable);

        return query;
    }

    private Sort buildSort(SearchCriteriaDto criteria) {
        String sortBy = criteria.getSortBy();
        String sortDirection = criteria.getSortDirection();

        if (sortBy == null || sortBy.isEmpty()) {
            // Default sort by date created (newest first)
            return Sort.by(Sort.Direction.DESC, "createdDate");
        }

        Sort.Direction direction = Sort.Direction.ASC;
        if ("desc".equalsIgnoreCase(sortDirection)) {
            direction = Sort.Direction.DESC;
        }

        return switch (sortBy.toLowerCase()) {
            case "price" -> Sort.by(direction, "price");
            case "datecreated", "date", "datelisted", "listeddate" -> Sort.by(direction, "createdDate");
            case "lastmodifieddate", "updateddate" -> Sort.by(direction, "lastModifiedDate");
            case "squarefeet", "size" -> Sort.by(direction, "squareFeet");
            default -> Sort.by(Sort.Direction.DESC, "createdDate");
        };
    }

    /**
     * Calculate distance between two points using Haversine formula
     * @param lat1 Latitude of point 1
     * @param lon1 Longitude of point 1
     * @param lat2 Latitude of point 2
     * @param lon2 Longitude of point 2
     * @return Distance in kilometers
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS_KM = 6371;

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }



}
