package org.example.walletservice.service;

import org.example.walletservice.entity.WalletEntity;
import org.example.walletservice.exception.InsufficientBalanceException;
import org.example.walletservice.exception.WalletNotFoundException;
import org.example.walletservice.repository.WalletRepository;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Service
public class WalletService {

    private final StringRedisTemplate redisTemplate;
    private final RedissonClient redissonClient;
    private final WalletRepository walletRepository;

    private static final String BALANCE_KEY_PREFIX = "wallet:balance:";
    private static final String LOCK_KEY_PREFIX = "lock:wallet:balance:";
    private static final String NULL_SENTINEL = "-1";

    public WalletService(WalletRepository walletRepository, StringRedisTemplate redisTemplate, RedissonClient redissonClient) {
        this.walletRepository = walletRepository;
        this.redisTemplate = redisTemplate;
        this.redissonClient = redissonClient;
    }

    @Transactional
    public WalletEntity initializeWallet(UUID userId, String name) {
        if (this.walletRepository.findByUserId(userId).isPresent()) {
            throw new RuntimeException("Wallet already exists");
        }
        WalletEntity walletEntity = new WalletEntity(userId, name, BigDecimal.ZERO);
        return walletRepository.save(walletEntity);
    }

    @Transactional(readOnly = true)
    public BigDecimal getBalance(UUID userId) {

        Optional<WalletEntity> user = walletRepository.findByUserId(userId);

        String walletId = user.get().getId();

        String cacheKey = BALANCE_KEY_PREFIX + walletId;



        String cachedValue = redisTemplate.opsForValue().get(cacheKey);
        if (cachedValue != null) {
            return parseCachedValue(cachedValue, walletId);
        }

        String lockKey = LOCK_KEY_PREFIX + walletId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean isLocked = lock.tryLock(2500, 5000, TimeUnit.MILLISECONDS);

            if (!isLocked) {
                Thread.sleep(100);
                String retryVal = redisTemplate.opsForValue().get(cacheKey);
                if (retryVal != null) {
                    return parseCachedValue(retryVal, walletId);
                }
                return walletRepository.findBalanceById(walletId)
                        .orElseThrow(() -> new WalletNotFoundException("Wallet not found: " + walletId));
            }

            try {
                cachedValue = redisTemplate.opsForValue().get(cacheKey);
                if (cachedValue != null) {
                    return parseCachedValue(cachedValue, walletId);
                }

                Optional<BigDecimal> balanceOpt = walletRepository.findBalanceById(walletId);
                if (balanceOpt.isEmpty()) {
                    redisTemplate.opsForValue().set(cacheKey, NULL_SENTINEL, Duration.ofSeconds(60));
                    throw new WalletNotFoundException("Wallet not found: " + walletId);
                }

                BigDecimal balanceFromDb = balanceOpt.get();

                int jitterSeconds = ThreadLocalRandom.current().nextInt(0, 181);
                long totalTtlSeconds = Duration.ofMinutes(15).getSeconds() + jitterSeconds;

                redisTemplate.opsForValue().set(cacheKey, balanceFromDb.toString(), Duration.ofSeconds(totalTtlSeconds));
                return balanceFromDb;

            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted while waiting for balance lock", e);
        }
    }

    private BigDecimal parseCachedValue(String val, String walletId) {
        if (NULL_SENTINEL.equals(val)) {
            throw new WalletNotFoundException("Wallet not found: " + walletId);
        }
        return new BigDecimal(val);
    }

    @Transactional
    public WalletEntity addFunds(UUID userId, String walletId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        WalletEntity wallet = walletRepository.findByIdAndUserId(walletId, userId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user: " + userId));

        wallet.setBalance(wallet.getBalance().add(amount));
        WalletEntity saved = walletRepository.save(wallet);
        redisTemplate.delete(BALANCE_KEY_PREFIX + walletId);
        return saved;
    }

    @Transactional
    public void debitFunds(UUID userId, String walletId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Debit amount must be positive");
        }
        walletRepository.findByIdAndUserId(walletId, userId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user: " + userId));

        int rowsUpdated = walletRepository.decrementBalanceAtomic(walletId, amount);
        if (rowsUpdated == 0) {
            throw new InsufficientBalanceException("Insufficient Balance for wallet: " + walletId);
        }
        redisTemplate.delete(BALANCE_KEY_PREFIX + walletId);
    }

    @Transactional
    public void creditFunds(UUID userId, String walletId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Credit amount must be positive");
        }
        walletRepository.findByIdAndUserId(walletId, userId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user: " + userId));

        int rowsUpdated = walletRepository.incrementBalanceAtomic(walletId, amount);
        if (rowsUpdated == 0) {
            throw new RuntimeException("Amount not credited");
        }
        redisTemplate.delete(BALANCE_KEY_PREFIX + walletId);
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
        redisTemplate.delete(BALANCE_KEY_PREFIX + walletId);
    }
}
