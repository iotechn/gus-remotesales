package com.dobbinsoft.gus.remotesales.client.gus.product.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Currency codes for major world currencies
 */
@Schema(description = "Currency codes for major world currencies")
public enum CurrencyCode {
    
    @Schema(description = "US Dollar")
    USD,
    
    @Schema(description = "Euro")
    EUR,
    
    @Schema(description = "Japanese Yen")
    JPY,
    
    @Schema(description = "British Pound Sterling")
    GBP,
    
    @Schema(description = "Chinese Yuan")
    CNY,
    
    @Schema(description = "Canadian Dollar")
    CAD,
    
    @Schema(description = "Swiss Franc")
    CHF,
    
    @Schema(description = "Australian Dollar")
    AUD,
    
    @Schema(description = "Hong Kong Dollar")
    HKD,
    
    @Schema(description = "Singapore Dollar")
    SGD
}
