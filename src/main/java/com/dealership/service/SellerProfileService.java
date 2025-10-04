package com.dealership.service;

import com.dealership.domain.SellerType;
import com.dealership.dto.SellerProfileDto;
import org.springframework.stereotype.Service;

import com.dealership.repo.SellerProfileRepository;
import com.dealership.domain.SellerProfile;

import java.util.ArrayList;

@Service
public class SellerProfileService {
    private final SellerProfileRepository repo;

    public SellerProfileService(SellerProfileRepository repo) {
        this.repo = repo;
    }

    public java.util.List<SellerProfileDto> list() {
        java.util.List<SellerProfileDto> out = new ArrayList<>();
        for (SellerProfile p : repo.findAll()) out.add(toDto(p));
        return out;
    }

    public SellerProfileDto get(String username) {
        if (username == null) return null;
        return repo.findById(username).map(this::toDto).orElse(null);
    }

    public SellerProfileDto upsert(SellerProfileDto dto) {
        if (dto == null || dto.username() == null || dto.username().isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        SellerProfile e = repo.findById(dto.username()).orElseGet(SellerProfile::new);
        e.setUsername(dto.username());
        e.setType(dto.type());
        e.setPhone(dto.phone());
        e.setEmail(dto.email());
        e.setFirstName(dto.firstName());
        e.setLastName(dto.lastName());
        e.setCompanyName(dto.companyName());
        e.setInn(dto.inn());
        e.setKpp(dto.kpp());
        e.setAddress(dto.address());
        e.setContactName(dto.contactName());
        return toDto(repo.save(e));
    }

    public String displayName(String username) {
        if (username == null || username.isBlank()) return null;
        SellerProfileDto p = get(username);
        if (p == null) return null; // profile deleted -> show blank in UI
        if (p.type() == SellerType.PERSON) {
            String fn = p.firstName() != null ? p.firstName() : "";
            String ln = p.lastName() != null ? p.lastName() : "";
            String full = (fn + " " + ln).trim();
            return full.isBlank() ? null : full;
        } else {
            String company = p.companyName();
            return (company != null && !company.isBlank()) ? company : null;
        }
    }

    private SellerProfileDto toDto(SellerProfile e) {
        return new SellerProfileDto(
                e.getUsername(),
                e.getType(),
                e.getPhone(),
                e.getEmail(),
                e.getFirstName(),
                e.getLastName(),
                e.getCompanyName(),
                e.getInn(),
                e.getKpp(),
                e.getAddress(),
                e.getContactName()
        );
    }

    public void delete(String username) {
        if (username == null || username.isBlank()) return;
        repo.deleteById(username);
    }
}
