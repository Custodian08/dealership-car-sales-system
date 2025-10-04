package com.dealership.dto;

import java.util.List;

public record UserAccountDto(
        String username,
        List<String> roles
) {}
