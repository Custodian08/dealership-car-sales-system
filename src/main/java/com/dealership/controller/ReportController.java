package com.dealership.controller;

import com.dealership.dto.SaleSummaryDto;
import com.dealership.service.SaleService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final SaleService saleService;

    public ReportController(SaleService saleService) {
        this.saleService = saleService;
    }

    @GetMapping("/sales/summary")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public SaleSummaryDto summary() {
        return saleService.summary();
    }
}
