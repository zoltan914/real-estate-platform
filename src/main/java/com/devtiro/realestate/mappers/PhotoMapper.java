package com.devtiro.realestate.mappers;

import com.devtiro.realestate.domain.dto.PhotoDto;
import com.devtiro.realestate.domain.entities.Photo;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PhotoMapper {

    List<PhotoDto> toPhotoDto(List<Photo> photos);

    List<Photo> toEntity(List<PhotoDto> photos);

}
