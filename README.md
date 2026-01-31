# PrestaShop Modernization - Phase 1

A modernized version of PrestaShop e-commerce platform built with Spring Boot and Next.js.

## Technology Stack

| Layer | Technology |
|-------|------------|
| Backend | Spring Boot 3.5, Java 21, Spring Security, JWT |
| Frontend | Next.js 14, React 18, TypeScript, Tailwind CSS |
| Database | MySQL 8.0 |
| API Docs | OpenAPI 3.0 (Swagger UI) |

## Project Structure

```
prestashop-mod/
├── backend/                 # Spring Boot REST API
│   ├── src/main/java/com/prestashop/
│   │   ├── config/          # Security, CORS, OpenAPI config
│   │   ├── controller/      # REST controllers (public + admin)
│   │   ├── dto/             # Request/Response DTOs
│   │   ├── entity/          # JPA entities
│   │   ├── exception/       # Exception handlers
│   │   ├── repository/      # Spring Data repositories
│   │   ├── security/        # JWT authentication
│   │   └── service/         # Business logic
│   └── src/main/resources/
│       └── application.yml
├── frontend/                # Next.js SPA with SSR
│   ├── src/
│   │   ├── app/             # App Router pages
│   │   │   ├── category/    # PLP (Product Listing)
│   │   │   ├── product/     # PDP (Product Details)
│   │   │   ├── search/      # Search results
│   │   │   └── admin/       # Admin panel
│   │   ├── components/      # React components
│   │   ├── lib/             # API client, utilities
│   │   └── types/           # TypeScript definitions
│   └── package.json
├── docker-compose.yml       # Full stack development
└── README.md
```

## Phase 1 Features

### Customer-Facing (SEO-Optimized with Server Components)
- **Product Listing Page (PLP)**: Browse products by category, search, pagination, sorting
- **Product Details Page (PDP)**: Full product info, image gallery, variant selection

### Admin Panel
- **Authentication**: JWT-based secure login
- **Product Management**: Create, edit, delete products
- **Category Management**: Hierarchical category tree
- **Image Upload**: Product image management

## Quick Start

### Option 1: Docker (Recommended)

```bash
# Start all services (MySQL, Backend, Frontend)
docker-compose up -d

# Access the application
# Frontend: http://localhost:3000
# Backend API: http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui.html
```

### Option 2: Manual Setup

**Prerequisites:**
- Java 21+
- Node.js 20+
- MySQL 8.0

**1. Start MySQL**
```bash
# Create database
mysql -u root -p -e "CREATE DATABASE prestashop;"
```

**2. Start Backend**
```bash
cd backend

# Windows
mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

**3. Start Frontend**
```bash
cd frontend
npm install
npm run dev
```

## API Endpoints

### Public APIs
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/products` | List products (PLP) |
| GET | `/api/v1/products/{slug}` | Product details (PDP) |
| GET | `/api/v1/products/search?q=` | Search products |
| GET | `/api/v1/categories` | Category tree |
| GET | `/api/v1/categories/{slug}/products` | Products by category |

### Admin APIs (JWT Required)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/login` | Admin login |
| GET/POST/PUT/DELETE | `/api/v1/admin/products` | Product CRUD |
| GET/POST/PUT/DELETE | `/api/v1/admin/categories` | Category CRUD |
| POST | `/api/v1/admin/products/{id}/images` | Upload image |
| POST | `/api/v1/admin/migration/legacy-images` | Migrate images from prestashop-legacy |

## Default Credentials

```
Email: admin@prestashop.com
Password: admin123
```

## Legacy Image Migration

To migrate product images from **prestashop-legacy** (PHP) to **prestashop-mod**:

### Using Docker
When running with Docker, the prestashop-legacy fixtures are automatically mounted. On first startup, images are migrated from `prestashop-legacy/install-dev/fixtures/fashion` if the path exists.

Ensure the folder structure:
```
prestashop/                    # or your repo root
├── prestashop-mod/
├── prestashop-legacy/
│   └── install-dev/
│       └── fixtures/
│           └── fashion/
│               ├── data/
│               │   └── image.xml
│               └── img/
│                   └── p/     # product images (*.jpg)
```

### Manual Setup
Set the legacy path in your environment or `application.yml`:

```bash
# Backend .env or environment
LEGACY_FIXTURES_PATH=../prestashop-legacy/install-dev/fixtures/fashion
LEGACY_MIGRATION_ENABLED=true
```

### Trigger Migration Manually (Admin API)
If migration didn't run at startup, trigger it via the admin API:

```bash
# Login first to get token
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@prestashop.com","password":"admin123"}' \
  | jq -r '.data.token')

# Run migration
curl -X POST "http://localhost:8080/api/v1/admin/migration/legacy-images" \
  -H "Authorization: Bearer $TOKEN"

# Or with custom path
curl -X POST "http://localhost:8080/api/v1/admin/migration/legacy-images?path=/path/to/fashion/fixtures" \
  -H "Authorization: Bearer $TOKEN"
```

## Configuration

### Backend (`backend/src/main/resources/application.yml`)
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/prestashop
    username: root
    password: root

jwt:
  secret: your-secret-key
  expiration: 86400000  # 24 hours
```

### Frontend (`.env.local`)
```env
NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1
```

## Development

### Backend
- API docs: http://localhost:8080/swagger-ui.html
- Hot reload enabled with Spring DevTools

### Frontend
- Hot reload enabled with Next.js Fast Refresh
- TypeScript strict mode enabled

## Build for Production

### Backend
```bash
cd backend
./mvnw package -DskipTests
java -jar target/prestashop-backend-1.0.0-SNAPSHOT.jar
```

### Frontend
```bash
cd frontend
npm run build
npm start
```

## Architecture Decisions

1. **React Server Components**: PLP and PDP pages are server-rendered for SEO
2. **JWT Authentication**: Stateless authentication for admin APIs
3. **Spring Data JPA**: Repository pattern with automatic query generation
4. **TypeScript**: Full type safety in frontend
5. **Tailwind CSS**: Utility-first styling with consistent design system

## Future Phases

- **Phase 2**: Shopping cart, checkout, customer accounts
- **Phase 3**: Order management, inventory, reporting
- **Phase 4**: Payment integration, shipping, notifications
