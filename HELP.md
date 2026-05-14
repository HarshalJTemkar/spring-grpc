# Spring gRPC OAuth2 — Complete Guide for Beginners

> **You are new to gRPC?** No problem. This guide explains every concept, every file, and every decision
> in this project from first principles — then shows you how to run it, deploy it, and extend it.

---

## Table of Contents

1. [What is gRPC? Why use it?](#1-what-is-grpc-why-use-it)
2. [What does this project do?](#2-what-does-this-project-do)
3. [Project Architecture](#3-project-architecture)
4. [Module-by-Module Explanation](#4-module-by-module-explanation)
   - [spring grpc proto](#41-spring-grpc-proto-contract)
   - [spring grpc server](#42-spring-grpc-server-grpc-backend)
   - [spring grpc graphql](#43-spring-grpc-graphql-api-gateway)
5. [How the OAuth2 ROPC Flow Works](#5-how-the-oauth2-ropc-flow-works)
6. [Understanding Every Key File](#6-understanding-every-key-file)
7. [Step-by-Step: Run Locally](#7-step-by-step-run-locally)
8. [Step-by-Step: Deploy with Docker Compose](#8-step-by-step-deploy-with-docker-compose)
9. [Step-by-Step: Deploy to Kubernetes](#9-step-by-step-deploy-to-kubernetes)
10. [How to Add a New gRPC Service](#10-how-to-add-a-new-grpc-service)
11. [How to Add a New GraphQL Operation](#11-how-to-add-a-new-graphql-operation)
12. [How to Add a New gRPC Interceptor](#12-how-to-add-a-new-grpc-interceptor)
13. [How to Connect Another Microservice as a gRPC Client](#13-how-to-connect-another-microservice-as-a-grpc-client)
14. [Security Model Explained](#14-security-model-explained)
15. [Database Migrations (Flyway)](#15-database-migrations-flyway)
16. [Redis Caching Strategy](#16-redis-caching-strategy)
17. [Configuration & Environment Variables](#17-configuration-environment-variables)
18. [Troubleshooting Common Issues](#18-troubleshooting-common-issues)
19. [Glossary](#19-glossary)

---

## 1. What is gRPC? Why use it?

### REST vs gRPC — side by side

| Feature | REST (HTTP/JSON) | gRPC (HTTP/2 + Protobuf) |
|---------|-----------------|--------------------------|
| Protocol | HTTP/1.1 | HTTP/2 |
| Data format | JSON (text, human-readable) | Protobuf (binary, compact) |
| Speed | Slower (text parsing) | ~5-10x faster |
| Type safety | No contract enforced | Strict `.proto` contract |
| Streaming | Limited | Native bi-directional streaming |
| Code generation | Manual or OpenAPI | Automatic from `.proto` |
| Browser support | Native | Needs proxy (grpc-web) |

### Why gRPC in this project?

This project implements an **OAuth2 server**. It handles login, token issuance, and token validation — operations
called by many other services. gRPC is ideal because:

- **Performance** — Token validation is called on every API request. Binary gRPC is much faster than JSON REST.
- **Contract** — The `.proto` file is the single source of truth. Any service calling us MUST match the contract.
- **Code generation** — Client and server stubs are generated automatically. No manual HTTP client code.
- **GraphQL as the public face** — External clients (browsers, mobile) use GraphQL. GraphQL calls gRPC internally.
  This gives you the best of both: friendly public API + fast internal communication.

### How gRPC works — the basics

```
1. You write a .proto file  →  defines your service and messages
2. Maven plugin generates Java code from the .proto file automatically
3. You implement the generated interface (the "server stub")
4. Clients use the generated "blocking stub" to call your server — like a normal Java method call
5. Under the hood: Protobuf serialization + HTTP/2 transport
```

---

## 2. What does this project do?

This project implements **OAuth2 Resource Owner Password Credentials (ROPC)** grant type.

**ROPC** means: a client app sends a username + password directly to the auth server and gets back
a JWT access token + refresh token.

```
Mobile App / Web App
        |
        | POST login mutation (GraphQL)
        |   username, password, clientId, clientSecret
        v
spring-grpc-graphql:8080  (GraphQL Gateway — public facing)
        |
        | PasswordGrant RPC call (gRPC)
        v
spring-grpc-server:9090   (gRPC Auth Server — internal)
        |
        |-- validates client credentials against PostgreSQL
        |-- validates user credentials (BCrypt password check)
        |-- issues JWT access token + refresh token
        |-- stores tokens in PostgreSQL
        |-- caches token in Redis
        v
Returns: { accessToken, refreshToken, expiresIn, tokenType }
```

---

## 3. Project Architecture

```
spring-grpc/
├── pom.xml                        ← Parent POM — manages all dependency versions
├── docker-compose.yml             ← Runs all services + infrastructure locally
├── .env.example                   ← Template for environment variables
├── README.md                      ← Quick-start
├── HELP.md                        ← This file
│
├── spring-grpc-proto/             ← MODULE 1: Shared contract (proto files)
│   └── src/main/proto/
│       ├── common/                ← Reusable message types
│       └── oauth/                 ← Service definitions
│
├── spring-grpc-server/            ← MODULE 2: gRPC Server (the auth logic)
│   └── src/main/java/
│       ├── config/                ← Spring configuration classes
│       ├── domain/                ← JPA entities, enums, repositories
│       ├── service/               ← Business logic
│       ├── grpc/impl/             ← gRPC service implementations
│       ├── grpc/interceptor/      ← gRPC cross-cutting concerns
│       ├── security/              ← JWT + password encoding
│       ├── cache/                 ← Redis token caching
│       ├── exception/             ← Error handling
│       ├── mapper/                ← Object conversion
│       └── util/                  ← Utilities
│
└── spring-grpc-graphql/           ← MODULE 3: GraphQL Gateway (public API)
    └── src/main/java/
        ├── config/                ← Spring + gRPC client config
        ├── resolver/              ← GraphQL query/mutation handlers
        ├── dto/                   ← Input/response data objects
        ├── interceptor/           ← GraphQL cross-cutting concerns
        ├── exception/             ← GraphQL error handling
        └── scalar/                ← Custom GraphQL scalar types
```

### Why three modules instead of one?

| Reason | Explanation |
|--------|-------------|
| **Separation of concerns** | Proto definitions are independent. Any service (Java, Go, Python) can use them. |
| **Independent deployment** | You can deploy the server without the GraphQL gateway and vice versa. |
| **Reusability** | `spring-grpc-proto` can be published to a Maven repository. Other teams import it. |
| **Build efficiency** | Only changed modules rebuild. |

---

## 4. Module-by-Module Explanation

### 4.1 `spring-grpc-proto` — The Contract

**Purpose:** Define WHAT services exist and WHAT data they exchange — language-neutral.

#### Why a separate module?
If you put `.proto` files inside `spring-grpc-server`, then `spring-grpc-graphql` would need to depend on the
whole server just to get the generated stubs. A separate module means only the contract is shared.

#### Proto file structure

```
src/main/proto/
├── common/
│   ├── common.proto    ← Shared types: PageRequest, PageInfo, Timestamp
│   └── error.proto     ← Error types: ErrorDetail, ErrorResponse
└── oauth/
    ├── auth_service.proto      ← AuthService: PasswordGrant, RevokeToken
    ├── token_service.proto     ← TokenService: RefreshToken, IntrospectToken
    ├── user_service.proto      ← UserService: GetUser, ListUsers
    └── resource_service.proto  ← ResourceService: ValidateToken
```

#### Reading a proto file

```protobuf
// auth_service.proto

syntax = "proto3";                              // always proto3 in modern projects
package oauth;                                  // proto package (like Java package)
option java_package = "com.springgrpc.grpc.oauth";  // Java package for generated code
option java_multiple_files = true;              // generate one .java file per message

// A "message" is like a Java class / record
message PasswordGrantRequest {
  string username = 1;       // field number 1 — used in binary encoding (NOT the value!)
  string password = 2;       // field number 2
  string client_id = 3;      // field number 3
  string client_secret = 4;
  string scope = 5;
}

// A "service" defines the RPC methods — like a Java interface
service AuthService {
  rpc PasswordGrant (PasswordGrantRequest) returns (OAuthTokenResponse);
  rpc RevokeToken   (RevokeTokenRequest)   returns (RevokeTokenResponse);
}
```

> **Field numbers (= 1, = 2...)** are NOT values. They identify fields in the binary wire format.
> Once deployed, **never change or reuse field numbers** — it breaks backwards compatibility.

#### How Maven generates Java code

In `spring-grpc-proto/pom.xml`, the `protobuf-maven-plugin` is configured. When you run `mvn compile`:

1. It downloads the `protoc` compiler automatically
2. It runs `protoc` on every `.proto` file
3. It generates Java classes in `target/generated-sources/protobuf/java/`
4. It generates gRPC stubs in `target/generated-sources/protobuf/grpc-java/`

Generated for `auth_service.proto`:
- `PasswordGrantRequest.java` — the request message class
- `OAuthTokenResponse.java` — the response message class
- `AuthServiceGrpc.java` — contains:
  - `AuthServiceGrpc.AuthServiceImplBase` — extend this to implement the server
  - `AuthServiceGrpc.AuthServiceBlockingStub` — use this to call the server as a client

---

### 4.2 `spring-grpc-server` — The gRPC Backend

**Purpose:** Implement the gRPC services. Contains all auth business logic.

**Runs on:** port `9090` (gRPC) + port `8081` (HTTP for actuator/health)

#### Layer-by-layer walkthrough

##### Layer 1: Domain (`domain/`)

```
domain/
├── entity/
│   ├── UserEntity.java          ← Maps to "users" table in PostgreSQL
│   ├── ClientEntity.java        ← Maps to "oauth_clients" table
│   ├── TokenEntity.java         ← Maps to "tokens" table (access tokens)
│   └── RefreshTokenEntity.java  ← Maps to "refresh_tokens" table
├── enums/
│   ├── UserStatus.java          ← ACTIVE, LOCKED, INACTIVE
│   ├── TokenType.java           ← ACCESS_TOKEN, REFRESH_TOKEN
│   ├── GrantType.java           ← PASSWORD, REFRESH_TOKEN
│   └── ErrorCode.java           ← All business error codes
└── repository/
    ├── UserRepository.java      ← JPA repository — findByUsername(), findById()
    ├── ClientRepository.java    ← findById(clientId)
    ├── TokenRepository.java     ← findByTokenValue()
    └── RefreshTokenRepository.java
```

**Why JPA entities?** PostgreSQL is the persistent store. JPA maps Java objects to database rows.
Spring Data JPA repositories give you `save()`, `findById()`, custom queries — without writing SQL.

##### Layer 2: Service (`service/`)

```
service/
├── AuthenticationService.java    ← Orchestrates the ROPC login flow
├── ClientValidationService.java  ← Validates OAuth client credentials
├── UserService.java              ← Finds and validates users
├── TokenService.java             ← Creates access + refresh tokens (JWT)
└── TokenRevocationService.java   ← Marks tokens as revoked
```

**Why a service layer?** The service layer contains business logic.
The gRPC impl layer should only translate between proto messages and Java calls — no business logic there.
This separation makes the code testable (you can test `AuthenticationService` without gRPC).

**How `AuthenticationService.authenticate()` works:**

```
1. clientValidationService.validateClient(clientId, clientSecret)
   → Looks up client in DB
   → BCrypt checks clientSecret against stored hash

2. clientValidationService.validateScope(client, scope)
   → Checks requested scope is in client's allowed scopes

3. userService.findByUsername(username)
   → Throws OAuthException(USER_NOT_FOUND) if not found

4. userService.validateUserActive(user)
   → Throws OAuthException(USER_LOCKED) or (USER_INACTIVE)

5. passwordEncoderService.matches(password, user.getPasswordHash())
   → BCrypt check. Throws OAuthException(INVALID_CREDENTIALS) if wrong.

6. tokenService.createAccessToken(userId, clientId, roles, scopes)
   → Generates JWT with claims
   → Saves TokenEntity to PostgreSQL
   → Caches in Redis

7. tokenService.createRefreshToken(userId, clientId, accessTokenId)
   → Generates refresh JWT
   → Saves RefreshTokenEntity to PostgreSQL

8. Returns TokenPair(accessToken, refreshToken)
```

##### Layer 3: gRPC Implementation (`grpc/impl/`)

```
grpc/impl/
├── AuthGrpcServiceImpl.java      ← extends AuthServiceGrpc.AuthServiceImplBase
├── TokenGrpcServiceImpl.java     ← extends TokenServiceGrpc.TokenServiceImplBase
├── UserGrpcServiceImpl.java      ← extends UserServiceGrpc.UserServiceImplBase
└── ResourceGrpcServiceImpl.java  ← extends ResourceServiceGrpc.ResourceServiceImplBase
```

Each class extends the generated `*ImplBase` class and overrides the RPC methods.

```java
// How a gRPC service implementation looks
@GrpcService                          // ← tells grpc-spring-boot-starter to register this
@RequiredArgsConstructor
public class AuthGrpcServiceImpl extends AuthServiceGrpc.AuthServiceImplBase {

    private final AuthenticationService authenticationService;

    @Override
    public void passwordGrant(PasswordGrantRequest request,
                              StreamObserver<OAuthTokenResponse> responseObserver) {
        // 1. Call the service layer
        TokenPair pair = authenticationService.authenticate(
            request.getUsername(), request.getPassword(),
            request.getClientId(), request.getClientSecret(), request.getScope());

        // 2. Build the proto response
        OAuthTokenResponse response = OAuthTokenResponse.newBuilder()
            .setAccessToken(pair.accessToken().getTokenValue())
            .setTokenType("Bearer")
            .setExpiresIn(3600)
            .build();

        // 3. Send response — always call onNext() then onCompleted()
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
```

> **StreamObserver pattern:** gRPC uses an observer pattern. `onNext()` sends a response,
> `onCompleted()` signals success, `onError()` signals failure.

##### Layer 4: Interceptors (`grpc/interceptor/`)

gRPC interceptors are like Spring MVC filters — they run before/after every RPC call.

```
interceptor/
├── CorrelationIdInterceptor.java  ← Reads/generates X-Correlation-ID header for tracing
├── LoggingInterceptor.java        ← Logs every RPC call and its duration
├── AuthInterceptor.java           ← Validates Bearer token on protected endpoints
└── RateLimitInterceptor.java      ← Limits requests per second (stub for extension)
```

**Why interceptors instead of putting logic in each service?**
Cross-cutting concerns (logging, auth, tracing) would mean duplicating code in every RPC method.
Interceptors apply once globally.

##### Layer 5: Security (`security/`)

```
security/
├── JwtTokenProvider.java       ← Creates and parses JWT tokens using JJWT library
├── PasswordEncoderService.java ← BCrypt encoding and verification
└── TokenClaims.java            ← Value object for JWT claims (userId, roles, scopes...)
```

**What is a JWT?**
A JSON Web Token is a self-contained, signed token. It has three parts separated by `.`:
- **Header** — algorithm used (HS256)
- **Payload** — claims (userId, roles, expiry)
- **Signature** — HMAC of header+payload using the secret key

Any service holding the secret key can verify the token without calling the auth server.
This is why token validation is fast — no database lookup needed.

##### Layer 6: Cache (`cache/`)

```
cache/
├── TokenCacheService.java   ← Redis: stores active tokens, manages blacklist
└── UserCacheService.java    ← Redis: caches user objects to reduce DB calls
```

**Why Redis?**
- Token validation happens on EVERY request to EVERY service
- Going to PostgreSQL each time would be slow
- Redis is an in-memory store — lookups are ~0.1ms vs ~2ms for DB
- Token blacklist: when a token is revoked, it's added to Redis with TTL = remaining lifetime

##### Layer 7: Exception Handling (`exception/`)

```
exception/
├── OAuthException.java       ← Business exception with an ErrorCode
├── GrpcStatusMapper.java     ← Maps OAuthException → gRPC Status code
└── GrpcExceptionAdvice.java  ← @GrpcAdvice catches exceptions, converts to gRPC status
```

**gRPC Status codes** (like HTTP status codes but for gRPC):

| gRPC Status | Meaning | When used |
|-------------|---------|-----------|
| `UNAUTHENTICATED` | Invalid credentials | Wrong password, invalid token |
| `PERMISSION_DENIED` | Not allowed | Invalid scope |
| `NOT_FOUND` | Resource missing | User not found |
| `FAILED_PRECONDITION` | Invalid state | User locked/inactive |
| `INTERNAL` | Server error | Unexpected exceptions |

---

### 4.3 `spring-grpc-graphql` — The API Gateway

**Purpose:** Expose a public GraphQL API. Translate GraphQL operations into gRPC calls.

**Runs on:** port `8080`

**Why GraphQL as the gateway?**
- gRPC is not browser-friendly (requires HTTP/2 + special client libraries)
- GraphQL is universally accessible from browsers, mobile apps, curl
- GraphQL lets clients ask for exactly the fields they need
- GraphQL playground (GraphiQL) at `/graphiql` for interactive testing

#### How a GraphQL mutation becomes a gRPC call

```
Browser sends GraphQL mutation:
  mutation { login(input: { username: "alice", password: "pass", ... }) { accessToken } }
        ↓
AuthMutationResolver.login(PasswordGrantInput input)
        ↓
  Builds PasswordGrantRequest (proto message)
  Calls authStub.passwordGrant(request)  ← gRPC blocking call
        ↓
spring-grpc-server receives PasswordGrant RPC
        ↓
  Returns OAuthTokenResponse (proto message)
        ↓
AuthMutationResolver converts to TokenResponse (Java DTO)
        ↓
Browser receives JSON: { "data": { "login": { "accessToken": "eyJ..." } } }
```

#### GraphQL Schema files (`*.graphqls`)

```graphql
# auth.graphqls — defines the login and revokeToken mutations
type Mutation {
  login(input: PasswordGrantInput!): TokenResponse!
  revokeToken(input: RevokeTokenInput!): Boolean!
}

# token.graphqls — extends Mutation and Query (GraphQL allows schema stitching)
extend type Mutation {
  refreshToken(input: RefreshTokenInput!): TokenResponse!
}

extend type Query {
  introspect(input: IntrospectTokenInput!): IntrospectionResponse!
}

# user.graphqls — defines the user query
type Query {
  user(userId: String!): UserResponse
}
```

> The `extend` keyword lets you add operations across multiple files. This keeps schema files small and focused.

#### gRPC Client Configuration (`GrpcClientConfig.java`)

```java
@GrpcClient("grpc-server")                              // "grpc-server" is the name in application.yml
private AuthServiceGrpc.AuthServiceBlockingStub stub;   // inject the generated stub
```

In `application.yml`:
```yaml
grpc:
  client:
    grpc-server:                                        # name must match @GrpcClient("grpc-server")
      address: static://localhost:9090                  # where the gRPC server is
      negotiation-type: plaintext                       # no TLS (dev only)
```

---

## 5. How the OAuth2 ROPC Flow Works

```
┌─────────────┐         ┌──────────────────┐         ┌─────────────────┐
│  Client App │         │ GraphQL Gateway  │         │  gRPC Server    │
│  (browser)  │         │  :8080           │         │  :9090          │
└──────┬──────┘         └────────┬─────────┘         └────────┬────────┘
       │                         │                            │
       │  1. login mutation      │                            │
       │  {username,password,    │                            │
       │   clientId,secret}      │                            │
       │────────────────────────►│                            │
       │                         │  2. PasswordGrant RPC     │
       │                         │────────────────────────────►
       │                         │                            │  3. Validate client (DB)
       │                         │                            │  4. Validate user (DB)
       │                         │                            │  5. BCrypt password check
       │                         │                            │  6. Generate JWT
       │                         │                            │  7. Save tokens (DB+Redis)
       │                         │  8. OAuthTokenResponse    │
       │                         │◄────────────────────────────
       │  9. TokenResponse JSON  │                            │
       │◄────────────────────────│                            │
       │                         │                            │
       │  10. Later: API call    │                            │
       │  Authorization: Bearer  │                            │
       │  <accessToken>          │                            │
       │────────────────────────►│                            │
       │                         │  11. ValidateToken RPC    │
       │                         │  (check Redis blacklist   │
       │                         │   + verify JWT signature) │
       │                         │────────────────────────────►
       │                         │◄────────────────────────────
       │  12. API Response       │                            │
       │◄────────────────────────│                            │
```

---

## 6. Understanding Every Key File

### Parent `pom.xml` (root)

```xml
<!-- Why a parent POM? -->
<!-- All 3 modules inherit the same dependency versions -->
<!-- Change a version in ONE place, it applies everywhere -->

<dependencyManagement>
  <dependencies>
    <!-- Spring Boot BOM — manages 300+ Spring dependency versions -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-dependencies</artifactId>
      <version>3.3.0</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
    <!-- gRPC BOM — manages grpc-core, grpc-stub, grpc-netty versions -->
    ...
  </dependencies>
</dependencyManagement>
```

### `GrpcServerApplication.java`

```java
@SpringBootApplication   // = @Configuration + @EnableAutoConfiguration + @ComponentScan
public class GrpcServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(GrpcServerApplication.class, args);
        // Spring Boot auto-configures gRPC server because grpc-server-spring-boot-starter is on classpath
        // It scans for @GrpcService classes and registers them automatically
    }
}
```

### `SecurityConfig.java`

```java
@Configuration @EnableWebSecurity
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder(12);
        // 12 = cost factor. Higher = slower = more secure.
        // BCrypt deliberately slow to defeat brute-force attacks.
        // Never store plain text passwords!
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())        // gRPC/JWT = stateless, CSRF not needed
            .sessionManagement(s -> s
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))  // no HTTP sessions
            .authorizeHttpRequests(a -> a
                .requestMatchers("/actuator/**").permitAll()  // health/metrics open
                .anyRequest().permitAll())                   // gRPC handles its own auth
            .build();
    }
}
```

### `JwtTokenProvider.java`

```java
// JWT = 3 base64url-encoded parts joined by dots
// eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyMSIsInJvbGVzIjpbIlJPTEVfVVNFUiJdfQ.xyz

public String generateAccessToken(TokenClaims claims) {
    return Jwts.builder()
        .subject(claims.getSub())              // "sub" = subject = userId
        .claim("roles", claims.getRoles())     // custom claim
        .claim("scopes", claims.getScopes())   // what the token is allowed to do
        .issuedAt(new Date())                  // "iat" = issued at
        .expiration(new Date(now + expirationMs))  // "exp" = expiry
        .signWith(getSigningKey())             // HMAC-SHA256 signature
        .compact();                            // serialize to string
}
```

### `CorrelationIdInterceptor.java`

```java
// Why correlation IDs?
// When a request flows through multiple services (GraphQL → gRPC → DB),
// you want ONE ID that appears in ALL log lines across ALL services.
// This lets you trace a request end-to-end in your log aggregator (e.g., Kibana).

@GrpcGlobalServerInterceptor   // applies to ALL gRPC services automatically
public class CorrelationIdInterceptor implements ServerInterceptor {
    @Override
    public <T, R> ServerCall.Listener<T> interceptCall(
            ServerCall<T, R> call, Metadata headers, ServerCallHandler<T, R> next) {

        String id = headers.get(CORRELATION_ID_KEY);
        if (id == null) id = UUID.randomUUID().toString();  // generate if not provided
        CorrelationIdHolder.set(id);                         // store in ThreadLocal
        try {
            return next.startCall(call, headers);
        } finally {
            CorrelationIdHolder.clear();                     // always clean up ThreadLocal!
        }
    }
}
```

### `V1__init_schema.sql`

```sql
-- Flyway runs this automatically on first startup
-- Naming: V{version}__{description}.sql
-- V1, V2, V3... must be sequential

CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,         -- UUID as string
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL, -- NEVER store plain text!
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    ...
);
```

> **Why Flyway?** Without it, you need to manually run SQL scripts when deploying.
> Flyway tracks which scripts have run (in a `flyway_schema_history` table) and runs only new ones.
> This makes database migrations reproducible and version-controlled.

---

## 7. Step-by-Step: Run Locally

### Prerequisites

```bash
# Check you have these installed:
java -version    # must be 21+
mvn -version     # must be 3.9+
docker --version # must be 20+
```

### Step 1 — Start infrastructure

```bash
cd spring-grpc

# Start PostgreSQL, Redis, and Vault only (not the apps yet)
docker-compose up -d postgres redis vault

# Verify they're running
docker-compose ps
```

### Step 2 — Configure environment

```bash
# Copy the example env file
copy .env.example .env          # Windows
# cp .env.example .env          # Mac/Linux

# Edit .env — at minimum change JWT_SECRET to something long (32+ chars)
# For local dev the defaults are fine
```

### Step 3 — Build the proto module first

```bash
# The proto module MUST be built first — server and graphql modules depend on its generated code
mvn clean install -pl spring-grpc-proto

# This generates Java classes from .proto files into:
# spring-grpc-proto/target/generated-sources/protobuf/
```

### Step 4 — Start the gRPC server

```bash
# In a new terminal:
mvn spring-boot:run -pl spring-grpc-server -Dspring-boot.run.profiles=dev

# You should see:
# Started GrpcServerApplication
# gRPC Server started, listening on port 9090
# Flyway: Successfully applied 2 migrations (V1, V2)
```

### Step 5 — Start the GraphQL gateway

```bash
# In another terminal:
mvn spring-boot:run -pl spring-grpc-graphql

# You should see:
# Started GraphqlGatewayApplication on port 8080
```

### Step 6 — Test with GraphiQL

Open your browser: **http://localhost:8080/graphiql**

```graphql
# Test login
mutation {
  login(input: {
    username: "admin"
    password: "admin123"
    clientId: "dev-client"
    clientSecret: "client-secret-dev"
    scope: "read"
  }) {
    accessToken
    tokenType
    expiresIn
    refreshToken
  }
}
```

```graphql
# Test token refresh
mutation {
  refreshToken(input: {
    refreshToken: "<paste refresh token from login>"
    clientId: "dev-client"
    clientSecret: "client-secret-dev"
  }) {
    accessToken
    expiresIn
  }
}
```

```graphql
# Test token introspection
query {
  introspect(input: {
    token: "<paste access token>"
    clientId: "dev-client"
    clientSecret: "client-secret-dev"
  }) {
    active
    sub
    exp
  }
}
```

---

## 8. Step-by-Step: Deploy with Docker Compose

```bash
# 1. Build JAR files
mvn clean package -DskipTests

# 2. Copy JARs to where Dockerfiles expect them
# (Dockerfiles use: COPY target/spring-grpc-server.jar app.jar)

# 3. Start everything
docker-compose up -d

# 4. View logs
docker-compose logs -f grpc-server
docker-compose logs -f graphql-gateway

# 5. Stop everything
docker-compose down

# 6. Stop and remove volumes (wipes database!)
docker-compose down -v
```

### docker-compose.yml explained

```yaml
services:
  postgres:          # PostgreSQL database
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: oauthdb      # database name
      POSTGRES_USER: oauthuser  # username
      POSTGRES_PASSWORD: oauthpass

  redis:             # Redis cache
    image: redis:7-alpine

  vault:             # HashiCorp Vault — secrets management
    image: hashicorp/vault:1.16
    environment:
      VAULT_DEV_ROOT_TOKEN_ID: myroot   # dev mode token (never use in prod!)

  grpc-server:       # Our gRPC auth server
    build: ./spring-grpc-server   # builds from Dockerfile in that directory
    ports: ["9090:9090"]          # host:container
    depends_on: [postgres, redis, vault]  # waits for these to start first

  graphql-gateway:   # Our GraphQL gateway
    build: ./spring-grpc-graphql
    ports: ["8080:8080"]
    depends_on: [grpc-server]
```

---

## 9. Step-by-Step: Deploy to Kubernetes

### Step 1 — Build and push Docker images

```bash
# Build images
docker build -t your-registry/spring-grpc-server:1.0.0 ./spring-grpc-server
docker build -t your-registry/spring-grpc-graphql:1.0.0 ./spring-grpc-graphql

# Push to registry
docker push your-registry/spring-grpc-server:1.0.0
docker push your-registry/spring-grpc-graphql:1.0.0
```

### Step 2 — Create Kubernetes manifests

Create `kubernetes/grpc-server-deployment.yaml`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: grpc-server
spec:
  replicas: 2
  selector:
    matchLabels:
      app: grpc-server
  template:
    metadata:
      labels:
        app: grpc-server
    spec:
      containers:
      - name: grpc-server
        image: your-registry/spring-grpc-server:1.0.0
        ports:
        - containerPort: 9090   # gRPC
        - containerPort: 8081   # HTTP (actuator)
        env:
        - name: DB_HOST
          value: "postgres-service"
        - name: REDIS_HOST
          value: "redis-service"
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:        # use Kubernetes Secret, NOT plain text!
              name: jwt-secret
              key: value
---
apiVersion: v1
kind: Service
metadata:
  name: grpc-server-service
spec:
  selector:
    app: grpc-server
  ports:
  - name: grpc
    port: 9090
    targetPort: 9090
  - name: http
    port: 8081
    targetPort: 8081
```

### Step 3 — Apply to cluster

```bash
kubectl apply -f kubernetes/
kubectl rollout status deployment/grpc-server
kubectl get pods
kubectl logs -f deployment/grpc-server
```

### Production checklist

- [ ] Set `grpc.server.security.enabled=true` and configure TLS certificates
- [ ] Use Kubernetes Secrets (or Vault) for JWT_SECRET, DB_PASSWORD
- [ ] Set `spring.flyway.locations` to point to versioned migration path
- [ ] Configure resource limits (`resources.requests`, `resources.limits`)
- [ ] Add liveness/readiness probes pointing to `/actuator/health`
- [ ] Set `SPRING_PROFILES_ACTIVE=prod` (disables debug logging, enables validation)

---

## 10. How to Add a New gRPC Service

This is the most important skill. Follow these **7 steps** every time.

### Example: Adding an `AuditService` that logs access events

#### Step 1 — Define the proto contract

Create `spring-grpc-proto/src/main/proto/oauth/audit_service.proto`:

```protobuf
syntax = "proto3";
package oauth;
option java_package = "com.springgrpc.grpc.oauth";
option java_outer_classname = "AuditServiceProto";
option java_multiple_files = true;

import "common/common.proto";

message AuditLogRequest {
  string user_id   = 1;
  string action    = 2;    // e.g., "LOGIN", "TOKEN_REFRESH"
  string ip_address = 3;
  int64  timestamp = 4;
}

message AuditLogResponse {
  string audit_id = 1;
  bool   success  = 2;
}

service AuditService {
  rpc LogEvent (AuditLogRequest) returns (AuditLogResponse);
}
```

#### Step 2 — Rebuild the proto module

```bash
mvn clean install -pl spring-grpc-proto
# Java classes AuditLogRequest, AuditLogResponse, AuditServiceGrpc are now generated
```

#### Step 3 — Create the domain/entity (if needed)

Create `spring-grpc-server/src/main/java/com/springgrpc/server/domain/entity/AuditLogEntity.java`:

```java
@Entity @Table(name = "audit_logs")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AuditLogEntity {
    @Id private String auditId;
    private String userId;
    private String action;
    private String ipAddress;
    private Instant timestamp;
}
```

#### Step 4 — Add a Flyway migration for the new table

Create `spring-grpc-server/src/main/resources/db/migration/V3__add_audit_logs.sql`:

```sql
CREATE TABLE audit_logs (
    audit_id   VARCHAR(36) PRIMARY KEY,
    user_id    VARCHAR(36) NOT NULL,
    action     VARCHAR(100) NOT NULL,
    ip_address VARCHAR(50),
    timestamp  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_audit_user_id ON audit_logs(user_id);
```

> **Important:** Never modify V1 or V2. Always add a NEW migration file with the next version number.

#### Step 5 — Create the service

Create `spring-grpc-server/src/main/java/com/springgrpc/server/service/AuditService.java`:

```java
@Service @RequiredArgsConstructor
public class AuditService {
    private final AuditLogRepository auditLogRepository;

    public String logEvent(String userId, String action, String ipAddress) {
        AuditLogEntity log = AuditLogEntity.builder()
            .auditId(UUID.randomUUID().toString())
            .userId(userId)
            .action(action)
            .ipAddress(ipAddress)
            .timestamp(Instant.now())
            .build();
        auditLogRepository.save(log);
        return log.getAuditId();
    }
}
```

#### Step 6 — Implement the gRPC service

Create `spring-grpc-server/src/main/java/com/springgrpc/server/grpc/impl/AuditGrpcServiceImpl.java`:

```java
@GrpcService                           // ← REQUIRED: registers this as a gRPC service
@RequiredArgsConstructor
public class AuditGrpcServiceImpl extends AuditServiceGrpc.AuditServiceImplBase {

    private final AuditService auditService;

    @Override
    public void logEvent(AuditLogRequest request,
                         StreamObserver<AuditLogResponse> responseObserver) {
        String auditId = auditService.logEvent(
            request.getUserId(),
            request.getAction(),
            request.getIpAddress()
        );
        responseObserver.onNext(AuditLogResponse.newBuilder()
            .setAuditId(auditId)
            .setSuccess(true)
            .build());
        responseObserver.onCompleted();
    }
}
```

#### Step 7 — Test it

```bash
# Rebuild and restart the server
mvn spring-boot:run -pl spring-grpc-server -Dspring-boot.run.profiles=dev

# Test using grpcurl (install: https://github.com/fullstorydev/grpcurl)
grpcurl -plaintext -d '{"user_id":"u1","action":"LOGIN","ip_address":"127.0.0.1"}' \
  localhost:9090 oauth.AuditService/LogEvent
```

---

## 11. How to Add a New GraphQL Operation

### Example: Add a `listUsers` query

#### Step 1 — Add to the GraphQL schema

Edit or create `spring-grpc-graphql/src/main/resources/graphql/user.graphqls`:

```graphql
extend type Query {
  listUsers(page: Int, size: Int): [UserResponse!]!
}
```

#### Step 2 — Add to the proto (if not already there)

In `user_service.proto`, add:

```protobuf
message ListUsersRequest {
  int32 page = 1;
  int32 size = 2;
}

message ListUsersResponse {
  repeated UserProfile users = 1;   // "repeated" = list/array
  int32 total_count = 2;
}

service UserService {
  rpc GetUser    (GetUserRequest)    returns (UserProfile);
  rpc ListUsers  (ListUsersRequest)  returns (ListUsersResponse);  // ← new
}
```

Rebuild proto: `mvn clean install -pl spring-grpc-proto`

#### Step 3 — Implement in the gRPC server

Add `listUsers()` to `UserGrpcServiceImpl.java`:

```java
@Override
public void listUsers(ListUsersRequest request,
                      StreamObserver<ListUsersResponse> responseObserver) {
    List<UserEntity> users = userService.listUsers(request.getPage(), request.getSize());
    ListUsersResponse.Builder builder = ListUsersResponse.newBuilder();
    users.forEach(u -> builder.addUsers(
        UserProfile.newBuilder().setUserId(u.getId()).setUsername(u.getUsername()).build()
    ));
    responseObserver.onNext(builder.build());
    responseObserver.onCompleted();
}
```

#### Step 4 — Add the GraphQL resolver in the gateway

Add to `UserQueryResolver.java`:

```java
@QueryMapping
public List<UserResponse> listUsers(@Argument Integer page, @Argument Integer size) {
    ListUsersResponse resp = userStub.listUsers(
        ListUsersRequest.newBuilder()
            .setPage(page != null ? page : 0)
            .setSize(size != null ? size : 20)
            .build());
    return resp.getUsersList().stream()
        .map(p -> UserResponse.builder()
            .userId(p.getUserId())
            .username(p.getUsername())
            .build())
        .collect(Collectors.toList());
}
```

---

## 12. How to Add a New gRPC Interceptor

Interceptors run before/after EVERY RPC call. Use them for cross-cutting concerns.

### Example: Add a `MetricsInterceptor` that counts RPC calls

```java
// spring-grpc-server/src/main/java/com/springgrpc/server/grpc/interceptor/MetricsInterceptor.java

@GrpcGlobalServerInterceptor    // ← applies to ALL services automatically
@RequiredArgsConstructor
@Slf4j
public class MetricsInterceptor implements ServerInterceptor {

    private final MeterRegistry meterRegistry;  // Micrometer (injected by Spring)

    @Override
    public <T, R> ServerCall.Listener<T> interceptCall(
            ServerCall<T, R> call, Metadata headers, ServerCallHandler<T, R> next) {

        String methodName = call.getMethodDescriptor().getFullMethodName();
        long startTime = System.currentTimeMillis();

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<T>(
                next.startCall(call, headers)) {
            @Override
            public void onComplete() {
                long duration = System.currentTimeMillis() - startTime;
                // Record metric: grpc_server_calls_total{method="oauth.AuthService/PasswordGrant"}
                meterRegistry.counter("grpc.server.calls.total",
                    "method", methodName).increment();
                meterRegistry.timer("grpc.server.call.duration",
                    "method", methodName).record(duration, TimeUnit.MILLISECONDS);
                super.onComplete();
            }
        };
    }
}
```

> No registration needed — `@GrpcGlobalServerInterceptor` makes grpc-spring-boot-starter
> pick it up automatically.

---

## 13. How to Connect Another Microservice as a gRPC Client

Say you have a separate `order-service` and it needs to validate tokens before processing orders.

### Step 1 — Add the proto dependency to order-service's pom.xml

```xml
<dependency>
    <groupId>com.springgrpc</groupId>
    <artifactId>spring-grpc-proto</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
<dependency>
    <groupId>net.devh</groupId>
    <artifactId>grpc-client-spring-boot-starter</artifactId>
    <version>3.1.0.RELEASE</version>
</dependency>
```

### Step 2 — Configure the gRPC client address

In `order-service/src/main/resources/application.yml`:

```yaml
grpc:
  client:
    spring-grpc-server:              # any name you choose
      address: static://auth-service:9090   # or service discovery address
      negotiation-type: plaintext    # plaintext for internal; tls for production
```

### Step 3 — Inject and use the stub

```java
@Service
public class TokenValidationService {

    @GrpcClient("spring-grpc-server")   // matches name in application.yml
    private ResourceServiceGrpc.ResourceServiceBlockingStub resourceStub;

    public boolean isTokenValid(String accessToken) {
        ValidateTokenResponse response = resourceStub.validateToken(
            ValidateTokenRequest.newBuilder()
                .setAccessToken(accessToken)
                .build());
        return response.getValid();
    }
}
```

That's it. The gRPC client is ready to use in any Spring service.

---

## 14. Security Model Explained

### Layers of security

```
Request
   │
   ▼
[1] GraphQL Gateway — Spring Security
    ├── /graphql endpoint open (auth via token in mutation input)
    ├── /graphiql open for dev (disable in prod)
    └── /actuator open for health checks

   │  gRPC call with Bearer token in metadata
   ▼
[2] AuthInterceptor (gRPC)
    ├── Reads "authorization" metadata header
    ├── If present: validates JWT signature
    └── If invalid: closes call with UNAUTHENTICATED

   │
   ▼
[3] TokenCacheService — Redis blacklist check
    ├── Looks up token ID in Redis blacklist
    └── If found: token was revoked

   │
   ▼
[4] JwtTokenProvider — Claims extraction
    ├── Parses JWT, extracts userId, roles, scopes
    └── Checks expiry
```

### Passwords

All passwords stored as **BCrypt hashes** with cost factor 12.
BCrypt is deliberately slow — it takes ~250ms to hash, making brute-force attacks impractical.

```
Plain: "admin123"
BCrypt: "$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQyCc3DsX4TBHiWaVgVB9dqq2"
```

### JWT Claims

```json
{
  "sub": "user-admin-001",
  "userId": "user-admin-001",
  "clientId": "dev-client",
  "roles": ["ROLE_ADMIN", "ROLE_USER"],
  "scopes": ["read", "write"],
  "iat": 1715700000,
  "exp": 1715703600
}
```

### Production security hardening

1. **Enable TLS on gRPC:**
   ```yaml
   grpc:
     server:
       security:
         enabled: true
         certificate-chain: classpath:tls/server.crt
         private-key: classpath:tls/server.key
   ```

2. **Store secrets in Vault** (already wired via `VaultConfig.java`):
   ```yaml
   spring:
     cloud:
       vault:
         uri: https://vault.your-company.com
         token: ${VAULT_TOKEN}
   ```

3. **Rotate JWT secret** regularly — set `JWT_SECRET` environment variable

---

## 15. Database Migrations (Flyway)

### How Flyway works

1. On startup, Flyway checks the `flyway_schema_history` table
2. It compares migration files on classpath vs already-applied versions
3. Runs any pending migrations in version order
4. Never runs the same migration twice

### Migration file naming

```
V{version}__{description}.sql

V1__init_schema.sql      ← creates all initial tables
V2__seed_data.sql        ← inserts default admin user and dev client
V3__add_audit_logs.sql   ← future addition
```

### Rules

- **Never modify** an already-applied migration file
- **Never delete** a migration file
- **Always increment** the version number
- Test migrations against a copy of production data before deploying

---

## 16. Redis Caching Strategy

### Token blacklist (revocation)

```
When token is revoked:
  → SET token:blacklist:{tokenId}  "1"  EX {remaining_seconds}

When token is used:
  → EXISTS token:blacklist:{tokenId}
  → if exists: reject with REVOKED_TOKEN error
```

### Active token cache

```
When token is created:
  → SET token:active:{tokenId}  {jwtString}  EX {expiry_seconds}

When token needs validation (fast path):
  → GET token:active:{tokenId}
  → if found: parse JWT from cache (skip DB lookup)
  → if not found: fall through to DB
```

### Why this matters

Without Redis:
- Every API call → DB query to check if token is revoked → ~2ms per call

With Redis:
- Every API call → Redis lookup → ~0.1ms per call
- For 1000 req/s, this saves ~1.9 seconds of DB load per second

---

## 17. Configuration & Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_HOST` | `localhost` | PostgreSQL hostname |
| `DB_PORT` | `5432` | PostgreSQL port |
| `DB_NAME` | `oauthdb` | Database name |
| `DB_USER` | `oauthuser` | Database username |
| `DB_PASSWORD` | `oauthpass` | Database password |
| `REDIS_HOST` | `localhost` | Redis hostname |
| `REDIS_PORT` | `6379` | Redis port |
| `GRPC_SERVER_PORT` | `9090` | gRPC server port |
| `JWT_SECRET` | (default — change!) | HMAC-SHA256 signing key (min 32 chars) |
| `JWT_EXPIRATION_MS` | `3600000` | Access token lifetime (1 hour) |
| `JWT_REFRESH_EXPIRATION_MS` | `86400000` | Refresh token lifetime (24 hours) |
| `GRPC_CLIENT_ADDRESS` | `static://localhost:9090` | Used by GraphQL gateway |
| `SERVER_PORT` | `8080` | GraphQL gateway HTTP port |

### Spring profiles

| Profile | `ddl-auto` | SQL Logging | Log Level |
|---------|------------|-------------|-----------|
| `dev`   | `create-drop` (recreates on restart) | enabled | DEBUG |
| `prod`  | `validate` (schema must match) | disabled | WARN |

Activate: `SPRING_PROFILES_ACTIVE=dev` or `SPRING_PROFILES_ACTIVE=prod`

---

## 18. Troubleshooting Common Issues

### ❌ `UNAVAILABLE: Connection refused` when GraphQL calls gRPC server

**Cause:** gRPC server not running, or wrong address.

```bash
# Check server is running
curl http://localhost:8081/actuator/health

# Check address in graphql application.yml
grpc.client.grpc-server.address=static://localhost:9090
```

### ❌ `Status{code=UNAUTHENTICATED, description=Invalid token}`

**Cause:** JWT secret mismatch between server and client, or token expired.

```bash
# Check both modules use the same JWT_SECRET
# Check token expiry — default is 1 hour
```

### ❌ Flyway migration fails on startup

**Cause:** SQL syntax error, or you modified an already-applied migration.

```bash
# Never edit V1 or V2 if they've been applied!
# Check flyway_schema_history table:
docker exec -it spring-grpc-postgres-1 psql -U oauthuser -d oauthdb -c "SELECT * FROM flyway_schema_history;"
```

### ❌ `proto` classes not found after changing .proto files

```bash
# You must rebuild the proto module first
mvn clean install -pl spring-grpc-proto

# Then rebuild dependents
mvn clean compile -pl spring-grpc-server,spring-grpc-graphql
```

### ❌ gRPC service not registered (no methods found)

**Cause:** Missing `@GrpcService` annotation or class not in component scan path.

```java
@GrpcService            // ← this annotation MUST be present
@RequiredArgsConstructor
public class MyServiceImpl extends MyServiceGrpc.MyServiceImplBase {
```

### ❌ `NullPointerException` on gRPC proto string fields

**Cause:** Proto3 strings default to `""` (empty string), not `null`. Calling `.isEmpty()` is safe but always check.

```java
// Safe pattern:
String scope = request.getScope();
if (scope != null && scope.length() > 0) { ... }

// Proto3 guarantee: string fields are never null, default is ""
```

---

## 19. Glossary

| Term | Meaning |
|------|---------|
| **gRPC** | Google Remote Procedure Call — framework for calling methods on remote servers |
| **Protobuf** | Protocol Buffers — Google's binary serialization format used by gRPC |
| **`.proto` file** | Contract file defining messages and services |
| **Stub** | Generated Java class used to call a gRPC server |
| **ImplBase** | Generated abstract class you extend to implement a gRPC server |
| **StreamObserver** | gRPC response callback — call `onNext()` then `onCompleted()` |
| **OAuth2** | Authorization framework for delegated access |
| **ROPC** | Resource Owner Password Credentials — user gives username/password directly to client |
| **JWT** | JSON Web Token — signed, self-contained token carrying user claims |
| **BCrypt** | Password hashing algorithm — deliberately slow to resist brute force |
| **Flyway** | Database migration tool — tracks and runs SQL scripts in order |
| **Interceptor** | Runs before/after every RPC call — used for auth, logging, tracing |
| **Correlation ID** | Unique ID attached to a request to trace it across multiple services |
| **Redis** | In-memory data store — used here for token caching and blacklist |
| **Vault** | HashiCorp secret manager — stores sensitive config outside of code |
| **Blocking Stub** | gRPC client that waits for the response (synchronous) |
| **Async Stub** | gRPC client that uses callbacks (asynchronous) |
| **HTTP/2** | Transport protocol used by gRPC — supports multiplexing and streaming |
| **TLS** | Transport Layer Security — encrypts gRPC communication |
| **`@GrpcService`** | Annotation that registers a class as a gRPC service implementation |
| **`@GrpcClient`** | Annotation that injects a gRPC client stub |
| **`@GrpcGlobalServerInterceptor`** | Annotation that applies an interceptor to all services |
| **Field number** | The `= 1`, `= 2` in proto — identifies fields in binary encoding |
| **GraphQL Resolver** | Method that handles a GraphQL query or mutation |
| **`@QueryMapping`** | Spring annotation mapping a method to a GraphQL query |
| **`@MutationMapping`** | Spring annotation mapping a method to a GraphQL mutation |
