package org.example.merchantservice.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "merchants")
public class Merchant {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "user_id", unique = true, nullable = false)
    private UUID userId;

    @Column(name = "business_name", nullable = false)
    private String businessName;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    private AccountStatus accountStatus;

    @Column(nullable = false)
    private BigDecimal balance;

    private LocalDateTime createdAt;

    public Merchant() {}

    public Merchant(UUID userId, String businessName) {
        this.userId = userId;
        this.businessName = businessName;
        this.balance = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
        this.accountStatus = AccountStatus.PENDING;
    }

    @PrePersist
    private void generateId() {
        if (this.id == null) {
            this.id = "mer_" + UUID.randomUUID().toString().replace("-", "");
        }
    }

    public String getId() {
        return id;
    }

    public UUID getUserId() {
        return this.userId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getBusinessName() {
        return this.businessName;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getBalance() {
        return this.balance;
    }
}
