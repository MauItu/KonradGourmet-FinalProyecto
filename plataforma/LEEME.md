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
| `s3` | SeaweedFS 3.97 | Almacén de objetos S3 compatible, bucket `documentos-pdf` |
| `correo` | Mailpit 1.24 | Servidor de correo certificado simulado (SMTP en el puerto 1025) |
| `ms-identidad` | Spring Boot | V01 y alta de usuarios: BCrypt, JWT RS256 con el menú del rol, JWKS en `/api/auth/jwks` |
| `kafka-topicos`, `s3-bucket` | — | Crean los tópicos y el bucket al arrancar, y terminan |

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
- Cada servicio vuelve a validar el token y comprueba el rol en sus endpoints (módulo `compartido`).

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
| 3 | ms-pedidos, ms-menu y ms-inventario (V02–V10) | Pendiente |
| 4 | ms-solicitudes, ms-cotizaciones y ms-integracion (V11–V19) | Pendiente |
| 5 | ms-ordenes y ms-notificaciones (V20–V23) | Pendiente |
| 6 | ms-administracion y ms-analitica (Administrador, Director comercial, tablero BAM) | Pendiente |
| 7 | Pruebas de punta a punta, README y revisión | Pendiente |

La versión en memoria (`./ejecutar.sh` y `./web.sh`) sigue funcionando igual y no necesita Docker.
