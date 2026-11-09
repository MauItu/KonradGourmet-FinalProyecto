<!-- §15. Correcciones: R-52 (filas de V03, V06, V13, V20 y V21) y R-60 (meseroId en V03). R-51 ya es cierto. R-53 es opcional y no se aplica. La sección va en páginas horizontales para que la tabla quepa en 12 pt. -->
!horizontal

# 15. Trazabilidad de las 23 ventanas

Cada ventana del prototipo está asignada a un servicio, a un método de su controlador REST y a un endpoint (tabla 13). La columna de servicio es la misma que aparece bajo cada miniatura en el mapa, y cada método lleva el comentario de la ventana que atiende. Algunas ventanas usan más de un método: por ejemplo, la 03 carga el catálogo y las mesas antes de registrar el pedido.

!tabla 13 | Trazabilidad de las ventanas a servicios, métodos y endpoints | 1.1,2.2,3.2,3.4,7.5,7.0
| V | Rol | Ventana | Servicio | Controlador y método | Endpoint |
|---|---|---|---|---|---|
| 01 | Acceso | Inicio de sesión | ms-identidad | AuthRestController.iniciarSesion() | POST /api/auth/token |
| 02 | Mesero | Inicio del mesero | ms-pedidos | PedidoRestController.pedidosDelTurno() | GET /api/pedidos?turno=actual |
| 03 | Mesero | Nuevo pedido | ms-pedidos | PedidoRestController.registrar() | POST /api/pedidos (el pedido guarda el meseroId del token JWT, HU-01) |
| 03 | Mesero | Nuevo pedido | ms-pedidos | PedidoRestController.catalogo(sucursalId) | GET /api/pedidos/catalogo |
| 03 | Mesero | Nuevo pedido | ms-pedidos | PedidoRestController.mesas(sucursalId) | GET /api/pedidos/mesas |
| 04 | Mesero | Pedido registrado | ms-pedidos | PedidoRestController.registrar(), respuesta 201 | 201 Created, PED-0251 |
| 05 | Jefe de cocina | Inicio del jefe | ms-inventario | InventarioRestController.alertas() | GET /api/inventario/alertas |
| 06 | Jefe de cocina | Nuevo plato | ms-menu | PlatoRestController.crear() | POST /api/platos |
| 06 | Jefe de cocina | Nuevo plato | ms-menu | PlatoRestController.productosPorCategoria(categoriaId) | Bajo /api/platos (ruta sin fijar) |
| 07 | Jefe de cocina | Inventario | ms-inventario | InventarioRestController.insumos() | GET /api/inventario/insumos |
| 08 | Jefe de cocina | Nueva solicitud | ms-solicitudes | SolicitudRestController.registrar() | POST /api/solicitudes |
| 09 | Jefe de cocina | Solicitud registrada | ms-solicitudes | SolicitudRestController.registrar(), respuesta 201 | 201 Created, SOL-031 |
| 10 | Jefe de cocina | Mis solicitudes | ms-solicitudes | SolicitudRestController.misSolicitudes() | GET /api/solicitudes?solicitante=yo |
| 11 | Auxiliar de compras | Inicio del auxiliar | ms-solicitudes | SolicitudRestController.consultar() | GET /api/solicitudes?estado=PENDIENTE |
| 12 | Auxiliar de compras | Inicio con Generar cotización | ms-solicitudes | SolicitudRestController.detalle() | GET /api/solicitudes/{id} |
| 13 | Auxiliar de compras | Generar solicitud de cotización | ms-cotizaciones | CotizacionRestController.generar() | POST /api/cotizaciones |
| 13 | Auxiliar de compras | Generar solicitud de cotización | ms-cotizaciones | ProveedorRestController.consultar(soloActivos): el desplegable de proveedores activos | GET /api/proveedores |
| 14 | Auxiliar de compras | Cotización enviada | ms-cotizaciones | CotizacionRestController.pdf() | GET /api/cotizaciones/{id}/pdf |
| 15 | Auxiliar de compras | Cotizaciones | ms-cotizaciones | CotizacionRestController.consultar() | GET /api/cotizaciones |
| 16 | Director de compras | Inicio del director | ms-cotizaciones | CotizacionRestController.pendientesDeAtencion() | GET /api/cotizaciones/pendientes |
| 17 | Director de compras | Todas las cotizaciones | ms-cotizaciones | CotizacionRestController.consultar() | GET /api/cotizaciones |
| 18 | Director de compras | Registrar valores | ms-cotizaciones | CotizacionRestController.registrarValores() | PUT /api/cotizaciones/{id}/valores |
| 19 | Director de compras | Validación completada | ms-cotizaciones | CotizacionRestController.validar() | POST /api/cotizaciones/{id}/validacion |
| 20 | Director de compras | Cotizaciones opcionadas | ms-cotizaciones | CotizacionRestController.opcionadas() | GET /api/cotizaciones?estado=OPCIONADA |
| 20 y 21 | Director de compras | Cotizaciones opcionadas y Generar orden de compra | ms-cotizaciones | CotizacionRestController.detalle(numero) | GET /api/cotizaciones/{id} |
| 21 | Director de compras | Generar orden de compra | ms-ordenes | OrdenCompraRestController.generar() | POST /api/ordenes-compra |
| 22 | Director de compras | Orden generada | ms-ordenes | OrdenCompraRestController.generar(), respuesta 201 | 201 Created, OC-0012 |
| 23 | Director de compras | Órdenes de compra | ms-ordenes | OrdenCompraRestController.consultar() | GET /api/ordenes-compra |
!nota En la ventana 21, ms-ordenes pide los datos de la cotización que la ventana muestra con el mismo GET /api/cotizaciones/{id}. Donde dice «ruta sin fijar», ninguna fuente fija la ruta exacta y no se inventa.

!vertical
