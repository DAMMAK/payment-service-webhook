# Payment Service Webhook

A robust, scalable webhook service for processing payment events from external providers like Stripe, PayPal, and Razorpay. This service ensures secure, reliable delivery of webhook events to merchant endpoints with built-in retry mechanisms and duplicate event prevention.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
  - [Clone the Repository](#clone-the-repository)
  - [Environment Setup](#environment-setup)
  - [Configuration](#configuration)
  - [Running the Application](#running-the-application)
- [API Documentation](#api-documentation)
- [Database Schema](#database-schema)
- [Testing](#testing)
- [Deployment](#deployment)
- [Monitoring](#monitoring)
- [Contributing](#contributing)
- [License](#license)

## Overview

The Payment Service Webhook is a backend service designed to manage and process webhook events from external payment providers. It enables secure, reliable, and asynchronous communication between payment systems and merchants by delivering event notifications (e.g., payment success, refund, etc.) to registered endpoints.

## Features

- **Secure Webhook Reception**: Accepts incoming webhook events from various payment providers with signature verification
- **Event Deduplication**: Prevents duplicate event processing using Redis-based idempotency keys
- **Reliable Delivery**: Ensures webhook events are delivered to merchant endpoints with retry mechanisms
- **Circuit Breaker Pattern**: Uses Resilience4j to avoid cascading failures during delivery issues
- **Asynchronous Processing**: Leverages Kafka for decoupling event processing from webhook reception
- **RESTful API Management**: Provides endpoints for managing webhook endpoints and subscriptions
- **Authentication & Authorization**: Secures endpoints with JWT and Spring Security
- **Monitoring & Observability**: Exposes metrics via Actuator and Prometheus integration
- **Swagger Documentation**: Interactive API documentation with OpenAPI/Swagger UI

## Architecture

The system follows a **modular monolith** architecture built on **Spring Boot**, with clear separation of concerns using domain-driven design (DDD) principles. It integrates event-driven components via **Kafka** and uses **asynchronous processing** for scalability.

```
┌─────────────────┐    ┌──────────────────┐    ┌──────────────────┐
│  Payment        │    │  Payment         │    │  Payment         │
│  Provider       │    │  Provider        │    │  Provider        │
│  (Stripe)       │    │  (PayPal)        │    │  (Razorpay)      │
└─────────┬───────┘    └─────────┬────────┘    └─────────┬────────┘
          │                      │                       │
          └──────────────────────┼───────────────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │ ProviderWebhookController│
                    │ (Receives webhooks)     │
                    └────────────┬────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │ WebhookService          │
                    │ (Validates & stores)    │
                    └────────────┬────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │ EventProcessingService  │
                    │ (Publishes to Kafka)    │
                    └────────────┬────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │ Kafka                   │
                    │ (Event streaming)       │
                    └────────────┬────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │ DeliveryService         │
                    │ (Delivers to merchants) │
                    └────────────┬────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │ WebhookEndpoint         │
                    │ (Merchant endpoint)     │
                    └─────────────────────────┘
```

## Technology Stack

- **Java 17**
- **Spring Boot 3.5.5**
- **Spring Modules**: Web, Data JPA, Security, Kafka, Actuator, AOP, Validation, Redis
- **Database**: PostgreSQL (runtime)
- **ORM**: Hibernate (via Spring Data JPA)
- **Migration Tool**: Flyway 10.15.0
- **Build Tool**: Maven
- **API Documentation**: Springdoc OpenAPI (Swagger UI)
- **Monitoring**: Micrometer + Prometheus
- **Fault Tolerance**: Resilience4j 2.1.0
- **Mapping**: MapStruct 1.5.5.Final
- **Utilities**: Lombok, JWT (jjwt-api 0.12.3)
- **Testing**: JUnit, Testcontainers, REST Assured

## Prerequisites

- JDK 17
- Maven 3.6+
- Docker (for containerization and dependencies)
- PostgreSQL
- Redis
- Kafka

## Getting Started

### Clone the Repository

```bash
git clone https://github.com/your-username/payment-service-webhook.git
cd payment-service-webhook
```

### Environment Setup

The easiest way to set up the development environment is using Docker Compose, which will start all required services:

```bash
docker-compose up -d
```

This will start:
- PostgreSQL database
- Redis
- Kafka with Zookeeper
- The application itself

### Configuration

The application is configured through `application.yml`. Key configuration options include:

- Database connection settings
- Redis connection settings
- Kafka bootstrap servers
- Security settings (JWT issuer URI)
- Webhook retry policies
- Delivery settings (timeout, thread pool size)

Environment variables can be used to override configuration values:

- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`
- `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`
- `KAFKA_BOOTSTRAP_SERVERS`
- `JWT_ISSUER_URI`

### Running the Application

#### Using Maven

```bash
# Build the project
./mvnw clean install

# Run the application
./mvnw spring-boot:run
```

#### Using Docker

```bash
# Build the Docker image
docker build -t payment-service-webhook .

# Run the container
docker run -p 8080:8080 payment-service-webhook
```

#### Using Docker Compose (Recommended for development)

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f
```

## API Documentation

The service provides interactive API documentation via Swagger UI:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- API Docs (JSON): `http://localhost:8080/v3/api-docs`

### Core API Endpoints

#### Provider Webhooks
- `POST /api/v1/webhooks/providers/{provider}/events` - Receive webhook from payment providers

#### Webhook Endpoint Management
- `POST /api/v1/webhooks/endpoints` - Create a new webhook endpoint
- `GET /api/v1/webhooks/endpoints/{id}` - Get webhook endpoint by ID
- `PUT /api/v1/webhooks/endpoints/{id}` - Update webhook endpoint
- `DELETE /api/v1/webhooks/endpoints/{id}` - Delete webhook endpoint
- `GET /api/v1/webhooks/endpoints` - List webhook endpoints
- `POST /api/v1/webhooks/endpoints/{id}/test` - Test webhook endpoint

All endpoint management APIs require authentication with a valid JWT token and merchant ID in the header.

## Database Schema

The service uses PostgreSQL with the following core tables:

1. **merchants** - Stores merchant information
2. **webhook_endpoints** - Configured webhook endpoints for merchants
3. **webhook_events** - Received webhook events from providers
4. **webhook_deliveries** - Delivery attempts to merchant endpoints
5. **event_subscriptions** - Event type subscriptions for endpoints
6. **merchant_ip_whitelist** - IP whitelist for merchants

Database migrations are managed with Flyway and can be found in `src/main/resources/db/migration/`.

## Testing

The project includes comprehensive unit and integration tests:

```bash
# Run all tests
./mvnw test

# Run tests with coverage
./mvnw verify
```

Testing components:
- Unit tests for services, controllers, and repositories
- Integration tests for API endpoints
- Repository tests using H2 in-memory database
- Test data builders for consistent test data

## Deployment

### Build for Production

```bash
# Build the JAR file
./mvnw clean package -DskipTests
```

### Docker Deployment

```bash
# Build the Docker image
docker build -t payment-service-webhook .

# Run with custom environment variables
docker run -d \
  --name payment-webhook \
  -p 8080:8080 \
  -e DB_HOST=your-db-host \
  -e REDIS_HOST=your-redis-host \
  -e KAFKA_BOOTSTRAP_SERVERS=your-kafka-servers \
  payment-service-webhook
```

### Kubernetes Deployment

The service includes comprehensive Kubernetes manifests for deployment. See the [k8s directory](k8s/) for detailed deployment instructions.

Key components include:

- Deployment with the application container
- Service for internal access
- ConfigMaps for configuration
- Secrets for sensitive data
- Ingress for external access
- Separate manifests for dependencies (PostgreSQL, Redis, Kafka)

See [k8s/README.md](k8s/README.md) for detailed deployment instructions.

## Monitoring

The service exposes several endpoints for monitoring:

- Health Check: `http://localhost:8080/actuator/health`
- Metrics: `http://localhost:8080/actuator/metrics`
- Prometheus: `http://localhost:8080/actuator/prometheus`

Key metrics include:
- Webhook processing rates
- Delivery success/failure rates
- Latency distributions
- Error rates
- Resource utilization

## GitHub Actions

This repository includes GitHub Actions for continuous integration and container image building:

1. **CI Workflow** (`ci.yml`): Runs tests on every pull request and push to main branch
2. **Docker Build and Push** (`docker-build-push.yml`): Builds and pushes Docker images to GitHub Container Registry on release creation
3. **Docker Hub Push** (`docker-hub-build-push.yml`): Alternative workflow to push images to Docker Hub

The workflows automatically trigger on:
- Pushes to the main branch (excluding documentation changes)
- Creation of new releases
- Manual triggering

### Secrets Required

For Docker Hub deployment, you'll need to set the following secrets in your repository:
- `DOCKERHUB_USERNAME`: Your Docker Hub username
- `DOCKERHUB_TOKEN`: Your Docker Hub access token

### Container Images

Container images are automatically built and tagged with:
- Branch name for branch builds
- Semantic version for releases
- Commit SHA for development builds

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a pull request

Please ensure your code follows the project's coding standards and includes appropriate tests.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.