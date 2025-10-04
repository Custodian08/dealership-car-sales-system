package com.dealership.dto;

import com.dealership.domain.SellerType;

public record SellerProfileDto(
        String username,
        SellerType type,
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
