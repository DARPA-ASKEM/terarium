# FROM ghcr.io/graalvm/graalvm-ce:java11-21.3.0 AS taskrunner_builder

# WORKDIR /taskrunner
# COPY ./packages/taskrunner .

# RUN ./gradlew nativeCompile

# FROM python:3.10-slim

# WORKDIR /

# RUN apt-get update && \
# 	apt-get install -y --no-install-recommends wget && \
# 	rm -rf /var/lib/apt/lists/*

# RUN wget -O gollm.tar.gz https://github.com/DARPA-ASKEM/GoLLM/archive/refs/heads/2544-golmm-in-hmi.tar.gz && \
# 	tar -zxvf gollm.tar.gz && \
# 	mv GoLLM-* GoLLM

# WORKDIR /GoLLM

# # ENV OPENAI_API_KEY=""

# RUN ls -la /GoLLM

# RUN pip install --no-cache-dir -r requirements.txt

# COPY --from=taskrunner_builder /taskrunner/build/native/nativeCompile .

# CMD ["/taskrunner/build/native/nativeCompile/terarium"]

# Build the Spring Boot application
FROM eclipse-temurin:17-jdk AS taskrunner_builder

WORKDIR /taskrunner
COPY ./packages/taskrunner .

RUN ./gradlew bootJar

# Set up the Python image with JRE
FROM python:3.10-slim

WORKDIR /

RUN apt-get update && \
	apt-get install -y --no-install-recommends wget && \
	rm -rf /var/lib/apt/lists/*

RUN wget -O gollm.tar.gz https://github.com/DARPA-ASKEM/GoLLM/archive/refs/heads/2544-golmm-in-hmi.tar.gz && \
	tar -zxvf gollm.tar.gz && \
	mv GoLLM-* GoLLM

WORKDIR /GoLLM

# ENV OPENAI_API_KEY=""

RUN ls -la /GoLLM

RUN pip install --no-cache-dir -r requirements.txt

# Install OpenJDK JRE
RUN apt-get update && \
	apt-get install -y --no-install-recommends openjdk-17-jre-headless && \
	rm -rf /var/lib/apt/lists/*

# Copy the Spring Boot fat JAR from the builder image
COPY --from=taskrunner_builder /taskrunner/build/libs/*.jar /taskrunner.jar

WORKDIR /

CMD ["java", "-jar", "taskrunner.jar"]
