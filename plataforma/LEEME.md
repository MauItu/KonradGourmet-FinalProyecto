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
| http://localhost:8025 | Mailpit: los correos que envía ms-notificaciones |

## Qué corre

| Contenedor | Imagen | Papel en la arquitectura |
|---|---|---|
| `web` | nginx 1.28 | Sirve la SPA en React + TypeScript (se compila desde `web/`); única entrada desde el navegador |
| `api-gateway` | Spring Cloud Gateway | Facade: valida el JWT y enruta los 13 prefijos a los 11 servicios |
| `service-registry` | Eureka | Registro de instancias vivas |
| `postgres` | PostgreSQL 17 | Una base y un usuario por servicio (`ms_pedidos`, `ms_cotizaciones`…); un usuario no puede conectarse a la base de otro |
| `kafka` | Kafka 3.9 (KRaft) | Broker de eventos con los 10 tópicos; la creación automática de tópicos está apagada |
| `redis` | Redis 7.4 | Caché de precios del Ministerio y canal de alarmas |
| `s3` | SeaweedFS 3.97 | Almacén de objetos S3 compatible: buckets `documentos-pdf` e `imagenes-platos` (DEC-67) |
| `correo` | Mailpit 1.24 | Servidor de correo certificado simulado (SMTP en el puerto 1025) |
| `ms-identidad` | Spring Boot | V01 y alta de usuarios: BCrypt, JWT RS256 con el menú del rol, JWKS en `/api/auth/jwks` |
| `ms-menu` | Spring Boot | V06: platos con su categoría, receta e imagen; publica `menu.actualizado` |
| `ms-pedidos` | Spring Boot | V02–V04: pedidos por mesa y catálogo del mesero (escucha `menu.actualizado`); publica `pedido.registrado` |
| `ms-inventario` | Spring Boot | V05 y V07: existencias, recetas (por `menu.actualizado`), consumo (por `pedido.registrado`) y cierre de las 23:59 con ShedLock; publica `inventario.stock-bajo`; con `solicitud.registrada` atiende las alertas |
| `ms-solicitudes` | Spring Boot | V08–V12: el jefe de cocina registra y consulta sus solicitudes; el auxiliar de compras las consulta todas (filtros de HU-07); publica `solicitud.registrada`; con `cotizacion.enviada` pasa a EN TRÁMITE las que quedan cotizadas y publica `solicitud.en-tramite` |
| `ms-cotizaciones` | Spring Boot + OpenPDF | V13–V19 y proveedores (HU-15): el auxiliar arma la cotización de una o varias solicitudes y la envía con su PDF (bucket `documentos-pdf`) y el precio de referencia de ms-integracion; la directora registra los valores del proveedor y valida con la cadena de reglas; publica `cotizacion.enviada` y `cotizacion.validada`, escucha `orden.generada` |
| `ms-integracion` | Spring Boot + Apache CXF | Frontera SOA: precio estándar del Ministerio por SOAP con la caché en Redis (Proxy) y el endpoint SOAP `/api/integracion/abastecimiento` (WSDL en `?wsdl`) |
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
  pendientes, de la directora de compras; las dos ven la lista, el detalle y el PDF. Proveedores: el auxiliar
  registra, edita e inactiva; la directora solo los lee.

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
  que la SPA manda al generar: la usan el PDF y las ventanas de la directora, DEC-74).

Los adaptadores del diagrama (`AuthRestController`, `UsuarioJpaAdapter`, `JwtEmisorAdapter`…) tienen el
mismo nombre, paquete y métodos que en el diagrama.

## Diferencias con el despliegue de la página 5 (DEC-58)

La funcionalidad es la misma; cambia la infraestructura, que en un solo equipo no se puede reproducir:

- una réplica por servicio, sin autoescalado ni zonas; un solo broker de Kafka (factor de replicación 1);
- un solo PostgreSQL con once bases separadas, en lugar de once servidores con réplica síncrona;
- sin CDN, WAF, DNS global, centro de datos alterno, MirrorMaker, mTLS ni ELK;
- los sistemas externos son simulados: el Ministerio de Comercio con WireMock (DEC-71), el servidor de correo
  certificado con Mailpit y, en el corte 6, las redes sociales;
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
| 5 | ms-ordenes y ms-notificaciones (V20–V23) | Pendiente |
| 6 | ms-administracion y ms-analitica (Administrador, Director comercial, tablero BAM) | Pendiente |
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
El correo al proveedor con el PDF lo envía ms-notificaciones en el corte 5: el evento ya lleva el `pdfUrl` y el
correo del proveedor.

La versión en memoria (`./ejecutar.sh` y `./web.sh`) sigue funcionando igual y no necesita Docker.
