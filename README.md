# PrestaShop Modernization - Phase 1

## Overview
This is a modernized version of PrestaShop e-commerce platform built with:
- **Backend**: Spring Boot 3.5.10
- **Frontend**: Next.js 14 with React Server Components
- **Database**: PostgreSQL (H2 for development)

## Monorepo Structure
```
prestashop-mod/
├── backend/                 # Spring Boot application
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/prestashop/
│   │   │   │   ├── PrestashopApplication.java
│   │   │   │   ├── config/           # Security, CORS, etc.
│   │   │   │   ├── entity/           # JPA entities
│   │   │   │   ├── repository/       # Spring Data repositories
│   │   │   │   ├── service/          # Business logic
│   │   │   │   ├── controller/       # REST controllers
│   │   │   │   ├── dto/              # Data transfer objects
│   │   │   │   └── security/         # JWT, authentication
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── db/migration/     # Flyway migrations
│   │   └── test/
│   ├── pom.xml
│   └── Dockerfile
├── frontend/                # Next.js application
│   ├── src/
│   │   ├── app/             # App Router (Server Components)
│   │   │   ├── layout.tsx
│   │   │   ├── page.tsx
│   │   │   ├── products/    # PLP
│   │   │   ├── product/     # PDP
│   │   │   └── admin/       # Admin pages
│   │   ├── components/      # React components
│   │   ├── lib/             # API client, utilities
│   │   └── types/           # TypeScript types
│   ├── package.json
│   ├── next.config.js
│   └── Dockerfile
├── docker-compose.yml       # Development environment
└── README.md
```

## Phase 1 Features

### Customer-Facing
- **Product Listing Page (PLP)**: Browse products by category with filtering, sorting, pagination
- **Product Details Page (PDP)**: View product details, images, variants, pricing

### Admin
- **Authentication**: Secure login with JWT
- **Product Catalog Management**: CRUD operations for products, categories, images

## Getting Started

### Prerequisites
- Java 21+
- Node.js 20+
- Docker & Docker Compose (optional)

### Development

**Backend:**
```bash
cd backend
./mvnw spring-boot:run
```

**Frontend:**
```bash
cd frontend
npm install
npm run dev
```

### API Documentation
- Swagger UI: http://localhost:8080/swagger-ui.html
- API Base URL: http://localhost:8080/api/v1

## License
Open Source
