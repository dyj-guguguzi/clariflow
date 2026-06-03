# ── Stage 1: Build ──
FROM maven:3.8-openjdk-8-slim AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline -B 2>/dev/null || true
COPY src/ ./src/
RUN mvn clean package -DskipTests -B

# ── Stage 2: Run ──
FROM openjdk:8-jre-slim
WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar

# H2 data volume
RUN mkdir -p /app/data
VOLUME /app/data

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget -qO- http://localhost:8080/api/work-items >/dev/null 2>&1 || exit 1

ENTRYPOINT ["java", "-jar", "app.jar", \
  "--spring.datasource.url=jdbc:h2:file:/app/data/clariflow;DB_CLOSE_DELAY=-1;MODE=MySQL"]
