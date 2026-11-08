# Registro de decisiones · Konrad Gourmet

Cada decisión dice por qué hubo que decidir, qué opciones había, cuál se escogió y qué implica. Las opciones descartadas se dejan escritas para poder revisarlas después con el equipo.

- **Decidió:** Mauricio Iturriza, el 2026-11-07, salvo que se indique otra cosa.
- **DEC-15 a DEC-22:** las decidió Mauricio el 2026-11-08 en la sesión 2. Siguió la recomendación del arquitecto-revisor, que revisó cada caso en el `.drawio`, el reporte y las HU. Los ítems P1-…, P2-…, P3a-…, P3b-…, P5-… y P6-… están en `INVENTARIO_SESION2.md`.
- **DEC-23 y DEC-24:** las tomó el arquitecto-revisor el 2026-11-08, a pedido de Mauricio («toma la mejor decisión»), al validar la sesión 2. Responden a los hallazgos S5 y S9 de `VALIDACION_SESION2.md`.
- **DEC-25 a DEC-32:** las decidió Mauricio el 2026-11-08, al empezar la sesión 3. Siguió la recomendación del arquitecto-revisor, salvo en DEC-25 y DEC-28, donde se escogió la variante que no toca las páginas 4a–4d (en la sesión 3 solo se puede cambiar la 4e). Los ítems C-…, M-…, R-…, T-…, W-…, P-…, J-… y Q-… están en `INVENTARIO_SESION3.md`.
- **Hallazgos citados (H1–H26, E1–E3, D1–D12):** están en `REVISION_2026-11-07.md` y en `ESTADO_PROYECTO_Y_C4.md` §5.

| Id | Tema | Decisión | Estado |
|---|---|---|---|
| DEC-01 | Carpetas y control de versiones | Dos carpetas hermanas, cada una con su repo, en la rama local `correcciones-revision` | Aplicada |
| DEC-02 | Código (hallazgo bloqueante H1 y D12) | Firmas, relaciones y tipos, más una demo real | Por aplicar (sesiones 3 y 4) |
| DEC-03 | Reporte PDF | No tocarlo hasta que aparezca el script original | Vigente |
| DEC-04 | Ritmo de trabajo | Por fases con pausa, en 5 sesiones | Vigente |
| DEC-05 | HU-09: solicitudes por cotización | Varias solicitudes; anotar que hay que corregir el mockup V13 | Aplicada en el `.drawio`; faltan el código (sesiones 3 y 4) y los mockups (sesión 5) |
| DEC-06 | Dueño de los KPI | Director comercial, para revisar con el equipo | Decidida |
| DEC-07 | Alcance de HU-15 y HU-19 | Las dos dentro de la arquitectura | Aplicada en el `.drawio`; faltan el código (sesión 3) y los mockups opcionales (sesión 5) |
| DEC-08 | Recomendaciones del revisor y D1–D8 | Se aceptan las recomendaciones | Asumida, por confirmar |
| DEC-09 | Datos comunes de los eventos (H11) | `EventoDominio` pasa a clase abstracta con id, usuario, correlación y fecha | Aplicada en el `.drawio`; falta el código (sesión 3) |
| DEC-10 | Regla de contraseña (H15) | Regla en el dominio, `cifrar()` y alta de usuarios por el administrador | Aplicada en el `.drawio`; faltan el código (sesión 3) y el reporte (sesión 5) |
| DEC-11 | Platos por sucursal (N1) | Se corrige ya, en la fase 2a | Aplicada en el `.drawio`; falta el código (sesión 3) |
| DEC-12 | Rutas del gateway (N6) | 13 prefijos: `/api/usuarios` y `/api/proveedores` aparte | Aplicada en el `.drawio`; falta el reporte (sesión 5) |
| DEC-13 | Destinatarios de los correos (N5) | Mixto: el correo del solicitante viaja en el evento, y ms-notificaciones guarda una libreta de jefes por sucursal | Aplicada en el `.drawio`; falta el código (sesión 3) |
| DEC-14 | Resto de la validación de la fase 2a | Se aplican N2–N4 y N7–N15 como los propone el revisor | Aplicada en el `.drawio`; falta el código (sesión 3) |
| DEC-15 | Adaptadores nuevos en el mapa (p. 1) | Una caja por tecnología y destino, explicada en la leyenda; cajas angostas solo en 2 filas | Aplicada en el `.drawio`; falta el reporte (sesión 5) |
| DEC-16 | Infraestructura nueva en el mapa | Almacén de objetos y ELK, este en pequeño y con una sola flecha | Aplicada en el `.drawio`; falta el reporte (sesión 5) |
| DEC-17 | Base de datos de ms-integracion | PostgreSQL propia para la tabla outbox | Aplicada en el `.drawio`; falta el reporte (sesión 5) |
| DEC-18 | ms-inventario en el recorrido (p. 6) | Línea de vida propia, antes del servidor de correo | Aplicada en el `.drawio`; faltan las tablas del reporte (sesión 5) |
| DEC-19 | Ejemplo de varias solicitudes (p. 6) | «Una o varias (aquí SOL-031)», con las listas escritas como listas | Aplicada |
| DEC-20 | Zonas en el despliegue (p. 5) | Tres cajas de zona con el reparto; Eureka y la SPA en las zonas A y B | Aplicada; lo de Eureka y la SPA es asumido, por confirmar con el equipo (sesión 5) |
| DEC-21 | Ítems fuera del plan de la sesión 2 | Se aplican 27 de 33; 6 no se aplican | Aplicada |
| DEC-22 | Caché del Proxy en memoria (4e) | No se toca la 4e; se anota para la sesión 3 | Resuelta en la sesión 3 por DEC-27 |
| DEC-23 | Ruta del gateway a ms-integracion (p. 1, S5) | Se deja como está: entra por arriba al endpoint SOAP | Decidida, sin cambio |
| DEC-24 | Color del proveedor (p. 2, S9) | Gris claro con borde gris, en el dibujo y en la leyenda | Aplicada |
| DEC-25 | Constructores y accesores (Q1) | Constructores en el código ya; la convención «se omiten constructores y accesores» va en el README, no en las leyendas | Por aplicar (sesión 3) |
| DEC-26 | Comentario de ventana (Q2) | «Atiende la ventana Vxx de los mockups», una línea por ventana | Por aplicar (sesión 3) |
| DEC-27 | Caché del Proxy en Redis (Q3, cierra DEC-22) | `{Redis}` en el atributo y una nota en la 4e | Por aplicar (sesión 3) |
| DEC-28 | El dominio de notificaciones usa puertos (Q4) | Se deja la 4d; se precisa la regla del README | Por aplicar (sesión 3) |
| DEC-29 | Eventos entre servicios (Q5) | Un servicio solo importa el paquete `eventos` de otro, nunca su dominio | Por aplicar (README, sesión 3) |
| DEC-30 | Adaptadores JPA con colecciones en memoria (Q6) | Se copian tal cual, explicados en el Javadoc | Por aplicar (sesión 3) |
| DEC-31 | Alcance de la demo (Q7) | La demo cubre las 7 salidas del reporte; el Singleton muestra un parámetro del tema | Por aplicar (sesión 4) |
| DEC-32 | `/api/integracion/**` en la 4e (D2) | En la misma línea del estereotipo de `AbastecimientoSoapEndpoint` | Por aplicar (sesión 3) |

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

## DEC-15 · ¿Cómo caben los adaptadores nuevos en los hexágonos del mapa (p. 1)?

**Por qué hubo que decidir.** La fase 2a agregó 18 adaptadores, y el mapa dibuja cada uno como una caja verde alrededor del hexágono de su servicio. Cada servicio tiene 6 posiciones: 3 arriba y 3 abajo. A ms-cotizaciones y a ms-notificaciones les faltan 4 cajas cada uno, y la fila de abajo de ms-inventario ya está llena.

**Opciones:**
- **A. Una caja por tecnología y por destino.** Se agrupan en una caja los adaptadores JPA que van a la misma base, los REST de un mismo servicio y sus listeners. Por ejemplo: «AuthRestController / UsuarioRestController» o «CotizacionJpaAdapter / ProveedorJpaAdapter». Es la regla de la arquitectura hexagonal, donde el adaptador se nombra por su tecnología, y así describe el reporte (p. 13, §9) las filas del mapa. Una línea de la leyenda la explica. Solo las filas de abajo de ms-cotizaciones y de ms-notificaciones necesitan 4 cajas angostas. ✅ **Escogida.**
- **B. Una caja por clase**, con filas de 4 cajas angostas en todos los servicios. La letra queda pequeña.
- **C. Ensanchar los 11 servicios.** Hay que mover todo el mapa y sus ~141 flechas para ganar poco.

**Qué implica:**
- En el mapa una caja ya no es una clase. El detalle por clase sigue en 4a–4e.
- `/api/usuarios/**` y `/api/proveedores/**` no llevan flechas nuevas desde el gateway: se suman a las etiquetas de las flechas que ya llegan a la caja REST (`v208` y `v218`).
- `ElasticsearchLogsAdapter` va en la fila de abajo de ms-administracion, en el lugar que deja libre agrupar `ParametrosJpaAdapter`, para que su flecha a ELK no cruce el hexágono.

## DEC-16 · ¿Qué infraestructura nueva se dibuja en el mapa?

**Por qué hubo que decidir.** En la fase 2a, los PDF pasaron al almacén de objetos (D1, H4 y N4) y ms-administracion consulta los logs en ELK (H14). El mapa dibuja las bases de datos y Redis, pero ninguno de los dos.

**Opciones:**
- **A. Solo el almacén de objetos.** ELK aparece en la 3a y en el despliegue. Pero la caja de `ElasticsearchLogsAdapter` sería la única del mapa que apunta a algo que no está dibujado.
- **B. Los dos, con ELK en pequeño.** El almacén, con sus flechas desde ms-cotizaciones, ms-ordenes y ms-notificaciones. ELK, como una caja pequeña con una sola flecha. Prometheus y Grafana no se dibujan, porque ningún adaptador los llama. ✅ **Escogida.**

**Qué implica:**
- En el mapa, todo adaptador de salida apunta a lo que usa, con dos excepciones:
  - BCrypt y la generación de PDF, porque son librerías;
  - los publicadores Kafka de ms-identidad y ms-notificaciones (`ad_identidad_b2` y `ad_notificaciones_b2`), porque solo publican auditoría, y esa salida la dibuja la flecha agrupada `e242`.
- **Sesión 5:** la lista de lo que muestra el mapa (reporte, p. 13) suma el almacén y ELK.

## DEC-17 · ¿ms-integracion tiene base PostgreSQL propia?

**Por qué hubo que decidir.** El mapa (`db_integracion`) y la 3a (`c3a_db_integracion`) solo le dibujan Redis a ms-integracion. En cambio:
- el reporte dice «once servicios… cada uno con su base de datos» (p. 3), «cada uno de los once microservicios… con su propia base PostgreSQL» (p. 7) y «costo de once bases de datos» (p. 31);
- el despliegue dice «PostgreSQL ×11, una base por servicio» (p8 `v24`);
- en la 4a, `KafkaEventPublisher` escribe el evento en la tabla outbox de cada servicio (`n_outbox`), y en la 4e ms-integracion publica auditoría (`ConsultarPrecioService.eventos`).

**Opciones:**
- **A. Darle su base.** Se dibuja «BD integración [PostgreSQL] · tabla outbox» en el mapa y en la 3a, colgada del publicador. No lleva adaptador JPA, porque la 4e no lo tiene ni lo necesita. ✅ **Escogida.**
- **B. Dejarlo sin base** y poner «×10» en el despliegue. Contradice tres páginas del reporte y la 4a, y el servicio no tendría dónde guardar el outbox.

**Qué implica:**
- El despliegue no cambia (×11).
- Si preguntan «¿una base para una sola tabla?»: el evento de auditoría no se puede perder, y el reporte (p. 31) ya prevé que «las bases pequeñas pueden compartir servidor con esquemas separados».
- **Sesión 5:** la lista del mapa en el reporte (p. 13) suma la BD de integración.

## DEC-18 · ¿Cómo aparece ms-inventario en el recorrido de compra (p. 6)?

**Por qué hubo que decidir.** En la fase 2a, ms-inventario consume `solicitud.registrada` (`InventarioEventsListener`, 4b) para apagar la alerta de stock bajo. Cumple un criterio literal de HU-16 (Story Mapping, p. 23): «la alerta debe permanecer activa hasta que se registre una solicitud para ese producto». La página 6 no tiene línea de vida para ms-inventario.

**Opciones:**
- **A. Una nota en el paso 2.** No mueve nada, pero contradice la regla que la misma página escribe en su leyenda (`c6_v188`): «en cada evento se dibujan todos los consumidores».
- **B. Línea de vida propia, entre ms-administracion y el servidor de correo.** Solo se corren las dos últimas columnas (servidor de correo y proveedor) con sus 7 mensajes. La flecha nueva cabe en el paso 2 sin mover nada hacia abajo. ✅ **Escogida.**

**Qué implica:** la página 6 muestra a ms-inventario recibiendo `solicitud.registrada` en el paso 2, y la leyenda queda fiel a su regla.

## DEC-19 · ¿Cómo muestra la página 6 que una cotización puede tener varias solicitudes (DEC-05)?

**Por qué hubo que decidir.** El ejemplo de la página 6 sale de los mockups: SOL-031 → COT-018, con una sola solicitud. V13 (UX, p. 19) y V21 muestran solo SOL-031.

**Opciones:**
- **A. «Una o varias solicitudes (aquí SOL-031)».** Las listas se escriben como listas, por ejemplo `solicitudes: [SOL-031]`. Una lista de un elemento es un caso válido de «una o varias». ✅ **Escogida.**
- **B. Inventar una segunda solicitud, SOL-032.** No existe en ninguna fuente, y habría que inventar productos y cantidades que la demo de la sesión 4 tendría que reproducir.

**Qué implica:** los pasos 4 (4.1), 5 (nota `c6_v64`) y 12 (12.2) dicen «una o varias solicitudes» o «cada solicitud de COT-018», con «(aquí SOL-031)». El paso 6 no lo necesita, porque `solicitud.en-tramite` es un evento por cada solicitud.

## DEC-20 · ¿Cómo se ven las tres zonas en el despliegue (p. 5, H24)?

**Por qué hubo que decidir.** El sistema corre en 3 zonas de disponibilidad para cumplir el 99,7 %. Hoy la página 5 solo lo dice en un texto. Ninguna fuente dice qué corre en cada zona.

**Opciones:**
- **A. Franjas detrás del clúster.** Las cajas «×3» y «×11» no pueden estar en una sola zona: cada caja parecería pertenecer a la franja que tapa.
- **B. Tres cajas «Zona A», «Zona B» y «Zona C»** en el espacio libre de la derecha, tituladas «Reparto por zona (las mismas instancias de la izquierda)», para que nadie cuente 6 gateways. ✅ **Escogida.**

**Qué se deduce de las fuentes:**
- **Un gateway y un broker de Kafka por zona.** El reporte (p. 5) pide «sin punto único de falla… tres réplicas del gateway, Kafka con factor de réplica 3, clúster en tres zonas». Con 3 réplicas en 3 zonas, cualquier otro reparto pierde 2 de 3 si cae una zona. `ESTADO_PROYECTO_Y_C4.md` (E2) ya dice «una por zona».
- **Las réplicas de cada servicio se reparten entre las zonas.** Con un mínimo de 2, ocupan 2 de las 3 zonas.
- **PostgreSQL, Redis y el almacén** están en la capa de datos, fuera del clúster, y no van en las cajas de zona. Su etiqueta dice el reparto: PostgreSQL con réplica síncrona en otra zona (reporte, p. 27), Redis con réplica en otra zona y el almacén multizona.

**Qué es asumido, por confirmar con el equipo:** que Eureka (×2) y la SPA (×2) están en las zonas A y B. La zona C se registra en las dos instancias de Eureka.

## DEC-21 · ¿Qué se hace con los 33 ítems del inventario que no estaban en el plan?

**Por qué hubo que decidir.** El inventario previo de la sesión 2 marcó 33 ítems como «N/P» (no estaba en el plan) u «opcional».

**Opciones:**
- **A. Aplicarlos con un filtro.** ✅ **Escogida.**
  - **21 se aplican** porque vienen de 4a–4e o de DEC-17: P1-28, P1-32, P1-39, P1-52, P1-53, P2-02, P2-04, P2-05, P2-06, P3a-24, P3a-25, P3a-26, P3a-27, P3a-28, P3b-21, P3b-24, P5-11, P5-12, P5-13, P6-12 y P6-20.
  - **4 se aplican** porque hoy la página se contradice o rompe la notación C4: P2-03 (`v1` promete el protocolo en cada flecha), P5-09, P5-10 y P3a-20 (en C4, entre corchetes va la tecnología, no el contenido).
  - **2 de forma** se aplican en un commit aparte: P1-54 y P5-14. Los cilindros de p1 y p8 pasan a `cylinder3`, como en la 3a, porque al exportar cortan la palabra «PostgreSQL».
  - **6 no se aplican**: P1-08, P1-09, P1-13, P1-29, P2-07 y P3b-26. Solo agregan nombres de métodos o descripciones, no corrigen nada y llenan cajas que ya están justas.
- **B. Solo los que vienen de 4a–4e.** Quedan contradicciones a la vista, como p2 sin protocolos.
- **C. Ninguno por ahora**, anotados para la sesión 5, que ya está cargada.

**Qué implica:** `c6_v137` (p. 6), que dice «en la BD de auditoría», también pasa a «BD de administración», junto con P1-28 y P3a-27.

## DEC-22 · ¿Qué se hace con la caché en memoria del Proxy de precios (4e)?

**Por qué hubo que decidir.** El revisor encontró, al preparar estas decisiones, que en la 4e `PrecioReferenciaCacheProxy` tiene `- cache: Map<Long, PrecioEstandar>`, una caché en la memoria de cada réplica. Viene así desde `main`. En cambio, el mapa, la 3a, el despliegue (p8 `v25`), la página 6 (`c6_v49`) y el reporte (p. 12) dicen que la caché está en Redis. Con 2 a 4 réplicas, cada una tendría su propia caché: el mismo problema de N7. Severidad media.

**Opciones:**
- **A. No tocar la 4e ahora** (las páginas 4a–4e ya están aprobadas) y anotarlo para la sesión 3. Las páginas de la sesión 2 siguen diciendo Redis, que es el diseño correcto. ✅ **Escogida.**
- **B. Hacer una excepción** y agregar ya en la 4e una nota de una línea: «la caché vive en Redis, compartida por las réplicas».

**Qué implica:** en la sesión 3 se corrige la 4e, con una nota o con un puerto hacia Redis.

## DEC-23 · ¿Por dónde entra la ruta del gateway a ms-integracion (p. 1, S5)?

**Por qué hubo que decidir.** Al validar la sesión 2, el revisor vio que la ruta `e225c` (`/api/integracion/**`) entra a `AbastecimientoSoapEndpoint` (`ad_integracion_t1`, la caja del medio) por arriba, y cruza la cabecera azul de ms-integracion. Las otras 10 rutas del gateway entran a su servicio por la izquierda.

**Opciones:**
- **A. Dejarla como está.** El cruce se lee bien, y el WebSocket `e231` ya cruza así la cabecera de ms-notificaciones. ✅ **Escogida.**
- **B. Intercambiar `ad_integracion_t0` y `t1`**, para que el endpoint SOAP quede a la izquierda y la ruta entre por ese lado. Pero la llamada interna de ms-cotizaciones (`e227`), que hoy entra por la izquierda a `PrecioRestController`, tendría que entrar por arriba y cruzaría la misma cabecera. El cruce solo cambia de flecha, y además hay que mover dos cajas con sus flechas.

**Qué implica:** el mapa no cambia.

## DEC-24 · ¿Cómo se distingue al proveedor, que es una persona externa, en el contexto (p. 2, S9)?

**Por qué hubo que decidir.** La leyenda nueva de la p. 2 distingue a la «persona externa» por su color. Pero el gris del proveedor (`v22`, #6B7079) casi no se diferencia del gris (#5B6474) que ya usan el director comercial (`v10`) y el administrador (`v12`).

**Opciones:**
- **A. Gris claro (#DCDFE4) con borde gris (#6B7079)** para el proveedor y para su muestra en la leyenda (`p2_sw2`). Lo externo sigue en gris, pero en uno más claro que el de los roles, para no confundirlos. ✅ **Escogida.**
- **B. Dejarlo así.** La leyenda queda menos clara.
- **C. Relleno blanco con borde gris**, como proponía el revisor. La figura se pierde sobre el fondo blanco.
- **D. El mismo gris de los sistemas externos (#8A8F98).** Se parece más al gris de los roles que el gris claro.

El C4 no fija colores. La paleta habitual pinta lo externo en gris medio, y así están los sistemas externos de la p. 2 (#8A8F98). Aquí se escogió un gris más claro solo para que la persona externa no se confunda con los roles grises.

**Qué implica:** el proveedor usa el mismo color en todas las páginas donde aparece:
- p. 2: `v22` y su muestra en la leyenda, `p2_sw2`;
- p. 1: `v225`;
- 3a: `c3a_p_proveedor`;
- p. 6: `c6_v16` y `c6_v154`.

La validación pidió extenderlo a esas páginas (S13).

## DEC-25 · ¿El código lleva constructores y accesores, aunque 4a–4e no los dibuja? (Q1)

**Por qué hubo que decidir.** La regla de la sesión 3 es que el código quede idéntico a 4a–4e, y los diagramas no dibujan constructores ni accesores: en UML es habitual omitirlos. Pero sin ellos, dos patrones quedan mal construidos en el código:
- **Singleton:** sin constructor privado, `ParametrosSistema` no es un Singleton, porque cualquiera puede crear otra instancia;
- **Decorator y Proxy:** sin constructor, no reciben el objeto que envuelven.

Además, sin accesores ningún listener puede leer los datos de un evento (J-01 y J-02 del inventario).

**Opciones:**
- **A. Constructores ahora y la convención escrita.** Unos 53 constructores en la sesión 3; los accesores, al estilo record (`numero()`, no `getNumero()`), solo donde los use la sesión 4.
  - **A1.** Escribir la convención en la leyenda de las 5 páginas, como proponía el revisor. Toca 4a–4d.
  - **A2.** Escribirla en el `README.md`. ✅ **Escogida**, porque en la sesión 3 solo se puede tocar la 4e.
- **B. Dibujarlos en 4a–4e.** Son cientos de líneas y cajas más grandes.
- **C. Dejarlo todo para la sesión 4.** Mientras tanto, el Singleton del código no es un Singleton; si la sesión 4 no toca esa clase, se entrega así.

**Qué implica:**
- **Constructor de inyección**, con todos sus atributos, en cada clase de `aplicacion` y de los adaptadores que tenga atributos, y en las 4 clases del Factory Method de ms-notificaciones que tienen un colaborador. Los colaboradores quedan `final`.
- **Constructor `private`** en `ParametrosSistema`.
- **Constructor `protected`** en `CorreoDecorator`, y uno público en cada decorador concreto, que le pasa el envuelto.
- **Las entidades y los eventos** reciben sus constructores y accesores en la sesión 4, según lo que use la demo.
- **El README** dice que los diagramas omiten constructores y accesores.

## DEC-26 · ¿Qué dice el comentario de cada ventana? (Q2)

**Por qué hubo que decidir.** El plan decía `// Atiende la ventana Vxx`. Pero el reporte (p. 26), que no se toca (DEC-03), afirma que el código lleva «Atiende la ventana Vxx de los mockups». Además, 5 métodos atienden dos ventanas.

**Opciones:**
- **A. La frase del reporte, una línea por ventana.** Son 29 líneas en 24 métodos. Lo que dice el reporte queda literalmente cierto, y `grep "Atiende la ventana V04"` encuentra cada ventana. ✅ **Escogida.**
- **B. La frase del plan.** Es más corta, pero no coincide con la que cita el reporte.
- **C. Una línea con varias ventanas** («Atiende las ventanas V03 y V04»). Un `grep` de una sola ventana no la encuentra.

**Qué implica:** cada método marcado ‹Vxx› en 4a–4e lleva, encima, `// Atiende la ventana Vxx de los mockups`, una vez por ventana.

## DEC-27 · ¿Cómo dice la 4e que la caché del Proxy vive en Redis? (Q3, cierra DEC-22)

**Por qué hubo que decidir.** Ver DEC-22. Las demás páginas y el reporte (p. 12 y p. 31) dicen Redis, y la 4e dibuja un `Map` en memoria. Pesan dos restricciones:
- el código no usa frameworks y compila solo con `javac`, así que no puede declarar un cliente de Redis;
- el reporte (§13) promete que la demo lee el segundo precio desde la caché, así que la caché tiene que funcionar sin Redis en la demo.

**Opciones:**
- **A. Solo una nota** junto al Proxy. Quien mire la caja sigue viendo un `Map` en memoria.
- **B. `{Redis}` en el atributo, más la nota.** El atributo pasa a `- cache: Map<Long, PrecioEstandar> {Redis}`, la misma notación que `{único}` en la 4d. `Map` es una interfaz: en producción se inyecta un mapa respaldado por Redis (el `RMap` de Redisson implementa `java.util.Map`) y en la demo un `HashMap`. ✅ **Escogida.**
- **C. Un puerto de caché con su adaptador Redis.** Suma 2 clases (193), una flecha y un cambio en el mapa (p. 1). Es lo más hexagonal, pero cuesta más por una caché.
- **D. Un tipo de cliente Redis en el atributo.** Descartada: no compila sin dependencias.

**Qué implica:**
- **4e:** el atributo cambia y se agrega la nota `n_cache` con su conector.
- **Código:** el Proxy recibe el `Map` por su constructor.
- **Cifras:** siguen 191 clases y 65 flechas.

## DEC-28 · ¿Qué se hace con el dominio de ms-notificaciones, que usa puertos? (Q4)

**Por qué hubo que decidir.** Cuatro clases de `notificaciones.dominio` tienen atributos de tipo `Correo` o `AlarmaPushPort`, que están en `puertos.salida`: `NotificacionCorreo`, `CreadorCorreo`, `AlarmaApp` y `CreadorAlarmaApp`, las del Factory Method. Desde H25, también `Correo` es un puerto. El README dice «El dominio no conoce a nadie» (T-07 del inventario).

**Opciones:**
- **A. Dejar la 4d y precisar la regla del README.** El dominio no importa frameworks ni adaptadores; en ms-notificaciones, el Factory Method usa las interfaces `Correo` y `AlarmaPushPort`, que son puertos del mismo núcleo. Se puede defender, porque los puertos son parte del hexágono. ✅ **Escogida**, porque en la sesión 3 no se puede tocar la 4d.
- **B. Pasar las 6 clases del Factory Method de «dominio» a «aplicacion»** en la 4d y en el código. Es la que prefería el revisor: el `grep` de imports por capa queda limpio sin excepciones. Pero toca la 4d.
- **C. Devolver `Correo` al dominio.** Deshace H25, y `AlarmaPushPort` sigue igual.

**Qué implica:** la línea 21 del README cambia. Si el equipo prefiere B, se puede hacer más adelante: son 6 estereotipos en la 4d y 6 `git mv`.

## DEC-29 · ¿Qué puede importar un servicio de otro? (Q5)

**Por qué hubo que decidir.** Para leer los datos de un evento ajeno, un listener tiene que importar su clase. Por ejemplo, `solicitudes.CotizacionEventsListener` lee `cotizaciones.eventos.CotizacionEnviadaEvent`. Además, dos eventos llevan tipos del dominio de su servicio: `CotizacionValidadaEvent.estado` y `TendenciaDetectadaEvent.sugerencias` (T-06).

**Opciones:**
- **A. Regla en el README:** un servicio solo puede importar el paquete `eventos` de otro, que es el contrato del tópico, y nunca su `dominio`. Los dos atributos de T-06 se resuelven en la sesión 4, solo si la demo consume esos eventos. ✅ **Escogida.**
- **B. Lo mismo, más `CotizacionValidadaEvent.estado: String`** en la 4c. Toca la 4c.
- **C. Mover los 10 eventos a `compartido.eventos`.** Es un cambio grande en 4a–4e y en el mapa.

**Qué implica:** en la sesión 3 no cambia ninguna firma; la regla queda escrita en el README.

## DEC-30 · ¿Qué se hace con los adaptadores JPA que guardan datos en memoria? (Q6)

**Por qué hubo que decidir.** Tres de los 15 `*JpaAdapter` tienen en el diagrama una colección que simula la tabla: `CotizacionJpaAdapter.datos`, `KpiJpaAdapter.hechos` y `AuditoriaJpaAdapter.bitacora`. Vienen del diseño original, y los otros 12 no tienen atributos.

**Opciones:**
- **A. Copiarlos tal cual.** El Javadoc explica que en el esqueleto simulan la tabla y que en el proyecto real serían un repositorio JPA. ✅ **Escogida.**
- **B. Quitar los 3 atributos y la flecha `e25`** de la 4c y la 4e, para que los 15 queden iguales. Toca dos páginas aprobadas por algo que no está mal.

**Qué implica:** nada más que el Javadoc. Los adaptadores en memoria que necesite la demo van en el paquete `demo` (sesión 4).

## DEC-31 · ¿Qué salidas debe mostrar la demo? (Q7)

**Por qué hubo que decidir.** El reporte (§13, p. 26) promete 7 salidas de la demo:
1. Composite y Factory Method: el menú del director de compras;
2. Observer: el pedido de $82.000;
3. Facade y Chain of Responsibility: COT-018, COT-019 y COT-020;
4. Factory Method y Decorator: el correo con `X-Certificado`;
5. Proxy y Adapter: el segundo precio, desde la caché;
6. Singleton: `umbral.stock = 25 %`;
7. Strategy: la bandeja paisa y el proveedor B.

El plan de la sesión 4 solo cubría la tercera. Con el reporte sin tocar (DEC-03), cada salida que falte es una afirmación falsa. Además, la del Singleton contradice la 4e (`n_parametros`): desde H13 el umbral es de ms-inventario.

**Opciones:**
- **A. Las 7 salidas.** La del Singleton muestra un parámetro del tema en lugar del umbral, y esa frase se corrige en `CORRECCIONES_REPORTE.md` (sesión 5). ✅ **Escogida.**
- **B. Solo el flujo de compra**, y anotar las otras 6 como falsas en `CORRECCIONES_REPORTE.md`.
- **C. Las 7, con `umbral.stock` en el Singleton.** Contradice la 4b y la 4e.

**Qué implica:** la sesión 4 crece. Su plan se actualiza al cerrar la sesión 3.

## DEC-32 · ¿Cómo se anota en la 4e que el endpoint SOAP se publica bajo `/api/integracion/**`? (D2)

**Por qué hubo que decidir.** Desde la sesión 2, el mapa y la 3a publican el SOAP de ms-integracion por el gateway, bajo el prefijo `/api/integracion/**` (DEC-12). La 4e no lo escribe.

**Opciones:**
- **A. En la misma línea del estereotipo:** `«SOAP · @WebService · /api/integracion/** (RNF integración)»`. Cabe en el ancho actual de la caja. ✅ **Escogida.**
- **B. Una línea nueva debajo:** «publicado por el API Gateway en /api/integracion/**». La caja crece unos 10 px.

**Qué implica:** cambia una celda de la 4e (`cls_integracion_AbastecimientoSoapEndpoint`). El Javadoc de la clase lo explica, y aclara que `@WebService` va solo en el comentario, porque JAX-WS no está en el JDK. El diagrama no menciona el número del prefijo: S12 quitó los ids internos de las páginas.
---

## Preguntas abiertas (sin decidir)

| # | Pregunta | Por qué importa | Quién |
|---|---|---|---|
| A1 | ¿Quién tiene el script de ReportLab que generó el reporte? | Sin él no se puede corregir el PDF (DEC-03) | Equipo |
| A2 | Con 2–4 réplicas de ms-notificaciones, ¿cómo llega la alarma por WebSocket a la réplica que tiene abierta la sesión del jefe? Las salidas posibles son sesión pegajosa en el balanceador, o difundir el mensaje a todas las réplicas, por ejemplo con Redis pub/sub | El profesor puede preguntarlo en la sustentación | Equipo |
| A3 | Los commits del repo son de la cuenta `mari00aa120-eng`. ¿Es de alguien del equipo? | Conviene saberlo antes de la sustentación | Equipo |
