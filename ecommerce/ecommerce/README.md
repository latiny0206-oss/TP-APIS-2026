# Ecommerce Service

## Perfiles disponibles
- `local` (default): usa H2 en memoria y no requiere MySQL.
- `mysql`: usa MySQL externo (local o Docker).
- `test`: usado por la suite de tests.

## Requisitos
- Java 17 recomendado por `pom.xml`.
- Maven Wrapper (`./mvnw`).

## Arranque rapido

### 1) Desarrollo local sin MySQL (default)
```bash
./mvnw spring-boot:run
```

Si el puerto 8080 esta ocupado:
```bash
SERVER_PORT=8081 ./mvnw spring-boot:run
```

### 2) Desarrollo con MySQL
```bash
SPRING_PROFILES_ACTIVE=mysql DB_HOST=localhost DB_PORT=3306 DB_NAME=trekking_ecommerce DB_USERNAME=root DB_PASSWORD=12345 ./mvnw spring-boot:run
```

Con puerto alternativo:
```bash
SPRING_PROFILES_ACTIVE=mysql SERVER_PORT=8081 DB_HOST=localhost DB_PORT=3306 DB_NAME=trekking_ecommerce DB_USERNAME=root DB_PASSWORD=12345 ./mvnw spring-boot:run
```

### 3) Levantar MySQL con Docker
```bash
docker compose up -d
```

## Troubleshooting rapido
- Ver puerto 8080 en uso:
```bash
lsof -nP -iTCP:8080 -sTCP:LISTEN
```
- Matar proceso que ocupa 8080 (si aplica):
```bash
kill -9 <PID>
```

