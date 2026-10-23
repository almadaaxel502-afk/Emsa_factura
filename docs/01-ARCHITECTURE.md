# Architecture Documentation

## Overview

The Emsa_factura billing system follows a **layered architecture** pattern, separating concerns across presentation, business logic, and data persistence layers. This architecture promotes maintainability, testability, and clear separation of responsibilities.

### Architecture Layers

```
+---------------------+
|   Presentation      |
|   (REST API)        |
+---------------------+
|     Business        |
|     Logic (Service) |
+---------------------+
|      Data           |
|    Access (Repo)    |
+---------------------+
|   Domain Model      |
|     (Entities)      |
+---------------------+
```

## Layer Details

### 1. Presentation Layer

**Location:** `src/main/java/ar/com/itec1misiones/emsa/controller/`

**Responsibilities:**
- Handle HTTP requests and responses
- Validate request parameters
- Translate HTTP concerns (status codes, headers) to business concerns
- No business logic - delegates to service layer

**Key Components:**
- `FacturacionController` - REST endpoints for billing operations

**Technology:**
- Spring Web (`@RestController`)
- Spring MVC request mapping

### 2. Business Logic Layer

**Location:** `src/main/java/ar/com/itec1misiones/emsa/service/`

**Responsibilities:**
- Implement business rules and workflows
- Coordinate between repositories
- Perform calculations (consumption, tariffs, taxes)
- Transaction management (`@Transactional`)

**Key Services:**
- `FacturacionService` - Billing operations and calculations
- `ClienteService` - Customer management operations
- `LecturaService` - Meter reading registration and validation
- `MedidorService` - Meter management operations
- `ImpuestoService` - Tax catalog management
- `TarifaService` - Tariff catalog management
- `TipoIVAService` - VAT type management
- `LoginService` - User authentication

**Technology:**
- Spring Service (`@Service`)
- Spring Transaction Management (`@Transactional`)

### 3. Data Access Layer

**Location:** `src/main/java/ar/com/itec1misiones/emsa/repository/`

**Responsibilities:**
- Database CRUD operations
- Custom queries (using JPQL)
- Repository pattern implementation

**Key Repositories:**
- `ClienteRepository` - Customer data access
- `MedidorRepository` - Meter data access
- `LecturaRepository` - Reading data access
- `FacturaRepository` - Invoice data access
- `ImpuestoRepository` - Tax catalog access
- `TarifaRepository` - Tariff catalog access
- `TipoIVARepository` - VAT type access
- `UsuarioRepository` - User data access
- `OperadorRepository` - Operator data access
- Additional repositories for reference data (Categoria, Zona, Dispositivo)

**Technology:**
- Spring Data JPA (`@Repository`)
- JPA/Hibernate annotations

### 4. Domain Model Layer

**Location:** `src/main/java/ar/com/itec1misiones/emsa/entity/`

**Responsibilities:**
- Represent business entities
- Encapsulate domain concepts
- Define relationships between entities

**Key Entities:**
- `Cliente` - Customer entity
- `Medidor` - Electricity meter entity
- `Lectura` - Meter reading entity
- `Factura` - Invoice entity
- `ItemConsumo` - Consumption line item
- `ItemImpuesto` - Tax line item
- `Tarifa` - Tariff rate entity
- `Impuesto` - Tax entity
- `TipoIVA` - VAT type entity
- `Operador` - System operator (extends Usuario)
- `Usuario` - Base user entity
- `Categoria` - Meter category
- `Zona` - Meter zone
- `Dispositivo` - Device associated with meter

**Technology:**
- JPA/Hibernate annotations
- Lombok for boilerplate reduction

## Component Relationships

### Dependency Flow

```
Controller → Service → Repository → Entity
```

- Controllers depend on Services
- Services depend on Repositories
- Repositories depend on Entities
- DTOs are used for data transfer between layers

### Data Flow Example: Invoice Generation

1. **Controller receives request**
   ```
   POST /api/facturas/liquidar-lectura
   ```

2. **Service layer processes**
   - Validates meter exists
   - Validates reading is not duplicate
   - Retrieves previous reading
   - Calculates consumption
   - Applies tariff blocks
   - Calculates taxes

3. **Repository layer persists**
   - Saves reading (if new)
   - Saves invoice with all line items

4. **Response returned**
   - Invoice DTO with all details

## Spring Boot Integration

### Configuration Patterns

**Configuration Classes:**
- `SecurityConfig` - Security configuration
- `AppContextProvider` - Application context provider
- `DataInitializer` - Initial data seeding

**Component Scanning:**
- Spring Boot auto-scans `ar.com.itec1misiones.emsa` package
- All `@Component`, `@Service`, `@Repository`, `@RestController` detected

### Dependency Injection

- Constructor injection preferred for dependencies
- Final fields for immutability where possible
- Spring-managed beans with `@Service`, `@Repository`

## Security Architecture

**Current Implementation:**
- Spring Security configured
- CSRF disabled (API-focused)
- All endpoints currently permitAll (for development)

**Security Configuration:**
```java
http.csrf(c -> c.disable())
   .authorizeHttpRequests(a -> a.anyRequest().permitAll());
```

**For Production:**
- Enable CSRF protection for browser clients
- Implement authentication (JWT, Session, OAuth2)
- Configure role-based authorization
- Add input validation at controller level

## Technology Stack

### Core Framework
- **Spring Boot:** 3.5.5
- **Java:** 17

### Data Access
- **Spring Data JPA:** ORM abstraction
- **Hibernate:** JPA provider
- **MySQL:** 8.3 (production)
- **H2:** In-memory database (development/testing)

### Build Tools
- **Maven:** Project management and build

### Additional Libraries
- **Lombok:** Reduces boilerplate code
- **JavaFX:** Desktop UI components

## Deployment Architecture

### Docker Setup

**Services:**
- MySQL database (port 3307 mapped to 3306)
- Application (runs on configured port)

**Network:**
- Custom bridge network for service communication
- Named volume for persistent data

### Environment Requirements

- Java 17 or higher
- Maven 3.6+ or Maven Wrapper
- MySQL 8.0+ (or use Docker Compose)

## Configuration

### Application Properties

Database configuration should be in `application.properties` or `application.yml`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3307/emsa
spring.datasource.username=emsa
spring.datasource.password=emsa
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

### Profile-Specific Configuration

- `application-dev.yml` - Development settings
- `application-prod.yml` - Production settings
- Profile activated via `spring.profiles.active`

## Error Handling Strategy

### Exception Hierarchy

- **DataIntegrityViolationException:** Database constraint violations
- **DuplicateKeyException:** Duplicate entry errors
- **EntityNotFoundException:** Entity not found in database

### Error Response Format

Use consistent error response structure:

```json
{
  "timestamp": "2026-10-23T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid meter reading",
  "path": "/api/facturas/liquidar-lectura"
}
```

## API Documentation Strategy

### OpenAPI/Swagger Integration

Add Springdoc OpenAPI dependency:

```xml
<dependency>
    <groupId>org.springdoc.org.springframework.boot</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>3.0.0</version>
</dependency>
```

Access documentation at: `/swagger-ui.html`

### Documenting Endpoints

Use OpenAPI annotations for detailed endpoint documentation:

```java
@Operation(summary = "Register meter reading and generate invoice")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Invoice generated"),
    @ApiResponse(responseCode = "400", description = "Invalid input")
})
```

## Testing Strategy

### Unit Tests

- Service layer: Test business logic in isolation
- Repository layer: Test query logic
- Controller layer: Test request mapping and validation

### Integration Tests

- Full stack tests with test database
- Repository tests with actual database
- End-to-end API tests

## Maintenance Considerations

### Code Organization

- Keep entities focused on data and relationships
- Keep services focused on business logic
- Keep controllers focused on HTTP translation
- Use DTOs to avoid exposing internal entities

### Database Changes

- Use Flyway or Liquibase for migrations (recommended)
- Or use `spring.jpa.hibernate.ddl-auto=update` for development
- Never use `update` in production

### Performance Considerations

- Use `@Transactional(readOnly = true)` for read-only operations
- Index frequently queried columns
- Consider pagination for large result sets
- Use Lazy loading appropriately

## Summary

The Emsa_factura system uses a clean layered architecture with clear separation of concerns. This design allows for:

- Easy testing of individual layers
- Clear understanding of data flow
- Maintainable code organization
- Scalable architecture for future enhancements