package com.springgrpc.graphql.resolver.mutation; 
import com.springgrpc.graphql.dto.input.RefreshTokenInput; 
import com.springgrpc.graphql.dto.response.TokenResponse; 
import com.springgrpc.grpc.oauth.*; 
import lombok.RequiredArgsConstructor; 
import org.springframework.graphql.data.method.annotation.Argument; 
import org.springframework.graphql.data.method.annotation.MutationMapping; 
import org.springframework.stereotype.Controller; 
@Controller @RequiredArgsConstructor 
public class TokenMutationResolver { 
    private final TokenServiceGrpc.TokenServiceBlockingStub tokenStub; 
    @MutationMapping 
    public TokenResponse refreshToken(@Argument RefreshTokenInput input) { 
        RefreshTokenRequest req = RefreshTokenRequest.newBuilder().setRefreshToken(input.getRefreshToken()).setClientId(input.getClientId()).setClientSecret(input.getClientSecret()).build(); 
        OAuthTokenResponse resp = tokenStub.refreshToken(req); 
        return TokenResponse.builder().accessToken(resp.getAccessToken()).tokenType(resp.getTokenType()).expiresIn(resp.getExpiresIn()).refreshToken(resp.getRefreshToken()).scope(resp.getScope()).build(); 
    } 
} 
