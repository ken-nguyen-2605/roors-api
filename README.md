# Restaurant Online Ordering and Reservation System (ROORS)

## 📝 Overview

ROORS addresses the evolving needs of the modern dining industry by providing a complete online solution that enhances customer experience while streamlining restaurant operations. The platform enables customers to browse dynamic menus, place orders with real-time tracking, and make table reservations with availability checking, while empowering restaurant staff with powerful management tools and analytics.

## 🌐 Deployments

### Live API

The API is deployed and accessible at: [Coming Soon]

### API Documentation

Interactive API documentation available at: `http://localhost:8080/swagger-ui.html` (when running locally)

## 📦 Prerequisites

Before running this project, ensure you have the following installed:

- **Java** (JDK 17 or higher)
- **Maven** (3.6.0 or higher)
- **Docker & Docker Compose** for database services
- **Git** for version control

Certainly! Here’s a concise project structure description for your README, incorporating the structure exactly as you provided:

---

## 📂 Project Structure

This project uses a **feature-based modular structure.** Each main business domain (module) contains its own logic, with optional sub-packages for controllers, services, DTOs, entities, and repositories as needed.

```
roors
├── RoorsApplication.java              # Application entry-point
├── config                             # Global configs (e.g. security, web, DB)
├── auth                               # Authentication & authorization
│   ├── controller                     # Auth controllers
│   ├── dto                            # Auth DTOs
│   ├── entity                         # Auth entities (e.g. User)
│   ├── repository                     # Auth data access
│   ├── service                        # Auth services (e.g. JWT)
│   ├── JwtTokenFilter.java            # JWT token filter
│   ├── JwtUtil.java                   # JWT utilities
│   └── ...                            # Other auth-specific files
├── reservation                        # Reservation feature (typically similar sub-structure)
├── order                              # Order management
├── menu                               # Menu management
├── administration                     # Admin tools and management
├── customerservice                    # Customer service features
├── notification                       # Notification service
└── common
    ├── exception                      # Shared exception handling
    └── util                           # Shared utilities
```

**Note:**
- Each feature/module (e.g. `order`, `menu`, etc.) may have its own submodules like `controller`, `service`, `dto`, `entity`, etc., as seen in `auth`.
- The `common` module contains code reused across multiple features.

## 🚀 Getting Started

### 1. Clone the Repository
```bash
git clone https://github.com/ken-nguyen-2605/roors.git
cd roors
```

### 2. Start Database Services
```bash
docker-compose -f docker-compose.dev.yml up -d
```

This will start:
- **PostgreSQL** on port `5432`
- **pgAdmin** on port `8081` (http://localhost:8081)
    - Email: `admin@gmail.com`
    - Password: `admin`

Wait for the services to be fully ready. You can check the logs to ensure everything is running:
```bash
docker-compose -f docker-compose.dev.yml logs -f
```

### 3. Run the Application
```bash
# Using Maven Wrapper
./mvnw spring-boot:run

# Or build and run JAR
./mvnw clean package -DskipTests
java -jar target/*-SNAPSHOT.jar
```

The API will be available at `http://localhost:8080`

### 4. Available Commands

- `./mvnw clean package` - Build the application
- `./mvnw test` - Run tests
- `./mvnw spring-boot:run` - Start development server
- `docker-compose -f docker-compose.dev.yml down` - Stop database services
- `docker-compose -f docker-compose.dev.yml down -v` - Stop and remove all data

## 🗄️ Database Management

### Connecting to PostgreSQL

**From pgAdmin (Web UI):**
1. Open http://localhost:8081
2. Login with credentials above
3. Register new server:
    - Host: `db`
    - Port: `5432`
    - Database: `roors`
    - Username: `postgres`
    - Password: `postgres`

**From Host Machine:**
```
Host:     localhost
Port:     5555 // Prevent conflict with local PostgreSQL
Database: roors
Username: postgres
Password: postgres
```

## ⚙️ Configuration

The application uses a single `application.properties` file with environment variables for different environments. Default values are provided for local development.

### Local Development

Simply start the Docker services and run the Spring Boot application - all configurations are pre-set for local development:

1. **Start Docker services** (PostgreSQL & pgAdmin):
   ```bash
   docker-compose -f docker-compose.dev.yml up -d
   ```
   Wait for the containers to be fully initialized. Check the logs to confirm:
   ```bash
   docker-compose -f docker-compose.dev.yml logs
   ```

2. **Run the Spring Boot application**:
   ```bash
   ./mvnw spring-boot:run
   ```

The application will automatically connect to the PostgreSQL container using the default development configuration.

## 🌿 Git Workflow & Branch Naming

### Git Workflow

1. **Create a new branch (use the naming conventions below)**
   ```bash
   git checkout -b feature/your-branch-name
   ```

2. **Work on your changes, then stage and commit**
   ```bash
   git add .
   git commit -m "feat: add user authentication endpoint"
   ```

3. **Push your branch to GitHub**
   ```bash
   git push -u origin feature/your-branch-name
   ```

4. **Open a Pull Request** on GitHub when your work is ready for review.

### Branch Naming Convention

Use prefixes to categorize your work:

- **`feature/`** - New features
  ```bash
  feature/order-management
  feature/reservation-system
  feature/payment-integration
  ```

- **`fix/`** or **`bug/`** - Bug fixes
  ```bash
  fix/order-validation
  bug/reservation-conflict
  fix/payment-processing
  ```

- **`chore/`** - Maintenance tasks
  ```bash
  chore/update-dependencies
  chore/database-migration
  chore/improve-logging
  ```

### Commit Message Convention

Follow conventional commits format:
```
<type>(<scope>): <subject>

<body>

<footer>
```

Types:
- `feat` - New feature
- `fix` - Bug fix
- `docs` - Documentation changes
- `style` - Code style changes
- `refactor` - Code refactoring
- `test` - Test additions/changes
- `chore` - Maintenance tasks

## 📚 API Documentation

Once the application is running, access the API documentation at:
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI Spec: http://localhost:8080/v3/api-docs