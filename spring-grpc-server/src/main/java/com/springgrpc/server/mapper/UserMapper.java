package com.springgrpc.server.mapper; 
import com.springgrpc.grpc.oauth.UserProfile; 
import com.springgrpc.server.domain.entity.UserEntity; 
import org.mapstruct.Mapper; 
import org.mapstruct.Mapping; 
@Mapper(componentModel = "spring") 
public interface UserMapper { 
    @Mapping(target = "userId", source = "id") 
    @Mapping(target = "rolesCount", ignore = true) 
    UserProfile toProto(UserEntity entity); 
} 
