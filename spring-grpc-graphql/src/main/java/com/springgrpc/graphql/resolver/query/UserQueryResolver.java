package com.springgrpc.graphql.resolver.query; 
import com.springgrpc.graphql.dto.response.UserResponse; 
import com.springgrpc.grpc.oauth.*; 
import lombok.RequiredArgsConstructor; 
import org.springframework.graphql.data.method.annotation.Argument; 
import org.springframework.graphql.data.method.annotation.QueryMapping; 
import org.springframework.stereotype.Controller; 
import java.util.List; 
@Controller @RequiredArgsConstructor 
public class UserQueryResolver { 
    private final UserServiceGrpc.UserServiceBlockingStub userStub; 
    @QueryMapping 
    public UserResponse user(@Argument String userId) { 
        UserProfile profile = userStub.getUser(GetUserRequest.newBuilder().setUserId(userId).build()); 
        return UserResponse.builder().userId(profile.getUserId()).username(profile.getUsername()).email(profile.getEmail()).status(profile.getStatus()).roles(profile.getRolesList()).build(); 
    } 
} 
