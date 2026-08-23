# System Architecture — Agro Linken

## Overview

Agro Linken uses a **split-client architecture**: a standalone Java Spring Boot REST API backend and a dependency-free Vanilla HTML/CSS/JS frontend hosted separately.

```
                        ┌─────────────────────────────────────┐
                        │           Client Browser             │
                        │                                      │
                        │   HTML5 / CSS3 / JS Frontend         │
                        │   (Firebase Hosting)                 │
                        │                                      │
                        │   ┌──────────┐  ┌────────────────┐  │
                        │   │  api.js  │  │ Firebase Web   │  │
                        │   │ (Client) │  │    SDK (Auth)  │  │
                        │   └────┬─────┘  └───────┬────────┘  │
                        └────────┼────────────────┼───────────┘
                                 │                │
                    Bearer JWT   │                │  Auth Requests
                    (REST HTTP)  │                │
                                 ▼                ▼
             ┌──────────────────────────┐   ┌────────────────┐
             │   Spring Boot Backend    │   │    Firebase     │
             │   (Render / Docker)      │◄──│  Auth Console  │
             │                          │   └────────────────┘
             │  ┌─────────────────────┐ │
             │  │   REST Controllers  │ │
             │  │  (10 endpoints)     │ │
             │  └──────────┬──────────┘ │
             │             │            │
             │  ┌──────────▼──────────┐ │
             │  │  WebSocket / STOMP  │ │   Real-time chat
             │  │  (/ws-chat)         │◄├──────────────────────
             │  └──────────┬──────────┘ │
             │             │            │
             │  ┌──────────▼──────────┐ │
             │  │   JPA Repositories  │ │
             │  └──────────┬──────────┘ │
             └─────────────┼────────────┘
                           │
                           ▼
                ┌──────────────────────┐
                │  PostgreSQL Database │
                │  (Render / Neon)     │
                └──────────────────────┘
```

---

## Component Responsibilities

### Frontend (`frontend/`)
| File | Purpose |
|---|---|
| `index.html` | Landing page, Firebase login (Email/Password + Google Sign-In) |
| `dashboard.html` | Main hub: orders, credit ledger, notifications, transporter bidding |
| `marketplace.html` | Browse & search products, place orders, crop subscriptions |
| `community.html` | Community forum — post farming tips, ask questions |
| `chat.html` | Real-time WebSocket peer-to-peer messaging |
| `schemes.html` | Government agriculture scheme browser |
| `scripts/api.js` | Centralized API client — all `fetch()` calls live here |
| `styles/main.css` | Global dark-mode design system |

### Backend (`src/main/java/com/agrosystem/`)
| Package | Purpose |
|---|---|
| `controller/` | 10 REST controllers exposing all API endpoints |
| `model/` | 11 JPA entity classes (database table definitions) |
| `repository/` | 11 Spring Data JPA repository interfaces |
| `config/` | Cross-cutting concerns: CORS, WebSocket, Firebase init, data seeding |

### Database
PostgreSQL relational database managed by Hibernate (`ddl-auto=update`). Schema is auto-generated from JPA entity annotations.

---

## Authentication Flow

```
1. User clicks "Login" in the browser
2. Firebase Web SDK handles credential verification (Email/Google)
3. Firebase returns a short-lived JWT (ID Token)
4. Frontend sends JWT in Authorization: Bearer <token> header on every API call
5. Spring Boot backend verifies the JWT via Firebase Admin SDK
6. On success, the request is processed; on failure, HTTP 401 is returned
```

---

## Database Entity Relationship

```
User (1) ─────────────── (N) Product
User (1) ─────────────── (N) Order (as buyer)
User (1) ─────────────── (N) Order (as transporter)
User (1) ─────────────── (N) CreditRecord
User (1) ─────────────── (N) CommunityPost
User (1) ─────────────── (N) Message
User (1) ─────────────── (N) CropSubscription
User (1) ─────────────── (N) Notification

Product (1) ────────────── (N) Order
Product (1) ────────────── (N) Review

Order (1) ──────────────── (N) DeliveryBid

GovernmentScheme            (standalone — seeded data)
```

---

## Deployment Architecture

```
Frontend  ──►  Firebase Hosting  (CDN, global edge network)
Backend   ──►  Render            (Docker container, auto-deploy from GitHub)
Database  ──►  PostgreSQL        (Render managed DB or Neon.tech)
Auth      ──►  Firebase Auth     (managed by Google)
```

### Docker Build (Backend)
The `Dockerfile` at the project root builds the Spring Boot JAR using the Maven wrapper inside the container:
```dockerfile
FROM maven:3.9.6-eclipse-temurin-17
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests
CMD ["sh", "-c", "java -Xmx350m -Xms256m -jar target/*.jar"]
```
JVM flags `-Xmx350m` are tuned for Render's free-tier 512 MB RAM limit.
