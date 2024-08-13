# Build the Spring Boot application
FROM eclipse-temurin:17.0.12_7-jdk-focal AS gollm_taskrunner_builder

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
COPY ./packages/gollm/gollm-version.txt /gollmVersion.txt
RUN COMMIT_SHA="$(cat /gollmVersion.txt)" && \
	echo "Using GoLLM commit $COMMIT_SHA" && \
	wget --progress=dot:giga -O gollm.tar.gz "https://github.com/DARPA-ASKEM/GoLLM/archive/${COMMIT_SHA}.tar.gz" && \
	tar -zxvf gollm.tar.gz && \
	rm gollm.tar.gz && \
	mv GoLLM-* GoLLM
WORKDIR /GoLLM
RUN pip install --no-cache-dir .

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

# Install OpenJDK JRE and wget
RUN apt-get update && \
	apt-get install -y --no-install-recommends \
	openjdk-17-jre-headless && \
	rm -rf /var/lib/apt/lists/*

# Copy the Spring Boot fat JAR from the builder image
COPY --from=gollm_taskrunner_builder /taskrunner/build/libs/*.jar /taskrunner.jar

# Install GoLLM
COPY --from=gollm_taskrunner_builder /GoLLM /GoLLM
WORKDIR /GoLLM
RUN pip install --no-cache-dir .

# Install taskrunner
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
