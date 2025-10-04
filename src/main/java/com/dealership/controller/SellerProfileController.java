package com.dealership.controller;

import com.dealership.dto.SellerProfileDto;
import com.dealership.service.SellerProfileService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sellers")
public class SellerProfileController {
    private final SellerProfileService service;

    public SellerProfileController(SellerProfileService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE','SALESPERSON','ACCOUNTANT','LISTER')")
    public List<SellerProfileDto> list() {
        return service.list();
    }

    @GetMapping("/{username}")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE','SALESPERSON','ACCOUNTANT','LISTER')")
    public SellerProfileDto get(@PathVariable String username) {
        SellerProfileDto dto = service.get(username);
        if (dto == null) throw new IllegalArgumentException("Seller profile not found");
        return dto;
    }

    @PutMapping("/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public SellerProfileDto upsert(@PathVariable String username, @RequestBody @Validated SellerProfileDto body) {
        // Enforce path username
        SellerProfileDto fixed = new SellerProfileDto(
                username,
                body.type(),
                body.phone(),
                body.email(),
                body.firstName(),
                body.lastName(),
                body.companyName(),
                body.inn(),
                body.kpp(),
                body.address(),
                body.contactName()
        );
        return service.upsert(fixed);
    }

    @DeleteMapping("/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable String username) {
        service.delete(username);
    }
}
