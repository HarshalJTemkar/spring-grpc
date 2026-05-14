package com.springgrpc.graphql.resolver; 
import com.springgrpc.graphql.resolver.mutation.AuthMutationResolver; 
import com.springgrpc.grpc.oauth.*; 
import org.junit.jupiter.api.Test; 
import org.junit.jupiter.api.extension.ExtendWith; 
import org.mockito.*; 
import org.mockito.junit.jupiter.MockitoExtension; 
import static org.assertj.core.api.Assertions.*; 
import static org.mockito.Mockito.*; 
@ExtendWith(MockitoExtension.class) 
class AuthMutationResolverTest { 
    @Mock AuthServiceGrpc.AuthServiceBlockingStub authStub; 
    @InjectMocks AuthMutationResolver resolver; 
    @Test 
    void login_success() { 
        OAuthTokenResponse grpcResp = OAuthTokenResponse.newBuilder().setAccessToken("jwt").setTokenType("Bearer").setExpiresIn(3600).setRefreshToken("refresh").setScope("read").build(); 
        when(authStub.passwordGrant(any(PasswordGrantRequest.class))).thenReturn(grpcResp); 
        com.springgrpc.graphql.dto.input.PasswordGrantInput input = new com.springgrpc.graphql.dto.input.PasswordGrantInput(); 
        input.setUsername("alice"); input.setPassword("pass"); input.setClientId("c1"); input.setClientSecret("s1"); 
        com.springgrpc.graphql.dto.response.TokenResponse resp = resolver.login(input); 
        assertThat(resp.getAccessToken()).isEqualTo("jwt"); 
        assertThat(resp.getTokenType()).isEqualTo("Bearer"); 
    } 
} 
