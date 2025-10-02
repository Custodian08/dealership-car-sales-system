package com.dealership.controller;

import com.dealership.domain.Vehicle;
import com.dealership.dto.ReserveRequest;
import com.dealership.dto.VehicleDto;
import com.dealership.service.ReservationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/vehicles")
public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/{id}/reserve")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public VehicleDto reserve(@PathVariable UUID id, @RequestBody @Validated ReserveRequest req) {
        Vehicle v = reservationService.reserve(id, req.customerEmail(), req.customerFirstName(), req.customerLastName(), req.deposit(), req.minutesToExpire());
        return new VehicleDto(v.getId(), v.getVin(), v.getMake(), v.getModel(), v.getYear(), v.getStatus().name(), v.getPrice());
    }

    @PostMapping("/{id}/cancel-reservation")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public VehicleDto cancel(@PathVariable UUID id) {
        Vehicle v = reservationService.cancel(id);
        return new VehicleDto(v.getId(), v.getVin(), v.getMake(), v.getModel(), v.getYear(), v.getStatus().name(), v.getPrice());
    }
}
