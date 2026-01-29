package com.devtiro.realestate.mappers;

import com.devtiro.realestate.domain.dto.GeoLocationDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GeoLocationMapper {

    default GeoPoint toGeoPoint(GeoLocationDto location) {
        if (location == null) {
            return null;
        }
        return new GeoPoint(location.getLat(), location.getLon());
    }

    default GeoLocationDto toGeoLocationDto(GeoPoint location) {
        if (location == null) {
            return null;
        }
        GeoLocationDto dto = new GeoLocationDto();
        dto.setLat(location.getLat());
        dto.setLon(location.getLon());
        return dto;
    }
}