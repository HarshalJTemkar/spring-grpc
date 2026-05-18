package com.springgrpc.server.domain.entity; 
import jakarta.persistence.*; import lombok.*; import java.time.Instant; import java.util.Set; 
@Entity @Table(name = "oauth_clients") @Data @Builder @NoArgsConstructor @AllArgsConstructor 
public class ClientEntity { 
    @Id private String clientId; 
    @Column(nullable = false) private String clientSecret; 
    private String clientName; 
    @ElementCollection(fetch = FetchType.EAGER) @CollectionTable(name = "client_scopes") @Column(name = "scope") private Set<String> allowedScopes; 
    @ElementCollection(fetch = FetchType.EAGER) @CollectionTable(name = "client_grant_types") @Column(name = "grant_type") private Set<String> allowedGrantTypes; 
    private boolean active; 
    private Instant createdAt; 
} 
