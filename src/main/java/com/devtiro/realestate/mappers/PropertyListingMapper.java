package com.devtiro.realestate.mappers;

import com.devtiro.realestate.domain.dto.PropertyListingCreateRequest;
import com.devtiro.realestate.domain.dto.PropertyListingResponseDto;
import com.devtiro.realestate.domain.dto.PropertyListingUpdateRequest;
import com.devtiro.realestate.domain.entities.PropertyListing;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {PhotoMapper.class, GeoLocationMapper.class})
public interface PropertyListingMapper {

    @Mapping(target = "listedDate", source = "createdDate")
    @Mapping(target = "updatedDate", source = "lastModifiedDate")
    PropertyListingResponseDto toPropertyListingResponseDto(PropertyListing propertyListing);

    PropertyListing toEntity(PropertyListingCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget PropertyListing propertyListing, PropertyListingUpdateRequest request);

}
