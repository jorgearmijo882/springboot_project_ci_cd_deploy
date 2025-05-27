# Spring Boot CI/CD Pipeline with Azure DevOps

Este proyecto demuestra un flujo completo de CI/CD para una aplicación monolítica Spring Boot usando:

- **Docker** (multi-stage Dockerfile & docker-compose)
- **Azure DevOps** (self-hosted agent, YAML pipelines, templates)
- **Análisis estático** con SpotBugs
- **Despliegue continuo** por SSH/SCP a un servidor remoto

---

## Estructura del Repositorio

```
├── Dockerfile
├── docker-compose.yml
├── README.md
├── src/
│   └── main/java/...            # Código fuente Spring Boot
├── templates/
│   ├── variables.yml            # Variables globales del pipeline
│   ├── build.yml                # Job Build: Docker build & extracción JAR
│   ├── sast.yml                 # Job SAST: SpotBugs
│   ├── release.yml              # Job Release: PublishPipelineArtifact
│   └── deploy.yml               # Job Deploy: Download, SCP/SSH, backup, restart, health-check
└── azure-pipelines.yml          # Pipeline padre que orquesta los 4 stages
```

---

## Prerrequisitos

1. **Windows 10/11** con **Docker Desktop** instalado y funcionando.  
2. **OpenSSH Client** habilitado en Windows.  
3. **Azure DevOps**:
   - Organización y proyecto configurados.
   - **Self-hosted agent** registrado y online (ver run.cmd o servicio).  
   - Variables de pipeline configuradas en _Library_ o _Pipeline variables_:
     - `RemoteHost`, `RemoteUser`, `RemotePath`, `BackupPath`, `HealthCheckUrl`.
   - (Opcional) Service Connection ARM si usas tareas AzureCLI.

4. **Git** instalado.  

---

## Preparar el Ambiente

1. **Clonar el repositorio**  
   ```bash
   git clone https://github.com/jorgearmijo882/springboot_project_ci_cd_deploy
   cd springboot_project_ci_cd_deploy
   ```

2. **Levantar servicios con Docker-Compose** (para pruebas locales)  
   ```bash
   docker-compose up --build
   ```
   - Define servicios `app` y `db` (si aplica) en `docker-compose.yml`.
   - Prueba `http://localhost:8080/hello`.

3. **Registrar el self-hosted agent**  
   ```powershell
   cd C:zagent
   .\config.cmd --url https://dev.azure.com/TuOrg --pool Default --auth pat --token <TU_PAT>
   .\run.cmd
   ```
   O como servicio:
   ```powershell
   .\svc install
   .\svc start
   ```

4. **Configurar variables de pipeline**  
   En _Pipelines → Library → Variable groups_ define:
   | Nombre         | Valor                          |
   | -------------- | ------------------------------ |
   | RemoteHost     | host.docker.internal           |
   | RemoteUser     | root                           |
   | RemotePath     | /opt/demo                      |
   | BackupPath     | /opt/demo/backups              |
   | HealthCheckUrl | http://host.docker.internal/hello |

---

## Dockerfile

\`\`\`dockerfile
# Stage 1: Builder
FROM maven:3.8.4-openjdk-17-slim AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -B -DskipTests

# Stage 2: Runtime
FROM openjdk:17-slim AS runtime
WORKDIR /app
COPY --from=builder /app/target/*.jar ./app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
\`\`\`

- **builder**: compila offline y empaqueta.  
- **runtime**: imagen ligera sólo con el JAR.

---

## docker-compose.yml

\`\`\`yaml
version: '3.8'

services:
  app:
    build:
      context: .
      target: runtime
    ports:
      - "8080:8080"
  compiler:
    build:
      context: .
      target: builder
    command: tail -f /dev/null
\`\`\`

- `app`: servicio Spring Boot.  
- `compiler`: contenedor efímero para CI local.

---

## Pipeline Padre (\`azure-pipelines.yml\`)

\`\`\`yaml
trigger:
  branches: [ main ]

variables:
- template: templates/variables.yml

stages:
- stage: Build
  jobs:
  - template: templates/build.yml
    parameters:
      imageName: '$(Build.Repository.Name)-compiler:$(Build.BuildId)'

- stage: SAST
  dependsOn: Build
  jobs:
  - template: templates/sast.yml

- stage: Release
  dependsOn: SAST
  jobs:
  - template: templates/release.yml
    parameters:
      artifactName: 'demo'
      version: '$(Build.BuildId)'

- stage: Deploy
  dependsOn: Release
  jobs:
  - template: templates/deploy.yml
    parameters:
      artifactName: 'demo'
      version: '$(Build.BuildId)'
      remoteHost: $(RemoteHost)
      remoteUser: $(RemoteUser)
      remotePath: $(RemotePath)
      backupPath: $(BackupPath)
      healthCheckUrl: $(HealthCheckUrl)
\`\`\`

---

## Templates

### templates/variables.yml


### templates/build.yml

### templates/sast.yml

### templates/release.yml

### templates/deploy.yml


---

## Ejecución del Pipeline

1. **Commit & push** todos los cambios.  
2. En Azure DevOps, selecciona **Run pipeline** en la rama `main`.  
3. Verifica **Build → SAST → Release → Deploy** exitosos.  
4. Para re-ejecutar solo el stage fallido, usa **Run → Rerun failed jobs** en la UI.

---

## Contacto

**Jorge Luis Armijo Quito**  
- LinkedIn: https://www.linkedin.com/in/jorge-armijo-05051264/  
- Teléfono: +593 99 255 4661  
