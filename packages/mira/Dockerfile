# Build the Spring Boot application
FROM eclipse-temurin:17.0.11_9-jdk-focal AS taskrunner_builder

WORKDIR /taskrunner

COPY ./packages/taskrunner .

RUN ./gradlew bootJar

# ------------------------------------------------------------------------------

# Set up the Python image with JRE
FROM python:3.10-slim

WORKDIR /

# Install OpenJDK JRE and git
RUN apt-get update && \
    apt-get install -y --no-install-recommends openjdk-17-jre-headless && \
    apt-get install -y --no-install-recommends wget && \
    rm -rf /var/lib/apt/lists/*

# Copy the Spring Boot fat JAR from the builder image
COPY --from=taskrunner_builder /taskrunner/build/libs/*.jar /taskrunner.jar

# Copy the echo script for testing
COPY ./packages/taskrunner/src/test/resources/echo.py /echo.py

# Install Mira
RUN echo "Cache bust so we can always pull latest MIRA" $(date -u)
COPY ./packages/mira/mira-version.txt /miraVersion.txt
RUN COMMIT_SHA=$(cat /miraVersion.txt) && \
    echo "Using MIRA commit $COMMIT_SHA" && \
    wget -O mira.tar.gz https://github.com/gyorilab/mira/archive/${COMMIT_SHA}.tar.gz

RUN tar -zxvf mira.tar.gz && \
    rm mira.tar.gz
RUN	mv -v mira-* mira

WORKDIR /mira
RUN pip install -e .[sbml,biomodels]

# Install taskrunner
WORKDIR /
COPY ./packages/taskrunner/setup.py /taskrunner/setup.py
COPY ./packages/taskrunner/taskrunner.py /taskrunner/taskrunner.py

WORKDIR /taskrunner
RUN pip install -e .

# Install Mira tasks
COPY ./packages/mira /mira_task

WORKDIR /mira_task
RUN pip install -e .

WORKDIR /
CMD ["java", "-jar", "taskrunner.jar"]
