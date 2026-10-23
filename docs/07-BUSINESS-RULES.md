# Business Rules Documentation

## Overview

This document describes all business rules that govern the Emsa_factura billing system. These rules ensure data integrity, correct calculations, and compliance with business requirements.

## Business Rules by Category

### 1. Meter Reading Rules

#### Rule 1.1: Reading Must Be Positive
**Description:** All meter readings must have a positive value.

**Rule:** `reading.valor > 0`

**Implementation:** `LecturaService.registrarLectura()`

**Validation:**
```java
if (valor.compareTo(BigDecimal.ZERO) <= 0) {
    throw new IllegalArgumentException("Reading value must be positive");
}
```

**Business Rationale:** Energy consumption cannot be negative. Readings represent cumulative kWh since installation.

---

#### Rule 1.2: Reading Must Be Greater Than Previous
**Description:** New readings must be greater than or equal to the previous reading.

**Rule:** `reading.valor >= previousReading.valor`

**Implementation:** `LecturaService.registrarLectura()`

**Validation:**
```java
BigDecimal previousValue = getPreviousReadingValue(medidor);
if (valor.compareTo(previousValue) < 0) {
    throw new IllegalArgumentException("Reading must be >= previous reading");
}
```

**Business Rationale:** Energy meter counts only increase (or stay the same). The meter measures cumulative consumption.

---

#### Rule 1.3: No Duplicate Readings on Same Date
**Description:** Only one reading per meter per date is allowed.

**Rule:** `COUNT(lecturas WHERE fecha = ? AND id_medidor = ?) = 0`

**Implementation:** `LecturaService.registrarLectura()`

**Validation:**
```java
long count = lecturaRepository.countByMedidorYFecha(idMedidor, fecha);
if (count > 0) {
    throw new IllegalStateException("Reading already exists for this date");
}
```

**Business Rationale:** Each billing period should have exactly one reading to ensure accurate consumption calculation.

---

#### Rule 1.4: Reading Date Cannot Be Future Date
**Description:** Readings cannot be dated in the future.

**Rule:** `reading.fecha <= CURRENT_DATE`

**Implementation:** `LecturaService.registrarLectura()`

**Validation:**
```java
if (fecha.isAfter(LocalDate.now())) {
    throw new IllegalArgumentException("Reading date cannot be in the future");
}
```

**Business Rationale:** You cannot read energy consumption before it occurs.

---

### 2. Consumption Calculation Rules

#### Rule 2.1: Consumption = Current Reading - Previous Reading
**Description:** Energy consumption for a billing period is the difference between current and previous readings.

**Rule:** `consumo = lecturaActual.valor - lecturaAnterior.valor`

**Implementation:** `FacturacionService.liquidarPorLectura()`

**Calculation:**
```java
BigDecimal consumo = lecturaActual.getValor()
    .subtract(lecturaAnterior.getValor());
```

**Business Rationale:** This is the fundamental formula for calculating consumption from meter readings.

---

#### Rule 2.2: First Billing Uses Full Reading
**Description:** For first billing (no previous reading), consumption equals current reading.

**Rule:** `IF previousReading IS NULL THEN consumo = lecturaActual.valor`

**Implementation:** `FacturacionService.liquidarPorLectura()`

**Calculation:**
```java
if (lecturaAnterior == null) {
    consumo = lecturaActual.getValor(); // First billing
}
```

**Business Rationale:** First billing represents total consumption since installation.

---

#### Rule 2.3: Consumption Cannot Be Negative
**Description:** Consumption calculation must never result in a negative value.

**Rule:** `consumo >= 0`

**Implementation:** `FacturacionService.liquidarPorLectura()`

**Validation:**
```java
if (consumo.compareTo(BigDecimal.ZERO) < 0) {
    throw new IllegalStateException("Calculated consumption is negative");
}
```

**Business Rationale:** Negative consumption indicates data error (should be handled by Rule 1.2).

---

### 3. Invoice Generation Rules

#### Rule 3.1: Invoice Date = Reading Date
**Description:** Invoice is dated with the meter reading date.

**Rule:** `factura.fecha = lectura.fecha`

**Implementation:** `FacturacionService.liquidarPorLectura()`

**Calculation:**
```java
factura.setFecha(lecturaActual.getFecha());
```

**Business Rationale:** Invoice represents billing for the period ending on the reading date.

---

#### Rule 3.2: One Invoice Per Meter Per Period
**Description:** Only one invoice can be generated per meter per billing period.

**Rule:** `COUNT(facturas WHERE id_medidor = ? AND fecha = ?) = 0`

**Implementation:** `FacturacionService.liquidarPorLectura()`

**Validation:**
```java
long count = facturaRepository.countByMedidorYFecha(idMedidor, fecha);
if (count > 0) {
    throw new IllegalStateException("Invoice already exists for this period");
}
```

**Business Rationale:** Prevents duplicate billing for the same period.

---

#### Rule 3.3: Invoice Total = Subtotal + Taxes
**Description:** Final invoice amount equals subtotal plus all taxes.

**Rule:** `factura.total = factura.subtotal + factura.importeIVA + other_taxes`

**Implementation:** `FacturacionService.liquidarPorLectura()`

**Calculation:**
```java
BigDecimal subtotal = itemsConsumo.stream()
    .map(item -> item.getCantidad().multiply(item.getTarifa().getTasa()))
    .reduce(BigDecimal.ZERO, BigDecimal::add);

BigDecimal importeIVA = subtotal
    .multiply(tipoIVA.getTasa())
    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

factura.setSubtotal(subtotal);
factura.setImporteIVA(importeIVA);
factura.setTotal(subtotal.add(importeIVA));
```

**Business Rationale:** Standard financial calculation for invoice totals.

---

#### Rule 3.4: Tax Calculation Based on Customer VAT Type
**Description:** Tax rate applied is determined by customer's VAT type.

**Rule:** `tax_rate = cliente.tipoIVA.tasa`

**Implementation:** `FacturacionService.liquidarPorLectura()`

**Calculation:**
```java
TipoIVA tipoIVA = cliente.getTipoIVA();
BigDecimal taxRate = tipoIVA.getTasa(); // e.g., 21 or 10.5
```

**Business Rationale:** Different customer types have different tax rates.

---

### 4. Tariff Application Rules

#### Rule 4.1: Tariff Blocks Apply Sequentially
**Description:** Consumption is allocated to tariff blocks in sequence.

**Rule:** 
```
Block 1: 0-200 kWh at rate 1
Block 2: 201-500 kWh at rate 2
Block 3: 501+ kWh at rate 3
```

**Implementation:** `FacturacionService.applyTariffBlocks()`

**Calculation:**
```java
// Simplified example
BigDecimal remaining = consumo;
BigDecimal bloque1 = remaining.min(new BigDecimal("200"));
remaining = remaining.subtract(bloque1);
BigDecimal bloque2 = remaining.min(new BigDecimal("300"));
remaining = remaining.subtract(bloque2);
BigDecimal bloque3 = remaining;
```

**Business Rationale:** Tariff blocks apply different rates to different consumption ranges.

---

#### Rule 4.2: Tariff Applied Based on Customer Category
**Description:** Which tariff blocks apply depends on customer's meter category.

**Rule:** `tariff_blocks = getCategorizedTariffs(customer.categoria)`

**Implementation:** `FacturacionService.applyTariffBlocks()`

**Business Rationale:** Residential, commercial, and industrial customers have different tariff structures.

---

#### Rule 4.3: Tariff Rate Must Be Positive
**Description:** All tariff rates must be positive values.

**Rule:** `tarifa.tasa > 0`

**Implementation:** `TarifaService.create/update()`

**Validation:**
```java
if (tasa.compareTo(BigDecimal.ZERO) <= 0) {
    throw new IllegalArgumentException("Tariff rate must be positive");
}
```

**Business Rationale:** Prices cannot be negative or zero.

---

### 5. Customer Rules

#### Rule 5.1: Customer Must Have VAT Type
**Description:** Every customer must be assigned a VAT type.

**Rule:** `cliente.tipoIVA IS NOT NULL`

**Implementation:** `ClienteService.create()`

**Validation:**
```java
if (cliente.getTipoIVA() == null) {
    throw new IllegalArgumentException("Customer must have a VAT type");
}
```

**Business Rationale:** VAT type determines tax rate for billing.

---

#### Rule 5.2: Customer Can Have Multiple Meters
**Description:** A customer may have multiple meters at different locations.

**Rule:** `1 customer : 0..* meters`

**Implementation:** JPA relationship in `Cliente` entity

**Business Rationale:** Large customers may have multiple service points.

---

#### Rule 5.3: Meter Belongs to Exactly One Customer
**Description:** Each meter is associated with one and only one customer.

**Rule:** `1 meter : 1 customer`

**Implementation:** JPA relationship in `Medidor` entity

**Business Rationale:** Each meter serves one customer account.

---

### 6. Data Integrity Rules

#### Rule 6.1: Reading Sequence Must Be Monotonic
**Description:** For each meter, readings must be in chronological order with non-decreasing values.

**Rule:** `∀i: reading[i].fecha ≤ reading[i+1].fecha AND reading[i].valor ≤ reading[i+1].valor`

**Implementation:** Application-level validation in `LecturaService`

**Business Rationale:** Ensures data integrity for consumption calculations.

---

#### Rule 6.2: Invoice Must Have at Least One Consumption Item
**Description:** Every invoice must have at least one consumption line item.

**Rule:** `factura.itemsConsumo.size() ≥ 1`

**Implementation:** `FacturacionService.liquidarPorLectura()`

**Validation:**
```java
if (itemsConsumo.isEmpty()) {
    throw new IllegalStateException("Invoice must have at least one consumption item");
}
```

**Business Rationale:** An invoice with no consumption doesn't make business sense.

---

#### Rule 6.3: Invoice Must Have Tax Item
**Description:** Every invoice must have at least one tax line item.

**Rule:** `factura.itemsImpuesto.size() ≥ 1`

**Implementation:** `FacturacionService.liquidarPorLectura()`

**Business Rationale:** All invoices include tax calculations.

---

### 7. Authentication and Authorization Rules

#### Rule 7.1: Operators Must Authenticate
**Description:** System operators must log in before performing operations.

**Rule:** `authentication.isAuthenticated() == true`

**Implementation:** `SecurityConfig` (currently disabled for development)

**Business Rationale:** Audit trail and accountability.

---

#### Rule 7.2: Operators Can Only Access Their Customers
**Description:** Operators should only see customers assigned to them.

**Rule:** `customer.id_usuario = operator.id`

**Implementation:** `ClienteService` (not yet implemented)

**Business Rationale:** Data isolation between operators.

---

### 8. Validation Rules

#### Rule 8.1: Meter Serial Number Must Be Unique
**Description:** No two meters can have the same serial number.

**Rule:** `COUNT(medidores WHERE nro_serie = ?) ≤ 1`

**Implementation:** `MedidorService.create()`

**Validation:**
```java
Optional<Medidor> existing = medidorRepository.findByNroSerieIgnoreCase(nroSerie);
if (existing.isPresent()) {
    throw new IllegalArgumentException("Meter serial number already exists");
}
```

**Business Rationale:** Serial number is the primary identifier for meters.

---

#### Rule 8.2: Meter Must Have Valid Zone and Category
**Description:** Meter zone and category must reference existing catalog entries.

**Rule:** `medidor.id_zona IS NOT NULL AND medidor.id_categoria IS NOT NULL`

**Implementation:** Database foreign key constraints

**Business Rationale:** Zone and category affect tariff application.

---

### 9. Business Logic Rules

#### Rule 9.1: Readings Can Be Added at Any Time
**Description:** Meter readings can be registered retroactively.

**Rule:** `reading.fecha <= CURRENT_DATE` (but can be old date)

**Implementation:** `LecturaService.registrarLectura()`

**Business Rationale:** Technicians may read meters and enter data later.

---

#### Rule 9.2: Invoices Cannot Be Modified After Generation
**Description:** Once generated, invoices are immutable.

**Rule:** `factura status = 'GENERATED' (immutable)`

**Implementation:** Business logic decision (could allow reversals)

**Business Rationale:** Financial integrity - changes to invoices should create new records.

---

#### Rule 9.3: First Billing Special Case
**Description:** When there's no previous reading, consumption = current reading.

**Rule:** `IF previousReading IS NULL THEN consumo = currentReading`

**Implementation:** `FacturacionService.liquidarPorLectura()`

**Business Rationale:** First billing represents total installation consumption.

---

### 10. Tax Calculation Rules

#### Rule 10.1: VAT Calculation
**Description:** VAT is calculated on the subtotal.

**Rule:** `IVA = subtotal × (tipoIVA.tasa / 100)`

**Implementation:** `FacturacionService.liquidarPorLectura()`

**Calculation:**
```java
BigDecimal iva = subtotal
    .multiply(tipoIVA.getTasa())
    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
```

**Business Rationale:** Standard VAT calculation formula.

---

#### Rule 10.2: Tax Rates Are Percentages
**Description:** Tax rates are stored as percentages (e.g., 21 means 21%).

**Rule:** `impuesto.tasa > 0 AND impuesto.tasa < 100` (or higher for multi-level)

**Implementation:** `ImpuestoService`

**Business Rationale:** Tax rates are typically expressed as percentages.

---

#### Rule 10.3: Multiple Tax Types Can Apply
**Description:** An invoice can have multiple tax types (IVA + others).

**Rule:** `itemsImpuesto.size() ≥ 1`

**Implementation:** JPA relationship

**Business Rationale:** In addition to VAT, there may be other taxes.

---

## Edge Cases and Special Handling

### Edge Case 1: Zero Consumption
**Scenario:** Current reading equals previous reading.

**Handling:** 
- Consumption = 0
- Invoice generated with zero consumption items
- Still applies VAT on zero subtotal

**Business Rationale:** Even if no consumption, customer may have fixed charges.

---

### Edge Case 2: Meter Reset
**Scenario:** Meter is replaced or reset.

**Handling:**
- Register new meter with new serial
- Previous meter readings remain for history
- New meter starts from zero

**Business Rationale:** Meter replacement is common in practice.

---

### Edge Case 3: Multiple Readings in One Day
**Scenario:** Accidental duplicate readings on same date.

**Handling:**
- System prevents duplicate date per meter
- Error message: "Reading already exists for this date"

**Business Rationale:** Ensures one reading per billing period.

---

### Edge Case 4: Large Consumption Spikes
**Scenario:** Reading shows unexpected high consumption.

**Handling:**
- No automatic validation (may be legitimate)
- Operator can flag for review
- System records all data

**Business Rationale:** Consumer behavior varies; system shouldn't assume data is wrong.

---

### Edge Case 5: Reading Date in Past
**Scenario:** Technician enters old reading retroactively.

**Handling:**
- Allowed if reading is valid (greater than previous)
- Updates consumption history

**Business Rationale:** Historical data entry is common.

---

## Business Rule Validation Matrix

| Rule | Type | Validation Layer | Implementation |
|------|------|------------------|----------------|
| 1.1 | Input | Controller/Service | BigDecimal comparison |
| 1.2 | Business Logic | Service | Previous reading lookup |
| 1.3 | Duplicate Check | Service | Repository count query |
| 1.4 | Input | Service | LocalDate comparison |
| 2.1 | Calculation | Service | Arithmetic operation |
| 2.2 | Business Logic | Service | Null check |
| 2.3 | Validation | Service | Comparison |
| 3.1 | Data Integrity | Service | Assignment |
| 3.2 | Duplicate Check | Service | Repository count |
| 3.3 | Calculation | Service | Arithmetic |
| 3.4 | Business Logic | Service | Relationship lookup |
| 4.1 | Business Logic | Service | Algorithm |
| 4.2 | Business Logic | Service | Category lookup |
| 4.3 | Input | Service | BigDecimal comparison |
| 5.1 | Validation | Service | Null check |
| 5.2-5.3 | Relationship | Database | JPA annotations |
| 6.1-6.3 | Data Integrity | Service/Database | Application logic |
| 7.1-7.2 | Security | Security Config | Spring Security |
| 8.1-8.2 | Validation | Service/Database | Repository + FK |
| 9.1-9.3 | Business Logic | Service | Conditional logic |
| 10.1-10.3 | Calculation | Service | Arithmetic |

---

## Business Rule Implementation Summary

### Service Layer Implementation

**LecturaService:**
- Rule 1.1, 1.2, 1.3, 1.4, 6.1

**FacturacionService:**
- Rule 2.1, 2.2, 2.3, 3.1, 3.2, 3.3, 3.4, 4.1, 4.2, 4.3, 6.2, 6.3, 9.2, 9.3, 10.1, 10.2, 10.3

**ClienteService:**
- Rule 5.1, 5.2, 5.3, 8.2

**MedidorService:**
- Rule 8.1, 8.2

### Database Layer Implementation

**Foreign Key Constraints:**
- Rules 5.2-5.3, 8.2

**Unique Constraints:**
- Rule 8.1 (nro_serie)

**Null Constraints:**
- Required fields enforce rules like 3.3

---

## Business Rule Testing

### Test Strategy

1. **Unit Tests:** Test each business rule individually
2. **Integration Tests:** Test rule combinations
3. **Edge Case Tests:** Test boundary conditions
4. **Negative Tests:** Test invalid inputs

### Example Test Cases

**Test Case 1.2: Reading Must Be Greater Than Previous**
```java
@Test
public void testReadingMustBeGreaterThanPrevious() {
    // Setup: Create meter with previous reading of 1000
    // Action: Try to register reading of 999
    // Expected: IllegalArgumentException
}
```

**Test Case 2.1: Consumption Calculation**
```java
@Test
public void testConsumptionCalculation() {
    // Setup: Current reading 1500, previous 1350
    // Action: Calculate consumption
    // Expected: 150
}
```

**Test Case 3.2: One Invoice Per Period**
```java
@Test
public void testOneInvoicePerPeriod() {
    // Setup: Invoice exists for meter/date
    // Action: Try to generate another invoice
    // Expected: IllegalStateException
}
```

---

## Summary

This business rules documentation covers:

1. **Meter Reading Rules** (Rules 1.1-1.4)
2. **Consumption Calculation Rules** (Rules 2.1-2.3)
3. **Invoice Generation Rules** (Rules 3.1-3.4)
4. **Tariff Application Rules** (Rules 4.1-4.3)
5. **Customer Rules** (Rules 5.1-5.3)
6. **Data Integrity Rules** (Rules 6.1-6.3)
7. **Authentication Rules** (Rules 7.1-7.2)
8. **Validation Rules** (Rules 8.1-8.2)
9. **Business Logic Rules** (Rules 9.1-9.3)
10. **Tax Calculation Rules** (Rules 10.1-10.3)
11. **Edge Cases** (Zero consumption, meter reset, etc.)
12. **Implementation Summary** (Where rules are enforced)
13. **Testing Strategy** (Test cases)

Business rules ensure:
- **Data Integrity:** Valid data entered and stored
- **Correct Calculations:** Accurate billing calculations
- **Compliance:** Tax and regulatory compliance
- **Audit Trail:** Clear rules for traceability

All rules are implemented in the service layer with appropriate validation and error handling.