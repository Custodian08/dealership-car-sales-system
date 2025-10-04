package com.dealership.dto;

import java.math.BigDecimal;

public record InventoryValuationDto(
        BigDecimal availableValue,
        BigDecimal reservedValue,
        BigDecimal soldValue,
        long availableCount,
        long reservedCount,
        long soldCount,
        BigDecimal totalValue
) {}
