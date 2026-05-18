package com.springgrpc.server.config;

import com.springgrpc.server.util.Constants;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Externalized authorization configuration. All flags are driven via
 * {@code application.yml} / {@code application.properties} - no hard-coding.
 *
 * <pre>
 * security:
 *   authorization:
 *     enabled: true|false      # turn auth on/off globally
 *     header-name: authorization
 *     bearer-prefix: "Bearer "
 *     actuator-path-pattern: /actuator/**
 * </pre>
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "security.authorization")
public class SecurityProperties {

    /** When true, authorization is required; when false, all requests are permitted. */
    private boolean enabled = Constants.DEFAULT_AUTHZ_ENABLED;

    /** Name of the header (HTTP) / metadata key (gRPC) carrying the credential. */
    private String headerName = Constants.DEFAULT_AUTHZ_HEADER_NAME;

    /** Authorization scheme prefix, including trailing space (e.g. {@code "Bearer "}). */
    private String bearerPrefix = Constants.DEFAULT_AUTHZ_BEARER_PREFIX;

    /** Path pattern that is always permitted (actuator endpoints). */
    private String actuatorPathPattern = Constants.DEFAULT_ACTUATOR_PATH_PATTERN;
}
