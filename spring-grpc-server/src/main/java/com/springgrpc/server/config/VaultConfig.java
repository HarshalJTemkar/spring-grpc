package com.springgrpc.server.config; 
import org.springframework.context.annotation.Configuration; 
import org.springframework.vault.annotation.VaultPropertySource; 
@Configuration 
@VaultPropertySource("secret/spring-grpc-server") 
public class VaultConfig { } 
