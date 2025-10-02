package com.dealership.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CustomerUpsertRequest(
        @NotBlank(message = "First name is required") String firstName,
        @NotBlank(message = "Last name is required") String lastName,
        @NotBlank(message = "Email is required") @Email(message = "Email is invalid") String email,
        String phone,
        String passportNumber,
        String passportIssuedBy,
        java.time.LocalDate passportIssueDate,
        String address
) {}
