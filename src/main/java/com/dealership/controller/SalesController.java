package com.dealership.controller;

import com.dealership.domain.Sale;
import com.dealership.dto.SaleDto;
import com.dealership.service.PdfService;
import com.dealership.service.SaleService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sales")
public class SalesController {
    private final SaleService saleService;
    private final PdfService pdfService;

    public SalesController(SaleService saleService, PdfService pdfService) {
        this.saleService = saleService;
        this.pdfService = pdfService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE','ACCOUNTANT')")
    public List<SaleDto> list() {
        return saleService.list().stream().map(SalesController::toDto).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE','ACCOUNTANT')")
    public SaleDto get(@PathVariable UUID id) {
        return toDto(saleService.getOrThrow(id));
    }

    @GetMapping(value = "/{id}/contract.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE','ACCOUNTANT')")
    public ResponseEntity<byte[]> contract(@PathVariable UUID id) {
        Sale sale = saleService.getOrThrow(id);
        byte[] pdf = pdfService.generateSaleContract(sale);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=contract-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    private static SaleDto toDto(Sale s) {
        return new SaleDto(
                s.getId(),
                s.getVehicle().getVin(),
                s.getVehicle().getMake(),
                s.getVehicle().getModel(),
                s.getVehicle().getYear(),
                s.getCustomer().getFirstName(),
                s.getCustomer().getLastName(),
                s.getCustomer().getEmail(),
                s.getPrice(),
                s.getSalespersonUsername(),
                s.getSaleDate()
        );
    }
}
