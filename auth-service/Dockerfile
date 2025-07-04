FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY . .

RUN chmod +x ./gradlew

COPY settings.gradle.kts build.gradle.kts ./
COPY core/build.gradle.kts core/
COPY auth-service/build.gradle.kts auth-service/

RUN ./gradlew :auth-service:dependencies --no-daemon

COPY core/src core/src
COPY auth-service/src auth-service/src

RUN ./gradlew :auth-service:clean :auth-service:bootJar --no-daemon

FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY --from=builder /app/auth-service/build/libs/*.jar auth-service.jar

ENTRYPOINT ["java", "-jar", "auth-service.jar"]