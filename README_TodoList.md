# README_Todo List

# Todo List — Running the Pipeline Locally

This guide explains how to run the main pipeline stages locally before pushing changes to GitLab.

## Prerequisites

Make sure the following tools are installed:

| Tool | Version | Check |
| --- | --- | --- |
| Git | Any | `git --version` |
| Java | 21+ | `java --version` |
| Maven | 3.9+ | `mvn --version` |
| Docker | Any | `docker --version` |
| VPN | Active | Required for VM and GitLab access |

You will also need:

- Access to the university GitLab
- Access to the deployment VM
- Docker permissions on your machine

---

## Clone the Repository

```bash
git clone <repository-url>
cd todo-list
```

---

## Stage 1 — Build

Compile the project and download dependencies.

```bash
mvn clean compile
```

Expected output:

```
BUILD SUCCESS
```

---

## Stage 2 — Run Tests

Run unit and integration tests.

```bash
mvn test
mvn verify
```

Expected output:

```
Tests run: X, Failures: 0, Errors: 0
BUILD SUCCESS
```

---

## Stage 3 — Package

Create the application JAR file.

```bash
mvn package -DskipTests
```

Expected output:

```
target/todolist-0.0.1-SNAPSHOT.jar
```

---

## Stage 4 — SAST (Static Application Security Testing)

### Semgrep

Scan the source code for common security issues.

```bash
docker run --rm \
  -v $(pwd):/src \
  returntocorp/semgrep \
  semgrep scan --config auto /src
```

### Gitleaks

Scan the repository for accidentally committed secrets.

```bash
docker run --rm \
  -v $(pwd):/repo \
  zricethezav/gitleaks:latest \
  detect --source /repo
```

---

## Stage 5 — SCA Package (Dependency Check)

Scan project dependencies for known vulnerabilities.

```bash
mvn org.owasp:dependency-check-maven:check
```

Reports will be generated inside the project target folder.

If using an NVD API key:

```bash
export NVD_API_KEY=<your-key>
```

This makes the scan much faster.

---

## Stage 6 — Build Docker Image

Build the Docker image using the Dockerfile.

```bash
docker build -t todolist:local .
```

Check that the image exists:

```bash
docker images
```

Expected output:

```
todolist    local
```

---

## Stage 7 — Container Vulnerability Scan

Scan the Docker image using Trivy.

```bash
docker run --rm \
  aquasec/trivy:latest \
  image todolist:local
```

This checks for operating system and package vulnerabilities inside the container image.

---

## Stage 8 — Run the Application

Start the application locally.

```bash
docker run -d \
  --name todolist \
  -p 8080:8080 \
  todolist:local
```

Open:

```
http://localhost:8080
```

You should see the Todo List application.

---

## Stage 9 — Deploy to the VM

Connect to the VM:

```bash
ssh <username>@10.97.15.178
```

Pull the latest image:

```bash
docker pull <gitlab-registry-image>
```

Stop any existing container:

```bash
docker stop todolist || true
docker rm todolist || true
```

Run the application:

```bash
docker run -d \
  --name todolist \
  -p 8080:8080 \
  <gitlab-registry-image>
```

---

## Stage 10 — DAST (OWASP ZAP)

Run a basic security scan against the deployed application.

```bash
docker run --rm \
  ghcr.io/zaproxy/zaproxy:stable \
  zap-baseline.py \
  -t http://10.97.15.178:8080
```

This scan looks for common web security issues such as missing security headers and insecure configurations.

---

## Pipeline Order

The GitLab pipeline follows this order:

```
build
→ test
→ sast
→ package
→ sca-package
→ publish
→ sca-container
→ deploy
→ dast
→ security_scan
```

Most security jobs are configured with `allow_failure: true`, meaning vulnerabilities are reported but do not stop deployment.

---

## Troubleshooting

| Problem | Solution |
| --- | --- |
| Maven build fails | Check Java 21 is installed |
| Docker build fails | Verify Docker is running |
| Deploy fails | Check VPN connection and VM access |
| Trivy cannot scan image | Ensure the image was built successfully |
| Dependency Check is slow | Configure an NVD API key |
| ZAP scan fails | Verify the application is running first |
| Port 8080 already in use | Stop the existing container before starting a new one |

---

## Security Notes

- Secrets should be stored in GitLab CI/CD variables.
- Do not commit passwords, API keys, or tokens to the repository.
- Security scans are used to identify vulnerabilities early in the development process.
- Vulnerability reports can be downloaded from the GitLab pipeline artifacts after each run.