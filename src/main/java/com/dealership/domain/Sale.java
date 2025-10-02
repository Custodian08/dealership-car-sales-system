package com.dealership.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sales")
public class Sale {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    private Vehicle vehicle;

    @ManyToOne(optional = false)
    private Customer customer;

    @Column(nullable = false)
    private String salespersonUsername;

    private BigDecimal price;

    private LocalDateTime saleDate = LocalDateTime.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public String getSalespersonUsername() { return salespersonUsername; }
    public void setSalespersonUsername(String salespersonUsername) { this.salespersonUsername = salespersonUsername; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public LocalDateTime getSaleDate() { return saleDate; }
    public void setSaleDate(LocalDateTime saleDate) { this.saleDate = saleDate; }
}
