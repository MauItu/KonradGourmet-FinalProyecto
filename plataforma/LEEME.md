# Plataforma local de Konrad Gourmet

El diagrama de contenedores (página 3a del `.drawio`) corriendo en un solo equipo con Docker Compose.
Solo necesita Docker: los servicios Java se compilan dentro de un contenedor de Maven.

```bash
./plataforma/levantar.sh                # compila los servicios y levanta todo (≈ 1 min la primera vez)
./plataforma/levantar.sh --sin-compilar # levanta con lo último compilado
./plataforma/bajar.sh                   # apaga todo y conserva los datos
./plataforma/bajar.sh --borrar          # apaga todo y borra bases, tópicos y PDF
```

| Dirección | Qué es |
|---|---|
| http://localhost:8080 | La aplicación (Nginx sirve la SPA y pasa `/api/**` al gateway) |
| http://localhost:8080/plataforma/ | Estado de los 11 servicios en Eureka y sus prefijos en el gateway |
| http://localhost:8761 | Eureka: qué instancias están registradas |
| http://localhost:8025 | Mailpit: los correos que envía ms-notificaciones, con sus adjuntos y cabeceras |

## Qué corre

| Contenedor | Imagen | Papel en la arquitectura |
|---|---|---|
| `web` | nginx 1.28 | Sirve la SPA en React + TypeScript (se compila desde `web/`); única entrada desde el navegador |
| `api-gateway` | Spring Cloud Gateway | Facade: valida el JWT y enruta los 13 prefijos a los 11 servicios |
| `service-registry` | Eureka | Registro de instancias vivas |
| `postgres` | PostgreSQL 17 | Una base y un usuario por servicio (`ms_pedidos`, `ms_cotizaciones`…); un usuario no puede conectarse a la base de otro |
| `kafka` | Kafka 3.9 (KRaft) | Broker de eventos con los 10 tópicos y la cola de errores de ms-notificaciones (`notificaciones.dlq`); la creación automática de tópicos está apagada |
| `redis` | Redis 7.4 | Caché de precios del Ministerio y canal de alarmas (`alarmas:stock-bajo`, PD-01) |
| `s3` | SeaweedFS 3.97 | Almacén de objetos S3 compatible: buckets `documentos-pdf` e `imagenes-platos` (DEC-67) |
| `correo` | Mailpit 1.24 | Servidor de correo certificado simulado (SMTP en el puerto 1025) |
| `elasticsearch` | Elasticsearch 8.19 | Logs de error de los servicios (índice `logs-errores`, DEC-84): un nodo, sin TLS ni usuarios, solo dentro de la red |
| `ms-identidad` | Spring Boot | V01 y alta de usuarios: BCrypt, JWT RS256 con el menú del rol, JWKS en `/api/auth/jwks` |
| `ms-menu` | Spring Boot | V06: platos con su categoría, receta e imagen; publica `menu.actualizado` |
| `ms-pedidos` | Spring Boot | V02–V04: pedidos por mesa y catálogo del mesero (escucha `menu.actualizado`); publica `pedido.registrado` |
| `ms-inventario` | Spring Boot | V05 y V07: existencias, recetas (por `menu.actualizado`), consumo (por `pedido.registrado`) y cierre de las 23:59 con ShedLock; publica `inventario.stock-bajo`; con `solicitud.registrada` atiende las alertas |
| `ms-solicitudes` | Spring Boot | V08–V12: el jefe de cocina registra y consulta sus solicitudes; el auxiliar de compras las consulta todas (filtros de HU-07); publica `solicitud.registrada`; con `cotizacion.enviada` pasa a EN TRÁMITE las que quedan cotizadas y publica `solicitud.en-tramite` |
| `ms-cotizaciones` | Spring Boot + OpenPDF | V13–V20 y proveedores (HU-15): el auxiliar arma la cotización de una o varias solicitudes y la envía con su PDF (bucket `documentos-pdf`) y el precio de referencia de ms-integracion; la directora registra los valores del proveedor y valida con la cadena de reglas; publica `cotizacion.enviada` y `cotizacion.validada`, escucha `orden.generada` |
| `ms-ordenes` | Spring Boot + OpenPDF | V21–V23: la directora genera la orden de compra de una cotización OPCIONADA (OC-0012 en adelante), que lee de ms-cotizaciones con su JWT; el PDF de la orden va al bucket `documentos-pdf` y se puede abrir desde V23 (DEC-75); cuando el pedido llega, la directora la marca como recibida y pasa a FINALIZADA (DEC-80); publica `orden.generada`, con la que ms-cotizaciones pasa la cotización a CON_ORDEN y ms-solicitudes completa sus solicitudes |
| `ms-notificaciones` | Spring Boot + Jakarta Mail | Correos certificados (huella SHA-256 y estampa cronológica) por `inventario.stock-bajo`, `solicitud.registrada`, `solicitud.en-tramite`, `cotizacion.enviada` y `orden.generada`, con el PDF del almacén adjunto; la alarma de stock bajo al jefe de cocina por Redis y WebSocket (PD-01); libreta de destinatarios; traza de envíos que V14 consulta (DEC-76); reintentos y cola de errores, que se vuelve a procesar sola (DEC-81) |
| `ms-integracion` | Spring Boot + Apache CXF | Frontera SOA: precio estándar del Ministerio por SOAP con la caché en Redis (Proxy) y el endpoint SOAP `/api/integracion/abastecimiento` (WSDL en `?wsdl`) |
| `ms-administracion` | Spring Boot | Ventanas del administrador: parámetros y tema (el Singleton `ParametrosSistema`, que cada réplica refresca de la base), la auditoría de todos los servicios (escucha `auditoria.evento`; sus propias acciones van directo a su bitácora) y los logs de errores que consulta en Elasticsearch |
| `ministerio` | WireMock 3.13 | Servicio web SOAP de precios del Ministerio de Comercio, simulado (DEC-71); solo lo ve ms-integracion |
| `kafka-topicos`, `s3-bucket` | — | Crean los tópicos y los buckets al arrancar (`levantar.sh` los vuelve a correr), y terminan |

## Usuarios de demostración

Todos con la clave `Konrad2026` (la pantalla de inicio de sesión tiene un botón por rol):

| Correo | Rol |
|---|---|
| isabella.torres@konradgourmet.com | Mesero |
| carlos.mendoza@konradgourmet.com | Jefe de cocina |
| isabella.castro@konradgourmet.com | Auxiliar de compras |
| laura.gomez@konradgourmet.com | Directora de compras |
| andres.rojas@konradgourmet.com | Director comercial (DEC-63) |
| paula.herrera@konradgourmet.com | Administradora (DEC-63) |

## Seguridad

- ms-identidad firma el JWT con RS256 (DEC-62); la llave privada se guarda en su base y no sale del servicio.
- El gateway valida firma y vigencia en cada petición; solo `/api/auth/**` pasa sin token.
- Cada servicio vuelve a validar el token y comprueba el rol en sus endpoints con `@PreAuthorize` (módulo
  `compartido`). El usuario y la sucursal salen del JWT, nunca del cuerpo de la petición.
- Auditoría y trazas (DEC-64): el gateway pone `X-Correlation-Id` si no viene; los eventos salen con el
  correo de quien hizo la operación y el `correlacionId` del recorrido (los listeners heredan el del evento
  que reciben; el cierre diario publica «sistema»). El servicio devuelve la cabecera en la respuesta.
- Los servicios corren en hora de Bogotá: el turno del mesero y el cierre de las 23:59 se cuentan así.
- El endpoint SOAP de ms-integracion también pasa por el gateway, así que pide un token válido (de cualquier
  rol, DEC-72). El REST interno de precios (`/interno/precios/{id}`) no se publica en el gateway: solo se
  alcanza dentro de la red de Docker, con el token del auxiliar o del director de compras. ms-cotizaciones lo
  llama (`PrecioSugeridoRestAdapter`, por Eureka) con el JWT de quien genera la cotización y su `X-Correlation-Id`.
- En ms-cotizaciones, generar y reenviar son del auxiliar de compras; registrar valores, validar y ver las
  pendientes, de la directora de compras; las dos ven la lista, las opcionadas, el detalle y el PDF.
  Proveedores: el auxiliar registra, edita e inactiva; la directora solo los lee.
- En ms-ordenes, todo es de la directora de compras: generar la orden, ver la lista, abrir su PDF y marcarla como
  recibida (`PATCH /api/ordenes-compra/{id}/finalizar`, DEC-80). ms-ordenes
  lee la cotización de ms-cotizaciones (`CotizacionRestClient`, por Eureka y sin pasar por el gateway) con el
  JWT de la directora y su `X-Correlation-Id`.
- En ms-administracion, todo es del administrador, salvo el tema (`GET /api/admin/parametros/tema`), que lee cualquier
  usuario con sesión: la SPA lo aplica al iniciar sesión (DEC-86). La pantalla de inicio de sesión no lo usa: solo
  `/api/auth/**` pasa sin token. El alta de usuarios (`POST /api/usuarios`, ms-identidad) también es del administrador.
- Logs de errores (DEC-84): cada servicio con `compartido` envía sus errores (nivel ERROR) a Elasticsearch con
  `AppenderElasticsearch`, con la correlación y el usuario del recorrido; Elasticsearch no se publica fuera de la red de
  Docker y solo ms-administracion lo consulta.
- En ms-notificaciones, la libreta de destinatarios es del administrador; la traza de envíos de un recorrido
  (`GET /api/notificaciones/envios?correlacion=…`, V14) la leen el auxiliar, la directora y el administrador; y el
  WebSocket de las alarmas (`/api/notificaciones/alarmas`), solo el jefe de cocina. Como el navegador no deja
  poner la cabecera Authorization al abrir un WebSocket, solo en esa ruta el gateway y el servicio aceptan el
  JWT en `?access_token=` (y Nginx no la escribe en su registro). El gateway acepta la conexión antes de
  conectarse con el servicio, así que un rol sin permiso ve que se abre y se cierra enseguida.

## Clases que no están en el diagrama de clases

Cada servicio Spring tiene, además de las clases del diagrama (páginas 4a–4e), piezas de infraestructura
que el diagrama omite a propósito, como omite los constructores:

- la clase `…Application` y una clase de configuración que arma los servicios de aplicación (el núcleo de
  `src/` no lleva anotaciones de Spring);
- las entidades JPA y los repositorios de Spring Data, que son internos de su adaptador `…JpaAdapter`;
- en ms-identidad, `LlaveFirma` (la llave RSA), `JwksController` (la llave pública que usa el gateway) y
  `DatosIniciales` (los usuarios de demostración);
- en `compartido`, `FormatoEventos`, `ConfiguracionCompartida`, `SeguridadServicio`, `ManejoErrores`, `Sesion`
  (usuario y sucursal del JWT), y lo de DEC-64: `ContextoRecorrido`, `FiltroContexto`, `InterceptorCorrelacion`
  y `ConvertidorEventos` (arma el evento que recibe un `@KafkaListener`);
- en el gateway, `SeguridadGateway` y `CorrelacionGateway`;
- en ms-menu, `DatosIniciales` (la carta de la demo, creada con `PlatoService` para que viaje por el evento);
- en ms-inventario, `CierreDiaController` (`POST /api/inventario/cierre`, el botón «Cierre del día» de V07,
  solo para la demostración, DEC-68);
- en ms-integracion, `MapaPreciosRedis` (el mapa `{Redis}` del Proxy: una llave por precio que vence en un día)
  y la publicación del endpoint SOAP en Apache CXF, en `ConfiguracionIntegracion`. `MinisterioSoapClient` no
  tiene versión Spring: es Java puro y ms-integracion copia el de `src/` (DEC-71);
- en ms-cotizaciones, `NumeracionCotizaciones` (el número de la secuencia, COT-018 en adelante, que se pide
  antes de generar porque el PDF ya lo lleva) y `NombresProductos` (la copia local del nombre de cada producto,
  que la SPA manda al generar: la usan el PDF y las ventanas de la directora, DEC-74);
- en ms-ordenes, `NombresProductos` (la copia local del nombre de cada producto, que `CotizacionRestClient` toma
  de la cotización: la usan el PDF de la orden y V23, DEC-78);
- en `compartido` también, `AppenderElasticsearch` y `RegistroLogsElasticsearch` (envían los errores de cada servicio a
  Elasticsearch, DEC-84);
- en ms-administracion, `RefrescoParametros` (cada minuto recarga de la base la caché `ParametrosSistema` de la réplica;
  10 s en el compose, con `REFRESCO_PARAMETROS`). La bitácora guarda además la correlación del recorrido (columna
  `correlacion_id`) y no repite un evento entregado dos veces;
- en ms-notificaciones, `CanalAlarmas` y `ConfiguracionWebSocket` (las conexiones WebSocket de V05 en cada réplica,
  que escuchan el canal de Redis donde publica `AlarmaPushAdapter`, PD-01) y `EnviosController` (la consulta de
  envíos de V14, DEC-76) y `ReprocesoColaErrores` (vuelve a procesar `notificaciones.dlq`, DEC-81). Los decoradores del correo (`CorreoDecorator`, `CorreoCertificadoDecorator`,
  `EstampaCronologicaDecorator`) no tienen versión Spring: son Java puro y ms-notificaciones copia los de `src/`.

El `.drawio` las nombra sin caja, en notas «Infraestructura que no se dibuja» de las páginas 4a, 4c, 4d y 4e y en
`n_alarmapush` y `n_fallos` de la 4d (DEC-79); `ReprocesoColaErrores` solo aparece en el texto de `n_fallos`. Lo que
es solo de la demostración (`CierreDiaController` y los `DatosIniciales`) no se documenta (DEC-57).

Lo que todavía no está en la documentación (mockups, `.drawio` o reporte) está anotado en
`documentos/CAMBIOS_A_DOCUMENTAR.md` (DEC-77); `ReprocesoColaErrores` es CD-14.

Los adaptadores del diagrama (`AuthRestController`, `UsuarioJpaAdapter`, `JwtEmisorAdapter`…) tienen el
mismo nombre, paquete y métodos que en el diagrama.

## Diferencias con el despliegue de la página 5 (DEC-58)

La funcionalidad es la misma; cambia la infraestructura, que en un solo equipo no se puede reproducir:

- una réplica por servicio, sin autoescalado ni zonas; un solo broker de Kafka (factor de replicación 1);
- un solo PostgreSQL con once bases separadas, en lugar de once servidores con réplica síncrona;
- sin CDN, WAF, DNS global, centro de datos alterno, MirrorMaker ni mTLS;
- de ELK solo está Elasticsearch, con un nodo y sin TLS ni usuarios (DEC-84): los servicios le escriben directo, sin
  Logstash, y sin Kibana (los logs se ven en la ventana del administrador);
- los sistemas externos son simulados: el Ministerio de Comercio con WireMock (DEC-71), el servidor de correo
  certificado con Mailpit (SMTP sin TLS) y, en el corte 6, las redes sociales;
- un solo Redis sin usuarios separados para la caché de precios y el canal de alarmas;
- el almacén de objetos es SeaweedFS y no MinIO (DEC-59): la imagen de MinIO ya no se puede descargar
  y los dos hablan la misma API S3, así que el adaptador no cambia.

## Avance por cortes

| Corte | Contenido | Estado |
|---|---|---|
| 1 | Infraestructura, Eureka, gateway con los 13 prefijos, Nginx | Hecho |
| 2 | ms-identidad (login con JWT y BCrypt, menú por rol) y la SPA en React + TypeScript | Hecho |
| 3 | ms-pedidos, ms-menu y ms-inventario (V02–V07), DEC-64 y DEC-65 | Hecho |
| 4a | ms-solicitudes (V08–V12) y ms-integracion (precio de referencia con caché en Redis, SOAP), DEC-70 a DEC-72 | Hecho |
| 4b | ms-cotizaciones (V13–V19, proveedores y PDF con OpenPDF) y ms-solicitudes con `cotizacion.enviada`, DEC-73 y DEC-74 | Hecho |
| 5 | ms-ordenes y ms-notificaciones (V20–V23, correos con el PDF, alarma en vivo, orden recibida, reproceso de la cola de errores), DEC-75 a DEC-82 | Hecho |
| 6a | ms-administracion (auditoría, parámetros y tema, logs de errores con Elasticsearch, usuarios y destinatarios), DEC-83 a DEC-87 | Hecho |
| 6b | ms-analitica (tablero BAM del director comercial, promociones de HU-19 y `tendencia.detectada`, PD-05) | Pendiente |
| 7 | Pruebas de punta a punta, README y revisión | Pendiente |

## Probar de punta a punta

Con la plataforma arriba (mejor recién levantada con `./plataforma/bajar.sh --borrar` y `levantar.sh`):

```bash
python plataforma/pruebas/recorrido_corte3.py /tmp/capturas    # navegador: mesero, jefe, cierre, alerta (necesita Playwright y Chromium)
python plataforma/pruebas/verificar_corte3.py                  # revisa lo que quedó en PostgreSQL y en Kafka
python plataforma/pruebas/recorrido_corte4a.py /tmp/capturas   # navegador: solicitudes del jefe y del auxiliar (después del corte 3)
python plataforma/pruebas/verificar_corte4a.py /tmp/capturas   # PostgreSQL, Kafka, Redis, el Ministerio y el SOAP por el gateway
python plataforma/pruebas/recorrido_corte4b.py /tmp/capturas   # navegador: cotizaciones del auxiliar y de la directora, proveedores (después del 4a)
python plataforma/pruebas/verificar_corte4b.py /tmp/capturas   # PostgreSQL, el PDF en el almacén, Redis, Kafka de ida y vuelta, PENDIENTE_PRECIO
python plataforma/pruebas/recorrido_corte5.py /tmp/capturas    # navegador: V20 a V23, OC-0012 (y recibida), V14 con el correo, la alarma en vivo (después del 4b)
python plataforma/pruebas/verificar_corte5.py /tmp/capturas    # PostgreSQL, el PDF de la orden, Kafka, Mailpit con los adjuntos, WebSocket, DLQ y su reproceso
python plataforma/pruebas/recorrido_corte6a.py /tmp/capturas   # navegador: las ventanas del administrador, el tema y un error real (después del 5)
python plataforma/pruebas/verificar_corte6a.py /tmp/capturas   # la bitácora contra Kafka, el tema y su caché, Elasticsearch, usuarios y destinatarios
```

El recorrido registra un pedido de $82.000 (2 bandejas paisas y 1 ajiaco), crea un plato con imagen, lo edita
y lo desactiva mientras mira cómo cambia el catálogo del mesero, ejecuta el cierre del día y busca la alerta de
la res en V05, en escritorio y a 390 px; también prueba el aviso de sesión vencida.

El del corte 4a sigue desde ahí: el jefe registra SOL-031 (la de la demo: res 20 kg «Marca X» y arroz 15 kg),
la ve en «Mis solicitudes» y en V05, donde la alerta de la res desaparece porque ms-inventario recibió
`solicitud.registrada`; a 390 px pide papa desde «Solicitar» en V07 (SOL-032). La auxiliar de compras ve las dos
en su inicio (V11) y en «Todas las solicitudes» (V12), con los filtros de HU-07 y el detalle. El verificador
revisa las solicitudes en PostgreSQL; en Kafka, que el evento lleve el correo del jefe y la correlación de la
petición del navegador, y que ms-inventario lo haya recibido en el mismo recorrido; y en ms-integracion, que el
precio del pollo salga del Ministerio la primera vez (WireMock cuenta la llamada) y de Redis la segunda, que el
SOAP responda a través del gateway y que el REST interno no esté publicado.

El del corte 4b sigue con esas dos solicitudes. La auxiliar abre V13 desde «Generar cotización con SOL-031» de
V12 y envía COT-018 al Proveedor ABC (V14 con el PDF generado); desde V15 envía COT-019 al Proveedor C y descarga
su PDF; a 390 px envía COT-020 con SOL-031 y SOL-032 juntas al Proveedor D. El jefe ve sus dos solicitudes EN
TRÁMITE en V10 y V05. La directora, desde V16, registra los valores de COT-018 (res $21.250 y arroz $8.000) y
valida: V19 dice OPCIONADA, +4,81 % (las cifras de la demo); COT-019 (res $29.000) queda RECHAZADA, +34,62 %, y
COT-020 (res $4.000) SOSPECHOSA, −62,16 %. La auxiliar registra un proveedor, lo edita y lo inactiva, y ya no
sale en V13. El verificador revisa las cotizaciones y sus precios en PostgreSQL; que cada PDF esté en el bucket,
idéntico al que sirve el gateway y al que bajó el navegador, y que `pdftotext` lo lea; que el precio de cada
producto se le pidió al Ministerio una sola vez (las demás veces respondió Redis) y que ms-integracion registró
la consulta a nombre de la auxiliar y con la correlación del navegador; en Kafka, `cotizacion.enviada`,
`cotizacion.validada` y la ida y vuelta con ms-solicitudes (`solicitud.en-tramite` en el mismo recorrido); que un
producto que el Ministerio no conoce deja la cotización PENDIENTE_PRECIO, sin PDF ni evento; y los roles.
El del corte 5 sigue con COT-018 OPCIONADA. La auxiliar envía COT-022 al Proveedor B y V14 marca «Correo enviado»
cuando ms-notificaciones ya lo envió (DEC-76). La directora la valida (+7,69 %) y desde V19 abre V20, que compara
COT-018 y COT-022 de SOL-031 con su diferencia y su valor total; escoge COT-018 y genera OC-0012 (V21 → V22,
$545.000); V23 muestra la orden con su detalle y su PDF («Ver PDF» y «Descargar», DEC-75). COT-018 sale de V20
(CON_ORDEN), V16 cuenta la orden y el jefe ve SOL-031 COMPLETADA. A 390 px, la directora marca OC-0012 como recibida
(con su confirmación) y pasa a FINALIZADA (DEC-80). Para la alarma en vivo, el mesero registra por
la API un pedido de 50 pechugas y el jefe ejecuta el cierre del día en V07: el pollo queda en 24,5 % y la alarma
llega por WebSocket sin recargar; a 390 px pasa lo mismo con la papa (70 ajiacos) y «Solicitar» abre V08. El
verificador revisa la orden en PostgreSQL; que su PDF esté en el bucket, idéntico al que sirve el gateway, al que
bajó el navegador y al adjunto del correo; `orden.generada` con la directora y la correlación del navegador, y que
ms-cotizaciones, ms-solicitudes y ms-notificaciones la recibieron en el mismo recorrido; en Mailpit, el correo
certificado de cada cotización enviada y de la orden, con su PDF idéntico al del almacén y las cabeceras
`X-Certificado` y `X-Sello-Tiempo`, y los avisos al jefe; el canal de Redis y el WebSocket (token y rol); la
libreta de destinatarios; las reglas de HU-14 y los roles; y, con el servidor de correo en pausa, que el envío
falla, queda en la traza y pasa a `notificaciones.dlq` después de los reintentos; al volver el correo, la cola se
reprocesa sola (a los 30 s en local) y lo entrega una vez, en el recorrido original de la auxiliar (DEC-81); después,
el reenvío de la auxiliar (HU-12) entrega un segundo correo. También revisa que la orden quedó FINALIZADA, con su
auditoría a nombre de la directora, y que finalizarla otra vez da 409.

El del corte 6a es de la administradora. En «Parámetros y tema» crea `general.propina-sugerida`, prueba los mensajes
(valor vacío, clave mal escrita, color que no es #RRGGBB y un logo de más de 200 KB) y cambia la imagen corporativa a
«Konrad Gourmet Express», verde y con un logo PNG: la SPA la aplica enseguida y el mesero la ve al iniciar sesión, sin
redesplegar (la pantalla de inicio de sesión sigue igual, DEC-86); el mesero no entra a las ventanas del administrador.
«Auditoría» abre con los últimos 7 días, lo más reciente primero, con los mensajes del rango y «Hoy». Con el servidor de
correo en pausa, la auxiliar reenvía una cotización: el error de ms-notificaciones llega a Elasticsearch y «Logs de
errores» lo muestra al filtrar por ese servicio. En «Usuarios» registra a Sofía Ramírez, jefa de cocina de la Sucursal
Norte (y ella inicia sesión), y en «Destinatarios» la agrega a esa sucursal. A 390 px, las cinco ventanas caben a lo
ancho y la administradora restaura el tema de siempre. El verificador compara la bitácora con `auditoria.evento`: cada
evento de los cortes 3 a 6a está una vez, con su usuario y su correlación, y uno publicado otra vez no se repite; los
cambios de parámetros están a nombre de la administradora, con la correlación del navegador y sin pasar por Kafka.
Revisa la API de auditoría y sus roles; el tema con cualquier rol y sin token (401); que el contenedor `web` arrancó
antes del cambio de color; que un cambio hecho directo en la base llega a la caché cuando la réplica la refresca (10 s);
el índice `logs-errores`, el error del recorrido con su correlación y la consulta por servicio; que con Elasticsearch en
pausa la ventana avisa (503) y ese error llega después; y el alta de Sofía (hash BCrypt y auditoría) y su registro como
destinataria.

**La bitácora de ms-administracion.** El grupo de consumo `ms-administracion` empieza por el principio de
`auditoria.evento`: en una instalación que ya tenía datos (por ejemplo, la de los volúmenes `konrad-gourmet_*`), la
primera vez que arranca carga toda la auditoría desde el corte 3.

**La cola de errores (DEC-81).** `ReprocesoColaErrores` lee `notificaciones.dlq` con su propio grupo de consumo
(`ms-notificaciones-reproceso`): cada evento espera en la cola 5 minutos (30 s en este compose, con
`ESPERA_COLA_ERRORES`) y se vuelve a notificar; si el correo sigue caído, vuelve a la cola con la ronda en la cabecera
`kg-reproceso`, y después de la tercera ronda se queda ahí. En una instalación que ya tenía eventos en la cola (por
ejemplo, la de los volúmenes `konrad-gourmet_*` del corte 5), el grupo nuevo empieza por el principio de la cola: la
primera vez que arranca, vuelve a enviar esos correos una vez.

La versión en memoria (`./ejecutar.sh` y `./web.sh`) sigue funcionando igual y no necesita Docker.
