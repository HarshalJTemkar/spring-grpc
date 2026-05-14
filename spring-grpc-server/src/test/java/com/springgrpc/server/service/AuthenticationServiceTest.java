package com.springgrpc.server.service; 
import com.springgrpc.server.domain.entity.*; 
import com.springgrpc.server.domain.enums.UserStatus; 
import com.springgrpc.server.exception.OAuthException; 
import com.springgrpc.server.security.PasswordEncoderService; 
import org.junit.jupiter.api.BeforeEach; 
import org.junit.jupiter.api.Test; 
import org.junit.jupiter.api.extension.ExtendWith; 
import org.mockito.InjectMocks; 
import org.mockito.Mock; 
import org.mockito.junit.jupiter.MockitoExtension; 
import java.util.Set; 
import static org.assertj.core.api.Assertions.*; 
import static org.mockito.Mockito.*; 
@ExtendWith(MockitoExtension.class) 
class AuthenticationServiceTest { 
    @Mock UserService userService; 
    @Mock ClientValidationService clientValidationService; 
    @Mock TokenService tokenService; 
    @Mock PasswordEncoderService passwordEncoderService; 
    @InjectMocks AuthenticationService authService; 
    private UserEntity user; 
    private ClientEntity client; 
    @BeforeEach void setUp() { 
        user = UserEntity.builder().id("u1").username("alice").passwordHash("hash").status(UserStatus.ACTIVE).roles(Set.of("ROLE_USER")).build(); 
        client = ClientEntity.builder().clientId("client1").clientSecret("secret").active(true).allowedScopes(Set.of("read")).allowedGrantTypes(Set.of("password")).build(); 
    } 
    @Test void authenticate_success() { 
        when(clientValidationService.validateClient("client1","secret")).thenReturn(client); 
        when(userService.findByUsername("alice")).thenReturn(user); 
        when(passwordEncoderService.matches("pass","hash")).thenReturn(true); 
        TokenEntity at = TokenEntity.builder().tokenId("t1").tokenValue("jwt").scope("read").build(); 
        RefreshTokenEntity rt = RefreshTokenEntity.builder().tokenId("r1").tokenValue("refresh").build(); 
        when(tokenService.createAccessToken(any(),any(),any(),any())).thenReturn(at); 
        when(tokenService.createRefreshToken(any(),any(),any())).thenReturn(rt); 
        AuthenticationService.TokenPair result = authService.authenticate("alice","pass","client1","secret","read"); 
        assertThat(result.accessToken().getTokenValue()).isEqualTo("jwt"); 
    } 
    @Test void authenticate_invalidPassword_throws() { 
        when(clientValidationService.validateClient(any(),any())).thenReturn(client); 
        when(userService.findByUsername(any())).thenReturn(user); 
        when(passwordEncoderService.matches(any(),any())).thenReturn(false); 
    } 
} 
