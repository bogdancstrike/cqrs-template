# --- Build Stage ---
# Use an official Maven image with a specific JDK version (e.g., JDK 21) to build the application
FROM maven:3.9-eclipse-temurin-21 AS builder

# Set the working directory in the container for the build stage
WORKDIR /build

# Copy the pom.xml file to download dependencies
COPY pom.xml .

# Download all dependencies (this layer will be cached by Docker if pom.xml doesn't change)
# Using go-offline to ensure all dependencies are downloaded before trying to package
# This helps in environments where internet access might be restricted during later parts of the build
RUN mvn dependency:go-offline -B

# Copy the rest of the application source code
COPY src ./src

# Package the application (compile, test, and create the JAR)
# Skipping tests for faster Docker builds; tests should be run in CI pipeline
RUN mvn clean package -DskipTests

# --- Application Stage ---
# Use a slim OpenJDK runtime as the parent image for the final application
FROM eclipse-temurin:21-jre-jammy

# Set the working directory in the container
WORKDIR /app

# Add a volume for temporary files if needed by the app (e.g., for Spring Boot)
VOLUME /tmp

# Define an argument for the JAR name (though it's fixed from the build stage)
# The JAR name in the target directory of your project (e.g., cqrs-0.0.1-SNAPSHOT.jar)
# This needs to match the artifactId and version in your pom.xml
ARG FINAL_JAR_NAME=cqrs-0.0.1-SNAPSHOT.jar

# Copy the executable JAR file from the builder stage
COPY --from=builder /build/target/${FINAL_JAR_NAME} app.jar

# Make port 7676 available (as per your application logs)
EXPOSE 7676

# Run the JAR file when the container launches
# Using exec form to make Java the PID 1 process, allowing it to receive signals correctly
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar"]
