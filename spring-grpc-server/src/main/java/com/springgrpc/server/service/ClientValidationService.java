package com.springgrpc.server.service;
import com.springgrpc.server.domain.entity.ClientEntity;
import com.springgrpc.server.domain.repository.ClientRepository;
import com.springgrpc.server.exception.OAuthException;
import com.springgrpc.server.domain.enums.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
@Slf4j @Service @RequiredArgsConstructor
public class ClientValidationService {
    private final ClientRepository clientRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    public ClientEntity validateClient(String clientId, String clientSecret) {
        ClientEntity client = clientRepository.findById(clientId)
            .orElseThrow(() -> new OAuthException(ErrorCode.INVALID_CLIENT, "Unknown client"));
        if (!client.isActive()) throw new OAuthException(ErrorCode.INVALID_CLIENT, "Client disabled");
        boolean ok = bCryptPasswordEncoder.matches(clientSecret, client.getClientSecret());
        if (!ok) throw new OAuthException(ErrorCode.INVALID_CLIENT, "Invalid client secret");
        return client;
    }
    public void validateScope(ClientEntity client, String requestedScope) {
        if (requestedScope != null && requestedScope.length() > 0) {
            for (String s : requestedScope.split(" ")) {
                if (!client.getAllowedScopes().contains(s)) throw new OAuthException(ErrorCode.INVALID_SCOPE, "Scope not allowed: "+s);
            }
        }
    }
}
