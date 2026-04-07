# Flosek Backend API

A production-oriented Spring Boot backend for personal finance management.

Flosek helps users track expenses and income, manage budgets, set savings goals, and generate insightful financial reports. The API is secured with JWT and supports Google Sign-In.

## Table of Contents

- [Overview](#overview)
- [Core Features](#core-features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Environment Variables](#environment-variables)
- [Run Locally](#run-locally)
- [Run with Docker Compose](#run-with-docker-compose)
- [API Overview](#api-overview)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Build](#build)
- [Health Checks](#health-checks)
- [Security Notes](#security-notes)

## Overview

Flosek is a RESTful backend designed for modern finance applications. It provides modular domains for expenses, budgets, salaries, savings goals, categories, reports, dashboard analytics, notifications, and user profile management.

The codebase follows standard Spring layering:

- `controller` for REST endpoints
- `service` for business logic
- `repository` for data access
- `dto` + `mapper` for API contracts and model mapping
- `security` and `config` for authentication and application setup

## Core Features

- User authentication with JWT
- Google Sign-In authentication flow
- Expense and category management
- Budget planning and recurring budget renewal scheduler
- Salary tracking
- Savings goals and contribution tracking
- Financial reports (monthly, yearly, quarterly, custom)
- PDF export support for reports
- Dashboard endpoints for aggregated finance insights
- User profile and password management
- Admin-only endpoints with role-based access
- OpenAPI/Swagger UI and Actuator health endpoints

## Tech Stack

- Java 17
- Spring Boot 3.2.x
- Spring Web
- Spring Security
- Spring Data JPA (Hibernate)
- PostgreSQL (primary), H2 (optional runtime)
- MapStruct + Lombok
- JWT (`io.jsonwebtoken`)
- Google API Client (Google ID token verification)
- SpringDoc OpenAPI (Swagger UI)
- Apache POI + iText (report exports)
- Maven
- Docker + Docker Compose

## Project Structure

```text
src/main/java/com/flosek/flosek
|- config/
|- controller/
|- dto/
|- entity/
|- enums/
|- exception/
|- mapper/
|- repository/
|- scheduler/
|- security/
|- service/
`- util/
```

## Prerequisites

- Java 17+
- Maven 3.9+
- Docker + Docker Compose (optional, for containerized run)

## Environment Variables

The application loads configuration from `.env` using:

- `spring.config.import=optional:file:.env[.properties]`

Create a `.env` file in the project root (you can start from `.env.example`).

Minimum required values:

```env
SERVER_PORT=8081
DB_URL=jdbc:postgresql://<host>:<port>/<database>
DB_USERNAME=<db_user>
DB_PASSWORD=<db_password>
JWT_SECRET=<strong_random_secret>
JWT_EXPIRATION=86400000
GOOGLE_CLIENT_ID=<google_oauth_client_id>
```

Optional tuning values include logging and datasource pool parameters (see `src/main/resources/application.properties`).

## Run Locally

1. Install dependencies and compile:

```bash
./mvnw clean install
```

On Windows PowerShell:

```powershell
.\mvnw.cmd clean install
```

2. Start the application:

```bash
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

By default, the API runs on `http://localhost:8081`.

## Run with Docker Compose

```bash
docker compose up --build
```

This starts:

- `mysql` service
- `backend` service

Useful commands:

```bash
docker compose ps
docker compose logs -f backend
docker compose down
```

## API Overview

Base URL:

- `http://localhost:8081`

Main route groups:

- `/api/auth` - register, login, Google login
- `/api/expenses` - expense CRUD and filters
- `/api/categories` - category CRUD
- `/api/budgets` - budget CRUD and active budgets
- `/api/salaries` - salary management
- `/api/savings-goals` - savings goals and contributions
- `/api/reports` - financial report generation and summaries
- `/api/dashboard` - dashboard aggregated metrics
- `/api/users` - profile and password operations
- `/api/admin` - admin-only operations

## API Documentation

When the app is running:

- Swagger UI: `http://localhost:8081/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8081/v3/api-docs`

## Testing

Run all tests:

```bash
./mvnw test
```

On Windows PowerShell:

```powershell
.\mvnw.cmd test
```

## Build

Create a JAR artifact:

```bash
./mvnw clean package
```

The artifact is generated under `target/`.

## Health Checks

- Actuator Health: `http://localhost:8081/actuator/health`

## Render Deployment Notes

If you deploy on Render and notice the first request after inactivity takes 30-60 seconds, this is usually a combination of:

- Render web service cold start after idle
- PostgreSQL instance cold wake-up (depending on your DB provider/plan)

This project includes fail-fast datasource defaults for cloud environments. You can tune them with environment variables:

```env
DB_POOL_CONNECTION_TIMEOUT_MS=10000
DB_POOL_VALIDATION_TIMEOUT_MS=5000
DB_POOL_IDLE_TIMEOUT_MS=300000
DB_POOL_MAX_LIFETIME_MS=900000
DB_POOL_KEEPALIVE_MS=120000
DB_CONNECT_TIMEOUT_SEC=5
DB_SOCKET_TIMEOUT_SEC=30
```

To reduce cold starts further in production:

- Use an always-on Render plan for the web service.
- Use a database plan that does not sleep aggressively.
- Optionally ping `/actuator/health` every 5 minutes from an external uptime monitor.

### No-Upgrade Keep-Alive Option

If you do not want to upgrade plans, this repository includes a GitHub Actions workflow at `.github/workflows/render-keep-alive.yml`.

It sends a request every 5 minutes to your health endpoint to reduce idle cold starts.

Setup steps:

1. Open your GitHub repository settings.
2. Go to Secrets and variables > Actions.
3. Add a new repository secret named `RENDER_HEALTHCHECK_URL`.
4. Set it to your public health URL, for example:
	`https://your-service-name.onrender.com/actuator/health`
5. Ensure the workflow is enabled in the Actions tab.

Notes:

- If your health endpoint requires authentication, create a public lightweight ping endpoint.
- This reduces cold starts significantly, but cannot fully eliminate delays if your database provider force-sleeps very aggressively.

## Security Notes

- Never commit your `.env` file.
- Always use a strong random `JWT_SECRET` in production.
- Restrict CORS origins in production environments.
- Rotate credentials and tokens regularly.
