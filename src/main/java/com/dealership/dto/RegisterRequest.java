package com.dealership.dto;

import com.dealership.domain.SellerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterRequest(
        @NotBlank String username,
        @NotBlank String password,
        @NotNull SellerType type,
        String phone,
        String email,
        // PERSON
        String firstName,
        String lastName,
        // COMPANY
        String companyName,
        String inn,
        String kpp,
        String address,
        String contactName
) {}
