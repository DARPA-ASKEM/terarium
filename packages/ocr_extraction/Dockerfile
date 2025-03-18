# Build the Spring Boot application
FROM python:3.11-slim-bookworm AS ocr_extraction_taskrunner_builder

###### DEV ONLY ######
#VVVVVVVVVVVVVVVVVVVVV

# These deps are installed only for use during local development

# Install OpenJDK
RUN apt-get update && apt-get install --assume-yes --no-install-recommends \
	build-essential \
	git \
	libgl1 \
	libglib2.0-0 \
	poppler-utils \
	wget \
	openjdk-17-jdk-headless && \
	rm -rf /var/lib/apt/lists/*

# Install ocr-extraction
COPY ./packages/ocr_extraction/requirements.txt /ocr_extraction/requirements.txt
COPY ./packages/ocr_extraction/src /ocr_extraction/src
COPY ./packages/ocr_extraction/supervisord.conf /etc/supervisor/conf.d/supervisord.conf

WORKDIR /ocr_extraction
RUN pip3 install --no-cache-dir --requirement requirements.txt

#Install uvicorn and supervisord
RUN pip3 install --no-cache-dir uvicorn setuptools python-multipart supervisor


WORKDIR /taskrunner
COPY ./packages/taskrunner .
RUN ./gradlew bootJar


# Copy the jar to the root directory
WORKDIR /
RUN cp /taskrunner/build/libs/*.jar .
RUN mv /terarium-1.0.0-SNAPSHOT.jar /taskrunner.jar




# ------------------------------------------------------------------------------
FROM python:3.11-slim-bookworm

WORKDIR /

# Install OpenJDK JRE
RUN apt-get update && apt-get install -y --no-install-recommends \
	build-essential \
	git \
	libgl1 \
	libglib2.0-0 \
	poppler-utils \
	wget  \
	openjdk-17-jre-headless && \
	rm -rf /var/lib/apt/lists/*

# Copy the supervisord configuration file
COPY ./packages/ocr_extraction/supervisord.conf /etc/supervisor/conf.d/supervisord.conf

# Copy the Spring Boot fat JAR from the builder image
COPY --from=ocr_extraction_taskrunner_builder /taskrunner/build/libs/*.jar /taskrunner.jar

# Install table-extraction
COPY --from=ocr_extraction_taskrunner_builder /ocr_extraction /ocr_extraction


WORKDIR /ocr_extraction
RUN pip install --no-cache-dir --requirement requirements.txt

# Install taskrunner
COPY ./packages/taskrunner/setup.py /taskrunner/setup.py
COPY ./packages/taskrunner/taskrunner.py /taskrunner/taskrunner.py
WORKDIR /taskrunner
RUN pip install --no-cache-dir --editable .

# Install document_intelligence tasks
COPY ./packages/ocr_extraction/tasks /ocr_extraction/tasks
WORKDIR /ocr_extraction/tasks
RUN pip install --no-cache-dir --editable .

#Install uvicorn and supervisord
RUN pip install --no-cache-dir uvicorn setuptools python-multipart supervisor

WORKDIR /
CMD ["supervisord", "-c", "/etc/supervisor/conf.d/supervisord.conf"]
