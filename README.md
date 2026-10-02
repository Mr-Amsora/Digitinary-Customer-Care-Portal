# Digitinary Customer Care Portal

A secure, real-time REST API built with Spring Boot.

---

## How to Run

### Prerequisites
- Java 17+
- PostgreSQL running locally
- Maven 

### Database Setup
```sql
CREATE DATABASE digitinary_db;
```

### Configuration
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/digitinary_db
spring.datasource.username=YOUR_DB_USER
spring.datasource.password=YOUR_DB_PASSWORD
```

### Start the Application
```bash
./mvnw spring-boot:run
```

The app starts on **http://localhost:8080** and seeds the following accounts automatically:

- `admin` / `admin123` — role: ADMIN
- `customer1` to `customer10` / `password123` — role: CUSTOMER

---

## Entities

**Customer** — id, name, email, phone, createdAt. Has many orders and many tickets. Linked one-to-one with a User.

**Order** — id, customerId, status, total, createdAt. Belongs to one customer. Contains many order items.

**OrderItem** — id, orderId, itemId, quantity. Joins an order with an item.

**Item** — id, name, price. Can appear in many orders.

**Ticket** — id, customerId, subject, status, priority, createdAt. Belongs to one customer.

**User** — id, username, password, role. Linked one-to-one with a Customer (CUSTOMER role only).

Order status values: `PENDING`, `SHIPPED`, `DELIVERED`, `CANCELLED`

Ticket status values: `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`

Ticket priority values: `LOW`, `MEDIUM`, `HIGH`

User role values: `ADMIN`, `CUSTOMER`

---

## Authentication

### Obtain a JWT

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "expiresIn": 3600
}
```

### Use the JWT
Add it to every protected request:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### Register a new user
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "myuser",
  "password": "mypassword"
}
```

---

## Full Endpoint List

### Auth

- `POST /api/auth/login` — No auth required. Submit username and password to receive a JWT token.
- `POST /api/auth/register` — No auth required. Create a new user account with the CUSTOMER role.

### Customers

- `GET /api/customers` — ADMIN only. List all customers.
- `GET /api/customers/{id}` — Any authenticated user. Get a single customer by ID.
- `POST /api/customers` — Any authenticated user. Create a new customer profile.
- `PUT /api/customers/{id}` — ADMIN only. Fully replace a customer's data.
- `DELETE /api/customers/{id}` — ADMIN only. Delete a customer.
- `PUT /api/customers/{id}/user` — ADMIN only. Link an existing registered user to this customer.

### Orders

- `GET /api/customers/{id}/orders` — Owner or ADMIN. List all orders for a customer. Supports filtering, sorting, and pagination.
- `PUT /api/orders/{orderId}/status` — Owner or ADMIN. Update an order's status. Triggers the webhook delivery asynchronously.

### Tickets

- `GET /api/customers/{id}/tickets` — Owner or ADMIN. List all tickets for a customer. Supports filtering, sorting, and pagination.
- `POST /api/tickets` — Any authenticated user. Create a new ticket. Broadcasts the update via WebSocket. CUSTOMER users are linked automatically from their JWT. ADMIN must include `customerId` in the request body.
- `PUT /api/tickets/{ticketId}/status` — Owner or ADMIN. Update a ticket's status. Broadcasts the update via WebSocket.

### Webhooks

- `POST /api/webhooks/subscriptions` — Any authenticated user. Register a subscriber URL and event type.
- `POST /api/webhooks/incoming/orders` — No auth required. Simulated receiver endpoint that logs and verifies incoming webhook payloads.

### WebSocket

- STOMP endpoint: `ws://localhost:8080/ws` (SockJS fallback enabled)
- Broadcast topic: `/topic/tickets`
- Browser test client: `http://localhost:8080/ws-client.html`

---

## Query Parameters

### GET `/api/customers/{id}/orders`

- `status` — Filter by order status. Accepted values: `PENDING`, `SHIPPED`, `DELIVERED`, `CANCELLED`.
- `sort` — Sort direction. Use `newest` (default) or `oldest`.
- `page` — Zero-based page number. Default is `0`.
- `size` — Number of results per page. Default is `10`, maximum is `100`.

Example:
```
GET /api/customers/3/orders?status=SHIPPED&sort=oldest&page=0&size=5
```

### GET `/api/customers/{id}/tickets`

- `status` — Filter by ticket status. Accepted values: `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`.
- `priority` — Filter by priority. Accepted values: `LOW`, `MEDIUM`, `HIGH`.
- `sort` — Sort direction. Use `newest` (default) or `oldest`.
- `page` — Zero-based page number. Default is `0`.
- `size` — Number of results per page. Default is `10`, maximum is `100`.

Example:
```
GET /api/customers/3/tickets?status=OPEN&priority=HIGH&sort=newest&page=0&size=10
```

Unrecognized query parameters are silently ignored.

---

## Webhook Setup

### 1. Register a subscriber
```http
POST /api/webhooks/subscriptions
Authorization: Bearer <adminToken>
Content-Type: application/json

{
  "url": "http://localhost:8080/api/webhooks/incoming/orders",
  "eventType": "order.status_changed"
}
```

Response `201 Created`:
```json
{
  "id": "99d8f772-bb67-4380-9b34-bd2924355401",
  "url": "http://localhost:8080/api/webhooks/incoming/orders",
  "eventType": "order.status_changed"
}
```

### 2. Trigger the webhook
```http
PUT /api/orders/1/status
Authorization: Bearer <token>
Content-Type: application/json

{
  "status": "SHIPPED"
}
```

### 3. The receiver logs the signed payload
Check the application console for:
```
Webhook received | signatureValid=true
Webhook body: {"event":"order.status_changed","orderId":1,...}
```

Each delivery includes the header `X-Digitinary-Signature: sha256=<hmac>` computed with HmacSHA256 using the secret defined in `webhook.secret`.

Event payload shape:
```json
{
  "event": "order.status_changed",
  "orderId": 1,
  "previousStatus": "PENDING",
  "newStatus": "SHIPPED",
  "occurredAt": "2026-10-02T17:15:00Z"
}
```

---

## WebSocket Live Demo

1. Open `http://localhost:8080/ws-client.html` in a browser.
2. Click **Connect** — the status dot turns green.
3. In Postman, call `POST /api/tickets` or `PUT /api/tickets/{id}/status`.
4. A live update card appears in the browser feed instantly without refreshing.

Broadcast message shape:
```json
{
  "ticketId": 15,
  "customerId": 3,
  "status": "IN_PROGRESS",
  "updatedAt": "2026-10-02T17:20:00Z"
}
```

---

## Postman Collection

Import `Digitinary_CCP.postman_collection.json` into Postman.

Recommended run order:
1. Login as Admin — saves `adminToken` automatically
2. Login as Customer1 — saves `customerToken` automatically
3. Get Orders for Customer — saves `newOrderId`
4. Register Webhook Subscriber — must run before updating order status
5. Update Order Status — triggers webhook delivery
6. Create Ticket — saves `newTicketId` and broadcasts via WebSocket
7. Update Ticket Status — broadcasts via WebSocket

---

## Tech Stack

- Java 17
- Spring Boot 4.1.1
- Spring Security — stateless JWT authentication
- Spring Data JPA with Hibernate
- PostgreSQL 18

## short video demo URL:

- https://drive.google.com/file/d/17zvVngPn9qdlgOMftUlLBOuTWG7ak2zr/view?usp=sharing