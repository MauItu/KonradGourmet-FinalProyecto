# Validación de la Fase 2a (diagramas de clases 4a–4e) · 2026-11-07

Validó el agente `arquitecto-revisor` sobre la rama `correcciones-revision`. Los hallazgos nuevos se numeran N1–N15 para no confundirlos con H1–H26 de `REVISION_2026-11-07.md`.

**Veredicto: lista con correcciones.**
- 32 de las 34 correcciones de la tabla de la Fase 2a quedaron resueltas.
- Quedan parciales H2 (N2) y DEC-05 (N3).
- El revisor no daría el OK hasta cerrar N1, N2 y N3.

## Lo acordado se respetó

- **Microservicios y tópicos:** siguen 11 y 10. Solo hay tres consumidores nuevos:
  - ms-inventario ← `menu.actualizado`;
  - ms-inventario ← `solicitud.registrada`;
  - ms-cotizaciones ← `orden.generada`.
- **Páginas sin tocar:** la 1, 2, 3a, 3b, 5 y 6 son idénticas a `main`, celda por celda. Cada commit de la fase solo toca las páginas 4a–4e del `.drawio`.
- **Copia original:** `Arquitectura_Konrad_GourmetOriginalSinCorregir.drawio` conserva su SHA-256 (`10a49edd…`).
- **Revisión visual:** sin solapes, sin cajas fuera de su paquete y sin textos cortados.

## Hallazgos nuevos

| # | Sev. | Dónde | Qué está mal | Corrección mínima que propone el revisor |
|---|---|---|---|---|
| N1 | Alta | 4b: `Plato`, `MenuActualizadoEvent`, `PlatoCatalogo` y `catalogo()` | El plato no tiene sucursal, pero HU-04 pide "asociar automáticamente el plato a mi sucursal y a mi usuario" y "nombre único por sucursal". Así, el catálogo de V03 no se puede filtrar por sucursal | Agregar `sucursalId` y `creadoPor` a `Plato`, `existeNombre(sucursalId, nombre)`, `sucursalId` al evento y a `PlatoCatalogo`, y cambiar a `catalogo(sucursalId)` |
| N2 | Media | 4b: `InventarioRepositoryPort` y `RecetaPlato` | H2 quedó a medias: lo acumulado no se guarda por sucursal, y la receta llega sin unidad de medida | Agregar `acumular(sucursalId, consumo)` y `consumoPendiente(sucursalId)`, y una nota: "la receta viaja en la unidad del insumo" |
| N3 | Media | 4c: `registrarCotizacion(...)` y `CotizacionEnviadaEvent.valorSugerido` | Con varias solicitudes por cotización, cada solicitud recibiría el valor total. Además, `reenviar()` lo sumaría dos veces | Cambiar a `preciosSugeridos: Map<Long, BigDecimal>` y a `registrarCotizacion(numero, cotizacionNumero, precios)`, idempotente por cotización |
| N4 | Media | 4d ms-notificaciones | Recibe `pdfUrl`, pero no tiene cómo leer el PDF del almacén | Agregar `AlmacenArchivosPort.obtener()` en ms-notificaciones |
| N5 | Media | Eventos de 4b/4c/4e; nota `n_topicos` | ms-notificaciones no sabe a qué correo del jefe escribir, y `StockBajoEvent` no trae el producto, la cantidad restante ni la unidad (HU-16) | Agregar `correoSolicitante` a los eventos de solicitud y nombre, stock y unidad a `StockBajoEvent`. Los destinatarios se guardan en una tabla propia de ms-notificaciones |
| N6 | Media | 4a `/api/usuarios`; 4c `/api/proveedores` | Son dos prefijos nuevos: el gateway pasaría de 11 a 13 rutas | Decidir: 13 prefijos, o meterlos bajo `/api/auth` y `/api/cotizaciones` |
| N7 | Media | 4e `ParametrosSistema` | El Singleton solo vive en memoria: con 2 a 4 réplicas, cada una puede tener un tema distinto | Agregar `ParametrosRepositoryPort` con su adaptador JPA; el Singleton queda como caché |
| N8 | Media | 4d: los 3 decoradores de `Correo` | Después de H25 quedan en el dominio, pero implementan un puerto de salida | Pasarlos a `adaptadores.salida` |
| N9 | Baja | 4a `n_outbox` | La nota describe un outbox, pero el único adaptador publica directo en Kafka | Decir que `KafkaEventPublisher` escribe en la tabla outbox y que un relay publica |
| N10 | Baja | 4a `registrar(u: Usuario, clave)` | Recibe una clase abstracta y devuelve el hash | Cambiar a `registrar(nombre, correo, rol, sucursalId, clave)`, sin devolver el hash |
| N11 | Baja | 4b `alertaAtendida` | Nada la vuelve a `false` | Nota: `descontar()` la reactiva al volver a cruzar el umbral |
| N12 | Baja | 4d `OrdenCompra.cotizacionNumero` | Revisar con `existePorCotizacion()` y luego guardar no es atómico | Marcar el atributo `{único}` |
| N13 | Baja | 4d `EnvioRepositoryPort` | No guarda si el envío falló | Agregar `estado` (ENVIADO/FALLIDO) y una nota sobre la cola de errores (DLQ) |
| N14 | Baja | 4c `ProveedorRepositoryPort`; 4b `PlatoRestController` | Falta `todos()`, y las rutas de editar y desactivar platos no dicen el verbo | Agregar `todos()` y «PUT …/{id} · PATCH …/{id}/desactivar» |
| N15 | Baja | Leyendas | No explican que «extends EventoDominio» escrito dentro de la caja apunta a la 4a | Agregar una línea a la leyenda |

## Para propagar en otras sesiones (no estaba en el plan)

**Sesión 2**
- La auditoría la publican los otros 10 servicios: ms-administracion escribe directo en su bitácora. Cambia en la p1 (`e242`), la 3a (`c3a_e41`) y la p6 (`c6_v132`).
- La 3a necesita dos flechas nuevas:
  - ms-administracion → ELK `[HTTPS]` (H14);
  - ms-notificaciones → almacén de objetos (N4).
- En la p6, pasos 5 y 12, el PDF sale del almacén.
- Los adaptadores nuevos van en los hexágonos de la p1.
- Rutas nuevas:
  - `/api/usuarios` (DEC-10);
  - `GET /api/admin/parametros/tema`;
  - `POST /api/cotizaciones/{id}/reenvio`;
  - las de platos;
  - lo que se decida en N6.

**Sesión 3**
- Hay 29 clases nuevas: pasan de 155 a 184, más la demo.
- Se repiten tres nombres en dos paquetes: `AlmacenArchivosPort`, `AlmacenObjetosAdapter` y `GeneradorPdfPort`.
- Ahora hay 62 flechas `implements`/`extends`, antes 53, más 18 escritas dentro de las cajas. Ya no sirve la cifra de "33 que faltan".
- Por DEC-09, H8 queda en 5 tipos que no coinciden, no en 6.

**Sesión 5**
- Reporte, p. 21: la auditoría pasa de "los 11 servicios" a 10.
- El mockup V21 (UX p. 28) también muestra una sola solicitud. Se suma a V13 en la lista de DEC-05.

**Corrección al plan:** H10 de `ConsultarPrecioService` está en la 4e, no en la 4d.

## Seguimiento (2026-11-07)

**Decisiones de Mauricio:**
- **DEC-11:** N1 se corrige ya.
- **DEC-12:** N6 se resuelve con 13 prefijos.
- **DEC-13:** N5 queda mixto: el correo del solicitante viaja en el evento, y ms-notificaciones guarda una libreta de jefes por sucursal.
- **DEC-14:** se aplica todo lo demás.

| # | Estado | Commit |
|---|---|---|
| N1 | Aplicada | `N1: el plato pertenece a una sucursal…` |
| N2 | Aplicada | `N2: ms-inventario acumula el consumo por sucursal…` |
| N3 | Aplicada. El evento lleva `precios: Map<Long, BigDecimal>` (precio unitario sugerido); la solicitud guarda `cotizacionesAplicadas` y responde `yaAplicada()` | `N3: …` |
| N4 | Aplicada. `CorreoSmtpAdapter` usa `AlmacenArchivosPort.obtener()` de ms-notificaciones | `N4: …` |
| N5 | Aplicada. `DestinatarioRestController` en `/api/notificaciones/destinatarios`, dentro del prefijo de ms-notificaciones | `N5: …` |
| N6 | Decidida (DEC-12). No cambia las páginas 4a–4e: se aplica en las sesiones 2 y 5 | — |
| N7 | Aplicada. Para hacer espacio, ms-analitica se corrió 380 px a la derecha | `N7: …` |
| N8–N15 | Aplicadas | un commit cada una |

Clases en 4a–4e: 191 (antes 155).

## Segunda validación (2026-11-07)

**Veredicto del revisor: la fase 2a está lista para el OK.**
- Resueltos: los 14 hallazgos que tocaban 4a–4e.
- N6 no aplica a estas páginas (DEC-12).
- No entró ningún error de severidad media o mayor.
- Se sigue respetando lo acordado:
  - 11 servicios y 10 tópicos;
  - las páginas 1, 2, 3a, 3b, 5 y 6, idénticas a `main`;
  - el original, intacto.
- Cifras: 191 clases y 65 flechas `implements`/`extends`, más 18 relaciones escritas dentro de las cajas.

El revisor dejó dos detalles opcionales de severidad baja. Se aplicaron como ajustes de N2 y N3 (DEC-14):
- **N2 (ajuste):** `sucursalesConConsumo()` en el puerto de inventario, para que el descuento diario sepa qué sucursales tienen consumo pendiente.
- **N3 (ajuste):** `yaAplicada(cotizacion)` usa el mismo nombre de parámetro que `registrarCotizacion`.

También comentó que, en N4, se puede defender que `CorreoSmtpAdapter` use `AlmacenArchivosPort`: la dependencia apunta hacia una interfaz del núcleo. Si el profesor lo cuestiona, la alternativa es que `NotificacionService` lea el PDF y el `Mensaje` lleve los bytes.
