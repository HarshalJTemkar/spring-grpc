package com.springgrpc.graphql.resolver.query; 
import com.springgrpc.graphql.dto.input.IntrospectTokenInput; 
import com.springgrpc.graphql.dto.response.IntrospectionResponse; 
import com.springgrpc.grpc.oauth.*; 
import lombok.RequiredArgsConstructor; 
import org.springframework.graphql.data.method.annotation.Argument; 
import org.springframework.graphql.data.method.annotation.QueryMapping; 
import org.springframework.stereotype.Controller; 
@Controller @RequiredArgsConstructor 
public class TokenQueryResolver { 
    private final TokenServiceGrpc.TokenServiceBlockingStub tokenStub; 
    @QueryMapping 
    public IntrospectionResponse introspect(@Argument IntrospectTokenInput input) { 
        IntrospectTokenRequest req = IntrospectTokenRequest.newBuilder().setToken(input.getToken()).setClientId(input.getClientId()).setClientSecret(input.getClientSecret()).build(); 
        IntrospectTokenResponse resp = tokenStub.introspectToken(req); 
        return IntrospectionResponse.builder().active(resp.getActive()).sub(resp.getSub()).exp(resp.getExp()).build(); 
    } 
} 
