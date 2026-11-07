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

### Fase 2a · Diagramas de clases 4a–4e
Cada cambio en las páginas de clases arrastra el código de la sesión 3, que debe quedar igual al diagrama.

| Página | Correcciones |
|---|---|
| **4a** · identidad y transversal | H9 (rotular `EventPublisherPort` como Publish-Subscribe), H10 (`EventPublisherPort` en `IniciarSesionService`), H11 (`EventoDominio` con `id()`, `usuario()` y `correlacionId()`, más una nota del outbox), H15 (`CifradorPort.cifrar()` y la regla de 8+ caracteres), H22 (leyenda) |
| **4b** · pedidos, inventario y menú | H2 (líneas `(platoId, cantidad)` en `PedidoRegistradoEvent`; ms-inventario consume `menu.actualizado` y guarda las recetas), H3 (`actualizarCatalogo()` y un puerto del catálogo persistido), H6 (`Mesa`, `GET /api/pedidos/catalogo` y `GET /api/pedidos/mesas`), H13 (el umbral es de ms-inventario), D4 (ms-inventario consume `solicitud.registrada` y atiende la alerta), D5 (`editar()` y `desactivar()` de platos), H22 |
| **4c** · solicitudes y cotizaciones | DEC-05 (`solicitudNumeros: List` y la validación "al menos una"), H5 (`detalle(numero)` y `GET /api/cotizaciones/{id}`), H8 (`cadenaPorDefecto()`), H12 (`completar(numero)` para todas las solicitudes), H18 (`PENDIENTE_PRECIO` y `reenviar()`), D1 (`AlmacenArchivosPort` y `pdfUrl`), D3 (estado `CON ORDEN` y consumo de `orden.generada`), DEC-07/D7 (`Proveedor` completo y `ProveedorRestController /api/proveedores/**`), H22 |
| **4d** · órdenes, integración y notificaciones | H4 (`GeneradorPdfPort` y almacén en ms-ordenes; `OrdenGeneradaEvent` con `correoProveedor`, `pdfUrl` y `cotizacionNumero`), D3 (rechazar una segunda orden sobre la misma cotización), H10 (`EventPublisherPort` en `NotificacionService` y `ConsultarPrecioService`), H25 (`Correo` en `puertos.salida`), E3 (6 tópicos), D6 (EN TRÁMITE por correo), H22 |
| **4e** · administración y analítica | H7 (`kpis(desde, hasta, sucursalId)`), H10 (ms-administracion escribe directo en su bitácora, con una nota), H13 (`GET /api/admin/parametros/tema`; el umbral sale de `ParametrosSistema`), H14 (`LogsPort` y su adaptador de Elasticsearch), DEC-07/D8 (`SugerenciaPromocion`, histórico, aceptar o descartar, y un `tendencia.detectada` al día), H22 |

Al terminar: exportar las 5 páginas a PNG para revisarlas, y validar con `arquitecto-revisor`.

## Sesión 2 · `.drawio`, páginas 1, 2, 3a, 3b, 5 y 6

Propagar lo de la fase 2a a las vistas de arquitectura y corregir lo propio de cada página.

| Página | Correcciones |
|---|---|
| **1** · Mapa integrado | E1 (Redis · TLS), E2 (3 zonas), D2 (11 rutas en el gateway), nuevos consumidores: ms-inventario ← `menu.actualizado` y `solicitud.registrada`, ms-cotizaciones ← `orden.generada`. Rutas nuevas: proveedores, catálogo y mesas, detalle de cotización, sugerencias |
| **2** · Contexto | H22 (leyenda, y `[SMTP]` en la flecha correo → proveedor) |
| **3a** · Contenedores | H17 (Eureka `[HTTPS · mTLS]`), H22 (protocolo en las flechas de Kafka, `[SMTP]`), D2 (ruta de ms-notificaciones; el SOAP externo entra por el gateway), H4 (PDF de órdenes), los consumidores nuevos |
| **3b** · Componentes de ms-cotizaciones | H5 (consulta por número), D1 (almacén y `pdfUrl`), DEC-05 (varias solicitudes), DEC-07 (proveedores), H18 (reenvío), D3 (consumo de `orden.generada` → CON ORDEN) |
| **5** · Despliegue | H16 (réplica y respaldo del almacén de objetos), H24 (3 sub-cajas de zona y réplica de Redis), H22 (leyenda) |
| **6** · Dinámico | H23 (numerar 4.1, 4.2…), DEC-05 (varias solicitudes en los pasos de cotización), H4 (PDF de la orden en el paso 12), H12 (las solicitudes pasan a COMPLETADA), D3 (CON ORDEN) |

Al terminar: exportar las páginas a PNG y validar con `arquitecto-revisor`.

## Sesión 3 · Código: firmas, relaciones y tipos

En `KonradGourmet/`, rama `correcciones-revision`. El código debe quedar idéntico al `.drawio` corregido.
- **H1:** declarar en cada controlador los métodos de la §15 del reporte, con el comentario `// Atiende la ventana Vxx`. Hacer lo mismo con los métodos de casos de uso, puertos y dominio de 4a–4e; los cuerpos lanzan `UnsupportedOperationException` hasta la sesión 4.
- **H1:** agregar las 33 relaciones `implements`/`extends` que faltan.
- **H8:** corregir los 6 tipos de clase (`ReglaValidacion` abstracta con `siguiente` y `enlazar()`, `Correo` como interfaz, etc.).
- Agregar las clases nuevas de la fase 2a (`Mesa`, `SugerenciaPromocion`, `LogsPort`, `AlmacenArchivosPort`, etc.) y mover `Correo` a `puertos.salida` (H25).
- **H19 y D10:** la tabla de patrones del `README.md`, igual a la p. 22 del reporte.
- Compilar con `javac` sin errores, y anotar el nuevo total de clases para la lista del reporte.

Al terminar: validar con `arquitecto-revisor`.

## Sesión 4 · Código: demo real

- **D12:** `DemoArquitectura` recorre el flujo de compra con adaptadores en memoria y sin frameworks. Pasa por solicitudes, cotización COT-018, registro de valores, cadena de reglas y orden OC-0012.
- Debe reproducir los valores de V19: $520.000, $545.000, +4,81 % → **OPCIONADA**. También muestra los casos +34,62 % → RECHAZADA y −61,54 % → SOSPECHOSA.
- Implementar con lógica real la fachada, la cadena de reglas (`cadenaPorDefecto()`) y lo que use la demo. Debe respetar DEC-05 (lista de solicitudes) y D3 (CON ORDEN).
- Agregar pruebas simples que no necesiten Maven, más un script para compilar y ejecutar.

Al terminar: correr la demo y validar con `arquitecto-revisor`.

## Sesión 5 · Excel + documentos de cierre

- **Fase 4, Excel (H20):** 1 PHU = 25 h y 9 PHU por sprint. HU-14 = 1, con un total de 35. Pasar HU-17 al sprint 3. Corregir "EN SEMANAS" y "SOBRAN". Agregar fechas por sprint.
- **Fase 6, documentos:**
  - Actualizar `ESTADO_PROYECTO_Y_C4.md`: rutas nuevas (`documentos/`), Git, cambios hechos y estado de cada pendiente.
  - **`CORRECCIONES_REPORTE.md`:** todo lo que hay que cambiar en el reporte cuando aparezca el script (DEC-03). Incluye:
    - H1 (ya cierto) y D12;
    - "ocho" → 11 páginas, y agregar las páginas 3a, 3b y 6;
    - §11 (consumidores nuevos), §15 (métodos y rutas nuevas) y p. 22 (H8, H9, H13);
    - H21, H25 y el total de clases.
  - **`CORRECCIONES_HU_Y_MOCKUPS.md`:**
    - el mockup V13 con varias solicitudes (DEC-05);
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
| 1 · Fase 2a (clases 4a–4e) | Pendiente |
| 2 · Páginas 1, 2, 3a, 3b, 5 y 6 | Pendiente |
| 3 · Código: firmas | Pendiente |
| 4 · Código: demo | Pendiente |
| 5 · Excel y documentos | Pendiente |
