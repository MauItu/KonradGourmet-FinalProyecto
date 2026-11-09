# Plan para rehacer el reporte de arquitectura

**Archivo actual:** `Reporte_Arquitectura_Konrad_Gourmet.pdf`. Tiene 38 páginas, A4, y lo generó con IA un programa en Python (ReportLab) el 29-oct-2026, según su propia §18. No se modifica: queda como el original.

**Archivo nuevo:** `Reporte_Arquitectura_Konrad_Gourmet_v2.pdf`, con su versión editable `Reporte_Arquitectura_Konrad_Gourmet_v2.docx`.

**Decisiones que lo respaldan:**
- DEC-54: si el script original no aparece, se rehace con uno nuevo.
- DEC-55: formato sobrio, de documento académico.
- Las correcciones de contenido R-01 a R-66 de `CORRECCIONES_REPORTE.md`.

**Estado:** plan, sin ejecutar. Es parte del punto 1 de `PENDIENTES_POST_SESION6.md` y se hace después del Story Mapping corregido (punto 3).

## 1. Por qué rehacerlo y no solo corregirlo

El reporte es el entregable principal: es lo que el profesor lee y califica. Hoy contradice al código y a los diagramas en cosas que se comprueban en un minuto:
- habla de 8 páginas de diagramas, cuando hay 11;
- cuenta 155 clases, cuando hay 191;
- no muestra los niveles 2 y 3 del C4 ni el diagrama dinámico;
- no menciona el mesero del pedido ni el umbral «menor que 25 %».

Además, Mauricio pidió cambiar el formato (DEC-55). El original usa:
- tres familias de letra (Onest, Bricolage Grotesque y JetBrains Mono);
- tamaños distintos;
- títulos y cajas de color;
- una portada con un dibujo de los servicios;
- separadores «·» en encabezados y pies.

Ese aspecto delata el documento generado y no es el de un informe académico.

Con el cambio de formato, el script original ya no es imprescindible: aunque aparezca, solo ahorraría extraer el texto, porque todo el diseño se rehace. **A1 deja de bloquear el reporte.** Si aparece, se usa como referencia del contenido.

## 2. Formato (DEC-55)

**Letra y página:**
- Una sola familia, **Times New Roman de 12 pt**, en todo el documento: portada, títulos, texto, tablas, pies de figura, encabezado y pie de página.
- El código y los nombres técnicos van en la misma letra. No se usa monoespaciada.
- **Color:** todo el texto en negro. Sin fondos de color, sin cajas, sin cabeceras de tabla coloreadas y sin íconos.
- **Página:** A4, márgenes de 2,54 cm, interlineado 1,5 en el texto y sencillo en las tablas, texto alineado a la izquierda y sangría de primera línea de 1,27 cm.

**Títulos:**
- Del mismo tamaño que el texto, en **negrilla**, numerados (1, 1.1).
- El de primer nivel va centrado. El de segundo, a la izquierda.
- Sin mayúsculas sostenidas, para tener jerarquía sin cambiar el tamaño.

**Énfasis:** negrilla y cursiva con moderación. Sin subrayados ni resaltados.

**Tablas:**
- Líneas finas negras: superior, inferior y bajo la cabecera.
- Cabecera en negrilla.
- Numeradas («Tabla 3») con su título encima, en negrilla.

**Figuras:**
- Las capturas del `.drawio` conservan sus colores, porque son el diagrama entregado. Lo que va en negro es todo lo que las rodea.
- Numeradas («Figura 7»), con el título encima en negrilla y la nota debajo («Nota. Página 6 del archivo Arquitectura_Konrad_Gourmet.drawio.»).

**Encabezado y pie:**
- Encabezado vacío.
- Número de página abajo a la derecha.
- Sin el nombre del documento repetido en cada página.

**Portada**, todo en 12 pt y centrado:
- título;
- integrantes del equipo;
- asignatura;
- profesor;
- programa;
- universidad;
- ciudad y fecha.

**Redacción:**
- Frases directas, en tercera persona o en plural del equipo.
- Sin viñetas para lo que se puede decir en un párrafo.
- Sin cadenas de «·» y sin raya larga como adorno.
- Sin frases de relleno («En resumen», «Cabe destacar»).
- Una idea por párrafo.
- Las tablas solo cuando comparan o enumeran datos.
- **Referencias:** formato APA 7, con sangría francesa. Las citas en el texto van como (Newman, 2021), no [5].

## 3. Herramienta

**Recomendada: un script en Python que genera el `.docx` (con python-docx) y lo exporta a PDF con LibreOffice**, que ya está instalado (`soffice --headless --convert-to pdf`).
- **El `.docx` queda editable:** el equipo puede ajustar una frase en Word o en Google Docs sin depender de un programa.
- **El script queda guardado** en `documentos/reporte/generar_reporte.py`, para regenerar todo si cambian las cifras o las figuras.
- python-docx no está instalado: se descarga a un entorno virtual temporal, como se hizo con openpyxl.

**Alternativas descartadas:**
- **ReportLab, como el original.** Da control total del PDF, pero el equipo no puede editar el resultado.
- **Escribirlo a mano en Word.** Es lo más flexible, pero no se puede regenerar y es fácil que las cifras se desalineen.

## 4. Contenido

Se conserva la estructura de 19 secciones más el anexo, con dos ajustes:
- §14 pasa a llamarse «Modelo C4: contexto, contenedores, componentes y despliegue» (R-05);
- en §2 entra la tabla de niveles del C4 (R-62).

El texto base se extrae del PDF original con `pdftotext -raw`, no con `-layout`, que parte las tablas. Se corrige con las entradas de `CORRECCIONES_REPORTE.md` según su tipo:

| Tipo | Cuántas | Qué se hace |
|---|---|---|
| Cambiar | 52 (R-01 a R-05, R-07 a R-09, R-11 a R-13, R-15 a R-17, R-20 a R-24, R-26 a R-35, R-37, R-38, R-40 a R-43, R-45 a R-47, R-49, R-50, R-52, R-56 a R-66) | Se aplica el texto nuevo |
| Ya es cierto | 9 (R-06, R-10, R-18, R-25, R-36, R-44, R-51, R-54, R-55) | Se deja el texto; solo se revisa la redacción |
| Opcional | 4 (R-14, R-19, R-39, R-53) | Las decide Mauricio antes de empezar (sección 7) |
| Sin cambio | 1 (R-48) | Se deja |

Además de las 66 entradas:
- **Cifras:** se toman de la tabla «Cifras que el reporte debe usar» de `CORRECCIONES_REPORTE.md`, y se comprueban otra vez contra el código y el `.drawio` el día que se genere (191 clases, 20 archivos de demo, 176 verificaciones, 11 páginas, 13 prefijos, 10 tópicos).
- **Figuras:**
  - salen de `FinalProyecto/exportes/post_sesion6/`, exportadas a escala 1;
  - cada figura del original se reemplaza por el recorte equivalente de la página vigente (R-01);
  - las páginas 3a, 3b y 6 entran como figuras 12, 13 y 7 (R-02 y R-03);
  - si el `.drawio` cambia antes de generar, se exporta de nuevo.
- **Declaración de IA (§18):** se conserva la del equipo y se agrega el párrafo de R-58: Claude (Opus 5.5 y Sonnet 5.5) y ChatGPT (Astra 6).
- **Portada:** la del original no nombra al equipo, y la nueva sí, en este orden (Mauricio, 2026-11-08, provisional): Mauricio Jesús Iturriza Medina, Carlos Franco, Adrián David Corredor Camero e Isabela Castro.

## 5. Pasos

Cada paso es un commit en `documentos/` (rama `correcciones-revision`), con su id al inicio (`REP-01`, `REP-02`, …).

1. **REP-01:** extraer el texto del original a `reporte/texto_original.md`, sección por sección, y comprobar con `-raw` que no se perdieron filas de tablas.
2. **REP-02:** escribir el script con la plantilla de formato de la sección 2 (estilos de Word definidos una sola vez) y generar una página de prueba para que Mauricio apruebe el aspecto.
3. **REP-03 a REP-21:** pasar las secciones 1 a 19, una por commit, aplicando las R-xx que les tocan.
4. **REP-22:** el anexo A, con las páginas 4a a 4e recapturadas (R-59).
5. **REP-23:** las figuras, recortadas de `exportes/post_sesion6/` y numeradas según R-03.
6. **REP-24:** el índice, la portada y las referencias en APA 7.
7. **REP-25:** generar el `.docx` y el PDF, y revisarlos página por página:
   - que no haya texto cortado;
   - que las tablas no se partan mal;
   - que cada figura se lea.
8. **Validación:**
   - el `arquitecto-revisor` contrasta el reporte nuevo con el código, el `.drawio`, los casos de uso y el Story Mapping corregido;
   - se comprueba que cada R-xx de tipo «Cambiar» esté aplicada;
   - sus hallazgos se corrigen con su propio id.

Al terminar, el original sigue intacto en su lugar.

## 6. Comprobación final

- [ ] Una sola familia y un solo tamaño en todo el documento (se comprueba con `pdffonts`: solo Times New Roman y su negrilla o cursiva).
- [ ] Ningún texto en color: solo las figuras del `.drawio` tienen color.
- [ ] Ninguna cifra contradice al código ni al `.drawio`.
- [ ] Las 52 correcciones «Cambiar» están aplicadas, y las opcionales según la decisión de Mauricio.
- [ ] Las 23 ventanas siguen trazadas en §15.
- [ ] Las referencias están en APA 7 y cada cita del texto tiene su referencia.
- [ ] El original sigue sin cambios: el mismo SHA-256 que hoy.

## 7. Lo que Mauricio decide antes de empezar

1. **Las 4 opcionales:**
   - R-14 (§3): decir que ms-administracion consulta los logs en Elasticsearch por el puerto `LogsPort`;
   - R-19 (§6): completar la lista de estados con COMPLETADA, PENDIENTE_PRECIO y CON ORDEN;
   - R-39 (§12): agregar «almacén de objetos» a lo que coordina la fachada;
   - R-53 (§15): una tabla extra con los métodos de controlador que no tienen ventana.

   Recomendación: aplicar R-14, R-19 y R-39, que son una frase cada una y dejan el reporte más exacto, y omitir R-53, que suma una tabla larga que nadie pidió. **Mauricio (2026-11-09): quedan como opcionales, para revisar después.**
2. **La portada:** resuelta, con los nombres dados por Mauricio (provisional).
3. **El aspecto:** si la página de prueba de REP-02 está bien, antes de pasar todo el texto.
