FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
COPY core/build.gradle.kts ./core/build.gradle.kts
COPY document-service/build.gradle.kts ./document-service/build.gradle.kts

RUN chmod +x gradlew
RUN ./gradlew :document-service:dependencies --no-daemon

COPY core/src ./core/src
COPY document-service/src ./document-service/src

RUN ./gradlew :document-service:clean :document-service:bootJar --no-daemon

FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY --from=builder /app/document-service/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]