# Trekking E-Commerce — API REST  
**Trabajo Práctico Integrador · APIs 2026**

---

| Campo | Valor |
|---|---|
| **Nro. de Grupo** | [COMPLETAR] |
| **Integrante 1** | [Nombre Apellido — Legajo] |
| **Integrante 2** | [Nombre Apellido — Legajo] |
| **Integrante 3** | [Nombre Apellido — Legajo] |
| **Integrante 4** | [Nombre Apellido — Legajo] |
| **Fecha de entrega** | Abril 2026 |
| **Repositorio** | https://github.com/[ORG]/[REPO] |

---

## 1. Arquitectura del sistema

### 1.1 Capas de la aplicación

```
┌─────────────────────────────────────────────────────┐
│                    Cliente / Postman                │
└──────────────────────┬──────────────────────────────┘
                       │ HTTP Request
┌──────────────────────▼──────────────────────────────┐
│          Security Filter Chain (JWT)                │
│  JwtAuthenticationFilter → UserDetailsService       │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│            Capa Controller  (@RestController)        │
│  AuthController · UsuarioController · Producto...   │
│  CarritoController · OrdenController · ...          │
└──────────────────────┬──────────────────────────────┘
                       │ llama a interface
┌──────────────────────▼──────────────────────────────┐
│          Capa Service  (interface + impl)            │
│  *Service (interfaz) ← *ServiceImpl (@Service)      │
│  Lógica de negocio: stock, totales, descuentos...   │
└──────────────────────┬──────────────────────────────┘
                       │ llama a
┌──────────────────────▼──────────────────────────────┐
│         Capa Repository  (Spring Data JPA)          │
│  *Repository extends JpaRepository<Entidad, Long>   │
└──────────────────────┬──────────────────────────────┘
                       │ SQL / HQL
┌──────────────────────▼──────────────────────────────┐
│              Base de datos MySQL                    │
│         Schema: ecommerce_trekking                  │
└─────────────────────────────────────────────────────┘
```

### 1.2 Security Filter Chain

```
Petición HTTP
   │
   ├─► Rutas públicas (swagger, /api/auth/**, GET productos/categorias/marcas/variantes/fotos)
   │       └─► Pasan directo sin validación de token
   │
   └─► Rutas protegidas
           │
           ▼
     JwtAuthenticationFilter
     ├── Lee el header: Authorization: Bearer <token>
     ├── Valida firma y expiración (JwtUtil)
     ├── Carga UserDetails (UserDetailsServiceImpl)
     └── Registra autenticación en SecurityContext
           │
           ▼
     Verificación de rol (@PreAuthorize)
     ├── hasRole("ADMIN")  → acceso total a CRUDs
     ├── isAuthenticated() → carrito, orden, descuentos/activos
     └── Propietario       → usuario solo ve/edita sus propios carritos u órdenes
```

**Configuración:** sesión STATELESS, CORS habilitado, contraseñas con BCrypt.  
**Credenciales:** externalizadas via variables de entorno (`DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`).

---

## 2. Entidades y mapa de correspondencia

| Entidad | Clase Java | Tabla BD | PK |
|---|---|---|---|
| Usuario | `Usuario.java` | `usuario` | `id_usuario` |
| Producto | `Producto.java` | `producto` | `id_producto` |
| Categoría | `Categoria.java` | `categoria` | `id_categoria` |
| Marca | `Marca.java` | `marca` | `id_marca` |
| Variante de Producto | `VarianteProducto.java` | `variante_producto` | `id_variante` |
| Foto | `Foto.java` | `foto` | `id_foto` |
| Descuento | `Descuento.java` | `descuento` | `id_descuento` |
| Carrito | `Carrito.java` | `carrito` | `id_carrito` |
| Ítem de Carrito | `ItemCarrito.java` | `item_carrito` | `id_item_carrito` |
| Orden | `Orden.java` | `orden` | `id_orden` |
| Ítem de Orden | `ItemOrden.java` | `item_orden` | `id_item_orden` |

### Relaciones entre entidades

```
Categoria ──< Producto >── Marca
                │
                ├──< VarianteProducto >──┐
                │         │              │
                └──< Foto │              │
                          │              │
Descuento ──< Carrito >── Usuario        │
                │                        │
                └──< ItemCarrito >───────┘
                                         │
Descuento ──< Orden >────── Usuario      │
                │                        │
                └──< ItemOrden >─────────┘
```

**Enumeraciones:**

| Enum | Valores |
|---|---|
| `RolUsuario` | ADMIN, CLIENTE |
| `EstadoUsuario` | ACTIVO, INACTIVO |
| `EstadoProducto` | ACTIVO, PAUSADO, ELIMINADO |
| `EstadoCarrito` | ACTIVO, VACIO, ABANDONADO, CONVERTIDO |
| `EstadoOrden` | PENDIENTE, CONFIRMADA, ENVIADA, ENTREGADA, CANCELADA |
| `EstadoDescuento` | ACTIVO, EXPIRADO |
| `TipoDescuento` | PORCENTAJE, FIJO |
| `Estacion` | PRIMAVERA, VERANO, OTONO, INVIERNO |

---

## 3. Endpoints

> **AUTH** = requiere token JWT · **ADMIN** = requiere rol ADMIN · **PROP** = solo el propietario o ADMIN

### 3.1 Autenticación — `/api/auth`

| Método | URL | DTO entrada | DTO salida | Auth |
|---|---|---|---|---|
| POST | `/api/auth/login` | `LoginRequest` | `AuthResponse` | Pública |
| POST | `/api/auth/register` | `UsuarioRequest` | `AuthResponse` | Pública |

### 3.2 Usuarios — `/api/usuarios`

| Método | URL | DTO entrada | DTO salida | Auth |
|---|---|---|---|---|
| GET | `/api/usuarios` | — | `List<UsuarioResponse>` | ADMIN |
| GET | `/api/usuarios/{id}` | — | `UsuarioResponse` | ADMIN |
| POST | `/api/usuarios` | `UsuarioRequest` | `UsuarioResponse` | ADMIN |
| PUT | `/api/usuarios/{id}` | `UsuarioRequest` | `UsuarioResponse` | ADMIN |
| DELETE | `/api/usuarios/{id}` | — | — | ADMIN |

### 3.3 Categorías — `/api/categorias`

| Método | URL | DTO entrada | DTO salida | Auth |
|---|---|---|---|---|
| GET | `/api/categorias` | — | `List<CategoriaResponse>` | Pública |
| GET | `/api/categorias/{id}` | — | `CategoriaResponse` | Pública |
| POST | `/api/categorias` | `CategoriaRequest` | `CategoriaResponse` | ADMIN |
| PUT | `/api/categorias/{id}` | `CategoriaRequest` | `CategoriaResponse` | ADMIN |
| DELETE | `/api/categorias/{id}` | — | — | ADMIN |

### 3.4 Marcas — `/api/marcas`

| Método | URL | DTO entrada | DTO salida | Auth |
|---|---|---|---|---|
| GET | `/api/marcas` | — | `List<MarcaResponse>` | Pública |
| GET | `/api/marcas/{id}` | — | `MarcaResponse` | Pública |
| POST | `/api/marcas` | `MarcaRequest` | `MarcaResponse` | ADMIN |
| PUT | `/api/marcas/{id}` | `MarcaRequest` | `MarcaResponse` | ADMIN |
| DELETE | `/api/marcas/{id}` | — | — | ADMIN |

### 3.5 Productos — `/api/productos`

| Método | URL | DTO entrada | DTO salida | Auth |
|---|---|---|---|---|
| GET | `/api/productos` | — | `List<ProductoResponse>` | Pública |
| GET | `/api/productos/{id}` | — | `ProductoResponse` | Pública |
| GET | `/api/productos/categoria/{id}` | — | `List<ProductoResponse>` | Pública |
| GET | `/api/productos/marca/{id}` | — | `List<ProductoResponse>` | Pública |
| GET | `/api/productos/estado/{estado}` | — | `List<ProductoResponse>` | Pública |
| GET | `/api/productos/{id}/disponible` | — | `Boolean` | Pública |
| POST | `/api/productos` | `ProductoRequest` | `ProductoResponse` | ADMIN |
| PUT | `/api/productos/{id}` | `ProductoRequest` | `ProductoResponse` | ADMIN |
| DELETE | `/api/productos/{id}` | — | — | ADMIN |

### 3.6 Variantes de Producto — `/api/variantes`

| Método | URL | DTO entrada | DTO salida | Auth |
|---|---|---|---|---|
| GET | `/api/variantes` | — | `List<VarianteProductoResponse>` | Pública |
| GET | `/api/variantes/{id}` | — | `VarianteProductoResponse` | Pública |
| GET | `/api/variantes/{id}/precio` | — | `BigDecimal` | Pública |
| GET | `/api/variantes/{id}/stock/disponible` | — | `Boolean` | Pública |
| POST | `/api/variantes` | `VarianteProductoRequest` | `VarianteProductoResponse` | ADMIN |
| PUT | `/api/variantes/{id}` | `VarianteProductoRequest` | `VarianteProductoResponse` | ADMIN |
| DELETE | `/api/variantes/{id}` | — | — | ADMIN |

### 3.7 Fotos — `/api/fotos`

| Método | URL | DTO entrada | DTO salida | Auth |
|---|---|---|---|---|
| GET | `/api/fotos` | — | `List<FotoResponse>` | Pública |
| GET | `/api/fotos/{id}` | — | `FotoResponse` | Pública |
| GET | `/api/fotos/producto/{id}` | — | `List<FotoResponse>` | Pública |
| POST | `/api/fotos` | `FotoRequest` | `FotoResponse` | ADMIN |
| PUT | `/api/fotos/{id}` | `FotoRequest` | `FotoResponse` | ADMIN |
| DELETE | `/api/fotos/{id}` | — | — | ADMIN |

### 3.8 Descuentos — `/api/descuentos`

| Método | URL | DTO entrada | DTO salida | Auth |
|---|---|---|---|---|
| GET | `/api/descuentos` | — | `List<DescuentoResponse>` | ADMIN |
| GET | `/api/descuentos/activos` | — | `List<DescuentoResponse>` | AUTH |
| GET | `/api/descuentos/{id}` | — | `DescuentoResponse` | ADMIN |
| GET | `/api/descuentos/{id}/vigente` | — | `Boolean` | ADMIN |
| GET | `/api/descuentos/{id}/calcular?monto=X` | — | `BigDecimal` | ADMIN |
| POST | `/api/descuentos` | `DescuentoRequest` | `DescuentoResponse` | ADMIN |
| PUT | `/api/descuentos/{id}` | `DescuentoRequest` | `DescuentoResponse` | ADMIN |
| DELETE | `/api/descuentos/{id}` | — | — | ADMIN |

### 3.9 Carritos — `/api/carritos`

| Método | URL | DTO entrada | DTO salida | Auth |
|---|---|---|---|---|
| GET | `/api/carritos` | — | `List<CarritoResponse>` | AUTH |
| GET | `/api/carritos/{id}` | — | `CarritoResponse` | PROP |
| GET | `/api/carritos/{id}/items` | — | `List<ItemCarritoResponse>` | PROP |
| GET | `/api/carritos/{id}/total` | — | `BigDecimal` | PROP |
| POST | `/api/carritos` | `CarritoRequest` | `CarritoResponse` | AUTH |
| POST | `/api/carritos/{id}/items` | `ItemCarritoRequest` | `ItemCarritoResponse` | PROP |
| POST | `/api/carritos/{id}/vaciar` | — | — | PROP |
| POST | `/api/carritos/{id}/checkout` | — | `OrdenResponse` | PROP |
| PUT | `/api/carritos/{id}` | `CarritoRequest` | `CarritoResponse` | PROP |
| PUT | `/api/carritos/{id}/items/{itemId}` | `cantidad` (param) | `ItemCarritoResponse` | PROP |
| DELETE | `/api/carritos/{id}` | — | — | PROP |
| DELETE | `/api/carritos/{id}/items/{itemId}` | — | — | PROP |

### 3.10 Órdenes — `/api/ordenes`

| Método | URL | DTO entrada | DTO salida | Auth |
|---|---|---|---|---|
| GET | `/api/ordenes` | — | `List<OrdenResponse>` | AUTH |
| GET | `/api/ordenes/{id}` | — | `OrdenResponse` | PROP |
| GET | `/api/ordenes/{id}/items` | — | `List<ItemOrdenResponse>` | PROP |
| GET | `/api/ordenes/{id}/monto-final` | — | `BigDecimal` | PROP |
| GET | `/api/ordenes/usuario/{id}` | — | `List<OrdenResponse>` | PROP |
| POST | `/api/ordenes/{id}/confirmar` | — | `OrdenResponse` | PROP |
| POST | `/api/ordenes/{id}/cancelar` | — | `OrdenResponse` | PROP |
| DELETE | `/api/ordenes/{id}` | — | — | PROP |

---

## 4. Evidencias

> **Instrucción:** reemplazar cada cuadro con la captura de pantalla correspondiente.

### 4.1 Tablas en MySQL Workbench
*(Captura mostrando las 11 tablas del schema con datos de ejemplo visibles)*

`[INSERTAR CAPTURA — MySQL Workbench → schema ecommerce_trekking → tablas con filas de datos]`

### 4.2 Login exitoso — obtención de JWT
*(Captura de Postman: POST /api/auth/login con body `{"username":"...","password":"..."}` → respuesta 200 con `token`, `username`, `rol`)*

`[INSERTAR CAPTURA — Postman POST /api/auth/login → 200 OK con token JWT]`

### 4.3 Acceso a endpoint protegido con token válido
*(Captura de Postman: GET /api/carritos con header `Authorization: Bearer <token>` → 200 OK)*

`[INSERTAR CAPTURA — Postman GET /api/carritos con token → 200 OK]`

### 4.4 Acceso sin token — 401 Unauthorized
*(Captura de Postman: GET /api/carritos sin header Authorization → 401)*

`[INSERTAR CAPTURA — Postman GET /api/carritos sin token → 401 Unauthorized]`

### 4.5 Acceso con token de CLIENTE a endpoint de ADMIN — 403 Forbidden
*(Captura de Postman: DELETE /api/usuarios/1 con token de rol CLIENTE → 403)*

`[INSERTAR CAPTURA — Postman DELETE /api/usuarios/1 con token CLIENTE → 403 Forbidden]`

---

## 5. Repositorio y ejecución local

**Repositorio público:** `https://github.com/[ORG]/[REPO]`

### Pasos para ejecutar

```bash
# 1. Clonar el repositorio
git clone https://github.com/[ORG]/[REPO].git
cd [REPO]/ecommerce

# 2. Crear base de datos en MySQL
CREATE DATABASE ecommerce_trekking;

# 3. Configurar variables de entorno (opcional — tiene defaults)
# DB_USERNAME=root  DB_PASSWORD=12345  JWT_SECRET=<clave>
# O usar los defaults: usuario root, password 12345

# 4. Ejecutar el proyecto
./mvnw spring-boot:run

# La API queda disponible en: http://localhost:8080
# Swagger UI:               http://localhost:8080/swagger-ui.html
```

> Spring Boot crea las tablas automáticamente con `ddl-auto=validate`.  
> Para desarrollo local activar el perfil dev: `--spring.profiles.active=dev`
