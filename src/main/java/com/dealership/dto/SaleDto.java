package com.dealership.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record SaleDto(
        UUID id,
        String vehicleVin,
        String vehicleMake,
        String vehicleModel,
        Integer vehicleYear,
        String customerFirstName,
        String customerLastName,
        String customerEmail,
        BigDecimal price,
        String salespersonUsername,
        LocalDateTime saleDate
) {}
