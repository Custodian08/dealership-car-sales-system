package com.dealership.controller;

import com.dealership.domain.Vehicle;
import com.dealership.dto.SellRequest;
import com.dealership.dto.VehicleDto;
import com.dealership.service.SaleService;
import com.dealership.repo.SaleRepository;
import com.dealership.service.SellerProfileService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/vehicles")
public class SaleController {
    private final SaleService saleService;
    private final SaleRepository saleRepository;
    private final SellerProfileService sellerProfiles;

    public SaleController(SaleService saleService, SaleRepository saleRepository, SellerProfileService sellerProfiles) {
        this.saleService = saleService;
        this.saleRepository = saleRepository;
        this.sellerProfiles = sellerProfiles;
    }

    @PostMapping("/{id}/sell")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE','SALESPERSON')")
    public VehicleDto sell(@PathVariable UUID id, @RequestBody @Validated SellRequest req) {
        Vehicle v = saleService.sell(id, req.customerEmail(), req.price(), req.salespersonUsername());
        String username = saleRepository.findByVehicle(v).map(s -> s.getSalespersonUsername()).orElse(null);
        String lastSp = username != null ? sellerProfiles.displayName(username) : null;
        String owner = v.getOwnerUsername();
        String ownerDisp = owner != null ? sellerProfiles.displayName(owner) : null;
        return new VehicleDto(v.getId(), v.getVin(), v.getMake(), v.getModel(), v.getYear(), v.getStatus().name(), v.getPrice(), lastSp, owner, ownerDisp);
    }
}
