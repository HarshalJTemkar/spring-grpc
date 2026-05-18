package com.springgrpc.server.grpc; 
import com.springgrpc.grpc.oauth.*; 
import com.springgrpc.server.domain.entity.*; 
import com.springgrpc.server.grpc.impl.AuthGrpcServiceImpl; 
import com.springgrpc.server.service.AuthenticationService; 
import com.springgrpc.server.service.TokenRevocationService; 
import io.grpc.stub.StreamObserver; 
import org.junit.jupiter.api.Test; 
import org.junit.jupiter.api.extension.ExtendWith; 
import org.mockito.*; 
import org.mockito.junit.jupiter.MockitoExtension; 
import static org.assertj.core.api.Assertions.*; 
import static org.mockito.Mockito.*; 
@ExtendWith(MockitoExtension.class) 
class AuthGrpcServiceImplTest { 
    @Mock AuthenticationService authService; 
    @Mock TokenRevocationService revocationService; 
    @InjectMocks AuthGrpcServiceImpl authGrpcService; 
    @Test void passwordGrant_success() { 
        PasswordGrantRequest req = PasswordGrantRequest.newBuilder().setUsername("alice").setPassword("pass").setClientId("c1").setClientSecret("s1").build(); 
        TokenEntity at = TokenEntity.builder().tokenId("t1").tokenValue("jwt").scope("read").build(); 
        RefreshTokenEntity rt = RefreshTokenEntity.builder().tokenId("r1").tokenValue("refresh").build(); 
        when(authService.authenticate(any(),any(),any(),any(),any())).thenReturn(new AuthenticationService.TokenPair(at,rt)); 
        @SuppressWarnings("unchecked") StreamObserver<OAuthTokenResponse> observer = mock(StreamObserver.class); 
        authGrpcService.passwordGrant(req, observer); 
        verify(observer).onNext(any(OAuthTokenResponse.class)); 
        verify(observer).onCompleted(); 
    } 
} 
