package com.springgrpc.server.domain.entity; 
import jakarta.persistence.*; import lombok.*; import java.time.Instant; 
@Entity @Table(name = "refresh_tokens") @Data @Builder @NoArgsConstructor @AllArgsConstructor 
public class RefreshTokenEntity { 
    @Id private String tokenId; 
    @Column(nullable = false, unique = true) private String tokenValue; 
    private String userId; 
    private String clientId; 
    private String accessTokenId; 
    private Instant issuedAt; 
    private Instant expiresAt; 
    private boolean used; 
    private boolean revoked; 
} 
