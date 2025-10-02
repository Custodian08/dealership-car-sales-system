package com.dealership.service;

import com.dealership.domain.Customer;
import com.dealership.repo.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
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
}
