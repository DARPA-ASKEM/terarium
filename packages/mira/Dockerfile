# Build the Spring Boot application
FROM python:3.10-slim AS mira_taskrunner_builder

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
FROM python:3.10-slim

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
