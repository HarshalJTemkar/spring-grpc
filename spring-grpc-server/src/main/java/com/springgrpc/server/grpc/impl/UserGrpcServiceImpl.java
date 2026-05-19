package com.springgrpc.server.grpc.impl; 
import com.springgrpc.grpc.oauth.*; 
import com.springgrpc.server.domain.entity.UserEntity; 
import com.springgrpc.server.mapper.UserMapper;
import com.springgrpc.server.service.UserService; 
import io.grpc.stub.StreamObserver; 
import lombok.RequiredArgsConstructor; 
import net.devh.boot.grpc.server.service.GrpcService; 
@GrpcService @RequiredArgsConstructor 
public class UserGrpcServiceImpl extends UserServiceGrpc.UserServiceImplBase { 
    private final UserService userService; 
    private final UserMapper userMapper;
    
    @Override 
    public void getUser(GetUserRequest request, StreamObserver<UserProfile> responseObserver) { 
        UserEntity user = userService.findById(request.getUserId()); 
        UserProfile profile = userMapper.toProto(user);
        responseObserver.onNext(profile); 
        responseObserver.onCompleted(); 
    } 
} 
