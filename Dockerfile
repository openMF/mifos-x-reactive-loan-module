# ────────────────────────────────
# Stage 1  — Build the fat JAR
# ────────────────────────────────
FROM gradle:8.14-jdk21 AS builder
WORKDIR /app
COPY . .
# use the wrapper and skip tests for faster local build
RUN ./gradlew clean bootJar -x test --no-daemon

# ────────────────────────────────
# Stage 2  — Runtime image
# ────────────────────────────────
FROM openjdk:21-jdk-slim
WORKDIR /opt/app

# non-root user for better security
RUN useradd -ms /bin/bash loanuser
USER loanuser

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
