package com.springgrpc.server.exception; 
import com.springgrpc.server.domain.enums.ErrorCode; 
import lombok.Getter; 
@Getter 
public class OAuthException extends RuntimeException { 
    private final ErrorCode errorCode; 
    public OAuthException(ErrorCode errorCode, String message) { 
        super(message); 
        this.errorCode = errorCode; 
    } 
} 
