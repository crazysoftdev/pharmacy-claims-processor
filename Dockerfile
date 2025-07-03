# --- Stage 1: The Build Stage ---

FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .

COPY src ./src

# - Skip unit tests during the Docker build process
RUN mvn clean package -DskipTests


# --- Stage 2: The Runtime Stage ---

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy only the .jar file from the "build" stage into new stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT [ "java", "-jar", "app.jar" ]