FROM gcr.io/distroless/java21-debian12@sha256:5a7b2546ab61ed5dff8d1c7ca71357fef69486bc7619aee5738491826e223155
WORKDIR /app
COPY build/libs/app-*.jar app.jar
ENV JAVA_OPTS="-Dlogback.configurationFile=logback.xml"
ENV TZ="Europe/Oslo"
EXPOSE 8080
USER nonroot
CMD [ "app.jar" ]
