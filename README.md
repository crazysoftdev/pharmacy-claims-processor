# Pharmacy Claims Processor

This project is a real-world coding challenge to demonstrate the design and implementation of a scalable, production-grade pharmacy claims processing system using Java and Spring Boot.

## Overview

The application is a microservice designed to run on Kubernetes. It performs the following actions:
- **Consumes** pharmacy claims from a Kafka topic.
- **Applies** business validation rules.
- **Stores** the claim status in a PostgreSQL database.
- **Handles** failures with a configurable retry mechanism.
- **Publishes** successfully processed claims to an internal Kafka topic.
- **Generates** periodic reports.

## Technical Stack
- **Java 21**
- **Spring Boot 3.3.0**
- **Maven** for build management
- **Spring Data JPA** with PostgreSQL
- **Spring for Apache Kafka** for messaging
- **Spring Scheduling** for reports
- **Docker** for containerization
- **Kubernetes** for deployment orchestration
- **JUnit 5 & Mockito** for testing

---

## Build and Run Instructions

### Prerequisites
- Java 21
- Docker Desktop (with Kubernetes enabled)
- A command-line tool like Terminal or PowerShell
- Git

### 1. Clone the Repository
```sh
git clone https://github.com/YourUsername/pharmacy-claims-processor.git
cd pharmacy-claims-processor
```

### 2. Start Local Infrastructure
This command will start PostgreSQL and Kafka containers using Docker Compose.
```sh
docker-compose up -d
```

### 3. Build the Application
This command uses Maven to compile the code and package it into a runnable `.jar` file.
```sh
mvn clean package
```

### 4. Run the Application Locally
You can now run the application directly. It will connect to the services running in Docker.
```sh
java -jar target/claims-processor-0.0.1-SNAPSHOT.jar
```
The application will be running and listening for messages on the `claims-topic`.

---

## How to Test the Application

You can send a sample claim message to the `claims-topic` using a Kafka command-line tool.

1. **Start a shell inside the Kafka container:**
   ```sh
   docker exec -it kafka_broker /bin/bash
   ```

2. **Inside the container, start a Kafka console producer:**
   ```sh
   kafka-console-producer --broker-list localhost:9092 --topic claims-topic
   ```

3. **Paste the following JSON message and press Enter:**
   ```json
   {"patientId":"PAT123","pharmacyId":"PHARM456","insurancePolicyNumber":"POLICY789","claimCost":150.75,"insuranceCoverage":120.50}
   ```
   You should see logs in your running Spring Boot application indicating that the claim was received and processed.

---

## Docker & Kubernetes Deployment

### 1. Build the Docker Image
```sh
docker build -t claims-processor:1.0 .
```

### 2. Deploy to Kubernetes
Apply the Kubernetes manifests to your local cluster.
```sh
kubectl apply -f k8s/
```

### 3. Check the Deployment Status
```sh
kubectl get deployments
kubectl get pods
kubectl get services
```
The `claims-processor-service` should be available on `localhost:8080`.

### 4. Clean Up
```sh
kubectl delete -f k8s/
```