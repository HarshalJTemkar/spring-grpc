package com.springgrpc.server.exception; 
import com.springgrpc.server.domain.enums.ErrorCode; 
import io.grpc.Status; 
public final class GrpcStatusMapper { 
    private GrpcStatusMapper() { 
    } 
    public static Status map(ErrorCode code) { 
        if (code == null) { 
            return Status.INTERNAL; 
        } 
        return switch (code) { 
            case INVALID_CREDENTIALS, INVALID_CLIENT, INVALID_TOKEN, EXPIRED_TOKEN, REVOKED_TOKEN -> Status.UNAUTHENTICATED; 
            case INVALID_GRANT, INVALID_SCOPE -> Status.INVALID_ARGUMENT; 
            case INSUFFICIENT_SCOPE, USER_LOCKED, USER_INACTIVE -> Status.PERMISSION_DENIED; 
            case USER_NOT_FOUND -> Status.NOT_FOUND; 
            case INTERNAL_ERROR -> Status.INTERNAL; 
        }; 
    } 
} 