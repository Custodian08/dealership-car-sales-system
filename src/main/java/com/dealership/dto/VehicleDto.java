package com.dealership.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record VehicleDto(
        UUID id,
        String vin,
        String make,
        String model,
        Integer year,
        String status,
        BigDecimal price,
        String lastSalesperson
) {}
