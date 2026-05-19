package com.springgrpc.server.mapper; 
import com.springgrpc.grpc.oauth.UserProfile; 
import com.springgrpc.server.domain.entity.UserEntity; 
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper { 
    default UserProfile toProto(UserEntity entity) { 
        if (entity == null) {
            return null;
        }
        UserProfile.Builder builder = UserProfile.newBuilder()
            .setUserId(entity.getId())
            .setUsername(entity.getUsername())
            .setEmail(entity.getEmail())
            .setStatus(entity.getStatus().name());
        
        if (entity.getRoles() != null) {
            builder.addAllRoles(entity.getRoles());
        }
        
        return builder.build();
    } 
} 
