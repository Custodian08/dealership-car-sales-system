package com.dealership.controller;

import com.dealership.domain.Interaction;
import com.dealership.domain.InteractionType;
import com.dealership.dto.InteractionDto;
import com.dealership.service.InteractionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.UUID;
import java.time.LocalDate;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/customers/{customerId}/interactions")
public class InteractionController {
    private final InteractionService interactionService;

    public InteractionController(InteractionService interactionService) {
        this.interactionService = interactionService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE','ACCOUNTANT','SALESPERSON')")
    public List<InteractionDto> list(@PathVariable UUID customerId,
                                     @RequestParam(required = false) LocalDate from,
                                     @RequestParam(required = false) LocalDate to,
                                     @RequestParam(required = false) InteractionType type) {
        return interactionService.listForCustomer(customerId, from, to, type).stream().map(InteractionController::toDto).toList();
    }

    @GetMapping(value = "/export.csv", produces = "text/csv")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE','ACCOUNTANT','SALESPERSON')")
    public ResponseEntity<byte[]> exportCsv(@PathVariable UUID customerId,
                                            @RequestParam(required = false) LocalDate from,
                                            @RequestParam(required = false) LocalDate to,
                                            @RequestParam(required = false) InteractionType type) {
        List<Interaction> list = interactionService.listForCustomer(customerId, from, to, type);
        StringBuilder sb = new StringBuilder();
        sb.append("occurredAt,type,notes,vehicleId,employeeUsername\n");
        for (Interaction i : list) {
            sb.append(i.getOccurredAt()).append(',')
              .append(csv(i.getType() != null ? i.getType().name() : "" )).append(',')
              .append(csv(i.getNotes())).append(',')
              .append(i.getVehicle() != null ? i.getVehicle().getId() : "").append(',')
              .append(csv(i.getEmployeeUsername())).append('\n');
        }
        byte[] body = sb.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=interactions-" + customerId + ".csv")
                .contentType(MediaType.valueOf("text/csv"))
                .body(body);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE','SALESPERSON')")
    public InteractionDto create(@PathVariable UUID customerId, @RequestBody @Validated InteractionDto req) {
        Interaction i = interactionService.create(customerId, req);
        return toDto(i);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE','SALESPERSON')")
    public InteractionDto update(@PathVariable UUID customerId, @PathVariable UUID id, @RequestBody @Validated InteractionDto req) {
        Interaction i = interactionService.update(customerId, id, req);
        return toDto(i);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE','SALESPERSON')")
    public ResponseEntity<Void> delete(@PathVariable UUID customerId, @PathVariable UUID id) {
        interactionService.delete(customerId, id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
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

    private static String csv(String s) { return '"' + (s == null ? "" : s.replace("\"", "\"\"")) + '"'; }
}
