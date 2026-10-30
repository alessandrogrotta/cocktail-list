# 🍸 Cocktail Project API

A Spring Boot application that exposes REST APIs to manage cocktails, ingredients, preparation steps, and favorites. Security is handled by Keycloak (JWT), data lives in MySQL, and the documentation is browsable through Swagger. With Docker Compose, the environment includes three containers: the Spring Boot app (port 8080), MySQL (3306), and Keycloak (8081).

## 📑 Table of Contents

- [🧰 Technologies](#-technologies)
- [🚀 Quick Start](#-quick-start)
- [🏗️ Architecture](#-architecture)
- [📚 API Endpoints](#-api-endpoints-overview)
- [🎯 Implemented Optimizations](#-implemented-optimizations)
- [🧭 Tutorial: Login, CRUD, and Favorites](#-tutorial-login-crud-and-favorites)
- [🔐 Security Details](#-security-details)
- [🔧 Local Development](#-local-development)
- [🐳 Docker](#-docker)
- [📝 Development Notes](#-development-notes)
- [🤝 Contributing](#-contributing)
- [📄 License](#-license)

## 🧰 Technologies
- Framework: Spring Boot 4.0.0 (parent in `pom.xml`)
- Language: Java 17
- Database: MySQL 8
- ORM: JPA/Hibernate
- Security: Spring Security + OAuth2 Resource Server (Keycloak JWT)
- API Docs: SpringDoc OpenAPI 2.3.0 (Swagger UI)
- Build: Maven (Maven Wrapper included)
- Container: Docker & Docker Compose

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Docker & Docker Compose
- Maven 3.9+

### Starting with Docker Compose
```bash
# Start all services (MySQL, Keycloak, App)
docker compose down # if previously started
docker compose up -d --build

# Check the status
docker compose ps

# View the app logs
docker compose logs -f cocktail-app
```

### Accessing the Services
- REST API: http://localhost:8080/api/cocktails
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI Docs: http://localhost:8080/v3/api-docs
- Keycloak Admin UI: http://localhost:8081 (admin/admin)

## 🏗️ Architecture

### 3-Layer Structure
```
┌─────────────────┐
│   CONTROLLER    │  Receives HTTP
├─────────────────┤
│    SERVICE      │  Business logic
├─────────────────┤
│  REPOSITORY     │  Database access
├─────────────────┤
│    DATABASE     │  MySQL
└─────────────────┘
```

```
cocktail-project/
├── src/main/java/com/cocktail/cocktailproject/
│   ├── config/          # Configurations (Security, Swagger, global Beans)
│   ├── controller/      # REST Controllers (Cocktail, Auth, Favorites)
│   ├── dto/             # Data Transfer Objects
│   ├── entity/          # JPA Entities
│   ├── repository/      # Spring Data Repositories
│   └── service/         # Business Logic
├── src/main/resources/  # application.properties, templates/static
├── docker-compose.yml   # Service orchestration
└── Dockerfile           # Multi-stage build
```

### Components
- **Entity:** `Cocktail`, `Ingrediente`, `Preparazione`, `UserFavorito`
- **DTO:** `CocktailDTO`, `CreateCocktailRequestDTO`, `IngredientiDTO`, `LoginRequestDTO`, `TokenResponseDTO`, `UserRegistrationDTO`
- **Repository:** `CocktailRepository`, `IngredienteRepository`, `PreparazioneRepository`, `UserFavoritoRepository`
- **Service:** `CocktailService` (constructor injection), `FavoritiService`, `KeycloakUserService` (constructor injection)
- **Controller:** `CocktailController`, `AuthController`, `FavoritiController`
- **Config:** `SecurityConfig` (OAuth2/JWT permissions, ObjectMapper Bean, RestTemplate Bean), `SpringDocConfig` (Swagger)

### Dependency Injection

All services use **Constructor Injection** (modern Spring best practice):
```java
@Service
public class CocktailService {
    private final CocktailRepository cocktailRepository;
    private final IngredienteRepository ingredienteRepository;
    
    public CocktailService(
            CocktailRepository cocktailRepository,
            IngredienteRepository ingredienteRepository) {
        this.cocktailRepository = cocktailRepository;
        this.ingredienteRepository = ingredienteRepository;
    }
}
```

**Advantages:**
- ✅ Dependencies are always available and thread-safe
- ✅ Easy switching between implementations (e.g., switching databases)
- ✅ Simplified testing (easy mocking)
- ✅ Immutability (`final` fields)

### Permissions and Security
- Public: `/api/auth/**`, Swagger (`/swagger-ui/**`, `/v3/api-docs/**`), all `GET` requests on `/api/cocktails/**`
- Requires the `SOLDIER` role: `POST/PUT/DELETE` on `/api/cocktails/**`
- Requires authentication: all routes under `/api/favoriti/**`
- Roles are taken from `realm_access.roles` in the JWT and mapped as `ROLE_<role>`.

### Database Schema (logical)
```
cocktail (id, nome, descrizione, tempo_preparazione_minutes, note)
ingredienti (id, nome)
preparazione (id, cocktail_id, ingrediente_id, quantita, unita, step_order, istruzione)
user_favoriti (id, user_id, cocktail_id)
```
Relationships: Cocktail ↔ Preparazione (1:N); Ingrediente ↔ Preparazione (1:N); Cocktail ↔ Ingrediente (M:N via Preparazione); User ↔ Cocktail (M:N via user_favoriti).

### Typical Flow (GET /api/cocktails)
Client → Controller → Service → Repository/JPA → MySQL → conversion to DTO → JSON response.

## 📚 API Endpoints (overview)

### Cocktails
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/cocktails` | Paginated list of cocktails (`page`, `size`) |
| GET | `/api/cocktails/{id}` | Cocktail details by ID |
| GET | `/api/cocktails/search?nome=xxx` | Search cocktails by name (paginated) |
| GET | `/api/cocktails/ingredients` | Paginated list of ingredients |
| POST | `/api/cocktails` | Create a new cocktail (requires `SOLDIER`) |
| PUT | `/api/cocktails/{id}` | Update a cocktail (requires `SOLDIER`) |
| DELETE | `/api/cocktails/{id}` | Delete a cocktail (requires `SOLDIER`) |

### Favorites (authentication required)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/favoriti` | List the user's favorite cocktails |
| GET | `/api/favoriti/count` | Favorites count |
| GET | `/api/favoriti/check/{cocktailId}` | Check whether a cocktail is in the favorites |
| POST | `/api/favoriti/{cocktailId}` | Add to favorites |
| POST | `/api/favoriti/toggle/{cocktailId}` | Toggle add/remove |
| DELETE | `/api/favoriti/{cocktailId}` | Remove from favorites |

### Auth (public)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/login` | ROPC login, returns a JWT |
| POST | `/api/auth/register` | User registration on Keycloak |

## 🧭 Tutorial: Login, CRUD, and Favorites

### 📱 Option 1: With Swagger UI (Recommended)

Go to **http://localhost:8080/swagger-ui.html** for visual, intuitive navigation.

#### 1) Registration via Swagger
1. Open Swagger UI
2. Click **POST** `/api/auth/register`
3. Click **Try it out**
4. Fill in the JSON body with your details:
   ```json
   {
     "username": "newuser",
     "email": "newuser@example.com",
     "firstName": "John",
     "lastName": "Doe",
     "password": "MyPassword123!",
     "confirmPassword": "MyPassword123!"
   }
   ```
5. Click **Execute**
6. Wait for the success response

#### 2) Login via Swagger
1. Click **POST** `/api/auth/login`
2. Click **Try it out**
3. Fill in the JSON body with the **same credentials you just used for registration**:
   ```json
   {
     "username": "newuser",
     "password": "MyPassword123!"
   }
   ```
4. Click **Execute**
5. **Copy the `access_token` value** from the response

#### 2) Global authorization in Swagger
1. Click the **Authorize** button (lock icon) at the top right
2. In the "Bearer Token" field, paste the copied token (without `Bearer `)
3. Click **Authorize**, then **Close**
4. All protected endpoints will now automatically use the token

#### 3) Cocktails CRUD via Swagger

**View the cocktail list (public):**
- Click **GET** `/api/cocktails`
- Click **Try it out**
- Set `page: 0` and `size: 10`
- Click **Execute**

**Search by name:**
- Click **GET** `/api/cocktails/search`
- Click **Try it out**
- Fill in `nome: Margarita` and the pagination
- Click **Execute**

**Cocktail details:**
- Click **GET** `/api/cocktails/{id}`
- Click **Try it out**
- Enter the ID (e.g., `1`)
- Click **Execute**

**Create a cocktail (requires SOLDIER token):**
- Click **POST** `/api/cocktails`
- Click **Try it out**
- Fill in the body:
   ```json
   {
     "nome": "Margarita",
     "descrizione": "Classic Mexican cocktail",
     "tempoPreparazioneMinutes": 5,
     "note": "Serve cold",
     "ingredienti": [
       { "nome": "tequila", "quantita": 2, "unita": "oz" },
       { "nome": "lime juice", "quantita": 1, "unita": "oz" },
       { "nome": "triple sec", "quantita": 0.5, "unita": "oz" }
     ],
     "preparazione": "Shake with ice and serve"
   }
   ```
- Click **Execute**

**Update a cocktail:**
- Click **PUT** `/api/cocktails/{id}`
- Click **Try it out**
- Enter the ID
- Fill in the body with the updated data
- Click **Execute**

**Delete a cocktail:**
- Click **DELETE** `/api/cocktails/{id}`
- Click **Try it out**
- Enter the ID
- Click **Execute**

**View ingredients:**
- Click **GET** `/api/cocktails/ingredients`
- Click **Try it out**
- Set the pagination
- Click **Execute**

#### 4) Managing Favorites via Swagger (authentication required)

**List favorites:**
- Click **GET** `/api/favoriti`
- Click **Try it out** → **Execute**

**Add to favorites:**
- Click **POST** `/api/favoriti/{cocktailId}`
- Click **Try it out**
- Enter `cocktailId` (e.g., `1`)
- Click **Execute**

**Remove from favorites:**
- Click **DELETE** `/api/favoriti/{cocktailId}`
- Click **Try it out**
- Enter `cocktailId`
- Click **Execute**

**Toggle favorite:**
- Click **POST** `/api/favoriti/toggle/{cocktailId}`
- Click **Try it out**
- Enter `cocktailId`
- Click **Execute**

**Check whether it's in favorites:**
- Click **GET** `/api/favoriti/check/{cocktailId}`
- Click **Try it out**
- Enter `cocktailId`
- Click **Execute**

**Favorites count:**
- Click **GET** `/api/favoriti/count`
- Click **Try it out** → **Execute**

---

### 📟 Option 2: With cURL (from the terminal)

Below is a complete guide to using the project via curl. Note for Windows PowerShell: to set an environment variable in the session, use `$env:NAME = "value"`.

### 1) Login and obtaining the token (Keycloak)
Request:
```bash
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user1",
    "password": "password1"
  }'
```
Typical response:
```json
{
  "access_token": "eyJhbGciOi...",
  "expires_in": 300,
  "refresh_expires_in": 1800,
  "refresh_token": "...",
  "token_type": "Bearer",
  "scope": "profile email roles"
}
```
Save the token (PowerShell):
```powershell
$env:TOKEN = "<paste_access_token>"
```
Then send the Authorization header: `Authorization: Bearer $env:TOKEN`.

Optional – Registration:
```bash
curl -X POST "http://localhost:8080/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user1",
    "email": "user1@example.com",
    "firstName": "User",
    "lastName": "One",
    "password": "password1",
    "confirmPassword": "password1"
  }'
```

### 2) Cocktail Operations (CRUD)

- Paginated list (public):
```bash
curl "http://localhost:8080/api/cocktails?page=0&size=10"
```

- Search by name (public):
```bash
curl "http://localhost:8080/api/cocktails/search?nome=Margarita&page=0&size=5"
```

- Details by ID (public):
```bash
curl "http://localhost:8080/api/cocktails/1"
```

- Create a cocktail (requires the SOLDIER role):
```bash
curl -X POST "http://localhost:8080/api/cocktails" \
  -H "Authorization: Bearer $env:TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Margarita",
    "descrizione": "Classic Mexican cocktail",
    "tempoPreparazioneMinutes": 5,
    "note": "Serve cold",
    "ingredienti": [
      { "nome": "tequila", "quantita": 2, "unita": "oz" },
      { "nome": "lime juice", "quantita": 1, "unita": "oz" },
      { "nome": "triple sec", "quantita": 0.5, "unita": "oz" }
    ],
    "preparazione": "Shake with ice and serve"
  }'
```

- Update a cocktail (requires the SOLDIER role):
```bash
curl -X PUT "http://localhost:8080/api/cocktails/1" \
  -H "Authorization: Bearer $env:TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "nome": "Margarita (upd)",
    "descrizione": "Updated version",
    "tempoPreparazioneMinutes": 5,
    "note": null,
    "preparazione": []
  }'
```

- Delete a cocktail (requires the SOLDIER role):
```bash
curl -X DELETE "http://localhost:8080/api/cocktails/1" \
  -H "Authorization: Bearer $env:TOKEN"
```

- Available ingredients (public, paginated):
```bash
curl "http://localhost:8080/api/cocktails/ingredients?page=0&size=10"
```

### 3) Managing Favorites (authentication required)

- The user's favorites list:
```bash
curl -H "Authorization: Bearer $env:TOKEN" "http://localhost:8080/api/favoriti"
```

- Add to favorites:
```bash
curl -X POST -H "Authorization: Bearer $env:TOKEN" \
  "http://localhost:8080/api/favoriti/1"
```

- Remove from favorites:
```bash
curl -X DELETE -H "Authorization: Bearer $env:TOKEN" \
  "http://localhost:8080/api/favoriti/1"
```

- Toggle favorite (adds if absent, removes if present):
```bash
curl -X POST -H "Authorization: Bearer $env:TOKEN" \
  "http://localhost:8080/api/favoriti/toggle/1"
```

- Check whether a cocktail is in the favorites:
```bash
curl -H "Authorization: Bearer $env:TOKEN" \
  "http://localhost:8080/api/favoriti/check/1"
```

- Favorites count:
```bash
curl -H "Authorization: Bearer $env:TOKEN" \
  "http://localhost:8080/api/favoriti/count"
```

## 🔐 Security Details

- Resource Server enabled: JWTs are validated through the `issuer-uri` and `jwk-set-uri` configured in `application.properties`.
- Roles: `ROLE_SOLDIER` is required for write operations on cocktails.
- Required header: `Authorization: Bearer <access_token>`.

Configuration excerpt (`src/main/resources/application.properties`):
```properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=${KEYCLOAK_ISSUER_URI:http://keycloak:8081/realms/cocktail-realm}
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=${KEYCLOAK_ISSUER_URI:http://keycloak:8081/realms/cocktail-realm}/protocol/openid-connect/certs
```
For local development without Docker, set `KEYCLOAK_ISSUER_URI` to `http://localhost:8081/realms/cocktail-realm` or modify the properties accordingly.

## 🔧 Local Development

### Local Build and Run
```bash
# Build (skips tests)
./mvnw clean package -DskipTests

# Run (requires MySQL on localhost:3306 with configured credentials)
./mvnw spring-boot:run

# Or run the JAR
java -jar target/cocktailproject-0.0.1-SNAPSHOT.jar
```

### Database Configuration (local)
Update `src/main/resources/application.properties` if you're not using Docker:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/init
spring.datasource.username=cocktail_user
spring.datasource.password=cocktail_pass
```

## 🐳 Docker

### Services (docker-compose.yml)
- `cocktail-app`: Spring Boot on 8080 (container `cocktail-java-app`)
- `mysql`: MySQL 8.0 on 3306 (container `cocktail-db`)
- `keycloak`: Keycloak 24.0.5 on 8081 (container `keycloak`, automatic realm import)

### Useful Commands
```bash
# Rebuild and restart
docker compose up -d --build

# Stop all services
docker compose down

# Also remove volumes
docker compose down -v

# Logs of a specific service
docker compose logs -f mysql
```

## 📝 Development Notes

### Configuration and Patterns
- **Dependency Injection:** Constructor injection on all services (immutability, testability)
- **Central Beans:** ObjectMapper and RestTemplate are defined as Spring Beans in SecurityConfig
- **JPA Hibernate:** Uses `spring.jpa.hibernate.ddl-auto=update` to update the schema automatically
- **DTOs and Conversions:** Use of constructors (Lombok @AllArgsConstructor) instead of setters
- **Pagination:** All endpoints that return lists are paginated with `Pageable`
  - `page`: page number (default 0)
  - `size`: items per page (default 10)
  - No legacy non-paginated methods, to avoid OOM with large datasets

### Performance and Optimizations
- **N+1 Query Fix:** Ingredients are loaded in a batch map during DTO conversion
- **Scalability:** Mandatory pagination prevents memory leaks
- **Reuse:** ObjectMapper and RestTemplate are shared (1 instance for the whole app)
- **Test Data:** Loaded automatically from `docker-entrypoint-initdb.d/init.sql`

### Security
- **CORS:** Enabled for all origins (`*`) - should be restricted in production
- **Health Check:** Configured on `/api/cocktails` in docker-compose
- **JWT Validation:** Keycloak as the identity provider, JWT validated on every protected request
- **Password Storage:** Handled by Keycloak, never in plain text

## 🤝 Contributing
Educational project to assess Java/Spring Boot skills. PRs and issues are welcome.

## 📄 License
Apache 2.0