package com.dealership.repo;

import com.dealership.domain.Interaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InteractionRepository extends JpaRepository<Interaction, UUID> {
    List<Interaction> findByCustomer_IdOrderByOccurredAtDesc(UUID customerId);
}
