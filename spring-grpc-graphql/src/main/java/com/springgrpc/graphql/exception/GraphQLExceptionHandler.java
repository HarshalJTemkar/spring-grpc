package com.springgrpc.graphql.exception; 
import graphql.GraphQLError; 
import graphql.GraphqlErrorBuilder; 
import graphql.schema.DataFetchingEnvironment; 
import io.grpc.StatusRuntimeException; 
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter; 
import org.springframework.stereotype.Component; 
@Component 
public class GraphQLExceptionHandler extends DataFetcherExceptionResolverAdapter { 
    @Override 
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) { 
        if (ex instanceof StatusRuntimeException grpcEx) { 
            return GraphqlErrorBuilder.newError(env).message(grpcEx.getStatus().getDescription()).errorType(graphql.ErrorType.DataFetchingException).build(); 
        } 
        return GraphqlErrorBuilder.newError(env).message("An unexpected error occurred").errorType(graphql.ErrorType.DataFetchingException).build(); 
    } 
} 
