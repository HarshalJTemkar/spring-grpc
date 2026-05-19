package com.springgrpc.graphql.scalar; 
import graphql.language.StringValue; 
import graphql.schema.*; 
import java.time.OffsetDateTime; 
import java.time.format.DateTimeFormatter; 
public class DateTimeScalar { 
    public static final GraphQLScalarType DATE_TIME = GraphQLScalarType.newScalar() 
        .name("DateTime") 
        .description("ISO-8601 DateTime scalar") 
        .coercing(new Coercing<OffsetDateTime, String>() { 
            @Override public String serialize(Object input) throws CoercingSerializeException { return input instanceof OffsetDateTime dt ? dt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : input.toString(); } 
            @Override public OffsetDateTime parseValue(Object input) { return OffsetDateTime.parse(input.toString()); } 
            @Override public OffsetDateTime parseLiteral(Object ast) { return ast instanceof StringValue sv ? OffsetDateTime.parse(sv.getValue()) : null; } 
        }) 
        .build(); 
} 
