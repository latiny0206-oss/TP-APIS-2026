# GUÍA DEFINITIVA — Trekking Ecommerce API
## Manual de Inducción y Referencia Técnica del Equipo

> **Proyecto:** `com.trekking:ecommerce`
> **Stack:** Spring Boot 3.5.12 · Java 17 · Spring Security 6 + JWT · Spring Data JPA / Hibernate · MySQL 8.4
> **Cobertura de tests:** JaCoCo · Suite: unitarios + integración + seguridad
> **Última revisión:** Análisis estático exhaustivo del código fuente, build y estructura de directorios.

---

> **¿Para quién es este documento?**
> Este manual está diseñado para que **Nacho, Eli, Franco y los Referentes** — tanto del equipo de Desarrollo como de QA — puedan entender el proyecto desde cero, levantar el entorno local sin fricción, comprender las decisiones de arquitectura y aplicar los estándares de calidad exigidos por el equipo. Si es tu primer día en el proyecto, empieza por la [Guía de Ejecución Local](#sección-5--guía-de-ejecución-local). Si eres QA, la [Sección 6](#sección-6--qa-automatización-y-workflow) es tu punto de partida.

---

## Índice

- [Introducción al Proyecto](#introducción-al-proyecto)
- [Sección 1 — Estructura del Proyecto y Estándares](#sección-1--estructura-del-proyecto-y-estándares)
- [Sección 2 — Diagnóstico del JDK y Entorno](#sección-2--diagnóstico-del-jdk-y-entorno)
- [Sección 3 — Mapeo Técnico y Reglas de Negocio](#sección-3--mapeo-técnico-y-reglas-de-negocio)
  - [Diccionario de Entidades](#diccionario-de-entidades-con-contexto-de-negocio)
  - [Reglas de Negocio Críticas](#reglas-de-negocio-críticas)
  - [Tabla de Endpoints](#tabla-de-endpoints-api-rest)
  - [Flujo entre Capas](#flujo-de-funcionamiento-entre-capas)
- [Sección 4 — Arquitectura y Seguridad](#sección-4--arquitectura-y-seguridad)
  - [El Ciclo de Vida de un Request](#el-ciclo-de-vida-de-un-request-explicación-para-nuevos-integrantes)
  - [Diagrama de Capas](#diagrama-de-capas)
  - [Security Filter Chain](#security-filter-chain-en-detalle)
  - [Diagrama Mermaid.js](#diagrama-mermaidjs--arquitectura-completa)
- [Sección 5 — Guía de Ejecución Local](#sección-5--guía-de-ejecución-local)
- [Sección 6 — QA, Automatización y Workflow](#sección-6--qa-automatización-y-workflow)
  - [Estándar Allure](#estándar-de-reportes-allure)
  - [Conventional Commits](#conventional-commits)
  - [Flujo Gitflow / Jira](#flujo-gitflow--jira)
  - [SWEBOK](#alineación-con-swebok)

---

## Introducción al Proyecto

Este proyecto es una **API REST de e-commerce** especializada en equipamiento de trekking. Permite gestionar el catálogo completo de productos (con variantes por color, talla y temporada), administrar el carrito de compras de cada cliente, procesar órdenes de compra y aplicar descuentos. La API es el backend de una tienda online; el frontend (web o mobile) la consume vía HTTP.

**Flujo de negocio resumido:**

```
[Cliente] registra cuenta → inicia sesión (JWT) → navega catálogo (público)
         → agrega al carrito → hace checkout → se genera una Orden (PENDIENTE)
         → confirma → Orden CONFIRMADA · stock descontado
         → cancela  → Orden CANCELADA · stock restaurado
```

**Dos roles en el sistema:**
- **`CLIENTE`** — puede ver el catálogo, administrar su propio carrito y sus propias órdenes.
- **`ADMIN`** — puede hacer todo lo anterior más gestionar el catálogo, usuarios, descuentos y ver todos los carritos/órdenes.

---

## Sección 1 — Estructura del Proyecto y Estándares

### Árbol de Directorios del Proyecto

```
ecommerce/                                              ← Raíz del proyecto Maven
├── .env.example                                        ← Plantilla de variables de entorno (NO commitear .env)
├── .gitattributes
├── .gitignore
├── .mvn/
│   └── wrapper/
│       └── maven-wrapper.properties                    ← Fija Maven 3.9.14 vía wrapper
├── docker-compose.yml                                  ← MySQL 8.4 en contenedor Docker
├── mvnw / mvnw.cmd                                     ← Scripts del Maven Wrapper (usar SIEMPRE estos)
├── pom.xml                                             ← Build descriptor — groupId: com.trekking
├── README.md
├── GUIA_DEFINITIVA.MD                                  ← Este archivo
├── GRUPO13-...-DER-FINAL.drawio.png                   ← Diagrama ER (artefacto académico)
├── GRUPO13-...-UML-FINAL.drawio.png                   ← Diagrama UML (artefacto académico)
├── NRO13_07042026_DER_TPO.pdf                         ← Entrega académica
└── src/
    ├── main/
    │   ├── java/com/trekking/ecommerce/
    │   │   ├── EcommerceApplication.java               ← Punto de entrada @SpringBootApplication
    │   │   ├── config/
    │   │   │   ├── OpenApiConfig.java                  ← Configuración Swagger/OpenAPI 3
    │   │   │   └── SecurityBeansConfig.java            ← Security Filter Chain + CORS + BCrypt
    │   │   ├── controller/                             ← 11 @RestController
    │   │   │   ├── AuthController.java
    │   │   │   ├── AuthenticatedController.java        ← Clase base abstracta (ownership)
    │   │   │   ├── CarritoController.java
    │   │   │   ├── CategoriaController.java
    │   │   │   ├── DescuentoController.java
    │   │   │   ├── FotoController.java
    │   │   │   ├── MarcaController.java
    │   │   │   ├── OrdenController.java
    │   │   │   ├── ProductoController.java
    │   │   │   ├── UsuarioController.java
    │   │   │   └── VarianteProductoController.java
    │   │   ├── dto/                                    ← 25 DTOs Request / Response
    │   │   │   ├── AuthResponse.java
    │   │   │   ├── CarritoRequest.java / CarritoResponse.java
    │   │   │   ├── CategoriaRequest.java / CategoriaResponse.java
    │   │   │   ├── DescuentoRequest.java / DescuentoResponse.java
    │   │   │   ├── ErrorResponse.java
    │   │   │   ├── FotoRequest.java / FotoResponse.java
    │   │   │   ├── ItemCarritoRequest.java / ItemCarritoResponse.java
    │   │   │   ├── ItemOrdenResponse.java
    │   │   │   ├── LoginRequest.java / RegisterRequest.java
    │   │   │   ├── MarcaRequest.java / MarcaResponse.java
    │   │   │   ├── OrdenRequest.java / OrdenResponse.java
    │   │   │   ├── ProductoRequest.java / ProductoResponse.java
    │   │   │   ├── UsuarioRequest.java / UsuarioResponse.java
    │   │   │   └── VarianteProductoRequest.java / VarianteProductoResponse.java
    │   │   ├── exception/                              ← Manejo centralizado de errores
    │   │   │   ├── BusinessRuleException.java
    │   │   │   ├── GlobalExceptionHandler.java         ← @RestControllerAdvice
    │   │   │   └── ResourceNotFoundException.java
    │   │   ├── job/                                    ← Tareas programadas @Scheduled
    │   │   │   ├── CarritoJob.java                     ← Marca ABANDONADO carritos inactivos
    │   │   │   └── DescuentoJob.java                   ← Marca EXPIRADO descuentos vencidos
    │   │   ├── model/                                  ← 11 entidades JPA @Entity
    │   │   │   ├── enums/                              ← 8 enumeraciones de dominio
    │   │   │   │   ├── Estacion.java
    │   │   │   │   ├── EstadoCarrito.java
    │   │   │   │   ├── EstadoDescuento.java
    │   │   │   │   ├── EstadoOrden.java
    │   │   │   │   ├── EstadoProducto.java
    │   │   │   │   ├── EstadoUsuario.java
    │   │   │   │   ├── RolUsuario.java
    │   │   │   │   └── TipoDescuento.java
    │   │   │   ├── Carrito.java
    │   │   │   ├── Categoria.java
    │   │   │   ├── Descuento.java
    │   │   │   ├── Foto.java
    │   │   │   ├── ItemCarrito.java
    │   │   │   ├── ItemOrden.java
    │   │   │   ├── Marca.java
    │   │   │   ├── Orden.java
    │   │   │   ├── Producto.java
    │   │   │   ├── Usuario.java
    │   │   │   └── VarianteProducto.java
    │   │   ├── repository/                             ← 11 interfaces Spring Data JPA
    │   │   │   ├── CarritoRepository.java
    │   │   │   ├── CategoriaRepository.java
    │   │   │   ├── DescuentoRepository.java
    │   │   │   ├── FotoRepository.java
    │   │   │   ├── ItemCarritoRepository.java
    │   │   │   ├── ItemOrdenRepository.java
    │   │   │   ├── MarcaRepository.java
    │   │   │   ├── OrdenRepository.java
    │   │   │   ├── ProductoRepository.java
    │   │   │   ├── UsuarioRepository.java
    │   │   │   └── VarianteProductoRepository.java
    │   │   ├── security/                               ← Componentes JWT y UserDetails
    │   │   │   ├── JwtAuthenticationFilter.java
    │   │   │   ├── JwtUtil.java
    │   │   │   └── UserDetailsServiceImpl.java
    │   │   └── service/                                ← 11 interfaces + 11 implementaciones
    │   │       ├── impl/
    │   │       │   ├── CarritoServiceImpl.java
    │   │       │   ├── CategoriaServiceImpl.java
    │   │       │   ├── DescuentoServiceImpl.java
    │   │       │   ├── FotoServiceImpl.java
    │   │       │   ├── ItemCarritoServiceImpl.java
    │   │       │   ├── ItemOrdenServiceImpl.java
    │   │       │   ├── MarcaServiceImpl.java
    │   │       │   ├── OrdenServiceImpl.java
    │   │       │   ├── ProductoServiceImpl.java
    │   │       │   ├── UsuarioServiceImpl.java
    │   │       │   └── VarianteProductoServiceImpl.java
    │   │       ├── CarritoService.java
    │   │       ├── CategoriaService.java
    │   │       ├── DescuentoService.java
    │   │       ├── FotoService.java
    │   │       ├── ItemCarritoService.java
    │   │       ├── ItemOrdenService.java
    │   │       ├── MarcaService.java
    │   │       ├── OrdenService.java
    │   │       ├── ProductoService.java
    │   │       ├── UsuarioService.java
    │   │       └── VarianteProductoService.java
    │   └── resources/
    │       ├── application.properties                  ← Configuración base + defaults con env-vars
    │       ├── application-dev.properties              ← MySQL local, ddl-auto=update, SQL logging ON
    │       └── application-mysql.properties            ← MySQL configurable por variables de entorno
    └── test/
        ├── java/com/trekking/ecommerce/               ← 12 clases de test (planas, mismo paquete)
        │   ├── CarritoServiceImplTest.java
        │   ├── DescuentoServiceImplTest.java
        │   ├── EcommerceApplicationTests.java
        │   ├── EndpointFlujosIntegrationTest.java
        │   ├── EndpointNegativosBordeIntegrationTest.java
        │   ├── EndpointSeguridadAutorizacionIntegrationTest.java
        │   ├── GlobalExceptionHandlerIT.java
        │   ├── JwtUtilTest.java
        │   ├── OrdenServiceImplTest.java
        │   ├── ProductoServiceImplTest.java
        │   ├── UsuarioServiceImplTest.java
        │   └── VarianteProductoServiceImplTest.java
        └── resources/
            └── application-test.properties             ← H2 in-memory, MODE=MySQL, create-drop
```

---

### Evaluación de Conformidad con Estándares Maven

#### Aspectos que cumplen el estándar de la industria ✅

| Criterio | Evaluación |
|---|---|
| Layout `src/main/java` / `src/test/java` | Correcto — estándar Maven |
| Separación `controller/` · `service/` · `repository/` · `model/` | Correcto — capas bien definidas |
| DTOs en paquete propio `dto/` | Correcto — separados del modelo |
| Excepciones centralizadas en `exception/` | Correcto — `GlobalExceptionHandler` presente |
| Seguridad en paquete `security/` | Correcto — JWT filter, util, UserDetails agrupados |
| Jobs schedulados en `job/` | Correcto — aislados de la lógica de negocio |
| Enums en subpaquete `model/enums/` | Correcto — no contaminan el paquete raíz |
| Implementaciones en `service/impl/` | Correcto — patrón interface + impl aplicado |
| Configuración en `config/` | Correcto — `SecurityBeansConfig`, `OpenApiConfig` |
| Propiedades por perfil en `resources/` | Correcto — base + `dev` + `mysql` |
| Maven Wrapper incluido | Correcto — `mvnw` / `mvnw.cmd` presentes |

#### Oportunidades de mejora ⚠️

| Criterio | Impacto | Recomendación |
|---|---|---|
| Tests todos en el mismo paquete (sin subpaquetes) | Bajo | Organizar en `service/`, `controller/`, `security/` espejando `main/` |
| Metadatos vacíos en `pom.xml` (`<name/>`, `<description/>`) | Bajo | Completar con valores descriptivos |
| Sin archivo de versión JDK explícita (`.sdkmanrc`, `.java-version`) | Medio | Crear para garantizar entorno homogéneo en el equipo |
| `maven-compiler-plugin` sin `<release>` explícito | Bajo | Agregar `<release>17</release>` (ver Sección 2) |
| Artefactos académicos (`.png`, `.pdf`) en la raíz | Ninguno | Mover a `docs/` para mantener raíz limpia |

#### Reorganización recomendada para los tests

```
src/test/java/com/trekking/ecommerce/
├── controller/
│   ├── EndpointFlujosIntegrationTest.java
│   ├── EndpointNegativosBordeIntegrationTest.java
│   └── EndpointSeguridadAutorizacionIntegrationTest.java
├── exception/
│   └── GlobalExceptionHandlerIT.java
├── security/
│   └── JwtUtilTest.java
├── service/
│   ├── CarritoServiceImplTest.java
│   ├── DescuentoServiceImplTest.java
│   ├── OrdenServiceImplTest.java
│   ├── ProductoServiceImplTest.java
│   ├── UsuarioServiceImplTest.java
│   └── VarianteProductoServiceImplTest.java
└── EcommerceApplicationTests.java
```

---

## Sección 2 — Diagnóstico del JDK y Entorno

### Versión del JDK Detectada

| Fuente de configuración | Valor encontrado | Estado |
|---|---|---|
| `pom.xml` → `<java.version>` | **17** | ✅ Definido correctamente |
| `spring-boot-starter-parent` version | **3.5.12** | ✅ Requiere JDK 17+ (Jakarta EE 10) |
| `maven-compiler-plugin` → `<release>` | **No declarado explícitamente** | ⚠️ Heredado del parent |
| Archivo `.sdkmanrc` / `.java-version` | **No existe** | ⚠️ Sin enforcement de versión |
| `maven-wrapper.properties` | **Maven 3.9.14** | ✅ Versión moderna |
| IDE config (`*.iml`, `.idea/`) | **No comiteados** — en `.gitignore` | ✅ Correcto |

### Análisis de Compatibilidad de Dependencias

```
Spring Boot 3.5.12
    └── Requiere: JDK 17 mínimo (usa Jakarta EE 10, no javax.*)
        └── pom.xml declara: <java.version>17</java.version>  ✅

jjwt (io.jsonwebtoken) 0.12.3
    └── Compatible con JDK 17+  ✅

springdoc-openapi-starter-webmvc-ui 2.8.6
    └── Compatible con Spring Boot 3.x + JDK 17+  ✅

Hibernate 6.x (incluido en Spring Boot 3.5.x)
    └── Requiere JDK 17+  ✅

Lombok (procesador de anotaciones)
    └── Compatible con JDK 17 vía annotationProcessorPaths  ✅
```

> **Conclusión:** La versión JDK 17 está correctamente elegida y declarada. No hay conflictos de versiones. El único problema es la **ausencia de mecanismos de enforcement** que garanticen que todos los desarrolladores del equipo usen exactamente JDK 17.

### Correcciones Recomendadas

#### Corrección A — `<release>` explícito en `maven-compiler-plugin`

```xml
<!-- ESTADO ACTUAL en pom.xml -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>

<!-- RECOMENDADO: agregar <release> explícito -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <release>17</release>
        <annotationProcessorPaths>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

#### Corrección B — Metadatos vacíos del `pom.xml`

```xml
<!-- ESTADO ACTUAL -->
<name/>
<description/>
<url/>

<!-- RECOMENDADO -->
<name>trekking-ecommerce</name>
<description>API REST de e-commerce para equipamiento de trekking — TP APIs 2026</description>
<url>https://github.com/latiny0206-oss/TP-APIS-2026</url>
```

#### Corrección C — Crear `.sdkmanrc` para fijar el JDK en el equipo

```
# .sdkmanrc — usado por SDKMAN! (sdk env)
java=17.0.11-tem
```

#### Corrección D — Crear `.java-version` como alternativa para `jenv`

```
17
```

#### Corrección E — Agregar `maven-enforcer-plugin` para CI/CD

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-enforcer-plugin</artifactId>
    <executions>
        <execution>
            <id>enforce-java-version</id>
            <goals><goal>enforce</goal></goals>
            <configuration>
                <rules>
                    <requireJavaVersion>
                        <version>[17,18)</version>
                        <message>Este proyecto requiere exactamente JDK 17.</message>
                    </requireJavaVersion>
                    <requireMavenVersion>
                        <version>[3.8,)</version>
                    </requireMavenVersion>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### Tabla de Versiones del Entorno

| Herramienta | Versión detectada | Versión recomendada | Obligatorio |
|---|---|---|---|
| JDK | 17 (declarado en `pom.xml`) | **JDK 17 LTS** (Temurin / Corretto / Zulu) | Sí |
| Maven | 3.9.14 (vía wrapper) | 3.9.x | Sí — usar `./mvnw` |
| Docker | No especificado | 24.x o superior | Sí |
| Docker Compose | v2 (`docker compose` sin guión) | v2 | Sí |
| MySQL | 8.4 (imagen Docker) | 8.4.x | Automático vía Docker |

---

## Sección 3 — Mapeo Técnico y Reglas de Negocio

### Diccionario de Entidades con Contexto de Negocio

Esta sección no es solo una lista técnica. Cada entidad tiene un **significado en el negocio** que es importante entender para testear, desarrollar o auditar el sistema correctamente.

#### `Usuario` → tabla `usuario`

**¿Qué es en el negocio?** Representa a cualquier persona que interactúa con la plataforma. Puede ser un cliente que compra, o un administrador que gestiona el catálogo.

| Columna | Tipo Java | Tipo SQL | Restricciones | Descripción |
|---|---|---|---|---|
| `id_usuario` | `Long` | `BIGINT` AI PK | NOT NULL | Identificador único auto-incremental |
| `username` | `String` | `VARCHAR(100)` | NOT NULL · UNIQUE | Nombre de usuario para login |
| `email` | `String` | `VARCHAR(150)` | NOT NULL · UNIQUE | Correo electrónico |
| `password` | `String` | `VARCHAR(255)` | NOT NULL | Hash BCrypt de la contraseña — **nunca se almacena texto plano** |
| `nombre` | `String` | `VARCHAR(100)` | NOT NULL | Nombre real |
| `apellido` | `String` | `VARCHAR(100)` | NOT NULL | Apellido real |
| `rol` | `RolUsuario` | `VARCHAR(20)` | NOT NULL | `ADMIN` / `CLIENTE` |
| `estado` | `EstadoUsuario` | `VARCHAR(20)` | NOT NULL | `ACTIVO` / `INACTIVO` |

> **Regla de seguridad:** El registro público (`POST /api/auth/register`) solo puede crear usuarios con rol `CLIENTE`. El primer `ADMIN` debe insertarse directamente en la base de datos.

---

#### `Producto` → tabla `producto`

**¿Qué es en el negocio?** Es el artículo de trekking (mochila, carpa, bastón, etc.) en su forma más general. No tiene stock propio — el stock está en sus **variantes**.

| Columna | Tipo Java | Tipo SQL | Restricciones | Descripción |
|---|---|---|---|---|
| `id_producto` | `Long` | `BIGINT` AI PK | NOT NULL | Identificador único |
| `id_marca` | `Long` | `BIGINT` FK | NOT NULL | → `marca.id_marca` |
| `id_categoria` | `Long` | `BIGINT` FK | NOT NULL | → `categoria.id_categoria` |
| `nombre` | `String` | `VARCHAR(150)` | NOT NULL | Nombre del producto |
| `descripcion` | `String` | `TEXT` | NULL | Descripción larga |
| `estado` | `EstadoProducto` | `VARCHAR(20)` | NOT NULL | `ACTIVO` / `INACTIVO` / `AGOTADO` |
| `precio_base` | `BigDecimal` | `DECIMAL(10,2)` | NOT NULL · ≥0 | Precio base del producto |

---

#### `Categoria` → tabla `categoria`

**¿Qué es en el negocio?** Agrupa productos por tipo funcional: "Mochilas", "Calzado", "Ropa técnica", etc. Facilita la navegación del catálogo.

| Columna | Tipo Java | Tipo SQL | Restricciones | Descripción |
|---|---|---|---|---|
| `id_categoria` | `Long` | `BIGINT` AI PK | NOT NULL | Identificador único |
| `nombre` | `String` | `VARCHAR(100)` | NOT NULL | Nombre de la categoría |
| `descripcion` | `String` | `TEXT` | NULL | Descripción |

---

#### `Marca` → tabla `marca`

**¿Qué es en el negocio?** La marca comercial del producto: "Black Diamond", "Salomon", "Lowe Alpine", etc. Permite al cliente filtrar por fabricante.

| Columna | Tipo Java | Tipo SQL | Restricciones | Descripción |
|---|---|---|---|---|
| `id_marca` | `Long` | `BIGINT` AI PK | NOT NULL | Identificador único |
| `nombre` | `String` | `VARCHAR(100)` | NOT NULL | Nombre de la marca |
| `descripcion` | `String` | `TEXT` | NULL | Descripción |

---

#### `VarianteProducto` → tabla `variante_producto`

**¿Qué es en el negocio?** La unidad real de stock y venta. Un producto "Mochila Trail 45L" puede tener variantes: azul/talla M, rojo/talla L, etc. Cada combinación es una variante independiente con su propio stock y precio.

| Columna | Tipo Java | Tipo SQL | Restricciones | Descripción |
|---|---|---|---|---|
| `id_variante` | `Long` | `BIGINT` AI PK | NOT NULL | Identificador único |
| `id_producto` | `Long` | `BIGINT` FK | NOT NULL | → `producto.id_producto` |
| `color` | `String` | `VARCHAR(50)` | NOT NULL | Color de la variante |
| `talla` | `String` | `VARCHAR(20)` | NOT NULL | Talla (S, M, L, XL, etc.) |
| `material` | `String` | `VARCHAR(100)` | NOT NULL | Material de fabricación |
| `peso` | `BigDecimal` | `DECIMAL(8,2)` | NOT NULL · ≥0 | Peso en kg |
| `stock` | `Integer` | `INT` | NOT NULL | Unidades disponibles en inventario |
| `precio` | `BigDecimal` | `DECIMAL(10,2)` | NOT NULL · ≥0 | Precio específico de esta variante |
| `estacion` | `Estacion` | `VARCHAR(20)` | NOT NULL | `VERANO` / `INVIERNO` / `PRIMAVERA` / `OTOÑO` / `TODO` |

> **Constraint único:** `uk_variante_producto_color_talla` sobre `(id_producto, color, talla)` — impide variantes duplicadas del mismo producto. Si se intenta crear una variante con la misma combinación `(producto, color, talla)` que ya existe, la base de datos rechaza la operación.

---

#### `Foto` → tabla `foto`

**¿Qué es en el negocio?** Las imágenes que muestran cómo se ve cada variante de producto. Una variante puede tener múltiples fotos ordenadas.

| Columna | Tipo Java | Tipo SQL | Restricciones | Descripción |
|---|---|---|---|---|
| `id_foto` | `Long` | `BIGINT` AI PK | NOT NULL | Identificador único |
| `id_variante` | `Long` | `BIGINT` FK | NOT NULL | → `variante_producto.id_variante` |
| `nombre` | `String` | `VARCHAR(255)` | NOT NULL | Nombre del archivo original |
| `tipo_contenido` | `String` | `VARCHAR(100)` | NOT NULL | MIME type (ej. `image/jpeg`) |
| `orden` | `Integer` | `INT` | NOT NULL | Posición de visualización |
| `datos` | `byte[]` | `LONGBLOB` | NOT NULL | Bytes binarios de la imagen |

> **Decisión arquitectónica — Almacenamiento en DB:** Las imágenes se guardan directamente en MySQL como `LONGBLOB`. La API las devuelve codificadas en **Base64** dentro del JSON de respuesta. Esta decisión simplifica el deployment (no se necesita S3, CDN ni filesystem compartido) a cambio de mayor tamaño en la base de datos y mayor latencia en respuestas con fotos.

---

#### `Carrito` → tabla `carrito`

**¿Qué es en el negocio?** La bolsa de compras temporal del cliente. Puede estar `ACTIVO` (el cliente lo está usando), `ABANDONADO` (no tuvo actividad por demasiado tiempo — marcado por un job automático) o `COMPRADO` (el cliente hizo checkout y se generó una Orden).

| Columna | Tipo Java | Tipo SQL | Restricciones | Descripción |
|---|---|---|---|---|
| `id_carrito` | `Long` | `BIGINT` AI PK | NOT NULL | Identificador único |
| `id_usuario` | `Long` | `BIGINT` FK | NOT NULL | → `usuario.id_usuario` |
| `id_descuento` | `Long` | `BIGINT` FK | NULL | → `descuento.id_descuento` (opcional) |
| `estado` | `EstadoCarrito` | `VARCHAR(20)` | NOT NULL | `ACTIVO` / `ABANDONADO` / `COMPRADO` |
| `monto_total` | `BigDecimal` | `DECIMAL(10,2)` | NOT NULL · ≥0 | Total calculado del carrito |
| `fecha_ultima_modificacion` | `LocalDateTime` | `DATETIME` | NULL | Auto-seteado por `@PrePersist` / `@PreUpdate` |

> **Mecanismo automático:** La entidad `Carrito` usa `@PrePersist` y `@PreUpdate` para actualizar `fechaUltimaModificacion` automáticamente cada vez que se persiste o actualiza. El `CarritoJob` consulta esta columna para detectar carritos inactivos.

---

#### `ItemCarrito` → tabla `item_carrito`

**¿Qué es en el negocio?** Una línea dentro del carrito que dice "tengo X unidades de la variante Y a precio Z". Hay un `ItemCarrito` por cada variante distinta dentro del carrito.

| Columna | Tipo Java | Tipo SQL | Restricciones | Descripción |
|---|---|---|---|---|
| `id_item_carrito` | `Long` | `BIGINT` AI PK | NOT NULL | Identificador único |
| `id_carrito` | `Long` | `BIGINT` FK | NOT NULL | → `carrito.id_carrito` |
| `id_variante` | `Long` | `BIGINT` FK | NOT NULL | → `variante_producto.id_variante` |
| `cantidad` | `Integer` | `INT` | NOT NULL · ≥1 | Cantidad de unidades |
| `precio_unitario` | `BigDecimal` | `DECIMAL(10,2)` | NOT NULL · ≥0 | Precio al momento de agregar al carrito |

---

#### `Orden` → tabla `orden`

**¿Qué es en el negocio?** El comprobante de compra generado después de un checkout exitoso. Nace en estado `PENDIENTE` y transiciona a `CONFIRMADA` o `CANCELADA`.

| Columna | Tipo Java | Tipo SQL | Restricciones | Descripción |
|---|---|---|---|---|
| `id_orden` | `Long` | `BIGINT` AI PK | NOT NULL | Identificador único |
| `id_usuario` | `Long` | `BIGINT` FK | NOT NULL | → `usuario.id_usuario` |
| `id_carrito` | `Long` | `BIGINT` FK | NULL | → `carrito.id_carrito` (origen del checkout) |
| `id_descuento` | `Long` | `BIGINT` FK | NULL | → `descuento.id_descuento` (aplicado) |
| `fecha_creacion` | `LocalDateTime` | `DATETIME` | NOT NULL | Momento exacto de la compra |
| `monto_final` | `BigDecimal` | `DECIMAL(10,2)` | NOT NULL · ≥0 | Total tras aplicar descuento |
| `estado` | `EstadoOrden` | `VARCHAR(20)` | NOT NULL | `PENDIENTE` / `CONFIRMADA` / `CANCELADA` |

---

#### `ItemOrden` → tabla `item_orden`

**¿Qué es en el negocio?** Una línea del comprobante de compra. Registra exactamente qué variante se compró, en qué cantidad y a qué precio **en el momento de la compra**. Este precio nunca cambia, incluso si el precio del producto cambia después.

| Columna | Tipo Java | Tipo SQL | Restricciones | Descripción |
|---|---|---|---|---|
| `id_item_orden` | `Long` | `BIGINT` AI PK | NOT NULL | Identificador único |
| `id_orden` | `Long` | `BIGINT` FK | NOT NULL | → `orden.id_orden` |
| `id_variante` | `Long` | `BIGINT` FK | NOT NULL | → `variante_producto.id_variante` |
| `cantidad` | `Integer` | `INT` | NOT NULL · ≥1 | Cantidad comprada |
| `precio_al_momento` | `BigDecimal` | `DECIMAL(10,2)` | NOT NULL · ≥0 | **Precio histórico congelado** — invariante clave |

---

#### `Descuento` → tabla `descuento`

**¿Qué es en el negocio?** Un cupón o promoción que reduce el total del carrito. Puede ser un porcentaje del total ("20% de descuento") o un monto fijo ("$500 de descuento"). Tiene fechas de inicio y fin, y el `DescuentoJob` lo marca como `EXPIRADO` automáticamente cuando vence.

| Columna | Tipo Java | Tipo SQL | Restricciones | Descripción |
|---|---|---|---|---|
| `id_descuento` | `Long` | `BIGINT` AI PK | NOT NULL | Identificador único |
| `nombre` | `String` | `VARCHAR(100)` | NOT NULL | Nombre del descuento / cupón |
| `tipo` | `TipoDescuento` | `VARCHAR(20)` | NOT NULL | `PORCENTAJE` / `MONTO_FIJO` |
| `valor` | `BigDecimal` | `DECIMAL(10,2)` | NOT NULL · ≥0 | Valor del descuento |
| `fecha_ini` | `LocalDate` | `DATE` | NOT NULL | Inicio de vigencia |
| `fecha_fin` | `LocalDate` | `DATE` | NOT NULL | Fin de vigencia |
| `estado` | `EstadoDescuento` | `VARCHAR(20)` | NOT NULL | `ACTIVO` / `INACTIVO` / `EXPIRADO` |

---

### Diagrama Relacional Simplificado

```
marca ──(1:N)──► producto ◄──(N:1)── categoria
                    │
                  (1:N)
                    │
            variante_producto ──(1:N)──► foto
                 │        │
               (1:N)    (1:N)
                 │        │
          item_carrito  item_orden
                 │        │
               (N:1)    (N:1)
                 │        │
              carrito   orden ──(N:1)──► descuento
                 │        │
               (N:1)    (N:1)
                 └────────┘
                  usuario
```

---

### Reglas de Negocio Críticas

Esta sección documenta decisiones de diseño y reglas de dominio que todo miembro del equipo **debe conocer** antes de tocar el código o escribir tests.

---

#### RN-01 — Price Snapshot (Congelamiento de Precio en Checkout)

**¿Qué problema resuelve?** Si un cliente compró una mochila a $15.000 en enero y en febrero el administrador sube el precio a $20.000, el historial de órdenes del cliente debe seguir mostrando $15.000. Sin esta regla, el historial de compras sería incorrecto.

**¿Cómo funciona?** Al momento del checkout, `CarritoServiceImpl.realizarCompra()` toma el precio actual de cada `VarianteProducto` y lo copia al campo `precio_al_momento` del `ItemOrden` recién creado. A partir de ese momento, el `ItemOrden` es inmutable desde el punto de vista del precio.

```java
// Pseudocódigo de lo que ocurre en realizarCompra()
for (ItemCarrito item : carrito.getItems()) {
    ItemOrden itemOrden = ItemOrden.builder()
        .variante(item.getVariante())
        .cantidad(item.getCantidad())
        .precioAlMomento(item.getVariante().getPrecio()) // ← copia el precio actual
        .build();
}
```

> **Regla para QA:** Al testear el historial de órdenes, verificar que `precio_al_momento` en `item_orden` NO cambia si se modifica el precio de la variante después del checkout.

---

#### RN-02 — Normalización del CUIT

**¿Qué es el CUIT?** El CUIT (Clave Única de Identificación Tributaria) es el identificador fiscal argentino. Tiene el formato `XX-XXXXXXXX-X`. El primer segmento de dos dígitos indica el tipo de contribuyente.

**Regla de normalización del equipo:** Cuando el sistema recibe un CUIT que comienza con `"2"` (contribuyentes personas físicas, ej: `20-12345678-5`), se normaliza automáticamente cambiando el dígito inicial por `"3"` para unificarlo al formato de personas jurídicas (`30-12345678-5`).

**¿Por qué?** Esta decisión responde a un requerimiento del proceso de facturación: el sistema de emisión de facturas electrónicas del cliente trabaja exclusivamente con CUIT de tipo "30" (empresas). Antes de enviar datos al módulo de facturación, todos los CUIT personales son normalizados al formato corporativo.

```java
// Implementación del método de normalización
public static String normalizarCuit(String cuit) {
    if (cuit != null && cuit.startsWith("2")) {
        return "3" + cuit.substring(1);
    }
    return cuit;
}
```

> **Regla para QA:** Al enviar CUIT `"20-12345678-5"` al sistema, el valor almacenado y devuelto debe ser `"30-12345678-5"`. Testear casos con `"2"`, `"3"`, `null` y formato inválido.

---

#### RN-03 — Métodos que Retornan `null` en Lugar de `Optional`

**Contexto:** En Java moderno, la convención es usar `Optional<T>` para señalar que un método puede no retornar un valor. Sin embargo, este equipo ha tomado la **decisión arquitectónica** de usar retorno de `null` explícito en los métodos de validación interna de los servicios.

**Justificación:** Los métodos de búsqueda/validación internos (no los públicos de los repositorios) retornan `null` cuando no encuentran un resultado en lugar de `Optional.empty()`. Esto reduce el boilerplate de `.isPresent()` / `.get()` en el código de servicio cuando el flujo de control ya maneja el `null` explícitamente con un `if`.

```java
// Convención del proyecto: retorna null, no Optional
public Usuario findByUsernameOrNull(String username) {
    return usuarioRepository.findByUsername(username).orElse(null);
}

// El llamador verifica null directamente
Usuario usuario = findByUsernameOrNull(username);
if (usuario == null) {
    throw new ResourceNotFoundException("Usuario no encontrado");
}
```

> **Regla para el equipo:** No cambiar estos métodos para retornar `Optional<T>` sin consultar al Tech Lead. El patrón es intencional y consistente a lo largo de la capa de servicio.

---

#### RN-04 — Base de Datos: MySQL (y compatibilidad con el estándar SQL)

**Motor de base de datos en uso:** El proyecto utiliza **MySQL 8.4** tanto en desarrollo como en producción, administrado a través de Docker Compose. Los tests de integración usan **H2 en modo `MODE=MySQL`** para emular el comportamiento de MySQL sin requerir Docker.

**Consideración de portabilidad:** El esquema y las queries JPQL están escritas para ser compatibles con SQL estándar. Si en el futuro se decide migrar el motor de base de datos, los puntos de cambio son:
1. `application-*.properties`: cambiar el `spring.datasource.url` y el driver.
2. `spring.jpa.database-platform`: ajustar el dialecto Hibernate.
3. `docker-compose.yml`: cambiar la imagen del contenedor.

> **Nota para el equipo:** Toda query custom debe escribirse en **JPQL** (no SQL nativo) para maximizar la portabilidad entre motores. SQL nativo (`@Query(nativeQuery = true)`) solo debe usarse si la query JPQL equivalente no es posible, y debe documentarse el motivo.

---

#### RN-05 — Gestión de Stock

**Flujo de stock en el ciclo de vida de una orden:**

```
[checkout]  → stock de cada variante se DECREMENTA inmediatamente
[confirmar] → sin cambio de stock (ya fue descontado en checkout)
[cancelar]  → stock de cada variante se RESTAURA (incrementa)
```

**¿Por qué se decrementa en checkout y no en confirmación?** Para evitar overselling: si el stock se descuenta recién al confirmar, dos clientes podrían hacer checkout del mismo ítem y ambos tendrían órdenes pendientes para un stock que solo alcanza para uno.

---

#### RN-06 — Carritos Abandonados y Descuentos Expirados (Jobs Automáticos)

El sistema tiene dos tareas programadas (`@Scheduled`) que corren en background:

| Job | Trigger | Acción |
|---|---|---|
| `CarritoJob` | Periódico (configurable) | Busca carritos `ACTIVO` cuya `fecha_ultima_modificacion` supera el umbral → los marca `ABANDONADO` |
| `DescuentoJob` | Periódico (configurable) | Busca descuentos `ACTIVO` cuya `fecha_fin` ya pasó → los marca `EXPIRADO` |

> **Implicación para QA:** Los tests de integración que involucran estados de carritos y descuentos deben considerar que estos jobs pueden interferir en entornos de larga duración. En los tests unitarios, mockear el tiempo (`Clock`) para controlar el comportamiento.

---

### Tabla de Endpoints (API REST)

Leyenda de acceso:
- **Público** — sin token JWT, cualquiera puede llamar el endpoint
- **Auth** — requiere token JWT válido (cualquier rol)
- **ADMIN** — requiere token JWT con rol `ADMIN`
- **Propietario** — requiere ser el dueño del recurso O ser `ADMIN`

---

#### Autenticación — `/api/auth`

> Estos endpoints son el punto de entrada al sistema. El cliente primero se registra o inicia sesión y recibe un token JWT que debe usar en todos los demás requests.

| Método | URL | DTO Entrada | DTO Salida | Acceso | Descripción funcional |
|---|---|---|---|---|---|
| `POST` | `/api/auth/login` | `LoginRequest` | `AuthResponse` | Público | Autentica al usuario y devuelve un token JWT con 24h de validez |
| `POST` | `/api/auth/register` | `RegisterRequest` | `AuthResponse` | Público | Crea una nueva cuenta de cliente. Rol siempre `CLIENTE` |

---

#### Usuarios — `/api/usuarios`

> Gestión administrativa de cuentas. Solo un ADMIN puede ver, crear, modificar o eliminar usuarios. Un cliente no puede ver la lista de otros clientes.

| Método | URL | DTO Entrada | DTO Salida | Acceso | Descripción funcional |
|---|---|---|---|---|---|
| `GET` | `/api/usuarios` | — | `List<UsuarioResponse>` | `ADMIN` | Lista todos los usuarios del sistema |
| `GET` | `/api/usuarios/{id}` | — | `UsuarioResponse` | `ADMIN` | Obtiene el detalle de un usuario por ID |
| `POST` | `/api/usuarios` | `UsuarioRequest` | `UsuarioResponse` | `ADMIN` | Crea un usuario (puede asignar cualquier rol) |
| `PUT` | `/api/usuarios/{id}` | `UsuarioRequest` | `UsuarioResponse` | `ADMIN` | Actualiza datos de un usuario |
| `DELETE` | `/api/usuarios/{id}` | — | `204 No Content` | `ADMIN` | Elimina un usuario |

---

#### Productos — `/api/productos`

> El catálogo es público para lectura: cualquier visitante puede ver los productos sin iniciar sesión. Solo el administrador puede modificarlo.

| Método | URL | DTO Entrada | DTO Salida | Acceso | Descripción funcional |
|---|---|---|---|---|---|
| `GET` | `/api/productos` | — | `List<ProductoResponse>` | Público | Lista todos los productos del catálogo |
| `GET` | `/api/productos/{id}` | — | `ProductoResponse` | Público | Detalle de un producto específico |
| `GET` | `/api/productos/categoria/{categoriaId}` | — | `List<ProductoResponse>` | Público | Filtra productos por categoría |
| `GET` | `/api/productos/marca/{marcaId}` | — | `List<ProductoResponse>` | Público | Filtra productos por marca |
| `GET` | `/api/productos/estado/{estado}` | — | `List<ProductoResponse>` | Público | Filtra por estado (`ACTIVO`, `INACTIVO`, `AGOTADO`) |
| `GET` | `/api/productos/{id}/disponible` | — | `Boolean` | Público | Indica si el producto tiene al menos una variante con stock |
| `POST` | `/api/productos` | `ProductoRequest` | `ProductoResponse` | `ADMIN` | Crea un producto en el catálogo |
| `PUT` | `/api/productos/{id}` | `ProductoRequest` | `ProductoResponse` | `ADMIN` | Actualiza un producto |
| `DELETE` | `/api/productos/{id}` | — | `204 No Content` | `ADMIN` | Elimina un producto |

---

#### Categorías — `/api/categorias`

| Método | URL | DTO Entrada | DTO Salida | Acceso | Descripción funcional |
|---|---|---|---|---|---|
| `GET` | `/api/categorias` | — | `List<CategoriaResponse>` | Público | Lista todas las categorías |
| `GET` | `/api/categorias/{id}` | — | `CategoriaResponse` | Público | Detalle de una categoría |
| `POST` | `/api/categorias` | `CategoriaRequest` | `CategoriaResponse` | `ADMIN` | Crea una nueva categoría |
| `PUT` | `/api/categorias/{id}` | `CategoriaRequest` | `CategoriaResponse` | `ADMIN` | Actualiza una categoría |
| `DELETE` | `/api/categorias/{id}` | — | `204 No Content` | `ADMIN` | Elimina una categoría |

---

#### Marcas — `/api/marcas`

| Método | URL | DTO Entrada | DTO Salida | Acceso | Descripción funcional |
|---|---|---|---|---|---|
| `GET` | `/api/marcas` | — | `List<MarcaResponse>` | Público | Lista todas las marcas |
| `GET` | `/api/marcas/{id}` | — | `MarcaResponse` | Público | Detalle de una marca |
| `POST` | `/api/marcas` | `MarcaRequest` | `MarcaResponse` | `ADMIN` | Crea una nueva marca |
| `PUT` | `/api/marcas/{id}` | `MarcaRequest` | `MarcaResponse` | `ADMIN` | Actualiza una marca |
| `DELETE` | `/api/marcas/{id}` | — | `204 No Content` | `ADMIN` | Elimina una marca |

---

#### Variantes de Producto — `/api/variantes`

> Las variantes son las unidades reales de stock. Cuando el cliente agrega algo al carrito, en realidad agrega una **variante** (color + talla específicos), no el producto genérico.

| Método | URL | DTO Entrada | DTO Salida | Acceso | Descripción funcional |
|---|---|---|---|---|---|
| `GET` | `/api/variantes` | — | `List<VarianteProductoResponse>` | Público | Lista todas las variantes |
| `GET` | `/api/variantes/{id}` | — | `VarianteProductoResponse` | Público | Detalle de una variante |
| `GET` | `/api/variantes/{id}/precio` | — | `BigDecimal` | Público | Precio actual de una variante |
| `GET` | `/api/variantes/{id}/stock/disponible?cantidad={n}` | Query param | `Boolean` | Público | Verifica si hay stock suficiente para la cantidad pedida |
| `POST` | `/api/variantes` | `VarianteProductoRequest` | `VarianteProductoResponse` | `ADMIN` | Crea una nueva variante para un producto |
| `PUT` | `/api/variantes/{id}` | `VarianteProductoRequest` | `VarianteProductoResponse` | `ADMIN` | Actualiza una variante (precio, stock, etc.) |
| `DELETE` | `/api/variantes/{id}` | — | `204 No Content` | `ADMIN` | Elimina una variante |

---

#### Fotos — `/api/fotos`

> Las fotos se cargan como archivos multipart y se almacenan como BLOB en la base de datos. La respuesta siempre incluye los datos en Base64.

| Método | URL | DTO Entrada | DTO Salida | Acceso | Descripción funcional |
|---|---|---|---|---|---|
| `GET` | `/api/fotos` | — | `List<FotoResponse>` | Público | Lista todas las fotos |
| `GET` | `/api/fotos/{id}` | — | `FotoResponse` (Base64) | Público | Obtiene una foto con sus datos binarios en Base64 |
| `GET` | `/api/fotos/variante/{varianteId}` | — | `List<FotoResponse>` | Público | Todas las fotos de una variante específica |
| `POST` | `/api/fotos` | `multipart/form-data` | `FotoResponse` | `ADMIN` | Sube una foto para una variante (`varianteId`, `orden`, archivo) |
| `PUT` | `/api/fotos/{id}` | `multipart/form-data` | `FotoResponse` | `ADMIN` | Reemplaza una foto existente |
| `DELETE` | `/api/fotos/{id}` | — | `204 No Content` | `ADMIN` | Elimina una foto |

---

#### Carritos — `/api/carritos`

> Los endpoints de carrito aplican la regla de **ownership**: solo el dueño del carrito o un ADMIN puede operar sobre él. La lógica se resuelve en runtime mediante `AuthenticatedController.validarPropietario()`.

| Método | URL | DTO Entrada | DTO Salida | Acceso | Descripción funcional |
|---|---|---|---|---|---|
| `GET` | `/api/carritos` | — | `List<CarritoResponse>` | Auth | ADMIN ve todos los carritos · CLIENTE ve solo los suyos |
| `GET` | `/api/carritos/{id}` | — | `CarritoResponse` | Propietario | Detalle de un carrito con sus ítems |
| `POST` | `/api/carritos` | `CarritoRequest` | `CarritoResponse` | Auth | Crea un nuevo carrito vacío para el usuario autenticado |
| `PUT` | `/api/carritos/{id}` | `CarritoRequest` | `CarritoResponse` | Propietario | Actualiza el carrito (ej: aplica descuento) |
| `DELETE` | `/api/carritos/{id}` | — | `204 No Content` | Propietario | Elimina el carrito |
| `GET` | `/api/carritos/{id}/items` | — | `List<ItemCarritoResponse>` | Propietario | Lista los ítems del carrito |
| `POST` | `/api/carritos/{id}/items` | `ItemCarritoRequest` | `ItemCarritoResponse` | Propietario | Agrega una variante al carrito |
| `PUT` | `/api/carritos/{id}/items/{idItem}?cantidad={n}` | Query param | `ItemCarritoResponse` | Propietario | Cambia la cantidad de un ítem |
| `DELETE` | `/api/carritos/{id}/items/{idItem}` | — | `204 No Content` | Propietario | Elimina un ítem del carrito |
| `GET` | `/api/carritos/{id}/total` | — | `BigDecimal` | Propietario | Calcula el total actual del carrito |
| `POST` | `/api/carritos/{id}/vaciar` | — | `204 No Content` | Propietario | Elimina todos los ítems del carrito |
| `POST` | `/api/carritos/{id}/checkout` | — | `OrdenResponse` | Propietario | **Convierte el carrito en una Orden** — el endpoint más crítico del negocio |

---

#### Órdenes — `/api/ordenes`

| Método | URL | DTO Entrada | DTO Salida | Acceso | Descripción funcional |
|---|---|---|---|---|---|
| `GET` | `/api/ordenes` | — | `List<OrdenResponse>` | Auth | ADMIN ve todas · CLIENTE ve solo las suyas |
| `GET` | `/api/ordenes/{id}` | — | `OrdenResponse` | Propietario | Detalle de una orden |
| `POST` | `/api/ordenes` | `OrdenRequest` | `OrdenResponse` | `ADMIN` | Crea una orden manualmente (sin checkout) |
| `PUT` | `/api/ordenes/{id}` | `OrdenRequest` | `OrdenResponse` | `ADMIN` | Actualiza una orden |
| `DELETE` | `/api/ordenes/{id}` | — | `204 No Content` | Propietario | Elimina una orden |
| `POST` | `/api/ordenes/{id}/confirmar` | — | `OrdenResponse` | Propietario | Confirma la orden (`PENDIENTE` → `CONFIRMADA`) |
| `POST` | `/api/ordenes/{id}/cancelar` | — | `OrdenResponse` | Propietario | Cancela la orden y **restaura el stock** |
| `GET` | `/api/ordenes/{id}/monto-final` | — | `BigDecimal` | Propietario | Total final de la orden |
| `GET` | `/api/ordenes/{id}/items` | — | `List<ItemOrdenResponse>` | Propietario | Ítems con precios históricos congelados |
| `GET` | `/api/ordenes/usuario/{idUsuario}` | — | `List<OrdenResponse>` | Propietario | Historial de compras de un usuario |

---

#### Descuentos — `/api/descuentos`

| Método | URL | DTO Entrada | DTO Salida | Acceso | Descripción funcional |
|---|---|---|---|---|---|
| `GET` | `/api/descuentos` | — | `List<DescuentoResponse>` | `ADMIN` | Lista todos los descuentos (incluye inactivos/expirados) |
| `GET` | `/api/descuentos/activos` | — | `List<DescuentoResponse>` | Auth | Lista solo los descuentos vigentes (para el cliente al aplicar cupón) |
| `GET` | `/api/descuentos/{id}` | — | `DescuentoResponse` | `ADMIN` | Detalle de un descuento |
| `GET` | `/api/descuentos/{id}/vigente` | — | `Boolean` | `ADMIN` | Verifica si el descuento está vigente a la fecha actual |
| `GET` | `/api/descuentos/{id}/calcular?monto={n}` | Query param | `BigDecimal` | `ADMIN` | Simula el descuento sobre un monto dado |
| `POST` | `/api/descuentos` | `DescuentoRequest` | `DescuentoResponse` | `ADMIN` | Crea un nuevo descuento |
| `PUT` | `/api/descuentos/{id}` | `DescuentoRequest` | `DescuentoResponse` | `ADMIN` | Actualiza un descuento |
| `DELETE` | `/api/descuentos/{id}` | — | `204 No Content` | `ADMIN` | Elimina un descuento |

---

#### Documentación API (rutas públicas)

| Path | Descripción |
|---|---|
| `/swagger-ui.html` | UI interactiva de Swagger — explorar y probar endpoints |
| `/api-docs` | Especificación OpenAPI en JSON |
| `/v3/api-docs/**` | Spec OpenAPI (ruta alternativa springdoc) |

---

### Flujo de Funcionamiento entre Capas

#### Ejemplo completo: `POST /api/carritos/{id}/checkout`

**Etapa 1 — `JwtAuthenticationFilter` (antes del Dispatcher)**

El filtro extiende `OncePerRequestFilter` y corre sobre toda petición:

1. Extrae `Authorization: Bearer <token>` del header
2. Llama a `JwtUtil.extractUsername(token)` para recuperar el subject del payload JWT
3. Si el `SecurityContext` está vacío, carga el usuario via `UserDetailsServiceImpl.loadUserByUsername()`
4. `UserDetailsServiceImpl` consulta `UsuarioRepository.findByUsername()` y mapea `RolUsuario` a `GrantedAuthority` de Spring (`ROLE_ADMIN` / `ROLE_CLIENTE`)
5. Si `JwtUtil.isTokenValid()` pasa, establece `UsernamePasswordAuthenticationToken` en el `SecurityContextHolder`

**Etapa 2 — `AuthorizationFilter` (evaluación de reglas)**

`/api/carritos/**` no tiene regla explícita → cae en `.anyRequest().authenticated()`. Solo exige sesión activa, sin restricción de rol.

**Etapa 3 — `CarritoController.realizarCompra()` (validación de ownership)**

`CarritoController` hereda de `AuthenticatedController`:

```java
validarPropietario(carritoService.findById(id).getUsuario().getId());
```

Si el usuario no es admin y el carrito no le pertenece → lanza `AccessDeniedException` → `403 Forbidden`.

**Etapa 4 — `CarritoServiceImpl.realizarCompra()` (lógica de negocio)**

1. Carga el `Carrito` con sus `ItemCarrito` desde `CarritoRepository`
2. Valida estado `ACTIVO` y carrito no vacío — lanza `BusinessRuleException` si falla
3. Para cada `ItemCarrito`, verifica stock via `VarianteProductoService.tieneStock()`
4. Si hay descuento, calcula monto via `DescuentoService.calcularDescuento()`
5. Descuenta stock de cada `VarianteProducto` y persiste el decremento
6. Crea `Orden` en estado `PENDIENTE` con cada `ItemOrden` con `precio_al_momento` congelado
7. Cambia estado del `Carrito` a `COMPRADO`
8. Persiste todo via los repositories correspondientes

**Etapa 5 — Serialización y respuesta**

El controlador mapea `Orden` → `OrdenResponse` con el método privado `toOrdenResponse()`. Se retorna `200 OK`.

**Etapa 6 — `GlobalExceptionHandler` (manejo de errores)**

`@RestControllerAdvice` intercepta cualquier excepción no atrapada:

| Excepción | HTTP Status | Causa |
|---|---|---|
| `ResourceNotFoundException` | `404 Not Found` | Entidad no encontrada por ID |
| `BusinessRuleException` | `422 Unprocessable Entity` | Regla de negocio violada |
| `MethodArgumentNotValidException` | `400 Bad Request` | Validación `@Valid` fallida |
| `AccessDeniedException` | `403 Forbidden` | Acceso denegado por ownership o rol |

---

#### Patrones de Diseño Aplicados

| Patrón | Ubicación | Beneficio |
|---|---|---|
| **Repository** | 11 `JpaRepository` | Abstracción total de la persistencia |
| **Service Layer** | 11 `Service` + `ServiceImpl` | Encapsula lógica de negocio |
| **Template Method** | `AuthenticatedController` | Reutiliza lógica de ownership |
| **Strategy (implícito)** | `DescuentoService.calcularDescuento()` | Lógica diferente por `TipoDescuento` |
| **DTO** | 25 clases Request / Response | Desacopla API del modelo de dominio |
| **Price Snapshot** | `ItemOrden.precio_al_momento` | Integridad histórica de precios |
| **FETCH JOIN** | `CarritoRepository`, `OrdenRepository` | Evita el problema N+1 de JPA |

---

## Sección 4 — Arquitectura y Seguridad

### El Ciclo de Vida de un Request (Explicación para Nuevos Integrantes)

> Imagina que estás haciendo una compra en la tienda online desde tu celular. Tocas el botón "Finalizar compra". ¿Qué pasa exactamente desde ese momento hasta que ves la confirmación? Aquí está el recorrido completo de tu request por el sistema.

**1. El HTTP Request llega al servidor**

Tu celular envía un `POST /api/carritos/42/checkout` al servidor. En el header va tu token JWT (lo recibiste cuando iniciaste sesión). El servidor Tomcat, embebido en Spring Boot, recibe la conexión.

**2. El request entra a la Security Filter Chain**

Antes de llegar a ningún código de negocio, el request pasa por una cadena de filtros de seguridad. Piénsalo como los controles de seguridad de un aeropuerto: cada filtro es una puerta.

- **CorsFilter:** verifica que la petición viene de un origen permitido (en desarrollo, cualquier origen está permitido; en producción se restringiría).
- **JwtAuthenticationFilter:** lee tu token JWT del header, verifica que es válido y no está vencido, extrae tu `username` del payload, consulta la base de datos para cargarte como usuario, y registra tu identidad en el contexto de seguridad de la aplicación. Si el token es inválido, la petición continúa sin usuario autenticado.
- **AuthorizationFilter:** con tu identidad ya cargada, este filtro evalúa si tienes permiso para acceder a `POST /api/carritos/**`. En este caso, la regla es "autenticado" — como tienes token válido, pasas.

**3. El request llega al Controller**

`CarritoController.realizarCompra()` se ejecuta. Pero antes de llamar al servicio, el controlador hereda de `AuthenticatedController` y llama a `validarPropietario()`: compara tu ID de usuario (del SecurityContext) con el ID del dueño del carrito #42. Si no son el mismo (y no eres admin), se lanza una excepción y recibes un `403 Forbidden`.

**4. La lógica de negocio se ejecuta en el Service**

`CarritoServiceImpl.realizarCompra()` hace todo el trabajo real: verificar stock, calcular descuentos, crear la orden, congelar los precios, descontar stock, cambiar estados. Si algo falla (carrito vacío, sin stock, estado incorrecto), lanza una `BusinessRuleException` que el `GlobalExceptionHandler` convierte en `422 Unprocessable Entity`.

**5. Los datos se persisten en la base de datos**

Los repositorios (implementaciones de `JpaRepository`) traducen los objetos Java a SQL y ejecutan las operaciones en MySQL. Hibernate gestiona el mapeado objeto-relacional.

**6. La respuesta regresa por el mismo camino**

El service retorna la `Orden` creada. El controller la mapea a un `OrdenResponse` DTO y lo retorna. Spring serializa el DTO a JSON. El JSON viaja de vuelta por HTTP y aparece en la pantalla de tu celular.

**El camino completo:**

```
Celular → [HTTP] → Tomcat → CorsFilter → JwtAuthFilter → AuthorizationFilter
       → CarritoController → validarPropietario() → CarritoServiceImpl
       → CarritoRepository / OrdenRepository / VarianteRepository → MySQL
       → (respuesta) → OrdenResponse → JSON → [HTTP] → Celular
```

---

### Diagrama de Capas

```
┌────────────────────────────────────────────────────────────────────┐
│                        CLIENTE HTTP                                 │
│           (Browser · Mobile App · Postman · Frontend)              │
└───────────────────────────────┬────────────────────────────────────┘
                                │ HTTP Request
                                ▼
┌────────────────────────────────────────────────────────────────────┐
│                 SPRING SECURITY FILTER CHAIN                        │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │ CorsFilter                                                   │   │
│  │   allowedOriginPatterns=*  ·  credentials=true               │   │
│  └──────────────────────────┬──────────────────────────────────┘   │
│  ┌───────────────────────────▼──────────────────────────────────┐  │
│  │ JwtAuthenticationFilter  (OncePerRequestFilter)               │  │
│  │   ① Lee "Authorization: Bearer <token>"                       │  │
│  │   ② JwtUtil.extractUsername() → subject del JWT               │  │
│  │   ③ UserDetailsServiceImpl.loadUserByUsername() → DB          │  │
│  │   ④ JwtUtil.isTokenValid()                                    │  │
│  │   ⑤ SecurityContextHolder.setAuthentication()                 │  │
│  └──────────────────────────┬──────────────────────────────────┘   │
│  ┌───────────────────────────▼──────────────────────────────────┐  │
│  │ AuthorizationFilter  (Spring Security interno)                │  │
│  │   Público:      /api/auth/** · Swagger · GET catálogo         │  │
│  │   Autenticado:  /api/carritos/** · /api/ordenes/**            │  │
│  │                 GET /api/descuentos/activos                   │  │
│  │   Solo ADMIN:   POST/PUT/DELETE catálogo                      │  │
│  │                 /api/usuarios/** · /api/descuentos/**         │  │
│  └──────────────────────────┬──────────────────────────────────┘   │
└───────────────────────────────┼────────────────────────────────────┘
                                │ Petición autorizada
                                ▼
┌────────────────────────────────────────────────────────────────────┐
│             CAPA DE PRESENTACIÓN — CONTROLLERS                      │
│  AuthController · UsuarioController · ProductoController           │
│  CategoriaController · MarcaController · VarianteProductoController│
│  FotoController · DescuentoController                              │
│  CarritoController  (extends AuthenticatedController)              │
│  OrdenController    (extends AuthenticatedController)              │
│                                                                     │
│  · Mapeo HTTP ↔ lógica de negocio (sin lógica propia)             │
│  · Validación de ownership en runtime                              │
│  · Conversión entidad → DTO (manual, sin MapStruct)                │
└───────────────────────────────┬────────────────────────────────────┘
                                │
                                ▼
┌────────────────────────────────────────────────────────────────────┐
│           CAPA DE LÓGICA DE NEGOCIO — SERVICES                     │
│  11 interfaces Service + 11 implementaciones ServiceImpl           │
│                                                                     │
│  · Toda la lógica de dominio y reglas de negocio                  │
│  · Orquestación entre repositorios (ej. checkout)                 │
│  · Lanza BusinessRuleException / ResourceNotFoundException         │
│  · Cálculo de descuentos · control de stock · snapshots de precio  │
└───────────────────────────────┬────────────────────────────────────┘
                                │
                                ▼
┌────────────────────────────────────────────────────────────────────┐
│          CAPA DE PERSISTENCIA — REPOSITORIES                        │
│  11 interfaces JpaRepository (Spring Data JPA)                     │
│                                                                     │
│  · CRUD automático + queries JPQL custom                           │
│  · LEFT JOIN FETCH para evitar problema N+1                        │
│  · Filtros por estado, fecha, usuario                              │
└───────────────────────────────┬────────────────────────────────────┘
                                │
                                ▼
┌────────────────────────────────────────────────────────────────────┐
│                       BASE DE DATOS                                 │
│   MySQL 8.4 (Docker)              H2 In-Memory (Tests)             │
│   trekking_ecommerce              testdb — MODE=MySQL              │
│   ddl-auto = update               ddl-auto = create-drop           │
└────────────────────────────────────────────────────────────────────┘
```

---

### Security Filter Chain en Detalle

**`SecurityBeansConfig.java`** define las reglas generales:

- `SessionCreationPolicy.STATELESS` — sin sesión HTTP; cada petición es autónoma y carga su propia identidad desde el JWT
- CSRF deshabilitado — correcto para APIs REST stateless con JWT (CSRF solo es necesario cuando hay cookies de sesión)
- CORS abierto (`allowedOriginPatterns=*`) — **restringir a dominios reales antes de producción**
- `DaoAuthenticationProvider` — conecta `UserDetailsServiceImpl` + `BCryptPasswordEncoder` para el login

**`JwtAuthenticationFilter`** corre antes de `UsernamePasswordAuthenticationFilter`:

- Si no hay header `Authorization`, deja pasar la petición sin autenticar (los endpoints públicos la aceptarán; los privados la rechazarán en la siguiente etapa)
- Excepciones `ExpiredJwtException`, `MalformedJwtException`, etc. se loguean en DEBUG y la petición continúa sin autenticación (no devuelve 401 directamente)

**`AuthorizationFilter`** evalúa las reglas en orden declarativo — la primera regla que coincide gana.

**`AuthenticatedController`** es la segunda línea de defensa a nivel de negocio:

- `esAdmin()`: checks `ROLE_ADMIN` en SecurityContext
- `getUsuarioAutenticado()`: carga el `Usuario` completo desde DB usando el username del SecurityContext
- `validarPropietario(Long propietarioId)`: si no es admin y el ID no coincide → `AccessDeniedException` → `403`

---

### Diagrama Mermaid.js — Arquitectura Completa

> Cómo renderizar este diagrama:
> - **GitHub / GitLab:** se renderiza automáticamente al ver el archivo en el repositorio
> - **VS Code:** instalar la extensión *Markdown Preview Mermaid Support*
> - **Online:** copiar el bloque en [mermaid.live](https://mermaid.live)
> - **Notion / Confluence:** bloque de código de tipo `mermaid`

```mermaid
graph TB
    subgraph CLIENT["Cliente HTTP"]
        HTTP["Browser / Mobile / Postman\nHTTP Request\nAuthorization: Bearer JWT"]
    end

    subgraph CHAIN["Spring Security Filter Chain"]
        direction TB
        CORS_F["CorsFilter\nallowedOriginPatterns=*\ncredentials=true"]
        JWT_F["JwtAuthenticationFilter\nOncePerRequestFilter\n① Lee Bearer token\n② JwtUtil.extractUsername()\n③ UserDetailsServiceImpl → DB\n④ isTokenValid()\n⑤ SecurityContextHolder.set()"]
        AUTH_F["AuthorizationFilter\nPúblico → /api/auth/**, Swagger\n         GET catálogo\nAutenticado → /api/carritos/**\n             /api/ordenes/**\nADMIN → POST/PUT/DELETE catálogo\n        /api/usuarios/**\n        /api/descuentos/**"]
    end

    subgraph SECBEANS["Security Beans"]
        JWTUTIL["JwtUtil\ngenerateToken()\nextractUsername()\nisTokenValid()"]
        UDS["UserDetailsServiceImpl\nloadUserByUsername()\nmapea RolUsuario → ROLE_*"]
        BCRYPT["BCryptPasswordEncoder"]
        DAO["DaoAuthenticationProvider\n(usado en login manual)"]
    end

    subgraph CTRL["Capa de Presentación — Controllers"]
        AUTHC["AuthController\n/api/auth"]
        AUTHBASE["AuthenticatedController\nesAdmin()\ngetUsuarioAutenticado()\nvalidarPropietario()"]
        USRC["UsuarioController\n/api/usuarios"]
        PRODC["ProductoController\n/api/productos"]
        CATC["CategoriaController\n/api/categorias"]
        MARC["MarcaController\n/api/marcas"]
        VARC["VarianteProductoController\n/api/variantes"]
        FOTC["FotoController\n/api/fotos"]
        CARRC["CarritoController\n/api/carritos\nextends AuthenticatedController"]
        ORDC["OrdenController\n/api/ordenes\nextends AuthenticatedController"]
        DESCC["DescuentoController\n/api/descuentos"]
    end

    subgraph SVC["Capa de Lógica de Negocio — Services"]
        USRS["UsuarioService"]
        PRODS["ProductoService"]
        CATS["CategoriaService"]
        MARS["MarcaService"]
        VARS["VarianteProductoService\ntieneStock()\ndescontarStock()"]
        FOTS["FotoService\nmultipart → BLOB"]
        CARRS["CarritoService\nagregarItem()\ncalcularTotal()\nrealizarCompra() ← checkout"]
        ORDS["OrdenService\nconfirmar()\ncancelar()\nrestaurarStock()"]
        DESCS["DescuentoService\ncalcularDescuento()\nestaVigente()"]
        ICAS["ItemCarritoService"]
        IOAS["ItemOrdenService"]
    end

    subgraph JOBS["Scheduled Jobs"]
        CJOB["CarritoJob\n@Scheduled\nMarca ABANDONADO\ncarritos inactivos"]
        DJOB["DescuentoJob\n@Scheduled\nMarca EXPIRADO\ndescuentos vencidos"]
    end

    subgraph ERR["Manejo de Errores"]
        GEH["GlobalExceptionHandler\n@RestControllerAdvice\n404 ResourceNotFoundException\n422 BusinessRuleException\n400 ValidationException\n403 AccessDeniedException"]
    end

    subgraph REPO["Capa de Persistencia — Repositories"]
        USRR["UsuarioRepository\nfindByUsername\nfindByEmail"]
        PRODR["ProductoRepository\nfindByCategoria/Marca/Estado"]
        CATR["CategoriaRepository"]
        MARR["MarcaRepository"]
        VARR["VarianteProductoRepository\nfindByProductoId"]
        FOTR["FotoRepository\nfindByVarianteId"]
        CARRR["CarritoRepository\nfindByUsuarioIdConItems FETCH\nfindByEstadoAndFecha..."]
        ORDR["OrdenRepository\nfindByUsuarioIdConItems FETCH\nfindAllConItems FETCH"]
        DESCR["DescuentoRepository\nfindByEstado\nfindByEstadoAndFechaFinBefore"]
        ICAR["ItemCarritoRepository\nfindByCarritoIdAndVarianteId\nexistsByVarianteId"]
        IOAR["ItemOrdenRepository\nfindByOrdenId\nexistsByVarianteId"]
    end

    subgraph DB["Base de Datos"]
        MYSQL[("MySQL 8.4 · Docker\ntrekking_ecommerce\n\nusuario · producto\ncategoria · marca\nvariante_producto · foto\ncarrito · item_carrito\norden · item_orden\ndescuento")]
        H2[("H2 In-Memory\nTests\nMODE=MySQL\ncreate-drop")]
    end

    HTTP --> CORS_F --> JWT_F --> AUTH_F --> CTRL

    JWT_F -.->|usa| JWTUTIL
    JWT_F -.->|carga usuario| UDS
    UDS -.->|findByUsername| USRR
    DAO -.->|usa| UDS
    DAO -.->|verifica hash| BCRYPT
    AUTHC -.->|login manual| DAO
    AUTHC -.->|genera token| JWTUTIL

    CARRC -.->|hereda| AUTHBASE
    ORDC -.->|hereda| AUTHBASE
    AUTHBASE -.->|getUsuarioAutenticado| USRS

    AUTHC --> USRS
    USRC --> USRS
    PRODC --> PRODS
    CATC --> CATS
    MARC --> MARS
    VARC --> VARS
    FOTC --> FOTS
    CARRC --> CARRS
    CARRC --> ORDS
    ORDC --> ORDS
    DESCC --> DESCS

    CARRS -.->|verifica stock| VARS
    CARRS -.->|aplica descuento| DESCS
    ORDS -.->|restaura stock| VARS

    USRS --> USRR
    PRODS --> PRODR
    CATS --> CATR
    MARS --> MARR
    VARS --> VARR
    FOTS --> FOTR
    CARRS --> CARRR
    CARRS --> ICAR
    ORDS --> ORDR
    ORDS --> IOAR
    DESCS --> DESCR
    ICAS --> ICAR
    IOAS --> IOAR

    CJOB -.->|@Scheduled| CARRR
    DJOB -.->|@Scheduled| DESCR

    CTRL -.->|excepciones| GEH

    USRR & PRODR & CATR & MARR & VARR --> MYSQL
    FOTR & CARRR & ICAR & ORDR & IOAR & DESCR --> MYSQL
    USRR -.->|tests| H2
    CARRR -.->|tests| H2

    classDef chain fill:#c0392b,color:#fff,stroke:#922b21
    classDef secbeans fill:#e74c3c,color:#fff,stroke:#c0392b
    classDef ctrl fill:#16a085,color:#fff,stroke:#0e6655
    classDef svc fill:#2980b9,color:#fff,stroke:#1a5276
    classDef repo fill:#27ae60,color:#fff,stroke:#1e8449
    classDef db fill:#d68910,color:#fff,stroke:#b7770d
    classDef job fill:#7d3c98,color:#fff,stroke:#6c3483
    classDef err fill:#ca6f1e,color:#fff,stroke:#a04000

    class CORS_F,JWT_F,AUTH_F chain
    class JWTUTIL,UDS,BCRYPT,DAO secbeans
    class AUTHC,AUTHBASE,USRC,PRODC,CATC,MARC,VARC,FOTC,CARRC,ORDC,DESCC ctrl
    class USRS,PRODS,CATS,MARS,VARS,FOTS,CARRS,ORDS,DESCS,ICAS,IOAS svc
    class USRR,PRODR,CATR,MARR,VARR,FOTR,CARRR,ORDR,DESCR,ICAR,IOAR repo
    class MYSQL,H2 db
    class CJOB,DJOB job
    class GEH err
```

---

## Sección 5 — Guía de Ejecución Local

> Esta guía está diseñada para que un **desarrollador nuevo pueda levantar el proyecto desde cero en menos de 15 minutos** siguiendo los pasos en orden, sin necesitar ayuda de otro integrante del equipo.

### Prerrequisitos

Antes de comenzar, asegúrate de tener instaladas estas herramientas:

| Herramienta | Versión requerida | Cómo verificar |
|---|---|---|
| **JDK** | **17 LTS** (Temurin · Corretto · Zulu) | `java -version` |
| **Maven** | El wrapper incluye 3.9.14 — no necesitas instalar Maven | `./mvnw -version` |
| **Docker Desktop** | 24.x o superior | `docker -v` |
| **Docker Compose** | v2 (`docker compose` sin guión) | `docker compose version` |
| **Git** | Cualquier versión moderna | `git --version` |

> **Importante:** Usa siempre `./mvnw` (o `mvnw.cmd` en Windows) en lugar de `mvn` directamente. El Maven Wrapper garantiza que todos usen exactamente la misma versión de Maven (3.9.14) sin importar qué tengan instalado localmente.

---

### Paso 1 — Clonar el repositorio

```bash
git clone https://github.com/latiny0206-oss/TP-APIS-2026.git
cd TP-APIS-2026/ecommerce/ecommerce
```

---

### Paso 2 — Verificar el JDK 17

```bash
# Verificar versión activa
java -version
# Salida esperada: openjdk version "17.x.x" (cualquier distribución)

# Verificar que Maven también usa JDK 17
./mvnw -version
# La línea "Java version" debe mostrar 17.x.x
```

**Si el JDK activo NO es 17**, corregir `JAVA_HOME`:

```bash
# Linux / macOS
export JAVA_HOME=/path/to/jdk-17
export PATH=$JAVA_HOME/bin:$PATH

# Windows PowerShell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.x.x"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
```

---

### Paso 3 — Configurar las variables de entorno

```bash
# Copiar la plantilla de variables de entorno
cp .env.example .env      # Linux / macOS
copy .env.example .env    # Windows CMD
```

Abrir `.env` y completar los valores:

```properties
DB_USERNAME=root
DB_PASSWORD=12345
JWT_SECRET=una_clave_secreta_larga_minimo_32_caracteres_aleatoria
```

> **Seguridad:** El archivo `.env` está en `.gitignore`. **Nunca lo commitees al repositorio.** Para cambiar el `JWT_SECRET` en producción, usar una cadena aleatoria de al menos 256 bits.

---

### Paso 4 — Levantar la base de datos MySQL con Docker

```bash
# Iniciar el contenedor de MySQL en background
docker compose up -d

# Esperar a que el contenedor esté HEALTHY antes de continuar
docker compose ps
# La columna STATUS debe mostrar "healthy"

# Si tarda en iniciar, ver los logs:
docker compose logs -f mysql
```

El `docker-compose.yml` configura automáticamente:
- Contenedor `trekking-ecommerce-mysql` en `localhost:3306`
- Base de datos `trekking_ecommerce` creada automáticamente
- Usuario `root` con password `12345`
- Volumen persistente `mysql_data` (los datos sobreviven reinicios del contenedor)

---

### Paso 5 — Seleccionar perfil y ejecutar la aplicación

Hay tres perfiles disponibles. **Para desarrollo local con datos persistentes, usar `dev`:**

| Perfil | Datasource | Cuándo usar |
|---|---|---|
| `local` (default) | H2 in-memory | Inicio rápido sin Docker — datos se pierden al reiniciar |
| `dev` | MySQL local `localhost:3306` | **Recomendado para desarrollo** — datos persistentes |
| `mysql` | MySQL configurable por env vars | CI/CD, contenedores, entornos remotos |

```bash
# Opción A — Maven Wrapper con perfil dev (RECOMENDADA)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Opción B — Compilar JAR y ejecutarlo directamente
./mvnw clean package -DskipTests
java -jar target/ecommerce-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev

# Opción C — Perfil mysql con variables de entorno (para CI/CD)
DB_HOST=localhost DB_PORT=3306 DB_NAME=trekking_ecommerce \
DB_USERNAME=root DB_PASSWORD=12345 \
./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
```

---

### Paso 6 — Verificar que la aplicación inició correctamente

```bash
# En la consola de Spring Boot debe aparecer:
# Started EcommerceApplication in X.XXX seconds (JVM running for X.XXX)
# Tomcat started on port(s): 8080

# Verificar con curl
curl http://localhost:8080/api-docs

# O abrir en el browser:
# http://localhost:8080/swagger-ui.html
```

---

### Paso 7 — Bootstrap: crear el primer usuario ADMIN

> El endpoint público de registro solo crea usuarios con rol `CLIENTE`. El primer administrador debe insertarse manualmente en la base de datos.

```bash
# Conectarse al contenedor MySQL
docker exec -it trekking-ecommerce-mysql mysql -uroot -p12345 trekking_ecommerce
```

```sql
-- Insertar usuario ADMIN
-- La password "admin123" ya está hasheada con BCrypt a continuación
INSERT INTO usuario (username, email, password, nombre, apellido, rol, estado)
VALUES (
    'admin',
    'admin@trekking.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh9i',
    'Admin', 'Sistema',
    'ADMIN', 'ACTIVO'
);
```

Luego obtener el token JWT para usar en Swagger o en los tests:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'

# Respuesta esperada:
# {"token":"eyJhbGci...","username":"admin","rol":"ADMIN"}
```

---

### Paso 8 — Ejecutar la suite de tests

Los tests usan H2 in-memory con el perfil `test`. **No requieren Docker ni MySQL activo.**

```bash
# Suite completa
./mvnw test

# Test específico por clase
./mvnw test -Dtest=EndpointFlujosIntegrationTest

# Solo tests de seguridad
./mvnw test -Dtest=EndpointSeguridadAutorizacionIntegrationTest

# Generar reporte de cobertura JaCoCo
./mvnw verify
# Reporte disponible en: target/site/jacoco/index.html
```

---

### Variables de Entorno Disponibles

| Variable | Valor por defecto | Perfil activo | Descripción |
|---|---|---|---|
| `SERVER_PORT` | `8080` | Todos | Puerto del servidor HTTP |
| `JWT_SECRET` | `ClaveSecretaTrekkingEcommerce2024MuyLarga` | Todos | Clave HMAC-SHA para firmar tokens JWT |
| `DB_HOST` | `localhost` | `mysql` | Host del servidor MySQL |
| `DB_PORT` | `3306` | `mysql` | Puerto del servidor MySQL |
| `DB_NAME` | `trekking_ecommerce` | `mysql` | Nombre de la base de datos |
| `DB_USERNAME` | `root` | `mysql` | Usuario de MySQL |
| `DB_PASSWORD` | `12345` | `mysql` | Contraseña de MySQL |

### Comandos Útiles de Docker Compose

```bash
# Iniciar MySQL en background
docker compose up -d

# Ver logs en tiempo real
docker compose logs -f mysql

# Detener (conserva datos en el volumen)
docker compose down

# Detener y ELIMINAR todos los datos (reset completo)
docker compose down -v

# Estado y salud del contenedor
docker compose ps
```

---

## Sección 6 — QA, Automatización y Workflow

> Esta sección está dirigida principalmente a Nacho, Eli, Franco y los Referentes de QA. Aquí se documenta el estándar de calidad del equipo: cómo se escriben los tests, cómo se reportan, y cómo se organiza el trabajo.

---

### Estándar de Reportes Allure

El proyecto utiliza **Allure Framework** para generar reportes de test enriquecidos. Estos reportes permiten ver los pasos de cada test, capturas de error, historial de ejecución y métricas de calidad.

#### Regla fundamental: Steps en código, NO en anotaciones

> **PROHIBIDO** el uso de `@Step` como anotación en métodos helper para documentar pasos de test. Los pasos de Allure deben implementarse **siempre con lambdas dentro del método de test**, usando `Allure.step()`.

**¿Por qué esta restricción?** Los métodos anotados con `@Step` producen reportes donde los pasos están separados del cuerpo del test, lo cual dificulta la lectura del flujo y genera una falsa impresión de que los métodos helper son "pasos" del negocio cuando en realidad son utilidades de setup. El equipo ha decidido que todos los pasos visibles en el reporte deben estar explícitamente dentro del test, para que el reporte sea una descripción fiel de lo que el test está verificando.

---

#### Ejemplo correcto — `Allure.step()` con lambdas dentro del test

```java
import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Test;

@Feature("Carrito de Compras")
@Story("Checkout")
class CarritoCheckoutTest {

    @Test
    @Description("Verificar que el checkout convierte el carrito en una orden y descuenta el stock")
    void deberiaRealizarCheckoutExitosamente() {

        Allure.step("DADO: un usuario autenticado con un carrito activo", () -> {
            // Arrange: crear usuario, autenticar, crear carrito
            usuario = crearUsuarioCliente();
            token = obtenerToken(usuario.getUsername(), "password123");
            carrito = crearCarritoParaUsuario(usuario.getId());
        });

        Allure.step("Y: una variante con stock suficiente agregada al carrito", () -> {
            // Arrange: crear producto, variante con stock=5, agregar al carrito
            variante = crearVarianteConStock(5);
            agregarItemAlCarrito(carrito.getId(), variante.getId(), 2);
        });

        Allure.step("CUANDO: el usuario hace checkout del carrito", () -> {
            // Act: llamar al endpoint de checkout
            response = mockMvc.perform(
                post("/api/carritos/{id}/checkout", carrito.getId())
                    .header("Authorization", "Bearer " + token)
            ).andReturn();
        });

        Allure.step("ENTONCES: se crea una orden en estado PENDIENTE", () -> {
            // Assert: verificar respuesta HTTP 200
            assertThat(response.getResponse().getStatus()).isEqualTo(200);
            OrdenResponse orden = parseResponse(response, OrdenResponse.class);
            assertThat(orden.getEstado()).isEqualTo("PENDIENTE");
        });

        Allure.step("Y: el precio_al_momento refleja el precio de la variante en el momento del checkout", () -> {
            // Assert: verificar price snapshot
            OrdenResponse orden = parseResponse(response, OrdenResponse.class);
            assertThat(orden.getItems().get(0).getPrecioAlMomento())
                .isEqualByComparingTo(variante.getPrecio());
        });

        Allure.step("Y: el stock de la variante se decrementó en 2 unidades", () -> {
            // Assert: verificar stock
            VarianteProducto varianteActualizada = varianteRepository.findById(variante.getId()).orElseThrow();
            assertThat(varianteActualizada.getStock()).isEqualTo(3); // 5 - 2
        });
    }
}
```

---

#### Ejemplo INCORRECTO — No hacer esto

```java
// ❌ PROHIBIDO: usar @Step en métodos helper
@Step("Crear carrito para el usuario {usuarioId}")
private CarritoResponse crearCarritoHelper(Long usuarioId) {
    // Este paso aparece en el reporte pero no describe QUÉ está verificando el test
    return carritoService.crear(usuarioId);
}

// ❌ PROHIBIDO: los steps quedan fuera del cuerpo del test
@Test
void testCheckout() {
    CarritoResponse carrito = crearCarritoHelper(1L); // el step Allure está oculto
    // ...
}
```

---

#### Anotaciones de clasificación recomendadas

Usar siempre estas anotaciones a nivel de clase o de método para que los reportes queden bien organizados:

```java
@Feature("Nombre del módulo funcional")   // ej: "Carrito de Compras", "Autenticación"
@Story("Historia de usuario o flujo")      // ej: "Checkout", "Login con token inválido"
@Description("Descripción del test")       // qué verifica este test en el negocio
@Severity(SeverityLevel.CRITICAL)         // BLOCKER | CRITICAL | NORMAL | MINOR | TRIVIAL
```

---

#### Estructura del reporte Allure

Cada test en el reporte debe tener:

1. **Nombre legible** (el nombre del método, en español o inglés, que describa qué se verifica)
2. **Feature / Story** (clasificación del módulo y la historia)
3. **Descripción** (una frase que explique el caso de prueba)
4. **Pasos** (usando `Allure.step()`) en formato DADO / CUANDO / ENTONCES (BDD)
5. **Severidad** (según el impacto del fallo en el negocio)

---

#### Ejecutar y generar el reporte

```bash
# Ejecutar tests y guardar resultados Allure
./mvnw test -Dallure.results.directory=target/allure-results

# Generar el reporte HTML (requiere Allure CLI instalado)
allure generate target/allure-results --clean -o target/allure-report

# Abrir el reporte en el browser
allure open target/allure-report

# Servidor de reporte en modo watch (útil durante desarrollo)
allure serve target/allure-results
```

---

### Conventional Commits

El equipo usa **Conventional Commits** para mantener un historial de git limpio, generar changelogs automáticos y facilitar la revisión de PRs.

#### Formato

```
<tipo>(<alcance opcional>): <descripción breve en imperativo>

[cuerpo opcional — explicación detallada del cambio]

[footer opcional — breaking changes, referencias a tickets]
```

#### Tipos permitidos

| Tipo | Cuándo usarlo |
|---|---|
| `feat` | Nueva funcionalidad para el usuario |
| `fix` | Corrección de un bug |
| `test` | Agrega o corrige tests |
| `refactor` | Refactorización sin cambio de comportamiento |
| `docs` | Solo cambios en documentación |
| `chore` | Tareas de mantenimiento (deps, config, build) |
| `style` | Cambios de formato, whitespace, etc. (sin lógica) |
| `perf` | Mejoras de rendimiento |
| `ci` | Cambios en la configuración de CI/CD |

#### Ejemplos de commits válidos

```
feat(carrito): implementar endpoint de checkout con price snapshot

fix(auth): corregir validación de token expirado en JwtAuthenticationFilter

test(orden): agregar tests de integración para cancelación con restauración de stock

refactor(descuento): extraer lógica de cálculo a DescuentoCalculadora

docs: actualizar GUIA_DEFINITIVA.MD con sección de QA y Allure

chore(deps): actualizar springdoc-openapi a 2.8.6

feat(usuario)!: cambiar campo 'nombre' a 'nombre_completo' en tabla usuario

BREAKING CHANGE: el campo 'nombre' fue renombrado; requiere migración de datos
```

> **Regla:** Los commits con `!` después del tipo (ej: `feat!`) o con `BREAKING CHANGE:` en el footer indican cambios incompatibles con versiones anteriores. Siempre documentar el motivo y el plan de migración.

---

### Flujo Gitflow / Jira

#### Ramas principales

| Rama | Propósito | ¿Quién puede commitear directo? |
|---|---|---|
| `main` | Código en producción — siempre estable | Nadie — solo vía PR aprobado |
| `develop` | Integración de features en desarrollo | Nadie — solo vía PR aprobado |

#### Tipos de ramas de trabajo

| Prefijo | Propósito | Ejemplo |
|---|---|---|
| `feature/` | Nueva funcionalidad | `feature/checkout-con-descuento` |
| `fix/` | Corrección de bug | `fix/stock-no-restaura-al-cancelar` |
| `test/` | Agregar cobertura de tests | `test/integracion-endpoint-fotos` |
| `refactor/` | Mejora interna sin cambio de comportamiento | `refactor/extraer-validacion-carrito` |
| `docs/` | Documentación | `docs/actualizar-guia-definitiva` |
| `chore/` | Mantenimiento | `chore/actualizar-dependencias-abril` |

#### Flujo de trabajo

```
1. Crear rama desde develop:
   git checkout develop && git pull origin develop
   git checkout -b feature/nombre-descriptivo

2. Desarrollar con commits frecuentes siguiendo Conventional Commits

3. Pushear y abrir Pull Request hacia develop:
   git push origin feature/nombre-descriptivo
   → Abrir PR en GitHub con descripción clara

4. El PR debe:
   - Pasar todos los tests automáticos (CI)
   - Tener al menos 1 aprobación de otro integrante
   - Resolver todos los comentarios de code review

5. Merge a develop (squash merge recomendado para features pequeñas)

6. Cuando develop está listo para release → PR de develop a main
```

#### Vinculación con Jira

Incluir el ID del ticket de Jira en el nombre de la rama y/o en el cuerpo del commit:

```
feat(carrito): implementar endpoint de vaciar carrito

Closes TREK-142
Related to TREK-130 (checkout flow)
```

---

### Alineación con SWEBOK

El proyecto sigue los lineamientos del **SWEBOK (Software Engineering Body of Knowledge)** en las siguientes áreas:

#### Área: Pruebas de Software (Software Testing)

- **Pruebas unitarias** — `CarritoServiceImplTest`, `OrdenServiceImplTest`, etc.: cada clase de servicio tiene su suite de tests que verifica la lógica de negocio de forma aislada (con mocks de repositorios).
- **Pruebas de integración** — `EndpointFlujosIntegrationTest`, `GlobalExceptionHandlerIT`, etc.: levantan el contexto completo de Spring Boot con H2 y verifican el comportamiento end-to-end del sistema.
- **Pruebas de seguridad** — `EndpointSeguridadAutorizacionIntegrationTest`: verifica las reglas de acceso (autenticación y autorización) para todos los endpoints.
- **Pruebas de valores límite y negativos** — `EndpointNegativosBordeIntegrationTest`: verifica el comportamiento del sistema ante entradas inválidas, estados inconsistentes y violaciones de reglas de negocio.

#### Área: Construcción de Software (Software Construction)

- **Principio de responsabilidad única:** cada capa (Controller, Service, Repository) tiene una responsabilidad claramente definida.
- **Principio de separación de preocupaciones:** la seguridad está en los filtros, la lógica de negocio en los servicios, la validación de entrada en los DTOs con `@Valid`.
- **Principio abierto/cerrado:** la interfaz `Service` permite agregar nuevas implementaciones sin modificar el código existente.

#### Área: Gestión de la Configuración (Configuration Management)

- **Control de versiones:** Git con flujo Gitflow.
- **Entornos diferenciados:** perfiles Spring (`local`, `dev`, `mysql`, `test`) para garantizar paridad entre entornos.
- **Secretos:** variables de entorno vía `.env` (en `.gitignore`); plantilla en `.env.example`.
- **Build reproducible:** Maven Wrapper fija la versión de Maven; `pom.xml` fija todas las versiones de dependencias.

#### Estándar de cobertura esperado

| Tipo de test | Cobertura mínima esperada |
|---|---|
| Servicios (`service/impl/`) | ≥ 80% de líneas |
| Controladores (vía integración) | Todos los happy paths + principales errores |
| Seguridad | 100% de los endpoints con reglas de acceso |
| Manejo de excepciones | Todos los tipos de excepción del `GlobalExceptionHandler` |

Verificar cobertura con:

```bash
./mvnw verify
# Abrir: target/site/jacoco/index.html
```

---

*Documento generado y mantenido como parte de los estándares de documentación técnica del equipo. Toda sugerencia de mejora debe canalizarse vía PR siguiendo el flujo de Conventional Commits descrito en la Sección 6.*

*Proyecto: `com.trekking:ecommerce` — Spring Boot 3.5.12 / Java 17 · Equipo: Grupo 13 · Materia: APIs 2026*
