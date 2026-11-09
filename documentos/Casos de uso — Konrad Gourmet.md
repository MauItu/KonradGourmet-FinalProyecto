# Casos de uso — Konrad Gourmet

Carlos Franco · versión corregida del 8 de noviembre de 2026

Este documento fija 19 casos de uso, cada uno amarrado a una historia de usuario, a un requisito del enunciado y a sus ventanas. Los criterios salen de las historias del Story Mapping; lo que el equipo no definió queda marcado como propuesta.

## Dónde implementar cada caso de uso

Este cuadro asigna cada caso de uso a su adaptador de entrada (controlador REST, listener o tarea programada), al método del puerto de entrada que lo atiende y a los eventos que publica o consume. Los nombres son los del diagrama de clases (páginas 4a a 4e del `.drawio`) y del paquete `co.edu.konradlorenz.<servicio>`.

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
| CU-15 | `AdminRestController.parametrizar` y `tema` (administracion); `DestinatarioRestController` (notificaciones) | `AdministracionUseCase.parametrizar`, `tema`; `NotificarUseCase.registrarDestinatario`, `destinatarios` | Auditoría |
| CU-16 | `AdminRestController.auditoria` y `AuditoriaListener` | `AdministracionUseCase.auditoria`, `registrarAuditoria` | Consume `auditoria.evento` |
| CU-17 | `AdminRestController.logs` | `AdministracionUseCase.logs` | — |
| CU-18 | `AbastecimientoSoapEndpoint.consultarPrecioSugerido` (integracion) | `ConsultarPrecioUseCase.precioSugerido(productoId)` | — |
| CU-19 | `UsuarioRestController.registrar` (identidad) | `RegistrarUsuarioUseCase.registrar(nombre, correo, rol, sucursalId, clave)` | Auditoría |

**Decisiones del equipo que afectan a estos casos.** Ya están resueltas: el pedido guarda la sucursal (por la mesa) y el mesero que lo tomó (CU-02, DEC-51); el descuento diario de CU-05 corre en una sola réplica de ms-inventario, aunque haya de 2 a 6, porque el job lleva `@SchedulerLock` (ShedLock, DEC-36); el precio del Ministerio siempre pasa por la caché, porque el Proxy lleva `@Primary` (CU-08 y CU-18, DEC-35); `cotizacionesAplicadas` sirve para que la misma cotización no se aplique dos veces a una solicitud (CU-08); y el umbral del 25 % no tiene ruta, por diseño (CU-05, DEC-43). En el diseño, la alerta es una marca del insumo (`alertaAtendida`), no una entidad propia (CU-05). Las categorías del proveedor se guardan como texto (`List<String>`): son los nombres de las categorías de ms-menu, copiados por valor al registrar el proveedor, sin validarlos contra ms-menu (CU-09, PD-02).

## Actores

Hay seis roles que usan el sistema y cinco actores externos. Los seis roles salen del diagrama de contexto C4 del informe de arquitectura (página 2 del `.drawio`); los externos, del mismo diagrama y del enunciado.

| Actor | Tipo | Qué hace en el sistema | Fuente |
| --- | --- | --- | --- |
| Mesero | Rol principal | Registra los pedidos de cada mesa | RF-02; HU-01 a HU-03 |
| Jefe de cocina | Rol principal | Registra platos, solicita alimentos, recibe alertas de inventario y revisa promociones | RF-01, RF-03, RF-04, RF-10; HU-04, HU-05, HU-10, HU-16, HU-19 |
| Auxiliar de compras | Rol principal | Consulta solicitudes, envía solicitudes de cotización y mantiene proveedores | RF-05, RF-06, RF-12; HU-06 a HU-09, HU-12, HU-15 |
| Director de compras | Rol principal | Registra valores, valida cotizaciones y genera órdenes de compra | RF-07, RF-08; HU-11, HU-13, HU-14, HU-17 |
| Director comercial | Rol principal | Consulta el tablero BAM | RF-09; HU-18 |
| Administrador | Rol principal | Parametriza, registra usuarios y destinatarios, consulta la auditoría y los logs de error | RF-11 |
| Proveedor | Externo (persona) | Recibe el PDF de la solicitud de cotización y de la orden; responde por correo, fuera del sistema | [Reporte, §17, p. 52](Reporte_Arquitectura_Konrad_Gourmet_v2.pdf#page=52), supuesto S2: el proveedor responde por correo y el director digita los valores; HU-12, HU-14 |
| Ministerio de Comercio | Externo (sistema) | Publica un servicio web con el precio estándar por producto | RF-06; HU-12 |
| Servidor de correo certificado | Externo (sistema) | Entrega los correos con certificación y estampado cronológico | Requisito de seguridad 5; HU-12, HU-14 |
| Redes sociales | Externo (sistema) | Aportan señales de tendencia de productos | RF-10; HU-19 |
| Sistemas externos | Externo (sistema) | Consumen los servicios que el restaurante expone por SOAP | Requisito de integración |

### Referencias de requisitos

RF-01 a RF-12 son los identificadores usados en este documento; la tabla siguiente define su significado sin depender de una matriz de trazabilidad externa. No son identificadores impresos en el reporte. El enunciado original no está en las carpetas disponibles, por lo que las descripciones conservan el uso que este archivo da a cada código y se contrastan con las fuentes locales citadas. El [§3 del reporte, p. 6](Reporte_Arquitectura_Konrad_Gourmet_v2.pdf#page=6), trata los requisitos **no funcionales** (seguridad, parametrización de imagen corporativa, auditoría, logs e integración); las funciones se describen principalmente en §5 y §10. Las decisiones enlazadas precisan el diseño del reporte.

| Código | Función que identifica | Casos de uso | Fuente verificable |
| --- | --- | --- | --- |
| RF-01 | Registrar platos con sus ingredientes y recetas | CU-03 | [Reporte, §10, tabla 8, p. 26: jefe · platos](Reporte_Arquitectura_Konrad_Gourmet_v2.pdf#page=26) |
| RF-02 | Registrar y consultar pedidos por mesa | CU-02 | [Reporte, §10, tabla 8, p. 26: mesero · pedidos](Reporte_Arquitectura_Konrad_Gourmet_v2.pdf#page=26) |
| RF-03 | Registrar solicitudes de alimentos y confirmar por correo | CU-04 | [Reporte, §10.1, tabla 9, p. 28: pasos 1 y 2](Reporte_Arquitectura_Konrad_Gourmet_v2.pdf#page=28) |
| RF-04 | Descontar el consumo diario y alertar por inventario bajo | CU-05 | [Reporte, §10, tabla 8, p. 26: inventario y alarmas](Reporte_Arquitectura_Konrad_Gourmet_v2.pdf#page=26); [DEC-43 y DEC-46](DECISIONES.md) precisan el umbral y su cruce |
| RF-05 | Consultar y filtrar solicitudes de compras y ver su detalle | CU-07 | [Reporte, §10.1, tabla 9, p. 28: paso 3](Reporte_Arquitectura_Konrad_Gourmet_v2.pdf#page=28) |
| RF-06 | Generar y enviar cotizaciones con precios estándar del Ministerio | CU-08 | [Reporte, §10.1, tabla 9, p. 28: pasos 4 a 6](Reporte_Arquitectura_Konrad_Gourmet_v2.pdf#page=28) |
| RF-07 | Registrar valores del proveedor y validar cotizaciones | CU-10, CU-11 | [Reporte, §10.1, tabla 9, pp. 28 y 29: pasos 7 a 9](Reporte_Arquitectura_Konrad_Gourmet_v2.pdf#page=28); [DEC-43](DECISIONES.md) precisa los límites |
| RF-08 | Consultar cotizaciones opcionadas y generar órdenes de compra | CU-12, CU-13 | [Reporte, §10.1, tabla 9, p. 29: pasos 11 a 13](Reporte_Arquitectura_Konrad_Gourmet_v2.pdf#page=29) |
| RF-09 | Consultar indicadores en el tablero BAM | CU-14 | [Reporte, §10, tabla 8, p. 27: director comercial · BAM](Reporte_Arquitectura_Konrad_Gourmet_v2.pdf#page=27) |
| RF-10 | Detectar tendencias y sugerir promociones | CU-06 | [Reporte, §5, tabla 5, p. 12: ms-analitica](Reporte_Arquitectura_Konrad_Gourmet_v2.pdf#page=12); [DEC-07](DECISIONES.md), HU-19 |
| RF-11 | Administrar parámetros, usuarios y destinatarios; consultar auditoría y logs | CU-15, CU-16, CU-17, CU-19 | [Reporte, §3, p. 6](Reporte_Arquitectura_Konrad_Gourmet_v2.pdf#page=6) y [§5, tabla 5, p. 12](Reporte_Arquitectura_Konrad_Gourmet_v2.pdf#page=12); [DEC-10 y DEC-13](DECISIONES.md) fijan usuarios y destinatarios |
| RF-12 | Mantener los datos de proveedores | CU-09 | [DEC-07](DECISIONES.md), HU-15; [Reporte, §5, tabla 5, p. 12: ms-cotizaciones · proveedores](Reporte_Arquitectura_Konrad_Gourmet_v2.pdf#page=12) |

Las referencias DEC remiten a [DECISIONES.md](DECISIONES.md); las HU son las historias del Story Mapping y V01–V23 son las ventanas de [DISEÑO UXUI.pdf](DISE%C3%91O%20UXUI.pdf). Los supuestos S2 y S3 se definen junto a su uso y remiten al §17 del reporte.

## Diagrama de casos de uso

![Diagrama de casos de uso](casos_de_uso.png)

> **Pendiente:** el diagrama UML lo tiene quien escribió este documento. Falta exportarlo a `casos_de_uso.png`, en la misma carpeta que este archivo, con los casos de este documento (incluido CU-19, Registrar usuario), los 6 roles y los 5 actores externos.

Los roles de cocina y compras están a la izquierda y los roles de dirección, administración y los actores externos a la derecha. Los correos de CU-04 y CU-05 también salen por el servidor de correo certificado; esa línea no se dibuja para no cruzar las otras.

## Catálogo de casos de uso

Trece casos agrupan las 19 historias de usuario; los seis restantes son el inicio de sesión, el alta de usuarios, los otros tres del administrador y el servicio SOAP. El MVP es el del Story Mapping.

| CU | Nombre | Actor principal | Actores secundarios | Historias | Requisito | Ventanas | MVP |
| --- | --- | --- | --- | --- | --- | --- | --- |
| CU-01 | Iniciar sesión | Los seis roles | — | Ninguna (requisitos de seguridad) | — | V01 | — |
| CU-02 | Registrar pedido | Mesero | — | HU-01, HU-02, HU-03 | RF-02 | V02, V03, V04 | 1 |
| CU-03 | Registrar plato con ingredientes | Jefe de cocina | — | HU-04, HU-05 | RF-01 | V06 | 1 |
| CU-04 | Registrar solicitud de alimentos | Jefe de cocina | Servidor de correo certificado | HU-10 | RF-03 | V08, V09, V10 | 2 |
| CU-05 | Recibir alerta de inventario bajo | Jefe de cocina | Servidor de correo certificado | HU-16 | RF-04 | V05, V07 | 4 |
| CU-06 | Revisar promociones sugeridas | Jefe de cocina | Redes sociales, servidor de correo certificado | HU-19 | RF-10 | Sin mockup | 4 |
| CU-07 | Consultar solicitudes de compras | Auxiliar de compras | — | HU-06, HU-07, HU-08 | RF-05 | V11, V12 | 2 |
| CU-08 | Enviar solicitud de cotización | Auxiliar de compras | Proveedor, Ministerio de Comercio, servidor de correo certificado | HU-09, HU-12 | RF-06 | V13, V14, V15 | 2 y 3 |
| CU-09 | Registrar datos de proveedor | Auxiliar de compras | — | HU-15 | RF-12 | Sin ventana | 3 |
| CU-10 | Registrar valores de cotización | Director de compras | — | HU-11 | RF-07 | V18 | 2 |
| CU-11 | Validar cotización | Director de compras | — | HU-13 | RF-07 | V16, V17, V19 | 3 |
| CU-12 | Consultar cotizaciones opcionadas | Director de compras | — | HU-17 | RF-08 | V20 | 4 |
| CU-13 | Generar orden de compra | Director de compras | Proveedor, servidor de correo certificado | HU-14 | RF-08 | V21, V22, V23 | 3 |
| CU-14 | Consultar tablero BAM | Director comercial | — | HU-18 | RF-09 | Sin mockup | 4 |
| CU-15 | Parametrizar el sistema | Administrador | — | HU-20 (propuesta) | RF-11 | Sin mockup | — |
| CU-16 | Consultar auditoría | Administrador | — | HU-21 (propuesta) | RF-11 | Sin mockup | — |
| CU-17 | Consultar logs de errores | Administrador | — | HU-22 (propuesta) | RF-11 | Sin mockup | — |
| CU-18 | Consultar precio sugerido (SOAP) | Sistemas externos | — | Ninguna (requisito de integración) | — | Sin ventana | — |
| CU-19 | Registrar usuario | Administrador | — | Ninguna (DEC-10) | RF-11 | Sin ventana | — |

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
7. El sistema asocia el pedido a la sucursal de la mesa (`Mesa.sucursalId`) y al mesero que lo tomó (`Pedido.meseroId`, que sale del token; DEC-51), registra fecha y hora, genera el consecutivo y lo muestra (V04).

**Flujos alternos:**

- Sin ningún plato: el sistema no permite guardar.
- Cantidad no numérica, negativa o cero: "La cantidad debe ser un número entero mayor a cero".
- Mesa vacía: "Debe seleccionar una mesa para continuar".
- El mesero puede modificar la cantidad antes de confirmar.

**Postcondición:** pedido registrado con su consecutivo; la mesa queda visible en el detalle del pedido y en el número consecutivo generado; el movimiento queda en la auditoría (acción, usuario, fecha, hora).

**Reglas y fuente:** el valor total sale del precio de venta de cada plato. Se pueden consultar los pedidos activos filtrando por mesa (HU-03). El pedido guarda qué mesero lo tomó (DEC-51): sin mesero no se registra, y la ventana V02 (`pedidosDelTurno`) muestra solo los suyos. Según el informe, el pedido publica el evento `pedido.registrado`, que alimenta el descuento de inventario y los KPI.

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

**Actor:** jefe de cocina. **Actor secundario:** servidor de correo certificado. **Historia:** HU-10. **Ventanas:** V08, V09, V10.

**Precondición:** sesión iniciada; el jefe no tiene stock disponible de uno o más productos.

**Flujo principal:**

1. El jefe abre "Nueva solicitud de alimentos" (V08). La sucursal y el solicitante aparecen automáticamente.
2. Agrega uno o varios productos: categoría (obligatoria), producto dependiente de la categoría (obligatorio), cantidad (mayor a cero, obligatoria), marca (lista opcional) y unidad (obligatoria).
3. Envía la solicitud. El sistema verifica el formato de todos los datos.
4. Si todo es correcto, la solicitud queda en estado PENDIENTE, el sistema muestra su número (V09) y envía un correo de confirmación al solicitante.

**Flujos alternos:**

- Algún dato inválido: el sistema indica el campo con error y no registra la solicitud.
- Consultar mis solicitudes (V10, `GET /api/solicitudes?solicitante=yo`): el jefe ve las suyas y su estado.

**Postcondición:** solicitud PENDIENTE con número y correo enviado; la alerta de stock de esos productos queda atendida cuando ms-inventario consume `solicitud.registrada` (DEC-18).

**Reglas y fuente:** el correo debe ser certificado y con estampado cronológico (requisito de seguridad 5).

### CU-05 Recibir alerta de inventario bajo

**Actor:** jefe de cocina. **Actor secundario:** servidor de correo certificado. **Historia:** HU-16. **Ventanas:** V05, V07.

**Disparador:** tarea diaria del sistema (el `.drawio` la programa a las 23:59).

**Flujo principal:**

1. El sistema verifica las órdenes preparadas o vendidas del día y descuenta las cantidades correspondientes del inventario.
2. Cuando el porcentaje de un producto frente a su stock de referencia, redondeado a dos decimales, queda por debajo del 25 %, el sistema genera una alarma visible en la aplicación y envía un correo avisando que debe hacerse una solicitud de alimentos. La alarma sale una sola vez, la noche en que el producto cruza el umbral (DEC-43, DEC-46).
3. La alerta indica producto, cantidad restante, unidad y sucursal, y da acceso directo al formulario de solicitud de alimentos (CU-04).

**Flujos alternos:**

- Si el proceso diario falla, el error queda registrado en el log del sistema.

**Postcondición:** la alerta permanece activa hasta que se registre una solicitud para ese producto.

**Reglas y fuente:** el proceso no debe afectar el desempeño en horario de operación. El 25 % es un parámetro propio de ms-inventario (`UmbralStock`); no se cambia desde la parametrización (DEC-43). El modelo no tiene una entidad Alerta; en el código del 8 de noviembre la alerta es la marca `alertaAtendida` del insumo.

### CU-06 Revisar promociones sugeridas

**Actor:** jefe de cocina. **Actores secundarios:** redes sociales, servidor de correo certificado. **Historia:** HU-19. **Ventana:** sin mockup.

**Disparador:** análisis automático de tendencias.

**Flujo principal:**

1. El sistema identifica tendencias a partir del comportamiento en redes sociales y los productos más solicitados.
2. Envía al jefe un correo con las promociones o productos destacados.
3. El jefe consulta dentro de la aplicación el histórico de promociones sugeridas. Cada sugerencia indica el producto, el motivo y el periodo de vigencia.
4. El jefe marca una sugerencia como aceptada o descartada.

**Flujos alternos:**

- Si el análisis de tendencias falla, el error queda en el log sin interrumpir el resto del sistema.

**Reglas y fuente:** no se envía más de una notificación de promociones por día. El análisis corre cada semana sobre los 7 días anteriores: productos en tendencia del adaptador de redes y los 3 más pedidos de la semana; cada sugerencia vale 7 días (PD-04). La red social concreta y los valores 7 y 3 son supuestos del diseño (S11): ni la historia ni las fuentes locales los fijan.

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

**Reglas y fuente:** la consulta responde en menos de 3 segundos y el detalle no permite modificar datos. Los filtros (rango de fechas, producto, marca y estado) están en la tarjeta HU-07 corregida de `STORY_MAPPING_CORREGIDO.md` (S-01); la original repetía los criterios de HU-06. Un rango invertido muestra «La fecha inicial no puede ser posterior a la fecha final.» (PD-10).

### CU-08 Enviar solicitud de cotización

**Actor:** auxiliar de compras. **Actores secundarios:** proveedor, Ministerio de Comercio, servidor de correo certificado. **Historias:** HU-09, HU-12. **Ventanas:** V13, V14, V15.

**Precondición:** existen solicitudes PENDIENTES y al menos un proveedor activo.

**Flujo principal:**

1. El auxiliar selecciona una o varias solicitudes del listado.
2. Elige el proveedor en una lista desplegable (obligatorio) y registra la fecha máxima de plazo (obligatoria, no anterior a la fecha actual).
3. El sistema muestra un resumen de productos y cantidades antes de confirmar.
4. El auxiliar acepta. El sistema genera el número consecutivo y consulta el precio estándar de cada producto (a través de la caché del Proxy).
5. Genera el PDF con productos, cantidades, unidades y fecha máxima de plazo, lo guarda en el almacén de objetos y deja la cotización ENVIADA (V14).
6. Envía el PDF por correo certificado y con estampado cronológico al proveedor.
7. Por cada solicitud incluida, registra el valor total sugerido y, si todos sus productos ya tienen cotización, la pasa a EN TRÁMITE y avisa por correo a quien la creó.

**Flujos alternos:**

- Ninguna solicitud seleccionada: "Debe seleccionar al menos una solicitud".
- Consultar cotizaciones enviadas (V15, `GET /api/cotizaciones`).
- El Ministerio no responde, o falla el PDF: la cotización queda en PENDIENTE_PRECIO, el error queda en el log y en la auditoría, y el auxiliar puede reenviarla (`POST /api/cotizaciones/{id}/reenvio`).

**Postcondición:** si el envío se completa, cotización ENVIADA con su PDF; al consumir `cotizacion.enviada`, cada solicitud registra el valor sugerido y pasa a EN TRÁMITE si todos sus productos ya tienen cotización. Si falla el precio o el PDF, la cotización queda PENDIENTE_PRECIO, sin publicar `cotizacion.enviada`; ese intento no actualiza los valores ni el estado de las solicitudes.

**Reglas y fuente:** HU-09 cubre la captura (pasos 1 a 3) y HU-12 el envío (pasos 4 a 7); ambas usan las mismas ventanas. El informe de arquitectura guarda en caché el precio del Ministerio ([§17, p. 52](Reporte_Arquitectura_Konrad_Gourmet_v2.pdf#page=52), supuesto S3: cambia como máximo una vez al día).

### CU-09 Registrar datos de proveedor

**Actor:** auxiliar de compras. **Historia:** HU-15. **Ventana:** ninguna.

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

**Precondición:** existe una cotización ENVIADA y el proveedor respondió por correo, fuera del sistema ([§17, p. 52](Reporte_Arquitectura_Konrad_Gourmet_v2.pdf#page=52), supuesto S2: el proveedor responde por correo y el director digita los valores).

**Flujo principal:**

1. El director busca y selecciona la cotización generada por el sistema.
2. Ingresa el valor unitario ofertado por cada producto (solo numéricos mayores a cero).
3. El sistema calcula el valor total de la cotización.
4. El director guarda, total o parcialmente, y puede continuar después.

**Flujos alternos:**

- Cotización ya validada: el sistema no permite modificar los valores.

**Postcondición:** valores registrados; cada registro o modificación queda en la auditoría.

### CU-11 Validar cotización

**Actor:** director de compras. **Historia:** HU-13. **Ventanas:** V16, V17, V19.

**Precondición:** la cotización está en estado ENVIADA.

**Flujo principal:**

1. El director selecciona "Validar" sobre la cotización.
2. El sistema compara el valor cotizado con el valor estándar del Ministerio. Si lo supera en más de 25 %, queda RECHAZADA; si está por debajo en más de 50 %, queda SOSPECHOSA; si está dentro de ese rango, queda OPCIONADA.
3. El sistema muestra el resultado con el valor estándar, el valor cotizado y el porcentaje de diferencia (el mockup V19 muestra $520.000, $545.000, +4,81 % y OPCIONADA).

**Flujos alternos:**

- Sin valores registrados: "La cotización no tiene valores registrados para validar".
- Ver pendientes de validar (V16, `GET /api/cotizaciones/pendientes`): la pantalla de inicio del director.

**Postcondición:** el cambio de estado queda en la auditoría (acción, usuario, fecha, hora).

**Reglas y fuente:** el enunciado dice "en más de" 25 % y 50 %. Los porcentajes se redondean a dos decimales antes de comparar: +25,00 % y −50,00 % quedan OPCIONADA, +25,01 % queda RECHAZADA y −50,01 % queda SOSPECHOSA (DEC-43).

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
- La cotización ya tiene orden: "Ya existe una orden de compra para la cotización COT-018".

**Postcondición:** orden EN PROCESO; la cotización queda CON ORDEN y sale de V20; cada solicitud incluida pasa a COMPLETADA, aunque la cotización la haya cubierto solo en parte (DEC-46); la generación queda en la auditoría.

**Reglas y fuente:** el estado del paso 4 es CON\_ORDEN en el diseño y en el código del 8 de noviembre. Los estados de la orden (EN\_PROCESO y FINALIZADA) salen del diagrama, no del enunciado.

## Especificación: dirección comercial, administrador y sistemas externos

Estos seis casos no tienen mockup. Se escriben solo con lo que dicen el enunciado, las historias y el informe de arquitectura; lo que ninguna fuente define queda marcado como "por definir".

### CU-14 Consultar tablero BAM

**Actor:** director comercial. **Historia:** HU-18. **Ventana:** sin mockup.

**Flujo principal:**

1. El director comercial abre el tablero de control.
2. El sistema muestra tres indicadores: el producto con mayor demanda en el último mes, el restaurante o sucursal con mayor demanda en el último mes y el proveedor con los mejores precios en el último año.
3. Cada indicador aparece con su valor numérico y una representación gráfica.
4. El director filtra los indicadores por rango de fechas y por sucursal.

**Flujos alternos:**

- Datos insuficientes: "No hay información suficiente para el periodo seleccionado".

**Reglas y fuente:** los datos se actualizan automáticamente al menos una vez al día; el tablero es responsive, funciona en móviles y carga en menos de 5 segundos. El enunciado fija los periodos de los indicadores (último mes, último año) y la historia agrega un filtro por rango de fechas. Conviven así (PD-03): sin rango, la demanda (producto y sucursal) usa los últimos 30 días y el proveedor con mejores precios los últimos 365; con rango, los tres usan ese rango y la pantalla muestra el periodo aplicado. La sucursal filtra solo la demanda. «Mejores precios» es la menor diferencia promedio contra el precio estándar, solo con cotizaciones OPCIONADAS. Sin hechos: «No hay información suficiente para el periodo seleccionado». Un rango invertido muestra «La fecha inicial no puede ser posterior a la fecha final.»

### CU-15 Parametrizar el sistema

**Actor:** administrador. **Historia propuesta:** HU-20. **Requisito:** RF-11.

**Flujo principal:**

1. El administrador abre la parametrización y elige qué va a cambiar: un parámetro general o la imagen corporativa (tema y logos).
2. Cambia el valor.
3. El sistema valida y guarda el cambio, y lo registra en la auditoría. Mensajes: «La clave del parámetro es obligatoria.», «El valor del parámetro {clave} es obligatorio.» y «Parámetro actualizado.» (PD-10).
4. Registra los correos de los jefes de cocina por sucursal (`/api/notificaciones/destinatarios`, DEC-13). Un correo mal formado muestra «El formato del correo electrónico no es válido.» Este registro también se audita (`auditoria.evento`).

**Datos paramétricos que las fuentes mencionan:** categorías de producto, productos por categoría, unidades de medida y marcas (listas desplegables de RF-01 y RF-03); mesas por sucursal (HU-03); imagen corporativa, es decir tema y logos (requisito de interfaz 1). El alta de usuarios es CU-19 (DEC-10).

**Roles y permisos (PD-06):** los seis roles son fijos (un rol nuevo es un cambio de código) y los permisos son una matriz fija de rol a rutas: el gateway valida el JWT, cada servicio verifica el rol en sus endpoints y el menú lateral sale del rol. El administrador tiene alcance global: registra usuarios con un rol y una sucursal (CU-19), mantiene parámetros, tema, logos y destinatarios, y consulta auditoría y logs. Cambiar el rol o desactivar un usuario queda fuera de esta versión. **Supuesto S10:** las listas maestras (categorías, productos, unidades, marcas y mesas) y el primer administrador se cargan con la migración inicial del servicio dueño de cada una; no se mantienen desde esta pantalla.

### CU-16 Consultar auditoría

**Actor:** administrador. **Historia propuesta:** HU-21. **Requisito:** RF-11.

**Flujo principal:**

1. El administrador abre la auditoría y elige un rango de fechas.
2. El sistema lista los movimientos con acción, usuario, fecha y hora.

**Reglas y fuente:** cada acción CRUD del sistema debe registrar esos cuatro datos (requisito de mantenimiento 1). El informe asigna la consulta por rango de fechas a ms-administracion. El usuario de cada registro sale del JWT de quien hizo la acción. Filtros: solo por rango de fechas, obligatorio, y «desde» no puede ser posterior a «hasta» (PD-07); filtrar por usuario o acción es una evolución que cambia cinco firmas de la 4e. Mensajes: «La fecha inicial no puede ser posterior a la fecha final.» y «No hay movimientos de auditoría en el periodo seleccionado.» (PD-10).

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

**Reglas y fuente:** el informe de arquitectura publica este servicio con su WSDL en ms-integracion, y lo sirve desde una caché delante del servicio del Ministerio. Los consumidores externos se autentican con OAuth 2.0 client credentials (PD-08): cada sistema se registra como cliente en ms-identidad y obtiene el token en `POST /api/auth/token`; el gateway lo valida como cualquier JWT y solo permite `/api/integracion/**` a ese tipo de cliente; el `client_id` queda como usuario en la auditoría, y todo va por TLS 1.3. El registro de clientes externos es el supuesto S12, porque la 4a no lo dibuja. Si no hay precio, responde un SOAP Fault: «No hay precio estándar disponible para el producto {id}.»; un token inválido recibe 401 en el gateway.

### CU-19 Registrar usuario

**Actor:** administrador. **Decisión:** DEC-10. **Requisito:** RF-11. **Ventana:** ninguna (`POST /api/usuarios`, ms-identidad).

**Flujo principal:**

1. El administrador envía nombre, correo, rol, sucursal y contraseña.
2. El sistema valida que la contraseña tenga 8 o más caracteres, con mayúscula, minúscula y número (`PoliticaContrasena`).
3. La cifra (`CifradorPort.cifrar`), registra al usuario y deja la auditoría.

**Flujos alternos:**

- Si la contraseña no cumple la regla, el usuario no se registra.

**Reglas y fuente:** los requisitos de seguridad 3 y 4 del enunciado (contraseña y cifrado); la relación «Parametriza, registra usuarios y destinatarios» de la página 2 del `.drawio`.

## Historias propuestas para el administrador

El Story Mapping no tiene historias para el administrador. Estas tres siguen el mismo formato (HU-20 a HU-22). **Estado: propuesta de diseño aprobada por el arquitecto-revisor de Claude, con ajustes y sin estimar (PD-09); no forma parte del Story Mapping original.** Por decisión de Mauricio quedan **fuera del plan de sprints**: no se estiman ni se asignan a un sprint, y el plan de 5 sprints y 10 semanas no cambia (DEC-47). Son historias identificadas para una versión posterior; la arquitectura ya las soporta.

| Historia | Como | Quiero | Para | Criterios de aceptación (con su fuente) |
| --- | --- | --- | --- | --- |
| HU-20 Parametrizar el sistema | Administrador del sistema | Mantener los parámetros generales, la imagen corporativa y los destinatarios | Cambiarlos sin depender de un experto en diseño ni de un nuevo despliegue | (1) Puedo consultar y cambiar los parámetros generales y la imagen corporativa (tema y logos); la SPA los toma al iniciar, sin redespliegue (requisito de interfaz 1). (2) Puedo registrar y consultar los correos de los jefes de cocina por sucursal (DEC-13). (3) Las validaciones y mensajes son los de PD-10. (4) Cada cambio queda en la auditoría con acción, usuario, fecha y hora (mantenimiento 1). (5) Solo un usuario con rol administrador accede (seguridad 1). (6) Un error al guardar queda en el log (mantenimiento 2). (7) Las listas maestras (categorías, productos, unidades, marcas y mesas) quedan fuera: se cargan con la migración inicial de cada servicio (supuesto S10, PD-06) |
| HU-21 Consultar la auditoría | Administrador del sistema | Consultar los movimientos de auditoría | Verificar quién hizo cada acción y cuándo | (1) La lista muestra acción, usuario, fecha y hora (mantenimiento 1). (2) El rango de fechas es obligatorio y «desde» no puede ser posterior a «hasta» (informe de arquitectura, ms-administracion; PD-07). (3) La consulta es de solo lectura y solo el administrador accede (seguridad 1). (4) Los mensajes son los de PD-10 |
| HU-22 Consultar logs de errores | Administrador del sistema | Consultar los errores registrados | Diagnosticar fallas del sistema | (1) La lista muestra servicio, mensaje, fecha y hora (clase LogError del `.drawio`). (2) El servicio es opcional; sin él, salen todos (informe de arquitectura). (3) La consulta es de solo lectura y solo el administrador accede (seguridad 1). (4) Los mensajes son los de PD-10 |

Lo que antes quedaba por definir ya está decidido: los mensajes (PD-10, abajo), los filtros de la auditoría (PD-07) y quién administra roles y permisos (PD-06). No se estiman: quedan fuera del plan de sprints (PD-09).

## Mensajes de interfaz (PD-10)

Criterio: el mismo estilo que las tarjetas del Story Mapping, en una frase completa, sin códigos técnicos, que diga qué pasó y qué hacer; el mismo texto para el mismo caso en todas las pantallas. Los mensajes literales de las historias siguen siendo el contrato de la interfaz; estos completan los que estaban por definir. Los mensajes de las excepciones del dominio son internos y el adaptador REST los traduce a estos textos (efecto futuro: hoy el código conserva sus textos, que comprueban las pruebas).

| Caso | Situación | Mensaje |
| --- | --- | --- |
| CU-01 (V01) | Credenciales inválidas | «Correo o contraseña incorrectos.» (no dice cuál falló) |
| CU-01 | Rol sin acceso a la opción | «No tiene permiso para acceder a esta opción.» |
| CU-08 (HU-12) | El Ministerio no responde | «No se pudo consultar el precio del Ministerio de Comercio. La cotización {número} quedó pendiente de precio; puede reenviarla desde Cotizaciones.» |
| CU-08 (HU-12) | Falla el correo | «No se pudo enviar el correo al proveedor. La cotización {número} está guardada; puede reintentar el envío.» |
| CU-10 (HU-11) | Cotización ya validada | «La cotización ya fue validada; sus valores no se pueden modificar.» |
| CU-10 (HU-11) | Valor no válido | «El valor unitario debe ser un número mayor a cero.» |
| CU-19 | Contraseña que no cumple la regla | «La contraseña debe tener al menos 8 caracteres, con una mayúscula, una minúscula y un número.» |
| CU-19 | Correo repetido | «Ya existe un usuario registrado con ese correo.» |
| CU-15, CU-19 (HU-20) | Correo mal formado | «El formato del correo electrónico no es válido.» (el de HU-15) |
| CU-15 (HU-20) | Clave o valor vacío | «La clave del parámetro es obligatoria.» / «El valor del parámetro {clave} es obligatorio.» |
| CU-15 (HU-20) | Guardado | «Parámetro actualizado.» |
| CU-07, CU-14, CU-06, CU-16 (HU-07, HU-18, HU-19, HU-21) | Rango de fechas invertido | «La fecha inicial no puede ser posterior a la fecha final.» |
| CU-16 (HU-21) | Sin resultados | «No hay movimientos de auditoría en el periodo seleccionado.» |
| CU-17 (HU-22) | Sin resultados | «No hay errores registrados para el servicio seleccionado.» |
| CU-06 (HU-19) | Sin sugerencias / ya resuelta | «No hay promociones sugeridas para el periodo seleccionado.» / «Esta sugerencia ya fue aceptada o descartada.» |
| CU-18 | Producto sin precio (SOAP Fault) | «No hay precio estándar disponible para el producto {id}.»; un token inválido recibe 401 en el gateway |
