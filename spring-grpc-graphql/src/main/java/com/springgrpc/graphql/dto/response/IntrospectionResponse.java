package com.springgrpc.graphql.dto.response; 
import lombok.*; 
@Data @Builder 
public class IntrospectionResponse { 
    private boolean active; 
    private String sub; 
    private String scope; 
    private long exp; 
    private String clientId; 
} 
