package com.dealership.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record SellRequest(
        @Email @NotBlank String customerEmail,
        @NotNull BigDecimal price
) {}
