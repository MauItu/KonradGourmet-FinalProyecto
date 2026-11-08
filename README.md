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
- **Aviso:** `documentos/ESTADO_PROYECTO_Y_C4.md` es del 7 de noviembre y está desactualizado; se actualiza en la sesión 5. El reporte PDF tampoco tiene todavía las correcciones.

## Cómo abrir cada cosa

- **Diagramas:** abrir `documentos/Arquitectura_Konrad_Gourmet.drawio` en [draw.io](https://app.diagrams.net) o en draw.io Desktop. `Arquitectura_Konrad_GourmetOriginalSinCorregir.drawio` es la versión original, antes de las correcciones.
- **Código** (Java 21, sin Maven):

  ```bash
  cd codigo
  javac --release 21 -d out $(find src -name "*.java")
  java -cp out co.edu.konradlorenz.demo.DemoArquitectura
  ```

## Historia

Este repo une dos repos locales y conserva la historia de commits de ambos.
