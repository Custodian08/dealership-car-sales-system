package com.dealership.repo;

import com.dealership.domain.Vehicle;
import com.dealership.domain.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {
    List<Vehicle> findByStatus(VehicleStatus status);
}
