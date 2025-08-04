FROM gcr.io/distroless/java21-debian12@sha256:73c719485ac6fb38dd168053837b95812d33882d7136d03e56291bb0e1c13bf8
WORKDIR /app
COPY build/libs/app-*.jar app.jar
ENV JAVA_OPTS="-Dlogback.configurationFile=logback.xml"
ENV TZ="Europe/Oslo"
EXPOSE 8080
USER nonroot
CMD [ "app.jar" ]
