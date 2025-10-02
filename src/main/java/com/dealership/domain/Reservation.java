package com.dealership.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reservations", indexes = {
        @Index(columnList = "vehicle_id,status")
})
public class Reservation {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    private Vehicle vehicle;

    @ManyToOne(optional = false)
    private Customer customer;

    @Column(nullable = false)
    private String reservedByUsername;

    private BigDecimal deposit = BigDecimal.ZERO;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status = ReservationStatus.ACTIVE;

    @Version
    private Integer version;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public String getReservedByUsername() { return reservedByUsername; }
    public void setReservedByUsername(String reservedByUsername) { this.reservedByUsername = reservedByUsername; }
    public BigDecimal getDeposit() { return deposit; }
    public void setDeposit(BigDecimal deposit) { this.deposit = deposit; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
}
