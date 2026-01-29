package com.devtiro.realestate.domain.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Document(indexName = "property_viewings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyViewing extends Auditing {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String propertyListingId;

    @Field(type = FieldType.Keyword)
    private String userId;

    @Field(type = FieldType.Keyword)
    private String agentId;

    @Field(type = FieldType.Keyword)
    private String agentEmail;

    // User details
    @Field(type = FieldType.Text)
    private String userName;

    @Field(type = FieldType.Keyword)
    private String userEmail;

    @Field(type = FieldType.Keyword)
    private String userPhone;

    // Property details
    @Field(type = FieldType.Text)
    private String propertyAddress;

    @Field(type = FieldType.Text)
    private String propertyTitle;

    // Viewing details
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime scheduledDateTime;

    @Field(type = FieldType.Keyword)
    private ViewingStatus status;

    @Field(type = FieldType.Text)
    private String notes;

    @Field(type = FieldType.Text)
    private String cancellationReason;

}
