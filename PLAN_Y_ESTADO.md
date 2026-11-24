# Plan y estado del proyecto · Konrad Gourmet

Resumen único de **qué se planeó, qué se ha hecho y qué falta**. Es un mapa: el detalle de cada punto está en los documentos que se nombran. Estado a 2026-11-23, al terminar el paso 7d del corte 7.

- **Proyecto:** Sistema de Abastecimiento Konrad Gourmet, proyecto final de Arquitectura de Software (Fundación Universitaria Konrad Lorenz, semestre 2026-2, profesor Carlos Andrés López).
- **Equipo:** Mauricio Iturriza, Isabella Castro, Carlos Franco y Adrián Corredor.
- **Fecha de entrega final:** 23 de noviembre de 2026.
- **Arquitectura:** 11 microservicios y 10 tópicos de Kafka, orientada a eventos y hexagonal; REST, SOAP (ms-integracion), OAuth2/JWT, Eureka y balanceador.

## 1. El plan en una vista

| Etapa | Qué es | Estado |
|---|---|---|
| A. Revisión y correcciones (sesiones 1 a 6) | Corregir el `.drawio`, las historias de usuario, los casos de uso y el código base según la revisión del 2026-11-07 | Hecha y aprobada |
| B. Documentos de entrega (post sesión 6) | Reporte v2, Story Mapping corregido, casos de uso, decisiones de arquitectura (PD-01 a PD-11) | Hecha y aprobada; el reporte se regeneró en el 7d |
| C. Plataforma real (rama `stack-real`) | La web funcionando completa «tal como está especificada», en Docker Compose, en 7 cortes | Cortes 1 a 6b aprobados; del corte 7, 7a a 7c aprobados y el 7d hecho, esperando el OK |
| D. Cierre y sustentación | Reporte regenerado, pruebas finales, guiones de la sustentación | Reporte y pruebas hechos (7b y 7d); faltan los guiones |

Reglas de trabajo vigentes: un commit por corrección con su id al inicio; un paso por mensaje y esperar el OK de Mauricio; cada decisión se registra en `DECISIONES.md` con contexto, opciones y la escogida; todo cambio frente a mockups, diagramas o guías se anota en `CAMBIOS_A_DOCUMENTAR.md`; la documentación entregable describe el proyecto completo, sin hablar de límites de la demo (DEC-57); la IA solo se menciona en la §18 del reporte; ningún texto de la entrega dice que los mockups de `mockups_faltantes/` son capturas; sin push hasta que Mauricio lo pida.

## 2. Lo que ya está hecho

### Etapa A · Revisión y correcciones

- Dos repos: `documentos/` (diagramas, reporte, decisiones) y `KonradGourmet/` (código).
- `REVISION_2026-11-07.md`: 26 hallazgos (H1 a H26); decisiones en `DECISIONES.md` (hoy DEC-01 a DEC-106).
- Sesión 1: páginas de clases 4a a 4e del `.drawio` (191 clases, hoy 195) y validación (`VALIDACION_FASE2A.md`).
- Sesiones 2 a 5: código hexagonal con sus verificaciones, Excel de capacidad, historias de usuario y mockups (`CORRECCIONES_HU_Y_MOCKUPS.md`), `ESTADO_PROYECTO_Y_C4.md`. Cada sesión con su `VALIDACION_SESIONn.md` del arquitecto revisor.
- Sesión 6: `meseroId` en `Pedido` (DEC-51) y casos de uso corregidos (DEC-52).
- Detalle y avance por sesión: `PLAN_CORRECCIONES.md`.

### Etapa B · Documentos de entrega

- Reporte v2 (`Reporte_Arquitectura_Konrad_Gourmet_v2.pdf` y `.docx`, 69 páginas), generado con `reporte/generar_reporte.py` desde `reporte/contenido/`; el original se conserva intacto. Regenerado en el 7d con 195 clases, todos los CD que lo tocan, la outbox, la idempotencia y el límite de tasa (DEC-104), las 1.254 comprobaciones, el diagrama de casos de uso como figura 7 y las figuras del `.drawio` vigente (DEC-106).
- `STORY_MAPPING_CORREGIDO.md` aprobado por Mauricio (2026-11-09); el original no se toca.
- `Casos de uso — Konrad Gourmet.md` corregido (RCU-1 a RCU-13 y los CD del corte 7) y su diagrama `casos_de_uso.png` (DEC-92), generado con `diagrama_casos_de_uso/generar.py`.
- Decisiones de arquitectura PD-01 a PD-11 y siete supuestos confirmados (`REVISION_ARQUITECTO_POST_S6.md`).
- Mockups: los del administrador y los cambios a V05, V07, V14, V20 y V23 en `mockups_admin/`; el tablero BAM, las promociones, proveedores, editar y desactivar platos, reenviar cotización y M-01 a M-09 en `mockups_faltantes/` (DEC-93, DEC-94 y DEC-96). Ninguna ventana queda sin mockup.
- Repo público con README de entregables: `github.com/MauItu/KonradGourmet-FinalProyecto` (última publicación el 9 de noviembre).

### Etapa C · Plataforma real (`stack-real`)

Stack: Docker Compose con 11 microservicios Spring Boot 3.5 y Spring Cloud 2025.0, gateway, Eureka, Kafka KRaft, PostgreSQL (una base y un usuario por servicio), Redis, SeaweedFS (S3), Elasticsearch, Mailpit y Nginx; SPA en React + TypeScript. Detalle en `plataforma/LEEME.md` y `README.md` de `KonradGourmet/`.

| Corte | Contenido | Estado |
|---|---|---|
| 1 | Infraestructura, Eureka, gateway, Nginx | Aprobado |
| 2 | ms-identidad (login JWT, menú por rol) y la SPA | Aprobado |
| 3 | ms-pedidos, ms-menu, ms-inventario (V02 a V07) | Aprobado |
| 4a | ms-solicitudes (V08 a V12) y ms-integracion (SOAP, caché en Redis) | Aprobado |
| 4b | ms-cotizaciones (V13 a V19, proveedores, PDF) | Aprobado |
| 5 | ms-ordenes y ms-notificaciones (V20 a V23, correos con PDF, alarma en vivo, reproceso de la cola de errores) | Aprobado |
| 6a | ms-administracion (auditoría, parámetros y tema, logs con Elasticsearch, usuarios, destinatarios) | Aprobado |
| 6b | ms-analitica (tablero BAM, promociones sugeridas, `tendencia.detectada`, PD-05) | Aprobado |
| 7 | Plan (DEC-95) en cuatro pasos, uno por mensaje | 7a, 7b y 7c aprobados; 7d hecho, esperando el OK |

El corte 7, paso por paso:

| Paso | Qué | Estado |
|---|---|---|
| 7a | Documentación pendiente fuera del reporte (casos de uso, `.drawio`, `ESTADO_PROYECTO_Y_C4.md`), mockups de todo lo que faltaba (DEC-96) y mensajes de las historias en la aplicación (DEC-97) | Aprobado |
| 7b | Coma decimal (DEC-98); historia de punta a punta (`recorrido_corte7.py` y `verificar_corte7.py`) y `probar_todo.sh` (DEC-99); clon limpio | Aprobado |
| 7c | Revisión del arquitecto revisor (`VALIDACION_CORTE7.md`, C7-1 a C7-27) y sus correcciones: DEC-100 a DEC-105 (Solicitar con el producto, `reactivar()` en el diagrama, V05 y el correo con la sucursal, V11 de a 25, outbox con relé, consumidores idempotentes y límite de tasa) | Aprobado |
| 7d | README y LEEME finales (C7-18, C7-25, DEC-96 a DEC-105, `probar_todo.sh`), `ESTADO_PROYECTO_Y_C4.md` (C7-18), el reporte regenerado y este documento (DEC-106) | Hecho, esperando el OK |

**Pruebas** (`plataforma/probar_todo.sh`, sobre una instancia limpia): **1.254 comprobaciones sin fallas** = 400 verificaciones del núcleo (`pruebas/`, 8 clases) + 91 pruebas JUnit de los servicios + 763 de los recorridos de navegador y los verificadores de los cortes 3 a 7.

## 3. Lo que falta

### Para cerrar el corte 7

- [ ] OK de Mauricio al 7d y confirmación de DEC-106 (detalles del 7d).

### Para Mauricio y el equipo

- [ ] Guiones de la sustentación: se rehacen al final del proyecto. Los anteriores y los entregables viejos de Drive quedaron en `FinalProyecto/archivo_desactualizado/` (fuera de git).
- [ ] Publicar en el repo público lo posterior al 9 de noviembre, cuando Mauricio lo pida.
- [ ] Opcionales sin aplicar al reporte: R-14, R-19, R-39 y R-53 (Mauricio decide).

### Ya cerrado desde el corte 6b

- [x] Corte 6b aprobado y DEC-91 confirmada (2026-11-23).
- [x] DEC-78 y DEC-82 confirmadas al aprobar el corte 5.
- [x] La leyenda del eje del proveedor del tablero BAM, en una sola línea (7a).
- [x] El reporte regenerado con 195 clases, las rutas, los eventos y los supuestos de `CAMBIOS_A_DOCUMENTAR.md`, sin mencionar límites de la demo (7d). Ningún CD queda pendiente.
- [x] DEC-56 en la tabla de resumen de `DECISIONES.md` (7a).
- [x] Los números reales de pruebas y clases en el reporte, el README y el LEEME (7d).
- [x] Mockups M-01 a M-09, CD-27, CD-28 y las ventanas nuevas (fase 2 y 7a; DEC-93, DEC-94 y DEC-96).
- [x] Diagrama UML de los casos de uso: `casos_de_uso.png` (DEC-92), también en el reporte (figura 7).
- [x] Nombre del director comercial: Andrés Rojas (DEC-63). Fecha de entrega: 23 de noviembre.

## 4. Dónde está cada cosa

| Qué | Dónde |
|---|---|
| Decisiones (DEC-01 a DEC-106) y su historia | `documentos/DECISIONES.md` |
| Plan de las sesiones 1 a 6 con su avance | `documentos/PLAN_CORRECCIONES.md` |
| Pendientes después de la sesión 6 | `documentos/PENDIENTES_POST_SESION6.md` |
| Cambios de la plataforma frente a la documentación (CD-01 a CD-73) | `documentos/CAMBIOS_A_DOCUMENTAR.md` |
| Diagramas C4 y de clases | `documentos/Arquitectura_Konrad_Gourmet.drawio` (original sin corregir aparte) |
| Reporte | `documentos/Reporte_Arquitectura_Konrad_Gourmet_v2.pdf` y `.docx`; fuente en `documentos/reporte/` |
| Estado y C4 | `documentos/ESTADO_PROYECTO_Y_C4.md` |
| Historias corregidas | `documentos/STORY_MAPPING_CORREGIDO.md` |
| Casos de uso y su diagrama | `documentos/Casos de uso — Konrad Gourmet.md` y `documentos/casos_de_uso.png` |
| Mockups nuevos | `documentos/mockups_admin/` y `documentos/mockups_faltantes/` |
| Validaciones del arquitecto revisor | `documentos/VALIDACION_*.md` (la última, `VALIDACION_CORTE7.md`), `REVISION_*.md` |
| Plataforma, cómo levantarla y probarla | `KonradGourmet/plataforma/LEEME.md` y `KonradGourmet/README.md` |
| Servicios, SPA y pruebas de punta a punta | `KonradGourmet/servicios/`, `KonradGourmet/web/` y `KonradGourmet/plataforma/pruebas/` |

## 5. Estado de los repos

- `documentos/`: rama `correcciones-revision` (sin remoto propio; se publica en el repo público como `documentos/`).
- `KonradGourmet/`: rama `stack-real` (sale de `correcciones-revision`); el remoto de `iCastro117` tiene el push desactivado a propósito.
- Repo público: `MauItu/KonradGourmet-FinalProyecto`, rama `main`, que une las dos carpetas (`documentos/` y `codigo/`) con la historia de ambos. No tiene nada de la plataforma todavía.
