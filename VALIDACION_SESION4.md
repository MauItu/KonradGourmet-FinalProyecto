# Validación de la sesión 4 (demo real)

Revisó el agente `arquitecto-revisor` el 2026-11-08, sobre los 111 commits de `KonradGourmet` entre `37d655b` y `454aae2`, y el commit `3f26e62` de `documentos/`.

**Veredicto: lista con correcciones.** Los 111 commits compilan con `-Xlint:all` sin errores ni avisos; la demo cumple las 7 salidas del reporte (§13, p. 26) y pasan las 146 pruebas de entonces; no cambió ningún nombre ni firma de 4a–4e (se comparó con `javap -p`); los imports entre servicios cumplen DEC-29 (se comprobó con `jdeps`); los dos `.drawio` y el PDF no se tocaron, y el push sigue desactivado.

| # | Severidad | Qué pasaba | Estado |
|---|---|---|---|
| S4-1 | Alta | El cierre de cada noche volvía a publicar `inventario.stock-bajo` de todos los insumos bajo el umbral, aunque la alerta estuviera atendida | Corregido: solo al cruzar el umbral (DEC-46) |
| S4-2 | Alta | Una orden sobre una cotización que cubría solo parte de una solicitud dejaba sin completar las demás y sin pasar la cotización a CON_ORDEN | Corregido: `completar()` acepta PENDIENTE (DEC-46) |
| S4-3 | Media | Las escrituras del recorrido de compra no publicaban `auditoria.evento` | Corregido en 6 servicios (DEC-46) |
| S4-4 | Media | Faltaban pruebas (varias solicitudes, el día siguiente, eventos repetidos) y una decía algo que no probaba | Corregido, salvo eventos repetidos (queda anotado, S4-5): 159 verificaciones |
| S4-5 | Baja | Los consumidores no descartan eventos repetidos | Anotado en el README (límite) |
| S4-6 | Baja | La diferencia se redondea a 2 decimales antes de comparar | Anotado en DEC-43 y en el README |
| S4-7 | Baja | Proveedor B salía RECHAZADA en la demo y «mejores precios» en el tablero | Corregido: COT-019 y COT-020 son de Proveedor C y D |
| S4-8 | Baja | Comentarios «7. Singleton» y «8. Strategy» | Corregido |
| S4-9 | Baja | `parametrizar("x", null)` rompía después `tema()` | Corregido |
| S4-10 | Baja | (a) mesa activa validada con la que manda el cliente; (b) un número de cotización repetido la pisaba; (c) ENVIADA sin PDF si fallaba el almacén | (a) anotado en el README; (b) y (c) corregidos |
| S4-11 | Baja | El README decía que `compartido` tenía los 10 eventos | Corregido |
| S4-12 | Baja | El plan decía «Pendiente» en la sesión 4 | Corregido |

**Dudas del revisor para Mauricio** (las respuestas asumidas están en DEC-46 y DEC-40):
1. Una orden sobre una cotización parcial: ¿completa igual la solicitud (lo asumido) o debe quedar abierta?
2. Stock bajo: ¿una alarma al cruzar el umbral (lo asumido) o un recordatorio cada noche hasta que haya solicitud?
3. V18: ¿el director escribe el precio por kilo (lo que hace el código) o el valor de la línea (lo que muestra el mockup)?
4. Auditoría de cada escritura: ¿ahora (lo hecho) o como límite anotado?

**Después de las correcciones:** 126 commits sobre `37d655b` (111 revisados y 15 de arreglos) y 159 verificaciones bien; la segunda ronda del revisor está pendiente (se ofrece a Mauricio).
