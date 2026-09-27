package org.example.walletservice.service;

import org.example.walletservice.entity.WalletEntity;
import org.example.walletservice.exception.InsufficientBalanceException;
import org.example.walletservice.exception.WalletNotFoundException;
import org.example.walletservice.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class WalletService {

    private final WalletRepository walletRepository;

    public WalletService (WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Transactional
    public WalletEntity initializeWallet(UUID userId, String name) {
        if(this.walletRepository.findByUserId(userId).isPresent()) {
            throw new RuntimeException("Wallet already exists");
        }
        WalletEntity walletEntity = new WalletEntity(userId, name, BigDecimal.ZERO);
        return walletRepository.save(walletEntity);
    }

    @Transactional // readonly-true
    public BigDecimal getBalance(UUID userId, String walletId) {
        WalletEntity wallet = this.walletRepository.findByIdAndUserId(walletId, userId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found: " + walletId));
        return wallet.getBalance();
    }

    @Transactional
    public WalletEntity addFunds(UUID userId, String walletId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        WalletEntity wallet = walletRepository.findByIdAndUserId(walletId, userId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user: " + userId));

        wallet.setBalance(wallet.getBalance().add(amount));
        return walletRepository.save(wallet);
    }

    @Transactional
    public void debitFunds(UUID userId, String walletId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Debit amount must be positive");
        }
        WalletEntity wallet = walletRepository.findByIdAndUserId(walletId, userId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user: " + userId));

        int rowsUpdated = walletRepository.decrementBalanceAtomic(walletId, amount);
        if(rowsUpdated == 0) {
            throw new InsufficientBalanceException("Insufficient Balance for wallet: " + walletId);
        }
    }

    @Transactional
    public void creditFunds(UUID userId, String walletId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Credit amount must be positive");
        }
        WalletEntity wallet = walletRepository.findByIdAndUserId(walletId, userId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user: " + userId));

        int rowsUpdated = walletRepository.incrementBalanceAtomic(walletId, amount);
        if(rowsUpdated == 0) {
            throw new RuntimeException("Amount not credited");
        }
    }

    @Transactional
    public void refundFunds(UUID userId, String walletId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Refund amount must be positive");
        }
        WalletEntity wallet = walletRepository.findByIdAndUserId(walletId, userId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user: " + userId));

        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);
    }

}
