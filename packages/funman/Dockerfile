# Build the Spring Boot application
FROM eclipse-temurin:17.0.11_9-jdk-focal AS taskrunner_builder

WORKDIR /taskrunner

COPY ./packages/taskrunner .

RUN ./gradlew bootJar

# ------------------------------------------------------------------------------

# Funman-base, should contain dreal4, ibex, and python dependencies
FROM ghcr.io/darpa-askem/funman-base:latest

WORKDIR /

# Install OpenJDK JRE and git
RUN apt-get update && \
	apt-get install -y --no-install-recommends openjdk-17-jre-headless && \
  apt-get install -y --no-install-recommends git && \
	rm -rf /var/lib/apt/lists/*

# Copy the Spring Boot fat JAR from the builder image
COPY --from=taskrunner_builder /taskrunner/build/libs/*.jar /taskrunner.jar

# Copy the echo script for testing
COPY ./packages/taskrunner/src/test/resources/echo.py /echo.py

# Install funman-api
RUN echo "Cache bust so we can always pull latest Funman" $(date -u)
COPY ./packages/funman/funman-version.txt /funmanVersion.txt

RUN git clone https://github.com/DARPA-ASKEM/funman-api.git

WORKDIR /funman-api
RUN COMMIT_SHA=$(cat /funmanVersion.txt) && \
    echo "Using FUNMAN commit $COMMIT_SHA" && \
    git reset --hard $COMMIT_SHA
RUN pip install --no-cache-dir . && \
    pip install --no-cache-dir auxiliary_packages/funman_dreal && \
    pip install --no-cache-dir auxiliary_packages/funman_demo

# Install taskrunner
WORKDIR /
COPY ./packages/taskrunner/setup.py /taskrunner/setup.py
COPY ./packages/taskrunner/taskrunner.py /taskrunner/taskrunner.py

WORKDIR /taskrunner
RUN pip install --no-cache-dir -e .

# Install funman tasks
COPY ./packages/funman /funman_tasks

WORKDIR /funman_tasks
RUN pip install --no-cache-dir -e .

WORKDIR /
CMD ["java", "-jar", "taskrunner.jar"]
