# Story Mapping corregido · Konrad Gourmet

Versión corregida del Visual Story Mapping del proyecto (2026-2). El original es `proyecto/contexto/Visual Story Mapping.pdf`, de 32 páginas, y se conserva sin cambios. Cada sección indica la página del original de la que sale. Las correcciones están marcadas en el texto con su identificador y se resumen en la sección 11.

Presentado por:
- Iturriza Medina Mauricio Jesus
- Castro Camacho Isabella
- Franco Hernandez Carlos Alberto
- Corredor Camero Adrian David

## 1. Usuario final

Original: página 2. Actividades de cada usuario del sistema.

*Corregido (S-05): «Ver KPIs tablero control» pasa de la columna del director de compras a una columna nueva, «Director comercial», porque la tarjeta HU-18 la asigna a ese rol (DEC-06).*

| Jefe de cocina | Mesero | Auxiliar área compras | Director área compras | Director comercial |
|---|---|---|---|---|
| Registrar platos/menú | Registrar pedido/orden | Consultar solicitudes compras | Registrar valores cotización | Ver KPIs tablero control |
| Ingresar ingredientes plato | Ingresar cantidad platos | Filtrar solicitudes por criterios | Validar cotizaciones recibidas | |
| Registrar solicitud alimentos | Indicar mesa del pedido | Ver detalles solicitud | Consultar cotizaciones opcionadas | |
| Recibir alertas inventario | | Generar solicitud cotización | Generar orden compra | |
| Revisar promociones sugeridas | | Registrar datos de proveedor | | |

## 2. Complejidad

Original: página 3.

| Fácil | Medio | Medio | Alto |
|---|---|---|---|
| Registrar pedido/orden | Consultar solicitudes compras | Generar solicitud cotización (envío a proveedores) | Recibir alertas inventario |
| Ingresar cantidad platos | Filtrar solicitudes por criterios | Validar cotizaciones recibidas | Consultar cotizaciones opcionadas |
| Indicar mesa del pedido | Ver detalles solicitud | Generar orden compra | Ver KPIs tablero control |
| Registrar platos/menú | Generar solicitud cotización | Registrar datos de proveedor | Revisar promociones sugeridas |
| Ingresar ingredientes plato | Registrar solicitud alimentos | | |
| | Registrar valores cotización | | |

## 3. MVP (Minimum Viable Products)

Original: páginas 4 a 6.

| MVP | Nombre | Actividades |
|---|---|---|
| MVP 1 | Módulo de captura y registro básico de datos | Registrar pedido/orden (Mesero), Ingresar cantidad de platos (Mesero), Indicar mesa del pedido (Mesero), Registrar platos/menú (Jefe Cocina), Ingresar ingredientes plato (Jefe Cocina) |
| MVP 2 | Módulo de consulta y registro estándar | Consultar solicitudes de compras, Filtrar solicitudes por criterios, Ver detalles de solicitud y Generar solicitud de cotización (Auxiliar Compras); Registrar solicitud de alimentos (Jefe Cocina); Registrar valores de cotización (Director Compras) |
| MVP 3 | Módulo de gestión de compras y validación | Generar solicitud de cotización a proveedores (Auxiliar Compras), Validar cotizaciones recibidas (Director Compras), Generar orden de compra (Director Compras), Registrar datos de proveedor (Auxiliar Compras) |
| MVP 4 | Módulo de inventario avanzado e inteligencia de negocio | Recibir alertas automáticas de inventario bajo (Jefe Cocina), Consultar cotizaciones opcionadas (Director Compras), Ver KPIs y tablero de control (Director comercial, S-05), Revisar promociones sugeridas (Jefe Cocina) |

*Corregido (SM-01): el original da a cada MVP nombres distintos en la leyenda (p. 4), en los objetivos (pp. 5 y 6), en los separadores de las tarjetas y en el Planning Poker. En los objetivos, además, los nombres del MVP 3 y del MVP 4 no corresponden a su contenido. Se usa en todo el documento el nombre de los separadores de las tarjetas y del Planning Poker.*

**Objetivos de los MVP** (páginas 5 y 6):

- **MVP 1 · Módulo de captura y registro básico de datos.** Implementar funcionalidades básicas de captura e ingreso de datos operativos fundamentales del negocio, permitiendo registros simples y directos sin validaciones complejas ni integraciones externas.
- **MVP 2 · Módulo de consulta y registro estándar.** Implementar funcionalidades CRUD estándar de consulta y registro de información operativa con validaciones básicas, permitiendo filtrado de datos y visualización de detalles sin disparo de eventos automáticos.
- **MVP 3 · Módulo de gestión de compras y validación.** Implementar funcionalidades complejas de gestión de compras incluyendo generación de documentos (PDFs), validación de cotizaciones con lógica condicional, integración con APIs externas (Ministerio), y cambios de estado automáticos.
- **MVP 4 · Módulo de inventario avanzado e inteligencia de negocio.** Implementar funcionalidades avanzadas de monitoreo automático de inventario, Business Intelligence con análisis de datos históricos, e identificación de tendencias en redes sociales y de los productos más solicitados para sugerir promociones. *(Corregido, SM-02: el original decía «machine learning para recomendaciones inteligentes»; ni HU-19 ni la arquitectura usan aprendizaje automático. Las tendencias se detectan con reglas sobre los datos, PD-04.)*

## 4. Tabla de estimación PERT de HU-03

Original: página 7. Estimación de «Indicar mesa del pedido» (MVP 1). Calculado = (Optimista + 4 × Esperado + Pesimista) / 6.

| Fase | Tarea | Optimista (h) | Esperado (h) | Pesimista (h) | Calculado (h) |
|---|---|---|---|---|---|
| Análisis | Levantamiento de requerimientos de asignación de mesa | 1 | 1 | 2 | 1.17 |
| Análisis | Análisis de la parametrización de mesas por sucursal | 1 | 2 | 3 | 2.00 |
| Diseño | Diseño del modelo de datos (entidad Mesa y relación con Pedido) | 1 | 2 | 3 | 2.00 |
| Diseño | Diseño gráfico del componente de selección de mesa | 1 | 1 | 2 | 1.17 |
| Diseño | Creación de la tabla Mesa y carga de datos de parametrización | 1 | 2 | 3 | 2.00 |
| Construcción | Desarrollo del servicio de consulta de mesas por sucursal | 1 | 2 | 4 | 2.17 |
| Construcción | Desarrollo del componente desplegable de selección de mesa | 2 | 3 | 4 | 3.00 |
| Construcción | Implementación de la validación de campo obligatorio | 1 | 1 | 2 | 1.17 |
| Construcción | Asociación de la mesa seleccionada al registro del pedido | 1 | 2 | 3 | 2.00 |
| Pruebas | Pruebas unitarias de la validación de mesa obligatoria | 1 | 1 | 2 | 1.17 |
| Pruebas | Pruebas de integración del servicio de consulta de mesas | 1 | 2 | 3 | 2.00 |
| Pruebas | Pruebas funcionales del flujo de asignación de mesa al pedido | 1 | 2 | 3 | 2.00 |
| Entrega | Despliegue del componente en producción | 1 | 1 | 2 | 1.17 |
| Entrega | Documentación de la funcionalidad | 1 | 1 | 2 | 1.17 |
| **Total** | **Total HU-03** | **15** | **23** | **38** | **24.17** |

*Corregido (SM-03): el original da 24.57 h de total calculado. La suma de la columna es 24.19 h (por el redondeo de cada fila) y la fórmula aplicada a los totales da (15 + 4 × 23 + 38) / 6 = 24.17 h. Las otras tres columnas están bien sumadas. La conclusión no cambia: HU-03 vale 1 PHU.*

Equivalencia en puntos de historia de usuario: 25 h ≈ 1 PHU.

## 5. Historias de usuario

Original: páginas 8 a 26, una tarjeta por página. Se transcriben completas.

### MVP 1 — Módulo de captura y registro básico de datos

#### HU-01: Registrar pedido/orden

- **Como:** Mesero del restaurante Konrad Gourmet.
- **Quiero:** Registrar cada pedido u orden que solicitan los clientes en el sistema.
- **Para:** Para llevar un control digital y en tiempo real de las ventas, evitando errores de la toma manual de pedidos.

**Criterios de aceptación:**
1. Debo poder crear un nuevo pedido asociado a mi sucursal y a mi usuario.
2. Debo poder agregar uno o varios platos al pedido desde una lista desplegable del menú vigente.
3. El sistema debe registrar automáticamente la fecha y hora del pedido.
4. El sistema debe calcular y mostrar el valor total del pedido según el precio de venta de cada plato.
5. No debo poder guardar un pedido sin al menos un plato asociado.
6. Al guardar, el sistema debe generar un número consecutivo de pedido y mostrarlo en pantalla.
7. El sistema debe registrar el movimiento de auditoría (acción, usuario, fecha, hora).

#### HU-02: Ingresar cantidad de platos

- **Como:** Mesero del restaurante Konrad Gourmet.
- **Quiero:** Ingresar la cantidad de unidades de cada plato dentro del pedido.
- **Para:** Para que el descuento de inventario y el valor total del pedido se calculen de forma exacta.

**Criterios de aceptación:**
1. Debo poder ingresar una cantidad numérica por cada plato agregado al pedido (campo obligatorio).
2. El sistema solo debe aceptar números enteros positivos mayores a cero.
3. Si ingreso un valor no numérico, negativo o cero, el sistema debe mostrar el mensaje: «La cantidad debe ser un número entero mayor a cero».
4. Debo poder modificar la cantidad antes de confirmar el pedido.
5. El subtotal de cada línea debe recalcularse automáticamente al cambiar la cantidad.
6. El total del pedido debe actualizarse en pantalla sin necesidad de recargar.

#### HU-03: Indicar mesa del pedido

- **Como:** Mesero del restaurante Konrad Gourmet.
- **Quiero:** Indicar el número de mesa al que corresponde cada pedido.
- **Para:** Para poder identificar y entregar correctamente cada orden al cliente que la solicitó.

**Criterios de aceptación:**
1. Debo poder seleccionar el número de mesa desde una lista desplegable parametrizada por sucursal (campo obligatorio).
2. El sistema no debe permitir guardar el pedido sin una mesa asignada.
3. El sistema debe mostrar un mensaje: «Debe seleccionar una mesa para continuar» si el campo queda vacío.
4. Debo poder consultar los pedidos activos filtrando por número de mesa.
5. La mesa asignada debe quedar visible en el detalle del pedido y en el número consecutivo generado.

#### HU-04: Registrar platos/menú

- **Como:** Jefe de cocina de un punto del restaurante.
- **Quiero:** Registrar los platos que conforman el menú de mi punto de venta.
- **Para:** Para tener el menú digitalizado y permitir que las órdenes y el consumo de insumos se calculen automáticamente.

**Criterios de aceptación:**
1. Debo poder ingresar el nombre del plato (campo obligatorio y único por sucursal).
2. Debo poder ingresar el precio de venta del plato (campo obligatorio, valor numérico mayor a cero).
3. El sistema debe asociar automáticamente el plato a mi sucursal y a mi usuario.
4. El sistema debe validar el formato correcto de todos los datos antes de guardar.
5. Si el nombre del plato ya existe en mi sucursal, el sistema debe mostrar: «Ya existe un plato registrado con ese nombre».
6. Debo poder consultar, editar y desactivar los platos previamente registrados.
7. Cada acción sobre el plato debe quedar registrada en la auditoría (acción, usuario, fecha, hora).

#### HU-05: Ingresar ingredientes del plato

- **Como:** Jefe de cocina de un punto del restaurante.
- **Quiero:** Ingresar el listado de insumos que componen cada plato del menú.
- **Para:** Para que el sistema pueda estimar y descontar automáticamente las cantidades de alimentos requeridas por cada venta.

**Criterios de aceptación:**
1. Debo poder agregar uno o varios insumos al plato mediante un listado dinámico.
2. Por cada insumo debo poder seleccionar la categoría de producto desde una lista desplegable (Proteína, Carbohidrato, Verdura, Cereal, Grano, Fruta, etc.).
3. Debo poder seleccionar el producto desde una lista desplegable dependiente de la categoría elegida (Ej. Arroz, Frijol, Repollo, Res).
4. Debo poder ingresar la cantidad requerida del producto (numérico mayor a cero).
5. Debo poder seleccionar la unidad de medida desde una lista desplegable (Kg, Libra, Litro, etc.).
6. El sistema no debe permitir guardar el plato sin al menos un insumo asociado.
7. Si selecciono una categoría sin productos parametrizados, el sistema debe mostrar: «No hay productos disponibles para la categoría seleccionada».
8. Debo poder eliminar o modificar insumos antes de guardar el plato.

### MVP 2 — Módulo de consulta y registro estándar

#### HU-06: Consultar solicitudes de compras

- **Como:** Auxiliar del área de compras.
- **Quiero:** Consultar periódicamente las solicitudes de alimentos de todos los puntos del restaurante.
- **Para:** Para dar trámite oportuno a los requerimientos y conocer el estado de cada solicitud.

**Criterios de aceptación:**
1. Debo poder acceder a un listado consolidado con las solicitudes de todas las sucursales.
2. El listado debe mostrar las columnas: fecha de solicitud, producto, cantidad, marca y estado.
3. El listado debe estar ordenado por fecha de solicitud de forma descendente por defecto.
4. El listado debe estar paginado cuando existan más de 25 registros.
5. Si no existen solicitudes, el sistema debe mostrar el mensaje: «No se encontraron solicitudes registradas».
6. Solo debo poder acceder a esta consulta si mi perfil tiene el permiso correspondiente.
7. La consulta debe responder en un tiempo menor a 3 segundos.

#### HU-07: Filtrar solicitudes por criterios

*Corregida (S-01): la tarjeta original repetía palabra por palabra la de HU-06 y no hablaba de filtros. Los criterios salen de HU-06 y de los filtros que ya define la arquitectura (`CriteriosConsulta`: fecha desde, fecha hasta, producto, marca y estado).*

- **Como:** Auxiliar del área de compras.
- **Quiero:** Filtrar el listado de solicitudes de alimentos por rango de fechas, producto, marca y estado.
- **Para:** Para encontrar con rapidez las solicitudes que debo tramitar y dar trámite oportuno a los requerimientos.

**Criterios de aceptación:**
1. Debo poder filtrar por rango de fechas de solicitud (desde y hasta), producto, marca y estado (PENDIENTE, EN TRÁMITE o COMPLETADA).
2. Cada filtro es opcional y se pueden combinar; sin ningún filtro veo el listado completo de HU-06.
3. El resultado conserva las columnas de HU-06 (fecha de solicitud, producto, cantidad, marca y estado), ordenado por fecha de solicitud de forma descendente.
4. Si ningún registro cumple los criterios, el sistema debe mostrar: «No se encontraron solicitudes con los criterios indicados».
5. Debo poder limpiar los filtros con un solo clic.
6. *Criterio propuesto (PD-10):* si la fecha «desde» es posterior a la fecha «hasta», el sistema debe mostrar «La fecha inicial no puede ser posterior a la fecha final.» y no consultar.
7. Solo debo poder acceder a esta consulta si mi perfil tiene el permiso correspondiente.
8. La consulta debe responder en un tiempo menor a 3 segundos.

#### HU-08: Ver detalles de solicitud

- **Como:** Auxiliar del área de compras.
- **Quiero:** Consultar el detalle completo de una solicitud desde el listado.
- **Para:** Para conocer toda la información del requerimiento antes de generar la cotización correspondiente.

**Criterios de aceptación:**
1. Debo poder acceder al detalle haciendo clic sobre cualquier solicitud del listado.
2. El detalle debe mostrar: número de solicitud, sucursal, solicitante, fecha, estado y valor total sugerido.
3. El detalle debe mostrar el listado completo de productos con categoría, producto, cantidad, marca y unidad.
4. Debo poder regresar al listado conservando los filtros previamente aplicados.
5. Si la solicitud no existe o fue eliminada, el sistema debe mostrar: «La solicitud consultada no está disponible».
6. La consulta del detalle no debe permitir modificar la información.

#### HU-09: Generar solicitud de cotización

- **Como:** Auxiliar del área de compras.
- **Quiero:** Registrar los datos base de una solicitud de cotización a partir del listado de solicitudes.
- **Para:** Para iniciar el proceso de negociación con los proveedores de forma estructurada.

**Criterios de aceptación:**
1. Debo poder seleccionar una o varias solicitudes del listado para incluir en la cotización.
2. Debo poder seleccionar el proveedor desde una lista desplegable (campo obligatorio).
3. Debo poder registrar la fecha máxima de plazo para la cotización (campo obligatorio).
4. El sistema no debe permitir una fecha de plazo anterior a la fecha actual.
5. El sistema debe mostrar un resumen de los productos y cantidades incluidos antes de confirmar.
6. Si no selecciono ninguna solicitud, el sistema debe mostrar: «Debe seleccionar al menos una solicitud».
7. Al guardar, el sistema debe generar un número consecutivo de cotización.

#### HU-10: Registrar solicitud de alimentos

- **Como:** Jefe de cocina de un punto del restaurante.
- **Quiero:** Registrar una solicitud de alimentos cuando no tengo stock disponible en mi inventario.
- **Para:** Para asegurar el abastecimiento oportuno de mi punto de venta y evitar la interrupción del servicio.

**Criterios de aceptación:**
1. Debo poder agregar uno o varios productos a la solicitud mediante un listado dinámico.
2. Por cada producto debo seleccionar la categoría desde una lista desplegable (campo obligatorio).
3. Debo poder seleccionar el producto desde una lista desplegable dependiente de la categoría (campo obligatorio).
4. Debo poder ingresar la cantidad requerida (numérico mayor a cero, obligatorio).
5. Debo poder seleccionar la marca desde una lista desplegable (campo opcional).
6. Debo poder seleccionar la unidad de medida desde una lista desplegable (campo obligatorio).
7. El sistema debe registrar automáticamente la sucursal del restaurante y el solicitante.
8. El sistema debe verificar el formato correcto de todos los datos antes de guardar.
9. Si todo está correcto, la solicitud debe quedar en estado «PENDIENTE» y el sistema debe mostrar el número de solicitud generado.
10. El sistema debe enviar un correo al solicitante confirmando la operación exitosa.
11. Si algún dato es inválido, el sistema debe indicar el campo con error y no debe registrar la solicitud.

#### HU-11: Registrar valores de cotización

- **Como:** Director del área de compras.
- **Quiero:** Registrar en el sistema los valores que los proveedores envían por correo para cada cotización.
- **Para:** Para consolidar la información y poder compararla posteriormente contra el precio estándar del mercado.

**Criterios de aceptación:**
1. Debo poder buscar y seleccionar la cotización generada por el sistema a la que corresponden los valores.
2. Debo poder ingresar el valor unitario ofertado por cada producto incluido en la cotización.
3. El sistema solo debe aceptar valores numéricos mayores a cero.
4. El sistema debe calcular automáticamente el valor total de la cotización.
5. Debo poder guardar parcialmente y continuar el registro posteriormente.
6. Si la cotización ya fue validada, el sistema no debe permitir modificar los valores.
7. Cada registro o modificación debe quedar en la auditoría (acción, usuario, fecha, hora).

### MVP 3 — Módulo de gestión de compras y validación

#### HU-12: Generar solicitud de cotización a proveedores

- **Como:** Auxiliar del área de compras.
- **Quiero:** Enviar la solicitud de cotización al proveedor seleccionado con el documento formal adjunto.
- **Para:** Para formalizar el requerimiento ante el proveedor y dejar trazabilidad del envío.

**Criterios de aceptación:**
1. Al aceptar la cotización, el sistema debe cambiar su estado a «ENVIADA».
2. El sistema debe generar automáticamente un documento PDF con el detalle de los productos, cantidades, unidades y fecha máxima de plazo.
3. El sistema debe enviar el PDF por correo electrónico a la cuenta registrada del proveedor seleccionado.
4. El correo debe ser certificado y contar con estampado cronológico.
5. El sistema debe verificar si todos los alimentos de una solicitud ya tienen cotización asociada; en ese caso debe cambiar el estado de la solicitud de «PENDIENTE» a «EN TRÁMITE».
6. Por cada producto, el sistema debe consultar el precio sugerido del servicio web del Ministerio de Comercio y actualizar la solicitud con el valor total sugerido.
7. Si el servicio web del Ministerio no responde, el sistema debe registrar el error en el log y notificar al usuario sin perder la cotización.
8. Si el envío del correo falla, el sistema debe permitir reintentar el envío.

#### HU-13: Validar cotizaciones recibidas

- **Como:** Director del área de compras.
- **Quiero:** Validar automáticamente las cotizaciones registradas contra el precio estándar del Ministerio.
- **Para:** Para identificar de forma objetiva cuáles ofertas son viables, cuáles están sobrevaloradas y cuáles resultan sospechosas.

**Criterios de aceptación:**
1. Debo poder seleccionar la opción «Validar» sobre una cotización con valores ya registrados.
2. Si el valor de la cotización supera en más del 25% el valor estándar del Ministerio, el sistema debe dejarla en estado «RECHAZADA».
3. Si el valor de la cotización está por debajo en más del 50% del valor estándar del Ministerio, el sistema debe dejarla en estado «SOSPECHOSA».
4. Si el valor se encuentra dentro de ese rango, el sistema debe dejarla en estado «OPCIONADA».
5. El sistema debe mostrar en pantalla el resultado de la validación con el valor estándar de referencia y el porcentaje de diferencia.
6. El sistema no debe permitir validar una cotización sin valores registrados; debe mostrar: «La cotización no tiene valores registrados para validar».
7. El cambio de estado debe quedar registrado en la auditoría (acción, usuario, fecha, hora).

#### HU-14: Generar orden de compra

- **Como:** Director del área de compras.
- **Quiero:** Generar la orden de compra a partir de la cotización que seleccione como mejor opción.
- **Para:** Para formalizar la adquisición de los alimentos con el proveedor elegido y cerrar el ciclo de abastecimiento.

**Criterios de aceptación:**
1. Solo debo poder generar órdenes de compra a partir de cotizaciones en estado «OPCIONADA».
2. La orden de compra debe heredar los mismos datos de la cotización seleccionada (proveedor, productos, cantidades, unidades y valores).
3. El sistema debe generar un número consecutivo único de orden de compra.
4. El sistema debe generar el documento PDF de la orden y enviarlo por correo certificado al proveedor.
5. Al generar la orden, la cotización debe cambiar a un estado que impida generar una segunda orden sobre ella.
6. Si intento generar una orden sobre una cotización en otro estado, el sistema debe mostrar: «Solo se pueden generar órdenes de compra sobre cotizaciones opcionadas».
7. La generación debe quedar registrada en la auditoría (acción, usuario, fecha, hora).

#### HU-15: Registrar datos de proveedor

- **Como:** Auxiliar del área de compras.
- **Quiero:** Registrar y mantener actualizada la información de los proveedores del restaurante.
- **Para:** Para poder seleccionarlos en las solicitudes de cotización y garantizar que las comunicaciones lleguen a la cuenta correcta.

**Criterios de aceptación:**
1. Debo poder ingresar la razón social del proveedor (campo obligatorio).
2. Debo poder ingresar el NIT o número de identificación tributaria (campo único y obligatorio).
3. Debo poder ingresar el correo electrónico del proveedor (formato válido, obligatorio).
4. Debo poder ingresar el teléfono de contacto y la dirección.
5. Debo poder asociar al proveedor una o varias categorías de producto que maneja.
6. Si el NIT ya se encuentra registrado, el sistema debe mostrar: «Ya existe un proveedor registrado con ese número de identificación».
7. Si el correo no tiene formato válido, el sistema debe mostrar: «El formato del correo electrónico no es válido».
8. Debo poder consultar, editar e inactivar proveedores; un proveedor inactivo no debe aparecer en las listas desplegables de cotización.
9. Cada acción debe quedar registrada en la auditoría (acción, usuario, fecha, hora).

### MVP 4 — Módulo de inventario avanzado e inteligencia de negocio

#### HU-16: Recibir alertas automáticas de inventario bajo

- **Como:** Jefe de cocina de un punto del restaurante.
- **Quiero:** Recibir alertas automáticas cuando el inventario de un producto llegue a un nivel crítico.
- **Para:** Para realizar la solicitud de alimentos a tiempo y evitar quedarme sin insumos durante el servicio.

**Criterios de aceptación:**
1. El sistema debe verificar diariamente las órdenes preparadas o vendidas y descontar las cantidades correspondientes del inventario.
2. Cuando la existencia de un producto quede por debajo del 25 % de su cantidad de referencia (porcentaje redondeado a dos decimales), el sistema debe generar una alarma visible dentro de la aplicación. La alarma se genera una sola vez, el día en que el producto cruza ese umbral; un producto en 25,00 % todavía no está bajo. *(Corregido, SM-04: el original decía «llegue al 25 %»; la regla aprobada está en DEC-43 y DEC-46.)*
3. El sistema debe enviar un correo electrónico avisando que se debe realizar una solicitud de alimentos.
4. La alerta debe indicar el producto, la cantidad restante, la unidad y la sucursal afectada.
5. Debo poder acceder directamente al formulario de solicitud de alimentos desde la alerta.
6. La alerta debe permanecer activa hasta que se registre una solicitud para ese producto.
7. El proceso automático no debe afectar el desempeño de la aplicación en horario de operación.
8. Si el proceso diario falla, el error debe quedar registrado en el log del sistema.

#### HU-17: Consultar cotizaciones opcionadas

- **Como:** Director del área de compras.
- **Quiero:** Consultar en cualquier momento las cotizaciones que quedaron en estado «OPCIONADA».
- **Para:** Para analizar y decidir cuál representa la mejor opción de compra para el restaurante.

**Criterios de aceptación:**
1. Debo poder acceder a un listado exclusivo de cotizaciones en estado «OPCIONADA».
2. El listado debe mostrar: número de cotización, proveedor, fecha, valor total ofertado, valor estándar del Ministerio y porcentaje de diferencia.
3. Debo poder ordenar el listado por valor total o por porcentaje de diferencia.
4. Debo poder comparar dos o más cotizaciones del mismo producto en una vista paralela.
5. Debo poder acceder al detalle completo de cada cotización desde el listado.
6. Desde el listado debo poder seleccionar una cotización y generar la orden de compra.
7. Si no existen cotizaciones opcionadas, el sistema debe mostrar: «No hay cotizaciones opcionadas disponibles».

#### HU-18: Ver KPIs y tablero de control

- **Como:** Director comercial.
- **Quiero:** Visualizar un tablero de control (BAM) con los principales indicadores del negocio.
- **Para:** Para tomar decisiones estratégicas de abastecimiento y negociación basadas en datos consolidados y no en estimaciones manuales.

**Criterios de aceptación:**
1. El tablero debe mostrar el producto con mayor demanda en el último mes.
2. El tablero debe mostrar el restaurante o sucursal con mayor demanda en el último mes.
3. El tablero debe mostrar el proveedor con los mejores precios en el último año.
4. Cada indicador debe presentarse con su valor numérico y una representación gráfica.
5. Los datos deben actualizarse de forma automática al menos una vez al día.
6. Debo poder filtrar los indicadores por rango de fechas y por sucursal.
7. El tablero debe ser responsive y visualizarse correctamente desde dispositivos móviles.
8. El tablero debe cargar en un tiempo menor a 5 segundos.
9. Si no hay datos suficientes para calcular un indicador, el sistema debe mostrar: «No hay información suficiente para el periodo seleccionado».

#### HU-19: Revisar promociones sugeridas

- **Como:** Jefe de cocina de un punto del restaurante.
- **Quiero:** Recibir y revisar las promociones y productos destacados que el sistema identifica automáticamente.
- **Para:** Para aprovechar oportunidades de compra y ajustar mi menú según las tendencias del mercado.

**Criterios de aceptación:**
1. El sistema debe identificar automáticamente tendencias a partir del comportamiento en redes sociales.
2. El sistema debe identificar los productos más solicitados para ofrecer promociones.
3. El sistema debe enviarme un correo electrónico con las promociones o productos destacados.
4. Debo poder consultar dentro de la aplicación el histórico de promociones sugeridas.
5. Cada sugerencia debe indicar el producto, el motivo de la recomendación y el periodo de vigencia.
6. Debo poder marcar una sugerencia como aceptada o descartada.
7. El sistema no debe enviar más de una notificación de promociones por día.
8. Si el análisis de tendencias falla, el error debe quedar registrado en el log sin interrumpir el resto del sistema.

## 6. Planning Poker

Original: página 27. Cartas de la escala: 1, 2, 3, 5, 8 y 13. Cada carta es un número de PHU, y 1 PHU equivale a 25 horas.

*Corregido (S-04): las etiquetas del original («25PHU», «50PHU», «75PHU» y «125PHU») son horas, no PHU: 25 h = 1 PHU, 50 h = 2 PHU, 75 h = 3 PHU y 125 h = 5 PHU. HU-14 vale 1 PHU (25 h).*

| Historia | PHU | Horas |
|---|---|---|
| HU-01 Registrar pedido/orden | 1 | 25 h |
| HU-02 Ingresar cantidad de platos | 1 | 25 h |
| HU-03 Indicar mesa del pedido | 1 | 25 h |
| HU-04 Registrar platos/menú | 1 | 25 h |
| HU-05 Ingresar ingredientes plato | 1 | 25 h |
| HU-06 Consultar solicitudes de compras | 1 | 25 h |
| HU-07 Filtrar solicitudes por criterios | 1 | 25 h |
| HU-08 Ver detalles de solicitud | 1 | 25 h |
| HU-09 Generar solicitud de cotización | 2 | 50 h |
| HU-10 Registrar solicitud de alimentos | 2 | 50 h |
| HU-11 Registrar valores de cotización | 2 | 50 h |
| HU-12 Generar solicitud de cotización a proveedores | 2 | 50 h |
| HU-13 Validar cotizaciones recibidas | 2 | 50 h |
| HU-14 Generar orden de compra | 1 | 25 h |
| HU-15 Registrar datos de proveedor | 1 | 25 h |
| HU-16 Recibir alertas automáticas de inventario bajo | 5 | 125 h |
| HU-17 Consultar cotizaciones opcionadas | 2 | 50 h |
| HU-18 Ver KPIs y tablero de control | 3 | 75 h |
| HU-19 Revisar promociones sugeridas | 5 | 125 h |
| **Total** | **35** | **875 h** |

## 7. Supuesto de capacidad

Original: páginas 28 a 30.

| Elemento | Valor |
|---|---|
| Cantidad de personas | 4 |
| Dedicación semanal | 40 |
| Sprint | 2 |
| Factor productividad | 0.7 |
| Capacidad total por sprint | 224 |
| PHU en horas | 25 |
| PHUs por sprint | 8.96 (9 PHU) |

**Explicación de la tabla de capacidad del equipo:**

- **Cantidad de personas (4):** el equipo de desarrollo está compuesto por 4 miembros.
- **Dedicación semanal (40):** cada miembro del equipo dedica 40 horas semanales al proyecto (jornada laboral completa).
- **Duración por sprint (2):** la duración de cada sprint es de 2 semanas.
- **Factor de productividad (0.7):** representa la eficiencia real del equipo, considerando que no todo el tiempo dedicado es 100 % productivo (reuniones, daily meetings, imprevistos, cambios de contexto, revisión de código, etc.). Un factor de 0.7 significa que el equipo es productivo el 70 % del tiempo dedicado. Es un factor conservador, apropiado para equipos que están iniciando o que trabajan con tecnologías nuevas.
- **Capacidad total por sprint (224):** *(corregido, S-03: el original decía 120, pero la cuenta y la tabla dan 224)* es el total de horas productivas reales que el equipo puede entregar en un sprint. Se calcula como: 4 personas × 40 horas × 2 semanas × 0.7 de productividad = 224 horas.
- **PHU en horas (25):** es la equivalencia definida por el equipo: 1 punto de historia de usuario corresponde a 25 horas de trabajo. Sirve como unidad de medida estándar para estimar el esfuerzo de cada historia.
- **PHUs por sprint (8.96 ≈ 9 PHU):** es la velocidad (velocity) del equipo: cuántos puntos de historia de usuario puede completar en un sprint. Se calcula como: 224 horas de capacidad ÷ 25 horas por PHU = 8.96 PHU. Se redondea a 9 PHU por sprint, que es el compromiso máximo que el equipo puede asumir sin sobrecargarse.

## 8. Plan de sprints

Original: página 31.

*Corregido (S-06): el original planeaba 4 sprints, 8 semanas y «2 Meses», con los sprints 2 y 3 sobrecargados (10 PHU sobre una capacidad de 9). El plan del equipo, calculado en el Excel de capacidad, es de 5 sprints de 2 semanas: 10 semanas (2 meses y medio), del 3 de agosto al 11 de octubre de 2026, sin superar los 9 PHU de ningún sprint (DEC-47). Los entregables son los del Excel.*

| Sprint | Fechas | MVP | Historias | PHU | Entregables |
|---|---|---|---|---|---|
| Sprint 1 | 03/08 al 16/08/2026 | MVP 1, MVP 2 | HU-01 a HU-08 | 8 de 9 | Módulo de captura operativo: registro de pedidos con mesa y cantidades, y gestión completa del menú con sus platos e ingredientes. Se entrega el listado de solicitudes de compra con filtros por criterios y acceso al detalle. |
| Sprint 2 | 17/08 al 30/08/2026 | MVP 2, MVP 3 | HU-09 a HU-12 | 8 de 9 | Ciclo de solicitudes y cotizaciones funcionando: registro de solicitud de alimentos, generación de cotizaciones y captura de valores del proveedor. Se entrega el envío formal de cotizaciones a proveedores con PDF y correo certificado. |
| Sprint 3 | 31/08 al 13/09/2026 | MVP 3, MVP 4 | HU-13, HU-14, HU-15 y HU-17 | 6 de 9 | Cierre del proceso de compras: validación de cotizaciones contra el precio del Ministerio, generación de la orden de compra, maestro de proveedores y consulta de cotizaciones opcionadas. |
| Sprint 4 | 14/09 al 27/09/2026 | MVP 4 | HU-16 y HU-18 | 8 de 9 | Descuento automático diario de inventario con alertas por correo al bajar del 25 %. Se entrega el tablero de control (BAM) con los KPIs de mayor demanda, sucursal líder y mejor proveedor del año. |
| Sprint 5 | 28/09 al 11/10/2026 | MVP 4 | HU-19 | 5 de 9 | Motor de identificación de tendencias en redes sociales y de productos más solicitados. Se entregan las promociones sugeridas con notificación automática a los puntos de venta y el despliegue final del sistema. |
| **Total** | **10 semanas (2 meses y medio), del 03/08 al 11/10/2026** | Proyecto completo | 19 historias | **35 de 45** | Sistema completo y desplegado |

Cada sprint dura 2 semanas. Quedan libres 1, 1, 3, 1 y 4 PHU por sprint: 10 PHU de holgura (45 de capacidad contra 35 de trabajo), que absorben imprevistos sin mover las fechas.

HU-17 (Consultar cotizaciones opcionadas) es del MVP 4, pero entra en el sprint 3, antes que HU-16. Así el ciclo de compras queda completo en ese sprint: HU-13 valida las cotizaciones, HU-17 muestra las opcionadas y HU-14 genera la orden sobre una de ellas. Por eso, en la hoja SPRINT del Excel, F17 va antes que F16.

Página 32: cierre («Gracias», agosto de 2026).
