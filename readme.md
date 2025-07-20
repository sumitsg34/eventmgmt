# Event Management Service

This project is a **Spring Boot-based microservice** to manage live sports events. It:

- Accepts REST requests to update the live status of an event.
- Schedules periodic tasks to fetch event scores from an external API.
- Publishes those scores to a Kafka topic (`event-score-processor`).
- Includes comprehensive JUnit 5 test coverage.

---

## Tech Stack

- Java 17
- Spring Boot
- Apache Kafka (via Docker)
- KafkaTemplate (Producer)
- RestTemplate
- JUnit 5 + Mockito

---

## 🛠️ Setup & Run Instructions

### 1. Prerequisites

- Java 17 installed
- Docker installed
- Maven

---

### 2. Kafka Setup Using Docker

We use the [apache/kafka](https://hub.docker.com/r/apache/kafka) Docker image.

#### a. Pull and Run the Kafka Docker image

```bash
docker run -p 9092:9092 -d --name broker apache/kafka:latest
```

#### b. Create Kafka Topic

Once Kafka is running:

```bash
docker exec -it broker /opt/kafka/bin/kafka-topics.sh --create \
  --topic event-score-processor \
  --bootstrap-server localhost:9092 \
  --replication-factor 1 \
  --partitions 10
```

---

### 3. Build & Run Spring Boot Project

#### a. Using Maven

```bash
./mvnw clean install
./mvnw spring-boot:run
```

Application will start at: [http://localhost:8080/event-mgmt](http://localhost:8080/event-mgmt)

---

### 4. Run JUnit Tests

#### a. Maven

```bash
./mvnw test
```
---

## 📬 API Reference

### `PUT /event-mgmt/events`

Updates the live status of an event.

**Request Body:**

```json
{
  "eventId": 123,
  "live": true
}
```

**Responses:**

- `200 OK` – Status updated
- `400 Bad Request` – Invalid event input
- `500 Internal Server Error` – Unhandled error

---

### `GET /event-mgmt/event-score/{eventId}`

Mock endpoint to simulate score for a given event.

**Response:**

```json
{
  "eventId": 123,
  "score": 532.1
}
```

---

## 🧠 Design Decisions Summary

### 1. Service Responsibility

- `EventManagementService`: Manages live event state and task scheduling.
- `EventScoreProcessor`: Fetches scores from an external API and pushes to Kafka.

### 2. Scheduling

- Used `ScheduledThreadPoolExecutor` to control scheduling and cancellation.
- Tracked each scheduled task via a `ConcurrentHashMap<Integer, ScheduledFuture>` to allow dynamic cancellation.

### 3. Retry Logic

- Score fetch uses 3 retries with exponential backoff for transient failures.

### 4. Kafka

- Used `KafkaTemplate<Integer, EventScore>` to publish scores to a single topic: `event-score-processor`.

### 5. Separation of Concerns

- Controller is kept lean and delegates logic to the service layer.
- `EventScoreController` mock endpoint is separated for simulation purposes.

---

## 🤖 AI-Assisted Development Documentation

### 🔹 JUnit Test Generation

> **Tool Used:** ChatGPT (OpenAI)\
> **What was generated:** Unit tests for `EventManagementServiceImpl`, `EventScoreProcessor`, and controller classes.\
> **Post-processing Done:**
>
> - Verified mock interactions.
> - Verified edge cases like retries, nulls, and exceptions.
> - Ensured no overuse of `any()` operator—used specific test data instead.

### 🔹 Code Documentation

> **Tool Used:** ChatGPT\
> **What was generated:** JavaDoc-style inline comments for classes and methods.\
> **Post-processing Done:**
>
> - Checked all parameter types and descriptions.
> - Ensured class-level and method-level context is clearly conveyed.

### 🔹 README File

> **Tool Used:** ChatGPT\
> **What was generated:** This README content.\
> **Post-processing Done:**
>
> - Manually tested every Docker/Kafka instruction.
> - Verified Spring Boot run commands.
> - Validated Kafka topic creation and JUnit execution steps.
> - Ensured completeness and clarity for team onboarding.



---

## 📁 Project Structure

```
src/
 └── main/
     ├── java/
     │   └── com.sportygroup.eventmgmt
     │       ├── controller/
     │       │   ├── EventController.java
     │       │   └── EventScoreController.java
     │       ├── model/
     │       │   ├── Event.java, EventScore.java
     │       ├── processor/
     │       │   └── EventScoreProcessor.java
     │       └── service/
     │           ├── EventManagementService.java
     │           └── impl/EventManagementServiceImpl.java
     └── test/
         └── (JUnit 5 tests for all components)
```

---

## 📊 Test Coverage

- `EventManagementServiceImplTest` – Event scheduling, canceling, and validations
- `EventScoreProcessorTest` – Retry logic, API failures, Kafka publishing
- `EventControllerTest` – Happy path, validation, internal errors
- `EventScoreControllerTest` – Score generation and response integrity

---

## 📌 Notes

- The score generator is mock-only. If needed, the URL can be changed in `application.properties` for testing. Variable name: api.event-score.url
- Kafka configuration is added in `application.properties`
- For simplicity, this service uses a ConcurrentHashMap to track active events. In a real-world scenario with multiple instances of this service, a distributed cache solution like Redis would be preferred. 
- This service uses a ScheduledThreadPoolExecutor to schedule tasks. In a real-world scenario with multiple instances, a distributed task scheduler like Quartz would be preferred.
- 

---