# Konrad Gourmet · Proyecto final de Arquitectura de Software

Sistema de Abastecimiento Konrad Gourmet. Fundación Universitaria Konrad Lorenz, Arquitectura de Software, 2026-2.

Equipo: Mauricio Iturriza, Isabella Castro, Carlos Franco y Adrián Corredor.

## Qué hay en el repo

| Carpeta | Contenido |
|---|---|
| `documentos/` | Diagramas en `Arquitectura_Konrad_Gourmet.drawio` (11 páginas: mapa, C4 niveles 1 a 4, despliegue y dinámico), el reporte PDF, los mockups (`DISEÑO UXUI.pdf`), el flujo web, el Excel de capacidad y los `.md` de la revisión y las correcciones |
| `codigo/` | Esqueleto Java hexagonal de los 11 microservicios y la demo. Es el repo del equipo [`iCastro117/KonradGourmet`](https://github.com/iCastro117/KonradGourmet), con las correcciones aplicadas encima |

Los `.md` de `documentos/` llaman a la carpeta de código `KonradGourmet/`; aquí es `codigo/`.

## Por dónde empezar

- **Estado de las correcciones:** `documentos/PLAN_CORRECCIONES.md`, tabla «Avance» al final.
- **Decisiones tomadas:** `documentos/DECISIONES.md`.
- **Estado del proyecto:** [ESTADO_PROYECTO_Y_C4.md](documentos/ESTADO_PROYECTO_Y_C4.md), actualizado al cierre de la sesión 6 (8 de noviembre de 2026).
- **Validación de la sesión 6:** [VALIDACION_SESION6.md](documentos/VALIDACION_SESION6.md): pedido con `meseroId`, 176 verificaciones y 19 casos de uso revisados.
- **Pendientes de entrega:** falta `documentos/casos_de_uso.png`, que debe exportar el compañero, y aplicar al PDF las correcciones de `CORRECCIONES_REPORTE.md` cuando esté disponible su script.

## Cómo abrir cada cosa

- **Diagramas:** abrir `documentos/Arquitectura_Konrad_Gourmet.drawio` en [draw.io](https://app.diagrams.net) o en draw.io Desktop. `Arquitectura_Konrad_GourmetOriginalSinCorregir.drawio` es la versión original, antes de las correcciones.
- **Código** (Java 21, sin Maven):

  ```bash
  cd codigo
  ./ejecutar.sh
  ```

## Historia

Este repo une dos repos locales y conserva la historia de commits de ambos.
