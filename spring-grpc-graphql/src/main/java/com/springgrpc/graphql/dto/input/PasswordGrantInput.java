package com.springgrpc.graphql.dto.input; 
import jakarta.validation.constraints.NotBlank; 
import lombok.Data; 
@Data 
public class PasswordGrantInput { 
    @NotBlank private String username; 
    @NotBlank private String password; 
    @NotBlank private String clientId; 
    @NotBlank private String clientSecret; 
    private String scope; 
} 
