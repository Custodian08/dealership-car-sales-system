package com.dealership.service;

import com.dealership.domain.Customer;
import com.dealership.domain.Interaction;
import com.dealership.domain.InteractionType;
import com.dealership.domain.Vehicle;
import com.dealership.dto.InteractionDto;
import com.dealership.exception.NotFoundException;
import com.dealership.repo.CustomerRepository;
import com.dealership.repo.InteractionRepository;
import com.dealership.repo.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
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

    public List<Interaction> listForCustomer(UUID customerId, LocalDate from, LocalDate to, InteractionType type) {
        if (from == null && to == null && type == null) {
            return listForCustomer(customerId);
        }
        // Use DB-safe bounds (avoid LocalDate.MIN/MAX which overflow JDBC/DB timestamp range)
        LocalDateTime f = from != null ? from.atStartOfDay() : LocalDate.of(1970,1,1).atStartOfDay();
        LocalDateTime t = to != null ? to.plusDays(1).atStartOfDay().minusNanos(1) : LocalDate.of(9999,12,31).atTime(23,59,59);
        if (type == null) {
            return interactionRepository.findByCustomer_IdAndOccurredAtBetweenOrderByOccurredAtDesc(customerId, f, t);
        }
        return interactionRepository.findByCustomer_IdAndTypeAndOccurredAtBetweenOrderByOccurredAtDesc(customerId, type, f, t);
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

    @Transactional
    public Interaction update(UUID customerId, UUID id, InteractionDto req) {
        Interaction i = getForCustomerOrThrow(customerId, id);
        // Update allowed fields
        if (req.vehicleId() != null) {
            Vehicle v = vehicleRepository.findById(req.vehicleId())
                    .orElseThrow(() -> new NotFoundException("Vehicle not found"));
            i.setVehicle(v);
        } else {
            i.setVehicle(null);
        }
        if (req.type() != null) i.setType(req.type());
        i.setNotes(req.notes());
        if (req.occurredAt() != null) i.setOccurredAt(req.occurredAt());
        // Keep original employeeUsername
        return interactionRepository.save(i);
    }

    @Transactional
    public void delete(UUID customerId, UUID id) {
        Interaction i = getForCustomerOrThrow(customerId, id);
        interactionRepository.delete(i);
    }

    private Interaction getForCustomerOrThrow(UUID customerId, UUID id) {
        Interaction i = interactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Interaction not found"));
        if (!i.getCustomer().getId().equals(customerId)) {
            throw new NotFoundException("Interaction not found");
        }
        return i;
    }
}
