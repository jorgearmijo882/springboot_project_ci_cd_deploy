# Etapa 1: Builder (Maven + JDK)
FROM maven:3.8.4-openjdk-17-slim AS builder

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -B -DskipTests

# Etapa 2: Runtime (OpenJDK)
FROM openjdk:17-slim AS runtime

WORKDIR /app
COPY --from=builder /app/target/*.jar ./app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
