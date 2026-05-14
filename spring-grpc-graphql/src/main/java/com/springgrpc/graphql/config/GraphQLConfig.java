package com.springgrpc.graphql.config; 
import org.springframework.context.annotation.Configuration; 
import org.springframework.graphql.execution.RuntimeWiringConfigurer; 
import org.springframework.context.annotation.Bean; 
@Configuration 
public class GraphQLConfig { 
    @Bean 
    public RuntimeWiringConfigurer runtimeWiringConfigurer() { 
    } 
} 
