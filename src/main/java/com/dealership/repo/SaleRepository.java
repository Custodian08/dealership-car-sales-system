package com.dealership.repo;

import com.dealership.domain.Sale;
import com.dealership.domain.Vehicle;
import com.dealership.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface SaleRepository extends JpaRepository<Sale, UUID> {
    Optional<Sale> findByVehicle(Vehicle vehicle);

    @Query("select sum(s.price) from Sale s")
    BigDecimal sumTotal();

    @Query("select count(s) from Sale s")
    long countAll();

    boolean existsByCustomer(Customer customer);
}
