# syntax = docker/dockerfile:1.2

FROM maven:3-openjdk-17-slim as build

#ARG IMAGEREGISTRY

WORKDIR /app

COPY pom.xml settings.xml ./
COPY src ./src
RUN echo ${IMAGEREGISTRY}
RUN echo "imageRegistry is : ${IMAGEREGISTRY}"
RUN --mount=type=secret,id=IMAGEREGISTRY \
    export IMAGEREGISTRY=$(cat /run/secrets/IMAGEREGISTRY) && \
    echo "(1) imageRegistry: ${IMAGEREGISTRY}, $IMAGEREGISTRY"
RUN echo "(2) imageRegistry: ${IMAGEREGISTRY}"
RUN ["mvn",  "-s",  "settings.xml", "clean", "install"]

FROM openjdk:17
WORKDIR /app
COPY --from=build /app/target/jwt-rest-service-1.0-SNAPSHOT.jar /app/jwt-rest-service.jar
EXPOSE 8080

ENTRYPOINT [ "java", "-jar", "/app/jwt-rest-service.jar"]

LABEL org.opencontainers.image.source https://github.com/sonamsamdupkhangsar/jwt-rest-service