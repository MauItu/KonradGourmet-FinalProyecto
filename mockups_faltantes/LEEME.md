# Mockups faltantes · Konrad Gourmet

Los mockups que faltaban después de `mockups_admin/` (las seis ventanas del administrador y los cambios a V05, V07, V14, V20 y V23) y de `mockups_nuevos/07_proveedores` (HU-15): las dos ventanas nuevas de ms-analitica (CD-27 y CD-28) y las correcciones M-01 a M-09 de `CORRECCIONES_HU_Y_MOCKUPS.md` que quedaban abiertas.

Cada estado tiene dos imágenes: `nombre.png`, en escritorio (**1440×900**), y `nombre_movil.png`, a **390×844**. Cuando el contenido no cabe en la pantalla, `nombre_completa.png` y `nombre_movil_completa.png` muestran la ventana entera.

## Qué corrige cada carpeta

| Carpeta | Ventana | Corrección | Estados |
| --- | --- | --- | --- |
| [tablero_bam](tablero_bam/) | Tablero BAM (HU-18, CU-14), director comercial | CD-27 | `con_datos`, `sucursal_norte`, `rango_de_fechas`, `rango_invertido`, `sin_datos` |
| [promociones_sugeridas](promociones_sugeridas/) | Promociones sugeridas (HU-19, CU-06), jefe de cocina | CD-28 (y M-08 para HU-19) | `sugeridas`, `decididas`, `ya_resuelta`, `sin_datos` |
| [V13_generar_cotizacion](V13_generar_cotizacion/) | V13 · Generar solicitud de cotización | M-01 | `varias_solicitudes`, `sin_solicitud` |
| [V17_cotizaciones](V17_cotizaciones/) | V17 · Todas las cotizaciones | M-03 | `COT-018_opcionada`, `COT-018_con_orden` |
| [V18_registrar_valores](V18_registrar_valores/) | V18 · Registrar valores de cotización | M-05 | `COT-018` |
| [V19_validacion](V19_validacion/) | V19 · Validación completada (con el fondo de V17) | M-03 | `COT-018_opcionada` |
| [V21_generar_orden](V21_generar_orden/) | V21 · Generar orden de compra | M-02 y M-03 | `COT-018` |
| [V22_orden_generada](V22_orden_generada/) | V22 · Orden generada | M-03 | `OC-0012` |
| [V03_nuevo_pedido](V03_nuevo_pedido/) | V03 · Nuevo pedido | M-04 y M-09 | `pedido_82000` |
| [V04_pedido_registrado](V04_pedido_registrado/) | V04 · Pedido registrado | M-04 | `pedido_82000` |

## Los datos

Son los de la demostración (M-03, DEC-49), los mismos en todas las ventanas:

| Número | Proveedor | Total | Diferencia | Estado |
| --- | --- | --- | --- | --- |
| COT-018 | Proveedor ABC (res $21.250/kg, arroz $8.000/kg) | $545.000 | +4,81 % | OPCIONADA y luego CON ORDEN |
| COT-019 | Proveedor C | $700.000 | +34,62 % | RECHAZADA |
| COT-020 | Proveedor D | $200.000 | −61,54 % | SOSPECHOSA |
| COT-021 | Proveedor B (res $22.000/kg, arroz $8.000/kg) | $560.000 | +7,69 % | OPCIONADA |
| OC-0012 | COT-018, Proveedor ABC (res 20 kg $425.000, arroz 15 kg $120.000) | $545.000 | — | EN PROCESO |

Las cuatro cotizaciones son de SOL-031 (res 20 kg y arroz 15 kg, Sucursal Centro), con el estándar del Ministerio en $520.000. En V13 hay además SOL-032 (Sucursal Norte: res 10 kg y papa 8 kg) y SOL-033 (Sucursal Centro: papa 12 kg), para mostrar varias solicitudes. El pedido de V03 y V04 es de 2 bandejas paisas y 1 ajiaco, $82.000, en la mesa 5, tomado por Isabella Torres. El tablero BAM y las promociones usan el histórico de ventas y las tendencias de la semana; «ya resuelta» muestra la sugerencia de la Bandeja paisa, que Sofía Ramírez (jefa de cocina de la Sucursal Norte) aceptó mientras Carlos Mendoza tenía la lista abierta.

## Qué dice cada corrección

- **CD-27 · Tablero BAM.** Filtros «Desde», «Hasta» y «Sucursal», «Consultar» y «Restablecer periodo», la nota del periodo y las tres tarjetas (producto, sucursal y proveedor) con el periodo aplicado, el ganador, su valor y la gráfica de los cinco primeros. Con la Sucursal Norte cambia la demanda y no el proveedor (las compras son centrales). Con un rango, los tres indicadores usan ese rango. Rango invertido: «La fecha inicial no puede ser posterior a la fecha final.». Sin datos: «No hay información suficiente para el periodo seleccionado» en las tres tarjetas. A 390 px, las tarjetas van una debajo de otra.
- **CD-28 · Promociones sugeridas.** Contadores «Por revisar (vigentes)», «Aceptadas» y «Descartadas»; el histórico con producto, motivo, vigencia, estado y «Aceptar» o «Descartar». Decididas: el Ajiaco aceptado y la Limonada natural descartada. Ya resuelta: «Esta sugerencia ya fue aceptada o descartada.». Sin datos: «No hay promociones sugeridas para el periodo seleccionado.».
- **M-01 · V13.** El bloque «Solicitudes a incluir» con casillas (número, productos, fecha y estado); SOL-031 viene marcada y, al marcar SOL-032, «Productos a cotizar» y «Resumen» suman la res de las dos (30 kg). Sin ninguna marcada: «Debe seleccionar al menos una solicitud» y el botón deshabilitado.
- **M-02 · V21.** La etiqueta sigue el número de solicitudes de la cotización: «Solicitud: SOL-031» con una y «Solicitudes: SOL-031, SOL-032» con varias (DEC-93).
- **M-03 · V17, V19, V21 y V22.** Los datos de la tabla anterior; «Proveedor A» pasa a «Proveedor ABC». V20 y V23 ya estaban en `mockups_admin/`.
- **M-04 y M-09 · V03 y V04.** El pedido de 3 platos por $82.000 en las dos ventanas, y el saludo de V03 es el del mesero: Isabella Torres · Mesero · Sucursal Centro.
- **M-05 · V18.** El precio por unidad: el estándar del Ministerio ($20.000/kg y $8.000/kg) y el del proveedor ($21.250/kg y $8.000/kg), con el valor de la línea debajo ($425.000 y $120.000). V19 muestra solo los totales y la diferencia, sin líneas de producto, así que ahí no hay columna que corregir (DEC-93).
- **M-07** no se hace (opcional, DEC-93).
