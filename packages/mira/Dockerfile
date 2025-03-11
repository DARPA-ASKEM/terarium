# Build the Spring Boot application
FROM python:3.10-bookworm AS mira_taskrunner_builder

###### DEV ONLY ######
#VVVVVVVVVVVVVVVVVVVVV

# These deps are installed only for use during local development

# Install OpenJDK and libraries for Mira
RUN apt-get update && apt-get install --assume-yes --no-install-recommends \
	wget graphviz libgraphviz-dev pkg-config \
	openjdk-17-jdk-headless && \
	rm -rf /var/lib/apt/lists/*

# Install Mira
COPY ./packages/mira/mira-version.txt /miraVersion.txt
RUN COMMIT_SHA="$(cat /miraVersion.txt)" && \
	echo "Using MIRA commit $COMMIT_SHA" && \
	wget --progress=dot:giga -O mira.tar.gz "https://github.com/DARPA-ASKEM/mira/archive/${COMMIT_SHA}.tar.gz" && \
	tar -zxvf mira.tar.gz && \
	rm mira.tar.gz && \
	mv -v mira-* mira
WORKDIR /mira
RUN pip install --no-cache-dir .[sbml,biomodels]

# install sympy
RUN pip install --no-cache-dir sympy antlr4-python3-runtime==4.11 pygraphviz ipython

#^^^^^^^^^^^^^^^^^^^^
######################

WORKDIR /taskrunner

COPY ./packages/taskrunner .

RUN ./gradlew bootJar

# ------------------------------------------------------------------------------
FROM python:3.10-bookworm

WORKDIR /

# Install OpenJRE and libraries for Mira
RUN apt-get update && apt-get install --assume-yes --no-install-recommends \
	wget graphviz libgraphviz-dev pkg-config \
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
RUN pip install --no-cache-dir --editable .

# Install Mira tasks
COPY ./packages/mira /mira_task

WORKDIR /mira_task
RUN pip install --no-cache-dir --editable .

# install sympy
RUN pip install --no-cache-dir sympy antlr4-python3-runtime==4.11 pygraphviz ipython

WORKDIR /
CMD ["java", "-jar", "taskrunner.jar"]
