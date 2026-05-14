package com.springgrpc.server.grpc.impl; 
import com.springgrpc.grpc.oauth.*; 
import com.springgrpc.server.service.AuthenticationService; 
import com.springgrpc.server.service.TokenRevocationService; 
import com.springgrpc.server.util.CorrelationIdHolder; 
import io.grpc.stub.StreamObserver; 
import lombok.RequiredArgsConstructor; 
import lombok.extern.slf4j.Slf4j; 
import net.devh.boot.grpc.server.service.GrpcService; 
@Slf4j @GrpcService @RequiredArgsConstructor 
public class AuthGrpcServiceImpl extends AuthServiceGrpc.AuthServiceImplBase { 
    private final AuthenticationService authenticationService; 
    private final TokenRevocationService tokenRevocationService; 
    @Override 
    public void passwordGrant(PasswordGrantRequest request, StreamObserver<OAuthTokenResponse> responseObserver) { 
        AuthenticationService.TokenPair pair = authenticationService.authenticate( 
            request.getUsername(), request.getPassword(), 
            request.getClientId(), request.getClientSecret(), request.getScope()); 
        OAuthTokenResponse response = OAuthTokenResponse.newBuilder() 
            .setAccessToken(pair.accessToken().getTokenValue()) 
            .setTokenType("Bearer") 
            .setExpiresIn(3600) 
            .setRefreshToken(pair.refreshToken().getTokenValue()) 
            .setScope(pair.accessToken().getScope()) 
            .setCorrelationId(CorrelationIdHolder.get() != null ? CorrelationIdHolder.get() : "") 
            .build(); 
        responseObserver.onNext(response); 
        responseObserver.onCompleted(); 
    } 
    @Override 
    public void revokeToken(RevokeTokenRequest request, StreamObserver<RevokeTokenResponse> responseObserver) { 
        tokenRevocationService.revokeToken(request.getToken()); 
        responseObserver.onNext(RevokeTokenResponse.newBuilder().setSuccess(true).build()); 
        responseObserver.onCompleted(); 
    } 
} 
