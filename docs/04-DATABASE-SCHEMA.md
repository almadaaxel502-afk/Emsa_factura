# Database Schema Documentation

## Overview

This document describes the database schema for the Emsa_factura billing system. The schema is implemented using JPA/Hibernate with MySQL as the database.

## Entity-Relationship Diagram

```plantuml
@startuml
' PlantUML Database Schema Diagram
' This diagram shows all tables, their columns, and relationships

left to right direction

' Tables
table clientes {
  + id_cliente : bigint [PK]
  - nombre : varchar(255) [nullable]
  - apellido : varchar(255) [nullable]
  - identificacion_nacional : varchar(255) [nullable]
  - direccion : varchar(255) [nullable]
  - id_tipo_iva : bigint [FK]
  - id_usuario : bigint [FK]
}

table usuarios {
  + id_usuario : bigint [PK]
  - nombre : varchar(255)
  - contrasena : varchar(255)
  - tipo_usuario : varchar(31) [Discriminator]
}

table medidores {
  + id_medidor : bigint [PK]
  - nro_serie : varchar(255) [nullable]
  - numero_conexion : varchar(255) [nullable]
  - direccion : varchar(255) [nullable]
  - id_zona : bigint [FK]
  - id_categoria : bigint [FK]
  - id_dispositivo : bigint [FK]
  - id_cliente : bigint [FK]
}

table lecturas {
  + id_lectura : bigint [PK]
  - fecha : date
  - valor : decimal(19,2)
  - id_medidor : bigint [FK]
}

table facturas {
  + id_factura : bigint [PK]
  - fecha : date
  - importe_iva : decimal(19,2)
  - subtotal : decimal(19,2)
  - total : decimal(19,2)
  - id_medidor : bigint [FK]
}

table items_consumo {
  + id_item_consumo : bigint [PK]
  - cantidad : decimal(19,2)
  - id_tarifa : bigint [FK]
  - id_factura : bigint [FK]
}

table items_impuesto {
  + id_item_impuesto : bigint [PK]
  - cantidad : decimal(19,2)
  - tasa_impuesto : decimal(5,2)
  - id_impuesto : bigint [FK]
  - id_factura : bigint [FK]
}

table tarifas {
  + id_tarifa : bigint [PK]
  - descripcion : varchar(255) [nullable]
  - tasa : decimal(19,2)
}

table impuestos {
  + id_impuesto : bigint [PK]
  - descripcion : varchar(255) [nullable]
  - tasa : decimal(5,2)
}

table tipos_iva {
  + id_tipo_iva : bigint [PK]
  - descripcion : varchar(255) [nullable]
  - tasa : decimal(5,2)
}

table zonas {
  + id_zona : bigint [PK]
  - codigo : varchar(255) [nullable]
  - descripcion : varchar(255) [nullable]
}

table categorias {
  + id_categoria : bigint [PK]
  - codigo : varchar(255) [nullable]
  - descripcion : varchar(255) [nullable]
}

table dispositivos {
  + id_dispositivo : bigint [PK]
  - descripcion : varchar(255) [nullable]
  - consumo : decimal(19,2) [nullable]
}

' Relationships
clients_bills: clientes ||--o{ facturas : "1:0..*"
medidores_bills: medidores ||--o{ facturas : "1:0..*"
medidores_readings: medidores ||--o{ lecturas : "1:0..*"
medidores_clientes: clientes }o--|| medidores : "1:1"
factura_items_consumo: facturas ||--o{ items_consumo : "1:0..*"
factura_items_impuesto: facturas ||--o{ items_impuesto : "1:0..*"
items_tarifas: tarifas }o--|| items_consumo : "1:1"
items_impuestos: impuestos }o--|| items_impuesto : "1:1"
clientes_tipos_iva: tipos_iva }o--|| clientes : "1:1"
medidores_zonas: zonas }o--|| medidores : "1:1"
medidores_categorias: categorias }o--|| medidores : "1:1"
medidores_dispositivos: dispositivos }o--|| medidores : "1:1"
usuarios_clientes: usuarios }o--|| clientes : "1:1"
usuarios_operadores: usuarios ||--|| operadores : "1:1"
operadores: usuarios ||--|{ operadores : "1:1"

' Notes
note right of clientes
  Cliente entities can have
  multiple Facturas
end note

note right of medidores
  Medidor entities can have
  multiple Lecturas and Facturas
end note
@enduml
```

---

## Table Structures

### 1. clientes (Customers)

**Description:** Stores customer information for the electricity billing system.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id_cliente | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique customer identifier |
| nombre | VARCHAR(255) | NULL | Customer's first name |
| apellido | VARCHAR(255) | NULL | Customer's last name |
| identificacion_nacional | VARCHAR(255) | NULL | National ID number |
| direccion | VARCHAR(255) | NULL | Customer address |
| id_tipo_iva | BIGINT | FOREIGN KEY | References tipos_iva |
| id_usuario | BIGINT | FOREIGN KEY, NULL | References usuarios |

**Indexes:**
- PRIMARY KEY on `id_cliente`
- FOREIGN KEY on `id_tipo_iva`
- FOREIGN KEY on `id_usuario`

**Relationships:**
- **1:1** → `tipos_iva` - Customer's VAT type (COMPOSITION)
- **1:1** → `usuarios` - Associated user account (AGGREGATION)
- **1:0..*** ← `medidores` - Customer's meters (AGGREGATION)
- **1:0..*** ← `facturas` - Customer's invoices (via medidores)

**Business Rules:**
- A customer must have a Tipo IVA assigned
- A customer can have zero or more meters
- A customer can have an associated user account

---

### 2. usuarios (Users)

**Description:** Base table for all system users using single table inheritance.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id_usuario | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique user identifier |
| nombre | VARCHAR(255) | NOT NULL | Username/login |
| contrasena | VARCHAR(255) | NOT NULL | Password (hashed in production) |
| tipo_usuario | VARCHAR(31) | NOT NULL, DISCRIMINATOR | 'OPERADOR' for operators |

**Indexes:**
- PRIMARY KEY on `id_usuario`
- UNIQUE on `nombre` (recommended for login)

**Relationships:**
- **1:0..*** ← `clientes` - Customer accounts (AGGREGATION)
- **1:1** → `operadores` - Operator subclass

**Inheritance:**
- Uses SINGLE_TABLE inheritance strategy
- `tipo_usuario` column distinguishes Usuario from Operador

**Business Rules:**
- Usuario is the base class
- Operador inherits from Usuario
- All users have username and password

---

### 3. medidores (Meters)

**Description:** Stores electricity meter information.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id_medidor | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique meter identifier |
| nro_serie | VARCHAR(255) | NULL | Serial number |
| numero_conexion | VARCHAR(255) | NULL | Connection number |
| direccion | VARCHAR(255) | NULL | Installation address |
| id_zona | BIGINT | FOREIGN KEY | References zonas |
| id_categoria | BIGINT | FOREIGN KEY | References categorias |
| id_dispositivo | BIGINT | FOREIGN KEY, NULL | References dispositivos |
| id_cliente | BIGINT | FOREIGN KEY | References clientes |

**Indexes:**
- PRIMARY KEY on `id_medidor`
- FOREIGN KEY on `id_zona`
- FOREIGN KEY on `id_categoria`
- FOREIGN KEY on `id_dispositivo`
- FOREIGN KEY on `id_cliente`

**Unique Constraints:**
- `nro_serie` should be unique (recommended)

**Relationships:**
- **1:1** → `zonas` - Meter zone (COMPOSITION)
- **1:1** → `categorias` - Meter category (COMPOSITION)
- **1:1** → `dispositivos` - Associated device (AGGREGATION)
- **1:1** → `clientes` - Owner customer (AGGREGATION)
- **1:0..*** ← `lecturas` - Meter readings (AGGREGATION)
- **1:0..*** ← `facturas` - Invoices (AGGREGATION)

**Business Rules:**
- A meter belongs to exactly one customer
- A meter has one zone and one category
- A meter can have zero or more readings
- A meter can have zero or more invoices

---

### 4. lecturas (Readings)

**Description:** Stores meter readings for consumption calculation.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id_lectura | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique reading identifier |
| fecha | DATE | NOT NULL | Reading date |
| valor | DECIMAL(19,2) | NOT NULL | Cumulative kWh reading |
| id_medidor | BIGINT | FOREIGN KEY | References medidores |

**Indexes:**
- PRIMARY KEY on `id_lectura`
- FOREIGN KEY on `id_medidor`
- INDEX on (`id_medidor`, `fecha`) for performance

**Relationships:**
- **1:1** → `medidores` - Associated meter (AGGREGATION)

**Business Rules:**
- Each reading is associated with one meter
- Readings are cumulative (not periodic)
- New reading must be ≥ previous reading (application validation)
- Date of reading must be ≥ previous reading date

---

### 5. facturas (Invoices)

**Description:** Stores generated electricity invoices.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id_factura | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique invoice identifier |
| fecha | DATE | NOT NULL | Billing period end date |
| importe_iva | DECIMAL(19,2) | NOT NULL | VAT amount |
| subtotal | DECIMAL(19,2) | NOT NULL | Subtotal before tax |
| total | DECIMAL(19,2) | NOT NULL | Total invoice amount |
| id_medidor | BIGINT | FOREIGN KEY | References medidores |

**Indexes:**
- PRIMARY KEY on `id_factura`
- FOREIGN KEY on `id_medidor`
- INDEX on (`id_medidor`, `fecha`) for performance

**Relationships:**
- **1:1** → `medidores` - Meter being billed (AGGREGATION)
- **1:0..*** ← `items_consumo` - Consumption line items (AGGREGATION)
- **1:0..*** ← `items_impuesto` - Tax line items (AGGREGATION)

**Business Rules:**
- Each invoice is for one meter
- Total = Subtotal + ImporteIVA
- Multiple consumption items can exist
- Multiple tax items can exist

---

### 6. items_consumo (Consumption Items)

**Description:** Stores consumption line items for invoices.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id_item_consumo | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique item identifier |
| cantidad | DECIMAL(19,2) | NOT NULL | kWh consumption in this block |
| id_tarifa | BIGINT | FOREIGN KEY | References tarifas |
| id_factura | BIGINT | FOREIGN KEY | References facturas |

**Indexes:**
- PRIMARY KEY on `id_item_consumo`
- FOREIGN KEY on `id_tarifa`
- FOREIGN KEY on `id_factura`

**Relationships:**
- **1:1** → `tarifas` - Tariff rate applied (COMPOSITION)
- **1:1** → `facturas` - Parent invoice (AGGREGATION)

**Business Rules:**
- Each item represents consumption at one tariff rate
- Subtotal = cantidad × tarifa.tasa
- Multiple items can exist per invoice (different tariff blocks)

---

### 7. items_impuesto (Tax Items)

**Description:** Stores tax line items for invoices.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id_item_impuesto | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique item identifier |
| cantidad | DECIMAL(19,2) | NOT NULL | Taxable base amount |
| tasa_impuesto | DECIMAL(5,2) | NOT NULL | Tax rate (percentage) |
| id_impuesto | BIGINT | FOREIGN KEY | References impuestos |
| id_factura | BIGINT | FOREIGN KEY | References facturas |

**Indexes:**
- PRIMARY KEY on `id_item_impuesto`
- FOREIGN KEY on `id_impuesto`
- FOREIGN KEY on `id_factura`

**Relationships:**
- **1:1** → `impuestos` - Tax type (COMPOSITION)
- **1:1** → `facturas` - Parent invoice (AGGREGATION)

**Business Rules:**
- Tax amount = cantidad × (tasa_impuesto / 100)
- Multiple tax items can exist per invoice
- Common taxes: IVA (21%), national taxes, provincial taxes

---

### 8. tarifas (Tariffs)

**Description:** Catalog of tariff rates for consumption.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id_tarifa | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique tariff identifier |
| descripcion | VARCHAR(255) | NULL | Tariff description |
| tasa | DECIMAL(19,2) | NOT NULL | Price per kWh |

**Indexes:**
- PRIMARY KEY on `id_tarifa`

**Relationships:**
- **1:0..*** ← `items_consumo` - Consumption items using this tariff

**Business Rules:**
- Each tariff represents a rate per kWh
- Multiple tariffs can be applied to an invoice
- Tariffs are application-level (not time-based in this schema)

---

### 9. impuestos (Taxes)

**Description:** Catalog of tax types that can be applied to invoices.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id_impuesto | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique tax identifier |
| descripcion | VARCHAR(255) | NULL | Tax description |
| tasa | DECIMAL(5,2) | NOT NULL | Tax rate (percentage) |

**Indexes:**
- PRIMARY KEY on `id_impuesto`

**Relationships:**
- **1:0..*** ← `items_impuesto` - Tax line items using this tax

**Business Rules:**
- Each tax represents a type (IVA, national, provincial)
- Tax rate stored as percentage (e.g., 21 for 21%)
- Multiple taxes can apply to an invoice

---

### 10. tipos_iva (VAT Types)

**Description:** Catalog of VAT types for customers.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id_tipo_iva | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique VAT type identifier |
| descripcion | VARCHAR(255) | NULL | VAT type description |
| tasa | DECIMAL(5,2) | NOT NULL | VAT rate (percentage) |

**Indexes:**
- PRIMARY KEY on `id_tipo_iva`

**Relationships:**
- **1:1** ← `clientes` - Customer's VAT type

**Business Rules:**
- Each customer has one VAT type
- Common types: Consumidor Final (21%), Comercio Registrado (10.5%)
- VAT rate applied to subtotal for invoice calculation

---

### 11. zonas (Zones)

**Description:** Geographical zones for meter classification.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id_zona | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique zone identifier |
| codigo | VARCHAR(255) | NULL | Zone code |
| descripcion | VARCHAR(255) | NULL | Zone description |

**Indexes:**
- PRIMARY KEY on `id_zona`

**Relationships:**
- **1:1** ← `medidores` - Meter's zone

**Business Rules:**
- Zones may affect tariff application
- Used for geographical classification

---

### 12. categorias (Categories)

**Description:** Categories for meter classification.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id_categoria | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique category identifier |
| codigo | VARCHAR(255) | NULL | Category code |
| descripcion | VARCHAR(255) | NULL | Category description |

**Indexes:**
- PRIMARY KEY on `id_categoria`

**Relationships:**
- **1:1** ← `medidores` - Meter's category

**Business Rules:**
- Categories may affect tariff application
- Used for meter classification (residential, commercial, industrial)

---

### 13. dispositivos (Devices)

**Description:** Devices associated with meters.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id_dispositivo | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique device identifier |
| descripcion | VARCHAR(255) | NULL | Device description |
| consumo | DECIMAL(19,2) | NULL | Typical consumption |

**Indexes:**
- PRIMARY KEY on `id_dispositivo`

**Relationships:**
- **1:1** ← `medidores` - Associated meter

**Business Rules:**
- Optional association with meter
- May store device-specific information

---

## Database Constraints

### Primary Keys

All tables have a single-column primary key using BIGINT AUTO_INCREMENT.

### Foreign Key Constraints

| Constraint Name | Source Table | Source Column | Target Table | Target Column |
|----------------|--------------|---------------|--------------|---------------|
| FK_... | medidores | id_zona | zonas | id_zona |
| FK_... | medidores | id_categoria | categorias | id_categoria |
| FK_... | medidores | id_dispositivo | dispositivos | id_dispositivo |
| FK_... | medidores | id_cliente | clientes | id_cliente |
| FK_... | facturas | id_medidor | medidores | id_medidor |
| FK_... | items_consumo | id_tarifa | tarifas | id_tarifa |
| FK_... | items_consumo | id_factura | facturas | id_factura |
| FK_... | items_impuesto | id_impuesto | impuestos | id_impuesto |
| FK_... | items_impuesto | id_factura | facturas | id_factura |
| FK_... | clientes | id_tipo_iva | tipos_iva | id_tipo_iva |
| FK_... | clientes | id_usuario | usuarios | id_usuario |

### Nullability

**Required fields (NOT NULL):**
- usuarios.nombre
- usuarios.contrasena
- usuarios.tipo_usuario
- lecturas.fecha
- lecturas.valor
- facturas.fecha
- facturas.importe_iva
- facturas.subtotal
- facturas.total
- items_consumo.cantidad
- items_impuesto.cantidad
- items_impuesto.tasa_impuesto

**Optional fields (NULL allowed):**
- All `descripcion` fields
- All `codigo` fields
- Foreign keys where relationships are optional

---

## Database Indexes

### Performance-Critical Indexes

| Index | Table | Columns | Purpose |
|-------|-------|---------|---------|
| PRIMARY KEY | clientes | id_cliente | Unique identification |
| PRIMARY KEY | medidores | id_medidor | Unique identification |
| PRIMARY KEY | lecturas | id_lectura | Unique identification |
| PRIMARY KEY | facturas | id_factura | Unique identification |
| FK_... | lecturas | id_medidor | Reading lookup by meter |
| FK_... | facturas | id_medidor | Invoice lookup by meter |
| FK_... | facturas | id_cliente | Invoice lookup by customer |
| COMPOSI | lecturas | id_medidor, fecha | Reading history query |
| COMPOSI | facturas | id_medidor, fecha | Invoice history query |

### Recommended Additional Indexes

```sql
-- For customer search
CREATE INDEX idx_clientes_nombre ON clientes(apellido, nombre);

-- For meter lookup by serial number
CREATE UNIQUE INDEX idx_medidores_nro_serie ON medidores(nro_serie);

-- For reading lookup by date range
CREATE INDEX idx_lecturas_fecha ON lecturas(fecha);
```

---

## Database Initialization

### Initial Data Setup

The `DataInitializer` class populates initial catalog data:

**Catalogs:**
- Tipos IVA (VAT types)
- Tarifas (Tariff rates)
- Impuestos (Tax types)
- Zonas (Zones)
- Categorias (Categories)
- Dispositivos (Devices)

**Initial Operators:**
- Default operator accounts for system access

### Database Initialization Strategy

**Development:**
- Use `spring.jpa.hibernate.ddl-auto=update`
- Automatically creates tables from entities

**Production:**
- Use Flyway or Liquibase for migrations
- Control schema evolution
- Version control database changes

---

## Database Migration

### Using Flyway (Recommended for Production)

1. Add Flyway dependency:

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-mysql</artifactId>
</dependency>
```

2. Create migration scripts in `src/main/resources/db/migration/`:

```sql
-- V1__create_initial_schema.sql
CREATE TABLE ...;

-- V2__add_new_column.sql
ALTER TABLE clientes ADD COLUMN nuevo_campo VARCHAR(255);
```

3. Configure in `application.properties`:

```properties
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
```

---

## Database Operations

### Common Queries

#### 1. Get Customer by Meter Serial

```sql
SELECT c.* FROM clientes c
JOIN medidores m ON c.id_cliente = m.id_cliente
WHERE m.nro_serie = ?
```

#### 2. Get Meter Readings History

```sql
SELECT l.* FROM lecturas l
WHERE l.id_medidor = ?
ORDER BY l.fecha DESC, l.id_lectura DESC
```

#### 3. Get Invoice with Line Items

```sql
SELECT f.*, ic.*, ii.* FROM facturas f
LEFT JOIN items_consumo ic ON f.id_factura = ic.id_factura
LEFT JOIN items_impuesto ii ON f.id_factura = ii.id_factura
WHERE f.id_factura = ?
```

#### 4. Get Billing Period Summary

```sql
SELECT 
    m.id_medidor,
    m.nro_serie,
    l_actual.fecha as fecha_actual,
    l_actual.valor as valor_actual,
    l_anterior.fecha as fecha_anterior,
    l_anterior.valor as valor_anterior,
    (l_actual.valor - l_anterior.valor) as consumo
FROM medidores m
JOIN lecturas l_actual ON m.id_medidor = l_actual.id_medidor
LEFT JOIN lecturas l_anterior ON m.id_medidor = l_anterior.id_medidor
    AND l_anterior.fecha < l_actual.fecha
    AND l_anterior.id_lectura = (
        SELECT MAX(id_lectura) FROM lecturas 
        WHERE id_medidor = m.id_medidor AND fecha < l_actual.fecha
    )
WHERE m.nro_serie = ?
ORDER BY l_actual.fecha DESC
LIMIT 1
```

---

## Database Security

### Production Recommendations

1. **Use Separate Database User:**
   - Application user with minimal required permissions
   - NO DDL permissions (CREATE, ALTER, DROP)
   - Only SELECT, INSERT, UPDATE, DELETE

2. **Enable SSL:**
   - Use SSL connections for production
   - Verify SSL encryption

3. **Database Vault:**
   - Consider Oracle Database Vault for enhanced security
   - Or use AWS RDS with security groups

4. **Backup Strategy:**
   - Regular automated backups
   - Point-in-time recovery
   - Test backup restoration

5. **Audit Logging:**
   - Log sensitive operations
   - Track data changes
   - Monitor access patterns

---

## Database Troubleshooting

### Common Issues

#### 1. Duplicate Key Violation

**Error:** `Duplicate entry 'XXX' for key 'UK_...'`

**Causes:**
- Attempting to insert duplicate value for unique column
- Sequence out of sync

**Solution:**
- Check existing data before insert
- Fix sequence: `ALTER TABLE table_name AUTO_INCREMENT = N;`

#### 2. Foreign Key Constraint Violation

**Error:** `Cannot add or update a child row: a foreign key constraint fails`

**Causes:**
- Referencing non-existent parent record
- Attempting to delete parent with children

**Solution:**
- Verify parent exists before insert
- Use CASCADE DELETE if appropriate
- Delete children first, then parent

#### 3. Data Truncation

**Error:** `Data truncation: Data too long for column 'xxx' at row 1`

**Causes:**
- String exceeds column length
- Number exceeds numeric precision

**Solution:**
- Truncate/validate input data
- Increase column size if needed
- Add validation in application layer

---

## Database Maintenance

### Regular Maintenance Tasks

1. **Backup Database:**
   ```bash
   mysqldump -u emsa -p emsa > backup_$(date +%Y%m%d).sql
   ```

2. **Analyze Tables:**
   ```sql
   ANALYZE TABLE clientes, medidores, lecturas, facturas;
   ```

3. **Optimize Tables:**
   ```sql
   OPTIMIZE TABLE clientes, medidores, lecturas, facturas;
   ```

4. **Monitor Disk Space:**
   - Check table sizes
   - Monitor growth trends

5. **Archive Old Data:**
   - Move old readings to archive tables
   - Consider data retention policies

### Performance Monitoring

```sql
-- Check slow queries
SHOW VARIABLES LIKE 'slow_query%';

-- Check table sizes
SELECT 
    table_name,
    table_rows,
    data_length,
    index_length
FROM information_schema.tables
WHERE table_schema = 'emsa'
ORDER BY data_length DESC;
```