package com.devtiro.realestate.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Auditing {

    @CreatedBy
    protected String createdBy;

    @CreatedDate
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    protected LocalDateTime createdDate;

    @LastModifiedBy
    protected String lastModifiedBy;

    @LastModifiedDate
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    protected LocalDateTime lastModifiedDate;

}
