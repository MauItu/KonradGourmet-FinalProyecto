# Inventario previo de la Sesión 2 · 2026-11-07

Hecho por el agente `arquitecto-revisor` antes de cambiar nada. Compara las páginas 1, 2, 3a, 3b, 5 y 6 con las 4a–4e ya aprobadas (p3–p7) y con `PLAN_CORRECCIONES.md`.

- **N/P:** no estaba en el plan.
- **Opcional:** lo dice la misma fila.

## Página 1 · Mapa integrado (p1)

| # | Celda | Texto actual | Cambio propuesto | Origen | Respaldo en 4a–4e |
|---|---|---|---|---|---|
| P1-01 | `e202` | `JDBC` | `Redis · TLS` | E1 | p9 `c3a_e20` ya dice `[Redis · TLS]` |
| P1-02 | `v51` | `[Kubernetes · 2 zonas + centro de datos alterno]` | `[Kubernetes · 3 zonas + centro de datos alterno]` | E2 | p8 `v10` |
| P1-03 | `e242` | `los 11 servicios publican cada CRUD (RNF auditoría)` | `los otros 10 servicios publican cada CRUD (RNF auditoría); ms-administracion escribe directo en su bitácora` | H10 | p7 `n_bitacora`; `cls_administracion_AdministracionService` no tiene `EventPublisherPort` |
| P1-04 | nueva `ad_identidad_t2` | — | `«REST» · entrada / UsuarioRestController`, con «usa puerto» → `hex_identidad`. La fila de arriba pasa a 3 cajas de 118 px | DEC-10 | p3 `cls_identidad_UsuarioRestController`, `e_h15_usa` |
| P1-05 | nueva: arista `gw → ad_identidad_t2` y su etiqueta | — | `/api/usuarios/**`, con el mismo estilo que `v208` | DEC-12 | p3 `cls_identidad_UsuarioRestController` |
| P1-06 | `hex_identidad` | `IniciarSesionUseCase · Usuario.construirMenu()` | `IniciarSesionUseCase · RegistrarUsuarioUseCase · Usuario.construirMenu()` | DEC-10 | p3 `cls_identidad_RegistrarUsuarioUseCase` |
| P1-07 | nueva `ad_pedidos_t1`, o agrupada en `ad_pedidos_b1` | — | `«JPA» · salida / CatalogoJpaAdapter`, con «implementa puerto» y JDBC → `db_pedidos`. Si se agrupa: `PedidoJpaAdapter / CatalogoJpaAdapter` | H3 | p4 `cls_pedidos_CatalogoJpaAdapter`, `e_h3_impl` |
| P1-08 | `hex_pedidos` | `RegistrarPedidoUseCase · pedidosDelTurno()` | Agregar `· catalogo() · mesas()` (opcional) | H6 | p4 `cls_pedidos_RegistrarPedidoUseCase` |
| P1-09 | `hex_menu` | `RegistrarPlatoUseCase · productosPorCategoria()` | `RegistrarPlatoUseCase · editar() · desactivar()` (opcional) | D5 | p4 `cls_menu_RegistrarPlatoUseCase` |
| P1-10 | nueva `ad_inventario_b3` | — | `«Listener» · entrada / InventarioEvents / Listener`, con «usa puerto» | H2, D4 | p4 `cls_inventario_InventarioEventsListener`, `e_h2_usa` |
| P1-11 | nueva: arista `chip_menu_actualizado → ad_inventario_b3` | — | Ámbar punteada, como `e249` | H2 | p4 `InventarioEventsListener` |
| P1-12 | nueva: arista `chip_solicitud_registrada → ad_inventario_b3` | — | Ídem | D4 | ídem |
| P1-13 | `hex_solicitudes` | `GestionarSolicitudUseCase · registrarCotizacion()` | Agregar `· completar()` (opcional) | H12 | p5 `cls_solicitudes_GestionarSolicitudUseCase` |
| P1-14 | nueva `ad_cotizaciones_t3` | — | `«REST» · entrada / ProveedorRestController`, con «usa puerto» | DEC-07 | p5 `cls_cotizaciones_ProveedorRestController`, `e_dec07_usa` |
| P1-15 | nueva: arista `gw → ad_cotizaciones_t3` y su etiqueta | — | `/api/proveedores/**` | DEC-12 | ídem |
| P1-16 | `ad_cotizaciones_b1`, o una caja nueva | `«JPA» · salida / Cotizacion / JpaAdapter` | `CotizacionJpaAdapter / ProveedorJpaAdapter`, o en caja aparte con JDBC | DEC-07 | p5 `cls_cotizaciones_ProveedorJpaAdapter`, `e_dec07_jpa` |
| P1-17 | nueva `ad_cotizaciones_b0` | — | `«Listener» · entrada / OrdenGenerada / Listener`, con «usa puerto» | D3 | p5 `cls_cotizaciones_OrdenGeneradaListener`, `e_d3_usa` |
| P1-18 | nueva: arista `chip_orden_generada → ad_cotizaciones_b0` | — | Ámbar punteada | D3 | ídem |
| P1-19 | nueva `ad_cotizaciones_b3` | — | `«Almacén» · salida / AlmacenObjetos / Adapter`, con «implementa puerto» y `HTTPS` → almacén (P1-44) | D1 | p5 `cls_cotizaciones_AlmacenObjetosAdapter`, `e_d1_impl` |
| P1-20 | `hex_cotizaciones` | `GenerarCotizacionFacade · CotizacionService` | Agregar `· ProveedorService` | DEC-07 | p5 `cls_cotizaciones_ProveedorService` |
| P1-21 | `v122` | `Solicitud de cotización, PDF, registro de valores y validación` | `…, validación y proveedores` | DEC-07 | p5 `n_proveedores` |
| P1-22 | nueva `ad_ordenes_t2` | — | `«PDF» · salida / PdfOrden / Adapter`, con «implementa puerto». La fila de arriba pasa a 3×118 | H4 | p6 `cls_ordenes_PdfOrdenAdapter`, `e_h4_pdfimpl` |
| P1-23 | nueva `ad_ordenes_b0` | — | `«Almacén» · salida / AlmacenObjetos / Adapter`, con «implementa puerto» y `HTTPS` → almacén | H4 | p6 `cls_ordenes_AlmacenObjetosAdapter`, `e_h4_almimpl` |
| P1-24 | `v142` | `Órdenes de compra desde cotizaciones OPCIONADAS` | Agregar `y su PDF` | H4 | p6 `n_pdforden` |
| P1-25 | `e228` | `REST · cotización OPCIONADA` | `REST · GET /api/cotizaciones/{id} · cotización OPCIONADA` | H5 | p6 `cls_ordenes_CotizacionRestClient` |
| P1-26 | nueva `ad_administracion_t1` | — | `«ELK · HTTPS» · salida / Elasticsearch / LogsAdapter`, con «implementa puerto». La fila de arriba pasa a 2×181 | H14 | p7 `cls_administracion_ElasticsearchLogsAdapter`, `e_h14_impl` |
| P1-27 | nueva `ad_administracion_b2`, o agrupada en `b1` | — | `«JPA» · salida / Parametros / JpaAdapter`, con «implementa puerto» y JDBC | N7 | p7 `cls_administracion_ParametrosJpaAdapter`, `e_n7_impl` |
| P1-28 | `db_administracion` | `BD auditoría [PostgreSQL]` | `BD administración [PostgreSQL]`: ahora también guarda los parámetros | N7 · N/P | p7 `n_parametros` |
| P1-29 | `hex_administracion` | `AdministracionUseCase · parametrizar() · auditoria()` | Agregar `· logs() · tema()` (opcional) | H13, H14 | p7 `cls_administracion_AdministracionUseCase` |
| P1-30 | nueva `ad_analitica_t2` | — | `«REST» · entrada / PromocionRestController`, con «usa puerto». Entra por `/api/bam/**`, sin arista propia desde el gateway | DEC-07 | p7 `cls_analitica_PromocionRestController`, `e_d8_usa` |
| P1-31 | `ad_analitica_b1` | `«JPA» · salida / KpiJpaAdapter` | `KpiJpaAdapter / SugerenciaJpaAdapter`, o en caja aparte | DEC-07 | p7 `cls_analitica_SugerenciaJpaAdapter`, `e_d8_impl` |
| P1-32 | nueva `ad_analitica_t3` | — | `«Scheduler» · entrada / TendenciasJob`, con «usa puerto». Ya faltaba en el original | N/P | p7 `cls_analitica_TendenciasJob` |
| P1-33 | `v165` | `Tablero BAM (KPI) y detección de tendencias` | `Tablero BAM (KPI), detección de tendencias y promociones sugeridas` | DEC-07 | p7 `n_promociones` |
| P1-34 | nueva `ad_notificaciones_t2` | — | `«REST» · entrada / Destinatario / RestController`, con «usa puerto» | DEC-13 | p6 `cls_notificaciones_DestinatarioRestController`, `e_n5_usa` |
| P1-35 | nueva: arista `gw → ad_notificaciones_t2` y su etiqueta | — | `/api/notificaciones/**` | D2, DEC-12 | ídem |
| P1-36 | `ad_notificaciones_b1` | `«JPA» · salida / EnvioJpaAdapter` | `EnvioJpaAdapter / DestinatarioJpaAdapter`, o en caja aparte | DEC-13 | p6 `cls_notificaciones_DestinatarioJpaAdapter`, `e_n5_impl` |
| P1-37 | nueva `ad_notificaciones_b3` | — | `«Almacén» · salida / AlmacenObjetos / Adapter`, con «implementa puerto» y `HTTPS` → almacén | N4 | p6 `cls_notificaciones_AlmacenObjetosAdapter`, `e_n4_impl` |
| P1-38 | nueva `ad_notificaciones_b2` | — | `«Publisher» · salida / KafkaEvent / Publisher`, con «implementa puerto». La auditoría sale por `e242` | H10 | p6 `cls_notificaciones_NotificacionService` (`- eventos: EventPublisherPort`) |
| P1-39 | `v188` | `Factory Method crea la notificación; el correo se decora` | `Factory Method crea la notificación; los decoradores envuelven el puerto Correo desde los adaptadores de salida` | N8 · N/P | p6 `cls_notificaciones_CorreoDecorator` |
| P1-40 | nueva `ad_integracion_b2` | — | `«Publisher» · salida / KafkaEvent / Publisher`, con «implementa puerto» | H10 | p7 `cls_integracion_ConsultarPrecioService` (`- eventos`) |
| P1-41 | nueva: arista `gw → ad_integracion_t1` y su etiqueta | — | `/api/integracion/**` | D2, DEC-12 | ver la nota ² de los prefijos |
| P1-42 | `e233` | `ext_sistemas → ad_integracion_t1` | El destino pasa a `gw`; el texto se mantiene: `SOAP/HTTPS · servicios expuestos` | D2 | p7 `cls_integracion_AbastecimientoSoapEndpoint` |
| P1-43 | `v8` (V03) | `POST /api/pedidos` | `POST /api/pedidos · GET …/catalogo · GET …/mesas` | H6 | p4 `cls_pedidos_PedidoRestController` (‹V03›) |
| P1-44 | nueva `db_objetos` | — | Cilindro: `Almacén de objetos / [S3 compatible · PDF]` | D1, H4, N4 | p5/p6 `AlmacenObjetosAdapter` |
| P1-45 | `e226` | `recibe el PDF de la cotización y responde al director por correo` | `recibe los PDF de la cotización y de la orden / y responde al director por correo` | H4 | p6 `n_pdforden` |
| P1-46 | `v22` (V13) | `POST /api/cotizaciones` | Agregar `· GET /api/proveedores` | DEC-07 | p5 `ProveedorRestController.consultar(soloActivos) ‹V13›` |
| P1-47 | `v31` (V20) | `GET /api/cotizaciones?estado=OPCIONADA` | Agregar `· GET …/{id}` | H5 | p5 `CotizacionRestController.detalle ‹V20 · V21›` |
| P1-48 | `v32` (V21) | `POST /api/ordenes-compra` | Agregar `· GET /api/cotizaciones/{id}` | H5 | ídem |
| P1-49 | `v37` | `GET /api/bam/kpis` | `GET /api/bam/kpis?desde&hasta&sucursalId` | H7 | p7 `cls_analitica_TableroRestController` |
| P1-50 | `v38` | `Parámetros, auditoría y logs · /api/admin/**` | `Parámetros, tema, auditoría y logs · /api/admin/** · GET …/parametros/tema` | H13, H14 | p7 `cls_administracion_AdminRestController` |
| P1-51 | nuevas, en `grp_sin` | — | **Administrador:** Usuarios (`POST /api/usuarios`) y Destinatarios (`/api/notificaciones/destinatarios`). **Jefe de cocina:** Platos (`PUT /api/platos/{id}` · `PATCH …/{id}/desactivar`) y Promociones (`/api/bam/promociones`). **Auxiliar de compras:** Proveedores (`/api/proveedores/**`) y Reenvío (`POST /api/cotizaciones/{id}/reenvio`) | DEC-10, DEC-13, D5, DEC-07, H18 | p3, p6, p4, p7 y p5: los controladores respectivos |
| P1-52 | nueva `db_integracion_pg` | — | Según la Decisión 3 (BD PostgreSQL de ms-integracion) | N/P | p3 `n_outbox` («tabla outbox del servicio») |
| P1-53 | `bus` | `… consumidores idempotentes` | Agregar `· outbox transaccional` (opcional) | N9 · N/P | p3 `n_outbox` |
| P1-54 | `db_identidad` … `db_integracion` (11) | `shape=cylinder` | `shape=cylinder3`: al exportar, los cilindros salen deformes y el texto, cortado | N/P (forma) | — |

## Página 2 · C4 Contexto (p2)

| # | Celda | Texto actual | Cambio propuesto | Origen | Respaldo |
|---|---|---|---|---|---|
| P2-01 | nueva: leyenda | — | «Cómo leer el diagrama»: persona, persona externa, sistema propio, sistema externo, relación con propósito [protocolo] y lo que ocurre fuera del sistema | H22 | — |
| P2-02 | `e23` | `Entrega el PDF de la solicitud de cotización` | `Entrega los PDF de la solicitud de cotización y de la orden de compra [SMTP]` | H22, H4 · N/P | p6 `n_pdforden` |
| P2-03 | `e3`, `e5`, `e7`, `e9`, `e11`, `e13` | Sin protocolo | Agregar `[HTTPS]` al final: `v1` promete propósito y protocolo en cada flecha | N/P | — |
| P2-04 | `e7` | `Consulta solicitudes y envía solicitudes de cotización (V11–V15)` | Agregar `y registra proveedores` | DEC-07 · N/P | p5 `ProveedorRestController` |
| P2-05 | `e5` | `Registra platos, ve alarmas … (V05–V10)` | Agregar `y revisa promociones sugeridas` | DEC-07 · N/P | p7 `PromocionRestController` |
| P2-06 | `e13` | `Parametriza y revisa auditoría y logs` | `Parametriza, registra usuarios y destinatarios, y revisa auditoría y logs` | DEC-10, DEC-13 · N/P | p3 `UsuarioRestController`; p6 `DestinatarioRestController` |
| P2-07 | `v6`, `v12` | `Solicitudes y cotizaciones` / `Parámetros y auditoría` | `Solicitudes, cotizaciones y proveedores` / `Parámetros, usuarios y auditoría` (opcional) | N/P | ídem |

## Página 3a · C4 Contenedores (p9)

| # | Celda | Texto actual | Cambio propuesto | Origen | Respaldo |
|---|---|---|---|---|---|
| P3a-01 | `c3a_e9` | `Descubre instancias vivas [HTTP]` | `[HTTPS · mTLS]` | H17 | — |
| P3a-02 | `c3a_e10` | `… y envía su latido [HTTP]` | `[HTTPS · mTLS]` | H17 | — |
| P3a-03 | `c3a_e33`, `c3a_e34`, `c3a_e36`, `c3a_e37`, `c3a_e38`, `c3a_e39`, `c3a_e40` | Sin protocolo | Agregar `[Kafka/TLS]` | H22 | p10 `c3b_e16` ya usa `[Kafka/TLS]` |
| P3a-04 | `c3a_e35` | `publica inventario.stock-bajo / consume pedido.registrado` | `… consume / pedido.registrado / menu.actualizado / solicitud.registrada [Kafka/TLS]` | H2, D4, H22 | p4 `InventarioEventsListener`, `PedidoRegistradoListener` |
| P3a-05 | `c3a_e32` | `publica cotizacion.enviada cotizacion.validada` | Agregar `/ consume / orden.generada [Kafka/TLS]` | D3, H22 | p5 `OrdenGeneradaListener` |
| P3a-06 | `c3a_e41` | `todos los servicios publican auditoria.evento en cada CRUD` | `los otros 10 servicios publican / auditoria.evento en cada CRUD; ms-administracion / escribe directo en su bitácora [Kafka/TLS]` | H10, H22 | p7 `n_bitacora` |
| P3a-07 | `c3a_lt49` | `Evento asíncrono: hacia Kafka publica, desde Kafka consume` | Agregar `[Kafka/TLS]` | H22 | — |
| P3a-08 | `c3a_e19` | `Entrega los PDF de cotizaciones y órdenes` | Agregar `[SMTP]` | H22 | p6 `n_topicos` |
| P3a-09 | `c3a_ms_notificaciones` | `Sin ruta propia: reacciona a eventos` | `/api/notificaciones/** · alarma de V05` | D2, DEC-13 | p6 `DestinatarioRestController`, `AlarmaPushAdapter` |
| P3a-10 | `c3a_ms_integracion` | `Uso interno (REST) + endpoint SOAP · apoya V13` | `/api/integracion/** (SOAP) · REST interno · apoya V13` | D2 | p7 `AbastecimientoSoapEndpoint`, `PrecioRestController` |
| P3a-11 | `c3a_e15` | `c3a_x_sistemas → c3a_ms_integracion` | El destino pasa a `c3a_gateway`: `Consumen los servicios SOAP expuestos / (/api/integracion/**) [SOAP/HTTPS]` | D2 | ídem |
| P3a-12 | `c3a_ms_identidad` | `/api/auth/** · V01` | `/api/auth/** · /api/usuarios/** · V01` | DEC-12 | p3 `UsuarioRestController` |
| P3a-13 | `c3a_ms_cotizaciones` | `… cadena de reglas / /api/cotizaciones/** · V13–V20` | `… cadena de reglas, y proveedores / /api/cotizaciones/** · /api/proveedores/** · V13–V20` | DEC-12, DEC-07 | p5 `ProveedorRestController` |
| P3a-14 | `c3a_e11` | `Enruta /api/** por prefijo a una instancia sana` | `Enruta los 13 prefijos /api/… a una instancia sana` | DEC-12 | — |
| P3a-15 | `c3a_notes`, viñeta 1 | `… cada servicio indica su prefijo /api/…` | `… sus prefijos /api/… (13 para los 11 servicios)` | DEC-12 | — |
| P3a-16 | `c3a_ms_ordenes` | `Órdenes de compra desde cotizaciones OPCIONADAS` | Agregar `y su PDF` | H4 | p6 `n_pdforden` |
| P3a-17 | nueva: arista `c3a_ms_ordenes → c3a_db_objetos` | — | `Guarda el PDF de la orden [HTTPS]` | H4 | p6 `e_h4_alm` |
| P3a-18 | nueva: arista `c3a_ms_notificaciones → c3a_db_objetos` | — | `Lee el PDF para adjuntarlo (pdfUrl) [HTTPS]` | N4 | p6 `e_n4_alm`, `n_adjunto` |
| P3a-19 | `c3a_e31` | `Guarda los PDF [HTTPS]` | `Guarda y lee el PDF de la cotización [HTTPS]` | D1 | p5 `AlmacenArchivosPort` (`guardar`/`obtener`), `n_claim` |
| P3a-20 | `c3a_db_objetos` | `Almacén de objetos [PDF y archivos]` | `[S3 compatible · PDF y archivos]` (opcional) | D1 | p5/p6 `AlmacenObjetosAdapter` |
| P3a-21 | nueva `c3a_elk` | — | `Logs de error / [Contenedor: Elasticsearch (ELK)] / Reúne los logs de los 11 servicios (RNF mantenimiento)` | H14 | p7 `ElasticsearchLogsAdapter` |
| P3a-22 | nueva: arista `c3a_ms_administracion → c3a_elk` | — | `Consulta los logs de error [HTTPS]` | H14 | p7 `e_h14_logs` |
| P3a-23 | `c3a_e12` | `Consulta la cotización OPCIONADA [REST/JSON · mTLS]` | Agregar `(GET /api/cotizaciones/{id})` | H5 | p6 `CotizacionRestClient` |
| P3a-24 | `c3a_ms_solicitudes` | `(PENDIENTE → EN TRÁMITE)` | `(PENDIENTE → EN TRÁMITE → COMPLETADA)` | H12 · N/P | p5 `EstadoSolicitud` |
| P3a-25 | `c3a_ms_analitica` | `Tablero BAM (KPI) y detección de tendencias` | `Tablero BAM (KPI), tendencias y promociones sugeridas` | DEC-07 · N/P | p7 `PromocionRestController` |
| P3a-26 | `c3a_e2`, `c3a_e3`, `c3a_e6` | `Platos, inventario y solicitudes` / `Solicitudes y cotizaciones` / `Parámetros, auditoría y logs` | `…, solicitudes y promociones` / `…, cotizaciones y proveedores` / `Parámetros, usuarios, destinatarios, auditoría y logs` | DEC-07, DEC-10, DEC-13 · N/P | ídem |
| P3a-27 | `c3a_db_administracion` | `BD auditoría` | `BD administración` | N7 · N/P | p7 `ParametrosJpaAdapter` |
| P3a-28 | nueva `c3a_db_integracion_pg` | — | Según la Decisión 3 | N/P | p3 `n_outbox` |

## Página 3b · C4 Componentes de ms-cotizaciones (p10)

| # | Celda | Texto actual | Cambio propuesto | Origen | Respaldo |
|---|---|---|---|---|---|
| P3b-01 | `c3b_controller` | Rutas de V13 a V19 | Agregar la línea `GET …/{id} (V20, V21 y ms-ordenes) · POST …/{id}/reenvio (HU-12)` | H5, H18 | p5 `CotizacionRestController` |
| P3b-02 | `c3b_e5` | `Consulta la cotización OPCIONADA (V21)` | `GET /api/cotizaciones/{id}: cotización OPCIONADA (V21) [REST/JSON · mTLS]` | H5 | p6 `CotizacionRestClient` |
| P3b-03 | `c3b_service` | `Registra valores (V18), valida (V19), consulta (V15–V17, V20) y entrega el PDF (V14)` | Agregar: da el detalle (V20, V21), entrega el PDF desde el almacén (V14), reenvía (HU-12) y marca CON_ORDEN | H5, D1, H18, D3 | p5 `CotizacionService` |
| P3b-04 | `c3b_facade` | `Un clic de V13: precio sugerido → guarda → genera el PDF → publica cotizacion.enviada` | `Un clic de V13 con una o varias solicitudes: … → genera el PDF → lo guarda en el almacén → publica cotizacion.enviada con pdfUrl. Si el Ministerio no responde: PENDIENTE_PRECIO` | DEC-05, D1, H18 | p5 `n_variassol`, `n_claim`, `n_reenvio` |
| P3b-05 | `c3b_modelo` | `Cotizacion ◆ ItemCotizacion, Proveedor y EstadoCotizacion…` | `Cotizacion (una o varias solicitudes) ◆ ItemCotizacion, Proveedor y EstadoCotizacion (PENDIENTE_PRECIO · ENVIADA · RECHAZADA · SOSPECHOSA · OPCIONADA · CON_ORDEN)…` | DEC-05, H18, D3 | p5 `Cotizacion`, `EstadoCotizacion` |
| P3b-06 | `c3b_e11` | `genera el PDF (V13, V14) «GeneradorPdfPort»` | `solo la fachada: genera el PDF (V13) «GeneradorPdfPort»` | D1 | p5: solo `GenerarCotizacionFacade` tiene `pdf: GeneradorPdfPort` |
| P3b-07 | `c3b_e15` | `c3b_ad_pdf → c3b_objetos` · `Guarda el PDF` | Borrarla: `PdfCotizacionAdapter` solo genera `byte[]`. La reemplaza P3b-10 | D1 | p5 `PdfCotizacionAdapter` |
| P3b-08 | nueva `c3b_ad_almacen` | — | `AlmacenObjetosAdapter / [Componente: cliente S3 compatible · adaptador de salida] / Implementa AlmacenArchivosPort` | D1 | p5 `AlmacenObjetosAdapter` |
| P3b-09 | nueva: arista `c3b_app → c3b_ad_almacen` | — | `guarda el PDF (V13) y lo lee (V14) «AlmacenArchivosPort»` | D1 | p5 `e_d1_alm` |
| P3b-10 | nueva: arista `c3b_ad_almacen → c3b_objetos` | — | `Guarda y lee el PDF [HTTPS]` | D1 | ídem |
| P3b-11 | nueva `c3b_ad_listener` | — | `OrdenGeneradaListener / [Componente: consumidor Kafka · adaptador de entrada]` | D3 | p5 `OrdenGeneradaListener` |
| P3b-12 | nueva: arista `c3b_broker → c3b_ad_listener` | — | `orden.generada [Kafka/TLS]` | D3 | ídem |
| P3b-13 | nueva: arista `c3b_ad_listener → c3b_app` | — | `marcarConOrden() «GestionarCotizacionUseCase»` | D3 | p5 `e_d3_usa`, `n_conorden` |
| P3b-14 | nueva `c3b_prov_controller` | — | `ProveedorRestController / [Componente: Spring MVC · adaptador de entrada] / /api/proveedores/**: registrar, consultar (activos para V13), detalle, editar e inactivar` | DEC-07 | p5 `ProveedorRestController` |
| P3b-15 | nueva: arista `c3b_gateway → c3b_prov_controller` | — | `Enruta /api/proveedores/** [HTTPS · REST/JSON · mTLS]` | DEC-07, DEC-12 | ídem |
| P3b-16 | nueva `c3b_prov_service` | — | `Gestión de proveedores [Componente: aplicación · implementa GestionarProveedorUseCase] / ProveedorService: NIT único, correo válido, inactivar sin borrar` | DEC-07 | p5 `ProveedorService`, `n_proveedores` |
| P3b-17 | nueva: arista `c3b_prov_controller → c3b_prov_service` | — | `llama a «GestionarProveedorUseCase»` | DEC-07 | p5 `e_dec07_usa` |
| P3b-18 | `c3b_ad_jpa`, más una arista nueva | `CotizacionJpaAdapter … Implementa CotizacionRepositoryPort` | `CotizacionJpaAdapter · ProveedorJpaAdapter / Implementa CotizacionRepositoryPort y ProveedorRepositoryPort`, y la arista `c3b_prov_service → c3b_ad_jpa` con `«ProveedorRepositoryPort»` | DEC-07 | p5 `e_dec07_rep`, `e_dec07_jpa` |
| P3b-19 | nueva: arista `c3b_prov_service → c3b_ad_kafka` | — | `auditoría «EventPublisherPort»` | DEC-07 | p5 `ProveedorService.eventos` |
| P3b-20 | `c3b_broker` | `Consumen: ms-notificaciones y ms-solicitudes (cotizacion.enviada); ms-analitica (cotizacion.validada)` | Agregar `ms-administracion (auditoria.evento)`, y `Entrega a ms-cotizaciones orden.generada (de ms-ordenes)` | D3, H10 | p7 `AuditoriaListener`; p5 `OrdenGeneradaListener` |
| P3b-21 | `c3b_notes`, viñeta 1 | `…usan los puertos de repositorio, PDF y eventos; solo la fachada usa PrecioSugeridoPort` | `…repositorio, almacén y eventos; solo la fachada usa PrecioSugeridoPort y GeneradorPdfPort` | N/P | p5 `CotizacionService`, `GenerarCotizacionFacade` |
| P3b-22 | `c3b_notes`, viñeta 3 | `…4c todavía no tiene un puerto para guardar el PDF…` | `El PDF se guarda en el almacén con AlmacenArchivosPort, y cotizacion.enviada lleva solo pdfUrl (claim check).` | D1 | p5 `n_claim` |
| P3b-23 | `c3b_p_auxiliar`, `c3b_e1` | `Genera y consulta cotizaciones` / `Solicitudes y cotizaciones (V11–V15)` | Agregar `; gestiona proveedores` / `…, cotizaciones y proveedores (V11–V15) [HTTPS]` | DEC-07 | p5 `ProveedorRestController` |
| P3b-24 | `c3b_ad_kafka` | `Implementa EventPublisherPort` | Agregar `: escribe en la tabla outbox y un relé publica en Kafka` | N9 · N/P | p3 `n_outbox` |
| P3b-25 | `c3b_lt25` | `Evento asíncrono hacia Kafka` | `Evento asíncrono hacia o desde Kafka` | D3 | — |
| P3b-26 | `c3b_reglas` | `Rechazada … → Opcionada (el resto)` | Agregar `armada por cadenaPorDefecto()` (opcional) | H8 · N/P | p5 `ReglaValidacion` |

## Página 5 · Despliegue (p8)

| # | Celda | Texto actual | Cambio propuesto | Origen | Respaldo |
|---|---|---|---|---|---|
| P5-01 | `v26` | `Almacén de objetos / PDF de cotizaciones y archivos de carga · crece 200 %` | `Almacén de objetos [S3 compatible] / PDF de cotizaciones y órdenes, archivos de carga · versionado · crece 200 %` | H16, H4 | p5/p6 `AlmacenObjetosAdapter` |
| P5-02 | nueva `v39`, en `v30` | — | `Almacén de objetos réplica / replicación entre regiones` | H16 | — |
| P5-03 | nueva: arista `v26 → v39` | — | Magenta punteada: `replicación entre regiones` | H16 | — |
| P5-04 | nueva: arista `v26 → v27` | — | Gris punteada: `copia diaria` | H16 | — |
| P5-05 | `v27` | `Copia completa cada noche + registros continuos; retención 30 días` | `PostgreSQL: copia completa cada noche + WAL continuo · almacén de objetos: copia diaria; retención 30 días (RNF almacenamiento)` | H16 | — |
| P5-06 | nuevas `v40`–`v42`, dentro de `v10` | — | Tres sub-cajas, `Zona A`, `Zona B` y `Zona C`, con lo que corre en cada zona (Decisión 6) | H24 | — |
| P5-07 | `v25` | `Redis / caché de precios del Ministerio (Proxy)` | `Redis primario + réplica (otra zona) / …` | H24 | p7 `PrecioReferenciaCacheProxy` |
| P5-08 | nueva: leyenda | — | Tipos de flecha (síncrona, eventos, descubrimiento, observabilidad y respaldo, replicación y conmutación) y tipos de caja | H22 | — |
| P5-09 | `e18` | `publica / consume` | Agregar `[Kafka/TLS]` | N/P (coherencia con H22) | — |
| P5-10 | `e14` | `descubre` | `descubre [HTTPS · mTLS]` | N/P (coherencia con H17) | — |
| P5-11 | nueva: arista `v15 → v26` | — | `HTTPS · PDF: ms-cotizaciones y ms-ordenes guardan; ms-notificaciones lee` | H4, N4 · N/P | p5/p6 `AlmacenArchivosPort` |
| P5-12 | `e20` | `métricas, trazas y logs` | Agregar `· ms-administracion consulta los logs [HTTPS]` (opcional) | H14 · N/P | p7 `ElasticsearchLogsAdapter` |
| P5-13 | `v24` | `PostgreSQL ×11` | Según la Decisión 3. Hoy contradice a p1 y p9, donde ms-integracion solo tiene Redis | N/P | p3 `n_outbox` |
| P5-14 | `v24`, `v25`, `v26`, `v33` | `shape=cylinder` | `shape=cylinder3` (forma) | N/P | — |

## Página 6 · C4 Dinámico (p11)

| # | Celda | Texto actual | Cambio propuesto | Origen | Respaldo |
|---|---|---|---|---|---|
| P6-01 | 66 aristas | Sin número | Prefijo `x.y` en cada mensaje, contando también las respuestas. Paso 1: `l21`, `l22`, `l23`, `l25`, `l26` · 2: `l30`–`l33` · 3: `l38`–`l41` · 4: `l45`–`l48`, `l50`–`l52`, `l54`, `l55` · 5: `l59`–`l63` · 6: `l68`–`l74` · 7: `l79` · 8: `l84`–`l86`, `l88` · 9: `l92`–`l94`, `l96` · 10: `l100`, `l101` · 11: `l105`–`l109`, `l111`, `l112` · 12: `l116`–`l121`, más 12.7 · 13: `l125`–`l128` · 14: `l133`–`l136` (todas con el prefijo `c6_`) | H23 | — |
| P6-02 | `c6_title` | `…los números de la izquierda son los de la tabla…` | Agregar `x.y numera los mensajes dentro de cada paso.` | H23 | — |
| P6-03 | `c6_v187` | `Número del paso en el reporte…` | Agregar `· x.y = mensaje y del paso x` | H23 | — |
| P6-04 | `c6_l45` | `V13 Generar solicitud de cotización` | `4.1 V13: elige una o varias solicitudes (SOL-031) y genera la solicitud de cotización` | DEC-05 | p5 `n_variassol` |
| P6-05 | `c6_v53` | `guarda COT-018 · ENVIADA, genera el PDF y lo deja en el almacén de objetos` | `guarda COT-018 · ENVIADA con sus solicitudes, genera el PDF y lo deja en el almacén (pdfUrl)` | DEC-05, D1 | p5 `Cotizacion.solicitudNumeros`, `pdfUrl` |
| P6-06 | `c6_l59` | `publica cotizacion.enviada` | `5.1 publica cotizacion.enviada (pdfUrl, solicitudes y precios)` | D1, DEC-05 | p5 `CotizacionEnviadaEvent` |
| P6-07 | `c6_l61` | `PDF de la cotización al proveedor [SMTP/TLS]` | `5.3 lee el PDF del almacén (pdfUrl) y lo envía al proveedor [SMTP/TLS]` | N4 | p6 `n_adjunto`, `e_n4_alm` |
| P6-08 | `c6_v64` | `Si todos los productos de SOL-031 ya tienen cotización → EN TRÁMITE` | `Por cada solicitud de COT-018 (aquí SOL-031): si todos sus productos quedan cotizados → EN TRÁMITE (idempotente por cotización)` | DEC-05, N3 | p5 `n_listas` |
| P6-09 | `c6_l33` | `confirmación al jefe [SMTP/TLS]` | `2.4 confirmación a quien creó SOL-031 (correoSolicitante) [SMTP/TLS]` | DEC-13 | p5 `SolicitudRegistradaEvent.correoSolicitante` |
| P6-10 | `c6_l70` | `aviso al jefe [SMTP/TLS]` | `6.3 aviso a quien creó SOL-031 (correoSolicitante) [SMTP/TLS]` | DEC-13 | p5 `SolicitudEnTramiteEvent` |
| P6-11 | `c6_l108` | `consulta la cotización OPCIONADA [REST/JSON · mTLS]` | `11.4 GET /api/cotizaciones/{id}: cotización OPCIONADA [REST/JSON · mTLS]` | H5 | p6 `CotizacionRestClient` |
| P6-12 | `c6_l109` | `COT-018 · proveedor y valores` | `11.5 COT-018 · proveedor, correo, solicitudes y valores` (opcional) | H4 | p6 `CotizacionOpcionada` |
| P6-13 | `c6_v110` | `Guarda OC-0012 en su BD` | `Rechaza si COT-018 ya tiene orden; guarda OC-0012, genera su PDF y lo deja en el almacén (pdfUrl)` | D3, H4 | p6 `n_unaorden`, `n_pdforden` |
| P6-14 | `c6_l116` | `publica orden.generada` | `12.1 publica orden.generada (pdfUrl)` | H4 | p6 `OrdenGeneradaEvent` |
| P6-15 | `c6_l117` | `entrega orden.generada: la solicitud avanza` | `12.2 entrega orden.generada: cada solicitud de COT-018 (SOL-031) → COMPLETADA` | H12, DEC-05 | p5 `n_listas` |
| P6-16 | `c6_l119` | `orden de compra al proveedor [SMTP/TLS]` | `12.4 lee el PDF de OC-0012 del almacén y lo envía al proveedor [SMTP/TLS]` | N4, H4 | p6 `n_topicos` |
| P6-17 | nueva `c6_l121b` | — | Ámbar punteada, de Kafka a ms-cotizaciones: `12.7 entrega orden.generada: COT-018 → CON_ORDEN (sale de V20)` | D3 | p5 `OrdenGeneradaListener`, `n_conorden` |
| P6-18 | `c6_v132` | `…cada servicio publica auditoria.evento … no solo estos tres.` | `…cada uno de los otros 10 servicios publica auditoria.evento justo después de cada escritura, no solo estos tres; ms-administracion registra las suyas directo en su bitácora.` | H10 | p7 `n_bitacora` |
| P6-19 | `c6_v188` | Viñetas «Consumidores completos» y «Auditoría al final» | Agregar ms-cotizaciones (CON_ORDEN) en el paso 12, la excepción de ms-inventario (Decisión 4), y «10 servicios; ms-administracion escribe directo» | D3, D4, H10 | p5, p4, p7 |
| P6-20 | `c6_l72`, `c6_l39` | `GET /api/solicitudes` | `GET /api/solicitudes?solicitante=yo` (V10) y `GET /api/solicitudes?estado=PENDIENTE`, como en p1 `v17` y `v20` | N/P | p5 `misSolicitudes ‹V10›` |

## Los 13 prefijos del gateway

| # | Prefijo | Servicio | Respaldo | p1 (etiqueta / arista) | p9 |
|---|---|---|---|---|---|
| 1 | `/api/auth/**` | ms-identidad | p3 `AuthRestController` | `v208` / `e207` | `c3a_ms_identidad` |
| 2 | `/api/usuarios/**` | ms-identidad | p3 `UsuarioRestController` | nueva (P1-05) | P3a-12 |
| 3 | `/api/pedidos/**` | ms-pedidos | p4 `PedidoRestController` | `v210` / `e209` | `c3a_ms_pedidos` |
| 4 | `/api/platos/**` | ms-menu | p4 `PlatoRestController` | `v212` / `e211` | `c3a_ms_menu` |
| 5 | `/api/inventario/**` | ms-inventario | p4 `InventarioRestController` | `v214` / `e213` | `c3a_ms_inventario` |
| 6 | `/api/solicitudes/**` | ms-solicitudes | p5 `SolicitudRestController` | `v216` / `e215` | `c3a_ms_solicitudes` |
| 7 | `/api/cotizaciones/**` | ms-cotizaciones | p5 `CotizacionRestController` | `v218` / `e217` | `c3a_ms_cotizaciones` |
| 8 | `/api/proveedores/**` | ms-cotizaciones | p5 `ProveedorRestController` | nueva (P1-15) | P3a-13 |
| 9 | `/api/ordenes-compra/**` | ms-ordenes | p6 `OrdenCompraRestController` | `v220` / `e219` | `c3a_ms_ordenes` |
| 10 | `/api/notificaciones/**` | ms-notificaciones | p6 `DestinatarioRestController`¹ | nueva (P1-35) | P3a-09 |
| 11 | `/api/admin/**` | ms-administracion | p7 `AdminRestController` | `v222` / `e221` | `c3a_ms_administracion` |
| 12 | `/api/bam/**` | ms-analitica | p7 `TableroRestController`, `PromocionRestController` | `v224` / `e223` | `c3a_ms_analitica` |
| 13 | `/api/integracion/**` | ms-integracion | p7 `AbastecimientoSoapEndpoint`² | nueva (P1-41) | P3a-10 |

¹ Por este prefijo entra también el WebSocket de `AlarmaPushAdapter`. 4d no escribe la ruta del WebSocket.
² 4e no escribe este prefijo: sale de la tabla del reporte (p. 8) y de los «11 de antes» de DEC-12 (D2). En la sesión 3 hay que anotar que el `@WebService` se publica bajo esa ruta.

## Los 10 tópicos (según 4a–4e)

| Tópico | Productor | Consumidores | p1 | p9 | p11 |
|---|---|---|---|---|---|
| `menu.actualizado` | ms-menu | ms-pedidos, **ms-inventario** | `e234`; `e249` + P1-11 | `e36`; `e37` + `e35` | — |
| `pedido.registrado` | ms-pedidos | ms-inventario, ms-analitica | `e235`; `e247`, `e259` | `e37`; `e35`, `e40` | — |
| `inventario.stock-bajo` | ms-inventario | ms-notificaciones, ms-analitica | `e236`; `e258`, `e256` | `e35`; `e39`, `e40` | — |
| `solicitud.registrada` | ms-solicitudes | ms-notificaciones, ms-analitica, **ms-inventario** | `e237`; `e257`, `e253` + P1-12 | `e34`; `e39`, `e40` + `e35` | paso 2 (+ Decisión 4) |
| `solicitud.en-tramite` | ms-solicitudes | ms-notificaciones | `e238`; `e255` | `e34`; `e39` | paso 6 |
| `cotizacion.enviada` | ms-cotizaciones (también al reenviar) | ms-solicitudes, ms-notificaciones | `e239`; `e246`, `e254` | `e32`; `e34`, `e39` | paso 5 |
| `cotizacion.validada` | ms-cotizaciones | ms-analitica | `e240`; `e250` | `e32`; `e40` | paso 10 |
| `orden.generada` | ms-ordenes | ms-solicitudes, ms-notificaciones, ms-analitica, **ms-cotizaciones** | `e241`; `e252`, `e251`, `e248` + P1-18 | `e33`; `e34`, `e39`, `e40` + `e32` | paso 12 + P6-17 |
| `tendencia.detectada` | ms-analitica | ms-notificaciones | `e243`; `e245` | `e40`; `e39` | — |
| `auditoria.evento` | Los otros 10 servicios | ms-administracion | `e242`; `e244` | `e41`; `e38` | paso 14 |

ms-notificaciones consume 6 tópicos y ms-analitica 5. Con los cambios, p1 queda con 19 aristas de consumo y 10 de publicación.
