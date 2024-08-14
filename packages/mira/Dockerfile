# Build the Spring Boot application
FROM eclipse-temurin:17.0.12_7-jdk-focal AS mira_taskrunner_builder

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

# Install Mira
COPY ./packages/mira/mira-version.txt /miraVersion.txt
RUN COMMIT_SHA="$(cat /miraVersion.txt)" && \
	echo "Using MIRA commit $COMMIT_SHA" && \
	wget --progress=dot:giga -O mira.tar.gz "https://github.com/gyorilab/mira/archive/${COMMIT_SHA}.tar.gz" && \
	tar -zxvf mira.tar.gz && \
	rm mira.tar.gz && \
	mv -v mira-* mira
WORKDIR /mira
RUN pip install --no-cache-dir .[sbml,biomodels]

#^^^^^^^^^^^^^^^^^^^^
######################

WORKDIR /taskrunner

COPY ./packages/taskrunner .

RUN ./gradlew bootJar

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
COPY --from=mira_taskrunner_builder /taskrunner/build/libs/*.jar /taskrunner.jar

# Install Mira
COPY --from=mira_taskrunner_builder /mira /mira
WORKDIR /mira
RUN pip install --no-cache-dir .[sbml,biomodels]

# Install taskrunner
COPY ./packages/taskrunner/setup.py /taskrunner/setup.py
COPY ./packages/taskrunner/taskrunner.py /taskrunner/taskrunner.py
WORKDIR /taskrunner
RUN pip install --no-cache-dir -e .

# Install Mira tasks
COPY ./packages/mira /mira_task

WORKDIR /mira_task
RUN pip install --no-cache-dir -e .

WORKDIR /
CMD ["java", "-jar", "taskrunner.jar"]
