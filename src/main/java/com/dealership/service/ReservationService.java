package com.dealership.service;

import com.dealership.domain.*;
import com.dealership.repo.ReservationRepository;
import com.dealership.repo.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final VehicleRepository vehicleRepository;
    private final VehicleService vehicleService;
    private final CustomerService customerService;
    private final CurrentUserService currentUserService;

    public ReservationService(ReservationRepository reservationRepository,
                              VehicleRepository vehicleRepository,
                              VehicleService vehicleService,
                              CustomerService customerService,
                              CurrentUserService currentUserService) {
        this.reservationRepository = reservationRepository;
        this.vehicleRepository = vehicleRepository;
        this.vehicleService = vehicleService;
        this.customerService = customerService;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public Vehicle reserve(UUID vehicleId, String email, String firstName, String lastName, BigDecimal deposit, Long minutesToExpire) {
        Vehicle v = vehicleService.getOrThrow(vehicleId);
        if (v.getStatus() == VehicleStatus.SOLD) {
            throw new IllegalStateException("Vehicle already sold");
        }
        reservationRepository.findByVehicle_IdAndStatus(vehicleId, ReservationStatus.ACTIVE)
                .ifPresent(r -> { throw new IllegalStateException("Vehicle already reserved"); });
        if (v.getStatus() != VehicleStatus.AVAILABLE) {
            throw new IllegalStateException("Vehicle is not available");
        }
        Reservation r = new Reservation();
        r.setVehicle(v);
        r.setCustomer(customerService.getOrCreate(email, firstName, lastName));
        r.setReservedByUsername(currentUserService.getCurrentUsername());
        r.setDeposit(deposit != null ? deposit : BigDecimal.ZERO);
        if (minutesToExpire != null && minutesToExpire > 0) {
            r.setExpiresAt(LocalDateTime.now().plusMinutes(minutesToExpire));
        }
        reservationRepository.save(r);
        v.setStatus(VehicleStatus.RESERVED);
        vehicleRepository.save(v);
        return v;
    }

    @Transactional
    public Vehicle cancel(UUID vehicleId) {
        Reservation r = reservationRepository.findByVehicle_IdAndStatus(vehicleId, ReservationStatus.ACTIVE)
                .orElseThrow(() -> new IllegalStateException("No active reservation for vehicle"));
        r.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(r);
        Vehicle v = r.getVehicle();
        if (v.getStatus() == VehicleStatus.RESERVED) {
            v.setStatus(VehicleStatus.AVAILABLE);
            vehicleRepository.save(v);
        }
        return v;
    }
}
