# Build the Spring Boot application
FROM eclipse-temurin:17.0.11_9-jdk-focal AS gollm_taskrunner_builder

###### DEV ONLY ######
#VVVVVVVVVVVVVVVVVVVVV

# These deps are installed only for use during local development

# Install Python
RUN apt-get update && apt-get install -y --no-install-recommends \
	software-properties-common
RUN add-apt-repository ppa:deadsnakes/ppa
RUN apt-get update && apt-get install -y --no-install-recommends \
	python3.10 \
	wget \
	python3-pip && \
	rm -rf /var/lib/apt/lists/*

# Install gollm
RUN echo "Cache bust so we can always pull latest GoLLM" $(date -u)
RUN wget -O gollm.tar.gz https://github.com/DARPA-ASKEM/GoLLM/archive/refs/heads/main.tar.gz && \
	tar -zxvf gollm.tar.gz && \
	mv GoLLM-* GoLLM && \
	cd /GoLLM && \
	pip install -e .

#^^^^^^^^^^^^^^^^^^^^
######################

WORKDIR /taskrunner

COPY ./packages/taskrunner .

RUN ./gradlew bootJar

WORKDIR /

# ------------------------------------------------------------------------------

# Set up the Python image with JRE
FROM python:3.10-slim

WORKDIR /

# Install OpenJDK JRE
RUN apt-get update && \
	apt-get install -y --no-install-recommends openjdk-17-jre-headless && \
	rm -rf /var/lib/apt/lists/*

# Copy the Spring Boot fat JAR from the builder image
COPY --from=gollm_taskrunner_builder /taskrunner/build/libs/*.jar /taskrunner.jar

# Copy the echo script for testing
COPY ./packages/taskrunner/src/test/resources/echo.py /echo.py

# Install wget
RUN apt-get update && \
	apt-get install -y --no-install-recommends wget && \
	rm -rf /var/lib/apt/lists/*

RUN echo "Cache bust so we can always pull latest GoLLM" $(date -u)
RUN wget -O gollm.tar.gz https://github.com/DARPA-ASKEM/GoLLM/archive/refs/heads/main.tar.gz && \
	tar -zxvf gollm.tar.gz && \
	mv GoLLM-* GoLLM && \
	cd /GoLLM && \
	pip install -e .

# Install taskrunner
COPY ./packages/taskrunner/setup.py /taskrunner/setup.py
COPY ./packages/taskrunner/taskrunner.py /taskrunner/taskrunner.py
WORKDIR /taskrunner
RUN pip install -e .

# Install GoLLM tasks
COPY ./packages/gollm /gollm_task
WORKDIR /gollm_task
RUN pip install -e .

WORKDIR /

CMD ["java", "-jar", "taskrunner.jar"]
