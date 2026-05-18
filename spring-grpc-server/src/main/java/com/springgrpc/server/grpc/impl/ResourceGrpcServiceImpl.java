package com.springgrpc.server.grpc.impl; 
import com.springgrpc.grpc.oauth.*; 
import com.springgrpc.server.exception.OAuthException; 
import com.springgrpc.server.service.TokenService; 
import io.grpc.stub.StreamObserver; 
import io.jsonwebtoken.Claims; 
import lombok.RequiredArgsConstructor; 
import net.devh.boot.grpc.server.service.GrpcService; 
import java.util.List; 
@GrpcService @RequiredArgsConstructor 
public class ResourceGrpcServiceImpl extends ResourceServiceGrpc.ResourceServiceImplBase { 
    private final TokenService tokenService; 
    @Override 
    public void validateToken(ValidateTokenRequest request, StreamObserver<ValidateTokenResponse> responseObserver) { 
        try { 
            Claims claims = tokenService.validateAndParseToken(request.getAccessToken()); 
            @SuppressWarnings("unchecked") List<String> scopes = (List<String>) claims.get("scopes"); 
            ValidateTokenResponse response = ValidateTokenResponse.newBuilder() 
                .setValid(true) 
                .setUserId(claims.get("userId", String.class)) 
                .setUsername(claims.getSubject()) 
                .addAllScopes(scopes != null ? scopes : List.of()) 
                .setExpiresAt(claims.getExpiration().getTime() / 1000) 
                .build(); 
            responseObserver.onNext(response); 
        } catch (OAuthException e) { 
            responseObserver.onNext(ValidateTokenResponse.newBuilder().setValid(false).build()); 
        } 
        responseObserver.onCompleted(); 
    } 
} 
