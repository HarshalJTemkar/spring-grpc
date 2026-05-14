package com.springgrpc.server.domain.entity; 
import com.springgrpc.server.domain.enums.UserStatus; 
import jakarta.persistence.*; 
import lombok.*; 
import java.time.Instant; 
import java.util.Set; 
@Entity @Table(name = "users") @Data @Builder @NoArgsConstructor @AllArgsConstructor 
public class UserEntity { 
    @Id @GeneratedValue(strategy = GenerationType.UUID) private String id; 
    @Column(unique = true, nullable = false) private String username; 
    @Column(unique = true, nullable = false) private String email; 
    @Column(nullable = false) private String passwordHash; 
    @Enumerated(EnumType.STRING) private UserStatus status; 
    @ElementCollection(fetch = FetchType.EAGER) @CollectionTable(name = "user_roles") @Column(name = "role") private Set<String> roles; 
    private Instant createdAt; 
    private Instant updatedAt; 
    @Version private Long version; 
} 
