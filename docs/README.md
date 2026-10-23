# Emsa_factura Documentation

## Overview

This directory contains comprehensive technical documentation for the Emsa_factura electricity billing system. The documentation covers architecture, domain model, API, database schema, workflows, deployment, business rules, and troubleshooting.

## Documentation Structure

| Document | Description | Audience | Last Updated |
|----------|-------------|----------|--------------|
| [01-ARCHITECTURE.md](01-ARCHITECTURE.md) | System architecture, layers, and component relationships | Developers | 2026-10-23 |
| [02-DOMAIN-MODEL.md](02-DOMAIN-MODEL.md) | Entity relationships, business rules, and domain concepts | Developers | 2026-10-23 |
| [03-API-DOCUMENTATION.md](03-API-DOCUMENTATION.md) | REST API endpoints, parameters, and responses | Developers | 2026-10-23 |
| [04-DATABASE-SCHEMA.md](04-DATABASE-SCHEMA.md) | Database tables, constraints, and relationships | DBAs, Developers | 2026-10-23 |
| [05-WORKFLOWS.md](05-WORKFLOWS.md) | Business workflows and operational procedures | Operations | 2026-10-23 |
| [06-DEPLOYMENT.md](06-DEPLOYMENT.md) | Setup, configuration, and deployment guides | DevOps | 2026-10-23 |
| [07-BUSINESS-RULES.md](07-BUSINESS-RULES.md) | Business rules and validation logic | Business Analysts | 2026-10-23 |
| [08-TROUBLESHOOTING.md](08-TROUBLESHOOTING.md) | Troubleshooting and maintenance procedures | Operations | 2026-10-23 |
| [09-PLANTUML-DIAGRAMS.md](09-PLANTUML-DIAGRAMS.md) | UML diagrams in PlantUML format | Architects, Developers | 2026-10-23 |

## Quick Start Guide

### For Developers

1. **Read Architecture** - Understand system organization
2. **Review Domain Model** - Learn entity relationships
3. **Explore API** - See available endpoints
4. **Check Workflows** - Understand business processes

### For Database Administrators

1. **Review Schema** - Understand table structure
2. **Check Constraints** - Learn business rules
3. **Review Indexes** - Understand query optimization
4. **Understand Relationships** - See data dependencies

### For Operations Team

1. **Read Workflows** - Learn operational procedures
2. **Check Deployment** - Understand setup process
3. **Review Troubleshooting** - Know common issues
4. **Consult Business Rules** - Understand validation

### For DevOps Engineers

1. **Review Deployment** - Understand infrastructure
2. **Check Configuration** - Learn environment setup
3. **Monitor Health** - Set up monitoring
4. **Backup Data** - Implement backup strategy

## Technology Stack

### Backend
- **Spring Boot** 3.5.5
- **Java** 17
- **Spring Data JPA**
- **Hibernate**
- **Spring Security**

### Database
- **MySQL** 8.3
- **JPA/Hibernate** for ORM
- **Flyway/Liquibase** for migrations (recommended)

### Build Tools
- **Maven** 3.6+
- **Docker** / **Docker Compose**

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+ or Maven Wrapper
- MySQL 8.0+ (or use Docker)

### Quick Setup (Docker)

```bash
# Clone and navigate
git clone <repository-url>
cd Emsa_factura

# Start MySQL
docker-compose up -d

# Build and run application
./mvnw spring-boot:run
```

### Quick Setup (Manual)

```bash
# Clone repository
git clone <repository-url>
cd Emsa_factura

# Create database
mysql -u root -p -e "CREATE DATABASE emsa; CREATE USER 'emsa'@'localhost' IDENTIFIED BY 'emsa'; GRANT ALL PRIVILEGES ON emsa.* TO 'emsa'@'localhost';"

# Configure application
cp src/main/resources/application-example.yml src/main/resources/application.yml

# Build and run
./mvnw clean package
java -jar target/emsa-factura-0.0.1-SNAPSHOT.jar
```

## API Documentation

### Base URL

```
http://localhost:8080/api
```

### Key Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/facturas` | List all invoices |
| GET | `/api/facturas/cliente/{id}` | List invoices by customer |
| POST | `/api/facturas/liquidar-lectura` | Generate invoice from reading |
| GET | `/api/periodo` | Get billing period info |
| GET | `/api/clientes` | Search customers |
| POST | `/api/lecturas` | Register meter reading |

### API Endpoints

See [03-API-DOCUMENTATION.md](03-API-DOCUMENTATION.md) for complete API documentation.

## Database Schema

### Key Tables

| Table | Description |
|-------|-------------|
| `clientes` | Customer information |
| `medidores` | Electricity meters |
| `lecturas` | Meter readings |
| `facturas` | Invoices |
| `items_consumo` | Consumption line items |
| `items_impuesto` | Tax line items |
| `tarifas` | Tariff rates |
| `impuestos` | Tax catalog |
| `tipos_iva` | VAT types |

### Database Schema

See [04-DATABASE-SCHEMA.md](04-DATABASE-SCHEMA.md) for complete schema documentation.

## Business Rules

### Key Rules

1. **Reading Validation:** Readings must be positive and greater than previous
2. **Consumption Calculation:** Consumption = Current Reading - Previous Reading
3. **Invoice Generation:** One invoice per meter per billing period
4. **Tax Calculation:** VAT = Subtotal × (VAT Rate / 100)
5. **No Duplicates:** One reading per meter per date

### Business Rules

See [07-BUSINESS-RULES.md](07-BUSINESS-RULES.md) for complete business rules.

## Troubleshooting

### Common Issues

| Issue | Symptom | Solution |
|-------|---------|----------|
| Database Connection | Connection refused | Check MySQL is running |
| Negative Consumption | Calculation error | Check reading sequence |
| Duplicate Invoice | Conflict error | Check existing invoice |
| Slow Queries | Performance issue | Add indexes |

### Troubleshooting Guide

See [08-TROUBLESHOOTING.md](08-TROUBLESHOOTING.md) for detailed troubleshooting procedures.

## Diagrams

### UML Diagrams

The documentation includes PlantUML diagrams for:

- **Entity-Relationship Diagram** - Database schema
- **Class Diagram** - Object model
- **Sequence Diagrams** - Key workflows
- **Component Diagram** - Architecture
- **Deployment Diagram** - Infrastructure

### View Diagrams

1. **Online:** Visit [PlantUML Live](https://www.plantuml.com/plantuml/)
2. **VS Code:** Install PlantUML extension
3. **CLI:** Use plantuml command line tool

### PlantUML Diagrams

See [09-PLANTUML-DIAGRAMS.md](09-PLANTUML-DIAGRAMS.md) for all diagrams.

## Configuration

### Environment Variables

```bash
# Database
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/emsa
SPRING_DATASOURCE_USERNAME=emsa
SPRING_DATASOURCE_PASSWORD=emsa

# Application
SPRING_PROFILES_ACTIVE=dev
SERVER_PORT=8080
```

### Configuration Files

- `src/main/resources/application-dev.yml` - Development
- `src/main/resources/application-prod.yml` - Production
- `docker-compose.yml` - Docker setup

See [06-DEPLOYMENT.md](06-DEPLOYMENT.md) for complete configuration guide.

## Contributing

### Adding Documentation

1. Add new documentation to appropriate section
2. Update this README if needed
3. Test diagram rendering
4. Verify links work correctly

### Updating Documentation

1. Update relevant document
2. Check for cross-references
3. Update table of contents if needed
4. Verify accuracy

## Support

### Getting Help

- **Documentation Issues:** Check relevant document section
- **Code Issues:** Check source code comments
- **Configuration:** See deployment guide
- **Troubleshooting:** See troubleshooting guide

### Reporting Issues

1. Check existing documentation
2. Review troubleshooting guide
3. Check error logs
4. Provide clear reproduction steps

## License

This documentation is provided as-is for the Emsa_factura project.

## Changelog

### 2026-10-23
- Initial documentation release
- Complete architecture documentation
- Full API reference
- Database schema documentation
- Business rules documentation
- Deployment and troubleshooting guides

---

**Last Updated:** 2026-10-23
**Version: 1.1.0
**Project:** Emsa_factura - Electricity Billing System