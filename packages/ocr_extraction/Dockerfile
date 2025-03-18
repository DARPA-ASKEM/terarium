# Build the Spring Boot application
FROM python:3.11-slim-bookworm AS ocr_extraction_taskrunner_builder

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

# Install equation-extraction
WORKDIR /ocr_extraction
COPY ./packages/ocr_extraction /ocr_extraction
WORKDIR /ocr_extraction
RUN pip3 install --no-cache-dir --requirement requirements.txt

#Install uvicorn and supervisord
RUN pip3 install --no-cache-dir uvicorn setuptools augraphy supervisor

WORKDIR /taskrunner

COPY ./packages/taskrunner .

RUN ./gradlew bootJar

WORKDIR /

# Copy the jar to the root directory
RUN cp /taskrunner/build/libs/*.jar .
RUN mv /terarium-1.0.0-SNAPSHOT.jar /taskrunner.jar

# Copy the supervisord configuration file
COPY ./packages/ocr_extraction/supervisord.conf /etc/supervisor/conf.d/supervisord.conf

# Install taskrunner
COPY ./packages/taskrunner/setup.py /taskrunner/setup.py
COPY ./packages/taskrunner/taskrunner.py /taskrunner/taskrunner.py
WORKDIR /taskrunner
RUN pip install --no-cache-dir --editable .

# Install document_intelligence tasks
WORKDIR /ocr_extraction
RUN pip install --no-cache-dir --editable .

WORKDIR /
CMD ["supervisord", "-c", "/etc/supervisor/conf.d/supervisord.conf"]
