package com.dealership;

import com.dealership.dto.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IntegrationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    private TestRestTemplate as(String username, String password) {
        return restTemplate.withBasicAuth(username, password);
    }

    @Test
    void reserve_cancel_sell_and_reports() {
        // ensure a seller profile exists for owner linkage
        SellerProfileDto sp = new SellerProfileDto(
                "seller",
                com.dealership.domain.SellerType.PERSON,
                null, null,
                "Test", "Seller",
                null, null, null, null, null
        );
        as("admin","admin").exchange("/api/sellers/seller", HttpMethod.PUT, new HttpEntity<>(sp), SellerProfileDto.class);
        // 1) Create a new vehicle as admin
        String vin = "TST-" + UUID.randomUUID();
        VehicleUpsertRequest vreq = new VehicleUpsertRequest(
                vin, "TestMake", "TestModel", 2024, new BigDecimal("12345.67"), null, "seller"
        );
        ResponseEntity<VehicleDto> vCreate = as("admin", "admin").postForEntity("/api/vehicles", vreq, VehicleDto.class);
        assertThat(vCreate.getStatusCode().is2xxSuccessful()).isTrue();
        VehicleDto vehicle = vCreate.getBody();
        assertThat(vehicle).isNotNull();
        assertThat(vehicle.vin()).isEqualTo(vin);

        // 2) Reserve as seller
        ReserveRequest rreq = new ReserveRequest("buyer@example.com", "Alice", "Buyer", new BigDecimal("100.00"), 30L);
        ResponseEntity<VehicleDto> vRes = as("seller", "seller").postForEntity("/api/vehicles/" + vehicle.id() + "/reserve", rreq, VehicleDto.class);
        assertThat(vRes.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(vRes.getBody()).isNotNull();
        assertThat(vRes.getBody().status()).isEqualToIgnoringCase("RESERVED");

        // 3) Cancel reservation as seller
        ResponseEntity<VehicleDto> vCancel = as("seller", "seller").postForEntity("/api/vehicles/" + vehicle.id() + "/cancel-reservation", null, VehicleDto.class);
        assertThat(vCancel.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(vCancel.getBody()).isNotNull();
        assertThat(vCancel.getBody().status()).isEqualToIgnoringCase("AVAILABLE");

        // 4) Sell as seller
        SellRequest sreq = new SellRequest("buyer@example.com", new BigDecimal("12000.00"), null);
        ResponseEntity<VehicleDto> vSell = as("seller", "seller").postForEntity("/api/vehicles/" + vehicle.id() + "/sell", sreq, VehicleDto.class);
        assertThat(vSell.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(vSell.getBody()).isNotNull();
        assertThat(vSell.getBody().status()).isEqualToIgnoringCase("SOLD");

        // 5) Reports as admin (seller is not authorized for listing sales)
        LocalDate from = LocalDate.now().minusDays(1);
        LocalDate to = LocalDate.now().plusDays(1);

        ResponseEntity<List<SaleDto>> salesResp = as("admin", "admin").exchange(
                "/api/reports/sales?from=" + from + "&to=" + to,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<SaleDto>>() {}
        );
        assertThat(salesResp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(salesResp.getBody()).isNotEmpty();
        boolean foundSoldVin = salesResp.getBody().stream().anyMatch(s -> vin.equalsIgnoreCase(s.vehicleVin()));
        assertThat(foundSoldVin).isTrue();

        // Top sellers
        ResponseEntity<List<TopSellerDto>> topResp = as("admin", "admin").exchange(
                "/api/reports/top-sellers?from=" + from + "&to=" + to,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<TopSellerDto>>() {}
        );
        assertThat(topResp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(topResp.getBody()).isNotEmpty();
        boolean hasSeller = topResp.getBody().stream().anyMatch(t -> "seller".equalsIgnoreCase(t.salesperson()) && t.totalSales() >= 1);
        assertThat(hasSeller).isTrue();

        // Inventory valuation
        ResponseEntity<InventoryValuationDto> invResp = as("admin", "admin").getForEntity("/api/reports/inventory/valuation", InventoryValuationDto.class);
        assertThat(invResp.getStatusCode().is2xxSuccessful()).isTrue();
        InventoryValuationDto inv = invResp.getBody();
        assertThat(inv).isNotNull();
        assertThat(inv.soldCount()).isGreaterThanOrEqualTo(1);
        assertThat(inv.totalValue()).isNotNull();
    }
}
