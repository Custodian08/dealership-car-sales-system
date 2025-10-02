package com.dealership.repo;

import com.dealership.domain.Reservation;
import com.dealership.domain.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    Optional<Reservation> findByVehicle_IdAndStatus(UUID vehicleId, ReservationStatus status);
    List<Reservation> findAllByStatusAndExpiresAtBefore(ReservationStatus status, LocalDateTime time);
    boolean existsByCustomer_IdAndStatus(UUID customerId, ReservationStatus status);
    boolean existsByCustomer_Id(UUID customerId);
}
