FROM eclipse-temurin:21.0.6_7-jdk-noble AS builder

WORKDIR /app
COPY /packages/server .

RUN --mount=type=cache,target=/root/.gradle ./gradlew clean bootJar

# Extract JAR into layers for more optimal builds
RUN mkdir -p ./layers \
    && cp ./build/libs/*-SNAPSHOT.jar ./layers

WORKDIR /app/layers
RUN java -Djarmode=layertools -jar ./*-SNAPSHOT.jar extract

FROM eclipse-temurin:21.0.6_7-jre-noble

WORKDIR /app

RUN groupadd -r terarium \
    && useradd --no-log-init -r -g terarium terarium
VOLUME /tmp
USER terarium

# copy over the layers
ARG LAYERS=/app/layers
COPY --from=builder ${LAYERS}/dependencies/ ./
COPY --from=builder ${LAYERS}/snapshot-dependencies/ ./
COPY --from=builder ${LAYERS}/spring-boot-loader/ ./
COPY --from=builder ${LAYERS}/application/ ./
ENTRYPOINT ["java", "-XX:InitialRAMPercentage=10", "-XX:MinRAMPercentage=50", "-XX:MaxRAMPercentage=50", "org.springframework.boot.loader.JarLauncher"]
