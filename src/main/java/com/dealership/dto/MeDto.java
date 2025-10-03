package com.dealership.dto;

import java.util.List;

public record MeDto(
        String username,
        List<String> roles
) {}
