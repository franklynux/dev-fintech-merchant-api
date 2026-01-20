# Fintech-Merchant-Api

## Specification

[Tech Spec](SPEC.md)

Exposes API endpoints for merchants

## Security Scans Workflow (Trivy)

This project includes a GitHub Actions workflow for automated security scanning using **Trivy**. The workflow is triggered automatically on every **push** and **pull request** to any branch.

## Workflow Overview - configured in yaml 

The workflow performs multiple types of security scans to ensure the repository and its configuration are safe from vulnerabilities and misconfigurations. The main steps are:

1. **Checkout Repository**  
   The workflow starts by checking out the repository code using `actions/checkout@v4`.

2. **Install Trivy**  
   Trivy is installed on the runner using the official Aquasecurity repository. Required dependencies (`wget`, `apt-transport-https`, etc.) are installed to enable Trivy installation.

3. **Cache Trivy Database**  
   Trivy downloads a vulnerability database to detect security issues. The workflow caches this database (`~/.cache/trivy`) between runs to speed up subsequent scans.

4. **Secret Scanning**  
   Trivy scans the filesystem (`trivy fs`) for **secrets** such as API keys or credentials, focusing on **high** and **critical** severity. If any issues are found, the workflow fails with `--exit-code 1`.

5. **Vulnerability Scanning**  
   The workflow scans for **high and critical vulnerabilities** in dependencies, container images, and configuration files. Detected issues will fail the workflow, ensuring that vulnerabilities are caught early.

6. **Misconfiguration (PCI) Scanning**  
   Trivy also checks for **misconfigurations** in Dockerfiles, Kubernetes manifests, Terraform scripts, CloudFormation templates, YAML, and JSON files. Custom policies from the `./policies` directory are applied, with medium and higher severity issues causing the workflow to fail.

## Environment

- Runs on `ubuntu-latest` GitHub Actions runner.
- Uses `TRIVY_CACHE_DIR` to store the vulnerability database locally.

## Outcome

- If any secrets, vulnerabilities, or misconfigurations are detected at the configured severity levels, the workflow fails and prevents merging until issues are addressed.
- This ensures that code merged into the repository meets security standards and reduces the risk of introducing vulnerabilities.


## How to run


## How to run

### Prerequisites

* Java 17
* Maven 3.9+
* Docker
* Docker Compose

### Running Natively

1.  **Start Dependencies**: Ensure PostgreSQL and Redis are running locally.
    *   PostgreSQL: Port `5432`, Database `merchantdb`
    *   Redis: Port `6379`

2.  **Run the Application**:
    ```bash
    # Set required environment variables
    cp .env.example .env

    # Run with Maven
    mvn clean package
    mvn spring-boot:run
    ```
    The service will be available at `http://localhost:7000`.

3.  **Access Health Check Endpoint**:
    ```bash
    curl http://localhost:7000/actuator/health
    curl http://localhost:7000/system/health/
    ```

### Running with Docker Compose

This sets up the API, PostgreSQL, Redis, Prometheus, and Grafana.

1.  **Configure Environment**:
    Create a `.env` file in the root directory with the following variables:
    ```properties
    POSTGRES_DB=
    POSTGRES_USER=
    POSTGRES_PASSWORD=
    JWT_SECRET=
    ```

2.  **Start Services**:
    ```bash
    docker-compose up --build
    ```

3.  **Access Endpoints**:
    *   **API Home**: http://localhost:7000
    *   **Swagger UI**: http://localhost:7000/swagger-ui.html
    *   **Grafana**: http://localhost:3000
    *   **Prometheus**: http://localhost:9090
