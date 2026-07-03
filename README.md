# Donut Bank

A Spring Boot REST API for bank account handling: multi-currency accounts, credits, debits, currency exchange, and transaction history.

## Requirements

- Java 26 (no Maven install needed — the Maven wrapper is included)

## Setup & Run

```bash
cd bank
./mvnw spring-boot:run
```

The app starts on `http://localhost:8080` and uses a file-based H2 database (`bank/data/`), so no database setup is required. A demo user (`demo` / `password`) with sample data is seeded on first start.

To run the tests:

```bash
cd bank
./mvnw test
```

## API

All endpoints are under `http://localhost:8080/api`. Log in first to get a JWT, then pass it as a `Bearer` token:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "demo", "password": "password"}'
```

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/login` | Exchange credentials for a JWT |
| GET | `/api/users/me` | Current user info |
| GET | `/api/accounts` | List accounts |
| POST | `/api/accounts` | Create an account |
| GET | `/api/accounts/{id}` | Account details |
| GET | `/api/accounts/{id}/balance-history` | Balance history |
| GET | `/api/accounts/{id}/transactions` | Transaction history |
| POST | `/api/accounts/{id}/credits` | Credit an account |
| POST | `/api/accounts/{id}/debits` | Debit an account |
| POST | `/api/exchanges` | Exchange between accounts |
| GET | `/api/exchange-rates` | Current exchange rates |
| GET | `/api/payment-orders/{id}` | Payment order status |

Full API spec: [docs/openapi.yaml](docs/openapi.yaml). Design notes: [docs/design.md](docs/design.md).
