# API Documentation

## Overview

This document describes all REST API endpoints for the Emsa_factura billing system. The API follows REST conventions and returns JSON responses.

## Base URL

```
/api
```

## Authentication

**Current Implementation:** No authentication required (development mode)

**Production Implementation:** Should use JWT tokens or session-based authentication

## Error Response Format

```json
{
  "timestamp": "2026-10-23T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Error message describing what went wrong",
  "path": "/api/endpoint"
}
```

---

## Endpoints

### 1. List All Invoices

**Endpoint:** `GET /api/facturas`

**Description:** Retrieves all invoices in the system, ordered by date descending.

**Authentication:** None

**Request Parameters:** None

**Response:**
```json
[
  {
    "id": 1,
    "fecha": "2026-10-23",
    "clienteId": 123,
    "clienteNombre": "Juan Perez",
    "medidorId": 456,
    "medidorNroSerie": "MED001234",
    "kwhTotales": 150.5,
    "subtotal": 4515.0,
    "importeIVA": 948.15,
    "total": 5463.15,
    "itemsConsumo": [
      {
        "descripcion": "Primer Tramo",
        "cantidad": 100.0,
        "tasa": 30.0,
        "subtotal": 3000.0
      },
      {
        "descripcion": "Segundo Tramo",
        "cantidad": 50.5,
        "tasa": 30.0,
        "subtotal": 1515.0
      }
    ],
    "itemsImpuesto": [
      {
        "descripcion": "IVA",
        "cantidad": 4515.0,
        "tasa": 21.0,
        "importe": 948.15
      }
    ]
  }
]
```

**HTTP Status Codes:**
- `200 OK` - Success
- `500 Internal Server Error` - Server error

---

### 2. List Invoices by Customer

**Endpoint:** `GET /api/facturas/cliente/{id}`

**Description:** Retrieves all invoices for a specific customer.

**Authentication:** None

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | Long | Yes | Customer ID |

**Response:**
```json
[
  {
    "id": 1,
    "fecha": "2026-10-23",
    "clienteId": 123,
    "clienteNombre": "Juan Perez",
    "medidorId": 456,
    "medidorNroSerie": "MED001234",
    "kwhTotales": 150.5,
    "subtotal": 4515.0,
    "importeIVA": 948.15,
    "total": 5463.15,
    "itemsConsumo": [...],
    "itemsImpuesto": [...]
  }
]
```

**HTTP Status Codes:**
- `200 OK` - Success
- `404 Not Found` - Customer not found
- `500 Internal Server Error` - Server error

---

### 3. Register Meter Reading and Generate Invoice

**Endpoint:** `POST /api/facturas/liquidar-lectura`

**Description:** Registers a meter reading and generates an invoice for the billing period.

**Authentication:** None

**Request Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| nroSerie | String | Yes | Meter serial number |
| valor | BigDecimal | Yes | Current meter reading (cumulative kWh) |
| fecha | LocalDate | No | Reading date (default: today) |

**Request Example:**
```
POST /api/facturas/liquidar-lectura?nroSerie=MED001234&valor=1520.5&fecha=2026-10-23
```

**Response:**
```json
{
  "id": 1,
  "fecha": "2026-10-23",
  "clienteId": 123,
  "clienteNombre": "Juan Perez",
  "medidorId": 456,
  "medidorNroSerie": "MED001234",
  "kwhTotales": 150.5,
  "subtotal": 4515.0,
  "importeIVA": 948.15,
  "total": 5463.15,
  "itemsConsumo": [...],
  "itemsImpuesto": [...]
}
```

**Business Logic:**
1. Validates meter exists by serial number
2. Checks for duplicate readings on same date
3. Retrieves previous reading
4. Calculates consumption (current - previous)
5. Allocates consumption to tariff blocks
6. Calculates taxes based on customer's VAT type
7. Saves reading and invoice
8. Returns invoice DTO

**HTTP Status Codes:**
- `200 OK` - Success
- `400 Bad Request` - Invalid input (missing parameters, invalid meter)
- `409 Conflict` - Duplicate reading on same date
- `404 Not Found` - Meter not found
- `500 Internal Server Error` - Server error

---

### 4. Generate Invoice from Last Reading

**Endpoint:** `POST /api/facturas/liquidar-ultima`

**Description:** Generates an invoice based on the most recent reading for a meter.

**Authentication:** None

**Request Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| nroSerie | String | Yes | Meter serial number |

**Request Example:**
```
POST /api/facturas/liquidar-ultima?nroSerie=MED001234
```

**Response:**
```json
{
  "id": 1,
  "fecha": "2026-10-23",
  "clienteId": 123,
  "clienteNombre": "Juan Perez",
  "medidorId": 456,
  "medidorNroSerie": "MED001234",
  "kwhTotales": 150.5,
  "subtotal": 4515.0,
  "importeIVA": 948.15,
  "total": 5463.15,
  "itemsConsumo": [...],
  "itemsImpuesto": [...]
}
```

**Business Logic:**
1. Validates meter exists
2. Retrieves most recent reading
3. Calculates consumption from previous reading
4. Generates invoice as per normal billing process

**HTTP Status Codes:**
- `200 OK` - Success
- `400 Bad Request` - Invalid input
- `404 Not Found` - Meter or reading not found
- `500 Internal Server Error` - Server error

---

### 5. Get Billing Period Information

**Endpoint:** `GET /api/periodo`

**Description:** Retrieves the current billing period information for a meter.

**Authentication:** None

**Request Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| nroSerie | String | Yes | Meter serial number |

**Request Example:**
```
GET /api/periodo?nroSerie=MED001234
```

**Response:**
```json
{
  "medidorId": 456,
  "medidorNroSerie": "MED001234",
  "clienteId": 123,
  "clienteNombre": "Juan Perez",
  "tipoIVADescripcion": "Consumidor Final",
  "tipoIVATasa": 21.0,
  "lecturaActual": {
    "id": 789,
    "fecha": "2026-10-23",
    "valor": 1520.5,
    "medidorId": 456,
    "medidorNroSerie": "MED001234"
  },
  "lecturaAnterior": {
    "id": 788,
    "fecha": "2023-12-15",
    "valor": 1370.0,
    "medidorId": 456,
    "medidorNroSerie": "MED001234"
  },
  "kwhPeriodo": 150.5,
  "primeraLiquidacion": false
}
```

**Fields:**
- `lecturaActual` - Most recent reading
- `lecturaAnterior` - Previous reading (null for first billing)
- `kwhPeriodo` - Calculated consumption
- `primeraLiquidacion` - true if no previous reading exists

**HTTP Status Codes:**
- `200 OK` - Success
- `404 Not Found` - Meter not found
- `500 Internal Server Error` - Server error

---

### 6. List Customers

**Endpoint:** `GET /api/clientes`

**Description:** Searches for customers by name.

**Authentication:** None

**Request Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| nombre | String | No | Search term (partial match) |

**Request Examples:**
```
GET /api/clientes
GET /api/clientes?nombre=juan
```

**Response:**
```json
[
  {
    "id": 123,
    "nombre": "Juan",
    "apellido": "Perez",
    "nombreCompleto": "Juan Perez",
    "identificacionNacional": "12345678",
    "direccion": "Av. Siempre Viva 123",
    "tipoIVAId": 1,
    "tipoIVADescripcion": "Consumidor Final",
    "tipoIVATasa": 21.0,
    "medidores": ["MED001234", "MED005678"]
  }
]
```

**HTTP Status Codes:**
- `200 OK` - Success
- `500 Internal Server Error` - Server error

---

### 7. Get Customer by Meter

**Endpoint:** `GET /api/clientes/por-medidor`

**Description:** Retrieves customer information for a specific meter.

**Authentication:** None

**Request Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| nroSerie | String | Yes | Meter serial number |

**Request Example:**
```
GET /api/clientes/por-medidor?nroSerie=MED001234
```

**Response:**
```json
{
  "id": 123,
  "nombre": "Juan",
  "apellido": "Perez",
  "nombreCompleto": "Juan Perez",
  "identificacionNacional": "12345678",
  "direccion": "Av. Siempre Viva 123",
  "tipoIVAId": 1,
  "tipoIVADescripcion": "Consumidor Final",
  "tipoIVATasa": 21.0,
  "medidores": ["MED001234"]
}
```

**HTTP Status Codes:**
- `200 OK` - Success
- `404 Not Found` - Meter not found
- `500 Internal Server Error` - Server error

---

### 8. Register Meter Reading

**Endpoint:** `POST /api/lecturas`

**Description:** Registers a meter reading without generating an invoice.

**Authentication:** None

**Request Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| nroSerie | String | Yes | Meter serial number |
| valor | BigDecimal | Yes | Current meter reading (cumulative kWh) |
| fecha | LocalDate | No | Reading date (default: today) |

**Request Example:**
```
POST /api/lecturas?nroSerie=MED001234&valor=1520.5&fecha=2026-10-23
```

**Response:**
```json
"Lectura registrada."
```

**Business Logic:**
1. Validates meter exists
2. Checks for duplicate readings on same date
3. Validates reading is greater than previous
4. Saves reading to database

**HTTP Status Codes:**
- `200 OK` - Success
- `400 Bad Request` - Invalid input
- `409 Conflict` - Duplicate reading
- `404 Not Found` - Meter not found
- `500 Internal Server Error` - Server error

---

## DTO Schemas

### FacturaDTO

```json
{
  "id": 1,
  "fecha": "2026-10-23",
  "clienteId": 123,
  "clienteNombre": "Juan Perez",
  "medidorId": 456,
  "medidorNroSerie": "MED001234",
  "kwhTotales": 150.5,
  "subtotal": 4515.0,
  "importeIVA": 948.15,
  "total": 5463.15,
  "itemsConsumo": [...],
  "itemsImpuesto": [...]
}
```

### ClienteDTO

```json
{
  "id": 123,
  "nombre": "Juan",
  "apellido": "Perez",
  "nombreCompleto": "Juan Perez",
  "identificacionNacional": "12345678",
  "direccion": "Av. Siempre Viva 123",
  "tipoIVAId": 1,
  "tipoIVADescripcion": "Consumidor Final",
  "tipoIVATasa": 21.0,
  "medidores": ["MED001234"]
}
```

### MedidorDTO

```json
{
  "id": 456,
  "nroSerie": "MED001234",
  "numeroConexion": "123456",
  "direccion": "Av. Siempre Viva 123",
  "zona": "Zona Norte",
  "categoria": "Residencial",
  "dispositivo": "Smart Meter v1.0",
  "clienteId": 123,
  "clienteNombre": "Juan Perez"
}
```

### LecturaDTO

```json
{
  "id": 789,
  "fecha": "2026-10-23",
  "valor": 1520.5,
  "medidorId": 456,
  "medidorNroSerie": "MED001234"
}
```

### PeriodoFacturacionDTO

```json
{
  "medidorId": 456,
  "medidorNroSerie": "MED001234",
  "clienteId": 123,
  "clienteNombre": "Juan Perez",
  "tipoIVADescripcion": "Consumidor Final",
  "tipoIVATasa": 21.0,
  "lecturaActual": {...},
  "lecturaAnterior": {...},
  "kwhPeriodo": 150.5,
  "primeraLiquidacion": false
}
```

### ItemConsumoDTO

```json
{
  "descripcion": "Primer Tramo",
  "cantidad": 100.0,
  "tasa": 30.0,
  "subtotal": 3000.0
}
```

### ItemImpuestoDTO

```json
{
  "descripcion": "IVA",
  "cantidad": 4515.0,
  "tasa": 21.0,
  "importe": 948.15
}
```

---

## OpenAPI/Swagger Configuration

Add to `pom.xml`:

```xml
<dependency>
    <groupId>org.springdoc.org.springframework.boot</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>3.0.0</version>
</dependency>
```

Access documentation at:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

---

## Testing with cURL

### Example: Generate Invoice

```bash
curl -X POST "http://localhost:8080/api/facturas/liquidar-lectura?nroSerie=MED001234&valor=1520.5&fecha=2026-10-23"
```

### Example: List Invoices

```bash
curl -X GET "http://localhost:8080/api/facturas"
```

### Example: Search Customers

```bash
curl -X GET "http://localhost:8080/api/clientes?nombre=juan"
```

---

## Rate Limiting (Production)

For production, consider adding rate limiting:

```java
@Configuration
public class RateLimitingConfig {
    // Configure rate limiters for different endpoints
}
```

---

## Security Recommendations

1. **Enable Authentication:**
   - JWT tokens or session-based auth
   - Protect billing endpoints

2. **Add Input Validation:**
   - Validate meter serial numbers
   - Validate reading values are positive
   - Validate dates are reasonable

3. **Add Authorization:**
   - Operators can read/write
   - Admins have full access
   - Customers have read-only access to their data

4. **Add Audit Logging:**
   - Log billing operations
   - Log user actions
   - Track changes to critical data