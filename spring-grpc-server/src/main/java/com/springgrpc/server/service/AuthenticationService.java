package com.springgrpc.server.service;
import com.springgrpc.server.domain.entity.*;
import com.springgrpc.server.domain.enums.ErrorCode;
import com.springgrpc.server.exception.OAuthException;
import com.springgrpc.server.security.PasswordEncoderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Arrays;
import java.util.List;

@Slf4j @Service @RequiredArgsConstructor
public class AuthenticationService {
    private final UserService userService;
    private final ClientValidationService clientValidationService;
    private final TokenService tokenService;
    private final PasswordEncoderService passwordEncoderService;

    @Transactional
    public record TokenPair(TokenEntity accessToken, RefreshTokenEntity refreshToken) {}

    @Transactional
    public TokenPair authenticate(String username, String password, String clientId, String clientSecret, String scope) {
        ClientEntity client = clientValidationService.validateClient(clientId, clientSecret);
        clientValidationService.validateScope(client, scope);
        UserEntity user = userService.findByUsername(username);
        userService.validateUserActive(user);
        boolean pwOk = passwordEncoderService.matches(password, user.getPasswordHash());
        if (!pwOk) throw new OAuthException(ErrorCode.INVALID_CREDENTIALS, "Invalid username or password");
        List<String> scopes = (scope != null && scope.length() > 0) ? Arrays.asList(scope.split(" ")) : List.copyOf(client.getAllowedScopes());
        TokenEntity accessToken = tokenService.createAccessToken(user.getId(), clientId, List.copyOf(user.getRoles()), scopes);
        RefreshTokenEntity refreshToken = tokenService.createRefreshToken(user.getId(), clientId, accessToken.getTokenId());
        log.info("ROPC grant successful for user={} client={}", username, clientId);
        return new TokenPair(accessToken, refreshToken);
    }
}
