# Workflow Documentation

## Overview

This document describes the key business workflows in the Emsa_factura billing system. These workflows outline the step-by-step processes for common operations.

## Key Workflows

### 1. Meter Reading Registration Workflow

**Actor:** Operator

**Preconditions:**
- Meter exists in the system
- Operator has login credentials

**Flow:**

```
1. Operator logs into the system
   ↓
2. Operator navigates to "Register Reading" screen
   ↓
3. Operator enters meter serial number
   ↓
4. System validates meter exists
   ├─→ Meter not found: Show error, return to step 3
   └─→ Meter found: Continue
   ↓
5. Operator enters reading value (cumulative kWh)
   ↓
6. System validates reading:
   ├─→ Reading is negative: Show error, return to step 5
   ├─→ Reading equals previous: Show error, return to step 5
   └─→ Reading is valid: Continue
   ↓
7. System retrieves previous reading for this meter
   ↓
8. System checks for duplicate reading on same date
   ├─→ Duplicate found: Show error, return to step 5
   └─→ No duplicate: Continue
   ↓
9. System saves reading to database
   ↓
10. System confirms successful registration
   ↓
11. Operator can continue or exit
```

**Alternative Flow - First Reading:**
- Step 7: No previous reading exists
- System records this as first reading (no consumption calculation)

**Error Handling:**
- Invalid meter serial: Error message displayed
- Negative reading: Error message displayed
- Duplicate reading: Error message displayed
- Database error: System logs error, shows generic message

**Postconditions:**
- New reading saved to database
- Meter's reading history updated
- Available for billing calculation

---

### 2. Invoice Generation Workflow

**Actor:** Operator

**Preconditions:**
- Meter exists and has at least one reading
- Operator has logged in

**Flow:**

```
1. Operator selects "Generate Invoice" for a meter
   ↓
2. System retrieves meter information
   ↓
3. System retrieves latest reading (current reading)
   ↓
4. System retrieves previous reading
   ├─→ No previous reading: First billing
   └─→ Previous reading exists: Normal billing
   ↓
5. System calculates consumption:
   Consumption = Current Reading - Previous Reading
   
   For first billing: Consumption = Current Reading
   ↓
6. System retrieves customer information:
   ├─→ Customer's VAT type (Tipo IVA)
   ├─→ Customer details
   └─→ Meter details
   ↓
7. System retrieves applicable tariff rates
   ↓
8. System applies tariff blocks to consumption:
   ├─→ First block: Up to 200 kWh at rate 1
   ├─→ Second block: Next 300 kWh at rate 2
   └─→ Remaining: Above 500 kWh at rate 3
   
   (Tariff blocks depend on customer category)
   ↓
9. System calculates subtotal:
   Subtotal = Σ(consumption_block × tariff_rate)
   ↓
10. System calculates tax:
    Tax = Subtotal × (VAT_rate / 100)
   ↓
11. System calculates total:
    Total = Subtotal + Tax
   ↓
12. System creates invoice record
   ↓
13. System creates consumption line items
   ↓
14. System creates tax line items
   ↓
15. System saves invoice and line items
   ↓
16. System confirms invoice generation
   ↓
17. Operator can print invoice or continue
```

**Alternative Flow - Generate from Last Reading:**
- Step 2-4: Automatically use most recent reading
- Step 5-16: Same as normal billing

**Postconditions:**
- Invoice record created in database
- Reading marked as billed (optional, for tracking)
- Customer can view invoice
- Invoice can be printed/exported

**Error Conditions:**
- No readings for meter: Error "No readings available"
- Reading duplicate detected: Error "Already billed"
- Database error: System logs error, shows generic message

---

### 3. Billing Period Inquiry Workflow

**Actor:** Operator, Customer service representative

**Preconditions:**
- Meter exists in system

**Flow:**

```
1. Operator enters meter serial number
   ↓
2. System validates meter exists
   ↓
3. System retrieves current billing period:
   ├─→ Latest reading: Current reading
   ├─→ Previous reading: Previous reading
   └─→ Consumption calculation: Current - Previous
   ↓
4. System retrieves customer information:
   ├─→ Customer name
   ├─→ VAT type
   └─→ VAT rate
   ↓
5. System displays billing period summary:
   ├─→ Meter details (serial, address)
   ├─→ Customer details (name, address)
   ├─→ Current reading (date, value)
   ├─→ Previous reading (date, value)
   ├─→ Calculated consumption
   ├─→ VAT type and rate
   └─→ First billing indicator
   ↓
6. Operator can:
   ├─→ Generate invoice for this period
   ├─→ View billing history
   └─→ Return to search
```

**Alternative Flow - First Billing Check:**
- If no previous reading exists: Mark as first billing
- Display warning: "This is first billing, consumption = current reading"

---

### 4. Customer Search Workflow

**Actor:** Operator

**Preconditions:**
- Operator logged in

**Flow:**

```
1. Operator enters search term
   ├─→ Search by name
   ├─→ Search by ID number
   └─→ Search by meter serial
   ↓
2. System searches in database:
   ├─→ By name (partial match on apellido, nombre)
   ├─→ By ID number (identificacionNacional)
   └─→ By meter serial (through medidores relation)
   ↓
3. System returns matching customers:
   ├─→ Customer name
   ├─→ ID number
   ├─→ Address
   ├─→ VAT type
   └─→ Meter serial numbers
   ↓
4. Operator selects customer
   ↓
5. System displays customer details:
   ├─→ Personal information
   ├─→ VAT information
   └─→ Meter list
   ↓
6. Operator can:
   ├─→ View billing history
   ├─→ Register reading
   └─→ Return to search
```

**Postconditions:**
- Customer information displayed
- Operator can perform operations on selected customer

---

### 5. Meter Registration Workflow

**Actor:** Operator

**Preconditions:**
- Operator logged in
- Required reference data exists (zones, categories)

**Flow:**

```
1. Operator navigates to "Register Meter"
   ↓
2. Operator enters meter details:
   ├─→ Serial number (required)
   ├─→ Connection number
   ├─→ Installation address
   ├─→ Zone
   ├─→ Category
   └─→ Device (optional)
   ↓
3. System validates input:
   ├─→ Serial number: Required, unique
   ├─→ Zone: Must exist
   └─→ Category: Must exist
   ↓
4. System retrieves or creates meter:
   ├─→ Serial exists: Error "Meter already registered"
   └─→ Serial new: Continue
   ↓
5. System saves meter to database
   ↓
6. System confirms registration
   ↓
7. Operator can link meter to customer
```

**Postconditions:**
- Meter created in database
- Meter linked to zone and category
- Meter ready for readings

---

### 6. Troubleshooting Workflow - Billing Calculation Issues

**Actor:** Operator, Technical support

**Preconditions:**
- Invoice exists with questionable calculation

**Flow:**

```
1. Operator retrieves invoice in question
   ↓
2. System displays invoice details:
   ├─→ Meter and customer info
   ├─→ Reading dates and values
   ├─→ Consumption calculation
   ├─→ Tariff blocks applied
   ├─→ Tax calculation
   └─→ Total
   ↓
3. Operator verifies intermediate values:
   ├─→ Check readings: Current vs previous
   ├─→ Check consumption: Current - Previous
   ├─→ Check tariffs: Applied rates
   └─→ Check taxes: VAT rate applied
   ↓
4. If discrepancy found:
   ├─→ Check reading dates: Correct period?
   ├─→ Check tariff application: Correct category?
   └─→ Check VAT type: Correct customer type?
   ↓
5. If data issue:
   ├─→ Correct reading if duplicate/incorrect
   ├─→ Re-generate invoice if needed
   └─→ Document correction
   ↓
6. If calculation logic issue:
   ├─→ Escalate to technical support
   └─→ Document bug/report
```

**Common Issues:**

**Issue 1: Unexpected Consumption**
- Check: Are readings cumulative or periodic?
- Check: Are readings from same meter?
- Check: Was previous reading valid?

**Issue 2: Wrong Tax Amount**
- Check: Customer's VAT type
- Check: VAT rate applied
- Check: Subtotal calculation

**Issue 3: Missing Tariff Block**
- Check: Customer category
- Check: Tariff configuration
- Check: Consumption ranges

---

### 7. Data Integrity Verification Workflow

**Actor:** System administrator, Technical support

**Preconditions:**
- Suspected data integrity issue

**Flow:**

```
1. Run integrity checks:
   ├─→ Check readings sequence per meter
   ├─→ Check invoices have readings
   ├─→ Check no orphan records
   └─→ Check calculation consistency
   ↓
2. If issues found:
   ├─→ Review each issue individually
   ├─→ Determine cause (user error, system bug, etc.)
   └─→ Document findings
   ↓
3. Correct issues:
   ├─→ Delete duplicate readings
   ├─→ Recalculate affected invoices
   └─→ Update corrupted data
   ↓
4. Verify corrections:
   ├─→ Re-run integrity checks
   └─→ Confirm no remaining issues
   ↓
5. Implement preventive measures:
   ├─→ Add validation logic
   ├─→ Improve error messages
   └─→ Add monitoring
```

**Integrity Checks:**

**Check 1: Reading Sequence**
```sql
-- Verify readings are in chronological order
-- and each reading is greater than previous
SELECT 
    m.id_medidor,
    m.nro_serie,
    l.fecha,
    l.valor,
    LAG(l.valor) OVER (PARTITION BY m.id_medidor ORDER BY l.fecha, l.id_lectura) as prev_valor
FROM medidores m
JOIN lecturas l ON m.id_medidor = l.id_medidor
WHERE l.valor < LAG(l.valor) OVER (PARTITION BY m.id_medidor ORDER BY l.fecha, l.id_lectura)
```

**Check 2: Invoice-Reading Match**
```sql
-- Verify invoices have valid readings
SELECT f.id_factura, f.fecha, f.id_medidor
FROM facturas f
LEFT JOIN lecturas l ON f.id_medidor = l.id_medidor 
    AND l.fecha = f.fecha
WHERE l.id_lectura IS NULL
```

**Check 3: Orphan Records**
```sql
-- Check for orphan readings (meter doesn't exist)
SELECT l.* FROM lecturas l
LEFT JOIN medidores m ON l.id_medidor = m.id_medidor
WHERE m.id_medidor IS NULL

-- Check for orphan invoices (meter doesn't exist)
SELECT f.* FROM facturas f
LEFT JOIN medidores m ON f.id_medidor = m.id_medidor
WHERE m.id_medidor IS NULL
```

---

## Workflow Diagrams

### Meter Reading Registration (Sequence Diagram)

```plantuml
@startuml
actor Operator
participant "UI" as UI
participant "Controller" as Ctrl
participant "Service" asSvc
participant "Repository" as Repo

Operator -> UI: Enter meter serial
UI -> Ctrl: POST /lecturas
Ctrl -> Ctrl: Validate meter exists
Ctrl -> Repo: findByNroSerie()
Repo --> Ctrl: Meter or null
Ctrl -> Ctrl: Check for null
Ctrl -> Ctrl: Validate reading value
Ctrl -> Ctrl: Check for duplicate date
Ctrl -> Repo: findByMedidor()
Repo --> Ctrl: Previous reading or null
Ctrl -> Ctrl: Calculate consumption
Ctrl -> Repo: save(lectura)
Repo --> Ctrl: Saved reading
Ctrl --> UI: Success message
UI --> Operator: Display confirmation
@enduml
```

### Invoice Generation (Sequence Diagram)

```plantuml
@startuml
actor Operator
participant "UI" as UI
participant "Controller" as Ctrl
participant "Service" asSvc
participant "Repository" as Repo
participant "Helper" as Helper

Operator -> UI: Select meter
UI -> Ctrl: POST /facturas/liquidar-lectura
Ctrl -> Ctrl: Parse parameters
Ctrl -> Repo: findByNroSerie()
Repo --> Ctrl: Meter
Ctrl -> Ctrl: Validate meter
Ctrl -> Ctrl: Get current reading
Ctrl -> Ctrl: Get previous reading
Ctrl -> Ctrl: Calculate consumption
Ctrl -> Repo: findByMedidor(idCliente)
Repo --> Ctrl: Customer
Ctrl -> Ctrl: Get customer's VAT type
Ctrl -> Helper: ApplyTariffBlocks()
Helper --> Ctrl: List of consumption blocks
Ctrl -> Ctrl: Calculate subtotal
Ctrl -> Ctrl: Calculate tax
Ctrl -> Ctrl: Calculate total
Ctrl -> Repo: save(factura)
Ctrl -> Repo: save(itemConsumo x N)
Ctrl -> Repo: save(itemImpuesto x N)
Repo --> Ctrl: Saved records
Ctrl --> UI: Invoice DTO
UI --> Operator: Display invoice
@enduml
```

### Billing Period Inquiry (Sequence Diagram)

```plantuml
@startuml
actor Operator
participant "UI" as UI
participant "Controller" as Ctrl
participant "Service" asSvc
participant "Repository" as Repo

Operator -> UI: Enter meter serial
UI -> Ctrl: GET /periodo
Ctrl -> Ctrl: Validate meter
Ctrl -> Repo: findByNroSerie()
Repo --> Ctrl: Meter
Ctrl -> Ctrl: Get latest reading
Ctrl -> Ctrl: Get previous reading
Ctrl -> Ctrl: Calculate consumption
Ctrl -> Ctrl: Get customer info
Ctrl -> Ctrl: Get VAT type
Ctrl --> UI: PeriodoFacturacionDTO
UI --> Operator: Display billing period
@enduml
```

---

## Error Handling

### Common Errors and Responses

#### Error 1: Meter Not Found
```
HTTP 404 Not Found
{
  "message": "Meter not found: MED009999"
}
```

#### Error 2: Duplicate Reading
```
HTTP 409 Conflict
{
  "message": "Reading already exists for this date"
}
```

#### Error 3: Invalid Reading Value
```
HTTP 400 Bad Request
{
  "message": "Reading value must be positive"
}
```

#### Error 4: Reading Not Greater Than Previous
```
HTTP 400 Bad Request
{
  "message": "Reading must be greater than previous reading"
}
```

#### Error 5: No Readings for Meter
```
HTTP 400 Bad Request
{
  "message": "No readings available for this meter"
}
```

---

## Performance Considerations

### Optimization Points

1. **Reading History Query:**
   - Index on (`id_medidor`, `fecha`)
   - Use pagination for long histories

2. **Invoice Generation:**
   - Batch operations where possible
   - Transaction management
   - Connection pooling

3. **Search Queries:**
   - Index on search columns
   - Use partial matching efficiently
   - Consider full-text search for large datasets

### Monitoring

- Query execution time
- Connection pool status
- Database locks
- Error rates

---

## Audit Trail

### Logging Recommendations

1. **Operation Logging:**
   - Who performed the operation
   - When it was performed
   - What data was changed

2. **Access Logging:**
   - Who accessed what data
   - When access occurred

3. **Error Logging:**
   - Error type and message
   - Context information
   - Stack trace (development only)

### Example Audit Trail

```sql
-- Create audit table
CREATE TABLE auditoria_operaciones (
    id_auditoria BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario VARCHAR(100),
    operacion VARCHAR(50),
    tabla VARCHAR(50),
    registro_id BIGINT,
    datos_anteriores TEXT,
    datos_nuevos TEXT,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Log reading registration
INSERT INTO auditoria_operaciones 
(usuario, operacion, tabla, registro_id, datos_nuevos)
VALUES 
('operador1', 'REGISTRAR_LECTURA', 'lecturas', 1234, 
'{"fecha":"2026-10-23","valor":1520.5,"medidor":456}');
```

---

## Workflow Best Practices

### For Operators

1. **Always verify meter serial** before entering reading
2. **Double-check reading values** before submission
3. **Confirm successful registration** before moving to next task
4. **Report discrepancies** immediately
5. **Keep documentation** of manual corrections

### For System Administrators

1. **Regular backups** prevent data loss
2. **Integrity checks** catch issues early
3. **Monitoring alerts** help respond quickly
4. **Documentation updates** keep procedures current
5. **User training** reduces errors

---

## Summary

This workflow documentation covers:

1. **Meter Reading Registration** - Step-by-step process for registering readings
2. **Invoice Generation** - Complete billing calculation workflow
3. **Billing Period Inquiry** - How to view billing information
4. **Customer Search** - Searching and retrieving customer data
5. **Meter Registration** - Adding new meters to system
6. **Troubleshooting** - How to investigate billing issues
7. **Data Integrity** - Verification and correction procedures

Each workflow includes:
- Precondition requirements
- Step-by-step flow
- Alternative paths
- Error handling
- Postconditions
- Sequence diagrams

For technical implementation details, refer to the corresponding service classes in the codebase.