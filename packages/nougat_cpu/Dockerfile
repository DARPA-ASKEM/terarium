# Build the Spring Boot application
FROM eclipse-temurin:17.0.12_7-jdk-focal AS nougat_cpu_taskrunner_builder

###### DEV ONLY ######
#VVVVVVVVVVVVVVVVVVVVV

# These deps are installed only for use during local development

# Install Python
RUN apt-get update && apt-get install -y --no-install-recommends \
	software-properties-common
RUN add-apt-repository ppa:deadsnakes/ppa
RUN apt-get update && apt-get install -y --no-install-recommends \
	build-essential \
	python3.10 \
	python3.10-venv \
	python3.10-dev \
	python3-setuptools \
	wget \
	git \
	libgl1 \
	poppler-utils \
	python3-pip && \
	rm -rf /var/lib/apt/lists/*

# Set the default Python version to 3.10
RUN update-alternatives --install /usr/bin/python python /usr/bin/python3.10 1 && \
	update-alternatives --set python /usr/bin/python3.10

RUN python -m ensurepip --upgrade

# Verify the installation
RUN python --version
RUN pip3 --version

# Install nougat
COPY ./packages/nougat_cpu/nougat-version.txt /nougatVersion.txt
RUN COMMIT_SHA="$(cat /nougatVersion.txt)" && \
	echo "Using document_intelligence commit $COMMIT_SHA" && \
	wget --progress=dot:giga -O nougat.tar.gz "https://github.com/DARPA-ASKEM/document_intelligence/archive/${COMMIT_SHA}.tar.gz" && \
	tar -zxvf nougat.tar.gz && \
	rm nougat.tar.gz && \
	mv document_intelligence-* document_intelligence
WORKDIR /document_intelligence/document_intelligence/fast_latex
RUN pip3 install -r requirements.txt

#Install uvicorn and supervisord
RUN pip3 install uvicorn setuptools augraphy supervisor

#^^^^^^^^^^^^^^^^^^^^
######################

WORKDIR /taskrunner

COPY ./packages/taskrunner .

RUN ./gradlew bootJar

WORKDIR /

# Copy the jar to the root directory
RUN mv /taskrunner/build/libs/*.jar .
RUN mv /terarium-1.0.0-SNAPSHOT.jar /taskrunner.jar

# ------------------------------------------------------------------------------

# Set up the Python image with JRE
FROM python:3.10-slim

WORKDIR /

# Copy the supervisord configuration file
COPY ./packages/nougat_cpu/supervisord.conf /etc/supervisor/conf.d/supervisord.conf

# Install OpenJDK JRE and wget
RUN apt-get update && \
	apt-get install -y --no-install-recommends \
	build-essential \
	git \
	libgl1 \
	poppler-utils \
	openjdk-17-jre-headless && \
	rm -rf /var/lib/apt/lists/*

# Copy the Spring Boot fat JAR from the builder image
COPY --from=nougat_cpu_taskrunner_builder /taskrunner/build/libs/*.jar /taskrunner.jar

# Install nougat
COPY --from=nougat_cpu_taskrunner_builder /document_intelligence /document_intelligence
WORKDIR /document_intelligence/document_intelligence/fast_latex
RUN pip install -r requirements.txt

# Install taskrunner
COPY ./packages/taskrunner/setup.py /taskrunner/setup.py
COPY ./packages/taskrunner/taskrunner.py /taskrunner/taskrunner.py
WORKDIR /taskrunner
RUN pip install --no-cache-dir -e .

# Install document_intelligence tasks
COPY ./packages/nougat_cpu /nougat_task
WORKDIR /nougat_task
RUN pip install --no-cache-dir -e .

#Install uvicorn and supervisord
RUN pip install uvicorn setuptools augraphy supervisor

WORKDIR /
CMD ["supervisord", "-c", "/etc/supervisor/conf.d/supervisord.conf"]
