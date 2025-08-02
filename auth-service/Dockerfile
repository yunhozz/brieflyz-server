FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
COPY core/build.gradle.kts ./core/build.gradle.kts
COPY auth-service/build.gradle.kts ./auth-service/build.gradle.kts

RUN chmod +x gradlew
RUN ./gradlew :auth-service:dependencies --no-daemon

COPY core/src ./core/src
COPY auth-service/src ./auth-service/src

RUN ./gradlew :auth-service:clean :auth-service:bootJar --no-daemon

FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY --from=builder /app/auth-service/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]