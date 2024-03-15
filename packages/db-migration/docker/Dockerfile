FROM eclipse-temurin:17.0.10_7-jdk-focal AS builder

WORKDIR /app
COPY /packages/db-migration .

RUN --mount=type=cache,target=/root/.gradle ./gradlew clean bootJar

# Extract JAR into layers for more optimal builds
RUN mkdir -p ./layers \
    && cp ./build/libs/*-SNAPSHOT.jar ./layers

WORKDIR /app/layers
RUN java -Djarmode=layertools -jar ./*-SNAPSHOT.jar extract

FROM eclipse-temurin:17.0.10_7-jre-focal

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
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
