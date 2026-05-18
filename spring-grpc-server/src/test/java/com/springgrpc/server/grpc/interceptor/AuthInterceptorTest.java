package com.springgrpc.server.grpc.interceptor;

import com.springgrpc.server.config.SecurityProperties;
import com.springgrpc.server.security.JwtTokenProvider;
import com.springgrpc.server.util.Constants;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthInterceptorTest {

    private JwtTokenProvider jwtTokenProvider;
    private SecurityProperties props;
    private ServerCall<Object, Object> call;
    private ServerCallHandler<Object, Object> next;
    private MethodDescriptor<Object, Object> methodDescriptor;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        jwtTokenProvider = mock(JwtTokenProvider.class);
        props = new SecurityProperties();
        call = mock(ServerCall.class);
        next = mock(ServerCallHandler.class);
        methodDescriptor = mock(MethodDescriptor.class);
        when(call.getMethodDescriptor()).thenReturn(methodDescriptor);
    }

    @Test
    void disabled_allowsAnyCallThrough() {
        props.setEnabled(false);
        when(methodDescriptor.getFullMethodName()).thenReturn("oauth.UserService/GetUser");

        new AuthInterceptor(jwtTokenProvider, props).interceptCall(call, new Metadata(), next);

        verify(next).startCall(any(), any());
        verify(call, never()).close(any(), any());
    }

    @Test
    void enabled_publicTokenEndpoint_passesWithoutBearer() {
        props.setEnabled(true);
        when(methodDescriptor.getFullMethodName()).thenReturn(Constants.GRPC_SERVICE_AUTH + "/Login");

        new AuthInterceptor(jwtTokenProvider, props).interceptCall(call, new Metadata(), next);

        verify(next).startCall(any(), any());
        verify(call, never()).close(any(), any());
    }

    @Test
    void enabled_missingHeader_aborts() {
        props.setEnabled(true);
        when(methodDescriptor.getFullMethodName()).thenReturn("oauth.UserService/GetUser");

        new AuthInterceptor(jwtTokenProvider, props).interceptCall(call, new Metadata(), next);

        ArgumentCaptor<Status> captor = ArgumentCaptor.forClass(Status.class);
        verify(call).close(captor.capture(), any());
        assertThat(captor.getValue().getCode()).isEqualTo(Status.Code.UNAUTHENTICATED);
        verify(next, never()).startCall(any(), any());
    }

    @Test
    void enabled_validBearer_passes() {
        props.setEnabled(true);
        when(methodDescriptor.getFullMethodName()).thenReturn("oauth.UserService/GetUser");
        when(jwtTokenProvider.isTokenValid("good-jwt")).thenReturn(true);

        Metadata md = new Metadata();
        md.put(Metadata.Key.of(props.getHeaderName(), Metadata.ASCII_STRING_MARSHALLER),
                props.getBearerPrefix() + "good-jwt");

        new AuthInterceptor(jwtTokenProvider, props).interceptCall(call, md, next);

        verify(next).startCall(any(), any());
        verify(call, never()).close(any(), any());
    }

    @Test
    void enabled_invalidBearer_aborts() {
        props.setEnabled(true);
        when(methodDescriptor.getFullMethodName()).thenReturn("oauth.UserService/GetUser");
        when(jwtTokenProvider.isTokenValid("bad-jwt")).thenReturn(false);

        Metadata md = new Metadata();
        md.put(Metadata.Key.of(props.getHeaderName(), Metadata.ASCII_STRING_MARSHALLER),
                props.getBearerPrefix() + "bad-jwt");

        new AuthInterceptor(jwtTokenProvider, props).interceptCall(call, md, next);

        ArgumentCaptor<Status> captor = ArgumentCaptor.forClass(Status.class);
        verify(call).close(captor.capture(), any());
        assertThat(captor.getValue().getCode()).isEqualTo(Status.Code.UNAUTHENTICATED);
    }
}
