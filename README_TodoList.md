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

- Access to the university GitLab: `lv-gitlab.intern.th-ab.de`
- SSH access to the VM: `10.97.15.178`
- Your GitLab personal access token or SSH key configured

---

## Clone the Repository

```bash
git clone https://lv-gitlab.intern.th-ab.de/agilesec26/sdi-scrumpros/apps/todo-list.git
cd todo-list
```

---

## Stage 1 — Build

Compile the project and download dependencies.

```java
mvn clean compile
```

**Expected output:** BUILD SUCCESS. The compiled classes will be available in: `target/classes/`

---

## Stage 2 — Tests

Run unit and integration tests.

```java
mvn test
```

**Expected output:** Tests run: X, Failures: 0, Errors: 0, BUILD SUCCESS

---

## Stage 3 — SAST (Static Application Security Testing)

**Semgrep — scans source code for security issues**

```java
docker run --rm \
  -v $(pwd):/src \
  returntocorp/semgrep \
  semgrep scan --config auto \
  --json \
  --output /src/reports/semgrep-report.json
```

**Expected output:** JSON report saved to ****`reports/semgrep-report.json`

**Gitleaks — scans Git history for leaked secrets**

```markdown
docker run --rm \
  -v $(pwd):/repo \
  zricethezav/gitleaks:latest \
  detect --source /repo
```

**Expected output:** JSON report saved to `reports/gitleaks-report.json`

---

## Stage 4 — Package

Packages the Todo List application into an executable JAR.

```basic
mvn package -DskipTests
```

**Expected output:** Generated artifact `target/todolist-0.0.1-SNAPSHOT.jar`

---

## **Stage 5 — SCA Package (Dependency Vulnerability Scan)**

Scans Maven dependencies against the NVD database for known CVEs. Requires an NVD API key.

```basic
mvn org.owasp:dependency-check-maven:check \
  -Dformat=ALL \
  -Ddependency-check.failOnError=false
```

**Expected output:** 

- `target/dependency-check-report.html`
- `target/dependency-check-report.json`

> Reports are generated in the `target/` directory and contain vulnerable Maven dependencies and CVEs.
> 

---

## **Stage 6 — Publish (Build and Push Docker Image)**

Builds the Docker image from the Dockerfile and pushes it to the GitLab Container Registry.

```basic
# Login to the GitLab registry
docker login lv-gitlab.intern.th-ab.de:5050

# Build the image
docker build \
  -t lv-gitlab.intern.th-ab.de:5050/agilesec26/sdi-scrumpros/apps/todo-list:latest \
  .

# Push the image
docker push lv-gitlab.intern.th-ab.de:5050/agilesec26/sdi-scrumpros/apps/todo-list:latest
```

**Expected output:** `target/todolist-0.0.1-SNAPSHOT.jar`. An executable JAR file is generated in the `target/` directory.

---

## **Stage 7 — SCA Container (Container Vulnerability Scan)**

Scans the built Docker image for OS and package vulnerabilities.

```basic
docker run --rm \
  aquasec/trivy:latest \
  image lv-gitlab.intern.th-ab.de:5050/agilesec26/sdi-scrumpros/apps/todo-list:latest
```

**Expected output:** `reports/trivy-report.json`. A JSON report containing container vulnerabilities is generated in the `reports/` folder.

---

## **Stage 8 — Deploy (Run the Application)**

SSH into the VM and run the container.

```basic
# Step 1 — Connect to VM (must be on VPN)
ssh your-username@10.97.15.178

# Step 2 — Login to registry
docker login lv-gitlab.intern.th-ab.de:5050

# Step 3 — Pull the latest image
docker pull lv-gitlab.intern.th-ab.de:5050/agilesec26/sdi-scrumpros/apps/todo-list:latest

# Step 4 — Stop old container if running
docker stop todolist || true
docker rm todolist || true

# Step 5 — Run container
docker run -d \
  --name todolist \
  --restart unless-stopped \
  -p 8080:8080 \
  lv-gitlab.intern.th-ab.de:5050/agilesec26/sdi-scrumpros/apps/todo-list:latest
```

---

## **Stage 9 — Fuzzing (JavaFuzz)**

Runs a simple fuzzing test against the deployed Todo List application.

The fuzzing stage generates 100 random inputs and sends them to the Todo API to check how the application handles unexpected values.

**Example request:**

```
GET /api/todos?title=<random_string>
```

The fuzzing stage automatically:

- Generates 100 random strings
- Sends requests to the Todo API
- Records the HTTP response status code for each request
- Saves the results as a JSON report

**Expected output:** A fuzzing report is generated `reports/fuzz-report.json`

---

## Stage 10 — DAST (Dynamic Application Security Testing)

Runs OWASP ZAP against the live application.

The application must already be deployed and running.

```basic
docker run --rm \
  -v $(pwd)/reports:/zap/wrk \
  --entrypoint "" \
  ghcr.io/zaproxy/zaproxy:stable \
  zap-baseline.py \
  -t http://10.97.15.178:8080 \
  -J dast-zap-report.json
```

**Expected output: `reports/dast-zap-report.json`.** A JSON report containing runtime security findings is generated in the `reports/` folder.

---

## Access the Running Application

Once deployed, open:

```
http://10.97.15.178:8080
```

You should see the Todo List application.

---

## Understanding Security Reports

All reports are saved in the `reports/` folder or generated as build artifacts:

| File | Tool | What it finds |
| --- | --- | --- |
| `semgrep-report.json` | Semgrep | Security issues and insecure coding patterns in source code |
| `gitleaks-report.json` | Gitleaks | Hardcoded secrets, credentials, API keys, and tokens |
| `dependency-check-report.json` | OWASP Dependency Check | Vulnerable Maven dependencies and known CVEs |
| `dependency-check-report.html` | OWASP Dependency Check | Human-readable version of dependency findings |
| `trivy-report.json` | Trivy | Operating system and container image vulnerabilities |
| `fuzz-report.json` | JavaFuzz (Custom Bash Fuzzing) | API responses to randomly generated inputs and potential input-handling issues |
| `dast-zap-report.json` | OWASP ZAP | Runtime web application vulnerabilities discovered during dynamic testing |

In the GitLab pipeline, these reports are uploaded as artifacts and can be downloaded from:

```
CI/CD → Pipelines → Select Pipeline → Download Artifacts
```

---

## Troubleshooting

| Problem | Solution |
| --- | --- |
| Maven build fails | Verify Java 21 is installed |
| Tests fail | Run `mvn test` locally to inspect errors |
| Docker login fails | Ensure GitLab credentials and VPN are active |
| Cannot SSH to VM | Verify VPN connection and SSH access |
| Port 8080 already in use | Stop the existing container first |
| Trivy scan fails | Ensure the image exists locally |
| ZAP scan fails | Confirm the application is running on port 8080 |
| NVD rate limit (429) | Check the `NVD_API_KEY` configuration |

---

## Pipeline Stages Order

```
build
→ test
→ sast
→ package
→ sca-package
→ publish
→ sca-container
→ deploy
→ fuzzing
→ dast
→ security_scan
```

Each stage only runs after the previous one completes successfully, except stages marked with `allow_failure: true`, which allow the pipeline to continue even when vulnerabilities are found.

---

## Security Notes

- All credentials are stored as masked GitLab CI variables — never in code.
- Security stages use `allow_failure: true`, meaning findings are reported but do not block deployment.
- Dependency Check may report known vulnerable dependencies as part of the scan results.
- Trivy reports container and operating system vulnerabilities.
- Health checks verify that the application is running before DAST scans begin.
- The Docker image should be rebuilt whenever application dependencies are updated.