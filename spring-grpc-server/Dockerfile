FROM eclipse-temurin:21-jre-alpine 
WORKDIR /app 
COPY target/spring-grpc-server.jar app.jar 
EXPOSE 9090 8081 
ENTRYPOINT ["java", "-jar", "/app/app.jar"] 
