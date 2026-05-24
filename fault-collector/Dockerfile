# fault-collector servisini docker imageına paketleyen dosyam. İki aşamalı bir Dockerfile kullanarak build yapısı var.
# Build aşaması = Maven ile projeyi derleyip jar dosyasını üretiyoruz.
# runtime aşaması = sadece JRE olan hafif bir image üzerinde uygulamayı çalıştırıyor. Local geliştirmede agent'ı pom.xml'deki jvmArguments ile yükledik.
# Docker ortamında ise agent'ı image'ın içine kopyalayıp ENTRYPOINT'te -javaagent argümanıyla yüklüyoruz.
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
#Agent jar dosyasını infrastructure klasöründen alıp runtime image'ına kopyalıyoruz. Yani agent kod ile birlikte image'ın içine giriyor.
COPY infrastructure/otel-agent/opentelemetry-javaagent.jar otel-agent.jar
EXPOSE 8080
# Container başladığında JVM bu komutla başlıyor.
ENTRYPOINT ["java", "-javaagent:otel-agent.jar", "-jar", "app.jar"]
