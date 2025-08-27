FROM gcr.io/distroless/java21-debian12@sha256:608e5ec41871d236b304c0728841d3a988aff3dbc19d60c5ae0240cdb3654c86
WORKDIR /app
COPY build/libs/app-*.jar app.jar
ENV JAVA_OPTS="-Dlogback.configurationFile=logback.xml"
ENV TZ="Europe/Oslo"
EXPOSE 8080
USER nonroot
CMD [ "app.jar" ]
