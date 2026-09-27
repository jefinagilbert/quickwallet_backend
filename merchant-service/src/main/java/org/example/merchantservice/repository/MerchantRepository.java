package org.example.merchantservice.repository;
import org.example.merchantservice.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, String> {
    Optional<Merchant> findByUserId(UUID userId);
    Optional<Merchant> findByIdAndUserId(String id, UUID userId);
}
