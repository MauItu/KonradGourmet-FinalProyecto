# Plan y estado del proyecto · Konrad Gourmet

Resumen único de **qué se planeó, qué se ha hecho y qué falta**. Es un mapa: el detalle de cada punto está en los documentos que se nombran. Estado a 2026-11-23.

- **Proyecto:** Sistema de Abastecimiento Konrad Gourmet, proyecto final de Arquitectura de Software (Fundación Universitaria Konrad Lorenz, semestre 2026-2, profesor Carlos Andrés López).
- **Equipo:** Mauricio Iturriza, Isabella Castro, Carlos Franco y Adrián Corredor.
- **Fecha de entrega final:** 23 de noviembre de 2026.
- **Arquitectura:** 11 microservicios y 10 tópicos de Kafka, orientada a eventos y hexagonal; REST, SOAP (ms-integracion), OAuth2/JWT, Eureka y balanceador.

## 1. El plan en una vista

| Etapa | Qué es | Estado |
|---|---|---|
| A. Revisión y correcciones (sesiones 1 a 6) | Corregir el `.drawio`, las historias de usuario, los casos de uso y el código base según la revisión del 2026-11-07 | Hecha y aprobada |
| B. Documentos de entrega (post sesión 6) | Reporte v2, Story Mapping corregido, casos de uso, decisiones de arquitectura (PD-01 a PD-11) | Casi toda hecha; quedan puntos sueltos (sección 3) |
| C. Plataforma real (rama `stack-real`) | La web funcionando completa «tal como está especificada», en Docker Compose, en 7 cortes | 6 de 7 cortes hechos y aprobados; falta el corte 7 |
| D. Cierre y sustentación | Reporte regenerado, pruebas finales, guiones de la sustentación | Pendiente |

Reglas de trabajo vigentes: un commit por corrección con su id al inicio; un corte por mensaje y esperar el OK de Mauricio; cada decisión se registra en `DECISIONES.md` con contexto, opciones y la escogida; todo cambio frente a mockups, diagramas o guías se anota en `CAMBIOS_A_DOCUMENTAR.md`; la documentación entregable describe el proyecto completo, sin hablar de límites de la demo (DEC-57).

## 2. Lo que ya está hecho

### Etapa A · Revisión y correcciones

- Dos repos: `documentos/` (diagramas, reporte, decisiones) y `KonradGourmet/` (código).
- `REVISION_2026-11-07.md`: 26 hallazgos (H1 a H26); decisiones DEC-01 a DEC-57 en `DECISIONES.md`.
- Sesión 1: páginas de clases 4a a 4e del `.drawio` (191 clases) y validación (`VALIDACION_FASE2A.md`).
- Sesiones 2 a 5: código hexagonal con sus verificaciones, Excel de capacidad, historias de usuario y mockups (`CORRECCIONES_HU_Y_MOCKUPS.md`), `ESTADO_PROYECTO_Y_C4.md`. Cada sesión con su `VALIDACION_SESIONn.md` del arquitecto revisor.
- Sesión 6: `meseroId` en `Pedido` (DEC-51) y casos de uso corregidos (DEC-52).
- Detalle y avance por sesión: `PLAN_CORRECCIONES.md`.

### Etapa B · Documentos de entrega

- Reporte v2 regenerado (61 páginas, PDF y docx) con `reporte/generar_reporte.py`; el original se conserva intacto.
- `STORY_MAPPING_CORREGIDO.md` aprobado por Mauricio (2026-11-09); el original no se toca.
- `Casos de uso — Konrad Gourmet.md` corregido contra los 13 problemas de `REVISION_CASOS_DE_USO.md`.
- Decisiones de arquitectura PD-01 a PD-11 y siete supuestos confirmados (`REVISION_ARQUITECTO_POST_S6.md`).
- Mockups del administrador y de las ventanas nuevas en `mockups_admin/`.
- Repo público con README de entregables: `github.com/MauItu/KonradGourmet-FinalProyecto`.

### Etapa C · Plataforma real (`stack-real`)

Stack: Docker Compose con 11 microservicios Spring Boot 3.5 y Spring Cloud 2025.0, gateway, Eureka, Kafka KRaft, PostgreSQL (una base y un usuario por servicio), Redis, SeaweedFS (S3), Elasticsearch, Mailpit y Nginx; SPA en React + TypeScript. Detalle en `plataforma/LEEME.md`.

| Corte | Contenido | Estado |
|---|---|---|
| 1 | Infraestructura, Eureka, gateway, Nginx | Hecho |
| 2 | ms-identidad (login JWT, menú por rol) y la SPA | Hecho |
| 3 | ms-pedidos, ms-menu, ms-inventario (V02 a V07) | Hecho y aprobado |
| 4a | ms-solicitudes (V08 a V12) y ms-integracion (SOAP, caché en Redis) | Hecho y aprobado |
| 4b | ms-cotizaciones (V13 a V19, proveedores, PDF) | Hecho y aprobado |
| 5 | ms-ordenes y ms-notificaciones (V20 a V23, correos con PDF, alarma en vivo, reproceso de la cola de errores) | Hecho |
| 6a | ms-administracion (auditoría, parámetros y tema, logs con Elasticsearch, usuarios, destinatarios) | Hecho y aprobado |
| 6b | ms-analitica (tablero BAM, promociones sugeridas, `tendencia.detectada`, PD-05) | Hecho y aprobado (2026-11-23) |
| 7 | Pruebas de punta a punta, README y revisión | **Pendiente** |

Estado de las pruebas al corte 6b: 398 verificaciones en 8 clases, 86 pruebas JUnit, y los recorridos de navegador (`recorrido_corteN.py` y `verificar_corteN.py`) pasan también en un clon limpio.

## 3. Lo que falta

### Para cerrar la plataforma

- [x] (2026-11-23) Mauricio aprobó el corte 6b y confirmó DEC-91 (detalles asumidos por Claude: API de redes simulada, `TendenciasJob`, reglas de las sugerencias, correo, tablero, histórico de la demostración y ventanas sin mockup).
- [ ] Revisar que DEC-78 y DEC-82 (detalles del corte 5, asumidos por Claude) estén confirmadas en `DECISIONES.md`.
- [ ] **Corte 7:** pruebas de punta a punta con todos los servicios, README y LEEME finales, revisión del arquitecto revisor sobre la plataforma completa.
- [ ] Detalle visual: la leyenda del eje del proveedor se parte en dos líneas en el tablero BAM.

### Para los documentos

- [ ] Regenerar el reporte en el corte 7: 195 clases (la §13 dice 193 o 195 según la versión), las rutas, los eventos y los supuestos de `CAMBIOS_A_DOCUMENTAR.md` (CD-01 a CD-35), sin mencionar límites de la demo (DEC-57).
- [ ] Aplicar a los casos de uso, al `.drawio` y al reporte lo que `CAMBIOS_A_DOCUMENTAR.md` aún marca como *pendiente*.
- [ ] Marcar DEC-56 en la tabla de resumen de `DECISIONES.md`.
- [ ] Reportar en el reporte, en el README y en las pruebas los números reales (verificaciones, clases) cuando se cierre el corte 7.

### Para Mauricio y el equipo

- [ ] Mockups: M-01 a M-09 de `CORRECCIONES_HU_Y_MOCKUPS.md` (V13, V21, V17 y V19 a V23, V05, V03/V04) y dibujar los de las ventanas nuevas CD-27 y CD-28 (tablero BAM y promociones sugeridas) con las capturas de `capturas_referencia/corte6b/`.
- [x] (2026-11-23) Diagrama UML de los casos de uso: `casos_de_uso.png`, generado desde el md con `diagrama_casos_de_uso/generar.py` (DEC-92); la nota «Pendiente» del md se quitó.
- [x] Nombre del director comercial: Andrés Rojas (DEC-63; Mauricio, 2026-11-23).
- [ ] Guiones de la sustentación: se rehacen al final del proyecto. Los anteriores y los entregables viejos de Drive quedaron en `FinalProyecto/archivo_desactualizado/` (fuera de git).
- [ ] Opcionales sin aplicar al reporte: R-14, R-19, R-39 y R-53.
- [x] Fecha de entrega: 23 de noviembre (Mauricio, 2026-11-23).

## 4. Dónde está cada cosa

| Qué | Dónde |
|---|---|
| Decisiones (DEC-01 a DEC-91) y su historia | `documentos/DECISIONES.md` |
| Plan de las sesiones 1 a 6 con su avance | `documentos/PLAN_CORRECCIONES.md` |
| Pendientes después de la sesión 6 | `documentos/PENDIENTES_POST_SESION6.md` |
| Cambios de la plataforma frente a la documentación (CD-01 a CD-35) | `documentos/CAMBIOS_A_DOCUMENTAR.md` |
| Diagramas C4 y de clases | `documentos/Arquitectura_Konrad_Gourmet.drawio` (original sin corregir aparte) |
| Reporte | `documentos/Reporte_Arquitectura_Konrad_Gourmet_v2.pdf` y `.docx`; fuente en `documentos/reporte/` |
| Estado y C4 | `documentos/ESTADO_PROYECTO_Y_C4.md` |
| Historias corregidas | `documentos/STORY_MAPPING_CORREGIDO.md` |
| Casos de uso | `documentos/Casos de uso — Konrad Gourmet.md` |
| Validaciones del arquitecto revisor | `documentos/VALIDACION_*.md`, `REVISION_*.md` |
| Plataforma, cómo levantarla y probarla | `KonradGourmet/plataforma/LEEME.md` y `KonradGourmet/README.md` |
| Servicios y SPA | `KonradGourmet/servicios/` y `KonradGourmet/web/` |

## 5. Estado de los repos

- `documentos/`: rama `correcciones-revision` (sin remoto propio; se publica en el repo público como `documentos/`).
- `KonradGourmet/`: rama `stack-real` (sale de `correcciones-revision`); el remoto de `iCastro117` tiene el push desactivado a propósito.
- Repo público: `MauItu/KonradGourmet-FinalProyecto`, rama `main`, que une las dos carpetas (`documentos/` y `codigo/`) con la historia de ambos.
