# Registro de decisiones · Konrad Gourmet

Cada decisión dice por qué hubo que decidir, qué opciones había, cuál se escogió y qué implica. Las opciones descartadas se dejan escritas para poder revisarlas después con el equipo.

- **Decidió:** Mauricio Iturriza, el 2026-11-07, salvo que se indique otra cosa.
- **Hallazgos citados (H1–H26, E1–E3, D1–D12):** están en `REVISION_2026-11-07.md` y en `ESTADO_PROYECTO_Y_C4.md` §5.

| Id | Tema | Decisión | Estado |
|---|---|---|---|
| DEC-01 | Carpetas y control de versiones | Dos carpetas hermanas, cada una con su repo, en la rama local `correcciones-revision` | Aplicada |
| DEC-02 | Código (hallazgo bloqueante H1 y D12) | Firmas, relaciones y tipos, más una demo real | Por aplicar (sesiones 3 y 4) |
| DEC-03 | Reporte PDF | No tocarlo hasta que aparezca el script original | Vigente |
| DEC-04 | Ritmo de trabajo | Por fases con pausa, en 5 sesiones | Vigente |
| DEC-05 | HU-09: solicitudes por cotización | Varias solicitudes; anotar que hay que corregir el mockup V13 | Por aplicar (sesiones 1 a 4) |
| DEC-06 | Dueño de los KPI | Director comercial, para revisar con el equipo | Decidida |
| DEC-07 | Alcance de HU-15 y HU-19 | Las dos dentro de la arquitectura | Por aplicar (sesiones 1 a 3) |
| DEC-08 | Recomendaciones del revisor y D1–D8 | Se aceptan las recomendaciones | Asumida, por confirmar |

---

## DEC-01 · Carpetas y control de versiones

**Por qué hubo que decidir.** Mauricio pidió hacer las correcciones con historial de versiones en Git, para poder volver atrás si algo sale mal. Los documentos estaban sueltos en `FinalProyecto/`, que no era un repo. Dentro estaba `KonradGourmet/`, que ya era un repo con remoto en GitHub (`iCastro117/KonradGourmet`). Un repo nuevo en `FinalProyecto/` habría quedado con otro repo adentro.

**Opciones:**
- **A. Dos carpetas hermanas.** Mover los documentos a `FinalProyecto/documentos/` con un repo nuevo, y dejar `KonradGourmet/` donde estaba con una rama nueva. ✅ **Escogida.**
- **B. Repo de código arriba.** Mover `KonradGourmet/` a `01-ArquitecturaSW/` y convertir `FinalProyecto/` en el repo de documentos.
- **C. No mover nada.** Convertir `FinalProyecto/` en repo y poner `KonradGourmet/` en el `.gitignore`. Ninguna ruta cambiaba, pero quedaba un repo dentro de otro.

**Qué implica:**
- Los 7 documentos se movieron a `documentos/` sin cambios: se comprobaron los SHA-256 antes y después.
- En los dos repos, `main` queda como punto de retorno y el trabajo se hace en la rama `correcciones-revision`.
- Todo es local. En `KonradGourmet` se desactivó el push. Para reactivarlo: `git remote set-url --push origin https://github.com/iCastro117/KonradGourmet.git`.
- Las rutas que citan `ESTADO_PROYECTO_Y_C4.md` y la memoria cambian de `FinalProyecto/<archivo>` a `FinalProyecto/documentos/<archivo>`.

## DEC-02 · Código: hallazgo bloqueante (H1) y demo (D12)

**Por qué hubo que decidir.** El reporte afirma tres cosas que no son ciertas:
- que cada controlador declara los métodos de sus 23 ventanas con el comentario «Atiende la ventana Vxx»;
- que el esqueleto "implementa el mismo comportamiento";
- que una demo reproduce COT-018 (+4,81 % → OPCIONADA).

En realidad las 156 clases están vacías y la demo solo imprime una línea. Un profesor lo comprueba en un minuto.

**Opciones:**
- **A. Firmas y demo real.** Declarar los métodos de la §15 con su comentario, agregar las 33 relaciones `implements`/`extends`, corregir los 6 tipos de clase (H8) e implementar la lógica que recorre la demo, con adaptadores en memoria. ✅ **Escogida.**
- **B. Solo firmas.** Métodos sin lógica, que lanzan `UnsupportedOperationException`, y quitar del reporte lo de la demo.
- **C. Solo corregir el reporte.** Corregir los tipos y borrar las afirmaciones. Es lo más rápido, pero el código sigue vacío.

**Qué implica:**
- **Sesión 3:** las firmas y las relaciones.
- **Sesión 4:** la demo.
- Mauricio pidió que, si el trabajo resultaba muy extenso, se dividiera en varias sesiones. Por eso existe `PLAN_CORRECCIONES.md`.

## DEC-03 · Reporte PDF

**Por qué hubo que decidir.** El reporte (38 págs.) se generó con Python y ReportLab, pero nadie tiene el script (D11). Hay que agregar las páginas 3a, 3b y 6, cambiar "ocho páginas" por 11 y aplicar las correcciones de texto.

**Opciones:**
- **A. Reconstruirlo con un script nuevo**, a partir del texto y las imágenes del PDF actual. El aspecto podía variar un poco.
- **B. Anexo de correcciones** en un PDF aparte, dejando el original intacto.
- **C. Esperar el script original** y, mientras tanto, dejar anotadas las correcciones. ✅ **Escogida.**

**Qué implica:**
- El PDF del reporte no se modifica.
- En la sesión 5 se escribe `CORRECCIONES_REPORTE.md` con todos los cambios de texto que hay que aplicar cuando aparezca el script.
- Sigue abierto quién tiene el script.

## DEC-04 · Ritmo de trabajo

**Por qué hubo que decidir.** Son 26 hallazgos más 15 pendientes, repartidos entre el `.drawio`, el código, el Excel y los documentos. Eso no cabe bien en una sola sesión.

**Opciones:**
- **A. Por fases, con pausa.** Al terminar cada fase, el revisor la valida y se espera el OK para seguir. Son 5 sesiones. ✅ **Escogida.**
- **B. Todo de corrido**, con un reporte final.
- **C. Menos sesiones y más largas**, o más sesiones y más cortas.

**Qué implica:**
- El plan está en `PLAN_CORRECCIONES.md`.
- Se hace un commit por corrección, con el id del hallazgo en el mensaje, para que cualquier cambio se pueda revertir solo.

## DEC-05 · HU-09: ¿una cotización sale de una solicitud o de varias?

**Por qué hubo que decidir.** Las fuentes se contradicen:
- **La tarjeta HU-09** (Story Mapping, p. 16) dice: «Debo poder seleccionar **una o varias solicitudes** del listado para incluir en la cotización». También dice que, si no se selecciona ninguna, el sistema muestra «Debe seleccionar al menos una solicitud», y que hay que ver un resumen de productos y cantidades antes de confirmar.
- **El mockup V13** (DISEÑO UXUI, p. 19, «Generar solicitud de cotización») se abre desde **una sola** solicitud: «Solicitud origen: SOL-031», un campo no editable. Lo que se escoge son los productos de esa solicitud.
- **La arquitectura sigue al mockup:** en la 4c, la cotización guarda un solo `solicitudNumero`.

**Opciones:**
- **A. Una sola solicitud.** No cambian el diseño, los mockups ni la demo; solo habría que corregir el texto de la tarjeta HU-09.
- **B. Varias solicitudes.** La cotización guarda una lista de solicitudes. Es fiel a la HU, pero cambian el modelo, los eventos y las páginas, y el mockup V13 queda desactualizado. ✅ **Escogida.**

**Qué implica:**
- **Página 4c:**
  - `Cotizacion.solicitudNumero` pasa a `solicitudNumeros: List`.
  - El caso de uso valida que la lista tenga al menos una solicitud ("Debe seleccionar al menos una solicitud").
  - El resumen suma los productos y cantidades de todas las solicitudes seleccionadas.
- **Eventos:**
  - `CotizacionEnviadaEvent` lleva la lista. ms-solicitudes pasa **todas** las solicitudes a EN TRÁMITE.
  - Cuando llega `orden.generada`, ms-solicitudes pasa **todas** a COMPLETADA (H12).
- **Páginas 3b y 6, y el código:** se ajustan a la lista.
- **Demo:** COT-018 se arma con la lista de solicitudes que corresponda. Los valores de V19 no cambian ($520.000 / $545.000 / +4,81 %).
- **⚠ Hay que corregir el mockup V13** (DISEÑO UXUI, p. 19). Se hace en Canva, y no lo hacemos nosotros. Debe:
  - permitir escoger una o varias solicitudes desde el listado, en lugar de mostrar «Solicitud origen» fija;
  - mostrar el resumen de productos y cantidades sumados.

  Queda anotado en la lista de correcciones de HU y mockups de la sesión 5.

## DEC-06 · ¿De quién son los indicadores del tablero (KPI)?

**Por qué hubo que decidir.** Las fuentes asignan el tablero a actores distintos:
- **La tarjeta HU-18** (Story Mapping, p. 25) dice: «Como **director comercial** quiero visualizar un tablero de control (BAM) con los principales indicadores del negocio». Sus criterios piden:
  - producto, sucursal y proveedor destacados;
  - filtros por fechas y sucursal;
  - carga en menos de 5 s;
  - el mensaje «No hay información suficiente…».
- **La arquitectura y el reporte** ya usan al director comercial.
- **El Story Mapping** (el mapa visual, págs. 2, 5, 27 y 30) pone la actividad «Ver KPIs» bajo el **director de compras**.

**Opciones:**
- **A. Director comercial.** Manda la tarjeta, porque es la que tiene los criterios de aceptación. No cambian la arquitectura ni el código; solo hay que corregir la etiqueta del Story Mapping. ✅ **Escogida.**
- **B. Director de compras.** Habría que cambiar el rol en ms-identidad, el tablero y el reporte, y corregir la tarjeta HU-18. Solo tiene sentido si el equipo de verdad quería que fuera compras.

**Qué implica:**
- No hay cambios en el `.drawio` ni en el código por esta decisión.
- La etiqueta del Story Mapping queda en la lista de correcciones de HU de la sesión 5 (ver también H26).
- **Para revisar con el equipo:** confirmar que nadie pretendía que el tablero fuera del director de compras.

## DEC-07 · Alcance de HU-15 (proveedores) y HU-19 (promociones)

**Por qué hubo que decidir.** Las dos historias están incompletas en la arquitectura.

**HU-15, «Registrar y mantener proveedores»** (Story Mapping, p. 22; auxiliar de compras; MVP 3, el del flujo de compra).
- **Pide:**
  - razón social, NIT único y obligatorio, correo válido, teléfono, dirección y una o varias categorías de producto;
  - mensajes de error para NIT repetido y correo inválido;
  - consultar, editar e inactivar; un proveedor inactivo no sale en los desplegables de cotización;
  - auditoría de cada acción.
- **Hoy:** el proveedor es solo una clase con `id`, `nombre` y `correo` en la 4c, sin ruta ni pantalla.
- **Por qué pesa:** V13 tiene un desplegable «Proveedor» que nada alimenta.

**HU-19, «Promociones sugeridas»** (Story Mapping, p. 26; jefe de cocina; MVP 4, el último).
- **Ya estaba cubierto:**
  - detectar tendencias en redes y los productos más pedidos (ms-analitica);
  - el correo con las promociones (`tendencia.detectada` → ms-notificaciones).
- **Faltaba:**
  - el histórico dentro de la app;
  - producto, motivo y periodo de vigencia en cada sugerencia;
  - marcar una sugerencia como aceptada o descartada;
  - el máximo de una notificación de promociones por día;
  - que un fallo del análisis quede en el log sin interrumpir el resto.

**Opciones:**
- **A. Las dos dentro.** ✅ **Escogida.**
- **B. HU-15 dentro y HU-19 fuera de alcance**, por ser del último MVP.
- **C. Las dos fuera de alcance.** Es lo que menos trabajo da, pero el desplegable de V13 queda sin quién lo alimente.

**Qué implica:**
- **HU-15 → ms-cotizaciones:**
  - `Proveedor` pasa a tener `{id, razonSocial, nit, correo, telefono, direccion, categorias, activo}`;
  - `ProveedorRestController` en `/api/proveedores/**`, con registrar, consultar, editar e inactivar;
  - la regla del NIT único, con un puerto de repositorio que verifica si el NIT existe;
  - la validación del formato del correo;
  - el desplegable de V13 lista solo los proveedores activos;
  - la auditoría va por `auditoria.evento`.
- **HU-19 → ms-analitica:**
  - `SugerenciaPromocion` con `{producto, motivo, vigenciaDesde, vigenciaHasta, estado: SUGERIDA | ACEPTADA | DESCARTADA}`;
  - rutas bajo `/api/bam/**` para el histórico y para aceptar o descartar;
  - la regla de máximo un `tendencia.detectada` por día;
  - un fallo de la tarea semanal queda en el log.
- **Qué se mantiene:** no hacen falta microservicios, tópicos ni ventanas nuevas, así que siguen 11 servicios y 10 tópicos.
- **Qué falta:** ninguna de las dos tiene mockup. Queda anotado en la lista de la sesión 5, por si el equipo quiere diseñar esas pantallas.

## DEC-08 · Recomendaciones del revisor y D1–D8

**Por qué hubo que decidir.** Mauricio pidió aplicar "todas las correcciones que mencionas". Algunas traían una decisión pequeña adentro.

**Qué se asume:**
- Se aplican las correcciones mínimas de H1–H26 como las propuso el revisor, incluidas sus alternativas descartadas. Por ejemplo, en H2 se descarta que ms-inventario llame por REST a ms-menu, y en H13 se descarta un tópico nuevo.
- **D1:** el PDF va al almacén de objetos y el evento lleva solo la referencia (`pdfUrl`).
- **D2:** el gateway tiene 11 rutas.
- **D3:** el estado nuevo de la cotización se llama **CON ORDEN**. ms-ordenes rechaza una segunda orden sobre la misma cotización, y ms-cotizaciones consume `orden.generada`.
- **D4:** ms-inventario consume `solicitud.registrada` y marca la alerta como atendida.
- **D5:** se agregan `editar()` y `desactivar()` de platos.
- **D6:** el aviso EN TRÁMITE va por **correo**, como ya lo dibuja la página 6.
- **D7 y D8:** ver DEC-07.

**Estado:** asumida, porque se deduce de "haz todas las correcciones" y no se preguntó una por una. Si alguna no le parece a Mauricio o al equipo, se revierte su commit.

---

## Preguntas abiertas (sin decidir)

| # | Pregunta | Por qué importa | Quién |
|---|---|---|---|
| A1 | ¿Quién tiene el script de ReportLab que generó el reporte? | Sin él no se puede corregir el PDF (DEC-03) | Equipo |
| A2 | Con 2–4 réplicas de ms-notificaciones, ¿cómo llega la alarma por WebSocket a la réplica que tiene abierta la sesión del jefe? Las salidas posibles son sesión pegajosa en el balanceador, o difundir el mensaje a todas las réplicas, por ejemplo con Redis pub/sub | El profesor puede preguntarlo en la sustentación | Equipo |
| A3 | Los commits del repo son de la cuenta `mari00aa120-eng`. ¿Es de alguien del equipo? | Conviene saberlo antes de la sustentación | Equipo |
