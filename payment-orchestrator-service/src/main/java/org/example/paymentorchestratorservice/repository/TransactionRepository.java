package org.example.paymentorchestratorservice.repository;

import org.example.paymentorchestratorservice.entity.TransactionState;
import org.example.paymentorchestratorservice.entity.Transactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transactions, UUID> {
    Optional<Transactions> findByIdempotencyKey(String idempotencyKey);
    List<Transactions> findByStatusAndUpdatedAtBefore(TransactionState status, LocalDateTime updatedAt);
}
