package com.springgrpc.graphql.resolver.mutation; 
import com.springgrpc.graphql.dto.input.PasswordGrantInput; 
import com.springgrpc.graphql.dto.input.RevokeTokenInput; 
import com.springgrpc.graphql.dto.response.TokenResponse; 
import com.springgrpc.grpc.oauth.*; 
import lombok.RequiredArgsConstructor; 
import lombok.extern.slf4j.Slf4j; 
import org.springframework.graphql.data.method.annotation.Argument; 
import org.springframework.graphql.data.method.annotation.MutationMapping; 
import org.springframework.stereotype.Controller; 
@Slf4j @Controller @RequiredArgsConstructor 
public class AuthMutationResolver { 
    private final AuthServiceGrpc.AuthServiceBlockingStub authStub; 
    @MutationMapping 
    public TokenResponse login(@Argument PasswordGrantInput input) { 
        PasswordGrantRequest req = PasswordGrantRequest.newBuilder() 
            .setUsername(input.getUsername()) 
            .setPassword(input.getPassword()) 
            .setClientId(input.getClientId()) 
            .setClientSecret(input.getClientSecret()) 
            .setScope(input.getScope() != null ? input.getScope() : "") 
            .build(); 
        OAuthTokenResponse resp = authStub.passwordGrant(req); 
        return TokenResponse.builder() 
            .accessToken(resp.getAccessToken()) 
            .tokenType(resp.getTokenType()) 
            .expiresIn(resp.getExpiresIn()) 
            .refreshToken(resp.getRefreshToken()) 
            .scope(resp.getScope()) 
            .correlationId(resp.getCorrelationId()) 
            .build(); 
    } 
    @MutationMapping 
    public boolean revokeToken(@Argument RevokeTokenInput input) { 
        RevokeTokenRequest req = RevokeTokenRequest.newBuilder().setToken(input.getToken()).setClientId(input.getClientId()).setClientSecret(input.getClientSecret()).build(); 
        authStub.revokeToken(req); 
        return true; 
    } 
} 
