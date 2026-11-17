# Cambios a documentar · Konrad Gourmet

Lo que la plataforma (rama `stack-real` de KonradGourmet, DEC-58) agrega, quita o cambia frente a la documentación: los mockups (`DISEÑO UXUI.pdf`), el `.drawio`, el reporte, los casos de uso y las guías. Cada cambio dice qué artefacto se toca, dónde y por qué decisión. La regla es DEC-77 (Mauricio, 2026-11-17).

- **Mockups:** los cambia Mauricio en Canva, como M-01 a M-09 de `CORRECCIONES_HU_Y_MOCKUPS.md`.
- **`.drawio`:** se aplica al decidir cuando cambia una firma (por ejemplo, DEC-75); el resto se revisa en el corte 7.
- **Reporte:** se regenera en el corte 7 (por DEC-57, describe el proyecto completo, sin hablar del prototipo).
- **Estado:** *pendiente* o *aplicado (fecha)*.

## Corte 5 (ms-ordenes y ms-notificaciones)

| # | Artefacto | Dónde | Cambio | Origen | Estado |
|---|---|---|---|---|---|
| CD-01 | Mockup | V23 «Órdenes de compra + detalle» (p. 30), tarjeta «Detalle de orden de compra» | Agregar el botón «Ver PDF» bajo el total, que abre el PDF de la orden | DEC-75 | Pendiente |
| CD-02 | Reporte | Tabla de rutas REST (tabla 5) y CU-13 | Agregar `GET /api/ordenes-compra/{id}/pdf` (directora de compras) | DEC-75 | Pendiente (corte 7) |
| CD-03 | `.drawio` o reporte | Página 4d (ms-notificaciones) o la sección de clases de infraestructura | `EnviosController` (`GET /api/notificaciones/envios?correlacion=…`, solo lectura) y la columna `correlacion_id` de los envíos: decidir si se dibuja en la 4d o se nombra en el texto | DEC-76 | Pendiente (corte 7) |
| CD-04 | Mockup | V14 «Cotización generada con éxito» (p. 20), fila de marcas | «Correo enviado» tiene tres estados: esperando (sin marca), enviado (✓) y «Correo no enviado», con el mensaje de PD-10 («No se pudo enviar el correo al proveedor…»; se reenvía desde V15) | DEC-76 | Pendiente |
| CD-05 | Mockup | V20 «Cotizaciones opcionadas» (p. 27), tabla «Comparación de cotizaciones» y filtros | Agregar las columnas Nº y Fecha, el selector «Ordenar por» (diferencia, valor total o número) y el mensaje «No hay cotizaciones opcionadas disponibles», que pide HU-17 | DEC-78 | Pendiente |
| CD-06 | Mockup | V05 y V07 (pp. 10 y 12) | La alarma de stock bajo en vivo: un aviso arriba a la derecha, «Alarma de stock bajo · Pollo al 24,5 %: realice una solicitud», con «Solicitar» y la hora, hasta cerrarlo | DEC-78, PD-01 | Pendiente |
| CD-07 | `.drawio` o reporte | Página 4d (ms-notificaciones), junto a `AlarmaPushAdapter` | `CanalAlarmas` (entrega por WebSocket en cada réplica, escucha el canal de Redis) y `ConfiguracionWebSocket`, fuera del diagrama; la ruta `/api/notificaciones/alarmas` con el token en `?access_token=` | DEC-78 | Pendiente (corte 7) |
| CD-08 | `.drawio` y reporte | Página 3a (caja de Kafka) y nota `n_fallos` de la 4d; sección de eventos del reporte | Nombrar la cola de errores: `notificaciones.dlq`, con 2 reintentos cada 2 s antes de llegar a ella | DEC-78 | Pendiente (corte 7) |
| CD-09 | `.drawio` o reporte | Página 4d (ms-ordenes) | `NombresProductos` de ms-ordenes (copia local del nombre de cada producto, tabla `producto_nombre`), fuera del diagrama, como el de ms-cotizaciones (CD-53) | DEC-78 | Pendiente (corte 7) |
| CD-10 | Reporte | Tabla de rutas REST (tabla 5) | Agregar `GET /api/notificaciones/envios?correlacion=…` (DEC-76) y el WebSocket `/api/notificaciones/alarmas` (PD-01) | DEC-76, DEC-78 | Pendiente (corte 7) |

## Cortes 1 a 4b (anotados después, el 2026-11-17)

| # | Artefacto | Dónde | Cambio | Origen | Estado |
|---|---|---|---|---|---|
| CD-50 | Reporte | §13 «191 clases» | Pasa a 195: `EmisorTokenPort` y `JwtEmisorAdapter` (DEC-62), `AlmacenArchivosPort` y `AlmacenObjetosAdapter` de ms-menu (DEC-67) | DEC-62, DEC-67 | Pendiente (corte 7) |
| CD-51 | Mockup | V07 «Inventario + alertas» (p. 12) | Botón «Cierre del día (23:59)», solo para la demostración, que adelanta el cierre diario | DEC-68 | Pendiente: revisar si se dibuja o se deja como atajo de la demostración |
| CD-52 | `.drawio` o reporte | Página 4b (ms-inventario) | `CierreDiaController` (`POST /api/inventario/cierre`) fuera del diagrama | DEC-68 | Pendiente (corte 7) |
| CD-53 | `.drawio` o reporte | Página 4c (ms-cotizaciones) | `NumeracionCotizaciones` (el número se pide antes de generar) y `NombresProductos` (copia local del nombre de cada producto, tabla `producto_nombre`) fuera del diagrama | DEC-74 | Pendiente (corte 7) |
| CD-54 | `.drawio` o reporte | Páginas 4a y 4e, y la sección de seguridad del reporte | Infraestructura fuera del diagrama: `LlaveFirma`, `JwksController` y `DatosIniciales` (ms-identidad); `FormatoEventos`, `SeguridadServicio`, `ManejoErrores`, `Sesion`, `ContextoRecorrido`, `FiltroContexto`, `InterceptorCorrelacion` y `ConvertidorEventos` (compartido, DEC-64); `SeguridadGateway` y `CorrelacionGateway`; `MapaPreciosRedis` (ms-integracion) | DEC-62, DEC-64, DEC-71 | Pendiente (corte 7) |
| CD-55 | Reporte | Tabla de rutas REST y CU de precios | El REST interno de precios es `GET /interno/precios/{productoId}`, fuera del gateway, con el JWT del auxiliar o del director de compras | DEC-72 | Pendiente (corte 7) |
| CD-56 | Mockup | Ventana de proveedores (HU-15, sin mockup) | Ya está como M-08 (opcional) en `CORRECCIONES_HU_Y_MOCKUPS.md`; la plataforma la tiene con las piezas de V12 | DEC-07, DEC-74 | Pendiente (M-08) |
