# Plan de correcciones · Konrad Gourmet

Este plan aplica las correcciones de la revisión del 2026-11-07 (`REVISION_2026-11-07.md`) siguiendo las decisiones de `DECISIONES.md`. Está dividido en 5 sesiones (DEC-04); cada sesión empieza leyendo estos tres archivos.

## Reglas de trabajo

- **Dos repos locales, rama `correcciones-revision` en los dos:**
  - `FinalProyecto/documentos/`: `.drawio`, Excel, PDF y archivos `.md`.
  - `FinalProyecto/KonradGourmet/`: código; el push está desactivado.
- **`main` no se toca** en ninguno de los dos: es el punto de retorno.
- **Todo es local:** nada de push ni de fetch.
- **Un commit por corrección**, con el id al inicio del mensaje. Ejemplos: `H17: Eureka con HTTPS y mTLS en la 3a`, `DEC-05: cotización con varias solicitudes en la 4c`.
- **No se modifican:**
  - el reporte PDF (DEC-03);
  - `Arquitectura_Konrad_GourmetOriginalSinCorregir.drawio`;
  - el PDF de UX/UI y el del flujo web;
  - el Story Mapping, que está fuera de `FinalProyecto`.
  
  Lo que haya que cambiar en ellos se anota en la sesión 5.
- **Se mantienen los 11 microservicios y los 10 tópicos.** Solo se agregan consumidores, rutas, clases y métodos.
- **Pausa por fase:** al terminar cada fase se muestra el resultado y se espera el OK de Mauricio. Al final de cada sesión, el agente `arquitecto-revisor` valida lo hecho.
- **Ante cualquier duda, preguntar antes de cambiar.**

### Cómo deshacer

```bash
cd FinalProyecto/documentos            # o FinalProyecto/KonradGourmet
git log --oneline                      # ver los cambios, uno por commit
git revert <commit>                    # deshacer uno solo, dejando registro
git switch main                        # ver el estado original completo
git switch correcciones-revision       # volver al trabajo
```

---

## Sesión 1 · Git + `.drawio`, diagramas de clases 4a–4e

### Fase 1 · Montar Git ✅ (2026-11-07)
- [x] Mover los 7 documentos a `documentos/` (los SHA-256 no cambiaron)
- [x] `git init` en `documentos/`, con el commit inicial en `main` y la rama `correcciones-revision`
- [x] Rama `correcciones-revision` en `KonradGourmet` y push desactivado
- [x] `REVISION_2026-11-07.md`, `DECISIONES.md` y este plan

### Fase 2a · Diagramas de clases 4a–4e ✅ (2026-11-07)
Cada cambio en las páginas de clases arrastra el código de la sesión 3, que debe quedar igual al diagrama.

| Página | Correcciones |
|---|---|
| **4a** · identidad y transversal | H9 (rotular `EventPublisherPort` como Publish-Subscribe), H10 (`EventPublisherPort` en `IniciarSesionService`), H11 (`EventoDominio` con `id()`, `usuario()` y `correlacionId()`, más una nota del outbox), H15 (`CifradorPort.cifrar()` y la regla de 8+ caracteres), H22 (leyenda) |
| **4b** · pedidos, inventario y menú | H2 (líneas `(platoId, cantidad)` en `PedidoRegistradoEvent`; ms-inventario consume `menu.actualizado` y guarda las recetas), H3 (`actualizarCatalogo()` y un puerto del catálogo persistido), H6 (`Mesa`, `GET /api/pedidos/catalogo` y `GET /api/pedidos/mesas`), H13 (el umbral es de ms-inventario), D4 (ms-inventario consume `solicitud.registrada` y atiende la alerta), D5 (`editar()` y `desactivar()` de platos), H22 |
| **4c** · solicitudes y cotizaciones | DEC-05 (`solicitudNumeros: List` y la validación "al menos una"), H5 (`detalle(numero)` y `GET /api/cotizaciones/{id}`), H8 (`cadenaPorDefecto()`), H12 (`completar(numero)` para todas las solicitudes), H18 (`PENDIENTE_PRECIO` y `reenviar()`), D1 (`AlmacenArchivosPort` y `pdfUrl`), D3 (estado `CON ORDEN` y consumo de `orden.generada`), DEC-07/D7 (`Proveedor` completo y `ProveedorRestController /api/proveedores/**`), H22 |
| **4d** · órdenes y notificaciones | H4 (`GeneradorPdfPort` y almacén en ms-ordenes; `OrdenGeneradaEvent` con `correoProveedor`, `pdfUrl` y `cotizacionNumero`), D3 (rechazar una segunda orden sobre la misma cotización), H10 (`EventPublisherPort` en `NotificacionService`), H25 (`Correo` en `puertos.salida`), E3 (6 tópicos), D6 (EN TRÁMITE por correo), H22 |
| **4e** · integración, administración y analítica | H7 (`kpis(desde, hasta, sucursalId)`), H10 (`EventPublisherPort` en `ConsultarPrecioService`; ms-administracion escribe directo en su bitácora, con una nota), H13 (`GET /api/admin/parametros/tema`; el umbral sale de `ParametrosSistema`), H14 (`LogsPort` y su adaptador de Elasticsearch), DEC-07/D8 (`SugerenciaPromocion`, histórico, aceptar o descartar, y un `tendencia.detectada` al día), H22 |

Al terminar: exportar las 5 páginas a PNG para revisarlas, y validar con `arquitecto-revisor`.

**Validación de la fase 2a:** el revisor encontró 15 hallazgos nuevos (N1–N15, en `VALIDACION_FASE2A.md`).
- Se decidieron DEC-11 a DEC-14.
- Todos se aplicaron, salvo N6, que no cambia las páginas 4a–4e: se aplica en las sesiones 2 y 5.
- Con eso hay 191 clases en 4a–4e (antes 155).

## Sesión 2 · `.drawio`, páginas 1, 2, 3a, 3b, 5 y 6

Propagar lo de la fase 2a a las vistas de arquitectura y corregir lo propio de cada página.

| Página | Correcciones |
|---|---|
| **1** · Mapa integrado | E1 (Redis · TLS), E2 (3 zonas), D2 y DEC-12 (13 prefijos en el gateway: los 11 de antes más `/api/usuarios` y `/api/proveedores`), H10 (la auditoría la publican los otros 10 servicios; ms-administracion escribe directo), los adaptadores nuevos de 4a–4e en cada hexágono, nuevos consumidores: ms-inventario ← `menu.actualizado` y `solicitud.registrada`, ms-cotizaciones ← `orden.generada`. Rutas nuevas: usuarios, proveedores, catálogo y mesas, detalle y reenvío de cotización, tema, sugerencias, destinatarios |
| **2** · Contexto | H22 (leyenda, y `[SMTP]` en la flecha correo → proveedor) |
| **3a** · Contenedores | H17 (Eureka `[HTTPS · mTLS]`), H22 (protocolo en las flechas de Kafka, `[SMTP]`), D2 (ruta de ms-notificaciones; el SOAP externo entra por el gateway), H4 (PDF de órdenes), los consumidores nuevos, H14 (ms-administracion → ELK `[HTTPS]`), N4 (ms-notificaciones → almacén de objetos), H10 (auditoría de 10 servicios), DEC-12 |
| **3b** · Componentes de ms-cotizaciones | H5 (consulta por número), D1 (almacén y `pdfUrl`), DEC-05 (varias solicitudes), DEC-07 (proveedores), H18 (reenvío), D3 (consumo de `orden.generada` → CON ORDEN) |
| **5** · Despliegue | H16 (réplica y respaldo del almacén de objetos), H24 (3 sub-cajas de zona y réplica de Redis), H22 (leyenda) |
| **6** · Dinámico | H23 (numerar 4.1, 4.2…), DEC-05 (varias solicitudes en los pasos de cotización), H4 (PDF de la orden en el paso 12), H12 (las solicitudes pasan a COMPLETADA), D3 (CON ORDEN), N4 (el PDF sale del almacén en los pasos 5 y 12), H10 (auditoría) |

Al terminar: exportar las páginas a PNG y validar con `arquitecto-revisor`.

**Estado (2026-11-08):** aplicada, validada por el revisor y aprobada por Mauricio, que pidió seguir con la sesión 3.
- El inventario previo del revisor (`INVENTARIO_SESION2.md`, 149 ítems con el id de cada celda) y las decisiones DEC-15 a DEC-22 guiaron los cambios.
- No se aplicaron 6 ítems opcionales (DEC-21): P1-08, P1-09, P1-13, P1-29, P2-07 y P3b-26. P5-13 no cambia nada, porque DEC-17 mantiene «PostgreSQL ×11».
- Cifras del mapa: 11 servicios, 10 tópicos, 19 flechas de consumo, 10 de publicación y 11 rutas del gateway con los 13 prefijos.
- Validación (`VALIDACION_SESION2.md`): S1–S11 en la primera ronda y S12–S14 en la segunda. Todos están aplicados. El arquitecto decidió S5 (DEC-23) y S9 (DEC-24) a pedido de Mauricio.
- PNG de las 6 páginas en `FinalProyecto/exportes/sesion2/`, fuera del repo.

## Sesión 3 · Código: firmas, relaciones y tipos

En `KonradGourmet/`, rama `correcciones-revision`. El código debe quedar idéntico al `.drawio` corregido.
- **H1:** declarar en cada controlador los métodos de la §15 del reporte, con el comentario `// Atiende la ventana Vxx`. Hacer lo mismo con los métodos de casos de uso, puertos y dominio de 4a–4e; los cuerpos lanzan `UnsupportedOperationException` hasta la sesión 4.
- **H1:** agregar las relaciones `implements`/`extends` de 4a–4e: 65 flechas en el diagrama, más 18 escritas dentro de las cajas. La cifra de 33 que daba la revisión ya no sirve: hay que contar de nuevo contra el código.
- **H8:** corregir los 5 tipos de clase (`ReglaValidacion` abstracta con `siguiente` y `enlazar()`, `Correo` como interfaz, etc.). `EventoDominio` ya es abstracta en el código y así quedó en el diagrama (DEC-09).
- Agregar las 36 clases nuevas de la fase 2a (`Mesa`, `SugerenciaPromocion`, `LogsPort`, `AlmacenArchivosPort`, etc.) y mover `Correo` a `puertos.salida` (H25).
  - Quedan 191 clases más la demo.
  - `AlmacenArchivosPort`, `AlmacenObjetosAdapter` y `GeneradorPdfPort` se repiten en varios paquetes: hay que comparar por nombre completo.
- **H19 y D10:** la tabla de patrones del `README.md`, igual a la p. 22 del reporte.
- **DEC-22 (4e):** `PrecioReferenciaCacheProxy` tiene `cache: Map`, una caché en la memoria de cada réplica. Corregirlo en la 4e y en el código para que la caché viva en Redis, como dicen las demás páginas y el reporte.
- **D2 (4e):** anotar que el `@WebService` de `AbastecimientoSoapEndpoint` se publica bajo `/api/integracion/**`, el prefijo 13 del gateway, que la 4e no escribe.
- Compilar con `javac` sin errores, y anotar el nuevo total de clases para la lista del reporte.

Al terminar: validar con `arquitecto-revisor`.

**Avance (2026-11-08):**
- **Inventario previo:** `INVENTARIO_SESION3.md`, del revisor. Faltan 40 clases por nombre completo (36 nuevas y 4 en otro paquete), 5 tienen el tipo equivocado (H8) y hay 714 miembros por declarar. De las 82 relaciones (65 flechas y 17 escritas en las cajas, no 18), el código tiene bien 19.
- **Decisiones DEC-25 a DEC-32**, que siguen la recomendación del revisor:
  - constructores ya, con la convención en el README;
  - «Atiende la ventana Vxx de los mockups»;
  - `{Redis}` en la caché del Proxy;
  - el dominio de notificaciones se deja como está;
  - regla de imports entre servicios;
  - los adaptadores en memoria, tal cual;
  - la demo cubre las 7 salidas del reporte (sesión 4);
  - `/api/integracion/**` en el estereotipo del endpoint SOAP.
- **Cómo se trabaja:**
  - un commit por clase, y uno por familia cuando la clase y sus subclases solo compilan juntas;
  - los cambios de paquete con `git mv`, en commits propios;
  - se compila con `javac --release 21`;
  - la historia de `Correo.java` y `CorreoDecorator.java` se ve con `git log --follow -M30%`, porque con el umbral normal git no reconoce su movimiento (DEC-34). También está anotado en el README de `KonradGourmet`.

**Resultado (2026-11-08):**
- **4e** (`documentos/`, 2 commits): `{Redis}` en `PrecioReferenciaCacheProxy.cache` y la nota `n_cache` con su conector `en_cache` (DEC-27); `/api/integracion/**` en el estereotipo de `AbastecimientoSoapEndpoint` (DEC-32). Las demás páginas no cambiaron.
- **Código** (`KonradGourmet/`, 176 commits sobre `ad71e69`, y cada uno compila):
  - 2 movimientos con `git mv`: H25 y N8;
  - 5 cambios de tipo (H8);
  - 162 commits de contenido, uno por clase o por familia;
  - 7 commits del README: la tabla de los 10 patrones (H19 y D10), DEC-25, DEC-26, DEC-28, DEC-29, P-08 y P-09.
- **Validación** (`VALIDACION_SESION3.md`): el revisor la dio por lista con correcciones. El código es idéntico a 4a–4e y los 176 commits compilan. Encontró 8 hallazgos de texto (K1–K8), y todos se aplicaron (K5 se revirtió después, por K11):
  - K1 con DEC-33: la anotación de Spring en el Javadoc de los 54 adaptadores, 54 commits;
  - K4 con DEC-34: la nota de `-M30%`;
  - con eso, `KonradGourmet` tenía 235 commits sobre `ad71e69`.
- **Segunda validación** (`VALIDACION_SESION3.md`): K1–K8 resueltos y el código compilado no cambió. Encontró 5 hallazgos de texto (K9–K13), y todos están aplicados:
  - K9 con DEC-35: `@Primary` en el Proxy de precios;
  - K10 con DEC-36: `@SchedulerLock` (ShedLock) en los 2 jobs;
  - K11 con DEC-37: el README vuelve a «V13–V20» para ms-cotizaciones (revierte K5);
  - K12 y K13: redacción del README y de `DECISIONES.md`;
  - con eso, `KonradGourmet` tiene 240 commits sobre `ad71e69`.
- **Tercera validación** (`VALIDACION_SESION3.md`): K9–K13 bien aplicados, y el código compilado sigue igual. Dejó 3 hallazgos de texto en `documentos/` (K14–K16) y una duda: si ShedLock y `@Primary` deben ir también en el reporte o en un diagrama. Todo está aplicado: K14 y K16, DEC-38 (una frase para el reporte en la sesión 5) y la frase completa del README sobre la clase de configuración. `KonradGourmet` tiene 241 commits sobre `ad71e69`.
- **Cuarta validación** (`VALIDACION_SESION3.md`): **lista**. Dejó dos detalles de redacción: K17, una errata del registro que no se cambia, y K18, una frase de DEC-36, ya corregida.
- **Total de clases para el reporte:** 191, más `DemoArquitectura`, en 192 archivos. Antes eran 155 más la demo.
- **Cifras:**
  - compila con `javac --release 21 -Xlint:all` sin errores ni avisos;
  - 82 relaciones `extends`/`implements` y 56 constructores;
  - 29 comentarios de ventana, que cubren las 23 ventanas;
  - las 23 notas del diagrama, en el Javadoc de su clase.

## Sesión 4 · Código: demo real

- **D12:** `DemoArquitectura` recorre el flujo de compra con adaptadores en memoria y sin frameworks. Pasa por solicitudes, cotización COT-018, registro de valores, cadena de reglas y orden OC-0012.
- Debe reproducir los valores de V19: $520.000, $545.000, +4,81 % → **OPCIONADA**. También muestra los casos +34,62 % → RECHAZADA y −61,54 % → SOSPECHOSA.
- Implementar con lógica real la fachada, la cadena de reglas (`cadenaPorDefecto()`) y lo que use la demo. Debe respetar DEC-05 (lista de solicitudes) y D3 (CON ORDEN).
- Agregar pruebas simples que no necesiten Maven, más un script para compilar y ejecutar.

- **DEC-31:** la demo cubre las 7 salidas que promete el reporte (§13, p. 26): el menú del director de compras, el pedido de $82.000, COT-018/019/020, el correo con `X-Certificado`, el segundo precio desde la caché del Proxy, el Singleton y el KPI. El Singleton muestra un parámetro del tema, no el umbral (H13).
- Los constructores y accesores de entidades y eventos se agregan según lo que use la demo (DEC-25). Si la demo consume `cotizacion.validada` o `tendencia.detectada`, se resuelve T-06 (DEC-29).

- Al terminar, actualizar en el README qué métodos tienen lógica, qué imprime la demo y el comando de las pruebas (K8).

Al terminar: correr la demo y validar con `arquitecto-revisor`.

**Resultado (2026-11-08):**
- **Código** (`KonradGourmet/`, 126 commits sobre `37d655b`; cada uno compila con `-Xlint:all`, verificado uno por uno):
  - los 274 cuerpos «Pendiente de implementar» pasan a 94: lógica real en todo el recorrido de compra, de punta a punta, y en los 8 patrones; sigue pendiente lo que habla con tecnología externa y lo que la demo no usa (la tabla está en el README);
  - `DemoArquitectura` recorre un día en la sucursal Centro: SOL-031, COT-018/019/020 y OC-0012, con las 7 salidas del reporte (DEC-31): menú del director de compras, pedido de $82.000, correo con `X-Certificado` y `X-Sello-Tiempo` más la alarma «Res al 23 %», precio de la res desde el servicio web y después desde la caché, Singleton con un parámetro del tema, y Bandeja paisa (40) con Proveedor B. Reproduce $520.000, $545.000, +4,81 % → OPCIONADA, +34,62 % → RECHAZADA y −61,54 % → SOSPECHOSA;
  - DEC-05 (lista de solicitudes) y D3 (CON ORDEN y segunda orden rechazada) funcionan; T-06 (DEC-29) no se activó, porque la demo no consume `cotizacion.validada` ni `tendencia.detectada`;
  - 19 clases de apoyo en `demo/` (adaptadores en memoria, bus y `Ensamble`), que no cuentan entre las 191 del modelo; 211 archivos `.java` en `src/`;
  - `pruebas/` con 160 verificaciones, sin Maven ni JUnit, y `ejecutar.sh` (compila, corre la demo y corre las pruebas);
  - el README dice qué métodos tienen lógica, qué imprime la demo y cuál es el comando de las pruebas (K8).
- **Decisiones** (`DECISIONES.md`): DEC-39 a DEC-46, asumidas mientras Mauricio no estaba. Después las revisó: confirmó el pedido de $82.000, el precio por kilo, la solicitud parcial y la alarma, y **decidió agregar `adjuntoUrl`** a `Notificacion.enviar()` y `CreadorNotificacion.notificar()`. Ese cambio ya está en el código y **la página 4d queda por actualizar en la sesión 5** (dos contratos afectados, reflejados en cuatro declaraciones: `Notificacion`, `AlarmaApp`, `NotificacionCorreo` y `CreadorNotificacion`).
- **Validación** (`VALIDACION_SESION4.md`): el revisor la dio por **lista con correcciones**. S4-1 y S4-2 (lógica) y S4-3 a S4-12 están aplicados o anotados. Falta una segunda ronda del revisor sobre los arreglos.
- Los dos `.drawio` y el reporte PDF no se tocaron.

## Sesión 5 · Excel + documentos de cierre

- **Fase 4, Excel (H20):** 1 PHU = 25 h y 9 PHU por sprint. HU-14 = 1, con un total de 35. Pasar HU-17 al sprint 3. Corregir "EN SEMANAS" y "SOBRAN". Agregar fechas por sprint.
- **Fase 6, documentos:**
  - Actualizar `ESTADO_PROYECTO_Y_C4.md`: rutas nuevas (`documentos/`), Git, cambios hechos y estado de cada pendiente.
  - **`CORRECCIONES_REPORTE.md`:** todo lo que hay que cambiar en el reporte cuando aparezca el script (DEC-03). Incluye:
    - H1: ya es cierto en la p. 26 y la p. 30 (métodos, comentarios y relaciones); la p. 3 depende de la demo (D12);
    - "ocho" → 11 páginas, y agregar las páginas 3a, 3b y 6;
    - §11 (consumidores nuevos), §15 (métodos y rutas nuevas) y p. 22 (H8, H9, H13);
    - H21, H25 y el total de clases;
    - p. 21: la auditoría la publican 10 servicios, no 11 (H10);
    - DEC-43, confirmada por Mauricio: cambiar «Existencia ≤ 25 %» en la tabla de eventos (p. 21) y aclarar «llega al 25 %» (p. 9). Se compara el porcentaje redondeado a 2 decimales (`HALF_UP`) con **< 25 %**; la alarma sale solo al cruzar ese umbral (DEC-46). También se redondea antes de aplicar las reglas de cotizaciones.
    - DEC-42, límite confirmado por Mauricio: el reporte promete usuario en la auditoría (p. 5) y correlación para las trazas (p. 9). Aclarar que la demo usa `sistema` (o el correo de la entidad) y una correlación nueva por evento; no propaga la identidad autenticada ni una correlación común a toda la compra. Separar este límite de la arquitectura prevista para el sistema real.
    - p. 7: 13 prefijos en el gateway (DEC-12);
    - supuesto S5 y §15: el alta de usuarios (DEC-10) y las rutas nuevas;
    - p. 13, §9: el mapa muestra también el almacén de objetos, ELK y la BD PostgreSQL de ms-integracion (DEC-16 y DEC-17), y una caja de adaptador agrupa los de una misma tecnología (DEC-15);
    - tabla de eventos y tabla del recorrido: ms-inventario consume `solicitud.registrada` en el paso 2 y ms-cotizaciones consume `orden.generada` en el paso 12 (DEC-18 y D3);
    - despliegue: el reparto por zona, con Eureka y la SPA en las zonas A y B, está asumido y hay que confirmarlo con el equipo (DEC-20);
    - de la sesión 3 (`INVENTARIO_SESION3.md`):
      - p. 26: 191 clases, no 155 (P-12);
      - p. 22: «Publish-Subscribe», y el Singleton sin el umbral (P-10);
      - p. 22: «el esqueleto Java implementa el mismo comportamiento» pasa a «el esqueleto Java declara las mismas clases, firmas y relaciones, y la demo ejecuta los patrones» (K2, P-10);
      - §13: la salida del Singleton (P-11, DEC-31);
      - §15: faltan `catalogo()` y `mesas()` (W-03), `productosPorCategoria()` (W-06), el desplegable de proveedores (W-13) y `detalle()` de la cotización (W-20 y W-21);
      - opcional: una tabla con los métodos de controlador que no atienden una ventana (W-24 a W-35).
      - p. 26: junto a las anotaciones, una frase que diga que el Proxy de precios lleva `@Primary` y que los 2 jobs llevan `@SchedulerLock` (ShedLock), para que corran en una sola réplica (DEC-35, DEC-36 y DEC-38).
  - **Nota de la 4a, `n_outbox` (DEC-42):** aclarar que la promesa de usuario y de una correlación que une el recorrido corresponde al sistema real. La demo conserva el límite confirmado: `sistema` o correo de la entidad y una correlación por evento. No modificar la nota hasta la sesión 5.
  - **4d del `.drawio` (DEC-42):** agregar `adjuntoUrl: String` a `Notificacion.enviar()`, `AlarmaApp.enviar()`, `NotificacionCorreo.enviar()` y `CreadorNotificacion.notificar()`, para que el diagrama quede igual al código.
  - **`CORRECCIONES_HU_Y_MOCKUPS.md`:**
    - los mockups V13 y V21 con varias solicitudes (DEC-05);
    - reescribir la tarjeta HU-07 con los filtros de `CriteriosConsulta` (D9);
    - las etiquetas del Story Mapping (H26 y DEC-06);
    - mockups opcionales para HU-15 y HU-19 (DEC-07).

Al terminar: validación final de todo con `arquitecto-revisor`.

---

## Texto para arrancar cada sesión

Copiar en una sesión nueva de Claude Code abierta en `FinalProyecto/`, cambiando el número:

> Continúa las correcciones de Konrad Gourmet. Lee `documentos/PLAN_CORRECCIONES.md`, `documentos/DECISIONES.md` y `documentos/REVISION_2026-11-07.md`, y haz la **Sesión N**. Trabaja en la rama `correcciones-revision` de cada repo, un commit por corrección y todo local. Para al final de cada fase y espera mi OK; al terminar la sesión, pide al `arquitecto-revisor` que valide. Si tienes dudas, pregúntame antes de cambiar.

## Avance

| Sesión | Estado |
|---|---|
| 1 · Fase 1 (Git) | ✅ Hecha (2026-11-07) |
| 1 · Fase 2a (clases 4a–4e) | ✅ Aprobada por Mauricio (2026-11-07). Validada por el revisor, con N1–N15 corregidos (DEC-11 a DEC-14) |
| 2 · Páginas 1, 2, 3a, 3b, 5 y 6 | ✅ Aprobada por Mauricio (2026-11-08). Validada por el revisor, con S1–S14 corregidos. Decisiones DEC-15 a DEC-24 |
| 3 · Código: firmas | Aplicada y validada por el revisor cuatro veces (2026-11-08); la cuarta la dio por **lista**. K1–K18 resueltos (K5, revertida por K11; K15 y K17 son erratas anotadas en los registros). Falta el OK de Mauricio. Son 191 clases más la demo, que compilan, en 241 commits sobre `ad71e69`. Decisiones DEC-25 a DEC-38 |
| 4 · Código: demo | Aplicada y validada una vez por el revisor (2026-11-08): **lista con correcciones**, ya aplicadas (S4-1 a S4-12). 126 commits sobre `37d655b` más los del adjunto, 160 pruebas, la demo cubre las 7 salidas del reporte. Mauricio confirmó las decisiones principales (DEC-40, DEC-42 y DEC-46); queda por actualizar la 4d (`adjuntoUrl`) en la sesión 5. Falta su OK final y, si quiere, una segunda ronda del revisor |
| 5 · Excel y documentos | Pendiente |
