# Build the Spring Boot application
FROM python:3.10-slim-bookworm AS mira_taskrunner_builder

###### DEV ONLY ######
#VVVVVVVVVVVVVVVVVVVVV

# These deps are installed only for use during local development

# Install OpenJDK
RUN apt-get update && apt-get install -y --no-install-recommends \
	wget \
	openjdk-17-jdk-headless && \
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

# install sympy
RUN pip install sympy antlr4-python3-runtime==4.11 pandas fastapi neo4j

#^^^^^^^^^^^^^^^^^^^^
######################

WORKDIR /taskrunner

COPY ./packages/taskrunner .

RUN ./gradlew bootJar

# ------------------------------------------------------------------------------
FROM python:3.10-slim-bookworm

WORKDIR /

# Install OpenJDK JRE
RUN apt-get update && apt-get install -y --no-install-recommends \
	wget \
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

# install sympy
RUN pip install sympy antlr4-python3-runtime==4.11 pandas fastapi neo4j

WORKDIR /
CMD ["java", "-jar", "taskrunner.jar"]
