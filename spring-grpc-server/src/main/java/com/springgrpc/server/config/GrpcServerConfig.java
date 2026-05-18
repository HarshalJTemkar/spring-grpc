package com.springgrpc.server.config; 
import com.springgrpc.server.grpc.interceptor.AuthInterceptor;
import com.springgrpc.server.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.interceptor.GlobalServerInterceptorConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Slf4j
@Configuration 
public class GrpcServerConfig { 
    @Bean
    public AuthInterceptor authInterceptor(JwtTokenProvider jwtTokenProvider,
                                           SecurityProperties securityProperties) {
        log.info("Registering gRPC AuthInterceptor (authorization.enabled={})",
                securityProperties.isEnabled());
        return new AuthInterceptor(jwtTokenProvider, securityProperties);
    }

    @Bean
    public GlobalServerInterceptorConfigurer authInterceptorConfigurer(AuthInterceptor authInterceptor) {
        return interceptors -> interceptors.add(authInterceptor);
    }
}