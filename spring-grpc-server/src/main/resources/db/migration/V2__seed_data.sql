-- Default admin user (password: admin123) 
INSERT INTO users (id, username, email, password_hash, status, created_at, updated_at, version) 
VALUES ( 
  'user-admin-001', 
  'admin', 
  'admin@example.com', 
  '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQyCc3DsX4TBHiWaVgVB9dqq2', 
  'ACTIVE', 
  NOW(), NOW(), 0 
) ON CONFLICT DO NOTHING; 
 
INSERT INTO user_roles (users_id, role) VALUES ('user-admin-001', 'ROLE_ADMIN') ON CONFLICT DO NOTHING; 
INSERT INTO user_roles (users_id, role) VALUES ('user-admin-001', 'ROLE_USER') ON CONFLICT DO NOTHING; 
 
-- Default OAuth client (secret: client-secret-dev) 
INSERT INTO oauth_clients (client_id, client_secret, client_name, active, created_at) 
VALUES ( 
  'dev-client', 
  '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LjTFkDy.Si', 
  'Development Client', 
  TRUE, 
  NOW() 
) ON CONFLICT DO NOTHING; 
 
INSERT INTO client_scopes (client_entity_client_id, scope) VALUES ('dev-client', 'read') ON CONFLICT DO NOTHING; 
INSERT INTO client_scopes (client_entity_client_id, scope) VALUES ('dev-client', 'write') ON CONFLICT DO NOTHING; 
INSERT INTO client_grant_types (client_entity_client_id, grant_type) VALUES ('dev-client', 'password') ON CONFLICT DO NOTHING; 
INSERT INTO client_grant_types (client_entity_client_id, grant_type) VALUES ('dev-client', 'refresh_token') ON CONFLICT DO NOTHING; 
