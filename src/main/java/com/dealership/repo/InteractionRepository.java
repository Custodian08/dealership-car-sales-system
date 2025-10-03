package com.dealership.repo;

import com.dealership.domain.Interaction;
import com.dealership.domain.InteractionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;

public interface InteractionRepository extends JpaRepository<Interaction, UUID> {
    List<Interaction> findByCustomer_IdOrderByOccurredAtDesc(UUID customerId);
    List<Interaction> findByCustomer_IdAndOccurredAtBetweenOrderByOccurredAtDesc(UUID customerId, LocalDateTime from, LocalDateTime to);
    List<Interaction> findByCustomer_IdAndTypeAndOccurredAtBetweenOrderByOccurredAtDesc(UUID customerId, InteractionType type, LocalDateTime from, LocalDateTime to);
    boolean existsByCustomer_Id(UUID customerId);
}
