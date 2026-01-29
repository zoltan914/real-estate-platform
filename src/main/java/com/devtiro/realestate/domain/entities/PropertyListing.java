package com.devtiro.realestate.domain.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Document(indexName = "property_listings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyListing extends Auditing {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String agentId;

    @Field(type = FieldType.Text)
    private String agentName;

    @Field(type = FieldType.Keyword)
    private String agentEmail;

    // Property details
    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    @Field(type = FieldType.Keyword)
    private PropertyType propertyType;

    @Field(type = FieldType.Keyword)
    private PropertyStatus status;

    // Property features
    @Field(type = FieldType.Integer)
    private Integer bedrooms;

    @Field(type = FieldType.Integer)
    private Integer bathrooms;

    @Field(type = FieldType.Double)
    private BigDecimal squareFeet;

    @Field(type = FieldType.Integer)
    private Integer yearBuilt;

    @Field(type = FieldType.Double)
    private BigDecimal price;

    // Location
    @Field(type = FieldType.Text)
    private String street;

    @Field(type = FieldType.Keyword)
    private String city;

    @Field(type = FieldType.Keyword)
    private String state;

    @Field(type = FieldType.Keyword)
    private String zipCode;

    @Field(type = FieldType.Text)
    private String neighborhood;

    @GeoPointField
    private GeoPoint location;

    // Media
    @Field(type = FieldType.Nested)
    private List<Photo> photos = new ArrayList<>();

    @Field(type = FieldType.Keyword)
    private String floorPlanUrl;

    @Field(type = FieldType.Keyword)
    private String virtualTourUrl;

    // Features and amenities
    @Field(type = FieldType.Keyword)
    private List<String> features = new ArrayList<>();

    @Field(type = FieldType.Boolean)
    private Boolean hasGarage;

    @Field(type = FieldType.Integer)
    private Integer garageSpaces;

    @Field(type = FieldType.Boolean)
    private Boolean hasPool;

    @Field(type = FieldType.Boolean)
    private Boolean hasGarden;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime soldDate;


}
