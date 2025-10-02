package com.dealership.service;

import com.dealership.domain.Vehicle;
import com.dealership.domain.VehicleStatus;
import com.dealership.exception.NotFoundException;
import com.dealership.repo.VehicleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class VehicleService {
    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public List<Vehicle> list(VehicleStatus status) {
        if (status == null) return vehicleRepository.findAll();
        return vehicleRepository.findByStatus(status);
    }

    public Vehicle getOrThrow(UUID id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Vehicle not found"));
    }
}
