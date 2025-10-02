package com.dealership.dto;

import java.time.LocalDate;
import java.util.UUID;

public record CustomerDto(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String passportNumber,
        String passportIssuedBy,
        LocalDate passportIssueDate,
        String address
) {}
