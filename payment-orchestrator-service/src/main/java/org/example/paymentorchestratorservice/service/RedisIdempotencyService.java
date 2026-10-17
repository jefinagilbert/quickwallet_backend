package org.example.paymentorchestratorservice.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class RedisIdempotencyService {
    private StringRedisTemplate stringRedisTemplate;

    public  RedisIdempotencyService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public boolean acquireLock(String idempotencyKey, Duration lockTtl) {
        String key = "idempotency:" + idempotencyKey;
        Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(key, "IN_PROGRESS", lockTtl);
        return Boolean.TRUE.equals(success);
    }

    public void markCompleted(String idempotencyKey, UUID transactionId, Duration cacheTtl) {
        String key = "idempotency:" + idempotencyKey;
        stringRedisTemplate.opsForValue().set(key, "COMPLETED:" + transactionId, cacheTtl);
    }

    public void releaseLock(String idempotencyKey) {
        stringRedisTemplate.delete("idempotency:" + idempotencyKey);
    }

}
