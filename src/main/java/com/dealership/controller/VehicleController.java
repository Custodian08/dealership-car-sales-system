package com.dealership.controller;

import com.dealership.domain.Vehicle;
import com.dealership.domain.VehicleStatus;
import com.dealership.dto.VehicleDto;
import com.dealership.dto.VehicleUpsertRequest;
import com.dealership.dto.ChangeStatusRequest;
import com.dealership.service.VehicleService;
import com.dealership.repo.SaleRepository;
import com.dealership.service.SellerProfileService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {
    private final VehicleService vehicleService;
    private final SaleRepository saleRepository;
    private final SellerProfileService sellerProfiles;

    public VehicleController(VehicleService vehicleService, SaleRepository saleRepository, SellerProfileService sellerProfiles) {
        this.vehicleService = vehicleService;
        this.saleRepository = saleRepository;
        this.sellerProfiles = sellerProfiles;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE','GUEST','LISTER','SALESPERSON','ACCOUNTANT')")
    public List<VehicleDto> list(@RequestParam(required = false) VehicleStatus status,
                                 @RequestParam(required = false, name = "q") String query) {
        return vehicleService.list(status, query).stream().map(this::toDto).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE','GUEST','LISTER','SALESPERSON','ACCOUNTANT')")
    public VehicleDto get(@PathVariable UUID id) {
        Vehicle v = vehicleService.getOrThrow(id);
        return toDto(v);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE','LISTER')")
    public VehicleDto create(@RequestBody @Validated VehicleUpsertRequest req) {
        Vehicle v = vehicleService.create(req);
        return toDto(v);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE','LISTER')")
    public VehicleDto update(@PathVariable UUID id, @RequestBody @Validated VehicleUpsertRequest req) {
        Vehicle v = vehicleService.update(id, req);
        return toDto(v);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE','LISTER')")
    public void delete(@PathVariable UUID id) {
        vehicleService.delete(id);
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public VehicleDto changeStatus(@PathVariable UUID id, @RequestBody @Validated ChangeStatusRequest req) {
        Vehicle v = vehicleService.changeStatus(id, req.status(), req.undoSale());
        return toDto(v);
    }

    private VehicleDto toDto(Vehicle v) {
        String username = saleRepository.findByVehicle(v).map(s -> s.getSalespersonUsername()).orElse(null);
        String lastSp = username != null ? sellerProfiles.displayName(username) : null;
        String owner = v.getOwnerUsername();
        String ownerDisp = owner != null ? sellerProfiles.displayName(owner) : null;
        return new VehicleDto(
                v.getId(), v.getVin(), v.getMake(), v.getModel(), v.getYear(), v.getStatus().name(), v.getPrice(),
                lastSp, owner, ownerDisp
        );
    }
}
