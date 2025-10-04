package com.dealership.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "seller_profiles")
public class SellerProfile {
    @Id
    @Column(length = 100, nullable = false, updatable = false)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private SellerType type;

    @Column(length = 100)
    private String phone;

    @Column(length = 200)
    private String email;

    // PERSON
    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    // COMPANY
    @Column(name = "company_name", length = 200)
    private String companyName;

    @Column(length = 32)
    private String inn;

    @Column(length = 32)
    private String kpp;

    @Column(length = 500)
    private String address;

    @Column(name = "contact_name", length = 200)
    private String contactName;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public SellerType getType() { return type; }
    public void setType(SellerType type) { this.type = type; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getInn() { return inn; }
    public void setInn(String inn) { this.inn = inn; }
    public String getKpp() { return kpp; }
    public void setKpp(String kpp) { this.kpp = kpp; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
}
