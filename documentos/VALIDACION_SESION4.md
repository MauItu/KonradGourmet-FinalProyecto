# Validación de la sesión 4 (demo real)

## Primera ronda (registro histórico)

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


## Segunda ronda · revisión y correcciones autorizadas (2026-11-08)

**Veredicto de la revisión: lista con correcciones.** Se revisaron `454aae2..680f2d5` de KonradGourmet y los commits `b01e555` y `644c4f2` de documentos. Se compilaron los 20 commits nuevos, el punto inicial `454aae2` y la referencia `37d655b`, extraídos con `git archive` en carpetas temporales: `javac --release 21 -Xlint:all` sobre fuentes y pruebas, sin errores ni avisos. La demo cumplió las siete salidas del §13 según DEC-31 y pasaron las 160 verificaciones de esa versión.

El arnés externo añadió 29 comprobaciones: cierre repetido, consumo de res al día siguiente con alerta atendida y sin atender, reenvíos sin duplicar valores ni EN_TRAMITE, dos solicitudes (una realmente parcial), CON_ORDEN y segunda orden rechazada, adjuntos, parámetros nulos, número repetido, fallos y recuperación. Reprodujo también los límites documentados: eventos duplicados, mesa validada con datos del cliente y redondeo. Encontró cero eventos de auditoría al guardar PENDIENTE_PRECIO por fallo del precio o del PDF.

| # | Severidad | Evidencia en la versión revisada | Qué corregir | Resultado de la fase 2 |
|---|---|---|---|---|
| S4-13 | Media | `GenerarCotizacionFacade.java`, líneas 57–74: ambos caminos de fallo guardaban sin publicar auditoría | Auditar las escrituras pendientes e incluir la causa | Corregido en `3d85fc5`: cotización, estado, usuario `sistema` y causa del fallo de precio o PDF; pruebas del fallo del almacén y del reenvío al recuperarse |
| S4-14 | Baja | `PruebasFlujoDeCompra.java`, líneas 84–91: el ajiaco no consume res | Usar Bandeja paisa y comprobar el descuento de res | Corregido en `988ad42`: la res baja de 11,5 a 11 kg, sin otra alarma ni reactivación de la alerta atendida |
| S4-15 | Baja | `README.md`, línea 148: decía tres proveedores y la demo carga cuatro | Actualizar el conteo | Corregido en `4b4a308`, cierre de conteos del README |
| S4-16 | Baja | `PLAN_CORRECCIONES.md`, línea 158: total anterior; `DECISIONES.md`, líneas 56, 61 y 710: estados y número de firmas ambiguos | Actualizar total, distinguir confirmaciones y describir dos contratos en cuatro declaraciones | Estados y contratos corregidos en `dc85d36`; total actualizado en el cierre de conteos |

**Estado de los hallazgos anteriores:** S4-1 y S4-2 comprobados; S4-3 se completa con S4-13 y S4-4 con S4-14. S4-5, S4-6 y S4-10(a) continúan como límites documentados. S4-7, S4-8, S4-9, S4-10(b/c), S4-11 y S4-12 están corregidos. `reenviar()` no duplica el valor de las solicitudes ni su aviso EN_TRAMITE, aunque sí reenvía el correo, como se espera.

**Contratos y arquitectura:** `javap -p` sobre las 191 clases frente a `37d655b` confirmó que DEC-42 es el único cambio de contrato, aparte de constructores y accesores autorizados por DEC-25, auxiliares privados y `synchronized`. Son `Notificacion.enviar()`, sus implementaciones `AlarmaApp` y `NotificacionCorreo`, y `CreadorNotificacion.notificar()`. Todas reciben `adjuntoUrl`; el correo lo conserva en `Mensaje.adjuntoUrl`. Las 191 clases tienen caja en 4a–4e. Imports y `jdeps` comprobaron 13 dependencias entre servicios, todas permitidas por DEC-29. Los dos `.drawio` y el PDF no tienen cambios desde `ac6d500`.

**Decisiones de Mauricio:** mantuvo el redondeo previo a dos decimales en cotizaciones y stock (DEC-43), y confirmó `sistema` o correo de la entidad y una correlación por evento como límite de la demo (DEC-42). La sesión 5 debe corregir el «≤ 25 %» del reporte (p. 21), aclarar el umbral en p. 9 y distinguir las promesas de usuario y correlación del reporte (pp. 5 y 9) y de la nota `n_outbox` de la 4a. No se modificaron esos artefactos.

**Cierre de la fase 2: lista dentro del alcance de la demo.** Pasan 171 verificaciones; permanecen 94 cuerpos pendientes de implementar. Los commits `3d85fc5`, `988ad42` y `4b4a308`, extraídos individualmente con `git archive`, compilan sin errores ni avisos con `--release 21 -Xlint:all` (fuentes y pruebas). `./ejecutar.sh` sobre la copia temporal de `4b4a308` termina con código 0. El total es 134 commits sobre `37d655b`. Las declaraciones de las 191 clases del modelo, comparadas con `javap -p`, son idénticas a `680f2d5`: esta fase no cambia firmas. La demo mantiene sus siete salidas. Los mensajes WARNING de las pruebas de fallos son esperados en ejecución, no avisos de compilación.

**Trabajo local:** ambos repositorios permanecen en `correcciones-revision`, sin push; código conserva `DESACTIVADO-trabajo-solo-local` y documentos no tiene remoto. Los cambios de conteos se concentran al final, en un commit por repositorio, porque Git no permite un solo commit que abarque ambos. El historial previo agrupaba algunos cambios (por ejemplo S4-7/S4-8); no se reescribió. Sesión 5 sigue pendiente.
