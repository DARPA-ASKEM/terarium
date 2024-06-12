# Build the Spring Boot application
FROM eclipse-temurin:17.0.11_9-jdk-focal AS taskrunner_builder

WORKDIR /taskrunner

COPY ./packages/taskrunner .

RUN ./gradlew bootJar

# ------------------------------------------------------------------------------

# Set up the Python image with JRE
FROM python:3.10-slim

WORKDIR /

# Install OpenJDK JRE and wget
RUN apt-get update && \
    apt-get install -y --no-install-recommends openjdk-17-jre-headless && \
    apt-get install -y --no-install-recommends wget && \
    rm -rf /var/lib/apt/lists/*

# Copy the Spring Boot fat JAR from the builder image
COPY --from=taskrunner_builder /taskrunner/build/libs/*.jar /taskrunner.jar

# Copy the echo script for testing
COPY ./packages/taskrunner/src/test/resources/echo.py /echo.py

# Install GoLLM
RUN echo "Cache bust so we can always pull latest GoLLM" $(date -u)
COPY ./packages/gollm/gollm-version.txt /gollmVersion.txt
RUN COMMIT_SHA=$(cat /gollmVersion.txt) && \
    echo "Using GoLLM commit $COMMIT_SHA" && \
    wget -O gollm.tar.gz https://github.com/DARPA-ASKEM/GoLLM/archive/${COMMIT_SHA}.tar.gz

RUN tar -zxvf gollm.tar.gz && \
    rm gollm.tar.gz
RUN mv GoLLM-* GoLLM

WORKDIR /GoLLM
RUN pip install --no-cache-dir -e .

# Install taskrunner
WORKDIR /
COPY ./packages/taskrunner/setup.py /taskrunner/setup.py
COPY ./packages/taskrunner/taskrunner.py /taskrunner/taskrunner.py

WORKDIR /taskrunner
RUN pip install --no-cache-dir -e .

# Install GoLLM tasks
COPY ./packages/gollm /gollm_task

WORKDIR /gollm_task
RUN pip install --no-cache-dir -e .

WORKDIR /
CMD ["java", "-jar", "taskrunner.jar"]
