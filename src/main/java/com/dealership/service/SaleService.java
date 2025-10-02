package com.dealership.service;

import com.dealership.domain.*;
import com.dealership.dto.SaleSummaryDto;
import com.dealership.repo.ReservationRepository;
import com.dealership.repo.SaleRepository;
import com.dealership.repo.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class SaleService {
    private final SaleRepository saleRepository;
    private final VehicleRepository vehicleRepository;
    private final VehicleService vehicleService;
    private final ReservationRepository reservationRepository;
    private final CustomerService customerService;
    private final CurrentUserService currentUserService;

    public SaleService(SaleRepository saleRepository,
                       VehicleRepository vehicleRepository,
                       VehicleService vehicleService,
                       ReservationRepository reservationRepository,
                       CustomerService customerService,
                       CurrentUserService currentUserService) {
        this.saleRepository = saleRepository;
        this.vehicleRepository = vehicleRepository;
        this.vehicleService = vehicleService;
        this.reservationRepository = reservationRepository;
        this.customerService = customerService;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public Vehicle sell(UUID vehicleId, String customerEmail, BigDecimal price) {
        Vehicle v = vehicleService.getOrThrow(vehicleId);
        if (v.getStatus() == VehicleStatus.SOLD) {
            throw new IllegalStateException("Vehicle already sold");
        }
        reservationRepository.findByVehicle_IdAndStatus(vehicleId, ReservationStatus.ACTIVE)
                .ifPresent(rr -> { throw new IllegalStateException("Vehicle is reserved; cancel first"); });
        Sale s = new Sale();
        s.setVehicle(v);
        s.setCustomer(customerService.getOrCreate(customerEmail, "", ""));
        s.setSalespersonUsername(currentUserService.getCurrentUsername());
        s.setPrice(price);
        saleRepository.save(s);
        v.setStatus(VehicleStatus.SOLD);
        vehicleRepository.save(v);
        return v;
    }

    @Transactional(readOnly = true)
    public SaleSummaryDto summary() {
        BigDecimal revenue = saleRepository.sumTotal();
        if (revenue == null) revenue = BigDecimal.ZERO;
        long count = saleRepository.countAll();
        return new SaleSummaryDto(count, revenue);
    }
}
