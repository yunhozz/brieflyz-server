FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
COPY core/build.gradle.kts ./core/build.gradle.kts
COPY subscription-service/build.gradle.kts ./subscription-service/build.gradle.kts

RUN chmod +x gradlew
RUN ./gradlew :subscription-service:dependencies --no-daemon

COPY core/src ./core/src
COPY subscription-service/src ./subscription-service/src

RUN ./gradlew :subscription-service:clean :subscription-service:bootJar --no-daemon

FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY --from=builder /app/subscription-service/build/libs/*.jar ./app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]