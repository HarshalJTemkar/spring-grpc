# Spring gRPC OAuth2 ROPC Platform

Production-ready OAuth2 ROPC implementation using Spring Boot 3.3, gRPC and GraphQL.

## Architecture
```
GraphQL Client -> spring-grpc-graphql:8080 -> spring-grpc-server:9090 -> PostgreSQL + Redis + Vault
```

## Quick Start
```bash
cp .env.example .env
docker-compose up -d
# GraphiQL: http://localhost:8080/graphiql
```

## Build Locally
```bash
docker-compose up -d postgres redis vault
mvn install -pl spring-grpc-proto
mvn spring-boot:run -pl spring-grpc-server
mvn spring-boot:run -pl spring-grpc-graphql
```

## Example GraphQL Mutation
```graphql
mutation {
  login(input: {username:"admin",password:"admin123",clientId:"dev-client",clientSecret:"client-secret-dev",scope:"read"}){
    accessToken tokenType expiresIn refreshToken
  }
}
```

## Default Dev Credentials
- User: admin / admin123
- Client: dev-client / client-secret-dev

## Tech Stack
- Spring Boot 3.3, Java 21, gRPC 1.63, Spring for GraphQL
- PostgreSQL 16 + Flyway, Redis 7, HashiCorp Vault
- JWT (JJWT 0.12.5), MapStruct, Lombok, Resilience4j, Prometheus
