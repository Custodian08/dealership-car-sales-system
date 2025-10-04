package com.dealership.controller;

import com.dealership.dto.SaleSummaryDto;
import com.dealership.dto.SaleDto;
import com.dealership.service.SaleService;
import com.dealership.service.PdfService;
import com.dealership.service.VehicleService;
import com.dealership.domain.Sale;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import com.dealership.dto.TopSellerDto;
import com.dealership.dto.InventoryValuationDto;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final SaleService saleService;
    private final PdfService pdfService;
    private final VehicleService vehicleService;

    public ReportController(SaleService saleService, PdfService pdfService, VehicleService vehicleService) {
        this.saleService = saleService;
        this.pdfService = pdfService;
        this.vehicleService = vehicleService;
    }

    @GetMapping("/sales/summary")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE','ACCOUNTANT')")
    public SaleSummaryDto summary(@RequestParam(required = false) LocalDate from,
                                  @RequestParam(required = false) LocalDate to,
                                  @RequestParam(required = false) String salesperson) {
        if (salesperson != null && !salesperson.isBlank()) {
            return saleService.summaryRange(from, to, salesperson);
        }
        if (from == null && to == null) return saleService.summary();
        return saleService.summaryBetween(from, to);
    }

    @GetMapping("/sales")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE','ACCOUNTANT')")
    public List<SaleDto> list(@RequestParam(required = false) LocalDate from,
                              @RequestParam(required = false) LocalDate to,
                              @RequestParam(required = false) String salesperson) {
        List<Sale> sales = saleService.listRange(from, to, salesperson);
        return sales.stream().map(ReportController::toDto).toList();
    }

    @GetMapping(value = "/sales.csv", produces = "text/csv")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE','ACCOUNTANT')")
    public ResponseEntity<byte[]> exportCsv(@RequestParam(required = false) LocalDate from,
                                            @RequestParam(required = false) LocalDate to,
                                            @RequestParam(required = false) String salesperson) {
        List<Sale> sales = saleService.listRange(from, to, salesperson);
        StringBuilder sb = new StringBuilder();
        sb.append("saleDate,vin,make,model,year,customer,customerEmail,price,salesperson\n");
        for (Sale s : sales) {
            String customer = safe(s.getCustomer().getFirstName()) + " " + safe(s.getCustomer().getLastName());
            sb.append(s.getSaleDate()).append(',')
              .append(csv(s.getVehicle().getVin())).append(',')
              .append(csv(s.getVehicle().getMake())).append(',')
              .append(csv(s.getVehicle().getModel())).append(',')
              .append(s.getVehicle().getYear()).append(',')
              .append(csv(customer)).append(',')
              .append(csv(s.getCustomer().getEmail())).append(',')
              .append(s.getPrice()).append(',')
              .append(csv(s.getSalespersonUsername())).append('\n');
        }
        byte[] body = sb.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=sales.csv")
                .contentType(MediaType.valueOf("text/csv"))
                .body(body);
    }

    @GetMapping(value = "/sales.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE','ACCOUNTANT')")
    public ResponseEntity<byte[]> exportPdf(@RequestParam(required = false) LocalDate from,
                                            @RequestParam(required = false) LocalDate to,
                                            @RequestParam(required = false) String salesperson) {
        List<Sale> sales = saleService.listRange(from, to, salesperson);
        SaleSummaryDto summary = saleService.summaryRange(from, to, salesperson);
        byte[] pdf = pdfService.generateSalesReport(sales, summary, from, to);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=sales-report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/salespersons")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE','ACCOUNTANT')")
    public List<String> listSalespersons() {
        return saleService.listSalespersons();
    }

    // Top sellers report
    @GetMapping("/top-sellers")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE','ACCOUNTANT')")
    public List<TopSellerDto> topSellers(@RequestParam(required = false) LocalDate from,
                                         @RequestParam(required = false) LocalDate to) {
        return saleService.topSellers(from, to);
    }

    // Inventory valuation report
    @GetMapping("/inventory/valuation")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE','ACCOUNTANT','LISTER')")
    public InventoryValuationDto inventoryValuation() {
        return vehicleService.inventoryValuation();
    }

    private static String safe(String s) { return s == null ? "" : s; }
    private static String csv(String s) { return '"' + (s == null ? "" : s.replace("\"", "\"\"")) + '"'; }

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
