package com.springgrpc.server.cache; 
import lombok.RequiredArgsConstructor; 
import lombok.extern.slf4j.Slf4j; 
import org.springframework.data.redis.core.StringRedisTemplate; 
import org.springframework.stereotype.Service; 
import java.time.Duration; 
import java.util.Optional; 
@Slf4j @Service @RequiredArgsConstructor 
public class TokenCacheService { 
    private final StringRedisTemplate redis; 
    private static final String BL_PREFIX = "token:blacklist:"; 
    private static final String TOKEN_PREFIX = "token:active:"; 
    public void blacklistToken(String tokenId, Duration ttl) { 
        redis.opsForValue().set(BL_PREFIX + tokenId, "1", ttl); 
        redis.delete(TOKEN_PREFIX + tokenId); 
    } 
    public boolean isBlacklisted(String tokenId) { 
        return Boolean.TRUE.equals(redis.hasKey(BL_PREFIX + tokenId)); 
    } 
    public void cacheToken(String tokenId, String tokenValue, Duration ttl) { 
        redis.opsForValue().set(TOKEN_PREFIX + tokenId, tokenValue, ttl); 
    } 
    public Optional<String> getCachedToken(String tokenId) { 
        return Optional.ofNullable(redis.opsForValue().get(TOKEN_PREFIX + tokenId)); 
    } 
} 
