# División del TP por integrante

---

## Integrante 1 — Arquitectura y Seguridad

**Sección del PDF:** Portada + Sección 1 (Arquitectura)

### Qué explicar

**Portada**
- Presentar el proyecto: e-commerce de artículos de trekking.
- Mencionar el stack: Spring Boot 3, Java 17, MySQL, JWT, Lombok, Swagger.

**Capas de la aplicación**
- Explicar el diagrama de capas: Cliente → Security Filter → Controller → Service → Repository → DB.
- Por qué se separa en capas: cada capa tiene una responsabilidad única (SRP).
- El controller NO habla directo con la base de datos — usa interfaces de servicio.
- Los repository son Spring Data JPA: heredan de `JpaRepository<Entidad, Long>`, Spring genera el SQL automáticamente.

**Security Filter Chain**
- Qué es un filtro en Spring: intercepta cada request antes de llegar al controller.
- `JwtAuthenticationFilter`: extrae el token del header `Authorization: Bearer <token>`, valida la firma y expiración, carga el usuario autenticado en el `SecurityContext`.
- Rutas públicas vs protegidas: los GETs de productos/categorías/marcas/variantes/fotos son públicos (catálogo). Todo lo que involucra usuarios, carritos u órdenes requiere token.
- Roles: `ADMIN` puede hacer CRUDs, `CLIENTE` solo puede ver su propio carrito y órdenes.
- Seguridad de credenciales: contraseñas guardadas con BCrypt (hash, nunca texto plano). Credenciales de DB y JWT externalizadas en variables de entorno.

### Evidencia a mostrar
- Diagrama de arquitectura (sección 1 del PDF).
- Captura 4.4: request sin token → 401.
- Captura 4.5: token de CLIENTE intentando borrar usuario → 403.

---

## Integrante 2 — Modelo de Datos

**Sección del PDF:** Sección 2 (Entidades y mapa de correspondencia)

### Qué explicar

**Las 11 entidades**
- Recorrer la tabla del PDF: `Usuario`, `Producto`, `Categoria`, `Marca`, `VarianteProducto`, `Foto`, `Descuento`, `Carrito`, `ItemCarrito`, `Orden`, `ItemOrden`.
- Cada entidad es una clase Java con `@Entity` y se mapea a una tabla de la BD con el mismo nombre (en snake_case).
- La PK de cada tabla se llama `id_<nombre>` (ej: `id_producto`).

**Relaciones**
- `Producto` → muchos `VarianteProducto` (talle, color, stock, precio propios por variante).
- `Producto` → muchas `Foto` (imágenes separadas).
- `Carrito` → muchos `ItemCarrito`, cada ítem apunta a una `VarianteProducto`.
- `Orden` → muchos `ItemOrden`, guarda el precio al momento de la compra (no cambia si el producto cambia de precio después).
- `Carrito` y `Orden` pueden tener un `Descuento` opcional.
- `Usuario` → muchos `Carrito` y muchas `Orden`.

**Enumeraciones**
- Explicar brevemente los estados: `EstadoCarrito` (ACTIVO → CONVERTIDO al hacer checkout), `EstadoOrden` (PENDIENTE → CONFIRMADA → ENVIADA → ENTREGADA), `TipoDescuento` (FIJO = monto fijo, PORCENTAJE = %).

**DTOs**
- Mencionar que existe un DTO de request (lo que llega) y uno de response (lo que se devuelve) por cada entidad con endpoints de escritura.
- Los DTOs evitan exponer la entidad de BD directamente: separan la API del modelo interno.

### Evidencia a mostrar
- Captura 4.1: tablas en MySQL Workbench con filas de datos.

---

## Integrante 3 — Endpoints públicos y de administración

**Sección del PDF:** Sección 3.1 a 3.8 (Auth, Usuarios, Categorías, Marcas, Productos, Variantes, Fotos, Descuentos)

### Qué explicar

**Autenticación (`/api/auth`)**
- `POST /login`: recibe `username` + `password`, devuelve `AuthResponse` con el `token` JWT, `username` y `rol`.
- `POST /register`: crea un nuevo usuario (rol CLIENTE por defecto o el que se indique).
- Sin token, no se puede acceder a nada protegido.

**Catálogo público (Categorías, Marcas, Productos, Variantes, Fotos)**
- Los GETs son públicos: cualquier persona puede ver el catálogo sin loguearse.
- Los POST/PUT/DELETE requieren rol ADMIN: solo el admin gestiona el catálogo.
- `GET /api/productos/categoria/{id}`: filtra productos por categoría.
- `GET /api/productos/{id}/disponible`: true si el producto tiene al menos una variante con stock > 0.
- `GET /api/variantes/{id}/stock/disponible?cantidad=X`: verifica si hay stock suficiente.

**Descuentos (`/api/descuentos`)**
- Dos tipos: `FIJO` (resta un monto fijo al total) y `PORCENTAJE` (resta un porcentaje).
- `GET /api/descuentos/activos`: usuarios autenticados pueden ver los descuentos activos (para aplicarlos).
- `GET /api/descuentos/{id}/calcular?monto=X`: calcula cuánto descuento aplica sobre un monto dado.
- Un job automático (`DescuentoJob`) corre todos los días a medianoche y expira los descuentos vencidos.

**Usuarios (`/api/usuarios`)**
- CRUD completo solo para ADMIN.
- Los usuarios se registran via `/api/auth/register`, no via este endpoint directamente.

### Evidencia a mostrar
- Captura 4.2: login y obtención del JWT en Postman.

---

## Integrante 4 — Flujo de compra y evidencias

**Sección del PDF:** Sección 3.9, 3.10 y Secciones 4 y 5 (Carritos, Órdenes, Evidencias, Repositorio)

### Qué explicar

**Carritos (`/api/carritos`)**
- Flujo completo de compra:
  1. `POST /api/carritos` — crear un carrito vacío (estado VACIO).
  2. `POST /api/carritos/{id}/items` — agregar ítems con `{"idVariante": 1, "cantidad": 2}`.
     - Valida que la variante tenga stock suficiente.
     - Guarda el `precioUnitario` en el ítem (snapshot del precio actual).
  3. `GET /api/carritos/{id}/total` — calcula el total con descuento aplicado si corresponde.
  4. `POST /api/carritos/{id}/checkout` — convierte el carrito en una Orden.
     - Verifica stock de cada ítem.
     - Crea la `Orden` con sus `ItemOrden`.
     - Descuenta el stock de cada `VarianteProducto`.
     - Cambia el estado del carrito a CONVERTIDO.
  5. `POST /api/carritos/{id}/vaciar` — vacía el carrito (elimina todos los ítems).
- Regla de propietario: un usuario CLIENTE solo puede ver y modificar sus propios carritos.
- Un job automático (`CarritoJob`) marca como ABANDONADOS los carritos inactivos por más de 7 días.

**Órdenes (`/api/ordenes`)**
- Se crean automáticamente desde el checkout del carrito (no se crean manualmente).
- `POST /api/ordenes/{id}/confirmar` / `cancelar` — transiciones de estado.
- `GET /api/ordenes/usuario/{id}` — historial de compras de un usuario.
- El `precioAlMomento` en cada `ItemOrden` garantiza que el monto final no cambie aunque el precio del producto cambie después.

**Evidencias y repositorio**
- Mostrar capturas 4.3 (acceso con token válido → 200).
- Explicar el README: cómo clonar, crear la BD, y levantar el proyecto con `./mvnw spring-boot:run`.
- La API queda disponible en `localhost:8080`, con Swagger UI en `localhost:8080/swagger-ui.html`.

### Evidencia a mostrar
- Captura 4.3: GET /api/carritos con token → 200 OK.

---

## Resumen de capturas a tomar (todos juntos)

| # | Qué capturar | Herramienta | Quién presenta |
|---|---|---|---|
| 4.1 | 11 tablas + datos en MySQL Workbench | Workbench | Integrante 2 |
| 4.2 | POST /api/auth/login → 200 con token | Postman | Integrante 3 |
| 4.3 | GET /api/carritos con token → 200 | Postman | Integrante 4 |
| 4.4 | GET /api/carritos sin token → 401 | Postman | Integrante 1 |
| 4.5 | DELETE /api/usuarios/1 con token CLIENTE → 403 | Postman | Integrante 1 |

**Pasos para tomar las capturas:**
1. Levantar la app con `./mvnw spring-boot:run`.
2. Levantar MySQL Workbench, conectar al schema y hacer screenshot de las tablas con datos.
3. En Postman: crear colección con 3 requests y tomar capturas.
   - Request A: POST login (sin auth) → copiar token de la respuesta.
   - Request B: GET /api/carritos sin header → 401.
   - Request C: GET /api/carritos con header `Authorization: Bearer <token>` → 200.
   - Request D: DELETE /api/usuarios/1 con token de usuario CLIENTE → 403.
