# Konrad Gourmet · Contexto, estado y diagramas C4

> Documento de trabajo del equipo. Estado revisado el **8 de noviembre de 2026**, al cierre de la sesión 5 del plan de correcciones.
> Resume el proyecto, dónde está cada archivo, lo que se hizo en las cinco sesiones, el estado de cada hallazgo y todo lo relacionado con el modelo C4 del archivo `Arquitectura_Konrad_Gourmet.drawio`.
> La versión anterior de este documento (7 de noviembre) describía un proyecto con el código vacío, el reporte sin revisar y el Excel inconsistente. Nada de eso es ya cierto, salvo lo que dice la sección 2.

---

## Índice

1. [Contexto del proyecto](#1-contexto-del-proyecto)
2. [Estado actual](#2-estado-actual)
3. [Diagramas C4 del .drawio](#3-diagramas-c4-del-drawio)
4. [Qué se hizo, sesión por sesión](#4-qué-se-hizo-sesión-por-sesión)
5. [Estado de cada pendiente](#5-estado-de-cada-pendiente)
6. [Qué falta y quién lo hace](#6-qué-falta-y-quién-lo-hace)
7. [Glosario rápido](#7-glosario-rápido)

---

## 1. Contexto del proyecto

### 1.1 Datos generales

| | |
|---|---|
| **Curso** | Arquitectura de Software · Fundación Universitaria Konrad Lorenz · 2026-2 |
| **Profesor** | Carlos Andrés López |
| **Equipo** | Mauricio Iturriza, Isabella Castro, Carlos Franco, Adrián Corredor |
| **Proyecto** | Sistema de Abastecimiento Konrad Gourmet |

### 1.2 Qué hace el sistema

Konrad Gourmet es una cadena de restaurantes. El sistema cubre todo el ciclo de abastecimiento:

1. El **mesero** registra los pedidos por mesa, y eso descuenta el inventario.
2. El **jefe de cocina** gestiona los platos y el inventario. Cuando un producto baja del 25 %, recibe una alarma y crea una solicitud de alimentos.
3. El **auxiliar de compras** genera la solicitud de cotización, que puede reunir una o varias solicitudes (DEC-05). El sistema consulta el precio estándar del Ministerio de Comercio, genera un PDF y se lo envía al proveedor por correo certificado.
4. El **director de compras** registra los valores que respondió el proveedor y valida la cotización con las reglas de precio: **RECHAZADA** si está más de 25 % por encima, **SOSPECHOSA** si está más de 50 % por debajo y **OPCIONADA** en cualquier otro caso. Con las opcionadas genera la orden de compra.
5. El **director comercial** consulta el tablero BAM, con indicadores, tendencias y promociones sugeridas (DEC-06 y DEC-07).
6. El **administrador** gestiona los parámetros, la imagen corporativa, la auditoría y el alta de usuarios (DEC-10).

Requerimientos no funcionales principales:
- 200.000 usuarios concurrentes y 1.000 TPS.
- Disponibilidad del 99,7 % y recuperación ante desastres en un centro de datos alterno.
- Crecimiento del 200 % en archivos.
- Correo certificado con sello de tiempo y auditoría de cada operación de creación, lectura, edición o borrado (CRUD).
- Integración SOAP, en ambos sentidos.

### 1.3 Arquitectura elegida

| Estilo | Qué decide | Dónde se ve |
|---|---|---|
| **Microservicios** | 11 servicios desplegables, cada uno con su propia base de datos, detrás de un API Gateway | Mapa, páginas 3a y 5 |
| **Orientada a eventos** | Los servicios se avisan por Kafka sin esperarse, con 10 tópicos. Es publicar/suscribir, no el Observer de GoF (H9) | Mapa y páginas 3a y 6 |
| **Hexagonal** (puertos y adaptadores) | El dominio de cada servicio no depende de la base de datos, del SOAP, del SMTP ni del PDF | Mapa y páginas 3b y 4a–4e |

Apoyos:
- Cliente-servidor REST.
- SOA/SOAP en la frontera, a cargo de ms-integracion.
- OAuth2/JWT.
- API Gateway «Facade».
- Service Registry (Eureka).
- Balanceador L7.

**Los 11 microservicios** (el gateway tiene **13 prefijos**: ms-identidad y ms-cotizaciones tienen dos, DEC-12)

| Servicio | Responsabilidad | Ruta | Ventanas |
|---|---|---|---|
| ms-identidad | OAuth 2.0 / OIDC: usuarios, roles, menú por rol, alta de usuarios; emite el JWT | `/api/auth/**` · `/api/usuarios/**` | V01 |
| ms-pedidos | Pedidos por mesa (de 4 a 20 réplicas en hora pico) | `/api/pedidos/**` | V02–V04 |
| ms-inventario | Existencias por sucursal, descuento diario a las 23:59, umbral del 25 % | `/api/inventario/**` | V05, V07 |
| ms-menu | Platos, recetas, categorías y productos | `/api/platos/**` | V06 |
| ms-solicitudes | Solicitudes de alimentos (PENDIENTE → EN TRÁMITE → COMPLETADA) | `/api/solicitudes/**` | V08–V12 |
| ms-cotizaciones | Cotización, PDF, registro de valores, cadena de reglas y proveedores | `/api/cotizaciones/**` · `/api/proveedores/**` | V13–V20 |
| ms-ordenes | Órdenes de compra desde cotizaciones OPCIONADAS, y su PDF | `/api/ordenes-compra/**` | V21–V23 |
| ms-integracion | Consume el SOAP del Ministerio y expone servicios SOAP | `/api/integracion/**` (SOAP) | apoya V13 |
| ms-notificaciones | Correos certificados y alarmas en la app | `/api/notificaciones/**` | V05 (alarma) |
| ms-administracion | Parámetros, imagen corporativa, auditoría y logs | `/api/admin/**` | sin mockup |
| ms-analitica | Tablero BAM, tendencias y promociones sugeridas | `/api/bam/**` | sin mockup |

**Los 10 tópicos de Kafka** (con los consumidores nuevos de las sesiones 2 a 4 en cursiva)

| Tópico | Lo publica | Lo consumen |
|---|---|---|
| `menu.actualizado` | ms-menu | ms-pedidos, *ms-inventario* |
| `pedido.registrado` | ms-pedidos | ms-inventario, ms-analitica |
| `inventario.stock-bajo` | ms-inventario | ms-notificaciones, ms-analitica |
| `solicitud.registrada` | ms-solicitudes | ms-notificaciones, ms-analitica, *ms-inventario* |
| `solicitud.en-tramite` | ms-solicitudes | ms-notificaciones |
| `cotizacion.enviada` | ms-cotizaciones | ms-notificaciones, ms-solicitudes |
| `cotizacion.validada` | ms-cotizaciones | ms-analitica |
| `orden.generada` | ms-ordenes | ms-solicitudes, ms-notificaciones, ms-analitica, *ms-cotizaciones* |
| `tendencia.detectada` | ms-analitica | ms-notificaciones |
| `auditoria.evento` | los otros 10 servicios (ms-administracion escribe directo en su bitácora) | ms-administracion |

### 1.4 Dónde está cada cosa

Desde el 7 de noviembre (DEC-01) hay **dos repositorios git locales**, uno al lado del otro, y un tercero público que los junta.

**`FinalProyecto/documentos/`** (repo git, rama `correcciones-revision`, sin remoto):

| Archivo | Qué es |
|---|---|
| `Arquitectura_Konrad_Gourmet.drawio` | Los diagramas: **11 páginas**, con una celda por línea para poder revertir cambios sueltos |
| `Arquitectura_Konrad_GourmetOriginalSinCorregir.drawio` | Copia intacta del original, con 8 páginas. **No modificar** |
| `Reporte_Arquitectura_Konrad_Gourmet.pdf` | Reporte de arquitectura (38 págs.). **No se modifica** hasta que aparezca el script (DEC-03) |
| `Copia de ARQUITECTURA-Supuestos de Capacidad  del equipo.xlsx` | Capacidad del equipo y sprints, ya corregido (DEC-47) |
| `DISEÑO UXUI.pdf`, `flujo-web-konrad-gourmet (1).pdf` | Mockups de las 23 ventanas (Canva) y flujo web. No se modifican |
| `PLAN_CORRECCIONES.md` | El plan de las 5 sesiones y su tabla de avance |
| `DECISIONES.md` | DEC-01 a DEC-50 con su contexto y todas las opciones, más las preguntas abiertas A1–A3 |
| `REVISION_2026-11-07.md` | Los hallazgos H1–H26 del arquitecto-revisor |
| `INVENTARIO_SESION2.md`, `INVENTARIO_SESION3.md` | Lista de ítems que cada sesión debía cambiar |
| `VALIDACION_FASE2A.md`, `VALIDACION_SESION2.md`, `VALIDACION_SESION3.md`, `VALIDACION_SESION4.md` | Lo que el revisor encontró al validar cada sesión |
| `CORRECCIONES_REPORTE.md` | Las 59 correcciones (R-01 a R-59) que hay que aplicar al reporte |
| `CORRECCIONES_HU_Y_MOCKUPS.md` | Lo que hay que cambiar en el Story Mapping y los mockups |
| `Casos de uso — Konrad Gourmet.md` | Los 18 casos de uso, escritos por un compañero de Mauricio; se incluyó sin modificar |
| `REVISION_CASOS_DE_USO.md` | La revisión del arquitecto sobre ese archivo: bien con correcciones, 13 problemas |
| `ESTADO_PROYECTO_Y_C4.md` | Este documento |

**`FinalProyecto/KonradGourmet/`** (repo git, rama `correcciones-revision`; el push al remoto de `iCastro117` está desactivado a propósito):

| Carpeta o archivo | Qué es |
|---|---|
| `src/` | 191 clases del modelo en 12 paquetes de servicio, más el paquete `demo` (20 archivos) |
| `pruebas/` | 171 verificaciones, sin JUnit (DEC-44) |
| `ejecutar.sh` | Compila, ejecuta la demo y corre las pruebas. Solo necesita un JDK 21 |
| `README.md` | Capas, módulos, patrones, qué métodos tienen lógica y los límites conocidos |

**`FinalProyecto/exportes/`**: PNG de las páginas del `.drawio` a escala 1 (a escala mayor salen en blanco). Las 11 páginas actuales están en `sesion5/`.

**Repo público:** `github.com/MauItu/KonradGourmet-FinalProyecto`, rama `main`. Junta `documentos/` y `KonradGourmet/` (que allí se llama `codigo/`) con la historia de ambos, para que un compañero lo clone. Se actualiza solo cuando Mauricio lo pide: clon temporal, `git merge -X subtree=…` de cada rama y push por HTTPS. Última actualización: `195b4b2` (8-nov-2026).

**Fuera de `FinalProyecto/`, en `../proyecto/`:**

| Archivo | Qué es |
|---|---|
| `../proyecto/contexto/Visual Story Mapping.pdf` | Las **19 historias de usuario** (HU-01 a HU-19). La tarjeta de la HU-n está en la página 7+n y es una imagen. MVP 1: HU-01–05 · MVP 2: HU-06–11 · MVP 3: HU-12–15 · MVP 4: HU-16–19. No se modifica |
| `../proyecto/contexto/Clases/` | Material de clase; `General1.pdf` y `General4.pdf` explican el C4 y el diagrama dinámico |
| `../proyecto/exposicion1/principalesHU.md` | 4 historias en texto (HU-01, 10, 13 y 18) |

---

## 2. Estado actual

| Área | Estado | Detalle |
|---|---|---|
| Diseño de arquitectura | **Sólido** | Microservicios + eventos + hexagonal, justificados contra los requerimientos no funcionales. Trazabilidad de las 23 ventanas |
| Diagramas C4 y de clases | **Corregidos y validados** | 11 páginas. Las sesiones 1 a 3 aplicaron los hallazgos del revisor, validados cuatro veces; 191 clases en 4a–4e. La 4d y la nota `n_outbox` de la 4a se actualizaron en la sesión 5 |
| Código Java | **Con firmas y demo** | 191 clases con sus firmas y relaciones, que compilan con `javac --release 21 -Xlint:all`. Los 23 métodos de ventana existen con su comentario. Quedan 94 cuerpos «Pendiente de implementar»: lo que habla con tecnología externa y lo que la demo no usa |
| Demo | **Funciona** | `DemoArquitectura` recorre pedido, alarma de stock, SOL-031, COT-018/019/020 y OC-0012, y cubre las 7 salidas que promete el reporte (DEC-31). Reproduce +4,81 % → OPCIONADA. 171 verificaciones pasan |
| Reporte PDF | **Desactualizado, sin tocar** | Sigue siendo el de octubre: dice «ocho páginas», y «155 clases». Sus 59 correcciones están escritas en `CORRECCIONES_REPORTE.md`; no se aplican sin el script de ReportLab (A1) |
| Historias de usuario y mockups | **Con correcciones anotadas** | V13 y V21 muestran una sola solicitud, V17 y V19–V23 cuentan otra historia de cotizaciones que la demo (DEC-49), V05 trae dos alertas que no alertan (DEC-50), V03 y V04 se contradicen, HU-07 es copia de HU-06, y el Story Mapping tiene etiquetas erróneas. Los archivos no son nuestros: la lista está en `CORRECCIONES_HU_Y_MOCKUPS.md` |
| Planeación (Excel) | **Corregido** | 1 PHU = 25 h, 9 PHU por sprint, HU-14 = 1 (total 35), HU-17 en el sprint 3. **5 sprints, 10 semanas, 35 PHU y fechas del 03/08 al 11/10/2026**, todo con fórmulas (DEC-47) |
| Prototipo web | **1 de 23 pantallas** | Solo el login (V01), en HTML/CSS estático y sin conexión al backend. No estaba en el alcance de las correcciones |

**Riesgos que siguen abiertos**
- **Reporte:** es el entregable principal y hoy contradice al código y a los diagramas. Se resuelve con el script (A1) o rehaciéndolo.
- **Límites de la demo**, que el reporte debe decir (R-13, R-21, R-23 y R-45): los eventos llevan `sistema` como usuario y cada uno crea su propia correlación (DEC-42); los consumidores no descartan eventos repetidos; todos los datos están en memoria.
- **Supuestos por confirmar:** DEC-08, DEC-20, DEC-39, DEC-41, DEC-44, DEC-45, DEC-47 (el orden de filas) y DEC-48. DEC-49 y DEC-50 las tomó el arquitecto-revisor a pedido de Mauricio.

---

## 3. Diagramas C4 del .drawio

### 3.1 Qué es el modelo C4

El C4 describe una arquitectura con zoom progresivo. Cada nivel abre una caja del nivel anterior:

| Nivel | Pregunta que responde | Página en el `.drawio` |
|---|---|---|
| 1 · Contexto | ¿Quién usa el sistema y con qué sistemas externos habla? | **2** |
| 2 · Contenedores | ¿Qué piezas se despliegan por separado y cómo se comunican? | **3a** |
| 3 · Componentes | ¿Qué hay dentro de un contenedor? | **3b** (ms-cotizaciones) |
| 4 · Código | ¿Qué clases implementan cada componente y dónde va cada patrón? | **4a–4e** |
| Complemento · Despliegue | ¿Dónde y cómo se ejecuta? | **5** |
| Complemento · Dinámico | ¿En qué orden ocurren los mensajes en un caso real? | **6** |
| Complemento · Ecosistema | Varios sistemas de la empresa juntos | No se hizo: nunca se pidió y, con un solo sistema, repetiría el contexto |

**Por qué hacían falta las páginas 3a, 3b y 6:** el propio reporte (pág. 4) dice que *«el modelo C4 es obligatorio en sus cuatro niveles —contexto, contenedores, componentes y código—»* y que *«los diagramas dinámico y de despliegue son complementarios»*. El original tenía los niveles 1 y 4 y el despliegue; los niveles 2 y 3 estaban mezclados dentro del mapa integrado y el dinámico no existía.

### 3.2 Inventario de páginas

Cuentas del archivo actual (`9916ff9`), en el orden en que aparecen en draw.io.

| # | Página | id | Origen | Cajas / flechas |
|---|---|---|---|---|
| 1 | 1 · Mapa integrado | p1 | Original, corregida en la sesión 2 | 323 / 161 |
| 2 | 2 · C4 Contexto | p2 | Original, corregida en la sesión 2 | 24 / 14 |
| 3 | **3a · C4 Contenedores** | p9 | Nueva (6-nov), corregida en la sesión 2 | 61 / 50 |
| 4 | **3b · C4 Componentes · ms-cotizaciones** | p10 | Nueva (6-nov), corregida en la sesión 2 | 45 / 29 |
| 5 | 4a · Código · compartido e identidad | p3 | Original, corregida en la fase 2a y la sesión 5 | 62 / 37 · 28 clases |
| 6 | 4b · Código · pedidos, menú e inventario | p4 | Original, corregida en la fase 2a | 88 / 48 · 39 clases |
| 7 | 4c · Código · solicitudes y cotizaciones | p5 | Original, corregida en la fase 2a | 87 / 50 · 40 clases |
| 8 | 4d · Código · órdenes y notificaciones | p6 | Original, corregida en la fase 2a y la sesión 5 | 83 / 52 · 40 clases |
| 9 | 4e · Código · integración, administración y analítica | p7 | Original, corregida en la fase 2a y la sesión 3 | 89 / 53 · 44 clases |
| 10 | 5 · Despliegue | p8 | Original, corregida en la sesión 2 | 49 / 23 |
| 11 | **6 · C4 Dinámico · recorrido de compra** | p11 | Nueva (7-nov), corregida en la sesión 2 | 89 / 105 |

Las clases de 4a–4e suman **191**. Las ocho páginas originales ya **no** son idénticas a `…OriginalSinCorregir.drawio`: ese archivo es el punto de comparación para ver qué cambió.

### 3.3 Página 1 · Mapa integrado

Vista general que combina los tres estilos: personas por rol, la SPA, el API Gateway con sus 13 prefijos, los 11 microservicios dibujados como hexágonos con sus adaptadores, la banda ámbar de Kafka, las bases de datos y los sistemas externos.

Cambios de las sesiones 1 y 2:
- Cada caja verde agrupa los adaptadores de una misma tecnología y destino; el detalle por clase está en 4a–4e (DEC-15).
- Se dibujan el **almacén de objetos** y **ELK** (DEC-16) y la **BD PostgreSQL de ms-integracion** con su tabla outbox (DEC-17).
- Los errores E1 y E2 se corrigieron: la flecha a Redis dice `Redis · TLS` y la plataforma dice «3 zonas». Las flechas `JDBC` que quedan van a bases PostgreSQL.

### 3.4 Página 2 · C4 Contexto, nivel 1

El sistema aparece como una sola caja. Alrededor:
- **6 personas:** mesero, jefe de cocina, auxiliar de compras, director de compras, director comercial y administrador.
- **El proveedor**, persona externa, en gris claro (DEC-24).
- **4 sistemas externos:** Ministerio de Comercio (SOAP), servidor de correo certificado (SMTP con sello de tiempo), redes sociales (APIs públicas) y sistemas externos que consumen los servicios SOAP del restaurante.

Las flechas llevan su protocolo y hay leyenda (H22).

### 3.5 Página 3a · C4 Contenedores, nivel 2

**Qué muestra:**
- Las 6 personas y el proveedor, cada una con el color de su rol.
- La **SPA** (React + TypeScript) y el **servidor web** (Nginx detrás de la CDN y el WAF).
- El **API Gateway «Facade»** y el **Service Registry** (Eureka, 2 réplicas, latido cada 30 s). Las flechas de Eureka dicen `[HTTPS · mTLS]` (H17).
- Los **11 microservicios**, cada uno con su responsabilidad, sus prefijos `/api/…` y sus ventanas.
- Una base **PostgreSQL** por servicio, incluida la de ms-integracion (con la tabla outbox), la **caché de precios** (Redis), el **almacén de objetos** y los **logs en ELK**.
- El **broker Kafka** (3 brokers, factor de réplica 3, retención de 7 días, cola de errores), con cada flecha de evento rotulada `[Kafka/TLS]`.
- Las dos llamadas síncronas entre servicios: ms-ordenes → ms-cotizaciones (la cotización OPCIONADA) y ms-cotizaciones → ms-integracion (el precio sugerido).
- Los sistemas externos, con el protocolo en cada flecha, y una leyenda con la nota «Qué se simplificó».

**Decisiones tomadas al dibujarla:**

| Decisión | Por qué |
|---|---|
| Se agregó el **servidor web Nginx** como contenedor | El reporte dice que la SPA la sirve Nginx detrás de una CDN; en C4, lo que se despliega por separado es un contenedor |
| El **almacén de objetos** se conectó a ms-cotizaciones, ms-ordenes y ms-notificaciones | Los tres guardan o leen PDF (D1, H4, N4) |
| La flecha a **Redis** dice `Redis · TLS` y no «JDBC» | JDBC es solo para bases relacionales |
| El gateway llega a los servicios con **una sola flecha** que dice «Enruta los 13 prefijos» | Así lo hace el mapa; cada servicio indica sus prefijos |
| **No se dibujó el balanceador** ni las réplicas | Son infraestructura y están en la página 5 |

### 3.6 Página 3b · C4 Componentes de ms-cotizaciones, nivel 3

**Por qué ms-cotizaciones:** atiende el flujo de V13 a V20, que es el ejemplo central del reporte, y concentra tres patrones: **Facade**, **Chain of Responsibility** y **Adapter**.

**Qué muestra:**
- **Adaptadores de entrada:** `CotizacionRestController` (con `detalle()` y el reenvío, H5 y H18) y `ProveedorRestController` (`/api/proveedores/**`, HU-15), y el listener de `orden.generada`.
- **Núcleo hexagonal** (no importa Spring, JPA, Kafka ni HTTP):
  - **Casos de uso:** `CotizacionService` y `GenerarCotizacionFacade «Facade»`, que resuelve el clic de V13 con una o varias solicitudes.
  - **Reglas «Chain of Responsibility»:** Rechazada (más de +25 %) → Sospechosa (más de −50 %) → Opcionada (el resto).
  - **Modelo:** `Cotizacion` ◆ `ItemCotizacion`, `Proveedor` y `EstadoCotizacion`.
  - **Gestión de proveedores:** `ProveedorService`.
- **Adaptadores de salida**, cada uno detrás de un puerto escrito en la flecha: JPA (cotizaciones y proveedores) → BD; `PdfCotizacionAdapter` → PDF; `AlmacenObjetosAdapter` → almacén de objetos; `KafkaEventPublisher` → Kafka; `PrecioSugeridoRestAdapter` → ms-integracion.
- **Contenedores vecinos:** SPA, Gateway, ms-ordenes, ms-integracion, Kafka, BD y almacén de objetos. Coinciden con la 3a.

### 3.7 Páginas 4a–4e · Código, nivel 4

Diagramas de clases UML por servicio, con paquetes `co.edu.konradlorenz.<servicio>.<capa>`. Los métodos marcados ‹Vxx› atienden esa ventana y el patrón de cada clase va en magenta. **Mandan sobre el código**: el código se generó de ellas.

| Página | Servicios | Clases | Patrones |
|---|---|---|---|
| 4a | Librería compartida + ms-identidad | 28 | Publish-Subscribe (el «Observer distribuido», H9), Factory Method, Composite (menú por rol) |
| 4b | ms-pedidos, ms-menu, ms-inventario | 39 | Listeners y Scheduler (adaptadores de entrada) |
| 4c | ms-solicitudes, ms-cotizaciones | 40 | Chain of Responsibility (`ReglaValidacion`, con `cadenaPorDefecto()`), Facade |
| 4d | ms-ordenes, ms-notificaciones | 40 | Factory Method, Decorator (correo) |
| 4e | ms-integracion, ms-administracion, ms-analitica | 44 | Proxy + Adapter (SOAP del Ministerio), Singleton, Strategy (`CalculadorKpi`) |

**Cambios que más pesan** (los de las sesiones 1 a 5):
- `EventoDominio` es una clase abstracta con `id`, `usuario`, `correlacionId` y `ocurridoEn` (DEC-09); los eventos la extienden. La nota `n_outbox` cuenta el outbox transaccional y separa lo del sistema real (usuario y correlación que une el recorrido) del límite de la demo (DEC-42).
- Los PDF van al almacén de objetos y el evento lleva solo `pdfUrl` (D1).
- Una cotización guarda una lista de solicitudes (DEC-05).
- `Notificacion.enviar()` y `CreadorNotificacion.notificar()` llevan `adjuntoUrl` (DEC-42, sesión 5).
- `Correo` está en `puertos.salida` (H25).

### 3.8 Página 5 · Despliegue

- Dispositivos → CDN + WAF → DNS global con conmutación.
- **Centro de datos principal** (región A): balanceador L7 activo-activo; Kubernetes en **3 zonas** con autoescalado; Gateway ×3, Eureka ×2, la SPA ×2 y los 11 microservicios con sus réplicas mínimas y máximas; Kafka ×3; observabilidad con Prometheus, Grafana, OpenTelemetry y ELK.
- **Reparto por zona** (H24, DEC-20): tres cajas «Zona A», «Zona B» y «Zona C» con un gateway y un broker de Kafka en cada una. **Que Eureka y la SPA estén en las zonas A y B es un supuesto por confirmar.**
- **Capa de datos:** PostgreSQL ×11 con réplica síncrona en otra zona; Redis; almacén de objetos multizona; respaldo diario con retención de 30 días.
- **Centro de datos alterno** (región B, en espera tibia): Kubernetes en espera; Kafka replicado con MirrorMaker 2; réplicas asíncronas de PostgreSQL (RPO ≤ 5 min) y **réplica del almacén de objetos entre regiones** (H16).
- **Salida controlada** hacia el Ministerio, el correo y las redes sociales.

### 3.9 Página 6 · C4 Dinámico · recorrido de compra

**Qué muestra:** cómo fluye una compra desde que el jefe de cocina pide alimentos (V08) hasta que el director hace seguimiento a la orden (V23), con los datos de los mockups: **SOL-031**, **COT-018** ($520.000 contra $545.000, +4,81 %, OPCIONADA) y **OC-0012**. Es una secuencia de **14 pasos**, con los mensajes dentro de cada paso numerados (4.1, 4.2…; H23).

**Participantes:** el jefe, el auxiliar y el director; la SPA y el API Gateway; ms-solicitudes, ms-inventario (DEC-18), ms-cotizaciones, ms-integracion, ms-ordenes, ms-notificaciones, ms-analitica y ms-administracion; el Ministerio, Kafka, el servidor de correo y el proveedor.

**Lo que cambió con las decisiones:** ms-inventario recibe `solicitud.registrada` en el paso 2 (DEC-18); el paso 4 dice «una o varias solicitudes (aquí SOL-031)» (DEC-19); en el paso 12 ms-cotizaciones también consume `orden.generada` y cada solicitud pasa a COMPLETADA (D3).

**Decisiones tomadas al dibujarla:** estilo secuencia y no colaboración numerada; las respuestas van directo a la SPA; sin bases de datos; cada evento muestra todos sus consumidores; el aviso EN TRÁMITE va por correo (D6, aceptada); la auditoría se agrupa al final, con una nota que aclara que los otros diez servicios la publican justo después de cada escritura.

---

## 4. Qué se hizo, sesión por sesión

El plan completo está en `PLAN_CORRECCIONES.md`. Cada corrección es un commit con su id al inicio, y se puede revertir sola con `git revert`.

| Sesión | Qué cubrió | Resultado |
|---|---|---|
| 1 · Git y clases 4a–4e | Montar los dos repos (DEC-01) y corregir 4a–4e | Aprobada el 7-nov. Validada dos veces por el revisor (N1–N15) |
| 2 · Páginas 1, 2, 3a, 3b, 5 y 6 | Inventario de ítems (`INVENTARIO_SESION2.md`) y DEC-15 a DEC-24 | Aprobada el 8-nov. ~100 commits; validada dos veces (S1–S14) |
| 3 · Código: firmas | Declarar las 191 clases, sus 403 métodos y las relaciones del diagrama, con el comentario de cada ventana. DEC-25 a DEC-38 | Aprobada el 8-nov. 241 commits en el código; validada cuatro veces (K1–K18) |
| 4 · Código: demo | Lógica para el recorrido de compra, adaptadores en memoria, `pruebas/` y `ejecutar.sh`. DEC-39 a DEC-46 | Lista dentro del alcance de la demo. 134 commits; validada con dos rondas (S4-1 a S4-16) |
| 5 · Excel y documentos | Excel (DEC-47), 4d y nota `n_outbox` (DEC-42), `CORRECCIONES_REPORTE.md`, este documento, `CORRECCIONES_HU_Y_MOCKUPS.md` y la validación final | En curso: ver la tabla de Avance del plan |

**Lo que no se tocó, a propósito:** `Arquitectura_Konrad_GourmetOriginalSinCorregir.drawio`, el reporte PDF, el PDF de UX/UI, el de flujo y el Story Mapping. Lo que hay que cambiar en ellos está en los archivos `CORRECCIONES_*.md`.

**Cómo deshacer:**
```bash
cd FinalProyecto/documentos            # o FinalProyecto/KonradGourmet
git log --oneline                      # ver los cambios, uno por commit
git revert <commit>                    # deshacer uno solo, dejando registro
git switch main                        # ver el estado original completo
git switch correcciones-revision       # volver al trabajo
```

**Cómo se hicieron las páginas nuevas (3a, 3b y 6):** un script de Python las insertó como texto dentro del XML, sin reescribir el resto del archivo; se exportó cada una a PNG, se revisó por zonas y solo entonces se aplicó al archivo real. Donde los documentos se contradecían, la decisión quedó escrita dentro de la propia página, en las notas «Qué se simplificó» y «Decisiones del diagrama», y en `DECISIONES.md`.

**Fuera del proyecto:** la memoria de Claude (`~/.claude/projects/.../memory/`) guarda dónde está cada entregable y el avance, para no releer todo en cada sesión. Los scripts generadores y las vistas previas estaban en una carpeta temporal y se perdieron; el código se puede regenerar a partir de las páginas 4a–4e, y el respaldo que vale es `…OriginalSinCorregir.drawio`.

---

## 5. Estado de cada pendiente

Los hallazgos H1–H26 están en `REVISION_2026-11-07.md`; los E1–E3 y D1–D12, de la versión anterior de este documento, se resuelven aquí.

### 5.1 Errores de texto (E)

| Id | Error | Estado |
|---|---|---|
| **E1** | La flecha a Redis decía «JDBC» (página 1) | ✅ Corregido: `Redis · TLS` |
| **E2** | «Kubernetes · 2 zonas» (página 1) | ✅ Corregido: «3 zonas» |
| **E3** | «Listener · 5 tópicos» en `NotificacionListeners` (4d) | ✅ Corregido: «6 tópicos» |

### 5.2 Decisiones de diseño y alcance (D)

| Id | Tema | Estado |
|---|---|---|
| **D1** | Dónde se guarda el PDF | ✅ Almacén de objetos; el evento lleva `pdfUrl` (DEC-08). Puertos y adaptadores en 4c y 4d; el correo lleva el PDF en `adjuntoUrl` (DEC-42) |
| **D2** | Rutas del gateway | ✅ 13 prefijos (DEC-12), en el mapa y la 3a. Falta el reporte (R-17) |
| **D3** | Una segunda orden sobre la misma cotización | ✅ Estado **CON ORDEN**; ms-ordenes rechaza la segunda orden y ms-cotizaciones consume `orden.generada` (la demo lo muestra) |
| **D4** | La alarma sigue activa hasta que haya solicitud | ✅ ms-inventario consume `solicitud.registrada` (DEC-18) |
| **D5** | Editar y desactivar platos | ✅ `editar()` y `desactivar()` en 4b y en el código. Sin mockup |
| **D6** | Canal del aviso EN TRÁMITE | ✅ Correo (aceptada con DEC-08) |
| **D7** | HU-15, proveedores | ✅ Dentro del alcance (DEC-07): `/api/proveedores/**` en ms-cotizaciones. Mockup opcional en `CORRECCIONES_HU_Y_MOCKUPS.md` |
| **D8** | HU-19, promociones | ✅ Dentro del alcance (DEC-07): `SugerenciaPromocion` y rutas en `/api/bam/**`. Mockup opcional. La demo no la ejecuta (cuerpos pendientes) |
| **D9** | HU-07 es copia de HU-06 | 🟡 El código ya filtra por fecha, producto, marca y estado (`CriteriosConsulta`). Falta reescribir la tarjeta: va en `CORRECCIONES_HU_Y_MOCKUPS.md` |
| **D10** | El README llamaba Strategy a la cadena de reglas | ✅ README corregido: Chain of Responsibility, con los 10 patrones (H19) |
| **D11** | El script del reporte | 🔴 Sigue abierto (A1). Los cambios están en `CORRECCIONES_REPORTE.md` |
| **D12** | La demo prometida | ✅ Existe y reproduce COT-018 y las otras seis salidas (DEC-31). Falta que el reporte describa el recorrido completo y sus límites (R-45; R-46 solo precisa el signo y los totales de la tabla) |

### 5.3 Hallazgos de la revisión (H1–H26)

Los textos del reporte se corrigen con `CORRECCIONES_REPORTE.md` (R-xx), cuando aparezca el script.

| Id | Sev. | Tema | Estado |
|---|---|---|---|
| H1 | Bloqueante | El reporte afirma métodos y comportamiento que el código no tenía | ✅ Código: los 23 métodos con su comentario y las relaciones. 🟡 Reporte: R-06, R-07, R-35, R-41, R-44 y R-51 |
| H2 | Alta | El descuento de inventario no se podía calcular | ✅ `pedido.registrado` lleva las líneas; ms-inventario consume `menu.actualizado`. 🟡 R-34 |
| H3 | Alta | El listener del menú iba directo al dominio | ✅ Pasa por caso de uso y el catálogo se persiste |
| H4 | Alta | Faltaba el PDF de la orden | ✅ `GeneradorPdfPort` en ms-ordenes; el evento lleva el correo y `pdfUrl` |
| H5 | Alta | No había consulta de cotización por número | ✅ `detalle()` y `GET /api/cotizaciones/{id}`. 🟡 R-52 |
| H6 | Alta | V03 no tenía de dónde leer el menú y las mesas | ✅ `catalogo()`, `mesas()` y la entidad `Mesa`. 🟡 R-52 |
| H7 | Alta | Los KPI no recibían filtros | ✅ `kpis(desde, hasta, sucursalId)` |
| H8 | Alta | 6 clases con tipo distinto al del diagrama | ✅ Alineadas; `cadenaPorDefecto()` existe. R-36 |
| H9 | Media | «Observer» no era Observer | ✅ «Publish-Subscribe (Observer distribuido)» en 4a, el código, el README y la demo. 🟡 R-37 |
| H10 | Media | Tres servicios no podían publicar auditoría | ✅ Agregado el puerto. 🟡 R-13, R-15 y R-33 |
| H11 | Media | Los eventos no llevaban id, usuario ni correlación | ✅ DEC-09 y nota del outbox. 🟡 R-21 |
| H12 | Media | No se podía completar una solicitud | ✅ `completar()` |
| H13 | Media | El umbral del 25 % en el Singleton | ✅ El umbral es de ms-inventario; `GET /api/admin/parametros/tema`. 🟡 R-38 |
| H14 | Media | Los logs estaban en ELK pero se leían de la BD | ✅ `LogsPort` con adaptador de Elasticsearch |
| H15 | Media | `CifradorPort` sin `cifrar()` ni regla de contraseña | ✅ DEC-10. 🟡 R-09 y R-57 |
| H16 | Media | El almacén de objetos sin réplica ni respaldo | ✅ Página 5. 🟡 R-12 y R-50 |
| H17 | Media | Eureka con `[HTTP]` | ✅ `[HTTPS · mTLS]` |
| H18 | Media | HU-12 parcial | ✅ `PENDIENTE_PRECIO` y `reenviar()` |
| H19 | Media | README con 6 de los 10 patrones | ✅ Los 10 patrones |
| H20 | Media | Excel inconsistente | ✅ Corregido (DEC-47) |
| H21 | Baja | Réplicas de ms-menu y «solo escalan 3» | 🟡 Solo texto del reporte: R-08 y R-16 |
| H22 | Baja | Sin leyendas ni protocolos | ✅ Leyendas en todas las páginas; `[Kafka/TLS]` y `[SMTP/TLS]` |
| H23 | Baja | Mensajes sin numerar en la página 6 | ✅ 4.1, 4.2… |
| H24 | Baja | Las 3 zonas eran solo un texto | ✅ Tres cajas de zona. 🟡 Falta confirmar Eureka y SPA (DEC-20) |
| H25 | Baja | `Correo` en `dominio` | ✅ En `puertos.salida` |
| H26 | Baja | Etiquetas del Story Mapping | 🟡 Anotado en `CORRECCIONES_HU_Y_MOCKUPS.md` |

Leyenda: ✅ hecho · 🟡 anotado, falta que alguien lo aplique en un archivo que no se toca · 🔴 abierto.

### 5.4 Las preguntas que dejó el revisor

| # | Pregunta | Estado |
|---|---|---|
| 1 | HU-09: ¿una o varias solicitudes por cotización? | ✅ DEC-05: varias |
| 2 | ¿Demo y firmas, o corregir el reporte? | ✅ DEC-02: las dos primeras. El script: abierta (A1) |
| 3 | ¿Los KPI son del director comercial o del de compras? | ✅ DEC-06: comercial |
| 4 | ¿Cómo llega la alarma por WebSocket a la réplica que tiene la sesión? | 🔴 Abierta (A2) |
| 5 | ¿De quién es la cuenta `mari00aa120-eng`? | 🔴 Abierta (A3) |

---

## 6. Qué falta y quién lo hace

**Falta en la sesión 5 (lo hace Claude):**
- Reescribir el plan y su tabla de Avance.
- Pasar la validación final del `arquitecto-revisor` y corregir lo que encuentre (`VALIDACION_SESION5.md`).

**Falta aplicar fuera de los repos (las listas están escritas):**

| Qué | Dónde | Quién |
|---|---|---|
| Aplicar las 59 correcciones al reporte, o rehacerlo | `CORRECCIONES_REPORTE.md` | Quien tenga el script (A1), o el equipo |
| Corregir los mockups V13, V17, V19–V23, V03, V04, V05 y V18 (y V07, opcional), la tarjeta HU-07 y las etiquetas del Story Mapping | `CORRECCIONES_HU_Y_MOCKUPS.md` | El equipo, en Canva |

**Decisiones del equipo:**

| Id | Qué hay que decidir | Por qué importa |
|---|---|---|
| **A1** | Quién tiene el script de ReportLab | Sin él, el reporte no se puede corregir |
| **DEC-20** | Dónde corren Eureka y la SPA en las zonas A y B | Es un supuesto del dibujo; el reporte lo repetiría |
| **T-06** | Cómo llevan `cotizacion.validada` y `tendencia.detectada` los tipos de otro servicio, sin romper DEC-29 | La demo no los consume: sin esto, el tablero de proveedores y las promociones no tendrían cómo alimentarse |
| **A2** | Cómo llega la alarma de WebSocket a la réplica correcta | El profesor puede preguntarlo |
| **A3** | De quién es la cuenta `mari00aa120-eng` | Conviene saberlo antes de la sustentación |
| **DEC-48** | Dónde van las páginas 3a, 3b y 6 en el reporte | Solo importa cuando se regenere |
| **A4** | Si `Pedido` guarda qué mesero lo tomó (`meseroId`) | HU-01 pide «asociado a… mi usuario»; cambia una firma, así que no se hizo |

**Supuestos por confirmar** (cada uno se revierte con su commit si no convence): DEC-08, DEC-39, DEC-40 (inventario inicial e histórico del tablero), DEC-41, DEC-44, DEC-45 y el orden de filas del Excel (DEC-47).

---

## 7. Glosario rápido

| Término | Qué significa aquí |
|---|---|
| **Contenedor (C4)** | Algo que se despliega o se ejecuta por separado: una aplicación, un servicio o una base de datos. No es lo mismo que un contenedor Docker |
| **Componente (C4)** | Una agrupación de clases dentro de un contenedor, con una responsabilidad clara |
| **API Gateway** | La única puerta de entrada desde afuera. Valida el JWT y envía cada ruta `/api/…` a su microservicio |
| **Ruta del gateway** | Un prefijo como `/api/pedidos/**` que indica a qué servicio va la petición. Un servicio sin ruta no es alcanzable desde afuera |
| **Kafka / tópico / evento** | Kafka es el buzón compartido. Un tópico es un canal temático, como `orden.generada`. Un evento es un mensaje corto que avisa que algo pasó, sin esperar respuesta |
| **Publish-Subscribe** | Quien publica no sabe quién lee. Es lo que hace Kafka; el Observer clásico, en cambio, guarda la lista de observadores |
| **Hexagonal / puerto / adaptador** | El dominio queda en el centro. Un puerto es la interfaz con la que el dominio pide algo afuera; un adaptador es la implementación concreta (JPA, Kafka, REST, OpenPDF) |
| **Outbox** | El evento se guarda en la misma transacción que el cambio y un proceso lo publica después, para no perderlo |
| **Claim check** | Guardar el archivo pesado aparte y enviar en el mensaje solo la referencia de dónde está |
| **JDBC** | El conector de Java para bases de datos relacionales (SQL). No sirve para Redis |
| **Redis** | Una caché en memoria, de tipo clave-valor. Aquí guarda los precios del Ministerio para no consultarlo cada vez (patrón Proxy) |
| **Zona de disponibilidad** | Un centro de datos físico separado dentro de la misma región de la nube. Si se cae una zona, las demás siguen |
| **WebSocket** | Una conexión que el navegador deja abierta para que el servidor le empuje avisos, como la alarma de stock bajo |
| **ShedLock** | Librería que da un candado en la base para que una tarea programada corra en una sola réplica |
| **PHU** | Punto de historia de usuario: aquí vale 25 horas |
| **SPA** | Una aplicación web de una sola página (React) que corre en el navegador y habla con la API |
| **BAM** | Business Activity Monitoring: el tablero de indicadores del director comercial |
