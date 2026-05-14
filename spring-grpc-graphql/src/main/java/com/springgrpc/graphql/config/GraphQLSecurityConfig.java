package com.springgrpc.graphql.config; 
import org.springframework.context.annotation.Bean; 
import org.springframework.context.annotation.Configuration; 
import org.springframework.security.config.annotation.web.builders.HttpSecurity; 
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity; 
import org.springframework.security.config.http.SessionCreationPolicy; 
import org.springframework.security.web.SecurityFilterChain; 
@Configuration @EnableWebSecurity 
public class GraphQLSecurityConfig { 
    @Bean 
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception { 
        return http 
                .requestMatchers("/graphql").permitAll() 
                .requestMatchers("/graphiql/**").permitAll() 
                .requestMatchers("/actuator/**").permitAll() 
                .anyRequest().authenticated()) 
            .build(); 
    } 
} 
