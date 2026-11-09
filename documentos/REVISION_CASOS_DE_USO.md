# Revisión de `Casos de uso — Konrad Gourmet.md` · 2026-11-08

El archivo lo escribió un compañero de Mauricio y está en este repo **sin modificar**, tal como lo entregó. Esta revisión la hizo el `arquitecto-revisor` contra las 19 HU del Story Mapping, los 23 mockups, el `.drawio` (páginas 2, 4a–4e y 6), el código de `KonradGourmet/` (con `./ejecutar.sh`: 171 de 171) y `DECISIONES.md`. Las correcciones las aplica quien mantiene el archivo; cada una trae el texto propuesto.

## Veredicto: BIEN CON CORRECCIONES

**No se puede entregar como está**, por los problemas 1 y 2. El contenido de fondo es bueno.

**Lo que está bien y no hay que tocar**
- Las 19 HU están cubiertas y cada una cae en un caso de uso.
- Los actores coinciden con la página 2 del `.drawio`: 6 roles, el Proveedor como persona externa y 4 sistemas externos.
- Los nombres de métodos, puertos, rutas y estados coinciden con el código.
- La numeración CU-01 a CU-18 es consistente y los MVP coinciden con el Story Mapping.

## Problemas, de mayor a menor

**1. Bloqueante · el estado del prototipo está desactualizado** (línea 9; también las líneas 13–30, 36, 59 y 61).
- Dice: «último commit 726c4b0… fase 3 de 5… sus 274 métodos lanzan "Pendiente de implementar", así que ningún caso de uso está implementado».
- Hoy: el commit 726c4b0 no existe en los repos locales (el último del código es `4a64164`); quedan **94** cuerpos pendientes, en 45 de los 211 `.java`, de los **274** que había al cerrar la sesión 3; `./ejecutar.sh` compila sin avisos, la demo corre y pasan 171 de 171; la carpeta es `src/`, no `codigo/src/`.
- Texto para la línea 9: *«Este cuadro contrasta cada caso de uso con el repositorio `KonradGourmet` (código `4a64164`, 8 de noviembre). La sesión 4 (demo real) está terminada y la 5 (documentos de cierre) está cerrada. El código compila sin avisos, la demo (`./ejecutar.sh`) recorre el flujo de compra con adaptadores en memoria y pasan las 171 verificaciones. De los 274 cuerpos que lanzaban «Pendiente de implementar» al cerrar la sesión 3 quedan 94: los adaptadores de tecnología externa y los módulos que la demo no usa. Tienen lógica CU-02, CU-04, CU-05, CU-07 a CU-14, CU-16, CU-17 y CU-18, y parte de CU-15 (parámetros, tema y destinatarios). Siguen sin lógica CU-01, CU-03, CU-06 y el alta de usuarios.»*
- Borrar la sección «Qué pide el plan… sesión 4» (línea 59), que ya se cumplió, y corregir «todaváa» (línea 36).

**2. Bloqueante · falta el diagrama y hay un enlace privado.**
- Línea 85: dice `[embedded content: casos de uso…]`; el diagrama UML no está en el archivo.
- Línea 9: enlaza a `claude.ai/artifact/…`, que el profesor no puede abrir.
- Corrección: exportar el diagrama a PNG e insertarlo con `![Diagrama de casos de uso](casos_de_uso.png)`, y borrar la frase «El detalle completo está en [Contraste con el prototipo](…)».

**3. Alta · la regla del 25 % y su dueño contradicen las decisiones** (CU-05 líneas 224 y 233; CU-15 línea 426; HU-20 línea 467, criterio 3).
- Dice «llega al 25 %» y «El 25 % es un parámetro (ver CU-15)». DEC-43 y DEC-46 dicen otra cosa, y `UmbralStock` es de ms-inventario: no se lee de `ParametrosSistema` y `AdminRestController` no tiene ruta para cambiarlo.
- Paso 2 de CU-05: *«Cuando el porcentaje de un producto frente a su stock de referencia, redondeado a dos decimales, queda por debajo del 25 %, el sistema genera una alarma visible en la aplicación y envía un correo. La alarma sale una sola vez, la noche en que el producto cruza el umbral (DEC-43, DEC-46).»*
- Reglas de CU-05: *«El 25 % es un parámetro propio de ms-inventario (`UmbralStock`); no se cambia desde la parametrización.»*
- En CU-15 y en HU-20: borrar «umbral de inventario, hoy 25 % (HU-16)» y el criterio (3).

**4. Alta · los límites de CU-11 ya están decididos, y su precondición choca con su propio flujo alterno** (líneas 342 y 356).
- Dice «Conviene confirmarlo con el profesor»; DEC-43 ya lo decidió. La precondición «tiene valores registrados» hace imposible el flujo alterno «sin valores».
- Precondición: *«la cotización está en estado ENVIADA.»*
- Reglas: *«Los porcentajes se redondean a dos decimales antes de comparar: +25,00 % y −50,00 % quedan OPCIONADA, +25,01 % queda RECHAZADA y −50,01 % queda SOSPECHOSA (DEC-43).»*

**5. Alta · el orden de CU-08 y sus flujos alternos no siguen la arquitectura** (líneas 280–298).
- El diseño (`GenerarCotizacionFacade.generarYEnviar` y la página 6, pasos 4.x) consulta el precio primero, luego genera el PDF y deja la cotización ENVIADA. Si el Ministerio no responde, la cotización queda `PENDIENTE_PRECIO` y se reintenta con `reenviar()` (`POST /api/cotizaciones/{id}/reenvio`).
- Pasos 4 a 7: *«4. El auxiliar acepta. El sistema genera el número consecutivo y consulta el precio estándar de cada producto (a través de la caché del Proxy). 5. Genera el PDF, lo guarda en el almacén de objetos y deja la cotización ENVIADA (V14). 6. Envía el PDF por correo certificado y con estampado cronológico al proveedor. 7. Por cada solicitud incluida, registra el valor total sugerido y, si todos sus productos ya tienen cotización, la pasa a EN TRÁMITE y avisa por correo a quien la creó.»*
- Flujo alterno: *«El Ministerio no responde, o falla el PDF: la cotización queda en PENDIENTE_PRECIO, el error queda en el log y en la auditoría, y el auxiliar puede reenviarla.»*
- Precondición: *«existen solicitudes PENDIENTES y al menos un proveedor activo.»*

**6. Alta · falta el caso del alta de usuarios (DEC-10), y los destinatarios no están en el flujo de CU-15.**
- La relación e13 de la página 2 dice «Parametriza, registra usuarios y destinatarios». La línea 426 lo deja como «falta confirmar».
- Agregar *«CU-19 Registrar usuario. Actor: administrador. Sin ventana (`POST /api/usuarios`, ms-identidad). Flujo: el administrador envía nombre, correo, rol, sucursal y contraseña; el sistema valida 8 o más caracteres con mayúscula, minúscula y número (`PoliticaContrasena`), la cifra (`CifradorPort.cifrar`), registra al usuario y deja la auditoría. Alterno: si la contraseña no cumple la regla, el usuario no se registra.»*
- En CU-15, agregar el paso *«registra los correos de los jefes de cocina por sucursal (`/api/notificaciones/destinatarios`, DEC-13).»*
- Renumerar: «18 casos» → «19 casos» (líneas 5 y 85) y «cinco restantes» → «seis restantes» (línea 91).

**7. Media · V10, V15 y V16 quedan sin caso de uso** (línea 32). Anexarlos como flujos alternos: CU-04 «consultar mis solicitudes (V10, `GET /api/solicitudes?solicitante=yo`)»; CU-08 «consultar cotizaciones enviadas (V15, `GET /api/cotizaciones`)»; CU-11 «ver pendientes de validar (V16, `GET /api/cotizaciones/pendientes`)». En el catálogo: CU-04 «V08, V09, V10»; CU-08 «V13, V14, V15»; CU-11 «V16, V17, V19». Así quedan cubiertas las 23 ventanas.

**8. Media · a CU-13 le faltan un flujo alterno y parte de la postcondición** (líneas 387–393).
- Flujo alterno: *«La cotización ya tiene orden: "Ya existe una orden de compra para la cotización COT-018".»* (es el mensaje real de la demo).
- Postcondición: *«orden EN PROCESO; la cotización queda CON ORDEN y sale de V20; cada solicitud incluida pasa a COMPLETADA, aunque la cotización la haya cubierto solo en parte (DEC-46); la generación queda en la auditoría.»*
- Línea 25: quitar «que extraje» (primera persona).

**9. Media · pedido sin usuario** (líneas 14, 157 y 166). El archivo tiene razón: `Pedido` no guarda quién lo tomó, y el criterio de HU-01 «asociado a… mi usuario» queda sin cubrir. Paso 7 nuevo: *«El sistema asocia el pedido a la sucursal de la mesa (`Mesa.sucursalId`), registra fecha y hora, genera el consecutivo y lo muestra (V04).»* En Reglas: *«El diseño actual no guarda qué mesero tomó el pedido.»* El arquitecto propone agregar `meseroId: Long` a `Pedido` (4b y código): es la pregunta abierta **A4** de `DECISIONES.md`, porque cambia una firma.

**10. Media · la línea 61 está desactualizada.** Ya están resueltos K9 (DEC-35), K10 (DEC-36, ShedLock) y K11 (DEC-37); `cotizacionesAplicadas` sirve para la idempotencia; el umbral no tiene ruta, por diseño. Siguen abiertos solo el pedido sin mesero y las categorías del proveedor como texto (`List<String>`).

**11. Media · referencias que nadie puede verificar.** Los códigos RF-01…12 y las brechas B6…B17 vienen de una «matriz de trazabilidad» que no está en ninguna carpeta, y se usan ids internos (H4…H18, K10, S2) sin definirlos. Anexar la tabla de RF citando el reporte §3, y quitar o definir los códigos B, H y K.

**12. Media · a CU-04 le falta un efecto en la postcondición** (línea 211): *«…y la alerta de stock de esos productos queda atendida (ms-inventario consume `solicitud.registrada`).»* CU-05 (línea 231) depende de ese efecto.

**13. Baja · forma.** Línea 3: dice «Nov 7» pero el contenido es del 8 de noviembre, y `@Carlos` no va en un documento que se entrega. CU-02 (línea 166): falta «y en el número consecutivo generado», que pide HU-03. CU-16: agregar *«En el prototipo, los eventos llevan «sistema» como usuario (DEC-42); en el sistema real sale del JWT.»* Fuera del archivo: en la demo el director se llama «Laura Gómez» y en V16–V23, «Isabella Castro».

## Lo que verificó el arquitecto
Las 19 tarjetas HU (como imágenes, criterio por criterio); los mockups V05, V07, V17, V19, V20, V21, V22 y V23; las páginas 2 y 6 del `.drawio`; las pp. 8, 11, 21 y 26 del reporte; los puertos de entrada, controladores, rutas, comentarios «Atiende la ventana», enums de estado y la fachada del código; el conteo de cuerpos pendientes; `./ejecutar.sh` en una copia (171 de 171); y `DECISIONES.md`. No pudo verificar el repo remoto.

## Quién lo hace
Quien mantiene el archivo (el compañero de Mauricio). Prioridad: problemas 1 y 2 (sin ellos no se entrega), luego 3 a 6 (contradicen decisiones), luego 7 a 13.
