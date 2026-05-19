package com.springgrpc.graphql.dto.input; 
import jakarta.validation.constraints.NotBlank; 
import lombok.Data; 
@Data 
public class RefreshTokenInput { 
    @NotBlank private String refreshToken; 
    @NotBlank private String clientId; 
    @NotBlank private String clientSecret; 
} 
