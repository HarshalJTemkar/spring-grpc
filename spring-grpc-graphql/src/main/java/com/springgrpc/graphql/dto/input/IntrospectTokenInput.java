package com.springgrpc.graphql.dto.input; 
import jakarta.validation.constraints.NotBlank; 
import lombok.Data; 
@Data 
public class IntrospectTokenInput { 
    @NotBlank private String token; 
    @NotBlank private String clientId; 
    @NotBlank private String clientSecret; 
} 
