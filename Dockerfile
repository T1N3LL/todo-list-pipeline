# ==========================================
# Todo List Application Dockerfile
# ==========================================
#
# Purpose:
# Run the Spring Boot Todo List application
# inside a Docker container.
#
# This Dockerfile is used by the GitLab CI/CD
# pipeline during the Publish stage.
#
# Prerequisites:
# - The package-jar stage must generate:
#   target/todolist-0.0.1-SNAPSHOT.jar
#
# Environment Variables:
# - SERVER_PORT (optional)
#   Default value: 8080
#
# Exposed Port:
# - 8080
#
# ==========================================

# Use Java 21 runtime image
FROM maven:3.9-eclipse-temurin-21

# Set working directory inside the container
WORKDIR /app

# Copy the packaged Spring Boot JAR file
COPY target/todolist-0.0.1-SNAPSHOT.jar app.jar

# Default application port
ENV SERVER_PORT=8080

# Expose application port
EXPOSE 8080

# Start the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
