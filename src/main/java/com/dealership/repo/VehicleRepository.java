package com.dealership.repo;

import com.dealership.domain.Vehicle;
import com.dealership.domain.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {
    List<Vehicle> findByStatus(VehicleStatus status);
    Optional<Vehicle> findByVinIgnoreCase(String vin);
    boolean existsByVinIgnoreCase(String vin);
    List<Vehicle> findByVinContainingIgnoreCaseOrMakeContainingIgnoreCaseOrModelContainingIgnoreCase(String vin, String make, String model);
}
