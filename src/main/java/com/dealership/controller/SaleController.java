package com.dealership.controller;

import com.dealership.domain.Vehicle;
import com.dealership.dto.SellRequest;
import com.dealership.dto.VehicleDto;
import com.dealership.service.SaleService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/vehicles")
public class SaleController {
    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    @PostMapping("/{id}/sell")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public VehicleDto sell(@PathVariable UUID id, @RequestBody @Validated SellRequest req) {
        Vehicle v = saleService.sell(id, req.customerEmail(), req.price());
        return new VehicleDto(v.getId(), v.getVin(), v.getMake(), v.getModel(), v.getYear(), v.getStatus().name(), v.getPrice());
    }
}
