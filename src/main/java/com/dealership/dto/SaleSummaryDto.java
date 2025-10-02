package com.dealership.dto;

import java.math.BigDecimal;

public record SaleSummaryDto(
        long totalSales,
        BigDecimal totalRevenue
) {}
