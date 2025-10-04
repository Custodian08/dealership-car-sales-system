package com.dealership.controller;

import com.dealership.domain.Customer;
import com.dealership.dto.CustomerDto;
import com.dealership.dto.CustomerUpsertRequest;
import com.dealership.service.CustomerService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE','GUEST','LISTER','SALESPERSON','ACCOUNTANT')")
    public List<CustomerDto> list(@RequestParam(name = "query", required = false) String query) {
        return customerService.list(query).stream().map(CustomerController::toDto).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE','GUEST','LISTER','SALESPERSON','ACCOUNTANT')")
    public CustomerDto get(@PathVariable UUID id) {
        Customer c = customerService.getOrThrow(id);
        return toDto(c);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public CustomerDto create(@RequestBody @Validated CustomerUpsertRequest req) {
        return toDto(customerService.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public CustomerDto update(@PathVariable UUID id, @RequestBody @Validated CustomerUpsertRequest req) {
        return toDto(customerService.update(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public void delete(@PathVariable UUID id) {
        customerService.delete(id);
    }

    private static CustomerDto toDto(Customer c) {
        return new CustomerDto(
                c.getId(), c.getFirstName(), c.getLastName(), c.getEmail(), c.getPhone(),
                c.getPassportNumber(), c.getPassportIssuedBy(), c.getPassportIssueDate(), c.getAddress()
        );
    }
}
