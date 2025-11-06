FROM gcr.io/distroless/java21-debian12@sha256:e49540ee46535ae570ed4553cc5b77eb07e539580f76b371aab46e18086fad67
WORKDIR /app
COPY build/libs/app-*.jar app.jar
ENV JAVA_OPTS="-Dlogback.configurationFile=logback.xml"
ENV TZ="Europe/Oslo"
EXPOSE 8080
USER nonroot
CMD [ "app.jar" ]
