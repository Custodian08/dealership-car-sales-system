package com.dealership.service;

import com.dealership.domain.*;
import com.dealership.dto.SaleSummaryDto;
import com.dealership.dto.TopSellerDto;
import com.dealership.repo.ReservationRepository;
import com.dealership.repo.SaleRepository;
import com.dealership.repo.VehicleRepository;
import com.dealership.repo.SellerProfileRepository;
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
    private final SellerProfileRepository sellerProfileRepository;

    public SaleService(SaleRepository saleRepository,
                       VehicleRepository vehicleRepository,
                       VehicleService vehicleService,
                       ReservationRepository reservationRepository,
                       CustomerService customerService,
                       CurrentUserService currentUserService,
                       SellerProfileRepository sellerProfileRepository) {
        this.saleRepository = saleRepository;
        this.vehicleRepository = vehicleRepository;
        this.vehicleService = vehicleService;
        this.reservationRepository = reservationRepository;
        this.customerService = customerService;
        this.currentUserService = currentUserService;
        this.sellerProfileRepository = sellerProfileRepository;
    }

    @Transactional
    public Vehicle sell(UUID vehicleId, String customerEmail, BigDecimal price) {
        return sell(vehicleId, customerEmail, price, null);
    }

    @Transactional
    public Vehicle sell(UUID vehicleId, String customerEmail, BigDecimal price, String salespersonOverride) {
        Vehicle v = vehicleService.getOrThrow(vehicleId);
        if (v.getStatus() == VehicleStatus.SOLD) {
            throw new IllegalStateException("Vehicle already sold");
        }
        reservationRepository.findByVehicle_IdAndStatus(vehicleId, ReservationStatus.ACTIVE)
                .ifPresent(rr -> { throw new IllegalStateException("Vehicle is reserved; cancel first"); });
        Sale s = new Sale();
        s.setVehicle(v);
        s.setCustomer(customerService.getOrCreate(customerEmail, "", ""));
        String actor = currentUserService.getCurrentUsername();
        if (currentUserService.isAdmin() && salespersonOverride != null && !salespersonOverride.isBlank()) {
            s.setSalespersonUsername(salespersonOverride);
        } else {
            s.setSalespersonUsername(actor);
        }
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
        LocalDateTime f = from != null ? from.atStartOfDay() : LocalDate.of(1970,1,1).atStartOfDay();
        LocalDateTime t = to != null ? to.plusDays(1).atStartOfDay().minusNanos(1) : LocalDate.of(9999,12,31).atTime(23,59,59);
        return saleRepository.findAllBySaleDateBetweenOrderBySaleDateDesc(f, t);
    }

    @Transactional(readOnly = true)
    public SaleSummaryDto summaryBetween(LocalDate from, LocalDate to) {
        LocalDateTime f = from != null ? from.atStartOfDay() : LocalDate.of(1970,1,1).atStartOfDay();
        LocalDateTime t = to != null ? to.plusDays(1).atStartOfDay().minusNanos(1) : LocalDate.of(9999,12,31).atTime(23,59,59);
        BigDecimal revenue = saleRepository.sumTotalBetween(f, t);
        if (revenue == null) revenue = BigDecimal.ZERO;
        long count = saleRepository.countBySaleDateBetween(f, t);
        return new SaleSummaryDto(count, revenue);
    }

    // Salesperson-aware range listing
    @Transactional(readOnly = true)
    public List<Sale> listRange(LocalDate from, LocalDate to, String salesperson) {
        LocalDateTime f = from != null ? from.atStartOfDay() : LocalDate.of(1970,1,1).atStartOfDay();
        LocalDateTime t = to != null ? to.plusDays(1).atStartOfDay().minusNanos(1) : LocalDate.of(9999,12,31).atTime(23,59,59);
        if (salesperson == null || salesperson.isBlank()) {
            if (from == null && to == null) return list();
            return saleRepository.findAllBySaleDateBetweenOrderBySaleDateDesc(f, t);
        }
        if (from == null && to == null) return saleRepository.findAllBySalespersonUsernameOrderBySaleDateDesc(salesperson);
        return saleRepository.findAllBySalespersonUsernameAndSaleDateBetweenOrderBySaleDateDesc(salesperson, f, t);
    }

    @Transactional(readOnly = true)
    public SaleSummaryDto summaryRange(LocalDate from, LocalDate to, String salesperson) {
        LocalDateTime f = from != null ? from.atStartOfDay() : LocalDate.of(1970,1,1).atStartOfDay();
        LocalDateTime t = to != null ? to.plusDays(1).atStartOfDay().minusNanos(1) : LocalDate.of(9999,12,31).atTime(23,59,59);
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
        java.util.Set<String> set = new java.util.LinkedHashSet<>();
        // Default known sales-capable users
        set.add("emp");
        set.add("seller");
        // From historical sales
        set.addAll(saleRepository.distinctSalespersons());
        // From seller profiles (admin-registered sellers)
        for (com.dealership.domain.SellerProfile p : sellerProfileRepository.findAll()) {
            if (p.getUsername() != null && !p.getUsername().isBlank()) set.add(p.getUsername());
        }
        return new java.util.ArrayList<>(set);
    }

    @Transactional(readOnly = true)
    public java.util.List<TopSellerDto> topSellers(LocalDate from, LocalDate to) {
        LocalDateTime f = from != null ? from.atStartOfDay() : LocalDate.of(1970,1,1).atStartOfDay();
        LocalDateTime t = to != null ? to.plusDays(1).atStartOfDay().minusNanos(1) : LocalDate.of(9999,12,31).atTime(23,59,59);
        java.util.List<Object[]> rows = saleRepository.topSellers(f, t);
        java.util.List<TopSellerDto> list = new java.util.ArrayList<>();
        for (Object[] r : rows) {
            String salesperson = (String) r[0];
            long totalSales = ((Number) r[1]).longValue();
            java.math.BigDecimal totalRevenue = (java.math.BigDecimal) r[2];
            list.add(new TopSellerDto(salesperson, totalSales, totalRevenue));
        }
        return list;
    }
}
