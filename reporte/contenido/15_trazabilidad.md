<!-- §15. Correcciones: R-52 (filas de V03, V06, V13, V20 y V21) y R-60 (meseroId en V03); en el 7d, CD-02, CD-10, CD-12, CD-23, CD-33, CD-55, CD-62 y CD-63 (rutas y ventanas nuevas). R-51 ya es cierto. R-53 es opcional y no se aplica. La sección va en páginas horizontales para que la tabla quepa en 12 pt. -->
!horizontal

# 15. Trazabilidad de las ventanas

Cada ventana está asignada a un servicio, a un método de su controlador REST y a un endpoint (tabla 13): primero las 23 del prototipo y después las ventanas nuevas del recuadro del mismo nombre en el mapa (proveedores, tablero BAM, promociones sugeridas y las del administrador). La columna de servicio es la misma que aparece bajo cada miniatura en el mapa, y cada método marcado ‹Vxx› lleva el comentario de la ventana que atiende. Algunas ventanas usan más de un método: por ejemplo, la 03 carga el catálogo y las mesas antes de registrar el pedido.

!tabla 13 | Trazabilidad de las ventanas a servicios, métodos y endpoints | 1.5,2.8,3.0,3.8,7.0,6.3
| V | Rol | Ventana | Servicio | Controlador y método | Endpoint |
|---|---|---|---|---|---|
| 01 | Acceso | Inicio de sesión | ms-identidad | AuthRestController.iniciarSesion() | POST /api/auth/token |
| 02 | Mesero | Inicio del mesero | ms-pedidos | PedidoRestController.pedidosDelTurno() | GET /api/pedidos?turno=actual |
| 03 | Mesero | Nuevo pedido | ms-pedidos | PedidoRestController.registrar() | POST /api/pedidos (el pedido guarda el meseroId del token JWT, HU-01) |
| 03 | Mesero | Nuevo pedido | ms-pedidos | PedidoRestController.catalogo(sucursalId) | GET /api/pedidos/catalogo |
| 03 | Mesero | Nuevo pedido | ms-pedidos | PedidoRestController.mesas(sucursalId) | GET /api/pedidos/mesas |
| 03 | Mesero | Nuevo pedido | ms-menu | PlatoRestController.imagen(id): la foto de cada plato | GET /api/platos/{id}/imagen |
| 04 | Mesero | Pedido registrado | ms-pedidos | PedidoRestController.registrar(), respuesta 201 | 201 Created, PED-0251 |
| 05 | Jefe de cocina | Inicio del jefe | ms-inventario | InventarioRestController.alertas() | GET /api/inventario/alertas |
| 05 | Jefe de cocina | Inicio del jefe | ms-notificaciones | AlarmaPushAdapter publica la alarma de stock bajo en Redis y cada réplica la entrega en vivo | WebSocket /api/notificaciones/alarmas (solo el jefe de cocina) |
| 06 | Jefe de cocina | Nuevo plato | ms-menu | PlatoRestController.crear() | POST /api/platos |
| 06 | Jefe de cocina | Nuevo plato | ms-menu | PlatoRestController.productosPorCategoria(categoriaId) | GET /api/platos/productos?categoria={id} (también para V08 y los filtros de V12) |
| 06 | Jefe de cocina | Platos de la sucursal | ms-menu | PlatoRestController.consultar(sucursalId), con los desactivados | GET /api/platos |
| 06 | Jefe de cocina | Editar, desactivar y activar un plato | ms-menu | PlatoRestController.editar(id, p), desactivar(id) y reactivar(id) | PUT /api/platos/{id}, PATCH /api/platos/{id}/desactivar y PATCH /api/platos/{id}/reactivar (solo los platos de su sucursal) |
| 06 | Jefe de cocina | Imagen del plato | ms-menu | PlatoRestController.subirImagen(id, contenido, tipo) y imagen(id) | PUT y GET /api/platos/{id}/imagen |
| 07 | Jefe de cocina | Inventario | ms-inventario | InventarioRestController.insumos() | GET /api/inventario/insumos |
| 08 | Jefe de cocina | Nueva solicitud | ms-solicitudes | SolicitudRestController.registrar() | POST /api/solicitudes |
| 09 | Jefe de cocina | Solicitud registrada | ms-solicitudes | SolicitudRestController.registrar(), respuesta 201 | 201 Created, SOL-031 |
| 10 | Jefe de cocina | Mis solicitudes | ms-solicitudes | SolicitudRestController.misSolicitudes() | GET /api/solicitudes?solicitante=yo |
| 11 | Auxiliar de compras | Inicio del auxiliar | ms-solicitudes | SolicitudRestController.consultar() | GET /api/solicitudes?estado=PENDIENTE |
| 12 | Auxiliar de compras | Inicio con Generar cotización | ms-solicitudes | SolicitudRestController.detalle() | GET /api/solicitudes/{id} |
| 13 | Auxiliar de compras | Generar solicitud de cotización | ms-cotizaciones | CotizacionRestController.generar() | POST /api/cotizaciones |
| 13 | Auxiliar de compras | Generar solicitud de cotización | ms-cotizaciones | ProveedorRestController.consultar(soloActivos): el desplegable de proveedores activos | GET /api/proveedores |
| 13 | Auxiliar de compras | Generar solicitud de cotización | ms-integracion | PrecioRestController.precioSugerido(productoId): el precio estándar que pide ms-cotizaciones | GET /interno/precios/{productoId} (interno, fuera del gateway, con el JWT de quien cotiza) |
| 14 | Auxiliar de compras | Cotización enviada | ms-cotizaciones | CotizacionRestController.pdf() | GET /api/cotizaciones/{id}/pdf |
| 14 | Auxiliar de compras | Cotización enviada | ms-notificaciones | EnviosController.porCorrelacion(): si el correo al proveedor ya salió | GET /api/notificaciones/envios?correlacion=… |
| 15 | Auxiliar de compras | Cotizaciones | ms-cotizaciones | CotizacionRestController.consultar() | GET /api/cotizaciones |
| 15 | Auxiliar de compras | Reenviar una cotización pendiente de precio | ms-cotizaciones | CotizacionRestController.reenviar(numero) | POST /api/cotizaciones/{id}/reenvio |
| 16 | Director de compras | Inicio del director | ms-cotizaciones | CotizacionRestController.pendientesDeAtencion() | GET /api/cotizaciones/pendientes |
| 17 | Director de compras | Todas las cotizaciones | ms-cotizaciones | CotizacionRestController.consultar() | GET /api/cotizaciones |
| 18 | Director de compras | Registrar valores | ms-cotizaciones | CotizacionRestController.registrarValores() | PUT /api/cotizaciones/{id}/valores |
| 19 | Director de compras | Validación completada | ms-cotizaciones | CotizacionRestController.validar() | POST /api/cotizaciones/{id}/validacion |
| 20 | Director de compras | Cotizaciones opcionadas | ms-cotizaciones | CotizacionRestController.opcionadas() | GET /api/cotizaciones?estado=OPCIONADA |
| 20 y 21 | Director de compras | Cotizaciones opcionadas y Generar orden de compra | ms-cotizaciones | CotizacionRestController.detalle(numero) | GET /api/cotizaciones/{id} |
| 21 | Director de compras | Generar orden de compra | ms-ordenes | OrdenCompraRestController.generar() | POST /api/ordenes-compra |
| 22 | Director de compras | Orden generada | ms-ordenes | OrdenCompraRestController.generar(), respuesta 201 | 201 Created, OC-0012 |
| 23 | Director de compras | Órdenes de compra | ms-ordenes | OrdenCompraRestController.consultar() | GET /api/ordenes-compra |
| 23 | Director de compras | Órdenes de compra: «Ver PDF» | ms-ordenes | OrdenCompraRestController.pdf(numero) | GET /api/ordenes-compra/{id}/pdf |
| 23 | Director de compras | Órdenes de compra: «Marcar como recibida» | ms-ordenes | OrdenCompraRestController.finalizar(numero): de EN PROCESO a FINALIZADA | PATCH /api/ordenes-compra/{id}/finalizar (404 si no existe, 409 si ya está finalizada) |
| Nueva | Auxiliar de compras | Proveedores | ms-cotizaciones | ProveedorRestController.registrar(p) y editar(id, p) | POST /api/proveedores y PUT /api/proveedores/{id} |
| Nueva | Auxiliar de compras | Proveedores | ms-cotizaciones | ProveedorRestController.consultar(soloActivos), detalle(id) e inactivar(id) | GET /api/proveedores, GET /api/proveedores/{id} y PATCH /api/proveedores/{id}/inactivar (el director de compras solo los consulta) |
| Nueva | Jefe de cocina | Promociones sugeridas | ms-analitica | PromocionRestController.sugerencias(desde, hasta) | GET /api/bam/promociones?desde=…&hasta=… (sin fechas, todo el histórico) |
| Nueva | Jefe de cocina | Promociones sugeridas | ms-analitica | PromocionRestController.aceptar(id) y descartar(id) | PATCH /api/bam/promociones/{id}/aceptar y …/{id}/descartar (404 si no existe, 409 si ya fue aceptada o descartada) |
| Nueva | Director comercial | Tablero BAM | ms-analitica | TableroRestController.kpis(desde, hasta, sucursalId): por indicador, el ganador, su valor, el ranking y el periodo | GET /api/bam/kpis?desde=…&hasta=…&sucursalId=… |
| Nueva | Administrador | Parámetros y tema | ms-administracion | AdminRestController.parametros() y parametrizar(clave, valor) | GET /api/admin/parametros y PUT /api/admin/parametros/{clave} |
| Nueva | Todos los roles | Imagen corporativa al iniciar sesión | ms-administracion | AdminRestController.tema() | GET /api/admin/parametros/tema (cualquier usuario con sesión) |
| Nueva | Administrador | Auditoría | ms-administracion | AdminRestController.auditoria(desde, hasta) | GET /api/admin/auditoria?desde=…&hasta=… (rango obligatorio) |
| Nueva | Administrador | Logs de errores | ms-administracion | AdminRestController.logs(servicio) | GET /api/admin/logs?servicio=… (servicio opcional; 503 si Elasticsearch no responde) |
| Nueva | Administrador | Usuarios | ms-identidad | UsuarioRestController.registrar(nombre, correo, rol, sucursalId, clave) | POST /api/usuarios |
| Nueva | Administrador | Destinatarios de alertas | ms-notificaciones | DestinatarioRestController.consultar(sucursalId) y registrar(sucursalId, correo) | GET y POST /api/notificaciones/destinatarios |
!nota En la ventana 21, ms-ordenes pide los datos de la cotización que la ventana muestra con el mismo GET /api/cotizaciones/{id}. «Nueva» marca las ventanas que no estaban entre las 23 del prototipo. Cada servicio comprueba el rol de quien llama.

!vertical
