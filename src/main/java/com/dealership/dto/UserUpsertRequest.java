package com.dealership.dto;

import java.util.List;

public record UserUpsertRequest(
        String username,
        String password,
        List<String> roles,
        Boolean enabled
) {}
