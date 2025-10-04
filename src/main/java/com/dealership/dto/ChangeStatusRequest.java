package com.dealership.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangeStatusRequest(
        @NotBlank String status,
        boolean undoSale
) {}
