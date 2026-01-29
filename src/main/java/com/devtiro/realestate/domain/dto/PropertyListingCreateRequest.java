package com.devtiro.realestate.domain.dto;

import com.devtiro.realestate.domain.entities.PropertyType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyListingCreateRequest {


    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @NotNull(message = "Property type is required")
    private PropertyType propertyType;

    @NotBlank(message = "Street address is required")
    private String street;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "ZIP code is required")
    @Pattern(regexp = "^\\d{5}(-\\d{4})?$", message = "Invalid ZIP code format")
    private String zipCode;

    private String neighborhood;

    @NotNull(message = "Location coordinates are required")
    private GeoLocationDto location;

    @NotNull(message = "Number of bedrooms is required")
    @Min(value = 0, message = "Bedrooms must be at least 0")
    private Integer bedrooms;

    @NotNull(message = "Number of bathrooms is required")
    @DecimalMin(value = "0", message = "Bathrooms must be at least 0")
    private Integer bathrooms;

    @NotNull(message = "Square feet is required")
    @DecimalMin(value = "1", message = "Square feet must be positive")
    private BigDecimal squareFeet;

    @Min(value = 1800, message = "Year built must be reasonable")
    @Max(value = 2100, message = "Year built must be reasonable")
    private Integer yearBuilt;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be positive")
    private BigDecimal price;

    private List<PhotoDto> photos = new ArrayList<>();
    private String floorPlanUrl;
    private String virtualTourUrl;
    private List<String> features = new ArrayList<>();

    private Boolean hasGarage;
    private Integer garageSpaces;
    private Boolean hasPool;
    private Boolean hasGarden;
}
