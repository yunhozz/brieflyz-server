FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY . .

RUN chmod +x ./gradlew

COPY settings.gradle.kts build.gradle.kts ./
COPY core/build.gradle.kts core/
COPY api-gateway/build.gradle.kts api-gateway/

RUN ./gradlew :api-gateway:dependencies --no-daemon

COPY core/src core/src
COPY api-gateway/src api-gateway/src

RUN ./gradlew :api-gateway:clean :api-gateway:bootJar --no-daemon

FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY --from=builder /app/api-gateway/build/libs/*.jar api-gateway.jar

ENTRYPOINT ["java", "-jar", "api-gateway.jar"]