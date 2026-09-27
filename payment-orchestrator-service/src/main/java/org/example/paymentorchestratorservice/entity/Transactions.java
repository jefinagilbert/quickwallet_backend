package org.example.paymentorchestratorservice.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transactions {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionState status;

    @Column(name = "source_userid")
    private UUID sourceUserId;

    @Column(name = "source_account", nullable = false, length = 64)
    private String sourceAccount;

    @Column(name = "destination_account", nullable = false, length = 64)
    private String destinationAccount;

    @Column(nullable = false)
    private BigDecimal amount;

    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private int retryCount = 0;

    public Transactions() {}

    public Transactions(String idempotencyKey, UUID sourceUserId, String sourceAccount, String destinationAccount, BigDecimal amount, String description) {
        this.idempotencyKey = idempotencyKey;
        this.status = TransactionState.PENDING;
        this.sourceUserId = sourceUserId;
        this.sourceAccount = sourceAccount;
        this.destinationAccount = destinationAccount;
        this.amount = amount;
        this.description = description;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public TransactionState getStatus() {
        return status;
    }

    public void setStatus(TransactionState status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
    public UUID getSourceUserId() {
        return sourceUserId;
    }
    public BigDecimal getAmount() { return amount; }
    public int getRetryCount() { return retryCount; }
    public void incrementRetryCount() { this.retryCount++; }

    public String getSourceAccount() { return sourceAccount; }
    public String getDestinationAccount() { return destinationAccount; }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
