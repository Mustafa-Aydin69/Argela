FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
# Cache Maven dependencies as a separate layer
COPY pom.xml .
COPY fault-collector/pom.xml fault-collector/
COPY fault-processor/pom.xml fault-processor/
RUN mvn dependency:resolve -q
# Build
COPY fault-collector/src fault-collector/src
COPY fault-processor/src fault-processor/src
RUN mvn package -pl fault-collector -DskipTests -q

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/fault-collector/target/*.jar app.jar
COPY infrastructure/otel-agent/opentelemetry-javaagent.jar otel-agent.jar
EXPOSE 8080
ENTRYPOINT ["java", "-javaagent:otel-agent.jar", "-jar", "app.jar"]
