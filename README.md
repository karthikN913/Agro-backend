<div align="center">

# 🌾 Agro Linken
### Rural Supply Chain & Market Link System

**A full-stack platform that directly connects farmers, buyers, shop owners, and transporters — eliminating middlemen and empowering rural agriculture.**

[![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=java)](https://www.java.com/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.4-6DB33F?style=for-the-badge&logo=spring)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-336791?style=for-the-badge&logo=postgresql)](https://www.postgresql.org/)
[![Firebase](https://img.shields.io/badge/Firebase-Auth_&_Hosting-FFCA28?style=for-the-badge&logo=firebase)](https://firebase.google.com/)
[![Render](https://img.shields.io/badge/Render-Deployed-46E3B7?style=for-the-badge&logo=render)](https://render.com/)

</div>

---

## 📖 Project Overview

Agro Linken is a **digital supply chain platform** designed to solve one of India's most persistent agricultural challenges: the gap between farmers and their markets. Traditional supply chains are plagued by middlemen who absorb the majority of profits, leaving farmers underpaid and buyers overpaying.

This platform creates a **direct marketplace** where:
- **Farmers** list their produce with real prices and connect directly with buyers
- **Buyers & Shop Owners** browse, filter, and order fresh farm produce
- **Transporters** bid competitively on delivery assignments, making logistics transparent
- **The community** shares farming knowledge and tips in a built-in forum

Key highlights include real-time WebSocket chat between users, a digital credit (Udhar) ledger for tracking rural credit transactions, automated crop price alert subscriptions, and a government scheme browser — all wrapped in a premium dark-mode interface accessible from any device.

---

## ✨ Key Features

| Feature | Description |
|---------|-------------|
| 🔐 **Firebase Authentication** | Email/password and Google Sign-In with JWT token verification |
| 👥 **Role-Based Access** | Five roles: Farmer, Buyer, Shop Owner, Transporter, Admin |
| 🛒 **Product Marketplace** | List, search, and filter fresh produce with advanced multi-parameter search |
| 📦 **Order Management** | Full order lifecycle: Pending → Accepted → Shipped → Delivered |
| 🚛 **Delivery Bidding System** | Transporters bid competitively on shipment assignments |
| 💬 **Real-Time WebSocket Chat** | STOMP-based peer-to-peer messaging persisted to PostgreSQL |
| 🔔 **Crop Price Alerts** | Subscribe to categories; get notified when matching products are listed |
| 📒 **Digital Credit Ledger** | Track rural credit (Udhar) transactions with settlement history |
| 🏛️ **Government Schemes Browser** | Browse 8+ active agricultural schemes with eligibility info |
| 🌐 **Community Forum** | Post farming tips, ask questions, and like helpful posts |
| 📄 **PDF Tax Invoice Generator** | Client-side jsPDF invoice download directly from the dashboard |
| 📍 **Live Transporter Tracking** | Real-time location updates logged to the order record |
| 🌍 **Multilingual Support** | Google Translate integration with CSS layout stabilization |

---

## 🛠️ Tech Stack

### Backend
| Technology | Purpose |
|-----------|---------|
| Java 17 | Core programming language |
| Spring Boot 3.2.4 | Web framework, REST API, dependency injection |
| Spring Data JPA / Hibernate | ORM, database queries |
| Spring WebSocket (STOMP) | Real-time bidirectional messaging |
| Firebase Admin SDK 9.2 | Server-side JWT token verification |
| Lombok | Boilerplate reduction |
| Maven | Build tool and dependency management |

### Frontend
| Technology | Purpose |
|-----------|---------|
| HTML5 / CSS3 | Structure and dark-mode design system |
| Vanilla JavaScript (ES6+) | Application logic, no framework overhead |
| Firebase Web SDK | Client-side authentication |
| STOMP.js + SockJS | WebSocket client for real-time chat |
| jsPDF | Client-side PDF invoice generation |
| Wikipedia REST API | Dynamic crop imagery in the marketplace |

### Database & Infrastructure
| Technology | Purpose |
|-----------|---------|
| PostgreSQL 15 | Primary relational database |
| HikariCP | Production-grade connection pooling |
| Firebase Hosting | Frontend CDN delivery |
| Render | Backend Docker container hosting |
| Docker | Container packaging for backend deployment |

---

## 🏗️ System Architecture

```
                    ┌─────────────────────────────────────┐
                    │           Client Browser             │
                    │   HTML5 / CSS3 / JS Frontend         │
                    │   (Firebase Hosting — Global CDN)    │
                    │                                      │
                    │   api.js ◄──── Firebase Web SDK      │
                    └──────────┬──────────────────────────┘
                               │  Bearer JWT (REST HTTP)
                               │  WebSocket (STOMP)
                               ▼
             ┌──────────────────────────────────┐
             │     Spring Boot Backend           │
             │     (Render / Docker)             │
             │                                  │
             │  REST Controllers (10 endpoints) │
             │  WebSocket / STOMP (/ws-chat)    │
             │  Firebase Admin SDK (JWT verify) │
             │  JPA Repositories                │
             └──────────────┬───────────────────┘
                            │  SQL (HikariCP)
                            ▼
                 ┌──────────────────────┐
                 │  PostgreSQL Database  │
                 │  (Render Managed DB) │
                 └──────────────────────┘
```

**Full architecture details:** [`docs/architecture/ARCHITECTURE.md`](docs/architecture/ARCHITECTURE.md)

---

## 📁 Project Structure

```
agro-linken/
│
├── README.md                          ← You are here
├── LICENSE                            ← MIT License
├── .env.example                       ← Environment variable template
├── .gitignore                         ← Git ignore rules
│
├── Dockerfile                         ← Docker build for Render deployment
├── .dockerignore                      ← Files excluded from Docker build
├── pom.xml                            ← Maven build config + dependencies
│
├── firebase.json                      ← Firebase Hosting config (public: frontend/)
├── .firebaserc                        ← Firebase project binding
├── firestore.rules                    ← Firestore security rules
├── firestore.indexes.json             ← Firestore index definitions
│
├── frontend/                          ← Vanilla HTML/CSS/JS Frontend
│   ├── index.html                     ← Landing page + Firebase login
│   ├── dashboard.html                 ← Main user hub (orders, bids, ledger)
│   ├── marketplace.html               ← Product browser + ordering
│   ├── community.html                 ← Community forum
│   ├── chat.html                      ← Real-time WebSocket chat
│   ├── schemes.html                   ← Government scheme browser
│   ├── scripts/
│   │   └── api.js                     ← Centralized API client (all fetch() calls)
│   └── styles/
│       └── main.css                   ← Global dark-mode design system
│
├── src/main/java/com/agrosystem/      ← Spring Boot Backend Source
│   ├── AgroSystemApplication.java     ← Application entry point
│   ├── config/
│   │   ├── CorsConfig.java            ← CORS policy (allows Firebase Hosting origin)
│   │   ├── DataLoader.java            ← Idempotent DB seed (schemes, community posts)
│   │   ├── FirebaseConfig.java        ← Firebase Admin SDK initialization
│   │   └── WebSocketConfig.java       ← STOMP WebSocket endpoint configuration
│   ├── controller/                    ← REST API Controllers
│   │   ├── UserController.java        ← /api/users
│   │   ├── ProductController.java     ← /api/products
│   │   ├── OrderController.java       ← /api/orders
│   │   ├── DeliveryBidController.java ← /api/bids
│   │   ├── ReviewController.java      ← /api/reviews
│   │   ├── CreditController.java      ← /api/credits
│   │   ├── CommunityPostController.java ← /api/community
│   │   ├── ChatController.java        ← WebSocket /app/chat.sendMessage
│   │   ├── GovernmentSchemeController.java ← /api/schemes
│   │   └── SubscriptionController.java ← /api/subscriptions
│   ├── model/                         ← JPA Entity Classes (DB Tables)
│   │   ├── User.java                  ← users table (5 roles + vehicle profile)
│   │   ├── Product.java               ← products table
│   │   ├── Order.java                 ← orders table (status lifecycle)
│   │   ├── DeliveryBid.java           ← delivery_bids table
│   │   ├── Review.java                ← reviews table
│   │   ├── CreditRecord.java          ← credit_records table
│   │   ├── CommunityPost.java         ← community_posts table
│   │   ├── Message.java               ← messages table (chat history)
│   │   ├── Notification.java          ← notifications table
│   │   ├── CropSubscription.java      ← crop_subscriptions table
│   │   └── GovernmentScheme.java      ← government_schemes table
│   └── repository/                    ← Spring Data JPA Repositories (11 interfaces)
│
├── src/main/resources/
│   └── application.properties         ← DB, JPA, HikariCP, Firebase config
│
├── functions/                         ← Firebase Cloud Functions (archived prototype)
│   └── index.js                       ← Early-stage mock server — NOT the real backend
│
└── docs/                              ← Project Documentation
    ├── DEVELOPMENT.md                 ← Full development history and phases
    ├── api/
    │   └── API.md                     ← Complete REST API reference
    └── architecture/
        └── ARCHITECTURE.md            ← System architecture diagrams
```

---

## 🚀 Getting Started

### Prerequisites

| Tool | Version | Download |
|------|---------|---------|
| Java JDK | 17+ | [adoptium.net](https://adoptium.net/) |
| Maven | 3.9+ | [maven.apache.org](https://maven.apache.org/) |
| PostgreSQL | 14+ | [postgresql.org](https://www.postgresql.org/) |
| Firebase CLI | Latest | `npm install -g firebase-tools` |
| Git | Latest | [git-scm.com](https://git-scm.com/) |

### 1. Clone the Repository

```bash
git clone https://github.com/karthikN913/Agro-backend.git
cd Agro-backend
```

### 2. Set Up PostgreSQL Database

```sql
-- Connect to PostgreSQL and create the database
CREATE DATABASE agro_linken;
CREATE USER agro_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE agro_linken TO agro_user;
```

### 3. Configure Firebase

1. Go to [Firebase Console](https://console.firebase.google.com/) → Your Project → Project Settings → Service Accounts
2. Click **"Generate new private key"** — download the JSON file
3. **Do NOT commit this file.** It is already listed in `.gitignore`.
4. For local development: rename the file to `serviceAccountKey.json` and place it in the project root
5. For production: set the entire JSON content as the `FIREBASE_CREDENTIALS` environment variable on Render

### 4. Configure Environment Variables

```bash
# Copy the example file
cp .env.example .env

# Edit .env with your actual values:
DB_URL=jdbc:postgresql://localhost:5432/agro_linken
DB_USERNAME=agro_user
DB_PASSWORD=your_password
FIREBASE_CREDENTIALS=  # Leave empty for local dev; use serviceAccountKey.json instead
```

> **Note:** Hibernate `ddl-auto=update` will automatically create all database tables on first boot.

### 5. Start the Backend

```bash
# Option A — Maven directly
mvn spring-boot:run

# Option B — Build JAR first
mvn clean package -DskipTests
java -jar target/agro-linken-0.0.1-SNAPSHOT.jar
```

Backend will start on: **http://localhost:8080**

Verify it's running:
```bash
curl http://localhost:8080/api/products/debug
# Expected: "DB Connection OK! Users count: 0, Products count: 0"
```

### 6. Start the Frontend

```bash
# Option A — Firebase CLI (recommended)
firebase serve --only hosting
# Frontend at: http://localhost:5000

# Option B — Any static server
npx serve frontend
# or simply open frontend/index.html in a browser
```

> **Important:** Update the `API_BASE_URL` constant in `frontend/scripts/api.js` to point to your backend:
> ```javascript
> const API_BASE_URL = 'http://localhost:8080'; // for local dev
> ```

---

## 🔐 Environment Variables

| Variable | Required | Description |
|----------|----------|-------------|
| `DB_URL` | ✅ Yes | Full JDBC URL to PostgreSQL (`jdbc:postgresql://host:port/dbname`) |
| `DB_USERNAME` | ✅ Yes | PostgreSQL username |
| `DB_PASSWORD` | ✅ Yes | PostgreSQL password |
| `FIREBASE_CREDENTIALS` | ✅ Yes (prod) | Full JSON content of Firebase service account key |

See [`.env.example`](.env.example) for a complete template with descriptions.

---

## 📡 API Documentation

The backend exposes a RESTful API on port `8080`. All endpoints are documented in **[`docs/api/API.md`](docs/api/API.md)**.

**Quick Reference — Core Endpoints:**

```
POST   /api/users/register          ← Register user (Firebase JWT required)
POST   /api/users/login             ← Login / auto-register

GET    /api/products                ← Browse all products
GET    /api/products/search         ← Advanced search (query, category, price range, location)
POST   /api/products                ← List a product (farmers only)

POST   /api/orders                  ← Place an order
PATCH  /api/orders/{id}/status      ← Update order status
GET    /api/orders/transporter/available ← Available shipments for bidding

POST   /api/bids                    ← Submit delivery bid (transporters only)
POST   /api/bids/{id}/accept        ← Accept a bid (assigns transporter)

WS     /ws-chat                     ← WebSocket STOMP endpoint (real-time chat)
```

---

## 🚢 Deployment

### Architecture

```
Frontend  ──►  Firebase Hosting   (CDN — auto-deploy via firebase deploy)
Backend   ──►  Render             (Docker container — auto-deploy from GitHub)
Database  ──►  PostgreSQL         (Render Managed DB or Neon.tech)
Auth      ──►  Firebase Auth      (managed by Google Cloud)
```

### Deploy Frontend (Firebase Hosting)

```bash
# Install Firebase CLI if not already done
npm install -g firebase-tools

# Login and deploy
firebase login
firebase deploy --only hosting
```

### Deploy Backend (Render)

1. Push your code to GitHub
2. On [Render](https://render.com/), create a **New Web Service**
3. Connect your GitHub repository
4. Render will automatically detect the `Dockerfile`
5. Add the following environment variables in Render's dashboard:
   - `DB_URL` — Your Render PostgreSQL connection string
   - `DB_USERNAME` — Database user
   - `DB_PASSWORD` — Database password
   - `FIREBASE_CREDENTIALS` — Full service account JSON (as a single-line string)

**Docker build details:** The `Dockerfile` builds the Spring Boot JAR inside the container with Maven and starts with JVM memory flags tuned for Render's free tier (`-Xmx350m -Xms256m`).

---

## 🔒 Security Notes

- Firebase JWT tokens are verified server-side on every protected request using the Firebase Admin SDK
- Database credentials and Firebase private keys are passed via environment variables — never hardcoded
- `serviceAccountKey.json` is explicitly listed in `.gitignore` and must never be committed
- The `.env` file is excluded from version control — use `.env.example` as a reference template

> ⚠️ **Important:** If you fork this project, generate your own Firebase service account key and PostgreSQL credentials. Never reuse credentials from this repository.

---

## 🗺️ Future Improvements

- [ ] Add a dedicated **Service Layer** to separate business logic from REST controllers
- [ ] Implement **Spring Security** for proper role-based endpoint authorization (currently role checks are manual in controllers)
- [ ] Add **unit and integration tests** (JUnit 5 + MockMvc + Testcontainers for PostgreSQL)
- [ ] Implement **refresh token** strategy for extended Firebase session management
- [ ] Add **image upload** for product listings (Firebase Storage)
- [ ] Build **admin dashboard** for platform management (user management, scheme editor)
- [ ] Add **CI/CD pipeline** (GitHub Actions) for automated build + deploy on push
- [ ] Implement **payment gateway** integration (Razorpay or UPI) as alternative to Cash on Delivery
- [ ] Add **offline support** with a Progressive Web App (PWA) manifest

---

## 📚 Documentation

| Document | Description |
|----------|-------------|
| [Architecture](docs/architecture/ARCHITECTURE.md) | System design, component diagrams, auth flow, DB schema |
| [API Reference](docs/api/API.md) | All REST endpoints with request/response examples |
| [Development History](docs/DEVELOPMENT.md) | Phase-by-phase implementation log and optimizations |

---

## 📝 License

This project is licensed under the [MIT License](LICENSE).

---

<div align="center">

Built with ❤️ for Indian farmers · **[Agro Linken](https://github.com/karthikN913/Agro-backend)**

</div>
