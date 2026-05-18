package com.springgrpc.server.service; 
import com.springgrpc.server.domain.entity.UserEntity; 
import com.springgrpc.server.domain.enums.ErrorCode; 
import com.springgrpc.server.domain.enums.UserStatus; 
import com.springgrpc.server.domain.repository.UserRepository; 
import com.springgrpc.server.exception.OAuthException; 
import lombok.RequiredArgsConstructor; 
import lombok.extern.slf4j.Slf4j; 
import org.springframework.stereotype.Service; 
@Slf4j @Service @RequiredArgsConstructor 
public class UserService { 
    private final UserRepository userRepository; 
    public UserEntity findByUsername(String username) { 
        return userRepository.findByUsername(username) 
            .orElseThrow(() -> new OAuthException(ErrorCode.USER_NOT_FOUND, "User not found: " + username)); 
    } 
    public UserEntity findById(String id) { 
        return userRepository.findById(id) 
            .orElseThrow(() -> new OAuthException(ErrorCode.USER_NOT_FOUND, "User not found: " + id)); 
    } 
    public void validateUserActive(UserEntity user) { 
        if (user.getStatus() == UserStatus.LOCKED) throw new OAuthException(ErrorCode.USER_LOCKED, "User account is locked"); 
        if (user.getStatus() != UserStatus.ACTIVE) throw new OAuthException(ErrorCode.USER_INACTIVE, "User account is not active"); 
    } 
} 
