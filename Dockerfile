# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

# Optimized JVM memory parameters for Render free tier (512MB RAM limit)
# SerialGC reduces JVM metadata overhead; MaxMetaspaceSize prevents memory growth causing SIGKILL (OOM)
EXPOSE 8080
CMD ["java", "-Xmx224m", "-Xms128m", "-XX:MaxMetaspaceSize=128m", "-XX:+UseSerialGC", "-jar", "app.jar"]
