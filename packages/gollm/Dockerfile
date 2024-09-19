# Build the Spring Boot application
FROM eclipse-temurin:21.0.4_7-jdk-noble AS gollm_taskrunner_builder

###### DEV ONLY ######
#VVVVVVVVVVVVVVVVVVVVV

# These deps are installed only for use during local development

# Install Python
RUN apt-get update && apt-get install -y --no-install-recommends \
	software-properties-common
RUN add-apt-repository ppa:deadsnakes/ppa
RUN apt-get update && apt-get install -y --no-install-recommends \
 python3.11 \
 python3.11-venv \
 wget && \
 rm -rf /var/lib/apt/lists/*

# Set the default Python version to 3.11
RUN update-alternatives --install /usr/bin/python python /usr/bin/python3.11 1 && \
 update-alternatives --set python /usr/bin/python3.11
RUN update-alternatives --install /usr/bin/python3 python3 /usr/bin/python3.11 1 && \
 update-alternatives --set python3 /usr/bin/python3.11

# install pip for python 3.11
RUN python -m ensurepip --upgrade
RUN /bin/sh -c set -eux [ -s "/usr/local/bin/pip3" ]; [ ! -e "/usr/local/bin/pip" ]; ln -svT "pip3" "/usr/local/bin/pip";

# Verify the installation
RUN python --version
RUN pip --version

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
FROM eclipse-temurin:21.0.4_7-jre-noble

WORKDIR /

# Install Python
RUN apt-get update && apt-get install -y --no-install-recommends \
	software-properties-common
RUN add-apt-repository ppa:deadsnakes/ppa
RUN apt-get update && apt-get install -y --no-install-recommends \
 python3.11 \
 python3.11-venv && \
 rm -rf /var/lib/apt/lists/*

# Set the default Python version to 3.11
RUN update-alternatives --install /usr/bin/python python /usr/bin/python3.11 1 && \
 update-alternatives --set python /usr/bin/python3.11
RUN update-alternatives --install /usr/bin/python3 python3 /usr/bin/python3.11 1 && \
 update-alternatives --set python3 /usr/bin/python3.11

# install pip for python 3.11
RUN python -m ensurepip --upgrade
RUN /bin/sh -c set -eux [ -s "/usr/local/bin/pip3" ]; [ ! -e "/usr/local/bin/pip" ]; ln -svT "pip3" "/usr/local/bin/pip";

# Verify the installation
RUN python --version
RUN pip --version

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
