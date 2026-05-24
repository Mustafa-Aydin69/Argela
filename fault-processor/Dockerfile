# Bu Dockerfile, fault-processor uygulamasını derleyip çalıştırmak için kullanılır. 
#İki aşamalı bir yapı kullanır: ilk aşamada Maven ile uygulama derlenir, ikinci aşamada ise sadece çalıştırmak için gerekli dosyalar alınarak daha küçük bir imaj oluşturulur.
# kısaca fault-collector Dockerfile dosyasıyla benzer ancak bu sefer sadece fault-processor için gerekli dosyalar kopyalanır ve çalıştırılır.
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
RUN mvn package -pl fault-processor -DskipTests -q

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/fault-processor/target/*.jar app.jar
COPY infrastructure/otel-agent/opentelemetry-javaagent.jar otel-agent.jar
EXPOSE 8081
ENTRYPOINT ["java", "-javaagent:otel-agent.jar", "-jar", "app.jar"]
