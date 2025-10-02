package com.dealership.service;

import com.dealership.domain.Customer;
import com.dealership.domain.ReservationStatus;
import com.dealership.repo.CustomerRepository;
import com.dealership.repo.ReservationRepository;
import com.dealership.repo.SaleRepository;
import com.dealership.dto.CustomerUpsertRequest;
import com.dealership.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final ReservationRepository reservationRepository;
    private final SaleRepository saleRepository;

    public CustomerService(CustomerRepository customerRepository,
                           ReservationRepository reservationRepository,
                           SaleRepository saleRepository) {
        this.customerRepository = customerRepository;
        this.reservationRepository = reservationRepository;
        this.saleRepository = saleRepository;
    }

    @Transactional
    public Customer getOrCreate(String email, String firstName, String lastName) {
        return customerRepository.findByEmail(email).orElseGet(() -> {
            Customer c = new Customer();
            c.setEmail(email);
            c.setFirstName(firstName != null ? firstName : "");
            c.setLastName(lastName != null ? lastName : "");
            return customerRepository.save(c);
        });
    }

    public List<Customer> list(String query) {
        if (query == null || query.isBlank()) return customerRepository.findAll();
        return customerRepository.search(query.trim());
    }

    public Customer getOrThrow(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer not found"));
    }

    @Transactional
    public Customer create(CustomerUpsertRequest req) {
        customerRepository.findByEmail(req.email()).ifPresent(c -> {
            throw new IllegalStateException("Customer with email already exists");
        });
        Customer c = new Customer();
        apply(c, req);
        return customerRepository.save(c);
    }

    @Transactional
    public Customer update(UUID id, CustomerUpsertRequest req) {
        Customer c = getOrThrow(id);
        customerRepository.findByEmail(req.email()).ifPresent(other -> {
            if (!other.getId().equals(id)) throw new IllegalStateException("Email already used by another customer");
        });
        apply(c, req);
        return customerRepository.save(c);
    }

    @Transactional
    public void delete(UUID id) {
        Customer c = getOrThrow(id);
        if (reservationRepository.existsByCustomer_IdAndStatus(id, ReservationStatus.ACTIVE)) {
            throw new IllegalStateException("Cannot delete customer with active reservation");
        }
        if (reservationRepository.existsByCustomer_Id(id)) {
            throw new IllegalStateException("Cannot delete customer with reservation history");
        }
        if (saleRepository.existsByCustomer(c)) {
            throw new IllegalStateException("Cannot delete customer with sales history");
        }
        customerRepository.delete(c);
    }

    private static void apply(Customer c, CustomerUpsertRequest req) {
        c.setFirstName(req.firstName());
        c.setLastName(req.lastName());
        c.setEmail(req.email());
        c.setPhone(req.phone());
        c.setPassportNumber(req.passportNumber());
        c.setPassportIssuedBy(req.passportIssuedBy());
        c.setPassportIssueDate(req.passportIssueDate());
        c.setAddress(req.address());
    }
}
