package com.springgrpc.graphql.dto.response; 
import lombok.*; 
import java.util.List; 
@Data @Builder 
public class UserResponse { 
    private String userId; 
    private String username; 
    private String email; 
    private String status; 
    private List<String> roles; 
} 
