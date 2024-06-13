# Build the Spring Boot application
FROM eclipse-temurin:17.0.11_9-jdk-focal AS taskrunner_dev_builder

COPY ./packages/taskrunner .

WORKDIR /taskrunner

CMD [ "./gradlew", "build", "--continuous", "--quiet", "-x", "test"]
