FROM gcr.io/distroless/java25-debian13@sha256:19c68cb513e9210500b4e9312582d4d0928b2ca8949e25c9ae0ca28065d732b8
WORKDIR /app
COPY build/libs/app-*.jar app.jar
ENV JAVA_OPTS="-Dlogback.configurationFile=logback.xml"
ENV TZ="Europe/Oslo"
EXPOSE 8080
USER nonroot
CMD [ "app.jar" ]
