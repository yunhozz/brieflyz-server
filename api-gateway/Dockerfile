FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
COPY core/build.gradle.kts ./core/build.gradle.kts
COPY api-gateway/build.gradle.kts ./api-gateway/build.gradle.kts

RUN chmod +x gradlew
RUN ./gradlew :api-gateway:dependencies --no-daemon

COPY core/src ./core/src
COPY api-gateway/src ./api-gateway/src

RUN ./gradlew :api-gateway:clean :api-gateway:bootJar --no-daemon

FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY --from=builder /app/api-gateway/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]