package com.dealership.controller;

import com.dealership.dto.UserAccountDto;
import com.dealership.dto.UserUpsertRequest;
import com.dealership.service.UsersService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UsersController {
    private final UsersService usersService;

    public UsersController(UsersService usersService) {
        this.usersService = usersService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserAccountDto> list() {
        return usersService.list();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public void create(@RequestBody UserUpsertRequest req) {
        usersService.create(req);
    }

    @PutMapping("/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public void update(@PathVariable String username, @RequestBody UserUpsertRequest req) {
        usersService.update(username, req);
    }

    @DeleteMapping("/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable String username) {
        usersService.delete(username);
    }
}
