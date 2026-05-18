package com.springgrpc.server.grpc.interceptor;
import com.springgrpc.server.security.JwtTokenProvider;
import io.grpc.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Slf4j @RequiredArgsConstructor
public class AuthInterceptor implements ServerInterceptor {
    public static final Metadata.Key<String> AUTH_KEY = Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);
    private static final String BEARER_PREFIX = "Bearer ";
    private final JwtTokenProvider jwtTokenProvider;
    @Override
    public <T, R> ServerCall.Listener<T> interceptCall(ServerCall<T, R> call, Metadata headers, ServerCallHandler<T, R> next) {
        String auth = headers.get(AUTH_KEY);
        if (auth != null && auth.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            String token = auth.substring(BEARER_PREFIX.length());
            boolean valid = jwtTokenProvider.isTokenValid(token);
            if (!valid) {
                call.close(Status.UNAUTHENTICATED.withDescription("Invalid token"), new Metadata());
                return new ServerCall.Listener<T>(){};
            }
        }
        return next.startCall(call, headers);
    }
}
