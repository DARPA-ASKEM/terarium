# Build the Spring Boot application
FROM eclipse-temurin:21-jdk-noble AS mira_taskrunner_builder

###### DEV ONLY ######
#VVVVVVVVVVVVVVVVVVVVV

# These deps are installed only for use during local development

# Install Python
RUN apt-get update && apt-get install -y --no-install-recommends \
	software-properties-common
RUN add-apt-repository ppa:deadsnakes/ppa
RUN apt-get update && apt-get install -y --no-install-recommends \
 python3.10 \
 python3.10-venv \
 wget && \
 rm -rf /var/lib/apt/lists/*

# Set the default Python version to 3.10
RUN update-alternatives --install /usr/bin/python python /usr/bin/python3.10 1 && \
 update-alternatives --set python /usr/bin/python3.10
RUN update-alternatives --install /usr/bin/python3 python3 /usr/bin/python3.10 1 && \
 update-alternatives --set python3 /usr/bin/python3.10

# install pip for python 3.10
RUN python -m ensurepip --upgrade
RUN /bin/sh -c set -eux [ -s "/usr/local/bin/pip3" ]; [ ! -e "/usr/local/bin/pip" ]; ln -svT "pip3" "/usr/local/bin/pip";

# Verify the installation
RUN python --version
RUN pip --version

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
FROM eclipse-temurin:21-jre-noble

WORKDIR /

# Install Python
RUN apt-get update && apt-get install -y --no-install-recommends \
	software-properties-common
RUN add-apt-repository ppa:deadsnakes/ppa
RUN apt-get update && apt-get install -y --no-install-recommends \
 python3.10 \
 python3.10-venv && \
 rm -rf /var/lib/apt/lists/*

# Set the default Python version to 3.10
RUN update-alternatives --install /usr/bin/python python /usr/bin/python3.10 1 && \
 update-alternatives --set python /usr/bin/python3.10
RUN update-alternatives --install /usr/bin/python3 python3 /usr/bin/python3.10 1 && \
 update-alternatives --set python3 /usr/bin/python3.10

# install pip for python 3.10
RUN python -m ensurepip --upgrade
RUN /bin/sh -c set -eux [ -s "/usr/local/bin/pip3" ]; [ ! -e "/usr/local/bin/pip" ]; ln -svT "pip3" "/usr/local/bin/pip";

# Verify the installation
RUN python --version
RUN pip --version

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
