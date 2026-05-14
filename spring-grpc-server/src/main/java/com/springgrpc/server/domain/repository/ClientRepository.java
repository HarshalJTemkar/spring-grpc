package com.springgrpc.server.domain.repository; 
import com.springgrpc.server.domain.entity.ClientEntity; 
import org.springframework.data.jpa.repository.JpaRepository; 
import org.springframework.stereotype.Repository; 
@Repository 
public interface ClientRepository extends JpaRepository<ClientEntity, String> { } 
