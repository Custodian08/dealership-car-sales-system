package com.dealership.service;

import com.dealership.domain.*;
import com.dealership.dto.SaleSummaryDto;
import com.dealership.repo.ReservationRepository;
import com.dealership.repo.SaleRepository;
import com.dealership.repo.VehicleRepository;
import com.dealership.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @Transactional(readOnly = true)
    public List<Sale> list() {
        return saleRepository.findAllByOrderBySaleDateDesc();
    }

    @Transactional(readOnly = true)
    public Sale getOrThrow(UUID id) {
        return saleRepository.findById(id).orElseThrow(() -> new NotFoundException("Sale not found"));
    }

    @Transactional(readOnly = true)
    public List<Sale> listBetween(LocalDate from, LocalDate to) {
        LocalDateTime f = from != null ? from.atStartOfDay() : LocalDate.MIN.atStartOfDay();
        LocalDateTime t = to != null ? to.plusDays(1).atStartOfDay().minusNanos(1) : LocalDate.MAX.atTime(23,59,59,999_999_999);
        return saleRepository.findAllBySaleDateBetweenOrderBySaleDateDesc(f, t);
    }

    @Transactional(readOnly = true)
    public SaleSummaryDto summaryBetween(LocalDate from, LocalDate to) {
        LocalDateTime f = from != null ? from.atStartOfDay() : LocalDate.MIN.atStartOfDay();
        LocalDateTime t = to != null ? to.plusDays(1).atStartOfDay().minusNanos(1) : LocalDate.MAX.atTime(23,59,59,999_999_999);
        BigDecimal revenue = saleRepository.sumTotalBetween(f, t);
        if (revenue == null) revenue = BigDecimal.ZERO;
        long count = saleRepository.countBySaleDateBetween(f, t);
        return new SaleSummaryDto(count, revenue);
    }

    // Salesperson-aware range listing
    @Transactional(readOnly = true)
    public List<Sale> listRange(LocalDate from, LocalDate to, String salesperson) {
        LocalDateTime f = from != null ? from.atStartOfDay() : LocalDate.MIN.atStartOfDay();
        LocalDateTime t = to != null ? to.plusDays(1).atStartOfDay().minusNanos(1) : LocalDate.MAX.atTime(23,59,59,999_999_999);
        if (salesperson == null || salesperson.isBlank()) {
            if (from == null && to == null) return list();
            return saleRepository.findAllBySaleDateBetweenOrderBySaleDateDesc(f, t);
        }
        if (from == null && to == null) return saleRepository.findAllBySalespersonUsernameOrderBySaleDateDesc(salesperson);
        return saleRepository.findAllBySalespersonUsernameAndSaleDateBetweenOrderBySaleDateDesc(salesperson, f, t);
    }

    @Transactional(readOnly = true)
    public SaleSummaryDto summaryRange(LocalDate from, LocalDate to, String salesperson) {
        LocalDateTime f = from != null ? from.atStartOfDay() : LocalDate.MIN.atStartOfDay();
        LocalDateTime t = to != null ? to.plusDays(1).atStartOfDay().minusNanos(1) : LocalDate.MAX.atTime(23,59,59,999_999_999);
        BigDecimal revenue;
        long count;
        if (salesperson == null || salesperson.isBlank()) {
            if (from == null && to == null) return summary();
            revenue = saleRepository.sumTotalBetween(f, t);
            if (revenue == null) revenue = BigDecimal.ZERO;
            count = saleRepository.countBySaleDateBetween(f, t);
        } else {
            revenue = saleRepository.sumTotalBySalespersonBetween(salesperson, f, t);
            if (revenue == null) revenue = BigDecimal.ZERO;
            count = saleRepository.countBySalespersonUsernameAndSaleDateBetween(salesperson, f, t);
        }
        return new SaleSummaryDto(count, revenue);
    }

    @Transactional(readOnly = true)
    public java.util.List<String> listSalespersons() {
        return saleRepository.distinctSalespersons();
    }
}
