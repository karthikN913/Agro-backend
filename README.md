# Agro Linken

Agro Linken is a digital agricultural marketplace and supply chain web application designed to connect farmers directly with produce buyers, shop owners, and transport providers. 

The application facilitates direct listing and ordering of agricultural produce, competitive delivery bidding for transporters, real-time messaging, a digital credit ledger for rural credit management, and informational access to government agricultural schemes.

---

## Features

### Implemented Features
- **User Authentication & Role Management**: Firebase Authentication (Email/Password and Google Sign-In) integrated with role-based access for Farmers, Buyers, Shop Owners, Transporters, and Admins.
- **Product Marketplace**: Product listing, categorization, price setting, stock tracking, and multi-field search (query, category, location, price range).
- **Order Management & Tracking**: End-to-end order status tracking (`PENDING` → `ACCEPTED` → `SHIPPED` → `DELIVERED`).
- **Delivery Bidding System**: Transporters browse available orders, submit freight quotes, and get assigned to shipments.
- **Real-Time WebSocket Chat**: Peer-to-peer messaging using STOMP over WebSockets (`ws-chat`), persisted to the relational database.
- **Digital Credit Ledger (Udhar Book)**: Recording and tracking customer credit purchases, repayments, and outstanding balances.
- **Crop Alert Subscriptions**: User subscriptions to specific produce categories with automated notification generation upon matching listings.
- **Product Reviews & Ratings**: Buyer review submissions and aggregated product ratings.
- **Government Schemes**: Catalog of government agricultural schemes and eligibility details.
- **Community Forum**: Discussion board for farming tips and updates.
- **Tax Invoice Generation**: Client-side PDF generation for order receipts.

### Planned / In-Progress Features
- Dedicated Service Layer refactoring for business logic encapsulation.
- Automated unit and integration testing suite (JUnit 5 + MockMvc + Testcontainers).
- Online payment gateway integration (Razorpay / UPI).
- File upload integration for product images (Firebase Storage).
- GitHub Actions CI/CD workflow for automated test execution and deployment.

---

## Tech Stack

### Backend
- **Language**: Java 17
- **Framework**: Spring Boot 3.2.4 (Spring Web, Spring Data JPA, Spring WebSocket)
- **Database**: PostgreSQL 15
- **Security & Auth**: Firebase Admin SDK 9.2 (Server-side JWT token verification)
- **Build Tool**: Maven

### Frontend
- **Core**: Vanilla HTML5, CSS3, JavaScript (ES6+)
- **Authentication**: Firebase Web SDK 10.8
- **Real-Time Messaging**: STOMP.js & SockJS
- **Document Generation**: jsPDF

### Infrastructure & Deployment
- **Backend Hosting**: Render (Docker containerized)
- **Frontend Hosting**: Firebase Hosting (CDN)
- **Database**: Render PostgreSQL Managed Instance

---

## Architecture

```
┌────────────────────────────────────────────────────────┐
│                    Client Browser                      │
│        (HTML5 / Vanilla JS / Firebase Web SDK)         │
└───────────────────────────┬────────────────────────────┘
                            │
              REST (HTTP)   │   WebSocket (STOMP)
              Bearer JWT    │   /ws-chat
                            ▼
┌────────────────────────────────────────────────────────┐
│                 Spring Boot Backend                    │
│            (Render / Docker Container)                 │
│                                                        │
│  - REST Controllers (Users, Products, Orders, etc.)    │
│  - Firebase Admin SDK (JWT Validation)                 │
│  - Spring Data JPA Repositories                        │
└───────────────────────────┬────────────────────────────┘
                            │ SQL / HikariCP
                            ▼
┌────────────────────────────────────────────────────────┐
│                   PostgreSQL Database                  │
└────────────────────────────────────────────────────────┘
```

For full architectural diagrams and authentication flows, see [`docs/architecture/ARCHITECTURE.md`](docs/architecture/ARCHITECTURE.md).

---

## Project Structure

```
agro-linken/
├── README.md                          # Project documentation
├── LICENSE                            # MIT License
├── .env.example                       # Environment configuration template
├── Dockerfile                         # Backend container definition
├── pom.xml                            # Spring Boot Maven configuration
├── firebase.json                      # Firebase Hosting configuration
│
├── frontend/                          # Frontend web assets
│   ├── index.html                     # Login & authentication page
│   ├── dashboard.html                 # Main dashboard (Orders, Bids, Ledger)
│   ├── marketplace.html               # Produce catalog & search
│   ├── community.html                 # Forum board
│   ├── chat.html                      # Real-time WebSocket messaging
│   ├── schemes.html                   # Government schemes directory
│   ├── scripts/
│   │   └── api.js                     # API client & HTTP helpers
│   └── styles/
│       └── main.css                   # Application styles
│
├── src/main/java/com/agrosystem/      # Backend Java source code
│   ├── AgroSystemApplication.java     # Application entry point
│   ├── config/                        # Security, CORS, WebSocket, & Data loaders
│   ├── controller/                    # REST API endpoints
│   ├── model/                         # JPA domain entities
│   └── repository/                    # Spring Data JPA repositories
│
└── docs/                              # Technical documentation
    ├── DEVELOPMENT.md                 # Development phases and changelog
    ├── api/API.md                     # Endpoint reference
    └── architecture/ARCHITECTURE.md   # Architectural reference
```

---

## Setup and Environment Configuration

### Prerequisites
- JDK 17 or higher
- Maven 3.9+
- PostgreSQL 14+
- Firebase CLI (`npm install -g firebase-tools`)

### 1. Database Setup
Create a PostgreSQL database and user:

```sql
CREATE DATABASE agro_linken;
CREATE USER agro_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE agro_linken TO agro_user;
```

### 2. Environment Variables
Copy `.env.example` to `.env` and supply your database credentials:

```bash
cp .env.example .env
```

Configured variables:
```env
DB_URL=jdbc:postgresql://localhost:5432/agro_linken
DB_USERNAME=agro_user
DB_PASSWORD=your_password
FIREBASE_CREDENTIALS=  # Required in production; local dev can use serviceAccountKey.json
```

### 3. Firebase Admin SDK Key (Backend)
1. Download a Service Account JSON key from your Firebase Console under **Project Settings > Service Accounts**.
2. For local execution, place the file in the project root named `serviceAccountKey.json` (ignored by git).
3. For production deployment, set `FIREBASE_CREDENTIALS` to the raw JSON string content of the key.

---

## Running Locally

### Backend
Run the Spring Boot application using Maven:

```bash
mvn spring-boot:run
```

Or build and run the packaged JAR:

```bash
mvn clean package -DskipTests
java -jar target/agro-linken-0.0.1-SNAPSHOT.jar
```

The server listens on `http://localhost:8080`.

### Frontend
Serve the static files from the `frontend/` directory:

```bash
firebase serve --only hosting
```
Or open `frontend/index.html` using any HTTP server (e.g. `npx serve frontend`).

---

## Core API Endpoints

A detailed API specification is available in [`docs/api/API.md`](docs/api/API.md).

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/users/login` | Authenticate user via Firebase JWT |
| `GET` | `/api/products` | Retrieve available produce listings |
| `GET` | `/api/products/search` | Search produce with filters |
| `POST` | `/api/products` | Post new produce listing |
| `POST` | `/api/orders` | Create produce purchase order |
| `PATCH` | `/api/orders/{id}/status` | Update lifecycle state of an order |
| `POST` | `/api/bids` | Submit transporter freight quote |
| `POST` | `/api/bids/{id}/accept` | Accept a delivery quote and assign transporter |
| `WS` | `/ws-chat` | STOMP WebSocket connection for messaging |

---

## Deployment

- **Frontend**: Hosted on Firebase Hosting pointing to the `frontend/` directory (`firebase deploy --only hosting`).
- **Backend**: Containerized via root `Dockerfile` and deployed on Render.
- **Database**: Managed PostgreSQL instance on Render.

---

## License

This project is open-source and licensed under the [MIT License](LICENSE).
