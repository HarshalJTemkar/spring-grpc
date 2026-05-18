package com.springgrpc.server.grpc.interceptor; 
import com.springgrpc.server.util.CorrelationIdHolder; 
import io.grpc.*; 
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor; 
import lombok.extern.slf4j.Slf4j; 
import java.util.UUID; 
@Slf4j @GrpcGlobalServerInterceptor 
public class CorrelationIdInterceptor implements ServerInterceptor { 
    public static final Metadata.Key<String> CORRELATION_ID_KEY = Metadata.Key.of("x-correlation-id", Metadata.ASCII_STRING_MARSHALLER); 
    @Override 
    public <T, R> ServerCall.Listener<T> interceptCall(ServerCall<T, R> call, Metadata headers, ServerCallHandler<T, R> next) { 
        String correlationId = headers.get(CORRELATION_ID_KEY); 
        if (correlationId == null) correlationId = UUID.randomUUID().toString(); 
        CorrelationIdHolder.set(correlationId); 
        final String finalId = correlationId; 
        log.debug("Request correlationId={}", finalId); 
        try { return next.startCall(call, headers); } finally { CorrelationIdHolder.clear(); } 
    } 
} 
