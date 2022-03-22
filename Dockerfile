FROM maven:3-openjdk-17-slim as build

WORKDIR /app

COPY pom.xml ./
COPY src ./src

RUN ["mvn", "clean", "install"]

FROM openjdk:16
WORKDIR /app
COPY --from=build /app/target/jwt-rest-service-1.0-SNAPSHOT.jar /app/jwt-rest-service.jar
EXPOSE 8080

ENTRYPOINT [ "java", "-jar", "/app/jwt-rest-service.jar"]

LABEL org.opencontainers.image.source https://github.com/sonamsamdupkhangsar/jwt-rest-service