-- Users 
CREATE TABLE IF NOT EXISTS users ( 
    id VARCHAR(36) PRIMARY KEY, 
    username VARCHAR(100) NOT NULL UNIQUE, 
    email VARCHAR(255) NOT NULL UNIQUE, 
    password_hash VARCHAR(255) NOT NULL, 
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', 
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), 
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), 
    version BIGINT NOT NULL DEFAULT 0 
); 
 
CREATE TABLE IF NOT EXISTS user_roles ( 
    users_id VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE, 
    role VARCHAR(50) NOT NULL, 
    PRIMARY KEY (users_id, role) 
); 
 
-- OAuth Clients 
CREATE TABLE IF NOT EXISTS oauth_clients ( 
    client_id VARCHAR(100) PRIMARY KEY, 
    client_secret VARCHAR(255) NOT NULL, 
    client_name VARCHAR(255), 
    active BOOLEAN NOT NULL DEFAULT TRUE, 
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW() 
); 
 
CREATE TABLE IF NOT EXISTS client_scopes ( 
    client_entity_client_id VARCHAR(100) NOT NULL REFERENCES oauth_clients(client_id) ON DELETE CASCADE, 
    scope VARCHAR(100) NOT NULL, 
    PRIMARY KEY (client_entity_client_id, scope) 
); 
 
CREATE TABLE IF NOT EXISTS client_grant_types ( 
    client_entity_client_id VARCHAR(100) NOT NULL REFERENCES oauth_clients(client_id) ON DELETE CASCADE, 
    grant_type VARCHAR(50) NOT NULL, 
    PRIMARY KEY (client_entity_client_id, grant_type) 
); 
 
-- Access Tokens 
CREATE TABLE IF NOT EXISTS tokens ( 
    token_id VARCHAR(36) PRIMARY KEY, 
    token_value TEXT NOT NULL, 
    user_id VARCHAR(36) NOT NULL REFERENCES users(id), 
    client_id VARCHAR(100) NOT NULL REFERENCES oauth_clients(client_id), 
    scope VARCHAR(500), 
    token_type VARCHAR(20) NOT NULL, 
    issued_at TIMESTAMPTZ NOT NULL, 
    expires_at TIMESTAMPTZ NOT NULL, 
    revoked BOOLEAN NOT NULL DEFAULT FALSE 
); 
 
-- Refresh Tokens 
CREATE TABLE IF NOT EXISTS refresh_tokens ( 
    token_id VARCHAR(36) PRIMARY KEY, 
    token_value VARCHAR(500) NOT NULL UNIQUE, 
    user_id VARCHAR(36) NOT NULL REFERENCES users(id), 
    client_id VARCHAR(100) NOT NULL REFERENCES oauth_clients(client_id), 
    access_token_id VARCHAR(36), 
    issued_at TIMESTAMPTZ NOT NULL, 
    expires_at TIMESTAMPTZ NOT NULL, 
    used BOOLEAN NOT NULL DEFAULT FALSE, 
    revoked BOOLEAN NOT NULL DEFAULT FALSE 
); 
 
-- Indexes 
CREATE INDEX idx_tokens_user_id ON tokens(user_id); 
CREATE INDEX idx_tokens_expires_at ON tokens(expires_at); 
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id); 
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at); 
