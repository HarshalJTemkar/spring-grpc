package com.springgrpc.server.util; 
import org.springframework.data.redis.core.StringRedisTemplate; 
import org.springframework.stereotype.Component; 
import lombok.RequiredArgsConstructor; 
import java.time.Duration; 
@Component @RequiredArgsConstructor 
public class IdempotencyKeyValidator { 
    private final StringRedisTemplate redisTemplate; 
    private static final String PREFIX = "idempotency:"; 
    private static final Duration TTL = Duration.ofHours(24); 
    public boolean isProcessed(String key) { 
        return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + key)); 
    } 
    public void markProcessed(String key) { 
        redisTemplate.opsForValue().set(PREFIX + key, "1", TTL); 
    } 
} 
