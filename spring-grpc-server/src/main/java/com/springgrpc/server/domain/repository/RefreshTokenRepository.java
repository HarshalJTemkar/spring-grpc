package com.springgrpc.server.domain.repository; 
import com.springgrpc.server.domain.entity.RefreshTokenEntity; 
import org.springframework.data.jpa.repository.JpaRepository; 
import org.springframework.stereotype.Repository; 
import java.util.Optional; 
@Repository 
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, String> { 
    Optional<RefreshTokenEntity> findByTokenValue(String tokenValue); 
} 
