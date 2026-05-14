package com.springgrpc.server.security; 
import io.jsonwebtoken.*; 
import io.jsonwebtoken.security.Keys; 
import lombok.extern.slf4j.Slf4j; 
import org.springframework.beans.factory.annotation.Value; 
import org.springframework.stereotype.Component; 
import javax.crypto.SecretKey; 
import java.nio.charset.StandardCharsets; 
import java.time.Instant; 
import java.util.*; 
@Slf4j @Component 
public class JwtTokenProvider { 
    @Value("${jwt.secret}") private String jwtSecret; 
    @Value("${jwt.expiration-ms:3600000}") private long expirationMs; 
    @Value("${jwt.refresh-expiration-ms:86400000}") private long refreshExpirationMs; 
    private SecretKey getSigningKey() { 
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)); 
    } 
    public String generateAccessToken(TokenClaims claims) { 
        Instant now = Instant.now(); 
        return Jwts.builder() 
            .subject(claims.getSub()) 
            .claim("userId", claims.getUserId()) 
            .claim("clientId", claims.getClientId()) 
            .claim("roles", claims.getRoles()) 
            .claim("scopes", claims.getScopes()) 
            .issuedAt(Date.from(now)) 
            .expiration(Date.from(now.plusMillis(expirationMs))) 
            .signWith(getSigningKey()) 
            .compact(); 
    } 
    public String generateRefreshToken(String userId) { 
        return Jwts.builder().subject(userId).issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + refreshExpirationMs)).signWith(getSigningKey()).compact(); 
    } 
    public Claims parseToken(String token) { 
        return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload(); 
    } 
    public boolean isTokenValid(String token) { 
        try { parseToken(token); return true; } catch (JwtException e) { return false; } 
    } 
    public long getExpirationMs() { return expirationMs; } 
    public long getRefreshExpirationMs() { return refreshExpirationMs; } 
} 
