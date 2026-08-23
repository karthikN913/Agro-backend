# Agro Linken — REST API Documentation

Base URL (Production): `https://agro-linken.onrender.com`  
Base URL (Local Dev): `http://localhost:8080`

All protected endpoints require a Firebase ID Token in the `Authorization` header:
```
Authorization: Bearer <firebase-id-token>
```

---

## Authentication & Users — `/api/users`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/api/users/register` | ✅ Required | Register/update a user profile linked to Firebase UID |
| `POST` | `/api/users/login` | ✅ Required | Login via Firebase JWT; auto-registers if first Google Sign-In |
| `GET` | `/api/users` | ❌ Open | Get all registered users (used by chat user list) |
| `GET` | `/api/users/{id}` | ❌ Open | Get a specific user by database ID |
| `PUT` | `/api/users/{id}/vehicle` | ❌ Open | Update vehicle profile (TRANSPORTER role only) |

**Register/Login Request Body:**
```json
{
  "name": "Ravi Kumar",
  "phone": "9876543210",
  "role": "FARMER",
  "location": "Pune, Maharashtra",
  "shopName": null
}
```
**Roles:** `FARMER` | `BUYER` | `SHOP_OWNER` | `TRANSPORTER` | `ADMIN`

---

## Products — `/api/products`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `GET` | `/api/products` | ❌ Open | Get all listed products |
| `GET` | `/api/products/farmer/{farmerId}` | ❌ Open | Get products listed by a specific farmer |
| `GET` | `/api/products/search` | ❌ Open | Advanced search with optional filters |
| `POST` | `/api/products` | ❌ Open | List a new product (FARMER role only) |
| `DELETE` | `/api/products/{id}?farmerId={id}` | ❌ Open | Delete own product |
| `GET` | `/api/products/debug` | ❌ Open | DB health check — returns user/product counts |

**Search Query Parameters:**
```
GET /api/products/search?query=tomato&category=Vegetables&minPrice=10&maxPrice=100&location=Pune
```

**Add Product Request Body:**
```json
{
  "name": "Organic Tomatoes",
  "category": "Vegetables",
  "price": 45.00,
  "quantity": 100,
  "unit": "kg",
  "description": "Fresh from farm",
  "location": "Pune",
  "farmer": { "id": 1 }
}
```

---

## Orders — `/api/orders`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/api/orders` | ❌ Open | Place a new order (BUYER or SHOP_OWNER only) |
| `GET` | `/api/orders/buyer/{buyerId}` | ❌ Open | Get all orders for a buyer |
| `GET` | `/api/orders/farmer/{farmerId}` | ❌ Open | Get all orders for a farmer's products |
| `GET` | `/api/orders/transporter/{transporterId}` | ❌ Open | Get orders assigned to a transporter |
| `GET` | `/api/orders/transporter/available` | ❌ Open | Get ACCEPTED orders with no transporter assigned |
| `PATCH` | `/api/orders/{id}/status` | ❌ Open | Update order status |
| `PATCH` | `/api/orders/{id}/assign/{transporterId}` | ❌ Open | Directly assign transporter to order |
| `PATCH` | `/api/orders/{id}/location` | ❌ Open | Update transporter's current location for tracking |

**Order Statuses:** `PENDING` → `ACCEPTED` → `SHIPPED` → `DELIVERED`

**Place Order Request Body:**
```json
{
  "buyer": { "id": 2 },
  "product": { "id": 5 },
  "quantity": 10
}
```

---

## Delivery Bids — `/api/bids`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/api/bids` | ❌ Open | Submit a delivery bid (TRANSPORTER role only) |
| `POST` | `/api/bids/{bidId}/accept` | ❌ Open | Accept a bid — assigns transporter, rejects others |
| `GET` | `/api/bids/order/{orderId}` | ❌ Open | Get all bids for a specific order |
| `GET` | `/api/bids/transporter/{transporterId}` | ❌ Open | Get all bids placed by a transporter |

**Submit Bid Request Body:**
```json
{
  "orderId": 12,
  "transporterId": 7,
  "bidAmount": 350.00,
  "estimatedDeliveryTime": "2 hours"
}
```

---

## Reviews — `/api/reviews`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/api/reviews` | ❌ Open | Submit a review for a product |
| `GET` | `/api/reviews/product/{productId}` | ❌ Open | Get all reviews for a product |
| `GET` | `/api/reviews/summaries` | ❌ Open | Get aggregated avg rating + count per product (single SQL query) |

**Submit Review Request Body:**
```json
{
  "product": { "id": 5 },
  "reviewer": { "id": 2 },
  "rating": 4,
  "comment": "Fresh and good quality!"
}
```

---

## Credit Ledger (Udhar Book) — `/api/credits`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/api/credits` | ❌ Open | Create a credit record (debt/payment) |
| `GET` | `/api/credits/user/{userId}` | ❌ Open | Get all credit records for a user |
| `PATCH` | `/api/credits/{id}/settle` | ❌ Open | Mark a credit record as settled |

---

## Community Forum — `/api/community`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `GET` | `/api/community` | ❌ Open | Get all community posts |
| `POST` | `/api/community` | ❌ Open | Create a new community post |
| `POST` | `/api/community/{id}/like` | ❌ Open | Increment like count on a post |

---

## Real-Time Chat — WebSocket

| Protocol | Endpoint | Description |
|----------|----------|-------------|
| WebSocket | `ws://host/ws-chat` | STOMP WebSocket connection |
| Subscribe | `/topic/messages/{roomId}` | Receive messages for a chat room |
| Publish | `/app/chat.sendMessage` | Send a new message |

**Message Payload:**
```json
{
  "senderId": 1,
  "receiverId": 2,
  "content": "Hello! Are tomatoes still available?"
}
```

---

## Government Schemes — `/api/schemes`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `GET` | `/api/schemes` | ❌ Open | Get all government agricultural schemes |

---

## Crop Subscriptions — `/api/subscriptions`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/api/subscriptions` | ❌ Open | Subscribe to price alerts for a crop category |
| `GET` | `/api/subscriptions/user/{userId}` | ❌ Open | Get all subscriptions for a user |
| `DELETE` | `/api/subscriptions/{id}` | ❌ Open | Remove a crop subscription |

---

## Notifications — (Internal, via ProductController)

Notifications are automatically dispatched by the backend when a new product matching a user's crop subscription is listed. They are consumed by the frontend dashboard.

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/notifications/user/{userId}` | Get all notifications for a user |
| `PATCH` | `/api/notifications/{id}/read` | Mark notification as read |
