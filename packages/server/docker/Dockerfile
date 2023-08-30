FROM eclipse-temurin:17-jdk-focal AS builder
WORKDIR /app
COPY /modules/server .
RUN --mount=type=cache,target=/root/.gradle ./gradlew clean bootJar

# Extract JAR into layers for more optimal builds
RUN mkdir -p ./layers
RUN cp ./build/libs/*-SNAPSHOT.jar ./layers
RUN (cd layers; java -Djarmode=layertools -jar *-SNAPSHOT.jar extract)

FROM eclipse-temurin:17-jre-focal
RUN groupadd -r pantera && useradd --no-log-init -r -g pantera pantera
VOLUME /tmp
USER pantera

# copy over the layers
ARG LAYERS=/app/layers
COPY --from=builder ${LAYERS}/dependencies/ ./
COPY --from=builder ${LAYERS}/snapshot-dependencies/ ./
COPY --from=builder ${LAYERS}/spring-boot-loader/ ./
COPY --from=builder ${LAYERS}/application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
