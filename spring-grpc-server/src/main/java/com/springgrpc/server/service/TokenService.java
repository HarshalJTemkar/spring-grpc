package com.springgrpc.server.service; 
import com.springgrpc.server.cache.TokenCacheService; 
import com.springgrpc.server.domain.entity.*; 
import com.springgrpc.server.domain.enums.*; 
import com.springgrpc.server.domain.repository.*; 
import com.springgrpc.server.exception.OAuthException; 
import com.springgrpc.server.security.*; 
import io.jsonwebtoken.Claims; 
import lombok.RequiredArgsConstructor; 
import lombok.extern.slf4j.Slf4j; 
import org.springframework.stereotype.Service; 
import org.springframework.transaction.annotation.Transactional; 
import java.time.Duration; 
import java.time.Instant; 
import java.util.*; 
@Slf4j @Service @RequiredArgsConstructor 
public class TokenService { 
    private final JwtTokenProvider jwtTokenProvider; 
    private final TokenRepository tokenRepository; 
    private final RefreshTokenRepository refreshTokenRepository; 
    private final TokenCacheService tokenCacheService; 
    @Transactional 
    public TokenEntity createAccessToken(String userId, String clientId, List<String> roles, List<String> scopes) { 
        String tokenId = UUID.randomUUID().toString(); 
        TokenClaims claims = TokenClaims.builder().sub(userId).userId(userId).clientId(clientId).roles(roles).scopes(scopes).build(); 
        String tokenValue = jwtTokenProvider.generateAccessToken(claims); 
        Instant now = Instant.now(); 
        TokenEntity entity = TokenEntity.builder().tokenId(tokenId).tokenValue(tokenValue).userId(userId).clientId(clientId).scope(String.join(" ", scopes)).tokenType(TokenType.ACCESS_TOKEN).issuedAt(now).expiresAt(now.plusMillis(jwtTokenProvider.getExpirationMs())).revoked(false).build(); 
        tokenRepository.save(entity); 
        tokenCacheService.cacheToken(tokenId, tokenValue, Duration.ofMillis(jwtTokenProvider.getExpirationMs())); 
        return entity; 
    } 
    @Transactional 
    public RefreshTokenEntity createRefreshToken(String userId, String clientId, String accessTokenId) { 
        String tokenId = UUID.randomUUID().toString(); 
        String tokenValue = jwtTokenProvider.generateRefreshToken(userId); 
        Instant now = Instant.now(); 
        RefreshTokenEntity entity = RefreshTokenEntity.builder().tokenId(tokenId).tokenValue(tokenValue).userId(userId).clientId(clientId).accessTokenId(accessTokenId).issuedAt(now).expiresAt(now.plusMillis(jwtTokenProvider.getRefreshExpirationMs())).used(false).revoked(false).build(); 
        refreshTokenRepository.save(entity); 
        return entity; 
    } 
    public Claims validateAndParseToken(String token) { 
        if (!jwtTokenProvider.isTokenValid(token)) throw new OAuthException(ErrorCode.INVALID_TOKEN, "Token is invalid"); 
        Claims claims = jwtTokenProvider.parseToken(token); 
        if (tokenCacheService.isBlacklisted(claims.getId())) throw new OAuthException(ErrorCode.REVOKED_TOKEN, "Token has been revoked"); 
        return claims; 
    } 
} 
