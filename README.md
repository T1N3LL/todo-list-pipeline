# README_Todo List

## **Running the Pipeline Locally**

### **Prerequisites**

Before running the pipeline locally, make sure the following tools are installed:

- Java 21
- Maven 3.9+
- Docker
- Git

### **Build the Application**

Compile and package the application into a JAR file:

```
mvn clean package
```

The generated JAR file can be found in:

```
target/todolist-0.0.1-SNAPSHOT.jar
```

### **Run Unit and Integration Tests**

```
mvn test
mvn verify
```

### **Build the Docker Image**

Build the container image using the provided Dockerfile:

```
docker build -t todolist:local .
```

### **Run the Application in Docker**

```
docker run -d -p 8080:8080 --name todolist todolist:local
```

The application will be available at `http://localhost:8080`

### **Security Scans**

Run Dependency Check:

```
mvn org.owasp:dependency-check-maven:check
```

Run Semgrep:

```
semgrep scan --config auto .
```

Run Gitleaks:

```
gitleaks detect --source .
```

Run Trivy Container Scan:

```
trivy image todolist:local
```

### **Stop the Container**

```
docker stop todolist
docker rm todolist
```

This process follows the same steps used in the GitLab CI/CD pipeline: build, test, package, publish, deploy, and security scanning.