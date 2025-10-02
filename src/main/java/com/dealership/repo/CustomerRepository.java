package com.dealership.repo;

import com.dealership.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByEmail(String email);

    @Query("""
        select distinct c from Customer c
        where lower(c.firstName) like lower(concat('%', ?1, '%'))
           or lower(c.lastName) like lower(concat('%', ?1, '%'))
           or lower(c.email) like lower(concat('%', ?1, '%'))
           or lower(c.phone) like lower(concat('%', ?1, '%'))
           or exists (select 1 from Sale s where s.customer = c and lower(s.vehicle.vin) like lower(concat('%', ?1, '%')))
           or exists (select 1 from Reservation r where r.customer = c and lower(r.vehicle.vin) like lower(concat('%', ?1, '%')))
        """)
    List<Customer> search(String q);
}
