package com.springgrpc.server.grpc.interceptor;

import com.springgrpc.server.config.SecurityProperties;
import com.springgrpc.server.security.JwtTokenProvider;
import com.springgrpc.server.util.Constants;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * gRPC interceptor enforcing bearer-token authentication on incoming calls.
 *
 * <p>Behaviour is fully driven by {@link SecurityProperties}:
 * <ul>
 *   <li>{@code security.authorization.enabled=false} - every call passes through</li>
 *   <li>{@code security.authorization.enabled=true}  - a valid JWT bearer token
 *       is required, except for token-issuing endpoints listed in {@link Constants}.</li>
 * </ul>
 */
@Slf4j
@RequiredArgsConstructor
public class AuthInterceptor implements ServerInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final SecurityProperties securityProperties;

    @Override
    public <T, R> ServerCall.Listener<T> interceptCall(ServerCall<T, R> call,
                                                       Metadata headers,
                                                       ServerCallHandler<T, R> next) {
        if (!securityProperties.isEnabled()) {
            return next.startCall(call, headers);
        }

        String fullMethod = call.getMethodDescriptor().getFullMethodName();
        if (isPublicMethod(fullMethod)) {
            return next.startCall(call, headers);
        }

        Metadata.Key<String> authKey = Metadata.Key.of(
                securityProperties.getHeaderName(), Metadata.ASCII_STRING_MARSHALLER);
        String auth = headers.get(authKey);

        if (auth == null || auth.isBlank()) {
            return abort(call, Status.UNAUTHENTICATED.withDescription(Constants.MSG_MISSING_AUTH_HEADER));
        }

        String prefix = securityProperties.getBearerPrefix();
        if (!auth.regionMatches(true, 0, prefix, 0, prefix.length())) {
            return abort(call, Status.UNAUTHENTICATED.withDescription(Constants.MSG_INVALID_AUTH_SCHEME));
        }

        String token = auth.substring(prefix.length());
        if (!jwtTokenProvider.isTokenValid(token)) {
            return abort(call, Status.UNAUTHENTICATED.withDescription(Constants.MSG_INVALID_TOKEN));
        }

        return next.startCall(call, headers);
    }

    private static boolean isPublicMethod(String fullMethodName) {
        return fullMethodName.startsWith(Constants.GRPC_SERVICE_AUTH + "/")
                || fullMethodName.startsWith(Constants.GRPC_SERVICE_TOKEN + "/");
    }

    private static <T> ServerCall.Listener<T> abort(ServerCall<T, ?> call, Status status) {
        log.debug("gRPC call aborted: {}", status.getDescription());
        call.close(status, new Metadata());
        return new ServerCall.Listener<T>() {
        };
    }
}