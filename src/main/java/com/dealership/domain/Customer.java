package com.dealership.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

@Entity
@Table(name = "customers", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class Customer {
    @Id
    @GeneratedValue
    private UUID id;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @Email
    @Column(nullable = false, unique = true)
    private String email;

    private String phone;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
