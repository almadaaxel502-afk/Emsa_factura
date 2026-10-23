# Troubleshooting and Maintenance Documentation

## Overview

This guide covers troubleshooting common issues that may arise during operation of the Emsa_factura billing system, along with maintenance procedures to ensure system health.

---

## Troubleshooting Guide

### 1. Database Connection Issues

#### Symptom 1.1: Connection Refused

**Error:**
```
Cannot connect to MySQL server on localhost:3306 (connection refused)
```

**Possible Causes:**
- MySQL server is not running
- Incorrect host/port configuration
- Network/firewall blocking connection

**Resolution Steps:**

1. **Check MySQL Status:**
   ```bash
   # Docker
   docker ps | grep mysql
   
   # Native
   sudo systemctl status mysql
   sudo service mysql status
   ```

2. **Start MySQL if Needed:**
   ```bash
   # Docker
   docker-compose up -d mysql
   
   # Native
   sudo systemctl start mysql
   ```

3. **Verify Connection:**
   ```bash
   mysql -h localhost -P 3306 -u emsa -p
   ```

4. **Check Application Configuration:**
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/emsa
   spring.datasource.username=emsa
   spring.datasource.password=emsa
   ```

---

#### Symptom 1.2: Authentication Failed

**Error:**
```
Access denied for user 'emsa'@'localhost'
```

**Possible Causes:**
- Wrong username/password
- User doesn't have proper privileges
- User doesn't exist

**Resolution Steps:**

1. **Verify Database User Exists:**
   ```sql
   SELECT user, host FROM mysql.user WHERE user='emsa';
   ```

2. **Check User Privileges:**
   ```sql
   SHOW GRANTS FOR 'emsa'@'localhost';
   ```

3. **Recreate User if Needed:**
   ```sql
   CREATE USER 'emsa'@'localhost' IDENTIFIED BY 'emsa';
   GRANT ALL PRIVILEGES ON emsa.* TO 'emsa'@'localhost';
   FLUSH PRIVILEGES;
   ```

4. **Update Application Configuration:**
   ```properties
   spring.datasource.password=correct_password
   ```

---

#### Symptom 1.3: Database Not Found

**Error:**
```
Unknown database 'emsa'
```

**Possible Causes:**
- Database doesn't exist
- Wrong database name in configuration

**Resolution Steps:**

1. **Create Database:**
   ```sql
   CREATE DATABASE emsa;
   ```

2. **Verify Database Exists:**
   ```sql
   SHOW DATABASES LIKE 'emsa';
   ```

3. **Check Configuration:**
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/emsa
   ```

---

#### Symptom 1.4: Connection Timeout

**Error:**
```
Communications link failure
The last packet sent successfully to the server was 0 milliseconds ago
```

**Possible Causes:**
- MySQL server not responding
- Network latency
- Connection pool exhausted

**Resolution Steps:**

1. **Check MySQL Response:**
   ```bash
   mysqladmin ping -h localhost -P 3306 -u emsa -p
   ```

2. **Increase Connection Timeout:**
   ```properties
   spring.datasource.hikari.connection-timeout=60000
   spring.datasource.hikari.maximum-pool-size=20
   ```

3. **Check Connection Pool Status:**
   ```java
   // Add monitoring
   management.endpoints.web.exposure.include=health,metrics
   ```

---

### 2. Billing Calculation Errors

#### Symptom 2.1: Negative Consumption

**Error:**
```
Calculated consumption is negative: -50.0
```

**Possible Causes:**
- Reading value is less than previous reading
- Data entry error
- Meter reset without proper handling

**Resolution Steps:**

1. **Check Readings:**
   ```sql
   SELECT l.id_lectura, l.fecha, l.valor, l.id_medidor
   FROM lecturas l
   WHERE l.id_medidor = ?
   ORDER BY l.fecha DESC
   LIMIT 5;
   ```

2. **Identify Problematic Reading:**
   - Look for reading with value less than previous
   - Check for data entry errors

3. **Correct Data:**
   ```sql
   -- Update the problematic reading
   UPDATE lecturas SET valor = ? WHERE id_lectura = ?;
   ```

4. **Re-generate Invoice:**
   - Delete incorrect invoice
   - Re-generate with corrected readings

5. **Add Prevention:**
   - Ensure application validates reading > previous reading
   - Train operators on proper data entry

---

#### Symptom 2.2: Incorrect Tax Amount

**Symptoms:**
- Tax amount doesn't match expected rate
- Customer VAT type not applied correctly

**Possible Causes:**
- Wrong VAT type assigned to customer
- Tax rate configuration incorrect
- Calculation logic error

**Resolution Steps:**

1. **Check Customer VAT Type:**
   ```sql
   SELECT c.id_cliente, c.nombre, c.apellido, t.descripcion, t.tasa
   FROM clientes c
   JOIN tipos_iva t ON c.id_tipo_iva = t.id_tipo_iva
   WHERE c.id_cliente = ?;
   ```

2. **Verify Tax Rate:**
   ```sql
   SELECT * FROM tipos_iva WHERE id_tipo_iva = ?;
   ```

3. **Check Invoice Calculation:**
   ```sql
   SELECT f.id_factura, f.subtotal, f.importe_iva, f.total
   FROM facturas f
   WHERE f.id_factura = ?;
   ```

4. **Verify Business Logic:**
   - Check `FacturacionService.liquidarPorLectura()`
   - Ensure VAT type is retrieved correctly
   - Verify tax calculation formula

5. **Correct Data:**
   ```sql
   -- Update customer VAT type if wrong
   UPDATE clientes SET id_tipo_iva = ? WHERE id_cliente = ?;
   ```

---

#### Symptom 2.3: Duplicate Invoice

**Error:**
```
Invoice already exists for this period
```

**Possible Causes:**
- Multiple generation attempts
- System error after first attempt but before confirmation
- User double-clicked submit

**Resolution Steps:**

1. **Check for Existing Invoice:**
   ```sql
   SELECT * FROM facturas 
   WHERE id_medidor = ? AND fecha = ?;
   ```

2. **If Invoice Exists:**
   - Verify it's valid
   - Check line items are correct
   - Provide invoice to user

3. **If Invoice Invalid:**
   - Delete invoice
   - Re-generate

4. **Add Prevention:**
   - Add unique constraint on (id_medidor, fecha)
   - Improve UI to prevent double submission
   - Use optimistic locking

---

### 3. Data Integrity Issues

#### Symptom 3.1: Orphan Readings

**Symptoms:**
- Readings exist without associated meter
- Foreign key violations

**Possible Causes:**
- Data deletion without cascade
- Manual database modifications
- Application bug

**Resolution Steps:**

1. **Identify Orphan Readings:**
   ```sql
   SELECT l.* FROM lecturas l
   LEFT JOIN medidores m ON l.id_medidor = m.id_medidor
   WHERE m.id_medidor IS NULL;
   ```

2. **Handle Orphans:**
   ```sql
   -- Option 1: Delete orphans
   DELETE FROM lecturas WHERE id_medidor NOT IN (SELECT id_medidor FROM medidores);
   
   -- Option 2: Restore meter if possible
   UPDATE lecturas SET id_medidor = ? WHERE id_lectura = ?;
   ```

3. **Prevent Future Orphans:**
   - Ensure foreign key constraints are enabled
   - Use CASCADE DELETE appropriately
   - Add application-level validation

---

#### Symptom 3.2: Duplicate Meter Serial Numbers

**Error:**
```
Duplicate entry 'MED001234' for key 'UK_...'
```

**Possible Causes:**
- Manual database insert without validation
- Application bug
- Data import error

**Resolution Steps:**

1. **Find Duplicates:**
   ```sql
   SELECT nro_serie, COUNT(*) as count
   FROM medidores
   GROUP BY nro_serie
   HAVING COUNT(*) > 1;
   ```

2. **Resolve Duplicates:**
   ```sql
   -- Delete duplicate (keep one)
   DELETE FROM medidores WHERE id_medidor = ?;
   
   -- Or update one to new serial
   UPDATE medidores SET nro_serie = ? WHERE id_medidor = ?;
   ```

3. **Add Validation:**
   - Ensure application checks for duplicates before save
   - Add unique constraint on nro_serie

---

#### Symptom 3.3: Calculation Inconsistency

**Symptoms:**
- Invoice total doesn't match subtotal + taxes
- Consumption doesn't match readings

**Possible Causes:**
- Data modified after invoice generation
- Calculation bug
- Currency rounding issues

**Resolution Steps:**

1. **Verify Invoice Calculation:**
   ```sql
   SELECT 
       f.id_factura,
       f.subtotal,
       f.importe_iva,
       f.total,
       (f.subtotal + f.importe_iva) as calculated_total
   FROM facturas f
   WHERE f.id_factura = ?;
   ```

2. **Check Line Items:**
   ```sql
   SELECT SUM(cantidad * tasa) as expected_subtotal
   FROM items_consumo
   WHERE id_factura = ?;
   ```

3. **Compare Readings:**
   ```sql
   SELECT 
       l_actual.valor - l_anterior.valor as calculated_consumo,
       (SELECT SUM(cantidad) FROM items_consumo WHERE id_factura = ?) as invoice_consumo
   FROM lecturas l_actual
   JOIN facturas f ON l_actual.id_lectura = (
       SELECT id_lectura FROM lecturas 
       WHERE id_medidor = f.id_medidor AND fecha = f.fecha
   )
   JOIN lecturas l_anterior ON l_anterior.id_medidor = f.id_medidor
   WHERE f.id_factura = ?;
   ```

4. **Correct Issues:**
   - Update invoice if calculation was wrong
   - Re-generate invoice if data changed

---

### 4. Authentication and Security Issues

#### Symptom 4.1: Access Denied

**Error:**
```
403 Forbidden or Access Denied
```

**Possible Causes:**
- User not authenticated
- Insufficient permissions
- CSRF token missing

**Resolution Steps:**

1. **Check Authentication:**
   - Verify user logged in
   - Check session validity

2. **Review Security Configuration:**
   ```java
   http.authorizeHttpRequests(a -> a
       .requestMatchers("/api/**").authenticated()
       .anyRequest().permitAll());
   ```

3. **Add CSRF Token (if needed):**
   ```javascript
   // Include CSRF token in requests
   const csrfToken = document.querySelector('input[name="_csrf"]').value;
   ```

4. **Enable Debug Logging:**
   ```properties
   logging.level.org.springframework.security=DEBUG
   ```

---

#### Symptom 4.2: Login Failed

**Error:**
```
Invalid credentials or Login failed
```

**Possible Causes:**
- Wrong username/password
- User account locked
- Password encoding mismatch

**Resolution Steps:**

1. **Check User Exists:**
   ```sql
   SELECT * FROM usuarios WHERE nombre = 'username';
   ```

2. **Reset Password:**
   ```sql
   UPDATE usuarios SET contrasena = 'encoded_password' WHERE nombre = 'username';
   ```

3. **Check Password Encoding:**
   ```java
   // Verify password encoder configuration
   @Bean
   public PasswordEncoder passwordEncoder() {
       return new BCryptPasswordEncoder();
   }
   ```

4. **Test Login:**
   - Use correct credentials
   - Check for typos

---

### 5. Performance Issues

#### Symptom 5.1: Slow Queries

**Symptoms:**
- API responses taking too long
- Database queries timing out

**Possible Causes:**
- Missing indexes
- Complex queries
- Large datasets

**Resolution Steps:**

1. **Enable Query Logging:**
   ```properties
   spring.jpa.show-sql=true
   logging.level.org.hibernate.SQL=DEBUG
   ```

2. **Check Query Execution Plan:**
   ```sql
   EXPLAIN SELECT * FROM lecturas WHERE id_medidor = ?;
   ```

3. **Add Missing Indexes:**
   ```sql
   CREATE INDEX idx_lecturas_medidor ON lecturas(id_medidor);
   CREATE INDEX idx_lecturas_fecha ON lecturas(fecha);
   CREATE INDEX idx_medidores_nro_serie ON medidores(nro_serie);
   ```

4. **Optimize Queries:**
   - Use pagination for large result sets
   - Add appropriate WHERE clauses
   - Avoid N+1 queries with JOINs

---

#### Symptom 5.2: High Memory Usage

**Symptoms:**
- Application running out of memory
- GC pauses causing timeouts

**Possible Causes:**
- Memory leak
- Large result sets
- Insufficient heap size

**Resolution Steps:**

1. **Check Memory Usage:**
   ```bash
   # JVM flags
   java -Xmx1g -jar app.jar
   ```

2. **Analyze Heap Dump:**
   ```bash
   jmap -dump:format=b,file=heapdump.hprof <pid>
   ```

3. **Optimize Data Loading:**
   - Use pagination
   - Fetch only required fields
   - Use DTOs instead of entities

4. **Configure JVM:**
   ```bash
   java -Xms512m -Xmx1g -XX:+UseG1GC -jar app.jar
   ```

---

#### Symptom 5.3: Connection Pool Exhausted

**Error:**
```
HikariPool-1 - Connection is not available
```

**Possible Causes:**
- Connection pool too small
- Connections not being released
- Database server limits

**Resolution Steps:**

1. **Check Connection Pool Stats:**
   ```properties
   management.endpoints.web.exposure.include=health,metrics
   ```

2. **Increase Pool Size:**
   ```properties
   spring.datasource.hikari.maximum-pool-size=20
   spring.datasource.hikari.minimum-idle=10
   ```

3. **Check for Connection Leaks:**
   ```properties
   spring.datasource.hikari.leak-detection-threshold=2000
   ```

4. **Review Connection Usage:**
   - Ensure @Transactional is used correctly
   - Close sessions properly
   - Avoid long-running transactions

---

## Maintenance Procedures

### 1. Database Maintenance

#### Daily Tasks

1. **Check Database Status:**
   ```bash
   docker-compose exec mysql mysqladmin -u root -p"root" ping
   ```

2. **Review Error Logs:**
   ```bash
   docker-compose logs mysql
   ```

3. **Monitor Disk Space:**
   ```bash
   df -h
   docker volume ls
   ```

#### Weekly Tasks

1. **Backup Database:**
   ```bash
   docker-compose exec mysql mysqldump -u root -p"root" emsa | gzip > backup/backup_$(date +%Y%m%d).sql.gz
   ```

2. **Analyze Tables:**
   ```sql
   ANALYZE TABLE clientes, medidores, lecturas, facturas;
   ```

3. **Check for Orphan Records:**
   ```sql
   -- Check for orphan readings
   SELECT COUNT(*) FROM lecturas l
   LEFT JOIN medidores m ON l.id_medidor = m.id_medidor
   WHERE m.id_medidor IS NULL;
   
   -- Check for orphan invoices
   SELECT COUNT(*) FROM facturas f
   LEFT JOIN medidores m ON f.id_medidor = m.id_medidor
   WHERE m.id_medidor IS NULL;
   ```

#### Monthly Tasks

1. **Optimize Tables:**
   ```sql
   OPTIMIZE TABLE clientes, medidores, lecturas, facturas;
   ```

2. **Review Index Usage:**
   ```sql
   SELECT 
       index_name,
       count_star,
       count_read
   FROM performance_schema.index_statistics
   WHERE table_name IN ('clientes', 'medidores', 'lecturas', 'facturas');
   ```

3. **Archive Old Data:**
   ```sql
   -- Move old readings to archive (optional)
   INSERT INTO lecturas_archivo SELECT * FROM lecturas WHERE fecha < '2023-01-01';
   DELETE FROM lecturas WHERE fecha < '2023-01-01';
   ```

---

### 2. Application Maintenance

#### Daily Tasks

1. **Review Application Logs:**
   ```bash
   tail -f logs/app.log | grep -i "error\|warn"
   ```

2. **Check Application Health:**
   ```bash
   curl http://localhost:8080/actuator/health
   ```

3. **Monitor Metrics:**
   ```bash
   curl http://localhost:8080/actuator/metrics
   ```

#### Weekly Tasks

1. **Update Dependencies:**
   ```bash
   ./mvnw versions:display-dependency-updates
   ./mvnw versions:display-plugin-updates
   ```

2. **Review Performance:**
   - Check slow query log
   - Review memory usage
   - Analyze GC logs

3. **Clean Temporary Files:**
   ```bash
   rm -rf target/*.tmp
   rm -rf logs/*.old
   ```

---

### 3. Security Maintenance

#### Daily Tasks

1. **Review Security Logs:**
   - Check for failed login attempts
   - Monitor for suspicious activity

2. **Update Security Patches:**
   - Monitor for security advisories
   - Apply updates promptly

#### Weekly Tasks

1. **Review Access Logs:**
   - Check for unauthorized access attempts
   - Verify user activity is appropriate

2. **Rotate Secrets:**
   - Update database passwords
   - Update API keys

---

### 4. Backup and Recovery

#### Backup Strategy

1. **Daily Backups:**
   ```bash
   # Create backup script
   #!/bin/bash
   docker-compose exec mysql mysqldump -u root -p"root" emsa | gzip > /backup/emsa_$(date +%Y%m%d).sql.gz
   
   # Keep last 30 days
   find /backup -name "emsa_*.sql.gz" -mtime +30 -delete
   ```

2. **Automate Backups:**
   ```bash
   # Add to crontab
   0 2 * * * /path/to/backup_script.sh
   ```

3. **Test Restoration:**
   ```bash
   # Regularly test backup restoration
   docker-compose exec mysql mysql -u root -p"root" emsa < /backup/emsa_20240115.sql.gz
   ```

---

### 5. Monitoring and Alerting

#### Key Metrics to Monitor

1. **Database:**
   - Connection pool usage
   - Query execution time
   - Replication lag (if applicable)

2. **Application:**
   - Response time (p50, p95, p99)
   - Error rate
   - Memory usage

3. **Business:**
   - Number of readings per day
   - Invoice generation rate
   - Error occurrences

#### Alerting Thresholds

1. **Critical:**
   - Database down
   - Error rate > 5%
   - Response time > 5s

2. **Warning:**
   - Connection pool > 80%
   - Memory usage > 80%
   - Response time > 2s

---

## Diagnostic Commands Reference

### Database Diagnostics

```bash
# Check database status
docker-compose exec mysql mysqladmin -u root -p"root" status

# Check connections
docker-compose exec mysql mysql -u root -p"root" -e "SHOW PROCESSLIST;"

# Check table size
docker-compose exec mysql mysql -u root -p"root" -e "SELECT table_name, table_rows, data_length FROM information_schema.tables WHERE table_schema = 'emsa';"

# Check slow queries
docker-compose exec mysql mysql -u root -p"root" -e "SHOW VARIABLES LIKE 'slow_query%';"
```

### Application Diagnostics

```bash
# Check application health
curl http://localhost:8080/actuator/health

# Check application metrics
curl http://localhost:8080/actuator/metrics

# Check thread dump
curl http://localhost:8080/actuator/threaddump

# Check heap usage
curl http://localhost:8080/actuator/metrics/jvm.memory.used
```

---

## Common Error Messages Reference

### HTTP Errors

| Error Code | Message | Cause | Resolution |
|------------|---------|-------|------------|
| 400 | Bad Request | Invalid input | Validate input data |
| 404 | Not Found | Resource doesn't exist | Check ID exists |
| 409 | Conflict | Duplicate data | Check for duplicates |
| 500 | Internal Server Error | Application error | Check logs |
| 503 | Service Unavailable | Service down | Check service status |

### Business Errors

| Error | Message | Cause | Resolution |
|-------|---------|-------|------------|
| Duplicate Reading | "Reading already exists for this date" | Duplicate date | Check reading date |
| Negative Consumption | "Calculated consumption is negative" | Reading < previous | Check reading values |
| No Readings | "No readings available for meter" | No reading data | Add readings first |
| Invalid Meter | "Meter not found" | Meter doesn't exist | Verify meter serial |

---

## Summary

This troubleshooting guide covers:

1. **Database Connection Issues** - Connection refused, auth failures, timeouts
2. **Billing Calculation Errors** - Negative consumption, incorrect taxes, duplicates
3. **Data Integrity Issues** - Orphan records, duplicates, calculation inconsistency
4. **Authentication and Security** - Access denied, login failures
5. **Performance Issues** - Slow queries, high memory, connection pool exhaustion
6. **Maintenance Procedures** - Daily, weekly, monthly tasks
7. **Backup and Recovery** - Strategy and testing
8. **Monitoring and Alerting** - Key metrics and thresholds
9. **Diagnostic Commands** - Quick reference
10. **Error Messages** - Common issues and resolutions

Regular maintenance and monitoring will help prevent most issues and ensure quick resolution when they occur.