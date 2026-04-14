# Estado del Proyecto — Ecommerce de Trekking

---

## 1. Resumen general

Aplicación REST API desarrollada en Spring Boot 3 para un e-commerce de productos de
trekking. Implementa un modelo de dominio completo con gestión de usuarios, catálogo de
productos (con variantes por talla/color/material), carrito de compras, órdenes y
descuentos. La arquitectura sigue el patrón de capas clásico de Spring Boot
(Controller → Service → Repository → Entity).

El proyecto se encuentra en un estado **funcional, seguro y documentado**: la base de
datos MySQL está conectada y operativa, todas las tablas fueron creadas por Hibernate,
la autenticación JWT está completamente implementada con rutas protegidas por rol,
CORS está configurado, y Swagger UI está disponible en `/swagger-ui.html`.

---

## 2. Stack y configuración

| Componente          | Valor                                                         |
|---------------------|---------------------------------------------------------------|
| Spring Boot version | 3.5.12                                                        |
| Java version        | JDK 17 (Eclipse Temurin)                                      |
| Base de datos (prod)| MySQL 9.6 — `trekking_ecommerce` en `localhost:3306`          |
| Base de datos (test)| H2 en memoria — `trekking_test`                               |
| Puerto              | 8080                                                          |
| Perfiles definidos  | `(default)`, `test`                                           |
| DDL auto            | `update` (prod) / `create-drop` (test)                        |
| JWT secret          | Hardcodeado en `application.properties` ⚠️                    |
| JWT expiración      | 86 400 000 ms (24 h)                                          |
| JWT librería        | `io.jsonwebtoken:jjwt` 0.12.3                                 |
| Swagger UI          | `http://localhost:8080/swagger-ui.html` ✅                     |
| OpenAPI JSON        | `http://localhost:8080/api-docs`                               |
| CORS                | Habilitado para todos los orígenes (`allowedOriginPatterns=*`) |
| ORM                 | Hibernate 6 / Spring Data JPA                                 |
| Gestor de build     | Maven (wrapper `mvnw`)                                        |

---

## 3. Entidades implementadas

### Usuario
**Atributos:** `id`, `username` (único), `email` (único), `password` (hasheada),
`nombre`, `apellido`, `rol` (`ADMIN`/`CLIENTE`), `estado` (`ACTIVO`/`INACTIVO`).
**Relaciones:** `@OneToMany → Carrito`, `@OneToMany → Orden`.

### Producto
**Atributos:** `id`, `nombre`, `descripcion`, `precioBase`, `estado`
(`ACTIVO`/`PAUSADO`/`ELIMINADO`).
**Relaciones:** `@ManyToOne → Marca`, `@ManyToOne → Categoria`,
`@OneToMany → VarianteProducto`, `@OneToMany → Foto`.

### Categoria
**Atributos:** `id`, `nombre`, `descripcion`.
**Relaciones:** `@OneToMany → Producto`.

### Marca
**Atributos:** `id`, `nombre`, `descripcion`.
**Relaciones:** `@OneToMany → Producto`.

### VarianteProducto
**Atributos:** `id`, `color`, `talla`, `material`, `peso`, `stock`, `precio`,
`estacion` (`PRIMAVERA`/`VERANO`/`OTONO`/`INVIERNO`).
**Relaciones:** `@ManyToOne → Producto`, `@OneToMany → ItemCarrito`,
`@OneToMany → ItemOrden`.

### Carrito
**Atributos:** `id`, `montoTotal`, `estado` (`ACTIVO`/`VACIO`/`ABANDONADO`/`CONVERTIDO`),
`fechaUltimaModificacion` (se actualiza automáticamente en cada `save` vía `@PrePersist`/`@PreUpdate`).
**Relaciones:** `@ManyToOne → Usuario`, `@ManyToOne → Descuento (nullable)`,
`@OneToMany → ItemCarrito`, `@OneToMany → Orden`.

### ItemCarrito
**Atributos:** `id`, `cantidad`, `precioUnitario`.
**Relaciones:** `@ManyToOne → Carrito`, `@ManyToOne → VarianteProducto`.

### Orden
**Atributos:** `id`, `fechaCreacion`, `montoFinal`, `estado`
(`PENDIENTE`/`CONFIRMADA`/`ENVIADA`/`ENTREGADA`/`CANCELADA`).
**Relaciones:** `@ManyToOne → Usuario`, `@ManyToOne → Carrito (nullable)`,
`@ManyToOne → Descuento (nullable)`, `@OneToMany → ItemOrden`.

### ItemOrden
**Atributos:** `id`, `cantidad`, `precioAlMomento`.
**Relaciones:** `@ManyToOne → Orden`, `@ManyToOne → VarianteProducto`.
✅ Captura precio al momento de la compra (snapshot correcto).

### Foto
**Atributos:** `id`, `nombre`, `orden`.
**Relaciones:** `@ManyToOne → Producto`.

### Descuento
**Atributos:** `id`, `nombre`, `tipo` (`PORCENTAJE`/`FIJO`), `valor`, `porcentaje`,
`fechaInicio` (`fecha_ini` en BD), `fechaFin`, `estado` (`ACTIVO`/`EXPIRADO`).
**Relaciones:** `@OneToMany → Carrito`, `@OneToMany → Orden`.
🔍 **Diseño validado:** Dos campos de valor (`valor` + `porcentaje`) — entidad plana
con enum, aceptado por el equipo.

---

## 4. Estado por capa

### 4.1 Model / Entities

| Clase              | Estado | Observaciones                                                                        |
|--------------------|--------|--------------------------------------------------------------------------------------|
| `Usuario`          | ✅      | Bien anotado. Validaciones presentes. Relaciones correctas. `@JsonIgnore` en colecciones. |
| `Producto`         | ✅      | Relaciones correctas. `@JsonIgnore` en colecciones.                                  |
| `Categoria`        | ✅      | Simple y correcta. `@JsonIgnore` en colección de productos.                          |
| `Marca`            | ✅      | Simple y correcta. `@JsonIgnore` en colección de productos.                          |
| `Carrito`          | ✅      | Campo `fechaUltimaModificacion` agregado con `@PrePersist`/`@PreUpdate`.             |
| `ItemCarrito`      | ✅      | Correcto. Captura `precioUnitario`.                                                  |
| `Orden`            | ✅      | Correcto. `@JsonIgnore` en colección de items.                                       |
| `ItemOrden`        | ✅      | Snapshot de precio correcto con `precioAlMomento`.                                   |
| `VarianteProducto` | ✅      | Typo `OTONIO` corregido a `OTONO` en enum `Estacion`. `@JsonIgnore` en colecciones. |
| `Foto`             | ✅      | Correcta.                                                                            |
| `Descuento`        | ✅      | Campo `nombre` agregado. Diseño de dos campos (`valor` + `porcentaje`) validado. 🔍  |

### 4.2 Repository

| Interfaz                    | Estado | Observaciones                                                        |
|-----------------------------|--------|----------------------------------------------------------------------|
| `UsuarioRepository`         | ✅      | `findByUsername`, `findByEmail` presentes.                           |
| `ProductoRepository`        | ✅      | `findByCategoriaId`, `findByMarcaId`, `findByEstado`.                |
| `CategoriaRepository`       | ✅      | CRUD suficiente.                                                     |
| `MarcaRepository`           | ✅      | CRUD suficiente.                                                     |
| `CarritoRepository`         | ✅      | `findByUsuarioId`, `findByUsuarioIdAndEstado`, `findActivosNoModificadosDesde`, `existsByDescuentoIdAndEstado`. |
| `ItemCarritoRepository`     | ✅      | `findByCarritoId`, `findByCarritoIdAndVarianteId`.                   |
| `OrdenRepository`           | ✅      | `findByUsuarioId` implementado correctamente.                        |
| `ItemOrdenRepository`       | ✅      | `findByOrdenId` suficiente.                                          |
| `VarianteProductoRepository`| ✅      | `findByProductoId` presente.                                         |
| `FotoRepository`            | ✅      | `findByProductoId`.                                                  |
| `DescuentoRepository`       | ✅      | `findByEstado`, `findActivosExpirados`.                              |

### 4.3 Service

| Clase                        | Estado | Observaciones                                                                                                                        |
|------------------------------|--------|--------------------------------------------------------------------------------------------------------------------------------------|
| `UsuarioServiceImpl`         | ✅      | DTOs, BCrypt, `@Transactional`. `findEntityById()` interno para referencias JPA.                                                     |
| `ProductoServiceImpl`        | ✅      | Usa `MarcaService`/`CategoriaService`. Métodos `findByCategoria`, `findByMarca`, `findByEstado`. `@Transactional`.                   |
| `CategoriaServiceImpl`       | ✅      | `CategoriaRequest`, `@Transactional`, `ResourceNotFoundException`.                                                                   |
| `MarcaServiceImpl`           | ✅      | `MarcaRequest`, `@Transactional`, `ResourceNotFoundException`.                                                                       |
| `CarritoServiceImpl`         | ✅      | Bug fix stock en `realizarCompra()`. Validación producto ACTIVO en `agregarItem()`. `vaciarCarritosAbandonados()`. `findByUsuario()`. |
| `ItemCarritoServiceImpl`     | ✅      | Service interno. `ResourceNotFoundException` en todos los métodos. Validación de existencia en `delete()`.                           |
| `OrdenServiceImpl`           | ✅      | `confirmar()` solo desde PENDIENTE. `cancelar()` bloqueado si ENTREGADA/CANCELADA. `@Transactional`.                                |
| `ItemOrdenServiceImpl`       | ✅      | Service interno. `ResourceNotFoundException` en todos los métodos. Validación de existencia en `delete()`.                           |
| `VarianteProductoServiceImpl`| ✅      | `VarianteProductoRequest`. `descontarStock()` con `BusinessRuleException`. `@Transactional`.                                         |
| `FotoServiceImpl`            | ✅      | `FotoRequest`. Usa `ProductoService`. `findByProducto`. `@Transactional`.                                                            |
| `DescuentoServiceImpl`       | ✅      | Validaciones de negocio en `create`/`update` (fechas, tipo, valor). `delete` protegido contra uso activo. `findActivos()`. `expirarVencidos()`. |

### 4.4 Controller

| Clase                       | Endpoints definidos                                                                                                                                    | Estado | Observaciones                                                                     |
|-----------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------|--------|-----------------------------------------------------------------------------------|
| `AuthController`            | `POST /api/auth/login`, `POST /api/auth/register`                                                                                                     | ✅      | Nuevo. Devuelve JWT + username + rol.                                             |
| `UsuarioController`         | `GET /`, `GET /{id}`, `POST /`, `PUT /{id}`, `DELETE /{id}`                                                                                           | ✅      | Solo ADMIN (protegido por `SecurityFilterChain`).                                 |
| `ProductoController`        | `GET /`, `GET /{id}`, `GET /categoria/{id}`, `GET /marca/{id}`, `GET /estado/{estado}`, `POST /`, `PUT /{id}`, `DELETE /{id}`, `GET /{id}/disponible` | ✅      | GETs públicos. Escrituras solo ADMIN.                                             |
| `CategoriaController`       | `GET /`, `GET /{id}`, `POST /`, `PUT /{id}`, `DELETE /{id}`                                                                                           | ✅      | GETs públicos. Escrituras solo ADMIN.                                             |
| `MarcaController`           | `GET /`, `GET /{id}`, `POST /`, `PUT /{id}`, `DELETE /{id}`                                                                                           | ✅      | GETs públicos. Escrituras solo ADMIN.                                             |
| `CarritoController`         | CRUD + items + total + vaciar + checkout                                                                                                               | ✅      | Ownership enforcement: CLIENTE solo ve/modifica sus carritos. ADMIN accede a todos. `usuarioId` derivado del JWT. |
| `OrdenController`           | `GET /`, `GET /{id}`, `DELETE /{id}`, `POST /{id}/confirmar`, `POST /{id}/cancelar`, `GET /{id}/monto-final`, `GET /{id}/items`, `GET /usuario/{id}` | ✅      | Ownership enforcement: CLIENTE solo ve/opera sus órdenes. ADMIN accede a todas. |
| `VarianteProductoController`| `GET /`, `GET /{id}`, `POST /`, `PUT /{id}`, `DELETE /{id}`, `GET /{id}/precio`, `GET /{id}/stock/disponible`                                         | ✅      | GETs públicos. Escrituras solo ADMIN.                                             |
| `FotoController`            | `GET /`, `GET /{id}`, `GET /producto/{id}`, `POST /`, `PUT /{id}`, `DELETE /{id}`                                                                     | ✅      | GETs públicos. Escrituras solo ADMIN.                                             |
| `DescuentoController`       | `GET /`, `GET /activos`, `GET /{id}`, `POST /`, `PUT /{id}`, `DELETE /{id}`, `GET /{id}/vigente`, `GET /{id}/calcular`                                | ✅      | Solo ADMIN, excepto `GET /activos` (cualquier autenticado).                       |
| `ItemCarritoController`     | —                                                                                                                                                      | ✅      | **Eliminado.** Items incluidos en `CarritoResponse`.                              |
| `ItemOrdenController`       | —                                                                                                                                                      | ✅      | **Eliminado.** Items incluidos en `OrdenResponse`.                                |

### 4.5 DTOs

| Clase                     | Uso                                      | Estado |
|---------------------------|------------------------------------------|--------|
| `LoginRequest`            | Input login (`username`, `password`)     | ✅      |
| `AuthResponse`            | Output JWT + username + rol              | ✅      |
| `UsuarioRequest`          | Input crear/actualizar usuario           | ✅      |
| `UsuarioResponse`         | Output usuario sin contraseña            | ✅      |
| `CategoriaRequest`        | Input crear/actualizar categoría         | ✅      |
| `CategoriaResponse`       | Output categoría                         | ✅      |
| `MarcaRequest`            | Input crear/actualizar marca             | ✅      |
| `MarcaResponse`           | Output marca                             | ✅      |
| `FotoRequest`             | Input crear/actualizar foto              | ✅      |
| `FotoResponse`            | Output foto                              | ✅      |
| `VarianteProductoRequest` | Input crear/actualizar variante          | ✅      |
| `VarianteProductoResponse`| Output variante con nombre de producto   | ✅      |
| `ProductoRequest`         | Input crear/actualizar producto (IDs FK) | ✅      |
| `ProductoResponse`        | Output producto con marca y categoría    | ✅      |
| `DescuentoRequest`        | Input crear/actualizar descuento         | ✅      | Campo `nombre` agregado. `usuarioId` sin `@NotNull` (derivado del JWT en controller). |
| `DescuentoResponse`       | Output descuento                         | ✅      | Campo `nombre` agregado.                                                              |
| `CarritoRequest`          | Input crear/actualizar carrito           | ✅      | `usuarioId` sin `@NotNull`; el controller lo inyecta desde el JWT.                    |
| `CarritoResponse`         | Output carrito con items embebidos       | ✅      | Campo `fechaUltimaModificacion` agregado.                                             |
| `ItemCarritoResponse`     | Output ítem de carrito                   | ✅      |
| `OrdenResponse`           | Output orden con items embebidos         | ✅      |
| `ItemOrdenResponse`       | Output ítem de orden                     | ✅      |
| `ErrorResponse`           | Cuerpo estándar de error HTTP            | ✅      |

### 4.6 Seguridad y Configuración

**`SecurityBeansConfig.java`:**
Centraliza todos los beans de seguridad: `PasswordEncoder` (BCrypt),
`AuthenticationManager` y `SecurityFilterChain` con JWT.

**Spring Security — ✅ Implementado:**
- `SecurityFilterChain` configurado con rutas por rol.
- `JwtAuthenticationFilter` (`OncePerRequestFilter`) valida el token en cada request.
- `UserDetailsServiceImpl` carga el usuario desde la BD por username.
- `JwtUtil` genera y valida tokens HS256.
- Sesiones `STATELESS` — no hay estado de sesión en el servidor.

**Rutas públicas (sin token):**
- `POST /api/auth/login`, `POST /api/auth/register`
- `GET /api/productos/**`, `GET /api/categorias/**`, `GET /api/marcas/**`
- `GET /api/variantes/**`, `GET /api/fotos/**`
- `/swagger-ui.html`, `/swagger-ui/**`, `/api-docs/**`, `/v3/api-docs/**`

**Solo ADMIN:**
- Escrituras en catálogo (productos, categorías, marcas, variantes, fotos)
- Todo `/api/usuarios/**`
- Todo `/api/descuentos/**` (excepto `GET /activos`)

**Autenticado (cualquier rol):**
- `GET /api/descuentos/activos` — permite ver descuentos vigentes para aplicar al carrito
- Todo `/api/carritos/**` — con control de ownership por usuario
- Todo `/api/ordenes/**` — con control de ownership por usuario

**Control de ownership (CLIENTE):**
- `GET /api/carritos` devuelve solo los carritos propios.
- `GET /api/ordenes` devuelve solo las órdenes propias.
- Cualquier operación sobre un carrito u orden ajeno devuelve **HTTP 403**.
- Al crear un carrito, el `usuarioId` se toma del JWT (no del body).
- Los ADMIN pueden acceder a todos los recursos sin restricción.

**`GlobalExceptionHandler` — ✅ Completo:**
| Excepción | HTTP |
|-----------|------|
| `ResourceNotFoundException` | 404 Not Found |
| `BusinessRuleException` | 400 Bad Request |
| `MethodArgumentNotValidException` | 400 Validation Error |
| `DataIntegrityViolationException` | 409 Conflict |
| `BadCredentialsException` | 401 Unauthorized |
| `DisabledException` | 403 Forbidden |
| `AccessDeniedException` | 403 Forbidden |
| `Exception` (genérico) | 500 Internal Server Error |

**CORS — ✅ Configurado:**
`CorsConfigurationSource` bean en `SecurityBeansConfig`. Permite todos los orígenes
(`allowedOriginPatterns = *`), métodos GET/POST/PUT/DELETE/OPTIONS, todos los headers,
y credenciales. Para producción se debe restringir `allowedOriginPatterns` al dominio
del frontend.

**Swagger/OpenAPI — ✅ Funcionando:**
Dependencia `springdoc-openapi-starter-webmvc-ui 2.5.0` agregada al `pom.xml`.
Rutas `/swagger-ui.html`, `/swagger-ui/**`, `/api-docs/**`, `/v3/api-docs/**`
habilitadas como públicas en el `SecurityFilterChain`.
Acceso: `http://localhost:8080/swagger-ui.html`. Usar botón **Authorize** con
`Bearer <token>` para probar endpoints protegidos desde la UI.

---

## 5. Problemas detectados y estado

| # | Severidad | Estado      | Problema                                                                                                      |
|---|-----------|-------------|---------------------------------------------------------------------------------------------------------------|
| 1 | **Alta**  | ✅ Resuelto  | Bug de stock: `realizarCompra()` valida y descuenta stock dentro de `@Transactional`.                        |
| 2 | **Alta**  | ✅ Resuelto  | Seguridad: `SecurityFilterChain` + JWT filter + rutas por rol implementados.                                 |
| 3 | **Alta**  | ✅ Resuelto  | Integridad del modelo: `ItemOrdenController` e `ItemCarritoController` eliminados.                           |
| 4 | **Alta**  | ⚠️ Pendiente | Secretos hardcodeados: JWT secret y contraseña de BD en `application.properties`.                            |
| 5 | **Alta**  | ✅ Resuelto  | `@Transactional` agregado a todos los métodos de escritura en todos los services.                            |
| 6 | **Media** | ✅ Resuelto  | `GlobalExceptionHandler` cubre 404, 400, 409, 401, 403, 500.                                                |
| 7 | **Media** | ✅ Resuelto  | Controllers usan DTOs de Request/Response. Entidades con `@JsonIgnore`.                                      |
| 8 | **Media** | ✅ Resuelto  | `POST /stock/descontar` eliminado del controller. Solo uso interno.                                          |
| 9 | **Media** | ✅ Resuelto  | Validación de transición de estados en `OrdenService`.                                                       |
| 10| **Media** | ✅ Resuelto  | `ItemCarritoServiceImpl` e `ItemOrdenServiceImpl`: reemplazados `IllegalArgumentException` → `ResourceNotFoundException` y agregada validación en `delete()`. |
| 11| **Media** | ✅ Resuelto  | `CarritoServiceImpl.agregarItem()`: valida que el producto de la variante sea `ACTIVO` antes de agregar.     |
| 12| **Baja**  | ✅ Resuelto  | Typo `OTONIO` → `OTONO` en `Estacion.java`.                                                                  |
| 13| **Baja**  | ⚠️ Aceptado  | Ambigüedad en `Descuento`: diseño de `valor` + `porcentaje` validado por el equipo.                         |
| 14| **Baja**  | ✅ Resuelto  | Queries de filtrado en `ProductoRepository`, `CarritoRepository`, `FotoRepository`, `DescuentoRepository`.   |
| 15| **Baja**  | ✅ Resuelto  | Swagger: `springdoc-openapi 2.5.0` agregado. Rutas públicas en `SecurityFilterChain`. Disponible en `/swagger-ui.html`. |
| 16| **Baja**  | ✅ Resuelto  | CORS: `CorsConfigurationSource` configurado en `SecurityBeansConfig` (todos los orígenes en desarrollo).    |
| 17| **Alta**  | ✅ Resuelto  | Sin control de ownership: cualquier usuario autenticado podía acceder a carritos/órdenes ajenos. Corregido con `validarPropietario()` en `CarritoController` y `OrdenController`. |
| 18| **Alta**  | ✅ Resuelto  | `POST /api/carritos` aceptaba `usuarioId` del cliente — cualquiera podía crear carritos a nombre de otro. `usuarioId` ahora se extrae del JWT. |
| 19| **Media** | ✅ Resuelto  | `Descuento` sin validaciones de negocio: fechas inconsistentes, `porcentaje` nulo para PORCENTAJE, valor 0 para FIJO. Validaciones agregadas en `DescuentoServiceImpl`. |
| 20| **Media** | ✅ Resuelto  | `Descuento.delete()` permitía eliminar descuentos en uso por carritos activos. Protegido con `existsByDescuentoIdAndEstado()`. |
| 21| **Baja**  | ✅ Resuelto  | `DescuentoJob` accedía al repository directamente. Refactorizado para usar `DescuentoService.expirarVencidos()`. |
| 22| **Baja**  | ✅ Resuelto  | `GET /api/descuentos/activos` creado como ADMIN-only accidentalmente. Ahora accesible a cualquier autenticado. |

---

## 6. Lo que falta implementar

1. **Variables de entorno para secretos** — extraer `jwt.secret` y contraseña de BD
   de `application.properties` hacia variables de entorno o `.env` no comiteado.

2. **Anotaciones Swagger en controllers** — agregar `@Tag`, `@Operation`, `@ApiResponse`
   para documentar cada endpoint en la UI. Actualmente la UI muestra todos los endpoints
   pero sin descripciones.

3. **CORS en producción** — restringir `allowedOriginPatterns` al dominio real del
   frontend cuando se despliegue.

4. **Cobertura de tests** — actualmente hay 1 test de integración. Faltan tests para:
   checkout (stock, total, estado de carrito), autenticación (login/register/token),
   descuentos, transiciones de estado de orden, endpoints protegidos por rol, y
   validación de ownership (acceso cruzado entre usuarios).

5. **Features pendientes (no críticas):**
   - Paginación en endpoints de colecciones (`Page<T>`).
   - ~~Lógica de expiración automática de descuentos (scheduled task).~~ ✅ Implementado en `DescuentoJob`.
   - ~~JOB de limpieza de carritos abandonados.~~ ✅ Implementado en `CarritoJob`.
   - Gestión de imágenes real (S3 o almacenamiento en disco).

---

## 7. Cambios realizados en Fase 3

## 8. Cambios realizados en Fase 4 (sesión actual — 2026-04-14)

| Archivo modificado / creado                          | Tipo de cambio   | Motivo                                                                                     |
|------------------------------------------------------|------------------|--------------------------------------------------------------------------------------------|
| `src/main/resources/application.properties`         | Corrección       | URL MySQL en una línea, dialect removido, `open-in-view=false`, nombre de app deduplicado |
| `src/test/resources/application-test.properties`    | Corrección       | Dialect H2 removido (Hibernate 6 auto-detecta)                                             |
| `pom.xml`                                            | Ampliación       | Dependencias JJWT 0.12.3 (`jjwt-api`, `jjwt-impl`, `jjwt-jackson`)                       |
| `config/SecurityBeansConfig.java`                   | Reemplazo total  | `SecurityFilterChain` real con JWT, rutas por rol, `AuthenticationManager` bean           |
| `security/JwtUtil.java`                              | Creación         | Genera y valida tokens HS256 con JJWT 0.12.x                                              |
| `security/UserDetailsServiceImpl.java`               | Creación         | Carga `Usuario` desde BD, mapea a `UserDetails` con `ROLE_` prefix                        |
| `security/JwtAuthenticationFilter.java`              | Creación         | `OncePerRequestFilter` que valida Bearer token en cada request                             |
| `controller/AuthController.java`                    | Creación         | `POST /api/auth/login` y `POST /api/auth/register` — devuelven JWT                        |
| `dto/LoginRequest.java`                              | Creación         | Input para login (`username`, `password`)                                                  |
| `dto/AuthResponse.java`                              | Creación         | Output JWT (`token`, `username`, `rol`)                                                    |
| `exception/GlobalExceptionHandler.java`             | Ampliación       | Nuevos handlers: `DataIntegrityViolationException` (409), `BadCredentialsException` (401), `DisabledException` (403) |
| `service/impl/ItemCarritoServiceImpl.java`           | Fix              | `IllegalArgumentException` → `ResourceNotFoundException`. Validación en `delete()`.       |
| `service/impl/ItemOrdenServiceImpl.java`             | Fix              | `IllegalArgumentException` → `ResourceNotFoundException`. Validación en `delete()`.       |
| `service/impl/CarritoServiceImpl.java`               | Ampliación       | `agregarItem()`: valida que `producto.estado == ACTIVO` antes de agregar al carrito        |
| `Trekking-Ecommerce.postman_collection.json`        | Creación         | Colección Postman con todos los endpoints, variables y bodies de ejemplo                   |

---

## 8. Cambios realizados en Fase 4 (sesión actual — 2026-04-14)

| Archivo modificado / creado              | Tipo de cambio | Motivo                                                                                         |
|------------------------------------------|----------------|------------------------------------------------------------------------------------------------|
| `pom.xml`                                | Ampliación     | Dependencia `springdoc-openapi-starter-webmvc-ui 2.5.0` para Swagger UI                      |
| `config/SecurityBeansConfig.java`        | Ampliación     | `CorsConfigurationSource` bean (todos los orígenes en desarrollo). Rutas Swagger en `permitAll`. |
| `ESTADO_PROYECTO.md`                     | Actualización  | Refleja estado actual: Swagger operativo, CORS configurado, problemas 15 y 16 resueltos        |

---

## 9. Cambios realizados en Fase 5 (sesión actual — 2026-04-14)

### 9.1 Mejoras en Descuento

| Archivo modificado / creado                        | Tipo de cambio | Motivo                                                                                             |
|----------------------------------------------------|----------------|----------------------------------------------------------------------------------------------------|
| `model/Descuento.java`                             | Ampliación     | Campo `nombre` agregado (`@NotBlank`, `@Size(max=100)`, `NOT NULL`)                               |
| `dto/DescuentoRequest.java`                        | Ampliación     | Campo `nombre` con validaciones Bean Validation                                                    |
| `dto/DescuentoResponse.java`                       | Ampliación     | Campo `nombre` incluido en el output                                                               |
| `service/DescuentoService.java`                    | Ampliación     | Métodos `findActivos()` y `expirarVencidos()` agregados a la interfaz                              |
| `service/impl/DescuentoServiceImpl.java`           | Ampliación     | Implementa `findActivos()` y `expirarVencidos()`. Validaciones en `create`/`update`: fechas coherentes, `porcentaje` obligatorio para PORCENTAJE, `valor > 0` para FIJO. `delete()` protegido si hay carritos activos usando el descuento. |
| `repository/DescuentoRepository.java`              | Ampliación     | Query `findActivosExpirados(LocalDate hoy)` para el job de expiración                             |
| `controller/DescuentoController.java`              | Ampliación     | Endpoint `GET /api/descuentos/activos`. Mapeo de campo `nombre` en `toResponse()`.               |
| `config/SecurityBeansConfig.java`                  | Corrección     | `GET /api/descuentos/activos` habilitado para usuarios autenticados (no solo ADMIN)               |

### 9.2 Mejoras en Carrito

| Archivo modificado / creado                        | Tipo de cambio | Motivo                                                                                             |
|----------------------------------------------------|----------------|----------------------------------------------------------------------------------------------------|
| `model/Carrito.java`                               | Ampliación     | Campo `fechaUltimaModificacion` con `@PrePersist`/`@PreUpdate` — se actualiza automáticamente     |
| `dto/CarritoResponse.java`                         | Ampliación     | Campo `fechaUltimaModificacion` incluido en el output                                              |
| `dto/CarritoRequest.java`                          | Corrección     | `@NotNull` removido de `usuarioId`; el controller lo inyecta desde el JWT                         |
| `repository/CarritoRepository.java`                | Ampliación     | `findActivosNoModificadosDesde(LocalDateTime)` y `existsByDescuentoIdAndEstado()` agregados        |
| `service/CarritoService.java`                      | Ampliación     | Métodos `findByUsuario(Long)` y `vaciarCarritosAbandonados(int)` en la interfaz                   |
| `service/impl/CarritoServiceImpl.java`             | Ampliación     | Implementa `findByUsuario()` y `vaciarCarritosAbandonados()`: encuentra carritos ACTIVOS inactivos 7+ días, borra sus ítems, los marca como `ABANDONADO` |

### 9.3 Jobs programados

| Archivo modificado / creado                        | Tipo de cambio | Motivo                                                                                             |
|----------------------------------------------------|----------------|----------------------------------------------------------------------------------------------------|
| `EcommerceApplication.java`                        | Ampliación     | `@EnableScheduling` habilitado                                                                     |
| `job/CarritoJob.java`                              | Creación       | Cron `0 0 2 * * MON` (lunes 02:00 AM): llama a `carritoService.vaciarCarritosAbandonados(7)`      |
| `job/DescuentoJob.java`                            | Creación       | Cron `0 5 0 * * *` (diario 00:05 AM): llama a `descuentoService.expirarVencidos()`               |

### 9.4 Seguridad — control de ownership

| Archivo modificado / creado                        | Tipo de cambio | Motivo                                                                                             |
|----------------------------------------------------|----------------|----------------------------------------------------------------------------------------------------|
| `exception/GlobalExceptionHandler.java`            | Ampliación     | Handler para `AccessDeniedException` → HTTP 403 con formato estándar de error                     |
| `controller/CarritoController.java`                | Seguridad      | CLIENTE solo ve/modifica sus carritos. ADMIN accede a todos. `usuarioId` derivado del JWT en `create()`. Helpers privados `validarPropietario()` y `esAdmin()`. |
| `controller/OrdenController.java`                  | Seguridad      | CLIENTE solo ve/opera sus órdenes. ADMIN accede a todas. Helpers privados `validarPropietario()` y `esAdmin()`. `GET /usuario/{id}` valida que el ID solicitado coincida con el usuario autenticado. |
| `service/CarritoService.java`                      | Ampliación     | Método `findByUsuario(Long)` para filtrado por usuario en `GET /api/carritos`                     |
