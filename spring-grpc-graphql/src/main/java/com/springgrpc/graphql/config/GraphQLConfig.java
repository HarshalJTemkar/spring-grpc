package com.springgrpc.graphql.config; 
import org.springframework.context.annotation.Configuration; 
import org.springframework.graphql.execution.RuntimeWiringConfigurer; 
import org.springframework.context.annotation.Bean; 
import com.springgrpc.graphql.scalar.DateTimeScalar;
@Configuration 
public class GraphQLConfig { 
    @Bean 
    public RuntimeWiringConfigurer runtimeWiringConfigurer() { 
        return wiringBuilder -> {
            wiringBuilder.scalar(DateTimeScalar.DATE_TIME);
        };
    } 
} 
