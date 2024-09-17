# Build the Spring Boot application
FROM eclipse-temurin:21-jdk-noble AS funman_taskrunner_builder

###### DEV ONLY ######
#VVVVVVVVVVVVVVVVVVVVV

# These deps are installed only for use during local development

# Install Python
RUN apt-get update && apt-get install -y --no-install-recommends \
	software-properties-common
RUN add-apt-repository ppa:deadsnakes/ppa
RUN apt-get update && apt-get install -y --no-install-recommends \
 python3.8 \
 python3.8-venv \
 gcc \
 git \
 wget && \
 rm -rf /var/lib/apt/lists/*

# Set the default Python version to 3.8
RUN update-alternatives --install /usr/bin/python python /usr/bin/python3.8 1 && \
 update-alternatives --set python /usr/bin/python3.8
RUN update-alternatives --install /usr/bin/python3 python3 /usr/bin/python3.8 1 && \
 update-alternatives --set python3 /usr/bin/python3.8

# install pip for python 3.8
RUN python -m ensurepip --upgrade
RUN /bin/sh -c set -eux [ -s "/usr/local/bin/pip3" ]; [ ! -e "/usr/local/bin/pip" ]; ln -svT "pip3" "/usr/local/bin/pip";

# Verify the installation
RUN python --version
RUN pip --version

# Install funman-api
COPY ./packages/funman/funman-version.txt /funmanVersion.txt
RUN COMMIT_SHA="$(cat /funmanVersion.txt)" && \
	echo "Using FUNMAN commit $COMMIT_SHA" && \
	wget --progress=dot:giga -O funman.tar.gz "https://github.com/DARPA-ASKEM/funman-api/archive/${COMMIT_SHA}.tar.gz" && \
	tar -zxvf funman.tar.gz && \
	rm funman.tar.gz && \
	mv -v funman-* funman-api
WORKDIR /funman-api
RUN pip install --no-cache-dir . && \
	pip install --no-cache-dir auxiliary_packages/funman_dreal && \
	pip install --no-cache-dir auxiliary_packages/funman_demo

#^^^^^^^^^^^^^^^^^^^^
######################

WORKDIR /taskrunner

COPY ./packages/taskrunner .

RUN ./gradlew bootJar

# ------------------------------------------------------------------------------

# Funman-base, should contain dreal4, ibex, and python dependencies
FROM ghcr.io/darpa-askem/funman-base:latest

WORKDIR /

# Install OpenJDK JRE and wget
RUN apt-get update && \
	apt-get install -y --no-install-recommends \
	openjdk-21-jre-headless && \
	rm -rf /var/lib/apt/lists/*

# Copy the Spring Boot fat JAR from the builder image
COPY --from=funman_taskrunner_builder /taskrunner/build/libs/*.jar /taskrunner.jar

# Install funman-api
COPY --from=funman_taskrunner_builder /funman-api /funman-api
WORKDIR /funman-api
RUN pip install --no-cache-dir . && \
	pip install --no-cache-dir auxiliary_packages/funman_dreal && \
	pip install --no-cache-dir auxiliary_packages/funman_demo

# Install taskrunner
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
