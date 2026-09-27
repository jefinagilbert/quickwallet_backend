package org.example.walletservice.entity;


import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "wallets")
public class WalletEntity {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(nullable = false, length = 64)
    private String name;

    @Column(nullable = false)
    private  BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    private AccountStatus accountStatus;

    private LocalDateTime createdAt;

    public WalletEntity() {}

    public WalletEntity(UUID userId, String name, BigDecimal balance) {
        this.userId = userId;
        this.name = name;
        this.balance = balance;
        this.createdAt = LocalDateTime.now();
        this.accountStatus = AccountStatus.PENDING;
    }

    @PrePersist
    public void generatePrefixedId() {
        if (this.id == null) {
            this.id = "wal_" + UUID.randomUUID().toString().replace("-", "");
        }
    }

    public String getId() {
        return this.id;
    }
    public BigDecimal getBalance() {
        return this.balance;
    }
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public AccountStatus getAccountStatus() {
        return accountStatus;
    }
    public void setAccountStatus(AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
    }
}
