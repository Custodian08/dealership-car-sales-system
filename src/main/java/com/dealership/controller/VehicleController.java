package com.dealership.controller;

import com.dealership.domain.Vehicle;
import com.dealership.domain.VehicleStatus;
import com.dealership.dto.VehicleDto;
import com.dealership.service.VehicleService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {
    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE','GUEST')")
    public List<VehicleDto> list(@RequestParam(required = false) VehicleStatus status) {
        return vehicleService.list(status).stream().map(VehicleController::toDto).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE','GUEST')")
    public VehicleDto get(@PathVariable UUID id) {
        Vehicle v = vehicleService.getOrThrow(id);
        return toDto(v);
    }

    private static VehicleDto toDto(Vehicle v) {
        return new VehicleDto(v.getId(), v.getVin(), v.getMake(), v.getModel(), v.getYear(), v.getStatus().name(), v.getPrice());
    }
}
