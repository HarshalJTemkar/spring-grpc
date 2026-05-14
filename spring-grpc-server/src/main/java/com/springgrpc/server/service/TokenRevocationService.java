package com.springgrpc.server.service; 
import com.springgrpc.server.cache.TokenCacheService; 
import com.springgrpc.server.domain.entity.TokenEntity; 
import com.springgrpc.server.domain.enums.ErrorCode; 
import com.springgrpc.server.domain.repository.TokenRepository; 
import com.springgrpc.server.exception.OAuthException; 
import lombok.RequiredArgsConstructor; 
import lombok.extern.slf4j.Slf4j; 
import org.springframework.stereotype.Service; 
import org.springframework.transaction.annotation.Transactional; 
import java.time.Duration; 
import java.time.Instant; 
@Slf4j @Service @RequiredArgsConstructor 
public class TokenRevocationService { 
    private final TokenRepository tokenRepository; 
    private final TokenCacheService tokenCacheService; 
    @Transactional 
    public void revokeToken(String tokenValue) { 
        TokenEntity token = tokenRepository.findByTokenValue(tokenValue) 
        token.setRevoked(true); 
        tokenRepository.save(token); 
        Duration remaining = Duration.between(Instant.now(), token.getExpiresAt()); 
        if (!remaining.isNegative()) tokenCacheService.blacklistToken(token.getTokenId(), remaining); 
        log.info("Token revoked: {}", token.getTokenId()); 
    } 
} 
