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
}
