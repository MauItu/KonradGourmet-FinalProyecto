<!-- §11. Correcciones: R-33 y R-34. La nota opcional de tendencia.detectada (DEC-07) no se aplica. -->
# 11. Eventos del sistema

Los diez tópicos del broker salen de las costuras entre roles que se identificaron en las ventanas. Cada uno tiene un único productor (salvo la auditoría, que publican los otros diez servicios) y uno o más consumidores, como resume la tabla 10.

!tabla 10 | Tópicos del broker de eventos | 3.9,3.1,3.6,5.6
| Tópico | Publica | Consumen | Qué lo dispara y para qué |
|---|---|---|---|
| menu.actualizado | ms-menu | ms-pedidos, ms-inventario | Plato creado o editado (06): actualiza el catálogo del mesero (03) y la copia local de las recetas con la que ms-inventario calcula el consumo |
| pedido.registrado | ms-pedidos | ms-inventario, ms-analitica | Pedido creado (03/04), con sus líneas (plato y cantidad): consumo para el descuento diario y KPI de demanda |
| inventario.stock-bajo | ms-inventario | ms-notificaciones, ms-analitica | Existencia por debajo del 25 % (porcentaje redondeado a 2 decimales), publicado la noche en que el insumo cruza el umbral: alarma en la app (05) y correo al jefe |
| solicitud.registrada | ms-solicitudes | ms-notificaciones, ms-analitica, ms-inventario | Solicitud PENDIENTE (08/09): correo de confirmación, KPI y alerta de stock atendida (HU-16) |
| solicitud.en-tramite | ms-solicitudes | ms-notificaciones | Todos los alimentos cotizados: el jefe ve EN TRÁMITE (10) |
| cotizacion.enviada | ms-cotizaciones | ms-solicitudes, ms-notificaciones | Cotización ENVIADA (13/14): PDF por correo al proveedor y revisión de EN TRÁMITE |
| cotizacion.validada | ms-cotizaciones | ms-analitica | RECHAZADA, SOSPECHOSA u OPCIONADA (19): KPI de proveedor con mejores precios |
| orden.generada | ms-ordenes | ms-solicitudes, ms-cotizaciones, ms-notificaciones, ms-analitica | Orden de compra (21/22): cada solicitud pasa a COMPLETADA, la cotización a CON ORDEN y se avisa al proveedor con el PDF de la orden |
| auditoria.evento | Los otros 10 servicios | ms-administracion | Cada CRUD de los otros 10 servicios (ms-administracion registra los suyos directo): acción, usuario, fecha y hora |
| tendencia.detectada | ms-analitica | ms-notificaciones | Productos más solicitados o tendencia en redes: correo de promociones a los jefes |
