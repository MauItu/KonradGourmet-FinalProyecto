# Correcciones a las historias de usuario y a los mockups · Konrad Gourmet

Este archivo reúne lo que hay que cambiar en dos documentos que **no se modifican desde aquí**:
- **El Story Mapping** (`../proyecto/contexto/Visual Story Mapping.pdf`, 32 págs.): las 19 historias de usuario son imágenes; la tarjeta de la HU-n está en la página 7+n.
- **Los mockups** (`DISEÑO UXUI.pdf`, 31 págs., hechos en Canva): V01 a V23, cada uno con su captura y su wireframe.

Los dos están fuera del alcance de las correcciones (`PLAN_CORRECCIONES.md`) y los edita el equipo. Cada ítem dice **dónde está**, **qué dice hoy**, **por qué hay que cambiarlo** y **qué debe decir**.

**Cómo leerlo**
- Cada corrección tiene un id **M-xx** (mockups) o **S-xx** (Story Mapping) para poder citarla.
- **Obligatoria:** el documento contradice a la arquitectura, al código o a otra parte de los mismos documentos.
- **Opcional:** mejora lo entregado, pero nada se rompe sin ella.
- **Por confirmar:** depende de una decisión asumida.
- La página de cada mockup es la del PDF `DISEÑO UXUI.pdf`: V13 = p. 19, V21 = p. 28, etc. (no siempre V-n = p. n+2: ver la tabla de la sección 1).

**Lo que ya está resuelto en la arquitectura y por eso no se cambia en los documentos:** una cotización agrupa una o varias solicitudes (DEC-05); los KPI son del director comercial (DEC-06); HU-15 y HU-19 están dentro del alcance (DEC-07); el pedido de la demo es de $82.000 y el precio es por unidad (DEC-40).

## Para revisar con Mauricio

Mauricio revisa los mockups y el Story Mapping por su cuenta (2026-11-08); no entran en la sesión 6. Lo que queda abierto o por mirar:

| Id | Qué hay que mirar | Estado |
|---|---|---|
| **M-04** | V03 muestra 4 platos con limonada ($88.000); V04, el reporte y la demo dicen 3 platos y $82.000. Opción 1: quitar la limonada de V03 (recomendada, no cambia nada más). Opción 2: pasar todo a $88.000 (cambia reporte, demo y pruebas) | **Abierto: decide Mauricio** |
| M-01 · M-02 | V13 y V21 con varias solicitudes por cotización (DEC-05) | Decidido; falta hacerlo en Canva |
| M-03 | V17 y V19 a V23 con los datos de la demo: COT-018, Proveedor ABC, OC-0012 por $545.000 (DEC-49, arquitecto) | Decidido; falta hacerlo en Canva |
| M-06 | V05 sin Arroz ni Pollo, tarjeta en «3 Stock bajo» (DEC-50, arquitecto) | Decidido; falta hacerlo en Canva |
| M-05 · M-07 · M-08 · M-09 | Precio unitario en V18/V19, detalle en V12, pantallas nuevas de HU-15 y HU-19, saludo de V03 | Opcionales |
| S-01 a S-07 | HU-07 reescrita, «(224)», «25 h», «Ver KPIs» del director comercial, p. 31 con 5 sprints y 10 semanas | Aplicadas en el documento nuevo `STORY_MAPPING_CORREGIDO.md` (2026-11-09), con SM-01 a SM-04; el PDF original queda igual |

---

## 1. Dónde está cada mockup

| Ventana | Pantalla | Página del PDF |
|---|---|---|
| V01 | Iniciar sesión | 3 y 4 |
| V02 | Mesero · dashboard | 6 (la p. 5 es el diagrama de flujo) |
| V03 · V04 | Mesero · pedidos (nuevo pedido y pedido registrado) | 7 y 8 |
| V05 | Jefe de cocina · dashboard con alertas | 10 |
| V06 | Jefe de cocina · platos | 11 |
| V07 | Jefe de cocina · inventario y alertas | 12 |
| V08 · V09 | Jefe de cocina · solicitar alimento y solicitud registrada | 13 y 14 |
| V10 | Jefe de cocina · solicitudes | 15 |
| V11 · V12 | Auxiliar · dashboard y solicitudes | 17 y 18 |
| V13 | Auxiliar · generar cotización | **19** |
| V14 | Auxiliar · cotización generada con éxito | 20 |
| V15 | Auxiliar · cotizaciones | 21 |
| V16 · V17 | Director · dashboard y todas las cotizaciones | 23 y 24 |
| V18 | Director · registrar valores | **25** |
| V19 | Director · validar resultados | **26** |
| V20 | Director · cotizaciones + detalle | 27 |
| V21 | Director · generar orden de compra | **28** |
| V22 | Director · orden generada | 29 |
| V23 | Director · órdenes de compra + detalle | 30 |

(Las páginas 2, 5, 9, 16 y 22 son el índice y los diagramas de flujo de cada rol, y la 31 es la carátula final; no se tocan.)

---

## 2. Mockups

### M-01 · V13 «Generar solicitud de cotización» con varias solicitudes · Obligatoria (DEC-05)
**Dónde:** `DISEÑO UXUI.pdf`, p. 19, captura y wireframe.
**Qué dice hoy:** un modal con un campo fijo, **«Solicitud origen: SOL-031»**, no editable. Debajo, la lista «Productos a cotizar» con casillas (Res y Arroz marcadas), el proveedor «Proveedor ABC», la fecha máxima, el resumen (Res 20 kg, Arroz 15 kg) y el botón «Generar y enviar cotización».
**Por qué hay que cambiarlo:** la tarjeta HU-09 (Story Mapping, p. 16) dice «Debo poder seleccionar **una o varias solicitudes** del listado para incluir en la cotización», que si no se selecciona ninguna debe salir «Debe seleccionar al menos una solicitud» y que hay que ver un resumen de productos y cantidades antes de confirmar. La arquitectura sigue la tarjeta: `Cotizacion.solicitudNumeros` es una lista, y la demo arma COT-018 con la lista de solicitudes.
**Qué debe decir:**
1. **Quitar** el bloque «Solicitud origen» (elemento 2 del wireframe).
2. **Agregar** un bloque **«Solicitudes a incluir»** con una lista de casillas: número (SOL-031…), sucursal, fecha y estado. Solo aparecen las solicitudes que se pueden cotizar. Si el auxiliar llegó desde la solicitud SOL-031 (V12), esa casilla ya viene marcada.
3. **Mensaje de error** debajo del bloque cuando no hay ninguna marcada: «Debe seleccionar al menos una solicitud». El botón «Generar y enviar cotización» queda deshabilitado mientras tanto.
4. **«Productos a cotizar»** pasa a mostrar los productos de **todas** las solicitudes marcadas, con la cantidad que se pide en cada una.
5. **«Resumen»** muestra los productos y las cantidades **sumados** (por ejemplo, Res 20 kg y Arroz 15 kg para SOL-031; si se marca otra solicitud con res, la fila de Res suma las dos).
6. Actualizar la **leyenda numerada** del wireframe con el bloque nuevo y renumerar los elementos.

**Lo que la arquitectura ya hace:** `POST /api/cotizaciones` recibe una o varias solicitudes; `cotizacion.enviada` lleva la lista; ms-solicitudes pasa a EN TRÁMITE cada solicitud cuyos productos ya están todos cotizados, y cuando llega `orden.generada` todas pasan a COMPLETADA.

### M-02 · V21 «Generar orden de compra» con varias solicitudes · Obligatoria (DEC-05)
**Dónde:** p. 28, captura y wireframe.
**Qué dice hoy:** un modal con «Cotización: COT-019», **«Solicitud: SOL-031»** y «Proveedor: Proveedor B», la tabla de productos y el total.
**Por qué:** una cotización puede incluir varias solicitudes (M-01), así que un solo campo «Solicitud» no alcanza.
**Qué debe decir:** cambiar «Solicitud» por **«Solicitudes»**, con la lista de números separados por coma (SOL-031 o, por ejemplo, SOL-031, SOL-034). El wireframe y su leyenda se ajustan igual.

### M-03 · V17 y V19 a V23: una sola historia de cotizaciones y órdenes · Obligatoria (DEC-49, decidida por el arquitecto)
**Dónde:** V17 (p. 24), el fondo de V19 (p. 26), V20 (p. 27), V21 (p. 28), V22 (p. 29) y V23 (p. 30).
**Qué dicen hoy:** V19 valida COT-018 (Proveedor A, $545.000, +4,81 %). V20 compara tres opcionadas (A $545.000, **B $510.000**, C $570.000) y resalta la de Proveedor B; V21 y V22 generan **OC-0012 sobre COT-019, Proveedor B, por $510.000** (res $395.000 y arroz $115.000); V23 lista «OC-012» y detalla OC-0012 con COT-019. V17 y los fondos de V19, V21 y V22 repiten otra lista: COT-019 Proveedor B OPCIONADA, COT-020 Proveedor C RECHAZADA, COT-021 Proveedor D ENVIADA (en el fondo de V22, COT-021 es de Proveedor A).
**Por qué:** la demo, la página 6 y el reporte hacen OC-0012 sobre COT-018 por $545.000, y los mockups se contradicen entre sí (Proveedor C está RECHAZADA en V17 y OPCIONADA con $570.000 en V20; las fechas de V23 son de 2026 y las demás de 2025). No hay una historia de los mockups que adoptar.
**Qué debe decir:** los datos de la demo en todas las pantallas:

| Número | Proveedor | Total | Diferencia | Estado |
|---|---|---|---|---|
| COT-018 | Proveedor ABC | $545.000 | +4,81 % | OPCIONADA, luego CON ORDEN |
| COT-019 | Proveedor C | $700.000 | +34,62 % | RECHAZADA |
| COT-020 | Proveedor D | $200.000 | −61,54 % | SOSPECHOSA |
| COT-021 | Proveedor B (res $22.000/kg, arroz $8.000/kg) | $560.000 | +7,69 % | OPCIONADA (solo en los mockups, para que V20 compare dos opcionadas, como pide HU-17) |
| OC-0012 | COT-018, Proveedor ABC (res $425.000, arroz $120.000) | $545.000 | — | EN PROCESO |

Cambios por página: **V17** la lista de cotizaciones; **V19** solo la lista del fondo; **V20** las filas y el panel de detalle (COT-018 resaltada, COT-021 como segunda opcionada); **V21** el modal, el fondo y la leyenda (COT-018, Proveedor ABC, $545.000); **V22** el modal, el fondo y la leyenda (OC-0012, Proveedor ABC, $545.000); **V23** la fila y el detalle, con «OC-012» → «OC-0012». En todos, «Proveedor A» pasa a **«Proveedor ABC»**.
**Riesgo para la sustentación:** si Canva no se corrige a tiempo, la línea de respaldo es «los datos canónicos son los de la demo; V17 a V23 conservan datos ilustrativos anteriores». La demo imprime «Cotizaciones opcionadas (V20): [COT-018]» y V20 mostraría dos: la demo genera un subconjunto, no contradice ningún número.

### M-04 · V03 y V04: el total del pedido · Obligatoria (DEC-40)
**Dónde:** V03 y V04, pp. 7 y 8.
**Qué dicen hoy:** V03 muestra un pedido con **2 bandejas paisas, 1 ajiaco y 1 limonada natural (4 platos, $88.000)**; V04 dice «Platos: 3».
**Por qué:** el reporte (p. 26) y la demo usan **$82.000** (2 bandejas paisas $32.000 y 1 ajiaco $18.000, 3 platos). Mauricio confirmó ese pedido en DEC-40, porque coincide con V04. V03 y V04 se contradicen entre sí.
**Qué debe decir:** una de dos, a elección del equipo:
- **Opción 1 (la de la demo):** quitar la limonada de V03, de modo que el pedido sea de 3 platos y $82.000, como V04.
- **Opción 2:** dejar la limonada, que V04 diga «Platos: 4» y **$88.000**, y cambiar el reporte y la demo (toca código). No se recomienda.

### M-05 · V18 y V19: valor de la línea frente al precio por unidad · Opcional (DEC-40)
**Dónde:** V18 (p. 25) y V19 (p. 26).
**Qué dicen hoy:** una columna de **valor de la línea** (Res 20 kg = $400.000; Arroz 15 kg = $120.000).
**Por qué:** el modelo (`ItemCotizacion`) guarda el **precio por unidad** de cada producto y el valor de la línea sale de multiplicar por la cantidad. Los totales de V19 ($520.000 y $545.000) cuadran igual, así que no hay un error de cifras, solo de lo que significa la columna.
**Qué debe decir:** poner la columna «Precio unitario» antes de «Valor»: Res $20.000/kg, Arroz $8.000/kg (valores estándar) y los del proveedor ($21.250/kg y $8.000/kg para COT-018). El valor de la línea se mantiene como resultado.

### M-06 · V05 y V07: cifras de stock · Obligatoria para V05 (DEC-50), opcional para V07
**Dónde:** V05 (p. 10) y V07 (p. 12).
**Qué dicen hoy:** V05 lista «Alertas de inventario» con **Res 23 %, Arroz 25 %, Papa 18 %, Tomate 24 % y Pollo 25 %**, y la tarjeta dice «4 Stock bajo». V07 trae «5 Stock bajo» pero solo marca 4 filas, y su primera fila tiene 4 kg de stock actual contra 10 kg de mínimo.
**Por qué:** la regla decidida (DEC-43) alerta solo si el porcentaje redondeado queda **por debajo** del 25 %, así que Arroz y Pollo al 25 % no alertan. V07 los marca como «Normal».
**Qué debe decir (DEC-50, decidida por el arquitecto):** en V05, **sacar Arroz y Pollo de la lista** (captura y wireframe), de modo que queden Res 23 %, Papa 18 % y Tomate 24 %, y la tarjeta diga **«3 Stock bajo»**. No se bajan a 24 %, porque V07 los marca «Normal». En V07 (opcional): que el contador coincida con las filas marcadas y que la fila de la res diga 11,5 kg de 50 kg (23 %).
**Riesgo para la sustentación:** la tarjeta HU-16 dice «llegue al 25 %», el reporte «≤ 25 %» (p. 21) y «llega al 25 %» (p. 9): se corrigen con R-22 y DEC-43. Respuesta lista: «el umbral se cruza al bajar del 25 %; un insumo en 25,00 % todavía no está bajo».

### M-07 · V12 y V21: cotización con varias solicitudes en el detalle · Opcional
V12 (detalle de la solicitud) puede mostrar, si la solicitud ya está en una cotización, el número de cotización y las otras solicitudes que comparten esa cotización. No está en las tarjetas; solo mejora la lectura de M-01.

### M-08 · Mockups nuevos para HU-15 y HU-19 · Opcional (DEC-07)
Las dos historias están **dentro del alcance de la arquitectura** y ninguna tiene mockup. Si el equipo quiere diseñarlos, esto es lo mínimo que cada pantalla debe tener, con las rutas que ya existen:

**Pantalla «Proveedores» (HU-15, auxiliar de compras, `/api/proveedores/**` en ms-cotizaciones)**
- Listado: razón social, NIT, correo, teléfono, categorías y estado (activo o inactivo), con buscador y botón «Registrar proveedor».
- Formulario de registro y edición: razón social, **NIT** (único y obligatorio), **correo** (con formato válido), teléfono, dirección y **una o varias categorías de producto**.
- Mensajes de error que pide la tarjeta: NIT repetido y correo inválido.
- Acciones por fila: ver detalle, editar e **inactivar**. Un proveedor inactivo **no aparece** en el desplegable «Proveedor» de V13.
- Cada acción deja auditoría (`auditoria.evento`).
- Rutas: `GET /api/proveedores` (el desplegable de V13 usa `soloActivos`), y registrar, detalle, editar e inactivar bajo `/api/proveedores/**`. La tarjeta HU-15 está en la p. 22 del Story Mapping.

**Pantalla «Promociones sugeridas» (HU-19, jefe de cocina, `/api/bam/promociones` en ms-analitica)**
- Histórico de sugerencias: producto, **motivo**, **vigencia** (desde y hasta) y **estado** (SUGERIDA, ACEPTADA o DESCARTADA).
- Dos botones por fila: **Aceptar** y **Descartar**.
- Aviso de que se envía como máximo una notificación de promociones por día.
- Rutas: sugerencias, aceptar y descartar bajo `/api/bam/promociones`. La tarjeta HU-19 está en la p. 26.
- Nota: el código de HU-19 sigue con cuerpos pendientes (la demo no la ejecuta): `README.md` de `KonradGourmet`.

---

### M-09 · V03: el saludo no corresponde al usuario · Opcional
**Dónde:** V03 (p. 7).
**Qué dice hoy:** la pantalla es la del mesero (perfil «Isabella Torres · Mesero», sucursal Centro), pero el saludo dice «Buenos días, **Carlos Mendoza** · Jefe de Cocina · Sucursal Centro», que es el del jefe de cocina de V05.
**Qué debe decir:** «Buenos días, Isabella Torres · Mesero · Sucursal Centro».

---

## 3. Story Mapping

### S-01 · HU-07 es una copia de HU-06 · Obligatoria (D9)
**Dónde:** p. 14 (HU-07) y p. 13 (HU-06) del Story Mapping.
**Qué dicen hoy:** las dos tarjetas dicen exactamente lo mismo: «Consultar periódicamente las solicitudes de alimentos de todos los puntos del restaurante», con los mismos siete criterios. HU-07 se llama «Filtrar solicitudes por criterios» pero no habla de filtros.
**Por qué:** la arquitectura ya filtra. `CriteriosConsulta` (4c y código) define **cinco filtros**: fecha desde, fecha hasta, producto, marca y estado. El Excel y el mapa de historias tienen F7 «Filtrar solicitudes por criterios» como una funcionalidad aparte (1 PHU, sprint 1).
**Qué debe decir (tarjeta reescrita; la HU-06 queda igual):**

| Como (Actor) | Quiero (Acción) | Para (Beneficio) | Criterios de aceptación |
|---|---|---|---|
| Auxiliar del área de compras | Filtrar el listado de solicitudes de alimentos por rango de fechas, producto, marca y estado | Encontrar con rapidez las solicitudes que debo tramitar y dar trámite oportuno a los requerimientos | • Debo poder filtrar por **rango de fechas** de solicitud (desde y hasta), **producto**, **marca** y **estado** (PENDIENTE, EN TRÁMITE o COMPLETADA).<br>• Cada filtro es **opcional** y se pueden combinar; sin ningún filtro veo el listado completo de HU-06.<br>• El resultado conserva las columnas de HU-06: fecha de solicitud, producto, cantidad, marca y estado, ordenado por fecha descendente.<br>• Si ningún registro cumple los criterios, el sistema debe mostrar: «No se encontraron solicitudes con los criterios indicados».<br>• Debo poder limpiar los filtros con un solo clic.<br>• *Propuesta, no está en el código:* si la fecha «desde» es posterior a «hasta», el sistema avisa y no consulta.<br>• Solo accede quien tenga el permiso correspondiente.<br>• La consulta debe responder en menos de 3 segundos. |

Los criterios en cursiva son una propuesta nueva; el resto sale de HU-06 y de `CriteriosConsulta`. El mockup V11 (p. 17) ya tiene la tabla de solicitudes: el equipo puede agregar encima la barra de filtros.

### S-02 · HU-09, mensaje y selección de solicitudes · Sin cambio en la tarjeta
La tarjeta HU-09 (p. 16) ya dice «una o varias solicitudes». **No se cambia la tarjeta: se cambia el mockup V13 (M-01).** Se anota para que nadie «corrija» la tarjeta por error.

### S-03 · El título «Capacidad total por sprint (120)» · Obligatoria (H26)
**Dónde:** p. 30.
**Qué dice hoy:** «CAPACIDAD TOTAL POR SPRINT **(120)**» y, debajo, «4 PERSONAS × 40 HORAS × 2 SEMANAS × 0.7 DE PRODUCTIVIDAD = **224 HORAS**».
**Por qué:** el título dice 120 y la cuenta da 224. El Excel y la p. 28 («224/25 · 4*40*2*0.7») dicen 224.
**Qué debe decir:** «CAPACIDAD TOTAL POR SPRINT **(224)**». Si el 120 aparece en otra página, se cambia igual.

### S-04 · «25PHU» en las cartas del Planning Poker · Obligatoria (H26)
**Dónde:** p. 27, las cartas del Planning Poker.
**Qué dice hoy:** las cartas dicen **«25PHU», «50PHU», «75PHU» y «125PHU»**.
**Por qué:** el equipo definió **1 PHU = 25 horas** (p. 30, «PHU EN HORAS (25)»). Esas cartas son **horas**, no PHU: 25 h = 1 PHU, 50 h = 2 PHU, 75 h = 3 PHU y 125 h = 5 PHU. Con las cartas escritas como PHU, el total de HU-14 (2 en una hoja y 1 en la otra) y los 35 PHU del plan no se entienden.
**Qué debe decir:** **«25 h»**, **«50 h»**, **«75 h»** y **«125 h»** (o el equivalente «1 PHU», «2 PHU», «3 PHU» y «5 PHU»). Además, **HU-14 vale 1 PHU (25 h)**, no 2: el total del Excel es **35 PHU = 875 h**.

### S-05 · «Ver KPIs» bajo el director de compras · Obligatoria (H26, DEC-06)
**Dónde:** p. 2 (columna «Director área compras», actividad «Ver KPIs tablero control») y p. 5 (MVP 4, «Ver KPIs y tablero de control (Director Compras)»); también las pp. 27 y 30 si repiten la columna.
**Qué dice hoy:** el tablero es del **director de compras**.
**Por qué:** la tarjeta **HU-18** (p. 25) dice «Como **director comercial** quiero visualizar un tablero de control (BAM)…». La arquitectura y el reporte usan al director comercial (DEC-06).
**Qué debe decir:** mover la actividad «Ver KPIs y tablero de control» a una columna **«Director comercial»** y cambiar «(Director Compras)» por «(Director Comercial)» en el MVP 4. El Story Mapping no tiene hoy la columna del director comercial: agregarla.

### S-06 · La p. 31 «Sprint»: 5 sprints, 10 semanas, 2 meses y medio · Obligatoria (H20, DEC-47)
**Dónde:** p. 31, la tabla «SPRINT».
**Qué dice hoy:** **4 sprints**, **8 semanas** y **«2 Meses»**, con los sprints 2 y 3 «levemente sobrecargados (10 sobre 9)», sobrantes de 1 punto en los sprints 1 y 4, y una frase que empieza «Un detalle para tu sustentación…».
**Por qué:** el Excel, que es el cálculo del equipo, tiene **5 sprints de 2 semanas (10 semanas, 2 meses y medio)**, con 9 PHU por sprint y 35 PHU en total. Mauricio decidió en DEC-47 que todo diga 5 sprints y 10 semanas. La tabla de la p. 31 supone una capacidad de 10 PHU en algunos sprints, que no existe (la capacidad es 9).
**Qué debe decir** (los números salen del Excel, hoja SPRINT, 8, 8, 6, 8 y 5 PHU):

| Sprint | Duración | Fechas | MVP | PHU | Entregables |
|---|---|---|---|---|---|
| Sprint 1 | 2 semanas | 03/08 al 16/08/2026 | MVP 1, MVP 2 | 8 de 9 | Módulo de captura operativo: registro de pedidos con mesa y cantidades, y gestión completa del menú con sus platos e ingredientes. Listado de solicitudes de compra con filtros por criterios y acceso al detalle |
| Sprint 2 | 2 semanas | 17/08 al 30/08/2026 | MVP 2, MVP 3 | 8 de 9 | Ciclo de solicitudes y cotizaciones funcionando: registro de solicitud de alimentos, generación de cotizaciones y captura de valores del proveedor. Envío formal de cotizaciones a proveedores con PDF y correo certificado |
| Sprint 3 | 2 semanas | 31/08 al 13/09/2026 | MVP 3, MVP 4 | 6 de 9 | Cierre del proceso de compras: validación de cotizaciones contra el precio del Ministerio, generación de la orden de compra, maestro de proveedores y consulta de cotizaciones opcionadas |
| Sprint 4 | 2 semanas | 14/09 al 27/09/2026 | MVP 4 | 8 de 9 | Descuento automático diario de inventario con alertas por correo al bajar del 25 %. Tablero de control (BAM) con los KPI de mayor demanda, sucursal líder y mejor proveedor |
| Sprint 5 | 2 semanas | 28/09 al 11/10/2026 | MVP 4 | 5 de 9 | Motor de tendencias en redes sociales y de productos más solicitados. Promociones sugeridas con notificación automática a los puntos de venta y despliegue final del sistema |
| **TOTAL** | **10 semanas** | **03/08 al 11/10/2026** | Proyecto completo | **35 de 45** | Sistema completo y desplegado |
| | **2 meses y medio** | | | | |

**Además:**
- **Quitar** el párrafo «Un detalle para tu sustentación: los sobrantes de 1 punto…». Habla en segunda persona, parece una nota de quien armó la tabla, y ya no es cierto (sobran 1, 1, 3, 1 y 4 PHU, 10 en total).
- Los sobrantes de **10 PHU** son la holgura del plan: 45 de capacidad contra 35 de trabajo.
- Los valores del Excel son fórmulas: si el equipo cambia la celda «Inicio del Sprint 1», las fechas se recalculan.

### S-07 · Otras referencias a «4 sprints» y «8 semanas» · Obligatoria (H20)
Buscar en las pp. 29 a 32 y en cualquier presentación basada en ellas: «4 sprints», «8 semanas» y «2 meses». Todo debe decir **5 sprints, 10 semanas y 2 meses y medio**.

### S-08 · De qué historia sale cada ventana · Informativa
No hay nada que corregir. Sirve para ubicar la tarjeta que corresponde a cada pantalla: **V13 sale de HU-09**, **V21 de HU-14**, **V05 de HU-16**, y **V11** es el listado de solicitudes de **HU-06** (HU-07 agrega los filtros sobre ese mismo listado). El reporte no cita las HU por número; la relación está en la trazabilidad de la sección 15 (ventana → método) y en los comentarios `Atiende la ventana Vxx` del código.

---

## 4. Resumen

| Id | Dónde | Qué | Tipo |
|---|---|---|---|
| M-01 | V13, p. 19 | Varias solicitudes en el modal | Obligatoria |
| M-02 | V21, p. 28 | «Solicitudes» en el modal | Obligatoria |
| M-03 | V17 y V19 a V23, pp. 24 y 26 a 30 | Datos de la demo (COT-018, Proveedor ABC, OC-0012 por $545.000) en las seis pantallas | Obligatoria |
| M-04 | V03 y V04, pp. 7 y 8 | $82.000 y 3 platos | Obligatoria |
| M-05 | V18 y V19, pp. 25 y 26 | Precio unitario | Opcional |
| M-06 | V05 y V07, pp. 10 y 12 | Sacar Arroz y Pollo de V05; tarjeta en 3 | Obligatoria (V05) |
| M-07 | V12 | Solicitudes de la misma cotización | Opcional |
| M-08 | Nuevos | Pantallas de HU-15 y HU-19 | Opcional |
| M-09 | V03, p. 7 | Saludo del mesero | Opcional |
| S-01 | HU-07, p. 14 | Reescribir la tarjeta con los 5 filtros | Obligatoria |
| S-03 | p. 30 | Capacidad (224), no (120) | Obligatoria |
| S-04 | p. 27 | «25 h», no «25PHU»; HU-14 = 1 | Obligatoria |
| S-05 | pp. 2 y 5 | «Ver KPIs» pasa al director comercial | Obligatoria |
| S-06 | p. 31 | 5 sprints, 10 semanas, fechas y reparto del Excel | Obligatoria |
| S-07 | pp. 29 a 32 | Quitar «4 sprints» y «8 semanas» | Obligatoria |

## 5. Quién lo hace y en qué orden

El equipo, en Canva (mockups y Story Mapping). Orden recomendado, de lo que más choca con el resto a lo que menos:
1. **S-06 y S-07** (la p. 31 contradice al Excel y trae una nota que no debe quedar).
2. **M-03 y M-04** (dos mockups del mismo recorrido dan cifras distintas, y el profesor puede ponerlos al lado).
3. **M-01, M-02 y S-01** (las historias contradicen a la arquitectura).
4. **S-03, S-04 y S-05** (etiquetas).
5. **M-05 a M-08** (opcionales).

**Pendiente de confirmar:** M-04 depende de elegir la opción 1 o 2. M-03 y M-06 están decididas (DEC-49 y DEC-50, por el arquitecto).
