package com.dealership.repo;

import com.dealership.domain.Customer;
import com.dealership.domain.Sale;
import com.dealership.domain.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;

public interface SaleRepository extends JpaRepository<Sale, UUID> {
    Optional<Sale> findByVehicle(Vehicle vehicle);

    @Query("select sum(s.price) from Sale s")
    BigDecimal sumTotal();

    @Query("select count(s) from Sale s")
    long countAll();

    boolean existsByCustomer(Customer customer);

    java.util.List<Sale> findAllByOrderBySaleDateDesc();

    java.util.List<Sale> findAllBySaleDateBetweenOrderBySaleDateDesc(LocalDateTime from, LocalDateTime to);

    @Query("select sum(s.price) from Sale s where s.saleDate between :from and :to")
    BigDecimal sumTotalBetween(LocalDateTime from, LocalDateTime to);

    long countBySaleDateBetween(LocalDateTime from, LocalDateTime to);
}
