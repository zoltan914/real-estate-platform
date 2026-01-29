package com.devtiro.realestate.domain.dto;

import com.devtiro.realestate.domain.entities.PropertyStatus;
import com.devtiro.realestate.domain.entities.PropertyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyListingResponseDto {
    private String id;
    private String agentId;
    private String title;
    private String description;
    private PropertyType propertyType;
    private PropertyStatus status;
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String neighborhood;
    private GeoLocationDto location;
    private Integer bedrooms;
    private Integer bathrooms;
    private BigDecimal squareFeet;
    private Integer yearBuilt;
    private BigDecimal price;
    private List<PhotoDto> photos = new ArrayList<>();
    private String floorPlanUrl;
    private String virtualTourUrl;
    private List<String> features = new ArrayList<>();
    private Boolean hasGarage;
    private Integer garageSpaces;
    private Boolean hasPool;
    private Boolean hasGarden;
    private LocalDateTime listedDate;
    private LocalDateTime updatedDate;
    private Double distanceInKm; // Distance from search point (only populated in geo searches)
    private LocalDateTime soldDate;


}
