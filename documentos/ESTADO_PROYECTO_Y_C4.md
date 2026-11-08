# Konrad Gourmet · Contexto, estado y diagramas C4

> Documento de trabajo del equipo. Estado revisado el **7 de noviembre de 2026**.
> Resume el proyecto, lo que hay hoy, los cambios hechos con su motivo y todo lo relacionado con el modelo C4 del archivo `Arquitectura_Konrad_Gourmet.drawio`.

---

## Índice

1. [Contexto del proyecto](#1-contexto-del-proyecto)
2. [Estado actual](#2-estado-actual)
3. [Diagramas C4 del .drawio](#3-diagramas-c4-del-drawio)
4. [Cambios realizados y por qué](#4-cambios-realizados-y-por-qué)
5. [Problemas encontrados y decisiones pendientes](#5-problemas-encontrados-y-decisiones-pendientes)
6. [Orden de trabajo recomendado](#6-orden-de-trabajo-recomendado)
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
3. El **auxiliar de compras** genera la solicitud de cotización. El sistema consulta el precio estándar del Ministerio de Comercio, genera un PDF y se lo envía al proveedor por correo certificado.
4. El **director de compras** registra los valores que respondió el proveedor y valida la cotización con las reglas de precio: **RECHAZADA** si está más de 25 % por encima, **SOSPECHOSA** si está más de 50 % por debajo y **OPCIONADA** en cualquier otro caso. Con las opcionadas genera la orden de compra.
5. El **director comercial** consulta el tablero BAM, con indicadores y tendencias.
6. El **administrador** gestiona los parámetros, la imagen corporativa y la auditoría.

Requerimientos no funcionales principales:
- 200.000 usuarios concurrentes y 1.000 TPS.
- Disponibilidad del 99,7 % y recuperación ante desastres en un centro de datos alterno.
- Crecimiento del 200 % en archivos.
- Correo certificado con sello de tiempo y auditoría de cada operación de creación, lectura, edición o borrado (CRUD).
- Integración SOAP, en ambos sentidos.

### 1.3 Arquitectura elegida

| Estilo | Qué decide | Dónde se ve |
|---|---|---|
| **Microservicios** | 11 servicios desplegables, cada uno con su propia base de datos, detrás de un API Gateway | Mapa, página 3a y página 5 |
| **Orientada a eventos** | Los servicios se avisan por Kafka sin esperarse, con 10 tópicos | Mapa y páginas 3a y 6 |
| **Hexagonal** (puertos y adaptadores) | El dominio de cada servicio no depende de la base de datos, del SOAP, del SMTP ni del PDF | Mapa y páginas 3b y 4a–4e |

Apoyos:
- Cliente-servidor REST.
- SOA/SOAP en la frontera, a cargo de ms-integracion.
- OAuth2/JWT.
- API Gateway «Facade».
- Service Registry (Eureka).
- Balanceador L7.

**Los 11 microservicios**

| Servicio | Responsabilidad | Ruta | Ventanas |
|---|---|---|---|
| ms-identidad | OAuth 2.0 / OIDC: usuarios, roles, menú por rol; emite el JWT | `/api/auth/**` | V01 |
| ms-pedidos | Pedidos por mesa (de 4 a 20 réplicas en hora pico) | `/api/pedidos/**` | V02–V04 |
| ms-inventario | Existencias por sucursal, descuento diario a las 23:59, umbral del 25 % | `/api/inventario/**` | V05, V07 |
| ms-menu | Platos, recetas, categorías y productos | `/api/platos/**` | V06 |
| ms-solicitudes | Solicitudes de alimentos (PENDIENTE → EN TRÁMITE) | `/api/solicitudes/**` | V08–V12 |
| ms-cotizaciones | Cotización, PDF, registro de valores y cadena de reglas | `/api/cotizaciones/**` | V13–V20 |
| ms-ordenes | Órdenes de compra desde cotizaciones OPCIONADAS | `/api/ordenes-compra/**` | V21–V23 |
| ms-integracion | Consume el SOAP del Ministerio y expone servicios SOAP | `/api/integracion/**` (según el reporte) | apoya V13 |
| ms-notificaciones | Correos certificados y alarmas en la app | `/api/notificaciones/**` (según el reporte) | V05 (alarma) |
| ms-administracion | Parámetros, imagen corporativa, auditoría y logs | `/api/admin/**` | sin mockup |
| ms-analitica | Tablero BAM y detección de tendencias | `/api/bam/**` | sin mockup |

**Los 10 tópicos de Kafka**

| Tópico | Lo publica | Lo consumen |
|---|---|---|
| `menu.actualizado` | ms-menu | ms-pedidos |
| `pedido.registrado` | ms-pedidos | ms-inventario, ms-analitica |
| `inventario.stock-bajo` | ms-inventario | ms-notificaciones, ms-analitica |
| `solicitud.registrada` | ms-solicitudes | ms-notificaciones, ms-analitica |
| `solicitud.en-tramite` | ms-solicitudes | ms-notificaciones |
| `cotizacion.enviada` | ms-cotizaciones | ms-notificaciones, ms-solicitudes |
| `cotizacion.validada` | ms-cotizaciones | ms-analitica |
| `orden.generada` | ms-ordenes | ms-solicitudes, ms-notificaciones, ms-analitica |
| `tendencia.detectada` | ms-analitica | ms-notificaciones |
| `auditoria.evento` | todos los servicios | ms-administracion |

### 1.4 Dónde está cada cosa

**En esta carpeta (`FinalProyecto/`, que no es un repo git):**

| Archivo | Qué es |
|---|---|
| `Arquitectura_Konrad_Gourmet.drawio` | Los diagramas: **11 páginas**, 3 de ellas nuevas (ver sección 4) |
| `Arquitectura_Konrad_GourmetOriginalSinCorregir.drawio` | Copia intacta del original, con 8 páginas. **No modificar** |
| `Reporte_Arquitectura_Konrad_Gourmet.pdf` | Reporte de arquitectura (38 págs.), generado con Python y ReportLab |
| `Copia de ARQUITECTURA-Supuestos de Capacidad  del equipo.xlsx` | Capacidad del equipo y sprints |
| `DISEÑO UXUI.pdf` | Mockups de las 23 ventanas (Canva) |
| `flujo-web-konrad-gourmet (1).pdf` | Flujo web entre ventanas |
| `KonradGourmet/` | Repo del código (`github.com/iCastro117/KonradGourmet`) |
| `ESTADO_PROYECTO_Y_C4.md` | Este documento |

**Fuera de esta carpeta, en `../proyecto/`:**

| Archivo | Qué es |
|---|---|
| `../proyecto/contexto/Visual Story Mapping.pdf` | Las **19 historias de usuario** (HU-01 a HU-19). La tarjeta de la HU-n está en la página 7+n y es una imagen. MVP 1: HU-01–05 · MVP 2: HU-06–11 · MVP 3: HU-12–15 · MVP 4: HU-16–19 |
| `../proyecto/contexto/Clases/` | Material de clase; `General1.pdf` y `General4.pdf` explican el C4 y el diagrama dinámico |
| `../proyecto/exposicion1/principalesHU.md` | 4 historias en texto (HU-01, 10, 13 y 18) |

---

## 2. Estado actual

| Área | Estado | Detalle |
|---|---|---|
| Diseño de arquitectura | **Sólido** | Microservicios + eventos + hexagonal, justificados contra los requerimientos no funcionales. Trazabilidad de las 23 ventanas |
| Diagramas C4 | **Completos** | Los 4 niveles más el despliegue y el dinámico. Quedan errores de texto en páginas originales (sección 5) |
| Reporte PDF | **Desactualizado** | No incluye las páginas 3a, 3b ni 6. Dice que el `.drawio` tiene 8 páginas. Afirma una demo que no existe |
| Historias de usuario | **Con huecos** | HU-07 duplicada. Varias historias tienen criterios que la arquitectura no cubre (sección 5) |
| Código Java | **Sin avance** | El repo sigue en `ad71e69` (29-oct-2026). Tiene 156 clases, todas vacías; solo hay un `main` que imprime una línea |
| Prototipo web | **1 de 23 pantallas** | Solo el login (V01), en HTML/CSS estático y sin conexión al backend |
| Planeación (Excel) | **Inconsistente** | El PHU vale 25 h en el Story Mapping y 30 h o 100 h en el Excel. El sprint 4 tiene 10 puntos con una capacidad de 7,47 PHU |

**El mayor riesgo de la entrega:** el reporte (págs. 3 y 32) afirma que *"una demostración ejecutable reproduce los valores de la ventana 19 (COT-018, +4,81 %, OPCIONADA)"* y que se verificó que el código compila y la reproduce. En el repo esa demo no existe, y cualquiera lo comprueba en segundos.

---

## 3. Diagramas C4 del .drawio

### 3.1 Qué es el modelo C4

El C4 describe una arquitectura con zoom progresivo. Cada nivel abre una caja del nivel anterior:

| Nivel | Pregunta que responde | Página en el `.drawio` |
|---|---|---|
| 1 · Contexto | ¿Quién usa el sistema y con qué sistemas externos habla? | **2** |
| 2 · Contenedores | ¿Qué piezas se despliegan por separado y cómo se comunican? | **3a** (nueva) |
| 3 · Componentes | ¿Qué hay dentro de un contenedor? | **3b** (nueva, ms-cotizaciones) |
| 4 · Código | ¿Qué clases implementan cada componente y dónde va cada patrón? | **4a–4e** |
| Complemento · Despliegue | ¿Dónde y cómo se ejecuta? | **5** |
| Complemento · Dinámico | ¿En qué orden ocurren los mensajes en un caso real? | **6** (nueva) |
| Complemento · Ecosistema | Varios sistemas de la empresa juntos | No se hizo: nunca se pidió y, con un solo sistema, repetiría el contexto |

**Por qué hacían falta las páginas nuevas:** el propio reporte (pág. 4) dice que *"el modelo C4 es obligatorio en sus cuatro niveles —contexto, contenedores, componentes y código—"* y que *"los diagramas dinámico y de despliegue son complementarios"*. El original tenía los niveles 1 y 4 y el despliegue. Los niveles 2 y 3 estaban mezclados dentro del mapa integrado, y el dinámico no existía.

### 3.2 Inventario de páginas

Están en el orden en que aparecen en draw.io.

| # | Página | id | Origen | Cajas / flechas |
|---|---|---|---|---|
| 1 | 1 · Mapa integrado | p1 | Original | 302 / 141 |
| 2 | 2 · C4 Contexto | p2 | Original | 13 / 12 |
| 3 | **3a · C4 Contenedores** | p9 | **Nueva** (6-nov) | 59 / 46 |
| 4 | **3b · C4 Componentes · ms-cotizaciones** | p10 | **Nueva** (6-nov) | 41 / 22 |
| 5 | 4a · Código · compartido e identidad | p3 | Original | 35 / 23 |
| 6 | 4b · Código · pedidos, menú e inventario | p4 | Original | 58 / 30 |
| 7 | 4c · Código · solicitudes y cotizaciones | p5 | Original | 52 / 30 |
| 8 | 4d · Código · órdenes y notificaciones | p6 | Original | 48 / 31 |
| 9 | 4e · Código · integración, administración y analítica | p7 | Original | 56 / 33 |
| 10 | 5 · Despliegue | p8 | Original | 23 / 15 |
| 11 | **6 · C4 Dinámico · recorrido de compra** | p11 | **Nueva** (7-nov) | 87 / 102 |

> Las 8 páginas originales están **idénticas** a las de `Arquitectura_Konrad_GourmetOriginalSinCorregir.drawio`. Se comprobó comparando el XML página por página.

### 3.3 Página 1 · Mapa integrado (original)

Es la vista general que combina los tres estilos:
- Personas por rol.
- La SPA.
- El API Gateway con sus rutas.
- Los 11 microservicios, dibujados como hexágonos con sus adaptadores.
- La banda ámbar de Kafka.
- Las bases de datos y los sistemas externos.

El reporte explica su notación en la sección 9.

**Errores conocidos:**
- La flecha a Redis dice "JDBC".
- Habla de "2 zonas", mientras que el despliegue y el reporte dicen 3.
- Muestra 9 rutas en el gateway; el reporte lista 11.

Detalle en la sección 5.

### 3.4 Página 2 · C4 Contexto, nivel 1 (original)

El sistema aparece como una sola caja. Alrededor:
- **6 personas:** mesero, jefe de cocina, auxiliar de compras, director de compras, director comercial y administrador.
- **El proveedor** (persona externa).
- **4 sistemas externos:**
  - Ministerio de Comercio, por SOAP.
  - Servidor de correo certificado, por SMTP con sello de tiempo.
  - Redes sociales, por APIs públicas.
  - Sistemas externos que consumen los servicios SOAP del restaurante.

Está en la figura 10 del reporte (págs. 27–28). No tiene errores conocidos.

### 3.5 Página 3a · C4 Contenedores, nivel 2 (nueva)

**Qué muestra:**
- Las 6 personas y el proveedor, cada una con el color de su rol.
- La **SPA** (React + TypeScript) y el **servidor web** (Nginx detrás de la CDN y el WAF).
- El **API Gateway «Facade»** (Spring Cloud Gateway) y el **Service Registry** (Eureka, con 2 réplicas y un latido cada 30 s).
- Los **11 microservicios**, cada uno con su responsabilidad, su ruta `/api/…` y sus ventanas.
- Cada servicio con **su propia base de datos** (PostgreSQL), más la **caché de precios** (Redis) y el **almacén de objetos** (PDF y archivos).
- El **broker Kafka** (3 brokers, factor de réplica 3, retención de 7 días, cola de errores), con lo que publica y consume cada servicio.
- Las llamadas síncronas entre servicios:
  - ms-ordenes → ms-cotizaciones, para consultar la cotización OPCIONADA.
  - ms-cotizaciones → ms-integracion, para pedir el precio sugerido.
- Los 4 sistemas externos, con el protocolo en cada flecha.
- Una leyenda y la nota "Qué se simplificó".

**De dónde sale:** las relaciones se extrajeron del mapa integrado (quién llama a quién y quién publica o consume cada tópico) y de las tablas del reporte. No se inventaron relaciones nuevas.

**Decisiones tomadas al dibujarla:**

| Decisión | Por qué |
|---|---|
| Se agregó el **servidor web Nginx** como contenedor | El reporte dice que la SPA la sirve Nginx detrás de una CDN, y en C4 lo que se despliega por separado es un contenedor |
| El **almacén de objetos** se conectó a ms-cotizaciones | La página 5 y el reporte (pág. 5) lo mencionan para los PDF, pero no dicen qué servicio lo usa |
| La flecha a **Redis** dice `Redis · TLS` y no "JDBC" | JDBC es solo para bases relacionales. En el mapa original sigue diciendo "JDBC" |
| El gateway llega a los servicios con **una sola flecha** | Así lo hace el mapa. Cada servicio indica su prefijo para no dibujar 11 flechas |
| **No se dibujó el balanceador** ni las réplicas | Son infraestructura y ya están en la página 5 |

**Error propio pendiente de corregir:** la caja de ms-notificaciones dice *"Sin ruta propia: reacciona a eventos"*, pero la flecha del WebSocket de la alarma pasa por el gateway. Se corrige junto con la decisión D2 (sección 5).

### 3.6 Página 3b · C4 Componentes de ms-cotizaciones, nivel 3 (nueva)

**Por qué ms-cotizaciones:** el C4 no exige el nivel 3 para todos los contenedores, solo donde aporta. Este servicio:
- atiende el flujo de compra de V13 a V20, que es el ejemplo central del reporte;
- concentra tres patrones: **Facade**, **Chain of Responsibility** y **Adapter**.

**Qué muestra:**
- **Adaptador de entrada.** `CotizacionRestController`, con sus 7 endpoints:
  - `POST /api/cotizaciones` (V13);
  - `GET …/{id}/pdf` (V14);
  - `GET /api/cotizaciones` (V15, V17 y V20);
  - `GET …/pendientes` (V16);
  - `PUT …/{id}/valores` (V18), y los demás.
- **Núcleo hexagonal** (no importa Spring, JPA, Kafka ni HTTP):
  - **Casos de uso de cotización:** `CotizacionService` y `GenerarCotizacionFacade «Facade»`. La fachada resuelve el clic de V13: consulta el precio sugerido de cada producto, guarda, genera el PDF y publica `cotizacion.enviada`.
  - **Reglas de validación «Chain of Responsibility»:** Rechazada (más de +25 %) → Sospechosa (más de −50 %) → Opcionada (el resto). Ejemplo: COT-018 da +4,81 % y queda OPCIONADA.
  - **Modelo de cotización:** `Cotizacion` ◆ `ItemCotizacion`, `Proveedor` y `EstadoCotizacion`.
- **Adaptadores de salida.** Cada uno se conecta con el núcleo a través de un puerto, cuyo nombre va escrito en la flecha:
  - `CotizacionJpaAdapter` → `CotizacionRepositoryPort` → BD cotizaciones;
  - `PdfCotizacionAdapter` → `GeneradorPdfPort` → almacén de objetos;
  - `KafkaEventPublisher` → `EventPublisherPort` → Kafka;
  - `PrecioSugeridoRestAdapter` → `PrecioSugeridoPort` → ms-integracion.
- **Contenedores vecinos:** SPA, Gateway, ms-ordenes, ms-integracion, Kafka y BD. Coinciden con la 3a.

**Decisiones tomadas al dibujarla:**

| Decisión | Por qué |
|---|---|
| El servicio y la fachada se dibujan como **un solo componente** de aplicación | Los dos usan los mismos tres puertos; separados, las flechas se cruzan sí o sí. Una nota aclara que solo la fachada usa `PrecioSugeridoPort` |
| Los puertos van **en las etiquetas de las flechas** | Un puerto es la interfaz que vive justo en el borde del hexágono, que es por donde cruza la flecha |
| Se conecta el PDF con el almacén de objetos | Lo dice la 3a. Ojo: la 4c no tiene un puerto para guardar el PDF (decisión D1) |

### 3.7 Páginas 4a–4e · Código, nivel 4 (originales)

Son diagramas de clases UML por servicio, con paquetes `co.edu.konradlorenz.<servicio>.<capa>`. Los métodos marcados ‹Vxx› atienden esa ventana, y el patrón de cada clase va en magenta.

| Página | Servicios | Patrones |
|---|---|---|
| 4a | Librería compartida + ms-identidad | Observer (eventos), Factory Method, Composite (menú por rol) |
| 4b | ms-pedidos, ms-menu, ms-inventario | Listeners y Scheduler (adaptadores de entrada) |
| 4c | ms-solicitudes, ms-cotizaciones | Chain of Responsibility (`ReglaValidacion`), Facade (`GenerarCotizacionFacade`) |
| 4d | ms-ordenes, ms-notificaciones | Factory Method, Decorator (correo) |
| 4e | ms-integracion, ms-administracion, ms-analitica | Proxy + Adapter (SOAP del Ministerio), Singleton, Strategy (`CalculadorKpi`) |

**Errores conocidos:**
- La 4d dice «Listener · 5 tópicos» en ms-notificaciones, pero son 6.
- La 4c no tiene un puerto para guardar el PDF, y el evento `CotizacionEnviadaEvent` lleva el PDF completo (`pdf: byte[]`).
- El código del repo no implementa estos diagramas:
  - las clases están vacías;
  - el Decorator no envuelve `Correo`;
  - `GrupoMenu` y `OpcionMenu` no extienden `ElementoMenu`;
  - el Proxy no implementa su puerto;
  - ningún `*Service` implementa su caso de uso.

### 3.8 Página 5 · Despliegue (original)

- Dispositivos → CDN + WAF → DNS global con conmutación.
- **Centro de datos principal** (región A):
  - balanceador L7 activo-activo;
  - Kubernetes en **3 zonas de disponibilidad** con autoescalado;
  - Gateway ×3, Eureka ×2 y 11 microservicios con sus réplicas mínimas y máximas;
  - Kafka ×3;
  - observabilidad con Prometheus, Grafana, OpenTelemetry y ELK.
- **Capa de datos:**
  - PostgreSQL ×11, con un primario y una réplica síncrona;
  - Redis;
  - almacén de objetos;
  - respaldo diario con retención de 30 días.
- **Centro de datos alterno** (región B, en espera tibia):
  - Kubernetes en espera;
  - Kafka replicado con MirrorMaker 2;
  - réplicas asíncronas de PostgreSQL (RPO ≤ 5 min).
- **Salida controlada** hacia el Ministerio, el correo y las redes sociales.

No tiene errores conocidos; es la referencia para corregir las "2 zonas" del mapa.

### 3.9 Página 6 · C4 Dinámico · recorrido de compra (nueva)

**Qué muestra:** cómo fluye una compra desde que el jefe de cocina pide alimentos (V08) hasta que el director de compras hace seguimiento a la orden (V23). Usa los contenedores de la 3a y los datos de los mockups: **SOL-031**, **COT-018** (precio estándar de $520.000 frente a $545.000 del proveedor, +4,81 %, OPCIONADA) y **OC-0012**.

**De dónde sale:** sigue los 14 pasos de la tabla *"Recorrido completo de una compra"* del reporte (pág. 21) y la tabla de eventos de la misma página. En clase, el profesor describió el dinámico como una *"secuencia de llamados o de mensajes"* (`General1.pdf`, `General4.pdf`).

**Participantes (16 líneas de vida):**
- Jefe de cocina, auxiliar de compras y director de compras.
- SPA y API Gateway.
- ms-solicitudes, ms-cotizaciones, ms-integracion, ms-ordenes, ms-notificaciones, ms-analitica y ms-administracion.
- Ministerio de Comercio y Kafka.
- Servidor de correo y proveedor.

**Los 14 pasos:**

| Fase | # | Paso | Tipo · ventanas |
|---|---|---|---|
| 1 · El jefe pide alimentos | 1 | El jefe registra la solicitud de alimentos (SOL-031 queda PENDIENTE) | Síncrono · V08 → V09 |
| | 2 | Confirmación al jefe por correo | Evento |
| 2 · El auxiliar cotiza | 3 | El auxiliar filtra las solicitudes y abre el detalle | Síncrono · V11 → V12 |
| | 4 | El auxiliar genera la solicitud de cotización. El Proxy responde desde Redis o consulta al Ministerio; la Facade guarda COT-018 como ENVIADA y genera el PDF | Síncrono · V13 → V14 |
| | 5 | PDF al proveedor y revisión de la solicitud | Evento |
| | 6 | La solicitud del jefe pasa a EN TRÁMITE | Evento · V10 |
| Fuera del sistema | 7 | El proveedor responde por correo, días después | Fuera del sistema |
| 3 · El director valida y compra | 8 | El director registra los valores del proveedor ($545.000) | Síncrono · V18 |
| | 9 | El director valida. La cadena de reglas da +4,81 % → OPCIONADA | Síncrono · V19 |
| | 10 | KPI del proveedor con mejores precios | Evento |
| | 11 | El director elige la opcionada y genera OC-0012 | Síncrono · V20 → V22 |
| | 12 | La orden se propaga | Evento |
| | 13 | Seguimiento de órdenes | Síncrono · V23 |
| Transversal | 14 | Auditoría de cada CRUD | Evento · todas |

**Notación:**
- Flecha del color del rol: acción del usuario.
- Flecha sólida oscura: llamada síncrona.
- Flecha punteada gris: respuesta.
- Flecha ámbar discontinua: evento por Kafka.
- Flecha de puntos en cursiva: ocurre fuera del sistema.

El número de cada paso va en un círculo coloreado según su tipo.

**Decisiones tomadas al dibujarla:**

| Decisión | Por qué |
|---|---|
| **Estilo secuencia** y no colaboración numerada | El C4 permite los dos. Con 14 pasos, tres usuarios y saltos en el tiempo, la secuencia deja ver el orden sin cruzar flechas |
| Las respuestas van **directo a la SPA** | Toda respuesta vuelve por el gateway; se dibuja una sola flecha para no duplicarlas |
| **Sin bases de datos** | Cada servicio escribe solo en su base, que ya está en la 3a; aquí aparecen como notas |
| **Consumidores completos** | Cada evento muestra todos los consumidores de la tabla de eventos. Por eso en el paso 2 aparece ms-analitica, que la tabla del recorrido no menciona |
| **Aviso de EN TRÁMITE por correo** | El reporte dice *"aviso al jefe"* sin indicar el canal. La única alarma en la app (WebSocket) que documenta es la de stock bajo. **El equipo debe confirmarlo** (decisión D6) |
| **Auditoría agrupada al final** | Para no repetirla después de cada escritura. Una nota aclara que en realidad cada servicio la publica justo después de escribir |

---

## 4. Cambios realizados y por qué

### 4.1 Resumen

| Fecha | Cambio | Archivo | Por qué |
|---|---|---|---|
| 2026-11-06 | Nueva página **3a · C4 Contenedores** (id p9), entre la 2 y la 4a | `.drawio` | El nivel 2 del C4 es obligatorio y no tenía página propia |
| 2026-11-06 | Nueva página **3b · C4 Componentes · ms-cotizaciones** (id p10), después de la 3a | `.drawio` | El nivel 3 es obligatorio y no tenía página propia |
| 2026-11-07 | Nueva página **6 · C4 Dinámico · recorrido de compra** (id p11), al final | `.drawio` | El dinámico es el complemento que pide el reporte, y faltaba |
| 2026-11-07 | Este documento | `ESTADO_PROYECTO_Y_C4.md` | Dejar por escrito el contexto, el estado y los cambios |

**Nada se eliminó** y **ninguna página original se modificó**.

### 4.2 Cómo se hicieron

1. Cada página se generó con un script de Python que **inserta la página nueva como texto** dentro del XML del `.drawio`, sin reescribir el resto del archivo. El script se niega a correr si la página ya existe.
2. Primero se aplicó sobre una copia. La página se exportó a PNG con `drawio`, se revisó por zonas y se corrigió lo que salía mal: cilindros deformes, textos cortados, un título pisado, bloques demasiado anchos y una nota que cruzaba las líneas de vida.
3. Solo después se aplicó al archivo real y se validó que el XML siguiera siendo correcto.
4. Las relaciones, tecnologías y protocolos salen del mapa integrado, las páginas 4a–4e y el reporte. Donde los documentos no decían nada o se contradecían, la decisión está escrita **dentro de la propia página**, en las notas "Qué se simplificó" y "Decisiones del diagrama".

### 4.3 Lo que no se tocó

- **El repo `KonradGourmet`:** sin cambios ni commits. Sigue en `ad71e69` con el árbol limpio.
- **El reporte PDF, el Excel, los PDF de diseño y de flujo, el README y el login:** intactos.
- **Las 8 páginas originales del `.drawio`:** intactas, aunque tienen los errores de la sección 5.
- **`Arquitectura_Konrad_GourmetOriginalSinCorregir.drawio`:** intacto (SHA-256 `10a49edd…`).

### 4.4 Cómo deshacer

- **Volver al original:** en draw.io, borrar las pestañas 3a, 3b y 6 (clic derecho → Eliminar). El archivo queda idéntico a la copia `…OriginalSinCorregir.drawio`. También se puede reemplazar el archivo por esa copia.
- **Quitar solo el dinámico:** borrar la pestaña 6.

### 4.5 Fuera del proyecto

- **Memoria de Claude** (`~/.claude/projects/.../memory/`): notas para no releer todo en cada sesión. Guardan dónde está cada entregable, dónde están las historias de usuario y sus problemas, la preferencia de hacer un entregable por mensaje y los datos del usuario.
- **Carpeta temporal de la sesión:** los scripts generadores (`c4_componentes.py` y `c4_dinamico.py`), respaldos intermedios y vistas previas en PNG. Es temporal y puede borrarse sola; el respaldo que vale es `…OriginalSinCorregir.drawio`.

---

## 5. Problemas encontrados y decisiones pendientes

Todos salen de comparar dos fuentes que deberían coincidir y no lo hacen: el `.drawio` contra el reporte, o las historias de usuario contra la arquitectura.

### 5.1 Errores de texto (no requieren decisión)

| Id | Error | Dónde está | Corrección | Por qué |
|---|---|---|---|---|
| **E1** | La flecha a Redis dice "JDBC" | Página 1, de `PrecioReferenciaCacheProxy` a "Caché precios [Redis]" | `Redis · TLS` | JDBC es el conector de Java para bases relacionales con SQL. Redis es una caché clave-valor con su propio protocolo. Un profesor lo ve como un error de concepto |
| **E2** | "Kubernetes · 2 zonas + centro de datos alterno" | Página 1 | 3 zonas | La página 5 y el reporte (págs. 5 y 27) dicen 3. Kafka tiene 3 brokers con 3 réplicas, una por zona |
| **E3** | «Listener · 5 tópicos» en `NotificacionListeners` | Página 4d | 6 tópicos | Según la tabla de eventos (pág. 21 del reporte), consume `inventario.stock-bajo`, `solicitud.registrada`, `solicitud.en-tramite`, `cotizacion.enviada`, `orden.generada` y `tendencia.detectada`. El «5 tópicos» de la 4e (ms-analitica) sí es correcto |

### 5.2 Decisiones de diseño (traen recomendación)

**D1. ¿Dónde se guarda el PDF de la cotización?**
- **Fuente:** hay tres versiones distintas.
  - El reporte (pág. 5) y la página 5 dicen que los PDF van a un *almacén de objetos*.
  - En la 4c, `CotizacionEnviadaEvent` tiene `pdf: byte[]`: el archivo completo viaja dentro del mensaje de Kafka.
  - `GeneradorPdfPort` solo genera el PDF; no hay un puerto para guardarlo.
- **Por qué importa:**
  - Kafka rechaza por defecto los mensajes de más de ~1 MB.
  - Guarda cada mensaje 7 días en 3 copias, y después lo borra.
  - El reporte exige soportar un crecimiento del 200 % en archivos.
- **Recomendación:** guardar el PDF en el almacén y que el evento lleve solo la referencia (patrón *claim check*). En la 4c significa:
  - agregar un puerto `AlmacenArchivosPort` y su adaptador;
  - cambiar `pdf: byte[]` por `pdfUrl`.

**D2. ¿El gateway tiene 9 rutas u 11?**
- **Fuente:** el mapa muestra 9. La tabla del reporte (págs. 7–8) agrega `/api/notificaciones/**` ("alarmas en la app") y `/api/integracion/**` ("expone servicios SOAP").
- **Por qué importa:**
  - El navegador solo puede abrir el WebSocket de la alarma a través del gateway.
  - Los sistemas externos necesitan una puerta para consumir el SOAP.
- **Recomendación:** dejar 11 rutas en el mapa y corregir la caja de ms-notificaciones en la 3a.

**D3. HU-14: impedir una segunda orden sobre la misma cotización**
- **Fuente:** el criterio de la tarjeta HU-14 dice: *"Al generar la orden, la cotización debe cambiar a un estado que impida generar una segunda orden sobre ella."* Pero `orden.generada` no lo consume ms-cotizaciones (reporte, pág. 21), y ningún diagrama muestra un control de duplicados.
- **Por qué importa:** COT-018 sigue OPCIONADA después de OC-0012. Un doble clic o un segundo director pueden comprar dos veces lo mismo.
- **Recomendación:**
  1. ms-ordenes verifica en su base si ya hay una orden para esa cotización y, si la hay, responde con error. Esto tiene efecto inmediato.
  2. ms-cotizaciones consume `orden.generada` y pasa la cotización a un estado nuevo, para que deje de aparecer en V20.
- **A decidir:** el nombre del estado. Se propone **CON ORDEN**.

**D4. HU-16: la alarma debe seguir activa hasta que se pida el producto**
- **Fuente:** el criterio de la tarjeta HU-16 dice: *"La alerta debe permanecer activa hasta que se registre una solicitud para ese producto."* Nadie apaga la alerta, y `solicitud.registrada` solo lo consumen notificaciones y analítica.
- **Recomendación:** que ms-inventario, dueño de las alertas (`GET /api/inventario/alertas`), consuma `solicitud.registrada` y marque la alerta como atendida.

**D5. HU-04: editar y desactivar platos**
- **Fuente:** la tarjeta HU-04 pide *"consultar, editar y desactivar los platos"*. Solo existe `POST /api/platos`, y en la 4b `PlatoRestController` solo tiene `crear()` y `productosPorCategoria()`.
- **Recomendación:** agregar `editar()` y `desactivar()` con sus rutas; el evento `menu.actualizado` ya existe. Como no hay mockup de esa pantalla, quedaría solo en la arquitectura.

**D6. Canal del aviso EN TRÁMITE**
- **Fuente:** el paso 6 del recorrido (reporte, pág. 21) dice *"solicitud.en-tramite → aviso al jefe"* y no indica el canal.
- **Recomendación:** correo, como quedó dibujado en la página 6. Es el canal que ms-notificaciones usa para todo, excepto la alarma de stock bajo.

### 5.3 Decisiones de alcance (le tocan al equipo)

**D7. HU-15: registrar proveedores**
- **Fuente:** la tarjeta pide:
  - razón social, NIT único y obligatorio, correo válido, teléfono y dirección;
  - categorías de producto;
  - consultar, editar e inactivar; un proveedor inactivo no sale en las cotizaciones;
  - auditoría de cada acción.

  En la arquitectura, el proveedor solo existe en la 4c, con `id`, `nombre` y `correo`, sin ruta ni ventana.
- **Por qué importa:** es del MVP 3, el mismo del flujo de compra, y V13 depende de elegir un proveedor.
- **Recomendación:** incluirla en ms-cotizaciones con la ruta `/api/proveedores/**`, o declararla fuera de alcance en el reporte.

**D8. HU-19: promociones sugeridas**
- **Ya cubiertos:**
  - tendencias en redes, con ms-analitica y su tarea semanal;
  - los productos más pedidos;
  - el correo, con `tendencia.detectada` → notificaciones.
- **Faltan:**
  - el histórico dentro de la app;
  - marcar cada sugerencia como aceptada o descartada;
  - el periodo de vigencia;
  - el límite de una notificación por día.
- **Recomendación:** agregar una ruta en ms-analitica y la regla del límite diario, sin ventana nueva. Como es del MVP 4, también puede quedar fuera de alcance.

**D9. HU-07 es una copia de HU-06**
- La tarjeta "HU-07: Filtrar solicitudes por criterios" repite el texto de HU-06. Hoy la arquitectura solo filtra por estado (`?estado=PENDIENTE`).
- **A decidir:** qué filtros lleva (¿estado, fecha, sucursal?) y quién reescribe la tarjeta.

### 5.4 Decisiones prácticas

**D10. El README llama Strategy a lo que es Chain of Responsibility**
- **Fuente:** la línea 47 de `KonradGourmet/README.md` dice *"Strategy | cotizaciones/dominio | ReglaValidacion, ReglaRechazada…"*. El reporte, en las págs. 17, 20, 22 y 24, lo llama Chain of Responsibility.
- **Por qué importa:** son patrones distintos.
  - **Strategy:** se elige una forma de hacer algo entre varias intercambiables. Es el caso de `CalculadorKpi` en analítica.
  - **Chain of Responsibility:** la petición pasa por una cadena y cada eslabón decide si la atiende o la pasa al siguiente. Es el caso de Rechazada → Sospechosa → Opcionada.
- **A decidir:** el repo es del equipo (`iCastro117`). Se puede hacer un commit desde aquí o pasárselo a quien lo mantiene.

**D11. El reporte no se puede editar directamente**
- Se generó con Python y ReportLab (pág. 32), pero el script no está ni en la carpeta ni en el repo.
- **Recomendación:** pedir el script a quien generó el reporte. Si no aparece, reconstruirlo a partir del texto del PDF.
- **Qué hay que actualizar:**
  - agregar las páginas 3a, 3b y 6;
  - pasar de "ocho páginas" a 11 (pág. 2);
  - incluir las correcciones de esta sección.

**D12. El reporte promete una demo que no existe**
- **Fuente:**
  - Pág. 3: *"una demostración ejecutable reproduce los valores de la ventana 19 (COT-018, +4,81 %, OPCIONADA)"*.
  - Pág. 32: *"verificó que el código compila, que la demostración reproduce los valores…"*.
  - En el repo, las 156 clases están vacías.
- **A decidir:** implementar la demo o corregir el reporte. Se recomienda implementar al menos ms-cotizaciones, con la fachada y la cadena de reglas, porque es el ejemplo central del documento.

### 5.5 Resumen de lo que hay que decidir

| Qué | Quién decide | Propuesta |
|---|---|---|
| E1, E2, E3 | Nadie: son errores | Corregir |
| D1, D2, D4, D5, D6, D8 | Mauricio / equipo | Aceptar las recomendaciones |
| D3 | Mauricio / equipo | Estado **CON ORDEN** |
| D7, D9 | Equipo | Incluir HU-15 en ms-cotizaciones; definir los filtros de HU-07 |
| D10 | Equipo (el repo está en la cuenta `iCastro117`) | Corregir el README |
| D11 | Quien generó el reporte | Conseguir el script de ReportLab |
| D12 | Equipo | Implementar la demo de ms-cotizaciones |

---

## 6. Orden de trabajo recomendado

1. **Corregir el `.drawio`:** E1–E3 y las decisiones aceptadas de diseño (D1–D6 y D8). Son ajustes de texto, flechas y un puerto.
2. **Decidir el alcance** de HU-07, HU-15 y HU-19 con el equipo.
3. **Código y demo:** como mínimo ms-cotizaciones, que reproduzca COT-018 (+4,81 % → OPCIONADA).
4. **Regenerar el reporte una sola vez**, con las páginas nuevas, las correcciones y lo que realmente haga el código.

---

## 7. Glosario rápido

| Término | Qué significa aquí |
|---|---|
| **Contenedor (C4)** | Algo que se despliega o se ejecuta por separado: una aplicación, un servicio o una base de datos. No es lo mismo que un contenedor Docker |
| **Componente (C4)** | Una agrupación de clases dentro de un contenedor, con una responsabilidad clara |
| **API Gateway** | La única puerta de entrada desde afuera. Valida el JWT y envía cada ruta `/api/…` a su microservicio |
| **Ruta del gateway** | Un prefijo como `/api/pedidos/**` que indica a qué servicio va la petición. Un servicio sin ruta no es alcanzable desde afuera |
| **Kafka / tópico / evento** | Kafka es el buzón compartido. Un tópico es un canal temático, como `orden.generada`. Un evento es un mensaje corto que avisa que algo pasó, sin esperar respuesta |
| **Hexagonal / puerto / adaptador** | El dominio queda en el centro. Un puerto es la interfaz con la que el dominio pide algo afuera; un adaptador es la implementación concreta (JPA, Kafka, REST, OpenPDF) |
| **JDBC** | El conector de Java para bases de datos relacionales (SQL). No sirve para Redis |
| **Redis** | Una caché en memoria, de tipo clave-valor. Aquí guarda los precios del Ministerio para no consultarlo cada vez (patrón Proxy) |
| **Zona de disponibilidad** | Un centro de datos físico separado dentro de la misma región de la nube. Si se cae una zona, las demás siguen |
| **WebSocket** | Una conexión que el navegador deja abierta para que el servidor le empuje avisos, como la alarma de stock bajo |
| **Claim check** | Guardar el archivo pesado aparte y enviar en el mensaje solo la referencia de dónde está |
| **SPA** | Una aplicación web de una sola página (React) que corre en el navegador y habla con la API |
| **BAM** | Business Activity Monitoring: el tablero de indicadores del director comercial |
