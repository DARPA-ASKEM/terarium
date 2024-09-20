# Build the Spring Boot application
FROM python:3.11-slim AS gollm_taskrunner_builder

###### DEV ONLY ######
#VVVVVVVVVVVVVVVVVVVVV

# These deps are installed only for use during local development

# Install OpenJDK21
RUN apt-get update && apt-get install -y --no-install-recommends \
	wget && \
	rm -rf /var/lib/apt/lists/*

RUN wget https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.4%2B7/OpenJDK21U-jdk_x64_linux_hotspot_21.0.4_7.tar.gz && \
	tar xvf OpenJDK21U-jdk_x64_linux_hotspot_21.0.4_7.tar.gz && \
	mv jdk-21.0.4+7 /usr/local/jdk-21 && \
	rm OpenJDK21U-jdk_x64_linux_hotspot_21.0.4_7.tar.gz

ENV JAVA_HOME=/usr/local/jdk-21
ENV PATH="$JAVA_HOME/bin:$PATH"

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
FROM python:3.11-slim

WORKDIR /

# Install OpenJRE21
RUN apt-get update && apt-get install -y --no-install-recommends \
	wget && \
	rm -rf /var/lib/apt/lists/*

RUN wget https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.4%2B7/OpenJDK21U-jre_x64_linux_hotspot_21.0.4_7.tar.gz && \
	tar xvf OpenJDK21U-jre_x64_linux_hotspot_21.0.4_7.tar.gz && \
	mv jdk-21.0.4+7-jre /usr/local/jre-21 && \
	rm OpenJDK21U-jre_x64_linux_hotspot_21.0.4_7.tar.gz

ENV JAVA_HOME=/usr/local/jre-21
ENV PATH="$JAVA_HOME/bin:$PATH"

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
