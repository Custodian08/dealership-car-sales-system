package com.dealership.config;

import com.dealership.domain.*;
import com.dealership.repo.CustomerRepository;
import com.dealership.repo.VehicleRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.math.BigDecimal;

@Configuration
public class DataInitializer {

    private final VehicleRepository vehicles;
    private final CustomerRepository customers;

    public DataInitializer(VehicleRepository vehicles, CustomerRepository customers) {
        this.vehicles = vehicles;
        this.customers = customers;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seed() {
        // Seeding disabled for now to isolate startup issue.
        // You can re-enable later by adding seed data here or via Flyway/data.sql.
    }
}
