package com.springgrpc.server.security; 
import lombok.*; import java.util.List; 
@Data @Builder 
public class TokenClaims { 
    private String sub; 
    private String userId; 
    private String clientId; 
    private List<String> roles; 
    private List<String> scopes; 
    private long iat; 
    private long exp; 
} 
