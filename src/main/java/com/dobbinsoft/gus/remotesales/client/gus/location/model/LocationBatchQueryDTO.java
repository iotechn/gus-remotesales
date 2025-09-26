package com.dobbinsoft.gus.remotesales.client.gus.location.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "Data Transfer Object for batch querying locations by IDs or codes")
public class LocationBatchQueryDTO {

    @NotEmpty(message = "Location IDs cannot be empty")
    @Size(max = 100, message = "Cannot query more than 100 locations at once")
    @Schema(description = "List of location IDs to query", example = "[1, 2, 3, 4, 5]")
    private List<Long> locationIds;

    @Schema(description = "List of location codes to query", example = "['WH001', 'WH002']")
    private List<String> locationCodes;
} 