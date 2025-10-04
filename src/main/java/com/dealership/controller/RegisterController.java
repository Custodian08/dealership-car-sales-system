package com.dealership.controller;

import com.dealership.dto.RegisterRequest;
import com.dealership.dto.SellerProfileDto;
import com.dealership.service.SellerProfileService;
import jakarta.validation.Valid;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
public class RegisterController {
    private final JdbcTemplate jdbc;
    private final SellerProfileService sellerProfiles;

    public RegisterController(JdbcTemplate jdbc, SellerProfileService sellerProfiles) {
        this.jdbc = jdbc;
        this.sellerProfiles = sellerProfiles;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@RequestBody @Valid RegisterRequest req) {
        // 1) Create user with ROLE_SALESPERSON
        // Use {noop} to stay compatible with existing seeded users
        String encoded = "{noop}" + req.password();
        // Ensure not exists
        Integer cnt = jdbc.queryForObject("select count(*) from users where username=?", Integer.class, req.username());
        if (cnt != null && cnt > 0) throw new DuplicateKeyException("Username already exists");
        jdbc.update("insert into users(username,password,enabled) values(?,?,true)", req.username(), encoded);
        jdbc.update("insert into authorities(username,authority) values(?,?)", req.username(), "ROLE_SALESPERSON");

        // 2) Upsert seller profile
        SellerProfileDto profile = new SellerProfileDto(
                req.username(),
                req.type(),
                req.phone(),
                req.email(),
                req.firstName(),
                req.lastName(),
                req.companyName(),
                req.inn(),
                req.kpp(),
                req.address(),
                req.contactName()
        );
        sellerProfiles.upsert(profile);
    }
}
