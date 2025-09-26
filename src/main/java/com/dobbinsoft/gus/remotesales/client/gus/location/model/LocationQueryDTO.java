package com.dobbinsoft.gus.remotesales.client.gus.location.model;

import com.dobbinsoft.gus.common.model.dto.PageSearchDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "Data Transfer Object for querying locations with pagination")
public class LocationQueryDTO extends PageSearchDTO {

    @Schema(description = "Type of the location (STORE, WAREHOUSE, VIRTUAL)", example = "STORE")
    private LocationType type;

    @Schema(description = "Status of the location (ENABLED, DISABLED)", example = "ENABLED")
    private LocationStatus status;

    @Schema(description = "ISO country code to filter by", example = "US")
    private String countryCode;

    @Schema(description = "Province or state name to filter by", example = "California")
    private String province;

    @Schema(description = "City name to filter by", example = "San Francisco")
    private String city;

    @Schema(description = "District or area name to filter by", example = "Financial District")
    private String district;

    @Schema(description = "Region code for the location")
    private String regionCode;

    @Schema(description = "Email address of the region manager")
    private String regionManagerEmail;

    @Schema(description = "Email address of the location manager")
    private String locationManagerEmail;

    @Schema(description = "Mobile phone number to filter by")
    private String mobile;

    @Schema(description = "Telephone number to filter by")
    private String telephone;

    @Schema(description = "List of features to filter locations by", example = "[\"PARKING\", \"WIFI\"]")
    private List<String> features;
} 