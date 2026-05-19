package com.springgrpc.graphql.dto.input; 
import jakarta.validation.constraints.NotBlank; 
import lombok.Data; 
@Data 
public class RevokeTokenInput { 
    @NotBlank private String token; 
    private String tokenTypeHint; 
    @NotBlank private String clientId; 
    @NotBlank private String clientSecret; 
} 
