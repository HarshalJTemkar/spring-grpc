package com.springgrpc.server.util;

/**
 * Centralized constants for property keys, header names and default values.
 * No values should be hard-coded across the codebase; reference these constants instead.
 */
public final class Constants {

    private Constants() {
    }

    // ---------- Property keys (used in @Value / @ConfigurationProperties) ----------
    public static final String PROP_AUTHZ_ENABLED = "security.authorization.enabled";
    public static final String PROP_AUTHZ_HEADER_NAME = "security.authorization.header-name";
    public static final String PROP_AUTHZ_BEARER_PREFIX = "security.authorization.bearer-prefix";
    public static final String PROP_AUTHZ_ACTUATOR_PATH_PATTERN = "security.authorization.actuator-path-pattern";

    // ---------- Default values ----------
    public static final boolean DEFAULT_AUTHZ_ENABLED = false;
    public static final String DEFAULT_AUTHZ_HEADER_NAME = "authorization";
    public static final String DEFAULT_AUTHZ_BEARER_PREFIX = "Bearer ";
    public static final String DEFAULT_ACTUATOR_PATH_PATTERN = "/actuator/**";

    // ---------- Spring @Value placeholder expressions with defaults ----------
    public static final String EXPR_AUTHZ_ENABLED =
            "${" + PROP_AUTHZ_ENABLED + ":" + DEFAULT_AUTHZ_ENABLED + "}";
    public static final String EXPR_AUTHZ_HEADER_NAME =
            "${" + PROP_AUTHZ_HEADER_NAME + ":" + DEFAULT_AUTHZ_HEADER_NAME + "}";
    public static final String EXPR_AUTHZ_BEARER_PREFIX =
            "${" + PROP_AUTHZ_BEARER_PREFIX + ":" + DEFAULT_AUTHZ_BEARER_PREFIX + "}";
    public static final String EXPR_ACTUATOR_PATH_PATTERN =
            "${" + PROP_AUTHZ_ACTUATOR_PATH_PATTERN + ":" + DEFAULT_ACTUATOR_PATH_PATTERN + "}";

    // ---------- Error / log messages ----------
    public static final String MSG_MISSING_AUTH_HEADER = "Missing authorization header";
    public static final String MSG_INVALID_AUTH_SCHEME = "Invalid authorization scheme; expected Bearer";
    public static final String MSG_INVALID_TOKEN = "Invalid or expired token";

    // ---------- gRPC method names exempt from authorization (open endpoints) ----------
    // Token-issuing and introspection endpoints typically should not require a
    // bearer token themselves (clients have not yet obtained one).
    public static final String GRPC_SERVICE_AUTH = "oauth.AuthService";
    public static final String GRPC_SERVICE_TOKEN = "oauth.TokenService";
}
