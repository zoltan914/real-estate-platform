package com.devtiro.realestate.domain.dto;

import com.devtiro.realestate.domain.entities.PropertyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchCriteriaDto {
    
    // Price filters
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    
    // Location filters
    private String city;
    private String zipCode;
    private String neighborhood;
    private String state;

    // Geolocation filter
    private GeoLocationDto location; // Center point for radius search
    private Double distance; // Distance in kilometers from the location point

    // Property type filter
    private PropertyType propertyType;
    
    // Bedroom and bathroom filters
    private Integer minBedrooms;
    private Integer maxBedrooms;
    private Integer minBathrooms;
    private Integer maxBathrooms;
    
    // Square footage filter
    private BigDecimal minSquareFeet;
    private BigDecimal maxSquareFeet;
    
    // Features filters
    private Boolean hasGarage;
    private Boolean hasPool;
    private Boolean hasGarden;
    
    // Sorting
    private String sortBy; // price, dateCreated, squareFeet
    private String sortDirection; // asc, desc
    
    // Pagination
    private Integer page;
    private Integer size;
}
