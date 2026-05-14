package com.springgrpc.server.exception; 
import com.springgrpc.server.domain.enums.ErrorCode; 
import io.grpc.Status; 
public class GrpcStatusMapper { 
    public static Status map(ErrorCode code) { 
        return switch (code) { 
        }; 
    } 
} 
