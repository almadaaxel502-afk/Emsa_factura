# Domain Model Documentation

## Overview

The domain model represents the core business concepts of the EMSA electricity billing system. This document describes all entities, their relationships, business rules, and validation constraints.

## Core Entities

### 1. Cliente (Customer)

Represents a customer of the electricity service.

**Attributes:**
- `idCliente` (Long, PK, Auto-generated)
- `nombre` (String, Optional) - First name
- `apellido` (String, Optional) - Last name
- `identificacionNacional` (String, Optional) - National ID
- `direccion` (String, Optional) - Customer address

**Relationships:**
- **COMPOSITION** → `TipoIVA` (One-to-One) - Customer's VAT type
- **AGGREGATION** → `Usuario` (One-to-One) - Associated user account
- **AGGREGATION** → `Medidor` (One-to-Many) - Customer's meters

**Business Rules:**
- A customer must have at least one meter to be billable
- A customer's full name is constructed from nombre + apellido
- A customer can have multiple meters (different locations)

**Constraints:**
- None specified at entity level (validation in service layer)

**Java Class:** `ar.com.itec1misiones.emsa.entity.Cliente`

---

### 2. Medidor (Electricity Meter)

Represents an electricity meter device installed at a customer's location.

**Attributes:**
- `idMedidor` (Long, PK, Auto-generated)
- `nroSerie` (String, Optional) - Serial number
- `numeroConexion` (String, Optional) - Connection number
- `direccion` (String, Optional) - Physical installation address

**Relationships:**
- **COMPOSITION** → `Zona` (One-to-One) - Meter zone
- **COMPOSITION** → `Categoria` (One-to-One) - Meter category
- **AGGREGATION** → `Dispositivo` (One-to-One) - Associated device
- **AGGREGATION** → `Cliente` (One-to-One) - Owner customer
- **AGGREGATION** → `Factura` (One-to-Many) - Invoices generated for this meter
- **AGGREGATION** → `Lectura` (One-to-Many) - Meter readings history

**Business Rules:**
- A meter belongs to exactly one customer
- A customer can have multiple meters
- Meters have associated zones and categories for tariff application
- Meter readings are time-series data for consumption calculation

**Constraints:**
- None specified at entity level

**Java Class:** `ar.com.itec1misiones.emsa.entity.Medidor`

---

### 3. Lectura (Meter Reading)

Represents a recorded meter reading at a specific point in time.

**Attributes:**
- `idLectura` (Long, PK, Auto-generated)
- `fecha` (LocalDate) - Date of the reading
- `valor` (BigDecimal) - Cumulative kWh value (accumulated since installation)

**Relationships:**
- **AGGREGATION** → `Medidor` (One-to-One) - Associated meter

**Business Rules:**
- Readings are cumulative (not periodic consumption)
- Consumption is calculated as: `current_reading - previous_reading`
- A new reading should be higher than the previous reading (validation in service)
- Readings are recorded on the billing date

**Constraints:**
- `fecha` is required (non-null)
- `valor` is required (non-null, positive)

**Java Class:** `ar.com.itec1misiones.emsa.entity.Lectura`

---

### 4. Factura (Invoice)

Represents a generated electricity invoice for a billing period.

**Attributes:**
- `idFactura` (Long, PK, Auto-generated)
- `fecha` (LocalDate) - Billing period end date (reading date)
- `importeIVA` (BigDecimal) - VAT amount
- `subtotal` (BigDecimal) - Subtotal before tax
- `total` (BigDecimal) - Total invoice amount

**Relationships:**
- **AGGREGATION** → `Medidor` (One-to-One) - Meter being billed
- **AGGREGATION** → `ItemConsumo` (One-to-Many) - Consumption line items
- **AGGREGATION** → `ItemImpuesto` (One-to-Many) - Tax line items

**Business Rules:**
- Invoice is generated for a specific billing period
- Total = Subtotal + ImporteIVA
- Multiple consumption items can exist (different tariff blocks)
- Multiple tax items can exist (different tax types)

**Constraints:**
- None specified at entity level

**Java Class:** `ar.com.itec1misiones.emsa.entity.Factura`

---

### 5. ItemConsumo (Consumption Line Item)

Represents a block of consumed kWh billed at a specific tariff rate.

**Attributes:**
- `idItemConsumo` (Long, PK, Auto-generated)
- `cantidad` (BigDecimal) - kWh consumption in this block

**Relationships:**
- **COMPOSITION** → `Tarifa` (One-to-One) - Tariff rate applied
- **AGGREGATION** → `Factura` (One-to-One) - Parent invoice

**Business Rules:**
- Each item represents consumption at a specific tariff
- Consumption is allocated to tariff blocks based on rate schedule
- Subtotal = cantidad × tarifa.tasa

**Constraints:**
- None specified at entity level

**Java Class:** `ar.com.itec1misiones.emsa.entity.ItemConsumo`

---

### 6. ItemImpuesto (Tax Line Item)

Represents a tax amount applied to the invoice.

**Attributes:**
- `idItemImpuesto` (Long, PK, Auto-generated)
- `cantidad` (BigDecimal) - Taxable base amount
- `tasaImpuesto` (BigDecimal) - Tax rate (percentage)

**Relationships:**
- **COMPOSITION** → `Impuesto` (One-to-One) - Tax type
- **AGGREGATION** → `Factura` (One-to-One) - Parent invoice

**Business Rules:**
- Tax amount = cantidad × (tasaImpuesto / 100)
- Multiple tax types can apply to an invoice
- Common taxes: IVA (VAT), national taxes

**Constraints:**
- None specified at entity level

**Java Class:** `ar.com.itec1misiones.emsa.entity.ItemImpuesto`

---

### 7. Tarifa (Tariff Rate)

Represents a tariff rate applied to consumption.

**Attributes:**
- `idTarifa` (Long, PK, Auto-generated)
- `descripcion` (String, Optional) - Description
- `tasa` (BigDecimal) - Price per kWh

**Relationships:**
- **COMPOSITION** ← `ItemConsumo` (One-to-Many) - Items using this tariff

**Business Rules:**
- Multiple tariff blocks may apply to a single invoice
- Tariffs are applied based on consumption ranges or customer category
- Fixed rates per kWh for consumption calculation

**Constraints:**
- None specified at entity level

**Java Class:** `ar.com.itec1misiones.emsa.entity.Tarifa`

---

### 8. Impuesto (Tax)

Represents a tax type in the tax catalog.

**Attributes:**
- `idImpuesto` (Long, PK, Auto-generated)
- `descripcion` (String, Optional) - Tax description
- `tasa` (BigDecimal) - Tax rate (percentage)

**Relationships:**
- **COMPOSITION** ← `ItemImpuesto` (One-to-Many) - Tax line items

**Business Rules:**
- Tax rate stored as percentage (e.g., 21 for 21%)
- Multiple tax types can apply to invoices
- Common tax types: IVA, national taxes, provincial taxes

**Constraints:**
- None specified at entity level

**Java Class:** `ar.com.itec1misiones.emsa.entity.Impuesto`

---

### 9. TipoIVA (VAT Type)

Represents different VAT classifications for customers.

**Attributes:**
- `idTipoIVA` (Long, PK, Auto-generated)
- `descripcion` (String, Optional) - VAT type description
- `tasa` (BigDecimal) - VAT rate (percentage)

**Relationships:**
- **COMPOSITION** ← `Cliente` (One-to-One) - Customer's VAT type

**Business Rules:**
- Different VAT rates for different customer types
- Common types: Consumidor Final (21%), Comercio Registrado (10.5%)
- VAT rate applied to subtotal to calculate importeIVA

**Constraints:**
- None specified at entity level

**Java Class:** `ar.com.itec1misiones.emsa.entity.TipoIVA`

---

### 10. Usuario (User)

Base class for system users with inheritance support.

**Attributes:**
- `idUsuario` (Long, PK, Auto-generated)
- `nombre` (String) - Username/login
- `contrasena` (String) - Password (hashed in production)

**Relationships:**
- **AGGREGATION** → `Cliente` (One-to-Many) - Customer accounts
- **DISCRIMINATOR** → `Operador` (Subclass) - System operators

**Inheritance Strategy:**
- SINGLE_TABLE inheritance
- Discriminator column: `tipo_usuario`

**Business Rules:**
- Base class for both operators and customers
- Operators have login credentials
- Customers may have associated user accounts

**Constraints:**
- None specified at entity level

**Java Class:** `ar.com.itec1misiones.emsa.entity.Usuario`

---

### 11. Operador (Operator)

System operator who logs in and performs billing operations.

**Attributes:**
- Inherits from `Usuario` (idUsuario, nombre, contrasena)

**Relationships:**
- **DISCRIMINATOR** → `Usuario` (Is-a relationship)

**Business Rules:**
- Inherits all Usuario functionality
- Used for authentication and authorization
- Performs billing operations (readings, invoices)

**Constraints:**
- None specified at entity level

**Java Class:** `ar.com.itec1misiones.emsa.entity.Operador`

---

### 12. Categoria (Category)

Meter category classification.

**Attributes:**
- `idCategoria` (Long, PK, Auto-generated)
- `codigo` (String, Optional) - Category code
- `descripcion` (String, Optional) - Category description

**Relationships:**
- **COMPOSITION** ← `Medidor` (One-to-One) - Meter category

**Business Rules:**
- Categories may affect tariff application
- Used for meter classification (residential, commercial, industrial)

**Constraints:**
- None specified at entity level

**Java Class:** `ar.com.itec1misiones.emsa.entity.Categoria`

---

### 13. Zona (Zone)

Geographical zone classification for meters.

**Attributes:**
- `idZona` (Long, PK, Auto-generated)
- `codigo` (String, Optional) - Zone code
- `descripcion` (String, Optional) - Zone description

**Relationships:**
- **COMPOSITION** ← `Medidor` (One-to-One) - Meter zone

**Business Rules:**
- Zones may affect tariff application
- Used for geographical classification

**Constraints:**
- None specified at entity level

**Java Class:** `ar.com.itec1misiones.emsa.entity.Zona`

---

### 14. Dispositivo (Device)

Device associated with a meter (e.g., smart meter model).

**Attributes:**
- `idDispositivo` (Long, PK, Auto-generated)
- `descripcion` (String, Optional) - Device description
- `consumo` (BigDecimal, Optional) - Typical/expected consumption

**Relationships:**
- **AGGREGATION** ← `Medidor` (One-to-One) - Meter device

**Business Rules:**
- Optional association with meter
- May store device-specific information

**Constraints:**
- None specified at entity level

**Java Class:** `ar.com.itec1misiones.emsa.entity.Dispositivo`

---

## Entity Relationship Diagram

```plantuml
@startuml
' Define styles
skinparam {
  backgroundColor #FFFFFF
  backgroundColor<<ENTITY>> #F0F8FF
  backgroundColor<<CATALOG>> #FFFACD
  defaultTextAlignment center
  roundCorner 5
  defaultFontName Arial
  defaultFontSize 12
}

' Entity Classes
<<ENTITY>> class Cliente {
  + Long idCliente
  + String nombre
  + String apellido
  + String identificacionNacional
  + String direccion
}

<<ENTITY>> class Usuario {
  + Long idUsuario
  + String nombre
  + String contrasena
}

<<ENTITY>> class Operador <<extends>> {
  + (inherits Usuario)
}

<<ENTITY>> class Medidor {
  + Long idMedidor
  + String nroSerie
  + String numeroConexion
  + String direccion
}

<<ENTITY>> class Lectura {
  + Long idLectura
  + LocalDate fecha
  + BigDecimal valor
}

<<ENTITY>> class Factura {
  + Long idFactura
  + LocalDate fecha
  + BigDecimal importeIVA
  + BigDecimal subtotal
  + BigDecimal total
}

<<ENTITY>> class ItemConsumo {
  + Long idItemConsumo
  + BigDecimal cantidad
}

<<ENTITY>> class ItemImpuesto {
  + Long idItemImpuesto
  + BigDecimal cantidad
  + BigDecimal tasaImpuesto
}

<<CATALOG>> class Tarifa {
  + Long idTarifa
  + String descripcion
  + BigDecimal tasa
}

<<CATALOG>> class Impuesto {
  + Long idImpuesto
  + String descripcion
  + BigDecimal tasa
}

<<CATALOG>> class TipoIVA {
  + Long idTipoIVA
  + String descripcion
  + BigDecimal tasa
}

<<CATALOG>> class Categoria {
  + Long idCategoria
  + String codigo
  + String descripcion
}

<<CATALOG>> class Zona {
  + Long idZona
  + String codigo
  + String descripcion
}

<<ENTITY>> class Dispositivo {
  + Long idDispositivo
  + String descripcion
  + BigDecimal consumo
}

' Relationships - Composition (filled diamond)
Cliente }--TipoIVA : composition\n(compose)\n0..1
Cliente }--Usuario : aggregation\n1..1
Medidor }--Zona : composition\n1..1
Medidor }--Categoria : composition\n1..1
Medidor }--Dispositivo : aggregation\n0..1
Medidor }--Cliente : aggregation\n1..1
ItemConsumo }--Tarifa : composition\n1..1
ItemImpuesto }--Impuesto : composition\n1..1

' Relationships - Aggregation (open diamond)
Cliente o--Medidor : 1..*\n1..1
Usuario o--Cliente : 1..*\n0..1
Medidor o--Factura : 1..*\n1..1
Medidor o--Lectura : 1..*\n1..*
Factura o--ItemConsumo : 1..*\n1..1
Factura o--ItemImpuesto : 1..*\n1..1

' Inheritance
Operador --|> Usuario : extends

note right of Cliente
  Note: Cliente can have
  multiple Medidores
end note

note right of Medidor
  Note: Medidor belongs
  to exactly one Cliente
end note
@enduml
```

---

## Business Rules Summary

### Consumption Calculation

1. **Consumption = Current Reading - Previous Reading**
   - For first billing: consumption = current reading (no previous)
   - Readings are cumulative (kWh since installation)

2. **Validation Rules:**
   - New reading must be ≥ previous reading
   - Reading date must be ≥ previous reading date
   - No duplicate readings on same date

### Invoice Generation

1. **Consumption Allocation:**
   - Allocate consumption to tariff blocks
   - Different rates for different consumption ranges

2. **Tax Calculation:**
   - Subtotal = sum of all item consumos
   - IVA = subtotal × TipoIVA.tasa / 100
   - Total = subtotal + IVA + other taxes

3. **Billing Rules:**
   - One invoice per meter per billing period
   - Multiple consumption items possible (tariff blocks)
   - Multiple tax items possible (IVA + other taxes)

### Data Integrity Rules

1. **Foreign Key Constraints:**
   - Medidor must have Cliente
   - Factura must have Medidor
   - ItemConsumo must have Tarifa and Factura
   - ItemImpuesto must have Impuesto and Factura

2. **Nullability:**
   - Some fields are optional (descriptions, codes)
   - Critical fields (dates, values) are required

### User and Security Rules

1. **Operador:**
   - Inherits Usuario functionality
   - Has login credentials
   - Performs billing operations

2. **Cliente:**
   - May have associated Usuario account
   - Has TipoIVA for tax calculation

## DTOs (Data Transfer Objects)

### Purpose
DTOs are used for:
- API responses (REST endpoints)
- Frontend data transfer
- Decoupling internal entities from external interface

### Key DTOs:

1. **FacturaDTO** - Invoice with all line items
2. **ClienteDTO** - Customer with meter list
3. **MedidorDTO** - Meter with associated data
4. **LecturaDTO** - Meter reading
5. **TarifaDTO** - Tariff rate
6. **ImpuestoDTO** - Tax
7. **TipoIVADTO** - VAT type
8. **ItemConsumoDTO** - Consumption line item
9. **ItemImpuestoDTO** - Tax line item
10. **PeriodoFacturacionDTO** - Billing period summary

### Note:
DTOs are separate from entities and do not have JPA annotations.
They are simple POJOs for data transfer.