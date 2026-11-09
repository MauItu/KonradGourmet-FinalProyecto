# Konrad Gourmet · Proyecto final de Arquitectura de Software

Sistema de Abastecimiento Konrad Gourmet. Fundación Universitaria Konrad Lorenz, Arquitectura de Software, 2026-2.

Equipo: Mauricio Iturriza, Isabella Castro, Carlos Franco y Adrián Corredor.

La propuesta combina tres arquitecturas, cada una en el nivel donde resuelve un problema distinto: **microservicios** (once servicios, cada uno con su base de datos, detrás de un API Gateway), **orientada a eventos** (Kafka, diez tópicos, para la comunicación entre roles y los procesos automáticos) y **hexagonal** (el dominio de cada servicio aislado de la base de datos, del SOAP, del correo y del PDF).

## Entregables

Todos están en `documentos/`.

| Entregable | Archivo | Qué contiene |
|---|---|---|
| Reporte de arquitectura | `Reporte_Arquitectura_Konrad_Gourmet_v2.pdf` (y `.docx` editable) | El documento principal: 19 secciones y un anexo, 61 páginas |
| Diagramas | `Arquitectura_Konrad_Gourmet.drawio` | Las 11 páginas: mapa integrado y modelo C4 completo |
| Casos de uso | `Casos de uso — Konrad Gourmet.md` | 19 casos de uso con actores, flujos, reglas y su trazabilidad a historias, ventanas y servicios |
| Historias de usuario | `STORY_MAPPING_CORREGIDO.md` | Las 19 historias del Story Mapping, con el MVP, la estimación y el plan de sprints |
| Capacidad del equipo | `Copia de ARQUITECTURA-Supuestos de Capacidad  del equipo.xlsx` | Supuestos de capacidad y sprints (5 sprints, 10 semanas) |
| Mockups | `DISEÑO UXUI.pdf` y `flujo-web-konrad-gourmet (1).pdf` | Las 23 ventanas (V01 a V23) y el flujo web |

El código Java está en `codigo/` (ver más abajo).

## Páginas del diagrama

Abrir `documentos/Arquitectura_Konrad_Gourmet.drawio` en [draw.io](https://app.diagrams.net) o en draw.io Desktop.

| Página | Qué muestra | Dónde se explica en el reporte |
|---|---|---|
| 1 · Mapa integrado | Las tres arquitecturas juntas: los mockups de cada rol, el gateway, los once microservicios con su hexágono, la banda de Kafka y los sistemas externos | §9 |
| 2 · C4 Contexto | El sistema, los seis roles y los sistemas externos | §14 |
| 3a · C4 Contenedores | SPA, gateway, microservicios, bases de datos, Kafka y almacenes | §14 |
| 3b · C4 Componentes | El interior de ms-cotizaciones | §14 |
| 4a a 4e · Código | Diagramas de clases UML de cada servicio, con los patrones de diseño en magenta | §12 y anexo A |
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
| 10 | Arquitectura por flujo de ventanas | 26 |
| 11 | Eventos del sistema | 30 |
| 12 | Patrones de diseño en el nivel de código | 33 |
| 13 | Del diagrama al código | 39 |
| 14 | Modelo C4: contexto, contenedores, componentes y despliegue | 40 |
| 15 | Trazabilidad de las 23 ventanas | 48 |
| 16 | Riesgos y mitigaciones | 51 |
| 17 | Supuestos | 52 |
| 18 | Declaración de uso de inteligencia artificial | 53 |
| 19 | Referencias | 54 |
| Anexo A | Nivel de código completo (páginas 4a a 4e) | 56 |

## Código

`codigo/` tiene el código Java de los once microservicios, organizado por capas hexagonales (paquete `co.edu.konradlorenz.<servicio>.<capa>`), con las mismas clases del diagrama. Viene del repo del equipo [`iCastro117/KonradGourmet`](https://github.com/iCastro117/KonradGourmet). Su `README.md` explica cómo está organizado. Para compilarlo y ejecutar las pruebas (Java 21, sin Maven):

```bash
cd codigo
./ejecutar.sh
```

## Pendiente

- El diagrama UML de casos de uso (`documentos/casos_de_uso.png`).

## Historial de la revisión

Los demás archivos de `documentos/` son el registro del proceso de revisión y corrección, no entregables: `DECISIONES.md` (cada decisión con sus opciones), `PLAN_*`, `CORRECCIONES_*`, `REVISION_*`, `VALIDACION_*`, `INVENTARIO_*`, `PENDIENTES_POST_SESION6.md` y `ESTADO_PROYECTO_Y_C4.md`. También se conservan las versiones originales, antes de las correcciones: `Reporte_Arquitectura_Konrad_Gourmet.pdf` y `Arquitectura_Konrad_GourmetOriginalSinCorregir.drawio`. La carpeta `documentos/reporte/` tiene el script y los textos con que se genera el reporte v2.

Los `.md` del historial llaman a la carpeta de código `KonradGourmet/`; aquí es `codigo/`. Este repo une dos repos locales y conserva la historia de commits de ambos.
