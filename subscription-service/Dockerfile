FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY . .

RUN chmod +x ./gradlew

COPY settings.gradle.kts build.gradle.kts ./
COPY core/build.gradle.kts core/
COPY subscription-service/build.gradle.kts subscription-service/

RUN ./gradlew :subscription-service:dependencies --no-daemon

COPY core/src core/src
COPY subscription-service/src subscription-service/src

RUN ./gradlew :subscription-service:clean :subscription-service:bootJar --no-daemon

FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY --from=builder /app/subscription-service/build/libs/*.jar subscription-service.jar

ENTRYPOINT ["java", "-jar", "subscription-service.jar"]