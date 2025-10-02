package com.dealership.controller;

import com.dealership.domain.Interaction;
import com.dealership.dto.InteractionDto;
import com.dealership.service.InteractionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers/{customerId}/interactions")
public class InteractionController {
    private final InteractionService interactionService;

    public InteractionController(InteractionService interactionService) {
        this.interactionService = interactionService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE','ACCOUNTANT')")
    public List<InteractionDto> list(@PathVariable UUID customerId) {
        return interactionService.listForCustomer(customerId).stream().map(InteractionController::toDto).toList();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public InteractionDto create(@PathVariable UUID customerId, @RequestBody @Validated InteractionDto req) {
        Interaction i = interactionService.create(customerId, req);
        return toDto(i);
    }

    private static InteractionDto toDto(Interaction i) {
        return new InteractionDto(
                i.getId(),
                i.getCustomer().getId(),
                i.getVehicle() != null ? i.getVehicle().getId() : null,
                i.getType(),
                i.getNotes(),
                i.getOccurredAt(),
                i.getEmployeeUsername()
        );
    }
}
