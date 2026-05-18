package com.springgrpc.server.grpc.impl; 
import com.springgrpc.grpc.oauth.*; 
import com.springgrpc.server.domain.entity.UserEntity; 
import com.springgrpc.server.service.UserService; 
import io.grpc.stub.StreamObserver; 
import lombok.RequiredArgsConstructor; 
import net.devh.boot.grpc.server.service.GrpcService; 
@GrpcService @RequiredArgsConstructor 
public class UserGrpcServiceImpl extends UserServiceGrpc.UserServiceImplBase { 
    private final UserService userService; 
    @Override 
    public void getUser(GetUserRequest request, StreamObserver<UserProfile> responseObserver) { 
        UserEntity user = userService.findById(request.getUserId()); 
        UserProfile profile = UserProfile.newBuilder() 
            .setUserId(user.getId()) 
            .setUsername(user.getUsername()) 
            .setEmail(user.getEmail()) 
            .setStatus(user.getStatus().name()) 
            .addAllRoles(user.getRoles()) 
            .build(); 
        responseObserver.onNext(profile); 
        responseObserver.onCompleted(); 
    } 
} 
