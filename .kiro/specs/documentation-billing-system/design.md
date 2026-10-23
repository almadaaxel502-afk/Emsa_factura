# Design Document

## Overview

This document describes the technical design for comprehensive documentation of the Emsa_factura billing system. The documentation serves two primary audiences: developers (for code maintenance and extension) and operations/support teams (for system deployment, troubleshooting, and day-to-day operations).

**Validates: Requirements 1-10**

## Architecture

### Documentation Structure

The documentation will be organized as a set of markdown files in the `docs/` directory:

```
docs/
├── README.md                      # Main documentation index
├── 01-ARCHITECTURE.md             # System architecture
├── 02-DOMAIN-MODEL.md             # Domain model and entities
├── 03-API-DOCUMENTATION.md        # REST API documentation
├── 04-DATABASE-SCHEMA.md          # Database schema documentation
├── 05-WORKFLOWS.md                # Business workflows
├── 06-DEPLOYMENT.md               # Deployment and setup
├── 07-BUSINESS-RULES.md           # Business rules
├── 08-TROUBLESHOOTING.md            # Troubleshooting guide
└── 09-PLANTUML-DIAGRAMS.md        # UML diagrams
```

### Documentation Delivery

- **Format:** Markdown files
- **Diagrams:** PlantUML format
- **Location:** `docs/` directory
- **Access:** Direct file access or web deployment

## Components and Interfaces

### Documentation Components

#### 1. Architecture Documentation Component

**Responsibilities:**
- Document layered architecture (Controller, Service, Repository, Entity)
- Explain component relationships
- Document Spring Boot integration
- Document security configuration

**Inputs:**
- Source code structure
- Configuration files
- Component relationships

**Outputs:**
- `docs/01-ARCHITECTURE.md`

---

#### 2. Domain Model Documentation Component

**Responsibilities:**
- Document all entities and their attributes
- Document entity relationships (compositions and aggregations)
- Document business rules for entities
- Document DTOs for API responses

**Inputs:**
- Entity classes
- Relationship annotations
- Business logic in services

**Outputs:**
- `docs/02-DOMAIN-MODEL.md`
- ERD diagram (PlantUML)

---

#### 3. API Documentation Component

**Responsibilities:**
- Document all REST endpoints
- Document request/response formats
- Document authentication
- Document error responses

**Inputs:**
- Controller classes
- DTO classes
- Request mappings

**Outputs:**
- `docs/03-API-DOCUMENTATION.md`
- Example cURL commands
- OpenAPI/Swagger configuration

---

#### 4. Database Schema Documentation Component

**Responsibilities:**
- Document table structures
- Document constraints and indexes
- Document relationships
- Document queries and optimization

**Inputs:**
- Entity classes with JPA annotations
- Repository queries
- Database configuration

**Outputs:**
- `docs/04-DATABASE-SCHEMA.md`
- ERD diagram (PlantUML)
- Example SQL queries

---

#### 5. Workflow Documentation Component

**Responsibilities:**
- Document meter reading registration
- Document invoice generation
- Document billing period inquiry
- Document troubleshooting procedures

**Inputs:**
- Service layer logic
- Business workflows
- Error handling patterns

**Outputs:**
- `docs/05-WORKFLOWS.md`
- Sequence diagrams (PlantUML)

---

#### 6. Deployment Documentation Component

**Responsibilities:**
- Document environment setup
- Document Docker configuration
- Document database migration
- Document configuration options

**Inputs:**
- Docker Compose file
- Configuration files
- Dependency information

**Outputs:**
- `docs/06-DEPLOYMENT.md`
- Example configurations

---

#### 7. Business Rules Documentation Component

**Responsibilities:**
- Document reading validation rules
- Document consumption calculation
- Document invoice generation rules
- Document tariff application

**Inputs:**
- Service layer validation
- Business logic
- Domain constraints

**Outputs:**
- `docs/07-BUSINESS-RULES.md`
- Validation matrix

---

#### 8. Troubleshooting Documentation Component

**Responsibilities:**
- Document common database issues
- Document billing calculation errors
- Document performance issues
- Document maintenance procedures

**Inputs:**
- Error handling patterns
- Log files
- Common issues from operations

**Outputs:**
- `docs/08-TROUBLESHOOTING.md`
- Diagnostic commands

---

#### 9. UML Diagrams Component

**Responsibilities:**
- Generate ERD diagram
- Generate class diagram
- Generate sequence diagrams
- Generate component/deployment diagrams

**Inputs:**
- Entity relationships
- Class structure
- Workflow information

**Outputs:**
- `docs/09-PLANTUML-DIAGRAMS.md`
- Individual PlantUML files

---

### Component Relationships

```
Documentation Generator
    ├── Architecture Component → 01-ARCHITECTURE.md
    ├── Domain Model Component → 02-DOMAIN-MODEL.md
    ├── API Component → 03-API-DOCUMENTATION.md
    ├── Database Component → 04-DATABASE-SCHEMA.md
    ├── Workflow Component → 05-WORKFLOWS.md
    ├── Deployment Component → 06-DEPLOYMENT.md
    ├── Business Rules Component → 07-BUSINESS-RULES.md
    ├── Troubleshooting Component → 08-TROUBLESHOOTING.md
    └── UML Diagrams Component → 09-PLANTUML-DIAGRAMS.md
```

## Data Models

### Documentation Data Models

#### Architecture Documentation Data

```json
{
  "layers": [
    {
      "name": "Presentation",
      "description": "REST API layer",
      "components": ["FacturacionController"]
    },
    {
      "name": "Business Logic",
      "description": "Service layer",
      "components": ["FacturacionService", "LecturaService"]
    },
    {
      "name": "Data Access",
      "description": "Repository layer",
      "components": ["FacturaRepository", "LecturaRepository"]
    },
    {
      "name": "Domain Model",
      "description": "Entity layer",
      "components": ["Cliente", "Medidor", "Factura"]
    }
  ],
  "dependencies": [
    {"from": "Controller", "to": "Service"},
    {"from": "Service", "to": "Repository"},
    {"from": "Repository", "to": "Entity"}
  ]
}
```

#### Domain Model Data

```json
{
  "entities": [
    {
      "name": "Cliente",
      "attributes": ["idCliente", "nombre", "apellido", "identificacionNacional", "direccion"],
      "relationships": [
        {"type": "COMPOSITION", "target": "TipoIVA", "cardinality": "1:1"},
        {"type": "AGGREGATION", "target": "Usuario", "cardinality": "1:1"},
        {"type": "AGGREGATION", "target": "Medidor", "cardinality": "1:*"}
      ]
    }
  ]
}
```

#### API Data

```json
{
  "endpoints": [
    {
      "method": "POST",
      "path": "/api/facturas/liquidar-lectura",
      "description": "Generate invoice from meter reading",
      "request": {
        "parameters": [
          {"name": "nroSerie", "type": "String", "required": true},
          {"name": "valor", "type": "BigDecimal", "required": true},
          {"name": "fecha", "type": "LocalDate", "required": false}
        ]
      },
      "response": {
        "type": "FacturaDTO",
        "description": "Generated invoice"
      }
    }
  ]
}
```

#### Database Schema Data

```json
{
  "tables": [
    {
      "name": "clientes",
      "columns": [
        {"name": "id_cliente", "type": "BIGINT", "constraints": ["PK", "AUTO_INCREMENT"]},
        {"name": "nombre", "type": "VARCHAR(255)", "constraints": ["NULL"]},
        {"name": "id_tipo_iva", "type": "BIGINT", "constraints": ["FK"]}
      ],
      "indexes": [
        {"name": "PRIMARY", "columns": ["id_cliente"]},
        {"name": "FK_clientes_tipo_iva", "columns": ["id_tipo_iva"]}
      ]
    }
  ]
}
```

## Correctness Properties

### Property 1: Documentation Completeness

*For any* documented feature, the documentation SHALL include architecture overview, component details, data models, and usage examples

**Validates: Requirements 1, 2, 3**

### Property 2: API Coverage

*For any* REST endpoint, the documentation SHALL specify the HTTP method, path, request parameters, and response format

**Validates: Requirements 2**

### Property 3: Database Schema Accuracy

*For any* database table, the documentation SHALL list all columns with data types and constraints

**Validates: Requirements 5**

### Property 4: Workflow Correctness

*For any* business workflow, the documentation SHALL describe the step-by-step process including preconditions and error handling

**Validates: Requirements 6**

### Property 5: Diagram Completeness

*For any* key workflow, the documentation SHALL include at least one UML diagram (sequence, class, or component)

**Validates: Requirements 7**

## Error Handling

### Documentation Generation Errors

| Error | Description | Resolution |
|-------|-------------|------------|
| Missing source data | Entity or controller not found | Verify source code exists |
| Invalid PlantUML syntax | Diagram rendering fails | Fix PlantUML syntax |
| Database connection error | Cannot access database | Verify database configuration |
| Permission denied | Cannot write to docs/ | Check directory permissions |

### Documentation Quality Errors

| Error | Description | Resolution |
|-------|-------------|------------|
| Incomplete documentation | Missing required sections | Add missing sections |
| Inconsistent terminology | Different terms for same concept | Standardize terminology |
| Outdated information | Documentation not matching code | Update documentation |

### Documentation Access Errors

| Error | Description | Resolution |
|-------|-------------|------------|
| File not found | Markdown file missing | Regenerate documentation |
| Invalid links | Broken cross-references | Fix link paths |
| Diagram not rendering | PlantUML fails | Check PlantUML syntax |

## Testing Strategy

### Documentation Validation

**Unit Tests:**

1. **Architecture Documentation Test**
   - Verify all layers documented
   - Verify component relationships documented

2. **API Documentation Test**
   - Verify all endpoints documented
   - Verify request/response formats documented

3. **Database Schema Test**
   - Verify all tables documented
   - Verify constraints and indexes documented

4. **Diagram Validation Test**
   - Verify PlantUML syntax is valid
   - Verify diagrams render correctly

**Integration Tests:**

1. **Complete Documentation Test**
   - Verify all documentation files exist
   - Verify cross-references work

2. **Documentation Generation Test**
   - Run documentation generation
   - Verify all outputs created

### Quality Checks

1. **Consistency Check**
   - Verify terminology consistency
   - Verify formatting consistency

2. **Completeness Check**
   - Verify all required sections present
   - Verify all components documented

3. **Accuracy Check**
   - Verify documentation matches code
   - Verify diagrams match schema

### Documentation Maintenance

1. **Update Process**
   - Update documentation with code changes
   - Review documentation regularly

2. **Version Control**
   - Track documentation changes
   - Document version history

### PlantUML Rendering Tests

1. **Syntax Validation**
   - Verify PlantUML files are valid
   - Test rendering with plantuml command

2. **Diagram Completeness**
   - Verify all required diagrams present
   - Verify diagrams cover key concepts

### Example Test Cases

**Test Case 1: Architecture Documentation**
```java
@Test
void testArchitectureDocumentation() {
    // Verify 01-ARCHITECTURE.md exists
    // Verify it contains layer descriptions
    // Verify it contains component relationships
}
```

**Test Case 2: API Coverage**
```java
@Test
void testApiCoverage() {
    // Find all REST controllers
    // Verify each endpoint documented in 03-API-DOCUMENTATION.md
}
```

**Test Case 3: Database Schema**
```java
@Test
void testDatabaseSchema() {
    // Find all entities
    // Verify each table documented in 04-DATABASE-SCHEMA.md
}
```