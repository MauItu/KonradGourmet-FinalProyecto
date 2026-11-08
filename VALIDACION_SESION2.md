# Validación de la sesión 2 (páginas 1, 2, 3a, 3b, 5 y 6) · 2026-11-08

Validó el agente `arquitecto-revisor` sobre la rama `correcciones-revision`. Los hallazgos nuevos se numeran S1–S11 para no confundirlos con H1–H26 (`REVISION_2026-11-07.md`) ni con N1–N15 (`VALIDACION_FASE2A.md`).

**Veredicto: lista con correcciones.** Hay un hallazgo de severidad media y diez bajos, casi todos de forma.

## Lo acordado se respetó

- **Inventario (`INVENTARIO_SESION2.md`, 149 ítems):**
  - están aplicados los 142 que pedían cambio;
  - P5-13 no necesitaba cambio (DEC-17);
  - los 6 que DEC-21 excluyó siguen sin tocar.
- **Coincidencia con 4a–4e:**
  - las 55 cajas de adaptador del mapa nombran clases que existen en 4a–4e;
  - las rutas, los 13 prefijos del gateway (DEC-12), la auditoría de 10 servicios y el PDF leído del almacén coinciden con esas páginas.
- **Mapa (p. 1):** 11 servicios y 10 tópicos, con 10 flechas de publicación y 19 de consumo.
- **Páginas 4a–4e:** no cambió ninguna celda, y cada página tiene el mismo SHA-256 que en `9c8c93d`.
- **Copia original:** `Arquitectura_Konrad_GourmetOriginalSinCorregir.drawio` conserva su SHA-256 (`10a49edd…`).
- **Formato del `.drawio`:** 1642 celdas, una por línea, y XML válido.

## Hallazgos nuevos

| # | Sev. | Dónde | Qué está mal | Corrección mínima que propone el revisor |
|---|---|---|---|---|
| S1 | Media | p. 6 `c6_v188`, «Consumidores completos» | Dice que dibuja los consumidores «de la tabla de eventos del reporte», pero esa tabla (p. 21) no tiene a ms-inventario ni a ms-cotizaciones | «…todos los consumidores de cada tópico según 4a–4e…» |
| S2 | Baja | 3a `c3a_e41` | La etiqueta pasó de 2 a 5 líneas y tapa toda la flecha de auditoría | Bajarla a 3 líneas y correrla a un lado |
| S3 | Baja | p. 5 `e47` | Por un desplazamiento (`offset`), la etiqueta flota a unos 120 px de su línea | Quitar el desplazamiento |
| S4 | Baja | p. 1 `v218` | El tronco del gateway (x=2425) corta el texto «/api/proveedores/**» | Partirla en 2 líneas de 190 px o menos |
| S5 | Baja | p. 1 `e225c` | La ruta entra por arriba al endpoint SOAP y cruza la cabecera de ms-integracion | Dejarla, o intercambiar `ad_integracion_t0` y `t1` (por decidir) |
| S6 | Baja | p. 6 `c6_l116` (12.1) | Es el único de los 68 mensajes con un texto más ancho que su flecha | «12.1 publica orden.generada (pdfUrl, [SOL-031])» |
| S7 | Baja | p. 6 `c6_v53` | Al ensancharla a 300 px tapa la línea de vida de ms-integracion | Ancho de 265 px o menos, en 4 líneas |
| S8 | Baja | p. 1 `db_objetos` | «[S3 compatible · PDF]» pone el contenido entre corchetes, contra el criterio de DEC-21 | «Almacén de objetos / [S3 compatible] / PDF» |
| S9 | Baja | p. 2 `v22` y `p2_sw2` | El gris de la persona externa casi no se distingue del de las personas internas | Otro relleno para la persona externa (por decidir) |
| S10 | Baja | 3b `c3b_e7` | Dice «delega generar()», pero en la 4c el método de la fachada es `generarYEnviar(c)`. Viene de antes de la sesión | «delega / generarYEnviar()» |
| S11 | Baja | `DECISIONES.md` | La tabla índice sigue diciendo «Por aplicar (sesión 2)». Además, DEC-16 omite dos publicadores que no apuntan a nada y DEC-19 cita el paso 6 | Actualizar los estados y ajustar esas dos frases |

El revisor no reportó lo que ya estaba antes de la sesión:
- el WebSocket `e231` cruza la cabecera de ms-notificaciones;
- en la p. 5, `e22` atraviesa la caja del DNS;
- en la p. 2, las flechas cruzan las descripciones de las personas.

## Seguimiento (2026-11-08)

Mauricio pidió al arquitecto-revisor que tomara las dos decisiones («toma la mejor decisión»):
- **DEC-23:** S5 se deja como está.
- **DEC-24:** el proveedor pasa a gris claro con borde gris.

Lo demás se aplica tal como lo propone el revisor.

| # | Estado | Commit |
|---|---|---|
| S1 | Aplicada: «…todos los consumidores de su tópico, según los diagramas de clases 4a–4e» | `9994836` |
| S2 | Aplicada: 3 líneas, a la derecha de la flecha (offset 114) | `c40b3f8` |
| S3 | Aplicada: sin offset y alineada a la izquierda, pegada a su línea. Centrada sobre la línea tapaba la línea JDBC vecina | `b3d1521` |
| S4 | Aplicada: 2 líneas de 190 px, subida 16 px para no tapar su ruta | `a9f4af1` |
| S5 | Decidida (DEC-23): sin cambio | — |
| S6 | Aplicada, con el texto propuesto | `6f467b9` |
| S7 | Aplicada: 262 px y 4 líneas | `4ffd96c` |
| S8 | Aplicada, con el texto propuesto | `57ba2d1` |
| S9 | Aplicada (DEC-24): relleno #DCDFE4 y borde #6B7079 | `3a039d2` |
| S10 | Aplicada | `9707d94` |
| S11 | Aplicada. También se pusieron al día los estados de DEC-05 a DEC-14, que seguían diciendo «sesión 1» | `cf9ae22` |

## Segunda validación (2026-11-08)

**Veredicto del revisor: lista con correcciones.**
- S1–S11 quedaron bien resueltos. El revisor revisó el diff de cada commit, y cada uno cambia solo la celda que nombra.
- DEC-23 y DEC-24 están bien registradas.
- Lo acordado se sigue respetando:
  - p3–p7 tienen el mismo SHA-256 que en `9c8c93d`;
  - el original no cambió;
  - el `.drawio` tiene 1642 celdas, una por línea;
  - el mapa tiene 11 servicios, 10 tópicos, 19 flechas de consumo y 10 de publicación;
  - están los 13 prefijos, la auditoría de 10 servicios y el PDF leído del almacén.
- `/api/integracion` está en el mapa y en la 3a, pero no en 4a–4e. Ya está planificado para la sesión 3 (D2 de la 4e).

Quedaban tres detalles de severidad baja, todos mecánicos:

| # | Dónde | Qué estaba mal | Estado | Commit |
|---|---|---|---|---|
| S12 | p. 5 `v46` y p. 6 `c6_v188` | Los diagramas citaban ids internos del registro, «(DEC-20)» y «DEC-18», que el profesor no conoce | Aplicada: se quitaron | `653c513` |
| S13 | p. 1 `v225`, 3a `c3a_p_proveedor`, p. 6 `c6_v16` y `c6_v154` | El proveedor solo estaba en gris claro en la p. 2, y cambiaba de color entre páginas | Aplicada: mismo color en las 4 celdas (DEC-24) | `61d1c71` |
| S14 | `DECISIONES.md`, DEC-24 | Decía que el C4 dibuja en gris claro lo externo, y no dejaba como opciones aparte el relleno blanco ni el gris de sistema externo | Aplicada: opciones A–D y frase corregida | `6487cbf` |

Como en la fase 2a, estos detalles menores se aplicaron sin otra ronda de validación completa. Cada commit pasó la comprobación automática: formato, 4a–4e, original, ids y referencias.

El revisor dejó una nota menor, sin acción: 5.1 dice «solicitudes: [SOL-031]» y 12.1 dice «[SOL-031]». Las dos coinciden con 4c y 4d.
