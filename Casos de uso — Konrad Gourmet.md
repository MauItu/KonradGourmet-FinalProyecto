# Casos de uso — Konrad Gourmet

Nov 7, 2026 · @Carlos

Este documento fija 18 casos de uso, cada uno amarrado a una historia de usuario, a un requisito del enunciado y a sus ventanas. Los criterios salen de las historias del Story Mapping; lo que el equipo no definió queda marcado como propuesta.

## Estado frente al prototipo (8 de noviembre)

Este cuadro contrasta cada caso de uso con el repositorio github.com/MauItu/KonradGourmet-FinalProyecto (carpeta `codigo/`, commit `22717f7` de `KonradGourmet`, 8 de noviembre). Las sesiones 4 (demo real) y 5 (documentos de cierre) están terminadas, y en la sesión 6 el pedido pasó a guardar el mesero que lo tomó (DEC-51). El código compila sin avisos, la demo (`./ejecutar.sh`) recorre el flujo de compra con adaptadores en memoria y pasan las 176 verificaciones. De los 274 cuerpos que lanzaban «Pendiente de implementar» al cerrar la sesión 3 quedan 94: los adaptadores de tecnología externa y los módulos que la demo no usa. Tienen lógica CU-02, CU-04, CU-05, CU-07 a CU-14, CU-16, CU-17 y CU-18, y parte de CU-15 (parámetros, tema y destinatarios). Siguen sin lógica CU-01, CU-03, CU-06 y el alta de usuarios. Las decisiones del equipo están en `DECISIONES.md`.

| CU | Qué declara el código del 8 de noviembre | Qué cambia o sigue abierto | ¿Tiene lógica? |
| --- | --- | --- | --- |
| CU-01 | V01: `POST /api/auth/token`. Alta de usuarios por el administrador: `POST /api/usuarios`, sin ventana (DEC-10) | La única pantalla del prototipo es V01 y no se conecta a ningún servicio | No: `IniciarSesionService` sigue pendiente |
| CU-02 | V02: `GET /api/pedidos?turno=actual`. V03 y V04: `POST /api/pedidos`. Catálogo y mesas: `GET /api/pedidos/catalogo` y `GET /api/pedidos/mesas` (H6) | `Pedido` tiene número, mesa, fecha, estado y detalles; no guarda sucursal ni usuario, que el paso 7 exige. Decisión del equipo pendiente | Sí |
| CU-03 | V06: `POST /api/platos`, productos por categoría, y editar (PUT) y desactivar (PATCH). `Plato` guarda `sucursalId` y `creadoPor` (DEC-11) | Coincide con el flujo alterno «consultar, editar y desactivar» | No: ms-menu sigue pendiente |
| CU-04 | V08 y V09: `POST /api/solicitudes` (201). La solicitud guarda `correoSolicitante` (DEC-13) | Coincide | Sí |
| CU-05 | V05: `GET /api/inventario/alertas`. V07: `GET /api/inventario/insumos`. ms-inventario consume `solicitud.registrada` y apaga la alerta (DEC-18), como pide HU-16 | La alerta es solo la marca `alertaAtendida` del insumo; no existe una entidad Alerta (B12, parcial). El 25 % es `UmbralStock`, propio de ms-inventario | Sí |
| CU-06 | `/api/bam/promociones`: sugerencias por fechas, aceptar y descartar. `SugerenciaPromocion` guarda producto, motivo, vigencia y estado | Sigue sin mockup (B8). La nota del código indica que `notificadaEl` limita el aviso a uno por día | No: las promociones siguen pendientes |
| CU-07 | V11: `GET /api/solicitudes?estado=…`. V12: `GET /api/solicitudes/{id}`. `CriteriosConsulta` trae fechas, producto, marca y estado | Los cuatro filtros quedan en la firma (B16, parcial) | Sí |
| CU-08 | V13: `POST /api/cotizaciones`. V14: `GET /api/cotizaciones/{id}/pdf`. La cotización guarda una lista de solicitudes (DEC-05), el estado `PENDIENTE_PRECIO` y un reenvío del correo (H18) | Los flujos «Ministerio no responde» y «reintentar el envío» ya tienen estado y operación en el diseño | Sí |
| CU-09 | `/api/proveedores`: registrar, consultar (el desplegable de V13), detalle, editar e inactivar (DEC-07) | Sigue sin ventana propia. `Proveedor.categorias` es una lista de texto: el equipo no ha decidido si será referencia a las categorías | Sí (`ProveedorService`; la demo guarda en memoria) |
| CU-10 | V18: `PUT /api/cotizaciones/{id}/valores` | Coincide | Sí |
| CU-11 | V19: `POST /api/cotizaciones/{id}/validacion`. V17: `GET /api/cotizaciones` | Coincide | Sí |
| CU-12 | V20: `GET /api/cotizaciones?estado=OPCIONADA` y `GET /api/cotizaciones/{id}` (H5) | Coincide | Sí |
| CU-13 | V21 y V22: `POST /api/ordenes-compra`. V23: `GET /api/ordenes-compra`. La cotización pasa a `CON_ORDEN` y no admite otra orden (D3). La orden guarda lista de solicitudes, `correoProveedor` y `pdfUrl`, y tiene su propio generador de PDF (H4) | B10 queda resuelta en el diseño. Los estados de la orden siguen siendo EN\_PROCESO y FINALIZADA (B13). `EstadoSolicitud` tiene `COMPLETADA` (H12); el texto del Story Mapping que extraje no lo menciona | Sí |
| CU-14 | `GET /api/bam/kpis?desde&hasta&sucursalId` (H7). El tablero es del director comercial (DEC-06) | Sigue sin mockup. B17 (periodos fijos del enunciado frente al filtro de la historia) sigue abierta | Sí (los hechos son un histórico cargado a mano) |
| CU-15 | `parametrizar(clave, valor)` y `GET /api/admin/parametros/tema`. `ParametrosSistema` guarda tema, logos y parámetros generales | El umbral del 25 % no vive ahí y el código no declara ninguna ruta para cambiarlo: el criterio (3) de HU-20 no tiene dónde apoyarse. La libreta de destinatarios por sucursal (`/api/notificaciones/destinatarios`, DEC-13) es un dato de parametrización que este caso aún no lista | En parte: parámetros, tema y destinatarios |
| CU-16 | `auditoria(desde, hasta)` | Coincide | Sí |
| CU-17 | `logs(servicio)` con un puerto propio para los logs (H14) | Coincide | Sí (la demo guarda los logs en memoria; Elasticsearch sigue pendiente) |
| CU-18 | `consultarPrecioSugerido(productoId)` en el endpoint SOAP, publicado bajo `/api/integracion/**` | Coincide | Sí (la demo simula el Ministerio) |

**Ventanas que ningún caso de uso cita.** El prototipo declara métodos para V10 (`GET /api/solicitudes?solicitante=yo`, mis solicitudes del jefe de cocina), V15 (`GET /api/cotizaciones`, cotizaciones del auxiliar) y V16 (`GET /api/cotizaciones/pendientes`, inicio del director). La matriz de trazabilidad ya las trata como consultas de apoyo que no cierran ninguna historia. Falta decidir si se anexan como flujos alternos de CU-04, CU-08 y CU-11 o se dejan como consultas de apoyo.

## Dónde implementar cada caso de uso

Este cuadro dice dónde vive cada caso de uso en el código. Sale de los controladores, listeners y puertos de entrada (carpeta `codigo/src/co/edu/konradlorenz/` del repositorio público; `src/co/edu/konradlorenz/` en `KonradGourmet`) y de las clases de eventos. En los casos que tienen lógica, la demo y las pruebas confirman los eventos que cada uno publica o consume; en CU-01, CU-03 y CU-06 se leen de los encabezados y de `DECISIONES.md`.

| CU | Adaptador de entrada (servicio) | Método del puerto de entrada | Eventos |
| --- | --- | --- | --- |
| CU-01 | `AuthRestController.iniciarSesion` (identidad) | `IniciarSesionUseCase.iniciarSesion(correo, clave)` | Auditoría (`AuditoriaEvent`) |
| CU-02 | `PedidoRestController`: `pedidosDelTurno`, `registrar`, `catalogo`, `mesas` (pedidos) | `RegistrarPedidoUseCase`: los mismos cuatro, y `actualizarCatalogo` | Publica `PedidoRegistradoEvent`. `MenuActualizadoListener` consume `menu.actualizado` |
| CU-03 | `PlatoRestController`: `crear`, `editar`, `desactivar`, `productosPorCategoria` (menu) | `RegistrarPlatoUseCase`: `registrar`, `editar`, `desactivar`, `productosPorCategoria`, `consultar` | Publica `MenuActualizadoEvent` |
| CU-04 | `SolicitudRestController.registrar` y `misSolicitudes` (solicitudes) | `GestionarSolicitudUseCase`: `registrar` y `consultar` | Publica `SolicitudRegistradaEvent`; ms-inventario la consume y apaga la alerta |
| CU-05 | `ConsumoDiarioJob` (diario 23:59), `PedidoRegistradoListener`, `InventarioEventsListener` y `InventarioRestController`: `alertas`, `insumos` (inventario) | `GestionarInventarioUseCase`: `descontarConsumoDiario`, `acumularConsumo`, `actualizarReceta`, `atenderAlertas`, `alertas`, `insumos` | Consume `pedido.registrado`, `menu.actualizado` y `solicitud.registrada`. Publica `StockBajoEvent` |
| CU-06 | `TendenciasJob` (semanal) y `PromocionRestController` (analitica) | `TableroUseCase`: `detectarTendencias`, `sugerencias`, `aceptarSugerencia`, `descartarSugerencia` | Publica `TendenciaDetectadaEvent` |
| CU-07 | `SolicitudRestController.consultar` y `detalle` (solicitudes) | `GestionarSolicitudUseCase`: `consultar`, `detalle` | — |
| CU-08 | `CotizacionRestController`: `generar`, `pdf`, `reenviar` (cotizaciones) | `GestionarCotizacionUseCase`: `generar`, `pdf`, `reenviar` | Publica `CotizacionEnviadaEvent`; ms-solicitudes la consume (`CotizacionEventsListener`) y puede publicar `SolicitudEnTramiteEvent` |
| CU-09 | `ProveedorRestController` (cotizaciones) | `GestionarProveedorUseCase`: `registrar`, `consultar`, `detalle`, `editar`, `inactivar` | — |
| CU-10 | `CotizacionRestController.registrarValores` | `GestionarCotizacionUseCase.registrarValores` | — |
| CU-11 | `CotizacionRestController.validar` y `consultar` | `GestionarCotizacionUseCase.validar`, `consultar` | Publica `CotizacionValidadaEvent` |
| CU-12 | `CotizacionRestController.opcionadas` y `detalle` | `GestionarCotizacionUseCase.consultar`, `detalle` | — |
| CU-13 | `OrdenCompraRestController`: `generar`, `consultar` (ordenes) | `GenerarOrdenUseCase`: `generar`, `consultar` | Publica `OrdenGeneradaEvent`. `OrdenGeneradaListener` (cotizaciones) llama a `marcarConOrden`; ms-solicitudes llama a `completar` |
| CU-14 | `TableroRestController.kpis` y `NegocioEventsListener` (analitica) | `TableroUseCase.kpis`, `registrarHecho` | Consume 5 tópicos |
| CU-15 | `AdminRestController.parametrizar` y `tema` (administracion); `DestinatarioRestController` (notificaciones); `UsuarioRestController.registrar` (identidad) | `AdministracionUseCase.parametrizar`, `tema`; `NotificarUseCase.registrarDestinatario`, `destinatarios`; `RegistrarUsuarioUseCase.registrar` | Auditoría |
| CU-16 | `AdminRestController.auditoria` y `AuditoriaListener` | `AdministracionUseCase.auditoria`, `registrarAuditoria` | Consume `auditoria.evento` |
| CU-17 | `AdminRestController.logs` | `AdministracionUseCase.logs` | — |
| CU-18 | `AbastecimientoSoapEndpoint.consultarPrecioSugerido` (integracion) | `ConsultarPrecioUseCase.precioSugerido(productoId)` | — |

**Decisiones del equipo que afectan a estos casos y siguen abiertas.** Pedido sin sucursal ni usuario (CU-02), categorías del proveedor como texto (CU-09), alerta como marca del insumo (CU-05), para qué sirve `cotizacionesAplicadas` en la solicitud (CU-08) y qué ruta cambia el umbral del 25 % (CU-15). También están abiertas K9, K10 y K11 de la validación de la sesión 3 (`VALIDACION_SESION3.md`): K10 es que el descuento diario de CU-05 correría en cada réplica de ms-inventario, de 2 a 6, si no se asegura que lo ejecute una sola.

## Actores

Hay seis roles que usan el sistema y cinco actores externos. Los seis roles salen del diagrama de contexto C4 del informe de arquitectura (página 2 del `.drawio`); los externos, del mismo diagrama y del enunciado.

| Actor | Tipo | Qué hace en el sistema | Fuente |
| --- | --- | --- | --- |
| Mesero | Rol principal | Registra los pedidos de cada mesa | RF-02; HU-01 a HU-03 |
| Jefe de cocina | Rol principal | Registra platos, solicita alimentos, recibe alertas de inventario y revisa promociones | RF-01, RF-03, RF-04, RF-10; HU-04, HU-05, HU-10, HU-16, HU-19 |
| Auxiliar de compras | Rol principal | Consulta solicitudes, envía solicitudes de cotización y mantiene proveedores | RF-05, RF-06, RF-12; HU-06 a HU-09, HU-12, HU-15 |
| Director de compras | Rol principal | Registra valores, valida cotizaciones y genera órdenes de compra | RF-07, RF-08; HU-11, HU-13, HU-14, HU-17 |
| Director comercial | Rol principal | Consulta el tablero BAM | RF-09; HU-18 |
| Administrador | Rol principal | Parametriza, consulta la auditoría y los logs de error | RF-11 |
| Proveedor | Externo (persona) | Recibe el PDF de la solicitud de cotización y de la orden; responde por correo, fuera del sistema | Informe de arquitectura, supuesto S2; HU-12, HU-14 |
| Ministerio de Comercio | Externo (sistema) | Publica un servicio web con el precio estándar por producto | RF-06; HU-12 |
| Servidor de correo certificado | Externo (sistema) | Entrega los correos con certificación y estampado cronológico | Requisito de seguridad 5; HU-12, HU-14 |
| Redes sociales | Externo (sistema) | Aportan señales de tendencia de productos | RF-10; HU-19 |
| Sistemas externos | Externo (sistema) | Consumen los servicios que el restaurante expone por SOAP | Requisito de integración |

Los códigos RF-01 a RF-12 son los de la matriz de trazabilidad: numeran los requisitos funcionales del enunciado en el orden en que aparecen, porque el enunciado reinicia su lista (1, 2, 3 y luego 2, 3, 4...).

## Diagrama de casos de uso

![Diagrama de casos de uso](casos_de_uso.png)

> **Pendiente:** el diagrama UML lo tiene quien escribió este documento. Falta exportarlo a `casos_de_uso.png`, en la misma carpeta que este archivo, con los casos de este documento (incluido CU-19, Registrar usuario), los 6 roles y los 5 actores externos.

Los roles de cocina y compras están a la izquierda y los roles de dirección, administración y los actores externos a la derecha. Los correos de CU-04 y CU-05 también salen por el servidor de correo certificado; esa línea no se dibuja para no cruzar las otras.

## Catálogo de casos de uso

Trece casos agrupan las 19 historias de usuario; los cinco restantes son el inicio de sesión, los tres del administrador y el servicio SOAP. El MVP es el del Story Mapping.

| CU | Nombre | Actor principal | Actores secundarios | Historias | Requisito | Ventanas | MVP |
| --- | --- | --- | --- | --- | --- | --- | --- |
| CU-01 | Iniciar sesión | Los seis roles | — | Ninguna (requisitos de seguridad) | — | V01 | — |
| CU-02 | Registrar pedido | Mesero | — | HU-01, HU-02, HU-03 | RF-02 | V02, V03, V04 | 1 |
| CU-03 | Registrar plato con ingredientes | Jefe de cocina | — | HU-04, HU-05 | RF-01 | V06 | 1 |
| CU-04 | Registrar solicitud de alimentos | Jefe de cocina | Servidor de correo certificado | HU-10 | RF-03 | V08, V09 | 2 |
| CU-05 | Recibir alerta de inventario bajo | Jefe de cocina | Servidor de correo certificado | HU-16 | RF-04 | V05, V07 | 4 |
| CU-06 | Revisar promociones sugeridas | Jefe de cocina | Redes sociales, servidor de correo certificado | HU-19 | RF-10 | Sin mockup | 4 |
| CU-07 | Consultar solicitudes de compras | Auxiliar de compras | — | HU-06, HU-07, HU-08 | RF-05 | V11, V12 | 2 |
| CU-08 | Enviar solicitud de cotización | Auxiliar de compras | Proveedor, Ministerio de Comercio, servidor de correo certificado | HU-09, HU-12 | RF-06 | V13, V14 | 2 y 3 |
| CU-09 | Registrar datos de proveedor | Auxiliar de compras | — | HU-15 | RF-12 | Sin ventana | 3 |
| CU-10 | Registrar valores de cotización | Director de compras | — | HU-11 | RF-07 | V18 | 2 |
| CU-11 | Validar cotización | Director de compras | — | HU-13 | RF-07 | V17, V19 | 3 |
| CU-12 | Consultar cotizaciones opcionadas | Director de compras | — | HU-17 | RF-08 | V20 | 4 |
| CU-13 | Generar orden de compra | Director de compras | Proveedor, servidor de correo certificado | HU-14 | RF-08 | V21, V22, V23 | 3 |
| CU-14 | Consultar tablero BAM | Director comercial | — | HU-18 | RF-09 | Sin mockup | 4 |
| CU-15 | Parametrizar el sistema | Administrador | — | HU-20 (propuesta) | RF-11 | Sin mockup | — |
| CU-16 | Consultar auditoría | Administrador | — | HU-21 (propuesta) | RF-11 | Sin mockup | — |
| CU-17 | Consultar logs de errores | Administrador | — | HU-22 (propuesta) | RF-11 | Sin mockup | — |
| CU-18 | Consultar precio sugerido (SOAP) | Sistemas externos | — | Ninguna (requisito de integración) | — | Sin ventana | — |

CU-18 viene del informe de arquitectura, que expone `consultarPrecioSugerido` por SOAP. El enunciado exige que todo servicio expuesto a sistemas externos use SOAP, pero no dice cuál exponer; es una decisión de diseño y como tal hay que defenderla.

## Especificación: acceso, mesero y jefe de cocina

Cada caso copia los criterios de aceptación de su historia en el Story Mapping, incluidos los mensajes de error textuales. Lo que viene del informe de arquitectura o del flujo web se señala en la línea de fuente.

### CU-01 Iniciar sesión

**Actor:** cualquiera de los seis roles.

**Precondición:** el usuario existe, tiene un rol y pertenece a una sucursal.

**Flujo principal:**

1. El usuario abre la web e ingresa correo y contraseña (V01).
2. El sistema valida las credenciales.
3. El sistema lo envía a la pantalla de inicio de su rol y arma el menú lateral según ese rol.

**Flujos alternos:**

- Credenciales inválidas: el sistema muestra el error y deja reintentar (flujo web).
- "Recordarme" mantiene la sesión en el equipo; el ojo de la contraseña la muestra u oculta (flujo web).
- "¿Olvidaste tu contraseña?" lleva a una pantalla de recuperación que no tiene mockup (flujo web).
- Usuario sin rol de acceso: acceso denegado (mapa de sitio del deck UX/UI).

**Postcondición:** el usuario queda autenticado con su rol y permisos.

**Reglas y fuente:** la contraseña debe tener 8 caracteres o más, con al menos una mayúscula, una minúscula y un número, y se guarda cifrada (requisitos de seguridad 3 y 4 del enunciado). Toda comunicación va por HTTPS (requisito 2). El informe de arquitectura asigna este caso a ms-identidad, con `POST /api/auth/token`.

### CU-02 Registrar pedido

**Actor:** mesero. **Historias:** HU-01, HU-02, HU-03. **Ventanas:** V02, V03, V04.

**Precondición:** sesión iniciada como mesero; hay platos en el menú vigente; las mesas están parametrizadas por sucursal.

**Flujo principal:**

1. El mesero abre "Nuevo pedido" (V03).
2. Agrega uno o varios platos desde una lista desplegable del menú vigente.
3. Ingresa la cantidad de cada plato (campo obligatorio, entero mayor a cero).
4. Selecciona la mesa en una lista desplegable parametrizada por sucursal (campo obligatorio).
5. El sistema recalcula cada subtotal y el total sin recargar la pantalla.
6. El mesero confirma.
7. El sistema asocia el pedido a su sucursal y a su usuario, registra fecha y hora, genera un número consecutivo y lo muestra (V04).

**Flujos alternos:**

- Sin ningún plato: el sistema no permite guardar.
- Cantidad no numérica, negativa o cero: "La cantidad debe ser un número entero mayor a cero".
- Mesa vacía: "Debe seleccionar una mesa para continuar".
- El mesero puede modificar la cantidad antes de confirmar.

**Postcondición:** pedido registrado con su consecutivo; la mesa queda visible en el detalle del pedido; el movimiento queda en la auditoría (acción, usuario, fecha, hora).

**Reglas y fuente:** el valor total sale del precio de venta de cada plato. Se pueden consultar los pedidos activos filtrando por mesa (HU-03). Según el informe, el pedido publica el evento `pedido.registrado`, que alimenta el descuento de inventario y los KPI.

### CU-03 Registrar plato con ingredientes

**Actor:** jefe de cocina. **Historias:** HU-04, HU-05. **Ventana:** V06.

**Precondición:** sesión iniciada como jefe de cocina; categorías, productos y unidades parametrizados.

**Flujo principal:**

1. El jefe abre "Nuevo plato" (V06) e ingresa el nombre (obligatorio, único por sucursal) y el precio de venta (obligatorio, mayor a cero).
2. Agrega ingredientes con un listado dinámico: categoría (lista desplegable), producto (lista que depende de la categoría), cantidad (mayor a cero) y unidad (lista desplegable).
3. Puede eliminar o modificar ingredientes antes de guardar.
4. Guarda. El sistema valida el formato de todos los datos y asocia el plato a su sucursal y a su usuario.

**Flujos alternos:**

- Nombre repetido en la sucursal: "Ya existe un plato registrado con ese nombre".
- Categoría sin productos: "No hay productos disponibles para la categoría seleccionada".
- Sin ningún ingrediente: el sistema no permite guardar.
- El jefe puede consultar, editar y desactivar platos ya registrados.

**Postcondición:** plato registrado con su receta; cada acción queda en la auditoría.

**Reglas y fuente:** la receta permite que el sistema calcule el consumo de insumos por cada venta. El informe publica `menu.actualizado` para refrescar el catálogo del mesero.

### CU-04 Registrar solicitud de alimentos

**Actor:** jefe de cocina. **Actor secundario:** servidor de correo certificado. **Historia:** HU-10. **Ventanas:** V08, V09.

**Precondición:** sesión iniciada; el jefe no tiene stock disponible de uno o más productos.

**Flujo principal:**

1. El jefe abre "Nueva solicitud de alimentos" (V08). La sucursal y el solicitante aparecen automáticamente.
2. Agrega uno o varios productos: categoría (obligatoria), producto dependiente de la categoría (obligatorio), cantidad (mayor a cero, obligatoria), marca (lista opcional) y unidad (obligatoria).
3. Envía la solicitud. El sistema verifica el formato de todos los datos.
4. Si todo es correcto, la solicitud queda en estado PENDIENTE, el sistema muestra su número (V09) y envía un correo de confirmación al solicitante.

**Flujos alternos:**

- Algún dato inválido: el sistema indica el campo con error y no registra la solicitud.

**Postcondición:** solicitud PENDIENTE con número y correo enviado.

**Reglas y fuente:** el correo debe ser certificado y con estampado cronológico (requisito de seguridad 5).

### CU-05 Recibir alerta de inventario bajo

**Actor:** jefe de cocina. **Actor secundario:** servidor de correo certificado. **Historia:** HU-16. **Ventanas:** V05, V07.

**Disparador:** tarea diaria del sistema (el `.drawio` la programa a las 23:59).

**Flujo principal:**

1. El sistema verifica las órdenes preparadas o vendidas del día y descuenta las cantidades correspondientes del inventario.
2. Cuando cualquier producto llega al 25 % de su cantidad, genera una alarma visible en la aplicación y envía un correo avisando que debe hacerse una solicitud de alimentos.
3. La alerta indica producto, cantidad restante, unidad y sucursal, y da acceso directo al formulario de solicitud de alimentos (CU-04).

**Flujos alternos:**

- Si el proceso diario falla, el error queda registrado en el log del sistema.

**Postcondición:** la alerta permanece activa hasta que se registre una solicitud para ese producto.

**Reglas y fuente:** el proceso no debe afectar el desempeño en horario de operación. El 25 % es un parámetro (ver CU-15); la brecha B12 de la matriz de trazabilidad recoge que el modelo no tiene una entidad Alerta; en el código del 8 de noviembre la alerta es solo la marca alertaAtendida del insumo.

### CU-06 Revisar promociones sugeridas

**Actor:** jefe de cocina. **Actores secundarios:** redes sociales, servidor de correo certificado. **Historia:** HU-19. **Ventana:** sin mockup (B8).

**Disparador:** análisis automático de tendencias.

**Flujo principal:**

1. El sistema identifica tendencias a partir del comportamiento en redes sociales y los productos más solicitados.
2. Envía al jefe un correo con las promociones o productos destacados.
3. El jefe consulta dentro de la aplicación el histórico de promociones sugeridas. Cada sugerencia indica el producto, el motivo y el periodo de vigencia.
4. El jefe marca una sugerencia como aceptada o descartada.

**Flujos alternos:**

- Si el análisis de tendencias falla, el error queda en el log sin interrumpir el resto del sistema.

**Reglas y fuente:** no se envía más de una notificación de promociones por día. Ni la historia ni el enunciado dicen de qué red social se leen las tendencias ni cuántos días abarca el análisis; eso queda por definir.

## Especificación: auxiliar y director de compras

### CU-07 Consultar solicitudes de compras

**Actor:** auxiliar de compras. **Historias:** HU-06, HU-07, HU-08. **Ventanas:** V11, V12.

**Precondición:** sesión iniciada como auxiliar y perfil con el permiso de consulta.

**Flujo principal:**

1. El auxiliar abre el listado consolidado de solicitudes de todas las sucursales (V11). Muestra fecha de solicitud, producto, cantidad, marca y estado, ordenado por fecha descendente y paginado si hay más de 25 registros.
2. Filtra por rango de fechas, producto, marca y estado.
3. Abre el detalle de una solicitud (V12): número, sucursal, solicitante, fecha, estado, valor total sugerido y el listado de productos con categoría, producto, cantidad, marca y unidad.
4. Regresa al listado conservando los filtros aplicados.

**Flujos alternos:**

- Sin resultados: "No se encontraron solicitudes registradas".
- Solicitud inexistente o eliminada: "La solicitud consultada no está disponible".

**Reglas y fuente:** la consulta responde en menos de 3 segundos y el detalle no permite modificar datos. Los cuatro filtros salen del enunciado: HU-07 repite los criterios de HU-06 y no los enumera (brecha B16).

### CU-08 Enviar solicitud de cotización

**Actor:** auxiliar de compras. **Actores secundarios:** proveedor, Ministerio de Comercio, servidor de correo certificado. **Historias:** HU-09, HU-12. **Ventanas:** V13, V14.

**Precondición:** existen solicitudes de alimentos y proveedores registrados.

**Flujo principal:**

1. El auxiliar selecciona una o varias solicitudes del listado.
2. Elige el proveedor en una lista desplegable (obligatorio) y registra la fecha máxima de plazo (obligatoria, no anterior a la fecha actual).
3. El sistema muestra un resumen de productos y cantidades antes de confirmar.
4. El auxiliar acepta. El sistema genera un número consecutivo de cotización, la deja en estado ENVIADA y genera un PDF con productos, cantidades, unidades y fecha máxima de plazo.
5. El sistema envía el PDF por correo certificado y con estampado cronológico a la cuenta registrada del proveedor (V14).
6. El sistema verifica si todos los alimentos de cada solicitud ya tienen cotización; si es así, la solicitud pasa de PENDIENTE a EN TRÁMITE.
7. Por cada producto, el sistema consulta el precio sugerido del servicio web del Ministerio de Comercio y actualiza la solicitud con el valor total sugerido.

**Flujos alternos:**

- Ninguna solicitud seleccionada: "Debe seleccionar al menos una solicitud".
- El servicio del Ministerio no responde: el sistema registra el error en el log y notifica al usuario sin perder la cotización.
- Falla el envío del correo: el sistema permite reintentar.

**Postcondición:** cotización ENVIADA con su PDF; solicitudes posiblemente EN TRÁMITE; valor sugerido registrado.

**Reglas y fuente:** HU-09 cubre la captura (pasos 1 a 3) y HU-12 el envío (pasos 4 a 7); ambas usan las mismas ventanas (brecha B6). El informe de arquitectura guarda en caché el precio del Ministerio (supuesto S3: cambia como máximo una vez al día).

### CU-09 Registrar datos de proveedor

**Actor:** auxiliar de compras. **Historia:** HU-15. **Ventana:** ninguna (B7).

**Flujo principal:**

1. El auxiliar ingresa razón social (obligatoria), NIT (obligatorio y único), correo electrónico (formato válido, obligatorio), teléfono y dirección.
2. Asocia al proveedor una o varias categorías de producto que maneja.
3. Guarda. También puede consultar, editar e inactivar proveedores.

**Flujos alternos:**

- NIT repetido: "Ya existe un proveedor registrado con ese número de identificación".
- Correo mal formado: "El formato del correo electrónico no es válido".

**Reglas y fuente:** un proveedor inactivo no aparece en las listas desplegables de cotización. Cada acción queda en la auditoría.

### CU-10 Registrar valores de cotización

**Actor:** director de compras. **Historia:** HU-11. **Ventana:** V18.

**Precondición:** existe una cotización ENVIADA y el proveedor respondió por correo, fuera del sistema (supuesto S2 del informe).

**Flujo principal:**

1. El director busca y selecciona la cotización generada por el sistema.
2. Ingresa el valor unitario ofertado por cada producto (solo numéricos mayores a cero).
3. El sistema calcula el valor total de la cotización.
4. El director guarda, total o parcialmente, y puede continuar después.

**Flujos alternos:**

- Cotización ya validada: el sistema no permite modificar los valores.

**Postcondición:** valores registrados; cada registro o modificación queda en la auditoría.

### CU-11 Validar cotización

**Actor:** director de compras. **Historia:** HU-13. **Ventanas:** V17, V19.

**Precondición:** la cotización tiene valores registrados (CU-10).

**Flujo principal:**

1. El director selecciona "Validar" sobre la cotización.
2. El sistema compara el valor cotizado con el valor estándar del Ministerio. Si lo supera en más de 25 %, queda RECHAZADA; si está por debajo en más de 50 %, queda SOSPECHOSA; si está dentro de ese rango, queda OPCIONADA.
3. El sistema muestra el resultado con el valor estándar, el valor cotizado y el porcentaje de diferencia (el mockup V19 muestra $520.000, $545.000, +4,81 % y OPCIONADA).

**Flujos alternos:**

- Sin valores registrados: "La cotización no tiene valores registrados para validar".

**Postcondición:** el cambio de estado queda en la auditoría (acción, usuario, fecha, hora).

**Reglas y fuente:** el enunciado dice "en más de" 25 % y 50 %; leído literalmente, un valor exactamente en ese límite queda OPCIONADA. Conviene confirmarlo con el profesor.

### CU-12 Consultar cotizaciones opcionadas

**Actor:** director de compras. **Historia:** HU-17. **Ventana:** V20.

**Flujo principal:**

1. El director abre el listado exclusivo de cotizaciones OPCIONADAS: número, proveedor, fecha, valor total ofertado, valor estándar del Ministerio y porcentaje de diferencia.
2. Ordena por valor total o por porcentaje de diferencia.
3. Compara dos o más cotizaciones del mismo producto en una vista paralela y abre el detalle de cada una.
4. Selecciona una cotización y pasa a generar la orden (CU-13).

**Flujos alternos:**

- Sin opcionadas: "No hay cotizaciones opcionadas disponibles".

### CU-13 Generar orden de compra

**Actor:** director de compras. **Actores secundarios:** proveedor, servidor de correo certificado. **Historia:** HU-14. **Ventanas:** V21, V22, V23.

**Precondición:** la cotización elegida está en estado OPCIONADA.

**Flujo principal:**

1. El director confirma "Generar orden de compra" (V21).
2. El sistema crea la orden con los mismos datos de la cotización (proveedor, productos, cantidades, unidades y valores) y un número consecutivo único (V22).
3. El sistema genera el PDF de la orden y lo envía por correo certificado al proveedor.
4. La cotización cambia a un estado que impide generar una segunda orden sobre ella.
5. El director puede consultar las órdenes generadas (V23).

**Flujos alternos:**

- Cotización en otro estado: "Solo se pueden generar órdenes de compra sobre cotizaciones opcionadas".

**Postcondición:** orden creada; la generación queda en la auditoría.

**Reglas y fuente:** el estado del paso 4 es CON\_ORDEN en el diseño y en el código del 8 de noviembre (brecha B10, resuelta en el diseño). Los estados de la orden (EN\_PROCESO y FINALIZADA) salen del diagrama, no del enunciado (B13).

## Especificación: dirección comercial, administrador y sistemas externos

Estos cinco casos no tienen mockup. Se escriben solo con lo que dicen el enunciado, las historias y el informe de arquitectura; lo que ninguna fuente define queda marcado como "por definir".

### CU-14 Consultar tablero BAM

**Actor:** director comercial. **Historia:** HU-18. **Ventana:** sin mockup (B8).

**Flujo principal:**

1. El director comercial abre el tablero de control.
2. El sistema muestra tres indicadores: el producto con mayor demanda en el último mes, el restaurante o sucursal con mayor demanda en el último mes y el proveedor con los mejores precios en el último año.
3. Cada indicador aparece con su valor numérico y una representación gráfica.
4. El director filtra los indicadores por rango de fechas y por sucursal.

**Flujos alternos:**

- Datos insuficientes: "No hay información suficiente para el periodo seleccionado".

**Reglas y fuente:** los datos se actualizan automáticamente al menos una vez al día; el tablero es responsive, funciona en móviles y carga en menos de 5 segundos. El enunciado fija los periodos de los indicadores (último mes, último año) y la historia agrega un filtro por rango de fechas: hay que decidir cómo conviven (brecha B17).

### CU-15 Parametrizar el sistema

**Actor:** administrador. **Historia propuesta:** HU-20. **Requisito:** RF-11.

**Flujo principal:**

1. El administrador abre la parametrización y elige el tipo de dato que va a alimentar.
2. Crea, edita o inactiva el dato.
3. El sistema valida y guarda el cambio, y lo registra en la auditoría.

**Datos paramétricos que las fuentes mencionan:** categorías de producto, productos por categoría, unidades de medida y marcas (listas desplegables de RF-01 y RF-03); mesas por sucursal (HU-03); umbral de inventario, hoy 25 % (HU-16); imagen corporativa, es decir tema y logos (requisito de interfaz 1). El equipo decidió (DEC-10) que el administrador da de alta a los usuarios con POST /api/usuarios, sin ventana; falta confirmar si roles y permisos también se administran aquí, porque el enunciado exige que existan pero no dice quién los mantiene.

### CU-16 Consultar auditoría

**Actor:** administrador. **Historia propuesta:** HU-21. **Requisito:** RF-11.

**Flujo principal:**

1. El administrador abre la auditoría y elige un rango de fechas.
2. El sistema lista los movimientos con acción, usuario, fecha y hora.

**Reglas y fuente:** cada acción CRUD del sistema debe registrar esos cuatro datos (requisito de mantenimiento 1). El informe asigna la consulta por rango de fechas a ms-administracion. Si se puede filtrar también por usuario o acción: por definir.

### CU-17 Consultar logs de errores

**Actor:** administrador. **Historia propuesta:** HU-22. **Requisito:** RF-11.

**Flujo principal:**

1. El administrador abre los logs de error y, si quiere, elige un servicio.
2. El sistema lista los errores con servicio, mensaje, fecha y hora.

**Reglas y fuente:** cada error producido en el sistema debe quedar registrado en un log (requisito de mantenimiento 2). Los campos de la lista salen de la clase LogError del `.drawio`.

### CU-18 Consultar precio sugerido (SOAP)

**Actor:** sistemas externos.

**Flujo principal:**

1. Un sistema externo invoca la operación `consultarPrecioSugerido` con el identificador de un producto, por SOAP.
2. El sistema responde con el precio estándar del producto.

**Reglas y fuente:** el informe de arquitectura publica este servicio con su WSDL en ms-integracion, y lo sirve desde una caché delante del servicio del Ministerio. Cómo se autentican los consumidores externos no está definido en ninguna fuente: por definir.

## Historias propuestas para el administrador

El Story Mapping no tiene historias para el administrador, y el enunciado lo exige. Estas tres siguen el mismo formato (HU-20 a HU-22) y **son una propuesta del documento, no del equipo**: cada criterio cita la fuente de donde sale, y lo que ninguna fuente define queda fuera o marcado. El equipo debe aprobarlas, ajustarlas o descartarlas, y estimarlas con planning poker.

| Historia | Como | Quiero | Para | Criterios de aceptación (con su fuente) |
| --- | --- | --- | --- | --- |
| HU-20 Alimentar la parametrización | Administrador del sistema | Alimentar la información de parametrización | Mantener listas, umbrales e imagen corporativa sin depender de un experto en diseño ni de un nuevo despliegue | (1) Puedo crear, consultar, editar e inactivar categorías de producto, productos por categoría, unidades de medida, marcas y mesas por sucursal (enunciado y HU-03, HU-05). (2) Puedo cambiar el tema y los logos sin redespliegue (requisito de interfaz 1). (3) Puedo consultar y modificar el umbral de inventario, hoy 25 % (HU-16). (4) Cada acción queda en la auditoría con acción, usuario, fecha y hora (mantenimiento 1). (5) Solo un usuario con rol administrador accede (seguridad 1). (6) Un error al guardar queda en el log (mantenimiento 2) |
| HU-21 Consultar y monitorear la auditoría | Administrador del sistema | Consultar los movimientos de auditoría | Verificar quién hizo cada acción y cuándo | (1) La lista muestra acción, usuario, fecha y hora (mantenimiento 1). (2) Puedo filtrar por rango de fechas (informe de arquitectura, ms-administracion). (3) La consulta es de solo lectura. (4) Solo el administrador accede (seguridad 1) |
| HU-22 Consultar logs de errores | Administrador del sistema | Consultar los errores registrados | Diagnosticar fallas del sistema | (1) La lista muestra servicio, mensaje, fecha y hora (clase LogError del `.drawio`). (2) Puedo consultar los errores de un servicio (informe de arquitectura). (3) Cada error producido en el sistema aparece en el log (mantenimiento 2). (4) La consulta es de solo lectura y solo el administrador accede (seguridad 1) |

Quedan por definir, y no se inventaron: los mensajes de error de cada pantalla, si se puede filtrar la auditoría por usuario o acción, y si usuarios, roles y permisos se administran desde HU-20.
