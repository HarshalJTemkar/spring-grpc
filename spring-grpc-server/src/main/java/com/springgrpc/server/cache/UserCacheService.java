package com.springgrpc.server.cache; 
import com.springgrpc.server.domain.entity.UserEntity; 
import com.fasterxml.jackson.databind.ObjectMapper; 
import lombok.RequiredArgsConstructor; 
import lombok.extern.slf4j.Slf4j; 
import org.springframework.cache.annotation.CacheEvict; 
import org.springframework.cache.annotation.Cacheable; 
import org.springframework.stereotype.Service; 
@Slf4j @Service @RequiredArgsConstructor 
public class UserCacheService { 
    @Cacheable(value = "users", key = "#username") 
    public UserEntity cacheUser(String username, UserEntity user) { return user; } 
    @CacheEvict(value = "users", key = "#username") 
    public void evictUser(String username) { log.debug("Evicted user cache for {}", username); } 
} 
