package com.springgrpc.server.grpc.impl; 
import com.springgrpc.grpc.oauth.*; 
import com.springgrpc.server.domain.entity.*; 
import com.springgrpc.server.domain.enums.ErrorCode; 
import com.springgrpc.server.domain.repository.RefreshTokenRepository; 
import com.springgrpc.server.exception.OAuthException; 
import com.springgrpc.server.service.ClientValidationService; 
import com.springgrpc.server.service.TokenService; 
import com.springgrpc.server.service.UserService; 
import io.grpc.stub.StreamObserver; 
import io.jsonwebtoken.Claims; 
import lombok.RequiredArgsConstructor; 
import lombok.extern.slf4j.Slf4j; 
import net.devh.boot.grpc.server.service.GrpcService; 
import java.util.List; 
@Slf4j @GrpcService @RequiredArgsConstructor 
public class TokenGrpcServiceImpl extends TokenServiceGrpc.TokenServiceImplBase { 
    private final TokenService tokenService; 
    private final ClientValidationService clientValidationService; 
    private final RefreshTokenRepository refreshTokenRepository; 
    private final UserService userService; 
    @Override 
    public void refreshToken(RefreshTokenRequest request, StreamObserver<OAuthTokenResponse> responseObserver) { 
        clientValidationService.validateClient(request.getClientId(), request.getClientSecret()); 
        RefreshTokenEntity rt = refreshTokenRepository.findByTokenValue(request.getRefreshToken()) 
            .orElseThrow(() -> new OAuthException(ErrorCode.INVALID_TOKEN, "Refresh token not found")); 
        if (rt.isUsed() || rt.isRevoked()) throw new OAuthException(ErrorCode.REVOKED_TOKEN, "Refresh token already used/revoked"); 
        rt.setUsed(true); 
        refreshTokenRepository.save(rt); 
        UserEntity user = userService.findById(rt.getUserId()); 
        TokenEntity at = tokenService.createAccessToken(user.getId(), rt.getClientId(), List.copyOf(user.getRoles()), List.of()); 
        RefreshTokenEntity newRt = tokenService.createRefreshToken(user.getId(), rt.getClientId(), at.getTokenId()); 
        OAuthTokenResponse response = OAuthTokenResponse.newBuilder().setAccessToken(at.getTokenValue()).setTokenType("Bearer").setExpiresIn(3600).setRefreshToken(newRt.getTokenValue()).build(); 
        responseObserver.onNext(response); 
        responseObserver.onCompleted(); 
    } 
    @Override 
    public void introspectToken(IntrospectTokenRequest request, StreamObserver<IntrospectTokenResponse> responseObserver) { 
        try { 
            Claims claims = tokenService.validateAndParseToken(request.getToken()); 
            IntrospectTokenResponse response = IntrospectTokenResponse.newBuilder().setActive(true).setSub(claims.getSubject()).setExp(claims.getExpiration().getTime() / 1000).build(); 
            responseObserver.onNext(response); 
        } catch (OAuthException e) { 
            responseObserver.onNext(IntrospectTokenResponse.newBuilder().setActive(false).build()); 
        } 
        responseObserver.onCompleted(); 
    } 
} 
