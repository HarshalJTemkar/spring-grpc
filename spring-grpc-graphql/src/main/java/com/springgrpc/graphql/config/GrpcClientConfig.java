package com.springgrpc.graphql.config; 
import com.springgrpc.grpc.oauth.*; 
import io.grpc.ManagedChannel; 
import net.devh.boot.grpc.client.inject.GrpcClient; 
import org.springframework.context.annotation.Bean; 
import org.springframework.context.annotation.Configuration; 
@Configuration 
public class GrpcClientConfig { 
    @GrpcClient("grpc-server") 
    private AuthServiceGrpc.AuthServiceBlockingStub authServiceStub; 
    @GrpcClient("grpc-server") 
    private TokenServiceGrpc.TokenServiceBlockingStub tokenServiceStub; 
    @GrpcClient("grpc-server") 
    private UserServiceGrpc.UserServiceBlockingStub userServiceStub; 
    @Bean 
    public AuthServiceGrpc.AuthServiceBlockingStub authStub() { return authServiceStub; } 
    @Bean 
    public TokenServiceGrpc.TokenServiceBlockingStub tokenStub() { return tokenServiceStub; } 
    @Bean 
    public UserServiceGrpc.UserServiceBlockingStub userStub() { return userServiceStub; } 
} 
