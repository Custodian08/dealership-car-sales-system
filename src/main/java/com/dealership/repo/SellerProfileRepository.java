package com.dealership.repo;

import com.dealership.domain.SellerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerProfileRepository extends JpaRepository<SellerProfile, String> {
}
