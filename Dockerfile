FROM gcr.io/distroless/java21-debian12@sha256:c04d060a6b212457673a4461fa026b82681c658cbed95c6b6c8a342bb175d323
WORKDIR /app
COPY build/libs/app-*.jar app.jar
ENV JAVA_OPTS="-Dlogback.configurationFile=logback.xml"
ENV TZ="Europe/Oslo"
EXPOSE 8080
USER nonroot
CMD [ "app.jar" ]
