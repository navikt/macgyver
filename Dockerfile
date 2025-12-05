FROM gcr.io/distroless/java21-debian12@sha256:d8f16c5beb203e0890e6477706912e725d55c01a3ff3fe03e744f4adb0be3335
WORKDIR /app
COPY build/libs/app-*.jar app.jar
ENV JAVA_OPTS="-Dlogback.configurationFile=logback.xml"
ENV TZ="Europe/Oslo"
EXPOSE 8080
USER nonroot
CMD [ "app.jar" ]
