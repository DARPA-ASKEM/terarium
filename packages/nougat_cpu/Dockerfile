# Build the Spring Boot application
FROM python:3.11-slim AS nougat_cpu_taskrunner_builder

###### DEV ONLY ######
#VVVVVVVVVVVVVVVVVVVVV

# These deps are installed only for use during local development

# Install OpenJDK21
RUN apt-get update && apt-get install -y --no-install-recommends \
	build-essential \
	git \
	libgl1 \
	poppler-utils \
	wget && \
	rm -rf /var/lib/apt/lists/*

RUN wget https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.4%2B7/OpenJDK21U-jdk_x64_linux_hotspot_21.0.4_7.tar.gz && \
	tar xvf OpenJDK21U-jdk_x64_linux_hotspot_21.0.4_7.tar.gz && \
	mv jdk-21.0.4+7 /usr/local/jdk-21 && \
	rm OpenJDK21U-jdk_x64_linux_hotspot_21.0.4_7.tar.gz

ENV JAVA_HOME=/usr/local/jdk-21
ENV PATH="$JAVA_HOME/bin:$PATH"

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
FROM python:3.11-slim

WORKDIR /

# Install OpenJRE21
RUN apt-get update && apt-get install -y --no-install-recommends \
	build-essential \
	git \
	libgl1 \
	poppler-utils \
	wget && \
	rm -rf /var/lib/apt/lists/*

RUN wget https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.4%2B7/OpenJDK21U-jre_x64_linux_hotspot_21.0.4_7.tar.gz && \
	tar xvf OpenJDK21U-jre_x64_linux_hotspot_21.0.4_7.tar.gz && \
	mv jdk-21.0.4+7-jre /usr/local/jre-21 && \
	rm OpenJDK21U-jre_x64_linux_hotspot_21.0.4_7.tar.gz

ENV JAVA_HOME=/usr/local/jre-21
ENV PATH="$JAVA_HOME/bin:$PATH"

# Copy the supervisord configuration file
COPY ./packages/nougat_cpu/supervisord.conf /etc/supervisor/conf.d/supervisord.conf

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
