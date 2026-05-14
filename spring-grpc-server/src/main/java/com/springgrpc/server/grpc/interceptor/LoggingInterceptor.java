package com.springgrpc.server.grpc.interceptor; 
import io.grpc.*; 
import lombok.extern.slf4j.Slf4j; 
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor; 
@Slf4j @GrpcGlobalServerInterceptor 
public class LoggingInterceptor implements ServerInterceptor { 
    @Override 
    public <T, R> ServerCall.Listener<T> interceptCall(ServerCall<T, R> call, Metadata headers, ServerCallHandler<T, R> next) { 
        long start = System.currentTimeMillis(); 
        log.info("gRPC call: {}", call.getMethodDescriptor().getFullMethodName()); 
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<T>(next.startCall(call, headers)) { 
            @Override 
            public void onComplete() { 
                log.info("gRPC completed: {} in {}ms", call.getMethodDescriptor().getFullMethodName(), System.currentTimeMillis() - start); 
                super.onComplete(); 
            } 
        }; 
    } 
} 
