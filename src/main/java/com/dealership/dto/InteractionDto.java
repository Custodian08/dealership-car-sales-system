package com.dealership.dto;

import com.dealership.domain.InteractionType;

import java.time.LocalDateTime;
import java.util.UUID;

public record InteractionDto(
        UUID id,
        UUID customerId,
        UUID vehicleId,
        InteractionType type,
        String notes,
        LocalDateTime occurredAt,
        String employeeUsername
) {}
