package com.dealership.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record VehicleUpsertRequest(
        @NotBlank(message = "VIN is required") String vin,
        @NotBlank(message = "Make is required") String make,
        @NotBlank(message = "Model is required") String model,
        @Min(value = 1900, message = "Year must be >= 1900")
        @Max(value = 2100, message = "Year must be <= 2100")
        Integer year,
        @NotNull(message = "Price is required")
        @Positive(message = "Price must be positive")
        BigDecimal price,
        // Optional: if null, defaults to AVAILABLE
        String status
) {}
