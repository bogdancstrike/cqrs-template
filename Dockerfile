# Use an official OpenJDK 21 runtime as a parent image
FROM eclipse-temurin:21-jdk-jammy

# Set the working directory in the container
WORKDIR /app

# Add a volume for temporary files if needed by the app (e.g., for Spring Boot)
VOLUME /tmp

# Arguments for the JAR file name (can be overridden at build time)
# This pattern should match your Spring Boot fat JAR.
ARG JAR_FILE=target/cqrs-*.jar

# Copy the executable JAR file from the target directory to the container
COPY ${JAR_FILE} app.jar

# Make port 7676 available (as per your application logs)
EXPOSE 7676

# Run the JAR file when the container launches
# Using exec form to make Java the PID 1 process, allowing it to receive signals correctly
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar"]
