package com.springgrpc.graphql.dto.response; 
import lombok.*; 
@Data @Builder 
public class TokenResponse { 
    private String accessToken; 
    private String tokenType; 
    private long expiresIn; 
    private String refreshToken; 
    private String scope; 
    private String correlationId; 
} 
