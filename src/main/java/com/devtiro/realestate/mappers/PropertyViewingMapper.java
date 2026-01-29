package com.devtiro.realestate.mappers;

import com.devtiro.realestate.domain.dto.PropertyViewingResponseDto;
import com.devtiro.realestate.domain.entities.PropertyViewing;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PropertyViewingMapper {

    @Mapping(target = "updatedDate", source = "lastModifiedDate")
    PropertyViewingResponseDto toPropertyViewingResponseDto(PropertyViewing propertyViewing);

}
