package com.dealership.service;

import com.dealership.domain.Customer;
import com.dealership.domain.Interaction;
import com.dealership.domain.Vehicle;
import com.dealership.dto.InteractionDto;
import com.dealership.exception.NotFoundException;
import com.dealership.repo.CustomerRepository;
import com.dealership.repo.InteractionRepository;
import com.dealership.repo.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class InteractionService {
    private final InteractionRepository interactionRepository;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final CurrentUserService currentUserService;

    public InteractionService(InteractionRepository interactionRepository,
                              CustomerRepository customerRepository,
                              VehicleRepository vehicleRepository,
                              CurrentUserService currentUserService) {
        this.interactionRepository = interactionRepository;
        this.customerRepository = customerRepository;
        this.vehicleRepository = vehicleRepository;
        this.currentUserService = currentUserService;
    }

    public List<Interaction> listForCustomer(UUID customerId) {
        return interactionRepository.findByCustomer_IdOrderByOccurredAtDesc(customerId);
    }

    @Transactional
    public Interaction create(UUID customerId, InteractionDto req) {
        Customer c = customerRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Customer not found"));
        Vehicle v = null;
        if (req.vehicleId() != null) {
            v = vehicleRepository.findById(req.vehicleId())
                    .orElseThrow(() -> new NotFoundException("Vehicle not found"));
        }
        Interaction i = new Interaction();
        i.setCustomer(c);
        i.setVehicle(v);
        i.setType(req.type());
        i.setNotes(req.notes());
        i.setOccurredAt(req.occurredAt() != null ? req.occurredAt() : LocalDateTime.now());
        i.setEmployeeUsername(currentUserService.getCurrentUsername());
        return interactionRepository.save(i);
    }
}
