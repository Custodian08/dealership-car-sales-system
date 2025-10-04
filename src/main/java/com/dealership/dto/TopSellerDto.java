package com.dealership.dto;

import java.math.BigDecimal;

public record TopSellerDto(
        String salesperson,
        long totalSales,
        BigDecimal totalRevenue
) {}
