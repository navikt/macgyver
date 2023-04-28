FROM ghcr.io/navikt/baseimages/temurin:17
COPY build/libs/*-all.jar app.jar
COPY /api/oas3/oas3/documentation.yaml /app/api/oas3/documentation.yaml
ENV JAVA_OPTS="-Dlogback.configurationFile=logback.xml"
