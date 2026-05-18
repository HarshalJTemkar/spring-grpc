package com.springgrpc.server.exception; 
import io.grpc.Status; 
import io.grpc.StatusRuntimeException; 
import net.devh.boot.grpc.server.advice.GrpcAdvice; 
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler; 
import lombok.extern.slf4j.Slf4j; 
@Slf4j @GrpcAdvice 
public class GrpcExceptionAdvice { 
    @GrpcExceptionHandler(OAuthException.class) 
    public StatusRuntimeException handleOAuthException(OAuthException e) { 
        log.warn("OAuthException: {} - {}", e.getErrorCode(), e.getMessage()); 
        return GrpcStatusMapper.map(e.getErrorCode()).withDescription(e.getMessage()).asRuntimeException(); 
    } 
    @GrpcExceptionHandler(Exception.class) 
    public StatusRuntimeException handleException(Exception e) { 
        log.error("Unexpected error", e); 
        return Status.INTERNAL.withDescription("Internal server error").asRuntimeException(); 
    } 
} 
