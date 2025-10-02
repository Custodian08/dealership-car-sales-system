package com.dealership.service;

import com.dealership.domain.Reservation;
import com.dealership.domain.ReservationStatus;
import com.dealership.domain.Vehicle;
import com.dealership.domain.VehicleStatus;
import com.dealership.repo.ReservationRepository;
import com.dealership.repo.VehicleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ReservationExpirationJob {
    private static final Logger log = LoggerFactory.getLogger(ReservationExpirationJob.class);

    private final ReservationRepository reservationRepository;
    private final VehicleRepository vehicleRepository;

    public ReservationExpirationJob(ReservationRepository reservationRepository,
                                    VehicleRepository vehicleRepository) {
        this.reservationRepository = reservationRepository;
        this.vehicleRepository = vehicleRepository;
    }

    // Run every minute; initial delay to let app fully start
    @Scheduled(initialDelay = 15_000L, fixedDelay = 60_000L)
    @Transactional
    public void expireReservations() {
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> expired = reservationRepository
                .findAllByStatusAndExpiresAtBefore(ReservationStatus.ACTIVE, now);
        if (expired.isEmpty()) {
            return;
        }
        for (Reservation r : expired) {
            r.setStatus(ReservationStatus.CANCELLED);
            reservationRepository.save(r);
            Vehicle v = r.getVehicle();
            if (v.getStatus() == VehicleStatus.RESERVED) {
                v.setStatus(VehicleStatus.AVAILABLE);
                vehicleRepository.save(v);
            }
            log.info("Expired reservation {} for vehicle {}", r.getId(), v.getId());
        }
    }
}
