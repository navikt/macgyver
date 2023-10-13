FROM eclipse-temurin:17 as jre-build
# Create a custom Java runtime
RUN $JAVA_HOME/bin/jlink \
         --add-modules ALL-MODULE-PATH \
         --strip-debug \
         --no-man-pages \
         --no-header-files \
         --compress=2 \
         --output /javaruntime \

# Runtime
FROM gcr.io/distroless/cc-debian11
WORKDIR /app
ENV TZ="Europe/Oslo"
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH "${JAVA_HOME}/bin:${PATH}"
ENV JAVA_OPTS="-Dlogback.configurationFile=logback.xml"
COPY --from=jre-build /javaruntime $JAVA_HOME
COPY build/libs/app-*.jar app.jar

EXPOSE 8080
USER nonroot
CMD ["java", "-jar", "app.jar"]
