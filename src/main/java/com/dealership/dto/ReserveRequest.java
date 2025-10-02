package com.dealership.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record ReserveRequest(
        @Email @NotBlank String customerEmail,
        String customerFirstName,
        String customerLastName,
        BigDecimal deposit,
        Long minutesToExpire
) {}
