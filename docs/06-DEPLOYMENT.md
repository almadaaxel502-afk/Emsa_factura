# Deployment and Setup Documentation

## Overview

This guide covers deployment and setup procedures for the Emsa_factura billing system. It includes environment setup, configuration, and deployment options.

## Prerequisites

### Software Requirements

1. **Java Development Kit (JDK)**
   - Version: 17 or higher
   - Download: [Adoptium](https://adoptium.net/) or [Oracle](https://www.oracle.com/java/technologies/downloads/)

2. **Apache Maven**
   - Version: 3.6 or higher
   - Download: [Maven](https://maven.apache.org/download.cgi)
   - Check version: `mvn --version`

3. **MySQL Server**
   - Version: 8.0 or higher
   - Download: [MySQL](https://dev.mysql.com/downloads/)
   - Alternative: Use Docker Compose (recommended for development)

4. **Git**
   - Version: 2.0 or higher
   - Check version: `git --version`

### Hardware Requirements

| Environment | CPU | RAM | Disk Space | Notes |
|-------------|-----|-----|------------|-------|
| Development | 2 cores | 4 GB | 20 GB | Local development |
| Testing | 2 cores | 4 GB | 20 GB | CI/CD environment |
| Production | 4 cores | 8 GB | 50 GB | With database |

---

## Quick Start (Docker Compose)

### For Development

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   cd Emsa_factura
   ```

2. **Start the services:**
   ```bash
   docker-compose up -d
   ```

3. **Verify services are running:**
   ```bash
   docker-compose ps
   ```

4. **Build and run the application:**
   ```bash
   ./mvnw spring-boot:run
   ```

5. **Access the application:**
   - API: `http://localhost:8080/api`
   - H2 Console (if enabled): `http://localhost:8080/h2-console`

### Services Started

- **MySQL Database:** `localhost:3307`
- **Application:** `localhost:8080`

### Docker Compose Configuration

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.3
    container_name: emsa-db
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: emsa
      MYSQL_USER: emsa
      MYSQL_PASSWORD: emsa
      TZ: 'America/Argentina/Buenos_Aires'
    ports:
      - "3307:3306"
    volumes:
      - emsa_mysql_data:/var/lib/mysql
    networks:
      - emsa-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      timeout: 5s
      retries: 10

volumes:
  emsa_mysql_data:
    driver: local

networks:
  emsa-network:
    driver: bridge
```

---

## Manual Installation (Non-Docker)

### Step 1: Install Java

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install openjdk-17-jdk
```

**macOS (Homebrew):**
```bash
brew install openjdk@17
```

**Windows:**
- Download from Adoptium
- Install and add to PATH

### Step 2: Install MySQL

**Ubuntu/Debian:**
```bash
sudo apt install mysql-server
sudo mysql_secure_installation
```

**macOS (Homebrew):**
```bash
brew install mysql
brew services start mysql
```

**Windows:**
- Download from MySQL website
- Run installer

### Step 3: Create Database

```sql
-- Connect to MySQL
mysql -u root -p

-- Create database and user
CREATE DATABASE emsa;
CREATE USER 'emsa'@'localhost' IDENTIFIED BY 'emsa';
GRANT ALL PRIVILEGES ON emsa.* TO 'emsa'@'localhost';
FLUSH PRIVILEGES;

-- Exit
EXIT;
```

### Step 4: Configure Application

**Create `application.properties`:**

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/emsa
spring.datasource.username=emsa
spring.datasource.password=emsa
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.open-in-view=false

# Server Configuration
server.port=8080
server.error.include-message=always

# Logging Configuration
logging.level.root=INFO
logging.level.ar.com.itec1misiones.emsa=DEBUG
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

### Step 5: Build the Application

```bash
./mvnw clean package
```

### Step 6: Run the Application

```bash
java -jar target/emsa-factura-0.0.1-SNAPSHOT.jar
```

---

## Environment Configuration

### Development Environment

**Configuration File:** `application-dev.yml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/emsa_dev
    username: emsa
    password: dev_password
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    open-in-view: false
  profile:
    active: dev

server:
  port: 8080
  error:
    include-message: always

logging:
  level:
    root: DEBUG
    ar.com.itec1misiones.emsa: DEBUG
    org.hibernate.SQL: DEBUG
```

### Testing Environment

**Configuration File:** `application-test.yml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/emsa_test
    username: emsa
    password: test_password
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    open-in-view: false
  profile:
    active: test

server:
  port: 8081

logging:
  level:
    root: INFO
    ar.com.itec1misiones.emsa: INFO
```

### Production Environment

**Configuration File:** `application-prod.yml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://prod-server:3306/emsa
    username: emsa_prod
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
  jpa:
    hibernate:
      ddl-auto: none
    show-sql: false
    open-in-view: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
        format_sql: false
  profile:
    active: prod

server:
  port: 8080
  error:
    include-message: false
    include-binding: false

logging:
  level:
    root: WARN
    ar.com.itec1misiones.emsa: INFO
    org.hibernate.SQL: WARN
```

### Profile Activation

**Command Line:**
```bash
java -jar app.jar --spring.profiles.active=prod
```

**Environment Variable:**
```bash
export SPRING_PROFILES_ACTIVE=prod
java -jar app.jar
```

**Configuration:**
```properties
spring.profiles.active=prod
```

---

## Database Migration

### Using Flyway

1. **Add Flyway dependency to `pom.xml`:**

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-mysql</artifactId>
</dependency>
```

2. **Create migration scripts:**

```
src/main/resources/db/migration/
├── V1__create_initial_schema.sql
├── V2__add_new_columns.sql
└── V3__add_indexes.sql
```

3. **Example migration script:**

```sql
-- V1__create_initial_schema.sql
CREATE TABLE clientes (
    id_cliente BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255),
    apellido VARCHAR(255),
    identificacion_nacional VARCHAR(255),
    direccion VARCHAR(255),
    id_tipo_iva BIGINT,
    id_usuario BIGINT,
    FOREIGN KEY (id_tipo_iva) REFERENCES tipos_iva(id_tipo_iva),
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
) ENGINE=InnoDB;

-- Additional tables...
```

4. **Configure Flyway:**

```properties
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.locations=classpath:db/migration
```

---

## Docker Deployment

### Build Docker Image

**Create `Dockerfile`:**

```dockerfile
FROM openjdk:17-jdk-slim
VOLUME /tmp
COPY target/emsa-factura-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

**Build image:**
```bash
docker build -t emsa-factura .
```

**Run container:**
```bash
docker run -d \
  --name emsa-app \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  --link emsa-db:mysql \
  emsa-factura
```

### Docker Compose Deployment

**Updated `docker-compose.yml` for production:**

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.3
    container_name: emsa-db
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: emsa
      MYSQL_USER: emsa
      MYSQL_PASSWORD: ${MYSQL_PASSWORD}
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./mysql/init:/docker-entrypoint-initdb.d
    networks:
      - emsa-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      timeout: 5s
      retries: 10
    deploy:
      resources:
        limits:
          memory: 2G

  app:
    image: emsa-factura:latest
    container_name: emsa-app
    restart: unless-stopped
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/emsa
      SPRING_DATASOURCE_USERNAME: emsa
      SPRING_DATASOURCE_PASSWORD: ${MYSQL_PASSWORD}
    depends_on:
      mysql:
        condition: service_healthy
    networks:
      - emsa-network
    deploy:
      resources:
        limits:
          memory: 1G
      replicas: 3

volumes:
  mysql_data:
    driver: local

networks:
  emsa-network:
    driver: bridge
```

**Deploy:**
```bash
docker-compose up -d
```

---

## Kubernetes Deployment

### Basic Kubernetes Manifest

**Create `deployment.yaml`:**

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: emsa-factura
spec:
  replicas: 3
  selector:
    matchLabels:
      app: emsa-factura
  template:
    metadata:
      labels:
        app: emsa-factura
    spec:
      containers:
      - name: emsa-factura
        image: emsa-factura:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: SPRING_DATASOURCE_URL
          valueFrom:
            secretKeyRef:
              name: emsa-db-secret
              key: url
        - name: SPRING_DATASOURCE_USERNAME
          valueFrom:
            secretKeyRef:
              name: emsa-db-secret
              key: username
        - name: SPRING_DATASOURCE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: emsa-db-secret
              key: password
        resources:
          limits:
            memory: "1Gi"
            cpu: "500m"
          requests:
            memory: "512Mi"
            cpu: "250m"
        livenessProbe:
          httpGet:
            path: /api/actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /api/actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: emsa-factura-service
spec:
  selector:
    app: emsa-factura
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
  type: LoadBalancer
```

**Apply deployment:**
```bash
kubectl apply -f deployment.yaml
```

---

## Health Checks

### Application Health Endpoints

**Add Spring Boot Actuator:**

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

**Configure endpoints:**

```properties
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=when-authorized
```

**Health Check Endpoint:**
```
GET /actuator/health
```

**Response:**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "MySQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 107374182400,
        "free": 85899345920,
        "threshold": 10485760
      }
    }
  }
}
```

---

## Monitoring

### Metrics

**Actuator Metrics Endpoint:**
```
GET /actuator/metrics
GET /actuator/metrics/{metricName}
```

**Key Metrics to Monitor:**
- `jvm.memory.used`
- `jvm.gc.pause`
- `http.server.requests`
- `database.pool.size`
- `hibernate.query.time`

### Logging

**Production Logging Configuration:**

```properties
logging.level.root=INFO
logging.file.name=/var/log/emsa-factura/app.log
logging.file.max-size=10MB
logging.file.max-history=30
```

**Log Format:**

```
2026-10-23 10:30:00.123 INFO 12345 --- [nio-8080-exec-1] a.i.e.s.FacturacionService : Invoice generated successfully
```

---

## Backup and Recovery

### Database Backup

**Manual Backup:**
```bash
mysqldump -u emsa -p emsa > backup_$(date +%Y%m%d).sql
```

**Automated Backup (Cron):**

```bash
# Add to crontab for daily backup at 2 AM
0 2 * * * mysqldump -u emsa -p emsa | gzip > /backup/emsa_$(date +\%Y\%m\%d).sql.gz
```

### Database Restore

```bash
mysql -u emsa -p emsa < backup_20240115.sql
```

### Application Backup

**Backup Configuration Files:**
```bash
cp application-prod.yml application-prod.yml.backup
```

**Backup Data Directory:**
```bash
tar -czvf data_backup_$(date +%Y%m%d).tar.gz data/
```

---

## Security Considerations

### Production Checklist

1. **Change Default Passwords:**
   ```yaml
   MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}  # From environment
   ```

2. **Use SSL/TLS:**
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/emsa?useSSL=true&requireSSL=true
   ```

3. **Enable CSRF Protection:**
   ```java
   http.csrf(cs -> cs.enable());
   ```

4. **Configure CORS:**
   ```java
   @Configuration
   public class CorsConfig {
       @Bean
       public WebMvcConfigurer corsConfigurer() {
           return new WebMvcConfigurer() {
               @Override
               public void addCorsMappings(CorsRegistry registry) {
                   registry.addMapping("/api/**")
                           .allowedOrigins("https://yourdomain.com")
                           .allowedMethods("GET", "POST", "PUT", "DELETE");
               }
           };
       }
   }
   ```

5. **Use Environment Variables for Secrets:**
   ```bash
   export DB_PASSWORD=secure_password
   export JWT_SECRET=secure_jwt_secret
   ```

6. **Enable HTTPS:**
   ```properties
   server.port=8443
   server.ssl.enabled=true
   server.ssl.key-store=classpath:keystore.p12
   server.ssl.key-store-password=${KEYSTORE_PASSWORD}
   server.ssl.key-store-type=PKCS12
   ```

---

## Troubleshooting

### Common Issues

#### Issue 1: Database Connection Failed

**Error:**
```
Cannot determine embedded database driver class for database type NONE
```

**Solution:**
- Verify MySQL is running
- Check database URL, username, password
- Ensure MySQL connector dependency is in `pom.xml`

#### Issue 2: Port Already in Use

**Error:**
```
Web server failed to start. Port 8080 was already in use
```

**Solution:**
- Change port in `application.properties`:
  ```properties
  server.port=8081
  ```
- Or kill process on port 8080:
  ```bash
  lsof -i :8080 | grep LISTEN
  kill -9 <PID>
  ```

#### Issue 3: Application Startup Timeout

**Error:**
```
Application did not start within the given time limit
```

**Solution:**
- Increase timeout in Docker:
  ```yaml
  healthcheck:
    timeout: 60s
  ```
- Check database connection
- Review logs for errors

#### Issue 4: Memory Issues

**Error:**
```
Java heap space
```

**Solution:**
- Increase JVM memory:
  ```bash
  java -Xmx1G -jar app.jar
  ```
- Or set in Docker:
  ```yaml
  deploy:
    resources:
      limits:
        memory: 2G
  ```

---

## Performance Tuning

### JVM Tuning

```bash
java -jar app.jar \
  -Xms512m \
  -Xmx1g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200
```

### Database Connection Pooling

```properties
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
```

### Caching

```java
@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("tarifas", "impuestos");
    }
}
```

---

## Summary

This deployment guide covers:

1. **Prerequisites** - Required software and hardware
2. **Quick Start (Docker)** - Fast setup with Docker Compose
3. **Manual Installation** - Step-by-step non-Docker setup
4. **Environment Configuration** - Dev, test, prod profiles
5. **Database Migration** - Using Flyway
6. **Docker Deployment** - Containerized deployment
7. **Kubernetes Deployment** - Orchestration setup
8. **Health Checks** - Monitoring and metrics
9. **Backup and Recovery** - Data protection
10. **Security** - Production security checklist
11. **Troubleshooting** - Common issues and solutions
12. **Performance Tuning** - Optimization tips

For detailed API and architecture information, refer to the other documentation files in the `docs/` directory.