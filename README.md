# **CQRS & Event Sourcing Project Template: Alert Management System**

This project serves as a production-ready template demonstrating an Alert Management System built with modern Java technologies and architectural patterns. It showcases the implementation of Domain-Driven Design (DDD), Command Query Responsibility Segregation (CQRS), Event Sourcing, and related concepts using Spring Boot and Axon Framework.

The primary goal is to provide a well-structured, scalable, and maintainable foundation for building event-driven microservices or applications.

## **Table of Contents**

1. [Purpose of the Project](#bookmark=id.c1kx9jnqxvz)  
2. [Core Architectural Concepts](#bookmark=id.m2rjenhm7054)  
   * [Domain-Driven Design (DDD)](#bookmark=id.tn82fli0vrh1)  
   * [CQRS (Command Query Responsibility Segregation)](#bookmark=id.gl4vl6swm16q)  
   * [Event Sourcing (ES)](#bookmark=id.trkwfh1cvxi6)  
   * [Sagas](#bookmark=id.7p4jthtpibwm)  
3. [System Architecture Overview](#bookmark=id.3q0w1wp6a6sr)  
   * [Diagram](#bookmark=id.l3hnw6xzxqfv)  
   * [Components](#bookmark=id.wrw3vuz9lkef)  
4. [Technology Stack](#bookmark=id.i3uhdhfup9ho)  
5. [Project Structure](#bookmark=id.go88dku1ya4c)  
6. [Setup and Run](#bookmark=id.rvw7puhzqeou)  
   * [Prerequisites](#bookmark=id.87v2cx1l1aq0)  
   * [Local Setup with Docker Compose](#bookmark=id.suyyx19697h)  
   * [Building the Application](#bookmark=id.vnuptneetuoo)  
   * [Running the Application](#bookmark=id.gxq51zu2tw6z)  
7. [Usage Examples](#bookmark=id.y0mp1wtntjd8)  
   * [Producing an Input Message to Kafka](#bookmark=id.85zytkybifeq)  
   * [Interacting via REST API](#bookmark=id.xc65ktbbc6tf)  
8. [API Documentation (Swagger/OpenAPI)](#bookmark=id.xmmb7nry79p2)  
9. [Testing](#bookmark=id.1v6l5lyqrj88)  
10. [Architectural Decision Records (ADRs)](#bookmark=id.2qhalmlvrdx6)  
11. [Business Documentation](#bookmark=id.apcad7ra2gr)  
12. [Configuration](#bookmark=id.n1js8njqtbft)

## **1. Purpose of the Project**

This template aims to:

* Provide a practical example of implementing CQRS and Event Sourcing with Axon Framework.  
* Demonstrate a DDD-aligned project structure.  
* Showcase integration with common enterprise technologies like Kafka, PostgreSQL, and Elasticsearch.  
* Offer a starting point for building robust, scalable, and event-driven systems.  
* Include best practices for configuration, testing, and documentation.  
* Illustrate how to separate write concerns (commands and events) from read concerns (queries), allowing for optimized data models and independent scaling.

The domain chosen for this template is a simple **Alert Management System**, which involves creating, updating, and querying alerts based on various inputs and criteria.

## **2. Core Architectural Concepts**

This project is built upon several key architectural patterns and principles:

### **2.1. Domain-Driven Design (DDD)**

DDD is an approach to software development that emphasizes a deep understanding of the business domain. It involves creating a rich domain model that reflects the business's language and processes.

* **Ubiquitous Language:** A common, rigorous language shared by developers and domain experts.  
* **Bounded Contexts:** Explicit boundaries within which a particular domain model is defined and applicable. This project primarily focuses on the "Alerting" bounded context.  
* **Aggregates:** A cluster of domain objects (entities and value objects) that can be treated as a single unit. An Aggregate has a root entity (Aggregate Root) and a defined boundary. All external references go to the Aggregate Root. Transactions and consistency rules are enforced within an Aggregate. In this project, AlertAggregate is the central aggregate.  
* **Entities:** Objects with a distinct identity that persists over time (e.g., an Alert).  
* **Value Objects:** Immutable objects defined by their attributes, not by a unique ID (e.g., AlertSeverity, AlertStatus).  
* **Repositories:** Provide an abstraction for accessing and persisting Aggregates (though in Event Sourcing, the Event Store often serves this role for loading aggregates).  
* **Domain Events:** Represent significant occurrences within the domain that have already happened. They are facts and are named in the past tense (e.g., AlertCreatedEvent).

**Advantages of DDD:**

* Improved communication between technical and business teams.  
* Software that more closely aligns with business needs.  
* More maintainable and adaptable systems due to clear boundaries and models.

### **2.2. CQRS (Command Query Responsibility Segregation)**

CQRS is an architectural pattern that separates the model for updating information (Commands) from the model for reading information (Queries).

* **Command Side:** Handles all operations that change state. Commands are imperative (e.g., CreateAlertCommand, AcknowledgeAlertCommand). They are processed by command handlers, which typically interact with aggregates to validate business rules and emit events.  
* **Query Side:** Handles all operations that retrieve state. Queries do not modify state. They are processed by query handlers that fetch data from optimized read models.  
* **Separation:** The command and query sides can use different data models, different persistence mechanisms, and can be scaled independently.

**Advantages of CQRS:**

* **Scalability:** Read and write workloads often have different characteristics. CQRS allows scaling each side independently. For example, you can have many read replicas without impacting the write database.  
* **Performance:** Read models can be highly optimized (e.g., denormalized) for specific query needs, leading to faster queries. Write models can be optimized for transactional consistency.  
* **Flexibility:** Different database technologies can be used for the write side (e.g., relational for event store) and read side (e.g., Elasticsearch for search, NoSQL for specific views).  
* **Simpler Models (per side):** Each model (command or query) becomes simpler because it only has one responsibility.  
* **Security:** Different security constraints can be applied to commands and queries.

### **2.3. Event Sourcing (ES)**

Event Sourcing is a persistence pattern where all changes to an application's state are stored as a sequence of immutable events. Instead of storing just the current state of an entity, you store every state change that has ever occurred.

* **Single Source of Truth:** The event log (Event Store) becomes the single source of truth.  
* **Rebuilding State:** The current state of an aggregate can be reconstructed at any time by replaying its events.  
* **Temporal Queries:** It's possible to determine the state of the system at any point in the past.  
* **Audit Trail:** Provides a complete and reliable audit log of all changes.  
* **Debugging:** Simplifies debugging as you can trace the exact sequence of events that led to a particular state.

**Advantages of Event Sourcing (often combined with CQRS):**

* **Full History:** Never lose data; all changes are preserved.  
* **Powerful Analytics:** Events can be used for business intelligence and to derive new insights.  
* **Flexibility in Projections:** New read models (projections) can be created by replaying events from the store, allowing the system to evolve its query capabilities without complex data migrations.  
* **Debugging and Auditing:** Provides a clear, immutable log of everything that happened.

### **2.4. Sagas**

Sagas are a way to manage long-lived transactions or processes that span multiple aggregates or bounded contexts, especially in distributed systems where distributed ACID transactions are not feasible or desirable. They coordinate a sequence of local transactions. If one local transaction fails, the saga executes compensating transactions to undo the preceding operations.

* **Orchestration vs. Choreography:** Sagas can be implemented using orchestration (a central coordinator) or choreography (each service listens to events and triggers further actions).  
* **Axon Framework Support:** Axon provides robust support for implementing Sagas.

**While this template does not explicitly implement a Saga, it's an important pattern in event-driven architectures that often complements CQRS and Event Sourcing when dealing with complex business processes.**

## **3. System Architecture Overview**

The system ingests alert-generating messages from Kafka. These messages are transformed into commands, which are handled by the AlertAggregate. The aggregate validates the command and, if successful, emits domain events. These events are persisted in an Event Store (PostgreSQL) and also published (potentially via Kafka) to update a denormalized read model in Elasticsearch. This read model is then queried via a REST API.


### **3.1. Components**

* **Kafka Input (alerts-input-topic):** Receives raw messages for alert creation.  
* **AlertInputKafkaConsumer:** Consumes from Kafka, transforms messages to CreateAlertCommand.  
* **Axon CommandGateway:** Dispatches commands.  
* **AlertAggregate:** Core domain logic, handles commands, emits events.  
* **Event Store (PostgreSQL):** Persists domain events using Axon's JPA entities. Also stores tracking tokens for event processors.  
* **Axon Events Kafka Topic (alerts-events-topic):** (Optional, configurable) Axon can publish domain events here for inter-component communication.  
* **AlertReadModel:** Event handling component (Tracking Event Processor) that consumes domain events and updates the Elasticsearch read model.  
* **Elasticsearch Read Model (alerts index):** Stores denormalized alert data for querying.  
* **AlertQueryHandler:** Handles query messages, fetches data from Elasticsearch.  
* **REST Controllers (AlertCommandController, AlertQueryController):** Expose HTTP endpoints for commands and queries.

## **4. Technology Stack**

* **Backend Framework:** Spring Boot 3.2.5  
* **Event Framework:** Axon Framework 4.9.3  
* **Java Version:** JDK 21  
* **Database (Write Model/Event Store):** PostgreSQL 16  
* **Database (Read Model):** Elasticsearch 8.11.3  
* **Event Transport (Input & Axon Events):** Apache Kafka (Bitnami image, KRaft mode)  
* **Build Tool:** Apache Maven 3.8.7  
* **API Documentation:** SpringDoc OpenAPI (Swagger UI)  
* **Containerization:** Docker, Docker Compose  
* **Testing:** JUnit 5, Testcontainers, Mockito, REST Assured (for API contract tests - not fully implemented in this template but recommended).

## **5. Setup and Run**

### **5.1. Prerequisites**

* Java JDK 21 or higher  
* Apache Maven 3.8.x or higher  
* Docker and Docker Compose  
* Git  
* jq (for the API test script - sudo apt-get install jq or brew install jq)

### **5.2. Local Setup with Docker Compose**

A docker-compose.yml file (ID: integrated_docker_compose_yml) is provided in the project root to set up the required infrastructure: PostgreSQL, Elasticsearch 8.11.3, and Kafka.

1. **Clone the repository:**  
   git clone <repository-url>  
   cd cqrs-template # Or your project's root directory

2. Start infrastructure using Docker Compose:  
   From the project root directory (containing docker-compose.yml):  
   docker-compose up -d

   This will start:  
   * PostgreSQL on port 5432 (service name postgres-db)  
   * Elasticsearch on port 9200 (service name elasticsearch-node)  
   * Kafka on port 9092 (service name kafka-broker)  
   * Kafka UI on port 8090 (service name kafka-ui)

### **5.3. Building the Application**

1. Using Maven from your terminal:  
   Navigate to the project's root directory (where pom.xml is) and run:  
```code
mvn clean install
```

   This will compile the code, run tests (if not skipped), and package the application into a JAR file in the target/ directory (e.g., target/cqrs-0.0.1-SNAPSHOT.jar).  
2. Building the Docker Image (if using the multi-stage Dockerfile):  
   The multi-stage Dockerfile (ID: alert_management_app_dockerfile) builds the JAR inside the Docker build process.  
   From the project root directory (containing the Dockerfile):  

```code
docker build -t template/cqrs-app .
```

   The docker-compose.yml also uses this Dockerfile to build the cqrs-template-app service.

### **5.4. Running the Application**

1. **Directly via Java (after mvn clean install):**

```code
java -jar target/cqrs-0.0.1-SNAPSHOT.jar
```

   Ensure your application.properties points to localhost for PostgreSQL, Elasticsearch, and Kafka if they are running directly on your host or via Docker port mappings. Your application.properties (ID alert_management_app_properties) uses localhost for Kafka and PostgreSQL, and 192.168.1.164:9200 for Elasticsearch. Adjust these if running services directly on host.  
2. Via Docker Compose (Recommended for local development):  

```code
docker compose up -d
```
   The command will build (if not already built) and run your application container (cqrs-template-app) along with all its dependencies. The application inside the container will use the service names defined in docker-compose.yml to connect (e.g., postgres-db, elasticsearch-node, kafka-broker). Your application will be accessible on http://localhost:7676.

## **6. Usage Examples**

### **6.1. Producing an Input Message to Kafka**

Messages to trigger alert creation should be sent to the alerts-input-topic Kafka topic.

**Example Message Format (JSON):**

```code
{  
  "messageId": "msg-$(uuidgen)",  
  "sourceSystem": "SystemX",  
  "severity": "CRITICAL",  
  "description": "Critical system failure detected in module Alpha.",  
  "timestamp": "$(date -u +"%Y-%m-%dT%H:%M:%SZ")",  
  "details": {  
    "host": "server-alpha-01",  
    "errorCode": "X500",  
    "component": "AuthService"  
  }  
}
```

How to send (using kafkacat or similar tool):  
Assuming Kafka is running and accessible on localhost:9092 from your host:  

```code
echo '{ "messageId": "kfk-msg-'$(uuidgen)'", "sourceSystem": "KafkaProducerTool", "severity": "HIGH", "description": "Test alert via kafkacat.", "timestamp": "'$(date -u +"%Y-%m-%dT%H:%M:%SZ")'", "details": { "tool": "kafkacat", "user": "$USER" }}' | kafkacat -P -b localhost:9092 -t alerts-input-topic
```

Check application logs for confirmation of message processing.

### **6.2. Interacting via REST API**

The application exposes REST endpoints on port 7676 (configurable).  
You can use the provided test_alerts_api.sh script (located in src/test/java/template/cqrs/docs/ as per your tree structure, or ID: api_test_script_sh if referring to the Canvas version) for a sequence of API calls.  
**Example: Create Alert via API**

```code
curl -X POST http://localhost:7676/api/v1/alerts \
-H "Content-Type: application/json" \
-d '{  
  "severity": "MEDIUM",  
  "description": "API Test: User login anomaly.",  
  "source": "APITestClient",  
  "details": {"ip_address": "192.168.1.100", "attempts": 5},  
  "eventTimestamp": "'$(date -u +"%Y-%m-%dT%H:%M:%SZ")'",  
  "initiatedBy": "api-tester"  
}'
```

The response will include the alertId of the newly created alert.

**Example: Get Alert by ID** (replace {alertId} with an actual ID)

```code
curl -X GET http://localhost:7676/api/v1/alerts/{alertId}
```

Refer to the AlertCommandController.java and AlertQueryController.java for all available endpoints and their request/response structures.

## **7. API Documentation (Swagger/OpenAPI)**

Once the application is running, API documentation is available via Swagger UI:

* **Swagger UI:** [http://localhost:7676/swagger-ui.html](http://localhost:7676/swagger-ui.html)  
* **OpenAPI Spec (JSON):** [http://localhost:7676/api-docs](http://localhost:7676/api-docs)

(Ensure springdoc.packagesToScan=template.cqrs.api.controller or springdoc.packagesToScan=template.cqrs in application.properties correctly points to your controller package or a base package that includes it.)