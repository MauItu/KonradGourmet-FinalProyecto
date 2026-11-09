# Validación final de la sesión 5 · 2026-11-08

Validó el agente `arquitecto-revisor` sobre la rama `correcciones-revision` de los dos repos (documentos `7964da6`, código `c690d82`), con los hallazgos numerados S5-1 a S5-15 para no confundirlos con H1–H26, N1–N15, S1–S14 ni K1–K18.

**Veredicto del revisor: lista con correcciones.** La sesión 5 cumple lo que prometía; lo que fallaba eran dos correcciones del reporte y textos de apoyo. Todos los hallazgos se atendieron el mismo día (ver abajo), salvo uno que depende de una decisión de Mauricio.

## Lo que el revisor verificó y salió bien

1. **`.drawio`.** Respecto de `b851f4a` solo cambiaron la nota `n_outbox` (4a) y las cuatro clases y ocho flechas de la 4d. XML válido, 1644 celdas, una por línea. Las cajas, flechas y clases por página coinciden con `ESTADO_PROYECTO_Y_C4.md` (191 clases en total). Las 191 clases y los 403 métodos de 4a–4e coinciden con `javap -p`, incluidos los cuatro `adjuntoUrl`. Las PNG de `exportes/sesion5/` muestran el estado actual.
2. **Código.** Entre `4b4a308` y HEAD solo cambiaron el README y el título de la demo. `./ejecutar.sh` en una copia temporal compila con `--release 21 -Xlint:all` sin avisos, pasa 171 de 171 y la demo da +4,81 % → OPCIONADA, COT-019 a $700.000 y COT-020 a $200.000. Cifras confirmadas: 94 cuerpos pendientes, 211 archivos en `src/` (20 en `demo/`), 6 archivos de prueba, 29 comentarios «Atiende la ventana» que cubren las 23 ventanas y 13 prefijos.
3. **Excel.** Las 67 fórmulas recalculan igual en LibreOffice: 35 PHU repartidos 8, 8, 6, 8 y 5, con 10 sobrantes; sprints del lunes 03/08 al 11/10/2026; 10 semanas y 2,5 meses. Los PHU de cada HU coinciden con el Planning Poker.
4. **`CORRECCIONES_REPORTE.md`.** Los «textos actuales» existen en la página citada (salvo R-46), y cubre todo lo que pide el plan de la sesión 5, incluidas W-24 a W-35.
5. **Mockups y Story Mapping.** DEC-49 es cierta; HU-07 es copia literal de HU-06; la p. 31 dice 4 sprints, 8 semanas y «10 sobre 9»; los valores de V03/V04, V07, V13, V18 y V19 coinciden con lo anotado.
6. **E1–E3, D1–D8, D10 y D12:** resueltos y comprobados en el `.drawio` y el código. D9 y D11 siguen pendientes (tarjeta HU-07 y script del reporte).

## Hallazgos y cómo se atendieron

| # | Sev. | Hallazgo | Estado |
|---|---|---|---|
| S5-1 | Alta | R-46 citaba como «texto actual» de la p. 26 un texto que el PDF no tiene; el PDF ya dice lo correcto. `ESTADO` repetía que el reporte tenía una salida «mal copiada» | ✅ R-46 queda como mejora opcional (signo y totales); se borró la nota falsa y la frase de `ESTADO` (`49f7dd2`, `c1406eb`). **Error mío:** extraje mal el PDF con `pdftotext -layout`, que partió la tabla; el revisor lo comprobó con `-raw` |
| S5-2 | Alta | R-24 quitaba la condición «si todos los productos ya tienen cotización» para pasar a EN TRÁMITE; el código (`todoCotizado()`) y HU-12 la mantienen | ✅ R-24 y M-01 la conservan (`49f7dd2`, `e15e262`) |
| S5-3 | Media | `ESTADO` decía que el reporte aclararía que los consumidores no descartan eventos repetidos; ninguna R lo decía | ✅ R-45 lo dice (`49f7dd2`). La nota `n_outbox` de la 4a no se tocó: el límite ya está en el README |
| S5-4 | Media | R-02 listaba el balanceador en la 3a, que no lo dibuja, y omitía el servidor web | ✅ Dice «servidor web (Nginx)» (`49f7dd2`) |
| S5-5 | Media | `ESTADO` no mencionaba DEC-49, ni V20–V22, ni DEC-49 entre los supuestos | ✅ Agregado (`c1406eb`) |
| S5-6 | Media | V05 trae Arroz y Pollo al 25 %, que con DEC-43 (< 25 %) no alertan, y la tarjeta dice «4 stock bajo» con 5 filas | ✅ Anotado en M-06 (`e15e262`) |
| S5-7 | Media | Excel: F5 y F6 en orden contrario al Story Mapping (HU-05 ingredientes, HU-06 consultar), la marca MVP2 en la fila equivocada y F18 decía «de compras» | ✅ Corregido en el Excel (`03e8914`); los PHU no cambian |
| S5-8 | Baja | DEC-05 y DEC-07 seguían con «falta… (sesión 5)»; filas DEC-45 y DEC-46 en desorden; la Fase 4 sin ✅ | ✅ (`46ec1ef`) |
| S5-9 | Baja | El Excel y S-06 decían «al llegar al umbral del 25 %», que R-22 corrige | ✅ «al bajar del 25 %» en los dos (`03e8914`, `e15e262`) |
| S5-10 | Baja | Cuatro imprecisiones en `CORRECCIONES_HU_Y_MOCKUPS.md` (7 criterios y no 8; la p. 28 y no la 29; nombres de proveedor en V22 y V18; el reporte no cita HU) | ✅ (`e15e262`) |
| S5-11 | Baja | DEC-49 decía que el reporte une OC-0012 con COT-018, y DEC-47 atribuía el «(120)» a cuatro páginas | ✅ DEC-49 ahora dice lo que de verdad dicen la demo, la p. 6 y el reporte; el «(120)» está solo en la p. 30 (`46ec1ef`) |
| S5-12 | Baja | La prueba seguía rotulada «Observer (Publish-Subscribe)» | ✅ Código `4a64164`; 171 verificaciones siguen pasando |
| S5-13 | Baja | Una «S» suelta en la celda M19 del Excel (ya venía del original) | ✅ Borrada (`03e8914`) |
| S5-14 | Baja | Carpeta vacía `c` en `documentos/` y `Casos de uso — Konrad Gourmet.md`, fuera de los repos, desactualizado | 🟡 La carpeta se borró. **El archivo de casos de uso queda a decisión de Mauricio** (ver abajo) |
| S5-15 | Baja | V03 saluda «Carlos Mendoza · Jefe de Cocina» en la pantalla del mesero | ✅ M-09 (`e15e262`) |

## Lo que el revisor no pudo verificar

- El repo público (`195b4b2`): no hay red.
- Las 11 PNG completas; solo revisó las zonas que cambiaron.
- Las tarjetas HU-01–05, 08, 10, 11, 13, 14, 16 y 17, ni el PDF del flujo web.
- Los modelos que nombra R-58 de `CORRECCIONES_REPORTE.md` (la declaración de uso de IA): el equipo debe confirmar esa redacción.

## Para Mauricio

1. **`Casos de uso — Konrad Gourmet.md`** está en `FinalProyecto/`, fuera de los repos. Su cabecera dice «274 métodos… ningún caso de uso está implementado», que ya no es cierto (quedan 94 cuerpos pendientes y el recorrido de compra se ejecuta). Si ese archivo se entrega o se comparte con el compañero que haga los casos de uso, hay que actualizarlo; si es solo un borrador, no hace falta.
2. **V05:** ¿bajan Arroz y Pollo al 24 % o salen de la lista de alertas? (M-06).
3. **DEC-49** sigue asumida: ¿V20–V22 se alinean con la demo (COT-018, $545.000), o al revés?

**Cierre.** Con los 15 hallazgos atendidos, la sesión 5 queda lista a la espera del OK de Mauricio. El repo público (`195b4b2`) tiene la sesión 5 hasta el título de la demo; **todo lo posterior (los `CORRECCIONES_HU_Y_MOCKUPS.md`, `ESTADO_PROYECTO_Y_C4.md`, este archivo, el Excel corregido y las correcciones S5) está solo en local** hasta que se pida un push.
