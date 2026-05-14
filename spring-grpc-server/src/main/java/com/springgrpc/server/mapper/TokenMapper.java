package com.springgrpc.server.mapper; 
import com.springgrpc.grpc.oauth.OAuthTokenResponse; 
import com.springgrpc.server.domain.entity.RefreshTokenEntity; 
import com.springgrpc.server.domain.entity.TokenEntity; 
import org.mapstruct.Mapper; 
@Mapper(componentModel = "spring") 
public interface TokenMapper { 
    default OAuthTokenResponse toProto(TokenEntity accessToken, RefreshTokenEntity refreshToken) { 
        return OAuthTokenResponse.newBuilder() 
            .setAccessToken(accessToken.getTokenValue()) 
            .setTokenType("Bearer") 
            .setExpiresIn(3600) 
            .setRefreshToken(refreshToken.getTokenValue()) 
            .setScope(accessToken.getScope()) 
            .build(); 
    } 
} 
