# Konrad Gourmet · Proyecto final de Arquitectura de Software

Sistema de Abastecimiento Konrad Gourmet. Fundación Universitaria Konrad Lorenz, Arquitectura de Software, 2026-2.

Equipo: Mauricio Iturriza, Isabella Castro, Carlos Franco y Adrián Corredor.

La propuesta combina tres arquitecturas, cada una en el nivel donde resuelve un problema distinto: **microservicios** (once servicios, cada uno con su base de datos, detrás de un API Gateway), **orientada a eventos** (Kafka, diez tópicos, para la comunicación entre roles y los procesos automáticos) y **hexagonal** (el dominio de cada servicio aislado de la base de datos, del SOAP, del correo y del PDF). La arquitectura corre completa en un equipo con Docker Compose: es la plataforma que se muestra en la sustentación.

## Entregables

| Entregable | Archivo | Qué contiene |
|---|---|---|
| Reporte de arquitectura | `documentos/Reporte_Arquitectura_Konrad_Gourmet_v2.pdf` (y `.docx` editable) | El documento principal: 19 secciones y un anexo, 70 páginas |
| Diagramas | `documentos/Arquitectura_Konrad_Gourmet.drawio` | Las 11 páginas: mapa integrado y modelo C4 completo |
| Casos de uso | `documentos/Casos de uso — Konrad Gourmet.md` y `documentos/casos_de_uso.png` | 19 casos de uso con actores, flujos, reglas y su trazabilidad a historias, ventanas y servicios, y su diagrama UML |
| Historias de usuario | `documentos/STORY_MAPPING_CORREGIDO.md` | Las 19 historias del Story Mapping, con el MVP, la estimación y el plan de sprints |
| Capacidad del equipo | `documentos/Copia de ARQUITECTURA-Supuestos de Capacidad  del equipo.xlsx` | Supuestos de capacidad y sprints (5 sprints, 10 semanas) |
| Mockups | `documentos/DISEÑO UXUI.pdf`, `documentos/flujo-web-konrad-gourmet (1).pdf`, `documentos/mockups_admin/` y `documentos/mockups_faltantes/` | Las 23 ventanas del prototipo y su flujo, y los mockups de las otras diez ventanas y de las correcciones; cada ventana tiene el suyo |
| Plataforma | `codigo/` | Los once microservicios, la SPA y la infraestructura, listos para levantar con Docker Compose (ver «La plataforma») |

## La plataforma

El diagrama de contenedores (página 3a) corriendo en un solo equipo: 11 microservicios en Spring Boot 3.5 y Spring Cloud, API Gateway, Eureka, Kafka, PostgreSQL (una base y un usuario por servicio), Redis, almacén de objetos S3, Elasticsearch, servidor de correo y la SPA en React + TypeScript con sus 33 ventanas. Solo necesita Docker.

```bash
cd codigo
./plataforma/levantar.sh                # compila los servicios y levanta todo (unos minutos la primera vez)
./plataforma/bajar.sh                   # la apaga y conserva los datos
```

| Dirección | Qué es |
|---|---|
| http://localhost:8080 | La aplicación |
| http://localhost:8080/plataforma/ | Los 11 servicios en Eureka y su prefijo en el gateway |
| http://localhost:8761 | Eureka |
| http://localhost:8025 | Los correos que envía el sistema, con sus adjuntos |

Usuarios de demostración, todos con la clave `Konrad2026` (la pantalla de inicio de sesión tiene un botón por rol):

| Rol | Usuario |
|---|---|
| Mesero | isabella.torres@konradgourmet.com |
| Jefe de cocina | carlos.mendoza@konradgourmet.com |
| Auxiliar de compras | isabella.castro@konradgourmet.com |
| Directora de compras | laura.gomez@konradgourmet.com |
| Director comercial | andres.rojas@konradgourmet.com |
| Administradora | paula.herrera@konradgourmet.com |

**Pruebas.** `./plataforma/probar_todo.sh` levanta una instancia limpia y corre 1.255 comprobaciones: 400 verificaciones del núcleo, 91 pruebas JUnit de los servicios y 764 comprobaciones de los recorridos en el navegador y de los verificadores, entre ellas una compra completa que cruza los 11 servicios y los 6 roles. Los detalles (qué corre en cada contenedor, seguridad, outbox, avance por cortes) están en `codigo/plataforma/LEEME.md`.

## Páginas del diagrama

Abrir `documentos/Arquitectura_Konrad_Gourmet.drawio` en [draw.io](https://app.diagrams.net) o en draw.io Desktop.

| Página | Qué muestra | Dónde se explica en el reporte |
|---|---|---|
| 1 · Mapa integrado | Las tres arquitecturas juntas: los mockups de cada rol (las 23 ventanas y las otras diez), el gateway, los once microservicios con su hexágono, la banda de Kafka y los sistemas externos | §9 |
| 2 · C4 Contexto | El sistema, los seis roles y los sistemas externos | §14 |
| 3a · C4 Contenedores | SPA, gateway, microservicios, bases de datos, Kafka y almacenes | §14 |
| 3b · C4 Componentes | El interior de ms-cotizaciones | §14 |
| 4a a 4e · Código | Diagramas de clases UML de cada servicio (195 clases), con los patrones de diseño en magenta | §12 y anexo A |
| 5 · Despliegue | Kubernetes, réplicas, centro alterno y respaldo | §14 |
| 6 · C4 Dinámico | El recorrido completo de una compra, paso a paso | §10 |

## Mapa del reporte

| Sección | Tema | Página |
|---|---|---|
| 1 | Resumen de la decisión | 3 |
| 2 | Método de análisis | 4 |
| 3 | Requerimientos no funcionales y decisiones | 6 |
| 4 | Estilos evaluados | 8 |
| 5 | Microservicios | 10 |
| 6 | Arquitectura orientada a eventos | 13 |
| 7 | Arquitectura hexagonal | 15 |
| 8 | Estilos de apoyo | 17 |
| 9 | El mapa integrado y su notación | 18 |
| 10 | Arquitectura por flujo de ventanas | 27 |
| 11 | Eventos del sistema | 33 |
| 12 | Patrones de diseño en el nivel de código | 36 |
| 13 | Del diagrama al código | 43 |
| 14 | Modelo C4: contexto, contenedores, componentes y despliegue | 45 |
| 15 | Trazabilidad de las ventanas | 53 |
| 16 | Riesgos y mitigaciones | 59 |
| 17 | Supuestos | 60 |
| 18 | Declaración de uso | 62 |
| 19 | Referencias | 63 |
| Anexo A | Nivel de código completo (páginas 4a a 4e) | 65 |

## Código

`codigo/` viene del repo del equipo [`iCastro117/KonradGourmet`](https://github.com/iCastro117/KonradGourmet) y tiene cuatro partes:

| Carpeta | Qué es |
|---|---|
| `src/` | El núcleo: las 195 clases de los diagramas de clases, organizadas por capas hexagonales (`co.edu.konradlorenz.<servicio>.<capa>`), en Java puro; `./ejecutar.sh` compila, corre la demostración y las verificaciones (Java 21, sin Maven) |
| `servicios/` | Los once microservicios en Spring Boot, que copian el núcleo de su módulo al compilar y le agregan sus adaptadores (REST, Kafka, JPA, SOAP, PDF, correo) |
| `web/` | La SPA en React + TypeScript |
| `plataforma/` | Docker Compose, los scripts para levantarla y probarla, y los recorridos de prueba |

Su `README.md` explica la organización y los patrones; `plataforma/LEEME.md`, la plataforma.

## Historial de la revisión

Los demás archivos de `documentos/` son el registro del proceso de revisión y corrección, no entregables: `PLAN_Y_ESTADO.md` (resumen del estado), `DECISIONES.md` (cada decisión con sus opciones), `CAMBIOS_A_DOCUMENTAR.md`, `PLAN_*`, `CORRECCIONES_*`, `REVISION_*`, `VALIDACION_*`, `INVENTARIO_*`, `PENDIENTES_POST_SESION6.md` y `ESTADO_PROYECTO_Y_C4.md`. `documentos/guias_sustentacion/` tiene el material del equipo para preparar la sustentación. También se conservan las versiones originales, antes de las correcciones: `Reporte_Arquitectura_Konrad_Gourmet.pdf` y `Arquitectura_Konrad_GourmetOriginalSinCorregir.drawio`. La carpeta `documentos/reporte/` tiene el script y los textos con que se genera el reporte v2.

Los `.md` del historial llaman a la carpeta de código `KonradGourmet/`; aquí es `codigo/`.
