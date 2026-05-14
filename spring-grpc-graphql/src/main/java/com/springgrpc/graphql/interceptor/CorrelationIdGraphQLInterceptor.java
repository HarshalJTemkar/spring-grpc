package com.springgrpc.graphql.interceptor; 
import graphql.GraphQLContext; 
import graphql.execution.instrumentation.InstrumentationContext; 
import graphql.execution.instrumentation.SimplePerformantInstrumentation; 
import graphql.execution.instrumentation.parameters.InstrumentationExecutionParameters; 
import org.springframework.stereotype.Component; 
import java.util.UUID; 
@Component 
public class CorrelationIdGraphQLInterceptor extends SimplePerformantInstrumentation { 
    public static final String CORRELATION_ID = "x-correlation-id"; 
    @Override 
    public InstrumentationContext<graphql.ExecutionResult> beginExecution(InstrumentationExecutionParameters params, graphql.execution.instrumentation.state.InstrumentationState state) { 
        GraphQLContext context = params.getExecutionInput().getGraphQLContext(); 
        context.putIfAbsent(CORRELATION_ID, UUID.randomUUID().toString()); 
        return super.beginExecution(params, state); 
    } 
} 
