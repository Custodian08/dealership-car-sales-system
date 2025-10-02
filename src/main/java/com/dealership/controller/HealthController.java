package com.dealership.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {
    @GetMapping({"/", "/health"})
    public ResponseEntity<?> ok() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}
