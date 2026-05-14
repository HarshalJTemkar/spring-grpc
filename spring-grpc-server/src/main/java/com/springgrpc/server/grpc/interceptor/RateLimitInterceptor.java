package com.springgrpc.server.grpc.interceptor; 
import io.grpc.*; 
import lombok.extern.slf4j.Slf4j; 
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor; 
import java.util.concurrent.*; 
import java.util.concurrent.atomic.AtomicInteger; 
@Slf4j @GrpcGlobalServerInterceptor 
public class RateLimitInterceptor implements ServerInterceptor { 
    private final ConcurrentHashMap<String, AtomicInteger> counters = new ConcurrentHashMap<>(); 
    private static final int MAX_REQUESTS_PER_SECOND = 100; 
    @Override 
    public <T, R> ServerCall.Listener<T> interceptCall(ServerCall<T, R> call, Metadata headers, ServerCallHandler<T, R> next) { 
        return next.startCall(call, headers); 
    } 
} 
