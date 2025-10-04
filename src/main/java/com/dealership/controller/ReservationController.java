package com.dealership.controller;

import com.dealership.domain.Vehicle;
import com.dealership.dto.ReserveRequest;
import com.dealership.dto.VehicleDto;
import com.dealership.service.ReservationService;
import com.dealership.repo.SaleRepository;
import com.dealership.service.SellerProfileService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/vehicles")
public class ReservationController {
    private final ReservationService reservationService;
    private final SaleRepository saleRepository;
    private final SellerProfileService sellerProfiles;

    public ReservationController(ReservationService reservationService, SaleRepository saleRepository, SellerProfileService sellerProfiles) {
        this.reservationService = reservationService;
        this.saleRepository = saleRepository;
        this.sellerProfiles = sellerProfiles;
    }

    @PostMapping("/{id}/reserve")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE','SALESPERSON')")
    public VehicleDto reserve(@PathVariable UUID id, @RequestBody @Validated ReserveRequest req) {
        Vehicle v = reservationService.reserve(id, req.customerEmail(), req.customerFirstName(), req.customerLastName(), req.deposit(), req.minutesToExpire());
        String username = saleRepository.findByVehicle(v).map(s -> s.getSalespersonUsername()).orElse(null);
        String lastSp = username != null ? sellerProfiles.displayName(username) : null;
        return new VehicleDto(v.getId(), v.getVin(), v.getMake(), v.getModel(), v.getYear(), v.getStatus().name(), v.getPrice(), lastSp);
    }

    @PostMapping("/{id}/cancel-reservation")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE','SALESPERSON')")
    public VehicleDto cancel(@PathVariable UUID id) {
        Vehicle v = reservationService.cancel(id);
        String username = saleRepository.findByVehicle(v).map(s -> s.getSalespersonUsername()).orElse(null);
        String lastSp = username != null ? sellerProfiles.displayName(username) : null;
        return new VehicleDto(v.getId(), v.getVin(), v.getMake(), v.getModel(), v.getYear(), v.getStatus().name(), v.getPrice(), lastSp);
    }
}
