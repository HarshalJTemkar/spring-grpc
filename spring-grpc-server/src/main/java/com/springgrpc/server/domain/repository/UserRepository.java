package com.springgrpc.server.domain.repository; 
import com.springgrpc.server.domain.entity.UserEntity; 
import org.springframework.data.jpa.repository.JpaRepository; 
import org.springframework.stereotype.Repository; 
import java.util.Optional; 
@Repository 
public interface UserRepository extends JpaRepository<UserEntity, String> { 
    Optional<UserEntity> findByUsername(String username); 
    Optional<UserEntity> findByEmail(String email); 
    boolean existsByUsername(String username); 
} 
