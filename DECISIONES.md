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
| DEC-09 | Datos comunes de los eventos (H11) | `EventoDominio` pasa a clase abstracta con id, usuario, correlación y fecha | Por aplicar (sesiones 1 y 3) |
| DEC-10 | Regla de contraseña (H15) | Regla en el dominio, `cifrar()` y alta de usuarios por el administrador | Por aplicar (sesiones 1 y 3) |
| DEC-11 | Platos por sucursal (N1) | Se corrige ya, en la fase 2a | Por aplicar (sesiones 1 y 3) |
| DEC-12 | Rutas del gateway (N6) | 13 prefijos: `/api/usuarios` y `/api/proveedores` aparte | Por aplicar (sesiones 2 y 5) |
| DEC-13 | Destinatarios de los correos (N5) | Mixto: el correo del solicitante viaja en el evento, y ms-notificaciones guarda una libreta de jefes por sucursal | Por aplicar (sesiones 1 y 3) |
| DEC-14 | Resto de la validación de la fase 2a | Se aplican N2–N4 y N7–N15 como los propone el revisor | Por aplicar (sesión 1) |

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


## DEC-09 · ¿Cómo llevan los eventos su id, usuario y correlación? (H11)

**Por qué hubo que decidir.** H11 pide que todo evento lleve identificador, usuario e id de correlación, porque el reporte (p. 9) promete eventos "con identificador, tipo, fecha y usuario" y consumidores que "recuerdan los identificadores". Las fuentes no coinciden:
- **En la 4a,** `EventoDominio` es una «interface» con `tipo()` y `ocurridoEn()`.
- **En el código,** es una `abstract class`, y los eventos hacen `extends`. H8 lo cuenta entre los 6 tipos que no coinciden.
- **Los 10 eventos de 4a–4e** repiten `tipo()` y `ocurridoEn()`, y casi todos repiten el atributo `ocurridoEn`.

**Opciones:**
- **A. Clase abstracta.** `EventoDominio` pasa a «abstract» y guarda `id`, `usuario`, `correlacionId` y `ocurridoEn` una sola vez. Los 10 eventos dicen «extends EventoDominio» y solo muestran sus datos propios y `tipo()`. El diagrama se alinea con el código, no al revés. ✅ **Escogida.**
- **B. Interfaz + `EventoBase`.** `EventoDominio` sigue como interfaz con los métodos nuevos, y una clase abstracta nueva los implementa. Es una clase más, y el código cambia de los dos lados.
- **C. Solo la interfaz.** Se agregan los 3 métodos a la interfaz, y cada uno de los 10 eventos suma 3 atributos y 3 métodos. No hay clases nuevas, pero se repite lo mismo en 5 páginas.

**Qué implica:**
- **4a:** `EventoDominio` «abstract», con `# id: UUID`, `# usuario: String`, `# correlacionId: UUID` y `# ocurridoEn: LocalDateTime`, sus métodos y `tipo()` abstracto. La flecha de `AuditoriaEvent` pasa de realización a herencia.
- **4b–4e:** cada evento cambia «implements EventoDominio» por «extends EventoDominio» y deja de repetir `ocurridoEn`.
- **H8:** `EventoDominio` sale de la lista de tipos que no coinciden, porque el código ya es una clase abstracta. Quedan 5.
- **Sesión 3:** el código conserva la clase abstracta y le agrega los campos y métodos.

## DEC-10 · ¿Quién usa la regla de contraseña y `cifrar()`? (H15)

**Por qué hubo que decidir.** H15 pide `cifrar()` en `CifradorPort` y la regla de contraseña del reporte (p. 5): "8 o más caracteres con mayúscula, minúscula y número, almacenada cifrada". Pero ms-identidad solo tiene el inicio de sesión, así que nada llamaría a `cifrar()` ni aplicaría la regla. Tampoco hay HU ni mockup para crear usuarios: el supuesto S5 (p. 31) dice que el administrador no tiene mockup y que "sus servicios y rutas quedan listos".

**Opciones:**
- **A. Regla + alta de usuarios.** Una clase de dominio `PoliticaContrasena`, `cifrar()` en el puerto y su adaptador, y un caso de uso para que el administrador registre usuarios (`POST /api/usuarios`), sin ventana, como dice S5. ✅ **Escogida.**
- **B. Solo la regla y `cifrar()`**, con una nota: "el alta de usuarios no tiene HU ni mockup; queda fuera del alcance". Es lo mínimo, pero en la sustentación pueden preguntar quién usa `cifrar()`.

**Qué implica:**
- **4a:** `PoliticaContrasena`, `CifradorPort.cifrar()`, `RegistrarUsuarioUseCase`, `RegistrarUsuarioService`, `UsuarioRestController` (`/api/usuarios`, rol Administrador) y `UsuarioRepositoryPort.guardar()`. El alta publica `auditoria.evento`.
- **Sesiones 2 y 5:** la ruta `/api/usuarios` se suma a las rutas de ms-identidad. En el reporte, §15 y supuesto S5.
- **Sin cambios** en servicios, tópicos ni ventanas: siguen 11 servicios, 10 tópicos y 23 ventanas.


## DEC-11 · ¿Se corrige ya que el plato no tenga sucursal? (N1)

**Por qué hubo que decidir.** La validación de la fase 2a (`VALIDACION_FASE2A.md`, N1, severidad alta) encontró que, en la 4b, el plato no tiene sucursal. La tarjeta HU-04 (Story Mapping, p. 11) pide «asociar automáticamente el plato a mi sucursal y a mi usuario» y «nombre único por sucursal». Sin sucursal, el catálogo de V03 no se puede filtrar para el mesero. El problema venía del diseño original, y H6 lo dejó a la vista.

**Opciones:**
- **A. Corregir ya, en la fase 2a.** Son las mismas clases que ya cambiaron H3, H6 y D5. ✅ **Escogida.**
- **B. Dejarlo para después.** Las sesiones 2 y 3 copiarían el diseño sin sucursal, y habría que corregirlo en varios lados.

**Qué implica:**
- `Plato` lleva `sucursalId` y `creadoPor`.
- El puerto de platos verifica `existeNombre(sucursalId, nombre)`.
- `menu.actualizado` y `PlatoCatalogo` llevan `sucursalId`.
- El catálogo de ms-pedidos se pide con `catalogo(sucursalId)`.

## DEC-12 · ¿Cuántos prefijos tiene el gateway? (N6)

**Por qué hubo que decidir.** El alta de usuarios (DEC-10) y los proveedores (DEC-07) agregan los prefijos `/api/usuarios` y `/api/proveedores`. D2 y DEC-08 hablan de 11 rutas, una por servicio, igual que la tabla del reporte (p. 7).

**Opciones:**
- **A. 13 prefijos.** `/api/usuarios` va a ms-identidad y `/api/proveedores` a ms-cotizaciones. Un gateway puede tener varios prefijos por servicio, y las rutas quedan más claras. ✅ **Escogida.**
- **B. Bajo prefijos existentes.** Pasan a `/api/auth/usuarios` y `/api/cotizaciones/proveedores`, y se mantienen 11 prefijos. Pero `/api/cotizaciones/proveedores` se puede confundir con `/api/cotizaciones/{id}`.

**Qué implica:**
- En las páginas 4a–4e no cambia nada.
- **Sesión 2:** el mapa (p. 1) y la 3a pasan de 11 a 13 rutas, con los mismos 11 servicios.
- **Sesión 5:** se corrige la tabla del reporte (p. 7).

## DEC-13 · ¿De dónde saca ms-notificaciones el correo del destinatario? (N5)

**Por qué hubo que decidir.** ms-notificaciones manda tres tipos de correo: stock bajo, «su solicitud está EN TRÁMITE» y promociones. Pero no sabe a qué dirección:
- los eventos no traen ningún correo;
- la lista de usuarios vive en ms-identidad, que tiene su propia base.

Además, HU-16 (p. 23) pide que la alerta muestre el producto, la cantidad restante, la unidad y la sucursal, y `StockBajoEvent` solo traía el id del producto y el porcentaje.

**Opciones:**
- **A. Mixto.**
  - Los eventos de una solicitud traen el correo de quien la creó (ms-solicitudes lo sabe por el inicio de sesión).
  - Para el stock bajo y las promociones, que no los crea nadie, ms-notificaciones guarda su propia libreta de jefes de cocina por sucursal, que llena el administrador.

  Ningún servicio depende de otro y no hay tópicos nuevos. ✅ **Escogida.**
- **B. Todo en los eventos.** Quien publica pone los correos. Pero ms-inventario y ms-analitica tendrían que saber los correos de los jefes, que no son datos suyos.
- **C. Preguntar a ms-identidad por REST en cada evento.** Si ms-identidad está caído, no salen los correos, y los dos servicios quedan acoplados.

**Qué implica:**
- **ms-solicitudes:** `SolicitudAlimentos`, `SolicitudRegistradaEvent` y `SolicitudEnTramiteEvent` llevan `correoSolicitante`.
- **ms-inventario:** `StockBajoEvent` suma `nombreProducto`, `stockActual` y `unidad`.
- **ms-notificaciones:**
  - `DestinatarioRepositoryPort` (`jefesDe(sucursalId)`, `jefes()`) y su adaptador JPA;
  - `DestinatarioRestController` en `/api/notificaciones/destinatarios`, para el administrador.

  La ruta cae dentro del prefijo `/api/notificaciones/**` que ya tiene ms-notificaciones (D2).
- Siguen 11 servicios y 10 tópicos.

## DEC-14 · ¿Qué se hace con el resto de la validación de la fase 2a?

**Por qué hubo que decidir.** El revisor dejó, además de N1, N5 y N6, otros 12 hallazgos que no traen una decisión de diseño (`VALIDACION_FASE2A.md`).

**Opciones:**
- **A. Aplicarlos todos**, un commit por hallazgo, como los propone el revisor, y volver a validar. ✅ **Escogida.**
- **B. Solo los de severidad media** (N2, N3, N4, N7 y N8), y dejar N9–N15 anotados.
- **C. Ninguno por ahora**, para revisarlos con el equipo.

**Qué implica:**
- N2, N3, N4, N7, N8 y N9–N15 se aplican en las páginas 4a–4e.
- Después se exportan otra vez las PNG y se pide una segunda validación.

---

## Preguntas abiertas (sin decidir)

| # | Pregunta | Por qué importa | Quién |
|---|---|---|---|
| A1 | ¿Quién tiene el script de ReportLab que generó el reporte? | Sin él no se puede corregir el PDF (DEC-03) | Equipo |
| A2 | Con 2–4 réplicas de ms-notificaciones, ¿cómo llega la alarma por WebSocket a la réplica que tiene abierta la sesión del jefe? Las salidas posibles son sesión pegajosa en el balanceador, o difundir el mensaje a todas las réplicas, por ejemplo con Redis pub/sub | El profesor puede preguntarlo en la sustentación | Equipo |
| A3 | Los commits del repo son de la cuenta `mari00aa120-eng`. ¿Es de alguien del equipo? | Conviene saberlo antes de la sustentación | Equipo |
