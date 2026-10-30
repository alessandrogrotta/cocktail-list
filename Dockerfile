# Build stage - Compila l'applicazione con Maven
FROM maven:3.9-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Copia i file di configurazione
COPY pom.xml .
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn

# Scarica le dipendenze
RUN mvn dependency:go-offline -B 2>&1 || true

# Copia il codice sorgente
COPY src ./src

# Compila l'applicazione
RUN mvn clean package -DskipTests

# Runtime stage - Esegue l'applicazione
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Installa curl per health check
RUN apk add --no-cache curl

# Copia il JAR dal build stage
COPY --from=build /app/target/*.jar app.jar

# Verifica che il JAR esista
RUN test -f app.jar || (echo "ERROR: app.jar not found!" && exit 1)

EXPOSE 8080

# Comando di avvio
CMD ["java", "-jar", "app.jar"]
