package com.dealership.repo;

import com.dealership.domain.Reservation;
import com.dealership.domain.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    Optional<Reservation> findByVehicle_IdAndStatus(UUID vehicleId, ReservationStatus status);
}
