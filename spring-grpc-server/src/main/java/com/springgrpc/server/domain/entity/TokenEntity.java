package com.springgrpc.server.domain.entity; 
import com.springgrpc.server.domain.enums.TokenType; 
import jakarta.persistence.*; import lombok.*; import java.time.Instant; 
@Entity @Table(name = "tokens") @Data @Builder @NoArgsConstructor @AllArgsConstructor 
public class TokenEntity { 
    @Id private String tokenId; 
    @Column(nullable = false, length = 2000) private String tokenValue; 
    private String userId; 
    private String clientId; 
    private String scope; 
    @Enumerated(EnumType.STRING) private TokenType tokenType; 
    private Instant issuedAt; 
    private Instant expiresAt; 
    private boolean revoked; 
} 
