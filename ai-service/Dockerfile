FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
COPY core/build.gradle.kts ./core/build.gradle.kts
COPY ai-service/build.gradle.kts ./ai-service/build.gradle.kts

RUN chmod +x gradlew
RUN ./gradlew :ai-service:dependencies --no-daemon

COPY core/src ./core/src
COPY ai-service/src ./ai-service/src

RUN ./gradlew :ai-service:clean :ai-service:bootJar --no-daemon

FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY --from=builder /app/ai-service/build/libs/*.jar ./app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]