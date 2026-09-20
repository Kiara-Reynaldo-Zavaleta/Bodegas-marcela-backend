# ── Etapa 1: compilación ──────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copiar solo el descriptor primero para aprovechar la caché de capas de Docker:
# si src/ cambia pero pom.xml no, Maven no re-descarga dependencias.
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B 2>/dev/null || true

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests -B

# ── Etapa 2: imagen de producción ─────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/Bodegas_Marcela-1.0-SNAPSHOT.jar app.jar

# Render inyecta PORT en tiempo de ejecución; Spring lo lee como ${PORT:8080}
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
