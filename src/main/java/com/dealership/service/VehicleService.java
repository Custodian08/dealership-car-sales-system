package com.dealership.service;

import com.dealership.domain.Vehicle;
import com.dealership.domain.VehicleStatus;
import com.dealership.exception.NotFoundException;
import com.dealership.repo.VehicleRepository;
import com.dealership.repo.ReservationRepository;
import com.dealership.repo.SaleRepository;
import com.dealership.domain.ReservationStatus;
import com.dealership.dto.VehicleUpsertRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;
import com.dealership.dto.InventoryValuationDto;

@Service
public class VehicleService {
    private final VehicleRepository vehicleRepository;
    private final ReservationRepository reservationRepository;
    private final SaleRepository saleRepository;

    public VehicleService(VehicleRepository vehicleRepository,
                          ReservationRepository reservationRepository,
                          SaleRepository saleRepository) {
        this.vehicleRepository = vehicleRepository;
        this.reservationRepository = reservationRepository;
        this.saleRepository = saleRepository;
    }

    public List<Vehicle> list(VehicleStatus status, String query) {
        if (query == null || query.isBlank()) {
            if (status == null) return vehicleRepository.findAll();
            return vehicleRepository.findByStatus(status);
        }
        List<Vehicle> found = vehicleRepository
                .findByVinContainingIgnoreCaseOrMakeContainingIgnoreCaseOrModelContainingIgnoreCase(query, query, query);
        if (status == null) return found;
        return found.stream().filter(v -> v.getStatus() == status).toList();
    }

    public Vehicle getOrThrow(UUID id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Vehicle not found"));
    }

    public Vehicle create(VehicleUpsertRequest req) {
        if (vehicleRepository.existsByVinIgnoreCase(req.vin())) {
            throw new IllegalStateException("Vehicle with VIN already exists");
        }
        Vehicle v = new Vehicle();
        v.setVin(req.vin());
        v.setMake(req.make());
        v.setModel(req.model());
        v.setYear(req.year());
        v.setPrice(req.price());
        v.setOwnerUsername(req.ownerUsername());
        // status controlled by flows; if provided, respect only AVAILABLE
        v.setStatus(VehicleStatus.AVAILABLE);
        return vehicleRepository.save(v);
    }

    public Vehicle update(UUID id, VehicleUpsertRequest req) {
        Vehicle v = getOrThrow(id);
        if (!v.getVin().equalsIgnoreCase(req.vin())) {
            throw new IllegalStateException("VIN cannot be changed");
        }
        v.setMake(req.make());
        v.setModel(req.model());
        v.setYear(req.year());
        v.setPrice(req.price());
        if (req.ownerUsername() != null && !req.ownerUsername().isBlank()) {
            v.setOwnerUsername(req.ownerUsername());
        }
        return vehicleRepository.save(v);
    }

    public void delete(UUID id) {
        Vehicle v = getOrThrow(id);
        if (v.getStatus() == VehicleStatus.RESERVED) {
            throw new IllegalStateException("Cannot delete reserved vehicle");
        }
        if (v.getStatus() == VehicleStatus.SOLD || saleRepository.findByVehicle(v).isPresent()) {
            throw new IllegalStateException("Cannot delete sold vehicle");
        }
        if (reservationRepository.findByVehicle_IdAndStatus(id, ReservationStatus.ACTIVE).isPresent()) {
            throw new IllegalStateException("Cannot delete vehicle with active reservation");
        }
        vehicleRepository.delete(v);
    }

    /**
     * Change vehicle status. Supported flows:
     * - RESERVED -> AVAILABLE: cancel active reservation if any.
     * - SOLD -> AVAILABLE: only when undoSale = true; delete last sale record and set AVAILABLE.
     * - AVAILABLE -> AVAILABLE: no-op.
     * Other transitions are not supported here; use dedicated flows (reserve/sell).
     */
    public Vehicle changeStatus(UUID id, String targetStatus, boolean undoSale) {
        Vehicle v = getOrThrow(id);
        if (targetStatus == null || targetStatus.isBlank()) {
            throw new IllegalArgumentException("Target status is required");
        }
        VehicleStatus to;
        try {
            to = VehicleStatus.valueOf(targetStatus.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unknown status: " + targetStatus);
        }
        VehicleStatus from = v.getStatus();
        if (from == to) return v;

        switch (to) {
            case AVAILABLE -> {
                if (from == VehicleStatus.RESERVED) {
                    reservationRepository.findByVehicle_IdAndStatus(id, ReservationStatus.ACTIVE).ifPresent(r -> {
                        r.setStatus(ReservationStatus.CANCELLED);
                        reservationRepository.save(r);
                    });
                    v.setStatus(VehicleStatus.AVAILABLE);
                    return vehicleRepository.save(v);
                }
                if (from == VehicleStatus.SOLD) {
                    if (!undoSale) {
                        throw new IllegalStateException("Reverting from SOLD to AVAILABLE requires undoSale=true");
                    }
                    saleRepository.findByVehicle(v).ifPresent(saleRepository::delete);
                    v.setStatus(VehicleStatus.AVAILABLE);
                    return vehicleRepository.save(v);
                }
                // from AVAILABLE or others: just set AVAILABLE
                v.setStatus(VehicleStatus.AVAILABLE);
                return vehicleRepository.save(v);
            }
            case RESERVED, SOLD -> throw new IllegalStateException("Use dedicated flows to set status to " + to);
        }
        return v;
    }

    public InventoryValuationDto inventoryValuation() {
        BigDecimal availableValue = nz(vehicleRepository.sumByStatus(VehicleStatus.AVAILABLE));
        BigDecimal reservedValue = nz(vehicleRepository.sumByStatus(VehicleStatus.RESERVED));
        BigDecimal soldValue = nz(vehicleRepository.sumByStatus(VehicleStatus.SOLD));
        long availableCount = vehicleRepository.countByStatus(VehicleStatus.AVAILABLE);
        long reservedCount = vehicleRepository.countByStatus(VehicleStatus.RESERVED);
        long soldCount = vehicleRepository.countByStatus(VehicleStatus.SOLD);
        BigDecimal totalValue = availableValue.add(reservedValue).add(soldValue);
        return new InventoryValuationDto(availableValue, reservedValue, soldValue, availableCount, reservedCount, soldCount, totalValue);
    }

    private static BigDecimal nz(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }
}
