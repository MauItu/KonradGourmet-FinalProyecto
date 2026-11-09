<!-- §10. Correcciones: R-29, R-30, R-31, R-32 y R-02 (figura 7, página 6 del .drawio). -->
!horizontal

# 10. Arquitectura por flujo de ventanas

Las tres arquitecturas están presentes en todo el sistema, pero en cada flujo una de ellas pesa más. La tabla 8 indica cuál y por qué, a partir de lo que muestran las ventanas.

!tabla 8 | Arquitectura dominante en cada flujo de ventanas | 2.6,1.8,3.4,3.2,9
| Flujo | Ventanas | Servicios | Domina | Por qué |
|---|---|---|---|---|
| Acceso | V01 | ms-identidad | Microservicios y OAuth 2.0 | Todos los roles entran por aquí. Separarlo permite escalarlo en los picos de inicio de turno y que una falla de otro servicio no impida autenticarse. El menú lateral de cada rol viaja en el token. |
| Mesero: pedidos | V02 a V04 | ms-pedidos (catálogo por evento de ms-menu) | Microservicios y eventos | Es el flujo de mayor volumen. La respuesta es síncrona e inmediata (PED-0251) y el descuento de inventario y los KPI ocurren después, con pedido.registrado. |
| Jefe: platos | V06 | ms-menu | Hexagonal y eventos | CRUD con reglas de receta (categoría, producto, cantidad, unidad). menu.actualizado refresca el catálogo del mesero sin acoplar servicios. |
| Jefe: inventario y alarmas | V05 y V07 | ms-inventario, ms-notificaciones | Eventos | La tarea diaria descuenta el consumo, evalúa el umbral del 25 % y publica inventario.stock-bajo cuando un insumo baja de ese umbral, que se convierte en alarma en la app (WebSocket) y en correo. La alerta queda atendida cuando el jefe registra una solicitud (solicitud.registrada). |
| Jefe: solicitudes | V08 a V10 | ms-solicitudes | Hexagonal y eventos | La validación de formato vive en el dominio. El paso de PENDIENTE a EN TRÁMITE no lo hace el jefe sino un evento de ms-cotizaciones. |
| Auxiliar: cotizaciones | V11 a V15 | ms-solicitudes, ms-cotizaciones, ms-integracion, ms-notificaciones | Eventos, hexagonal y SOA | Un clic (Facade) dispara precio del Ministerio por SOAP, PDF, correo al proveedor y cambio de estado de la solicitud, sin bloquear al auxiliar. |
| Director: validación | V16 a V20 | ms-cotizaciones | Hexagonal | La validación es una regla de negocio pura y comprobable: más de 25 % por encima, RECHAZADA; más de 50 % por debajo, SOSPECHOSA; el resto, OPCIONADA, incluidos +25,00 % y −50,00 % (Chain of Responsibility). La diferencia se redondea a 2 decimales antes de comparar. |
| Director: órdenes | V21 a V23 | ms-ordenes | Microservicios y REST | El director espera el número de orden, así que la consulta a ms-cotizaciones es síncrona; la propagación (solicitud, proveedor, KPI) es asíncrona con orden.generada. |
| Director comercial: BAM | Sin mockup | ms-analitica | Eventos y Strategy | Los KPI se calculan sobre un almacén propio alimentado por eventos, sin consultar las bases de otros servicios. Sin filtro, la demanda se mide sobre el último mes y el proveedor con mejores precios sobre el último año; si el director elige un rango de fechas, los tres indicadores usan ese rango, y la sucursal filtra la demanda (HU-18). El proveedor se mide con la diferencia promedio de sus cotizaciones OPCIONADAS frente al precio estándar. |
| Administrador | Sin mockup | ms-administracion | Eventos y Singleton | La auditoría de cada CRUD llega por auditoria.evento; los parámetros y la imagen corporativa viven en ParametrosSistema. |
| Sistemas externos | Ninguna | ms-integracion | SOA, Proxy y Adapter | Cumple el requerimiento de integración y aísla al resto del sistema de SOAP. |

## 10.1 Recorrido completo de una compra

El flujo más largo del sistema, de la solicitud del jefe a la orden de compra, muestra cómo se alternan las llamadas síncronas (el usuario espera) y los eventos (el sistema reacciona por su cuenta). La tabla 9 lo recorre paso a paso.

!tabla 9 | Recorrido completo de una compra | 0.7,2,5.6,2,9.6
| # | Ventana | Qué pasa | Tipo | Mensaje |
|---|---|---|---|---|
| 1 | V08 a V09 | El jefe registra la solicitud de alimentos | Síncrono | POST /api/solicitudes, que responde 201 SOL-031 PENDIENTE |
| 2 | Ninguna | Confirmación al jefe por correo y alerta de stock atendida | Evento | solicitud.registrada hacia ms-notificaciones (correo), ms-analitica (KPI) y ms-inventario (atiende la alerta de stock bajo) |
| 3 | V11 a V12 | El auxiliar filtra solicitudes y abre el detalle | Síncrono | GET /api/solicitudes y GET /api/solicitudes/{id} |
| 4 | V13 a V14 | El auxiliar genera la solicitud de cotización | Síncrono | POST /api/cotizaciones con una o varias solicitudes (aquí SOL-031): precio del Ministerio (REST y luego SOAP), PDF al almacén, ENVIADA |
| 5 | Ninguna | PDF al proveedor y revisión de la solicitud | Evento | cotizacion.enviada hacia ms-notificaciones y ms-solicitudes |
| 6 | V10 | La solicitud del jefe pasa a EN TRÁMITE | Evento | solicitud.en-tramite: aviso al jefe |
| 7 | Ninguna | El proveedor responde por correo | Fuera del sistema | Ninguno |
| 8 | V18 | El director registra los valores del proveedor | Síncrono | PUT /api/cotizaciones/{id}/valores |
| 9 | V19 | El director valida la cotización | Síncrono | POST /api/cotizaciones/{id}/validacion: cadena de reglas, OPCIONADA |
| 10 | Ninguna | KPI de proveedor con mejores precios | Evento | cotizacion.validada hacia ms-analitica |
| 11 | V20 a V22 | El director elige la opcionada y genera la orden | Síncrono | POST /api/ordenes-compra (GET /api/cotizaciones/{id} a ms-cotizaciones), que responde 201 OC-0012 |
| 12 | Ninguna | La orden se propaga | Evento | orden.generada hacia ms-solicitudes (cada solicitud a COMPLETADA), ms-cotizaciones (CON ORDEN), ms-notificaciones (PDF al proveedor) y ms-analitica |
| 13 | V23 | Seguimiento de órdenes | Síncrono | GET /api/ordenes-compra |
| 14 | Todas | Auditoría de cada CRUD | Evento | auditoria.evento de los otros diez servicios hacia ms-administracion |

La figura 7 dibuja el mismo recorrido como diagrama dinámico del C4.

!vertical

!figura 7 | figuras/6.png | Diagrama dinámico del recorrido de compra | Página 6 del archivo Arquitectura_Konrad_Gourmet.drawio. Los 14 pasos de la tabla 9 en forma de secuencia, con cada mensaje numerado (4.1, 4.2 y así sucesivamente) y su protocolo. En cada evento se dibujan todos sus consumidores. El ejemplo es el de los mockups: SOL-031, COT-018 y OC-0012, con los valores de V19.
