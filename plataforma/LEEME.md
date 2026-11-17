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
| `ms-inventario` | Spring Boot | V05 y V07: existencias, recetas (por `menu.actualizado`), consumo (por `pedido.registrado`) y cierre de las 23:59 con ShedLock; publica `inventario.stock-bajo` |
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
  solo para la demostración, DEC-68).

Los adaptadores del diagrama (`AuthRestController`, `UsuarioJpaAdapter`, `JwtEmisorAdapter`…) tienen el
mismo nombre, paquete y métodos que en el diagrama.

## Diferencias con el despliegue de la página 5 (DEC-58)

La funcionalidad es la misma; cambia la infraestructura, que en un solo equipo no se puede reproducir:

- una réplica por servicio, sin autoescalado ni zonas; un solo broker de Kafka (factor de replicación 1);
- un solo PostgreSQL con once bases separadas, en lugar de once servidores con réplica síncrona;
- sin CDN, WAF, DNS global, centro de datos alterno, MirrorMaker, mTLS ni ELK;
- los sistemas externos (Ministerio de Comercio, servidor de correo certificado, redes sociales) son simulados;
- el almacén de objetos es SeaweedFS y no MinIO (DEC-59): la imagen de MinIO ya no se puede descargar
  y los dos hablan la misma API S3, así que el adaptador no cambia.

## Avance por cortes

| Corte | Contenido | Estado |
|---|---|---|
| 1 | Infraestructura, Eureka, gateway con los 13 prefijos, Nginx | Hecho |
| 2 | ms-identidad (login con JWT y BCrypt, menú por rol) y la SPA en React + TypeScript | Hecho |
| 3 | ms-pedidos, ms-menu y ms-inventario (V02–V07), DEC-64 y DEC-65 | Hecho |
| 4 | ms-solicitudes, ms-cotizaciones y ms-integracion (V08–V19) | Pendiente |
| 5 | ms-ordenes y ms-notificaciones (V20–V23) | Pendiente |
| 6 | ms-administracion y ms-analitica (Administrador, Director comercial, tablero BAM) | Pendiente |
| 7 | Pruebas de punta a punta, README y revisión | Pendiente |

## Probar de punta a punta

Con la plataforma arriba (mejor recién levantada con `./plataforma/bajar.sh --borrar` y `levantar.sh`):

```bash
python plataforma/pruebas/recorrido_corte3.py /tmp/capturas   # navegador: mesero, jefe, cierre, alerta (necesita Playwright y Chromium)
python plataforma/pruebas/verificar_corte3.py                 # revisa lo que quedó en PostgreSQL y en Kafka
```

El recorrido registra un pedido de $82.000 (2 bandejas paisas y 1 ajiaco), crea un plato con imagen, lo edita
y lo desactiva mientras mira cómo cambia el catálogo del mesero, ejecuta el cierre del día y busca la alerta de
la res en V05, en escritorio y a 390 px; también prueba el aviso de sesión vencida.

La versión en memoria (`./ejecutar.sh` y `./web.sh`) sigue funcionando igual y no necesita Docker.
