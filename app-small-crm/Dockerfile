FROM gradle:8.8-jdk21-jammy AS builder

WORKDIR /app

COPY build.gradle settings.gradle ./
COPY gradle ./gradle

RUN gradle clean build --no-daemon || true

COPY . .

RUN gradle bootJar --no-daemon

FROM openjdk:21-slim-bullseye

WORKDIR /app

COPY --from=builder /app/build/libs/app-small-crm-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]