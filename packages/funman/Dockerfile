# Build the Spring Boot application
FROM eclipse-temurin:17.0.10_7-jdk-focal AS taskrunner_builder

WORKDIR /taskrunner

COPY ./packages/taskrunner .

RUN ./gradlew bootJar


# ------------------------------------------------------------------------------

# Funman-base, should contain dreal4, ibex, and python dependencies
FROM ghcr.io/darpa-askem/funman-base:latest-e5fb635757aa57007615a75371f55dd4a24851e0



# Install OpenJDK JRE
RUN apt-get update && \
	apt-get install -y --no-install-recommends openjdk-17-jre-headless && \
	rm -rf /var/lib/apt/lists/*


# Copy the Spring Boot fat JAR from the builder image
COPY --from=taskrunner_builder /taskrunner/build/libs/*.jar /taskrunner.jar

# Copy the echo script for testing
COPY ./packages/taskrunner/src/test/resources/echo.py /echo.py


ADD https://api.github.com/repos/DARPA-ASKEM/funman-api/git/refs/heads/develop version.json
RUN git clone https://github.com/DARPA-ASKEM/funman-api.git

WORKDIR /funman-api

RUN pip install .
RUN pip install auxiliary_packages/funman_dreal
RUN pip install auxiliary_packages/funman_demo

# Copy the funman package
COPY ./packages/funman /funman_tasks
WORKDIR /funman_tasks

# Copy the taskrunner interface
COPY ./packages/taskrunner/taskrunner.py /taskrunner/taskrunner.py

# Install the tasks for funman
RUN pip install -e .

WORKDIR /

CMD ["java", "-jar", "taskrunner.jar"]
