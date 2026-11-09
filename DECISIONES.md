# Registro de decisiones · Konrad Gourmet

Cada decisión dice por qué hubo que decidir, qué opciones había, cuál se escogió y qué implica. Las opciones descartadas se dejan escritas para poder revisarlas después con el equipo.

- **Decidió:** Mauricio Iturriza, el 2026-11-07, salvo que se indique otra cosa.
- **DEC-15 a DEC-22:** las decidió Mauricio el 2026-11-08 en la sesión 2. Siguió la recomendación del arquitecto-revisor, que revisó cada caso en el `.drawio`, el reporte y las HU. Los ítems P1-…, P2-…, P3a-…, P3b-…, P5-… y P6-… están en `INVENTARIO_SESION2.md`.
- **DEC-23 y DEC-24:** las tomó el arquitecto-revisor el 2026-11-08, a pedido de Mauricio («toma la mejor decisión»), al validar la sesión 2. Responden a los hallazgos S5 y S9 de `VALIDACION_SESION2.md`.
- **DEC-25 a DEC-32:** las decidió Mauricio el 2026-11-08, al empezar la sesión 3. Siguió la recomendación del arquitecto-revisor, salvo en DEC-25 y DEC-28, donde se escogió la variante que no toca las páginas 4a–4d (en la sesión 3 solo se puede cambiar la 4e). Los ítems C-…, M-…, R-…, T-…, W-…, P-…, J-… y Q-… están en `INVENTARIO_SESION3.md`.
- **DEC-33 y DEC-34:** las decidió Mauricio el 2026-11-08, al revisar la validación de la sesión 3 (hallazgos K1 y K4 de `VALIDACION_SESION3.md`). En DEC-33 no siguió la recomendación del revisor.
- **DEC-35 a DEC-37:** las decidió Mauricio el 2026-11-08, al revisar la segunda validación de la sesión 3 (hallazgos K9, K10 y K11 de `VALIDACION_SESION3.md`). Siguió la recomendación del revisor en las tres.
- **DEC-38:** la decidió Mauricio el 2026-11-08, al revisar la tercera validación de la sesión 3 (la duda de `VALIDACION_SESION3.md`). Siguió la recomendación del revisor.
- **DEC-39 a DEC-46:** se propusieron durante la sesión 4. Mauricio confirmó el 2026-11-08 el pedido de $82.000, los precios por unidad, completar las solicitudes parciales y la alarma solo al cruzar el umbral. Escogió `adjuntoUrl` (DEC-42): dos contratos, reflejados en cuatro declaraciones de la 4d. En la segunda ronda confirmó también el redondeo de cotizaciones y stock (DEC-43), y `sistema` con una correlación por evento como límite de la demo (DEC-42); autorizó completar la auditoría de los fallos (DEC-46, S4-13). Las demás partes siguen asumidas, por confirmar, según indica cada decisión.
- **DEC-49 y DEC-50:** las tomó el arquitecto-revisor el 2026-11-08, a pedido de Mauricio («actúa como el agente de arquitectura que tengo configurado y toma esas decisiones»). Mauricio puede revertirlas.
- **DEC-47:** la fecha de inicio del sprint 1 y la forma de mostrar «EN SEMANAS» las decidió Mauricio el 2026-11-08, al empezar la sesión 5 (hallazgo H20), y en las dos escogió la opción recomendada. Después pidió que todo diga 5 sprints y 10 semanas, y se quitó la fila de sprints mínimos. El orden de las filas del Excel es asumido, por confirmar.
- **PD-01 a PD-11 y las confirmaciones de los supuestos (DEC-08, DEC-20, DEC-39 a DEC-41, DEC-44, DEC-45, DEC-47 y DEC-48):** las tomó el agente `arquitecto-revisor` de Claude el 2026-11-08, por delegación expresa de Mauricio (puntos 4 y 5 de `PENDIENTES_POST_SESION6.md`), respetando sus decisiones ya confirmadas. El informe completo, con la evidencia, está en `REVISION_ARQUITECTO_POST_S6.md`. «PD» significa decisión posterior a la sesión 6. Mauricio puede revertir cualquiera.
- **Hallazgos citados (H1–H26, E1–E3, D1–D12):** están en `REVISION_2026-11-07.md` y en `ESTADO_PROYECTO_Y_C4.md` §5.

| Id | Tema | Decisión | Estado |
|---|---|---|---|
| DEC-01 | Carpetas y control de versiones | Dos carpetas hermanas, cada una con su repo, en la rama local `correcciones-revision` | Aplicada |
| DEC-02 | Código (hallazgo bloqueante H1 y D12) | Firmas, relaciones y tipos, más una demo real | Firmas, relaciones y tipos aplicados en el código (sesión 3); la demo real, aplicada en la sesión 4 |
| DEC-03 | Reporte PDF | No tocarlo hasta que aparezca el script original | Vigente; los cambios están en `CORRECCIONES_REPORTE.md` (R-01 a R-61, sesiones 5 y 6) |
| DEC-04 | Ritmo de trabajo | Por fases con pausa, en 5 sesiones | Vigente; Mauricio añadió la sesión 6 (DEC-51/52) y aprobó sus fases 1 y 2 antes del cierre |
| DEC-05 | HU-09: solicitudes por cotización | Varias solicitudes; anotar que hay que corregir el mockup V13 | Aplicada en el `.drawio`, en las firmas y en la lógica del código (sesión 4); el mockup V13 (y V21), anotado en `CORRECCIONES_HU_Y_MOCKUPS.md` (M-01 y M-02) |
| DEC-06 | Dueño de los KPI | Director comercial, para revisar con el equipo | Decidida |
| DEC-07 | Alcance de HU-15 y HU-19 | Las dos dentro de la arquitectura | Aplicada en el `.drawio` y en el código; los mockups opcionales, descritos en `CORRECCIONES_HU_Y_MOCKUPS.md` (M-08) |
| DEC-08 | Recomendaciones del revisor y D1–D8 | Se aceptan las recomendaciones | Asumida, por confirmar |
| DEC-09 | Datos comunes de los eventos (H11) | `EventoDominio` pasa a clase abstracta con id, usuario, correlación y fecha | Aplicada |
| DEC-10 | Regla de contraseña (H15) | Regla en el dominio, `cifrar()` y alta de usuarios por el administrador | Aplicada en el `.drawio` y en el código; el cambio del reporte está anotado en `CORRECCIONES_REPORTE.md` (R-09, R-17, R-53 y R-57) |
| DEC-11 | Platos por sucursal (N1) | Se corrige ya, en la fase 2a | Aplicada |
| DEC-12 | Rutas del gateway (N6) | 13 prefijos: `/api/usuarios` y `/api/proveedores` aparte | Aplicada en el `.drawio`; el reporte, anotado en `CORRECCIONES_REPORTE.md` (R-17) |
| DEC-13 | Destinatarios de los correos (N5) | Mixto: el correo del solicitante viaja en el evento, y ms-notificaciones guarda una libreta de jefes por sucursal | Aplicada |
| DEC-14 | Resto de la validación de la fase 2a | Se aplican N2–N4 y N7–N15 como los propone el revisor | Aplicada |
| DEC-15 | Adaptadores nuevos en el mapa (p. 1) | Una caja por tecnología y destino, explicada en la leyenda; cajas angostas solo en 2 filas | Aplicada en el `.drawio`; el reporte, anotado en `CORRECCIONES_REPORTE.md` (R-26) |
| DEC-16 | Infraestructura nueva en el mapa | Almacén de objetos y ELK, este en pequeño y con una sola flecha | Aplicada en el `.drawio`; el reporte, anotado en `CORRECCIONES_REPORTE.md` (R-26 y R-27) |
| DEC-17 | Base de datos de ms-integracion | PostgreSQL propia para la tabla outbox | Aplicada en el `.drawio`; el reporte, anotado en `CORRECCIONES_REPORTE.md` (R-26) |
| DEC-18 | ms-inventario en el recorrido (p. 6) | Línea de vida propia, antes del servidor de correo | Aplicada en el `.drawio`; las tablas del reporte, anotadas en `CORRECCIONES_REPORTE.md` (R-32 y R-34) |
| DEC-19 | Ejemplo de varias solicitudes (p. 6) | «Una o varias (aquí SOL-031)», con las listas escritas como listas | Aplicada |
| DEC-20 | Zonas en el despliegue (p. 5) | Tres cajas de zona con el reparto; Eureka y la SPA en las zonas A y B | Aplicada; lo de Eureka y la SPA es asumido, por confirmar con el equipo (R-49 de `CORRECCIONES_REPORTE.md` espera esa confirmación) |
| DEC-21 | Ítems fuera del plan de la sesión 2 | Se aplican 27 de 33; 6 no se aplican | Aplicada |
| DEC-22 | Caché del Proxy en memoria (4e) | No se toca la 4e; se anota para la sesión 3 | Resuelta en la sesión 3 por DEC-27 |
| DEC-23 | Ruta del gateway a ms-integracion (p. 1, S5) | Se deja como está: entra por arriba al endpoint SOAP | Decidida, sin cambio |
| DEC-24 | Color del proveedor (p. 2, S9) | Gris claro con borde gris, en el dibujo y en la leyenda | Aplicada |
| DEC-25 | Constructores y accesores (Q1) | Constructores en el código ya; la convención «se omiten constructores y accesores» va en el README, no en las leyendas | Aplicada en aplicación y adaptadores (sesión 3) y en entidades y eventos (sesión 4) |
| DEC-26 | Comentario de ventana (Q2) | «Atiende la ventana Vxx de los mockups», una línea por ventana | Aplicada |
| DEC-27 | Caché del Proxy en Redis (Q3, cierra DEC-22) | `{Redis}` en el atributo y una nota en la 4e | Aplicada |
| DEC-28 | El dominio de notificaciones usa puertos (Q4) | Se deja la 4d; se precisa la regla del README | Aplicada |
| DEC-29 | Eventos entre servicios (Q5) | Un servicio solo importa el paquete `eventos` de otro, nunca su dominio | Aplicada (README). La demo no consume `cotizacion.validada` ni `tendencia.detectada`, así que T-06 sigue abierta (DEC-42) |
| DEC-30 | Adaptadores JPA con colecciones en memoria (Q6) | Se copian tal cual, explicados en el Javadoc | Aplicada |
| DEC-31 | Alcance de la demo (Q7) | La demo cubre las 7 salidas del reporte; el Singleton muestra un parámetro del tema | Aplicada (sesión 4) |
| DEC-32 | `/api/integracion/**` en la 4e (D2) | En la misma línea del estereotipo de `AbastecimientoSoapEndpoint` | Aplicada |
| DEC-33 | Anotaciones de Spring en el código (K1) | Cada uno de los 54 adaptadores dice en su Javadoc qué anotación lleva en el proyecto real | Aplicada |
| DEC-34 | Historia de `Correo` y `CorreoDecorator` en git (K4) | No se reescribe; un commit anota que se ve con `git log --follow -M30%` | Aplicada |
| DEC-35 | Dos clases cumplen `PrecioReferenciaPort` (K9) | El Proxy lleva además `@Primary` | Aplicada |
| DEC-36 | Los jobs y las réplicas (K10) | Los 2 jobs llevan además `@SchedulerLock` (ShedLock) | Aplicada |
| DEC-37 | Ventanas de ms-cotizaciones (K11) | El README vuelve a «V13–V20»; revierte K5 | Aplicada |
| DEC-38 | ShedLock y `@Primary` fuera del código | Nada ahora; una frase en el reporte (p. 26) si aparece el script | Aplicada: la frase está en `CORRECCIONES_REPORTE.md` (R-43) |
| DEC-39 | Qué métodos tienen lógica en la sesión 4 | Solo lo que usa el recorrido de compra; los adaptadores de tecnología externa y los módulos que la demo no usa siguen pendientes (94 cuerpos) | Asumida, por confirmar |
| DEC-40 | Datos del recorrido que los mockups no fijan | Precios por unidad; pedido de $82.000; res en 12,5 kg; histórico del tablero cargado a mano | Mauricio confirmó precios por unidad y pedido; inventario inicial e histórico del tablero siguen asumidos |
| DEC-41 | Cómo cambia una entidad sin métodos para eso | Constructores de copia y accesores de lista modificables; no se agregan métodos | Asumida, por confirmar |
| DEC-42 | Lo que los contratos entre servicios no alcanzan a llevar | El PDF va como `adjuntoUrl` en la firma (decidido por Mauricio); usuario `sistema`; un `correlacionId` por evento; T-06 decidida en PD-05 | `adjuntoUrl` y límite de usuario/correlación: **confirmados por Mauricio**; 4d y nota `n_outbox` de la 4a actualizadas en la sesión 5 (`4da992c`, `f57a89e`); aclaraciones del reporte anotadas en `CORRECCIONES_REPORTE.md` (R-13, R-21, R-23 y R-45); T-06: **decidida por el arquitecto en PD-05, aplicación diferida** |
| DEC-43 | Límites de la cadena de reglas y del umbral | Porcentajes redondeados a 2 decimales antes de comparar; límites estrictos y stock por debajo del 25 % | Decidida por Mauricio en la segunda ronda de la sesión 4 (2026-11-08), incluido el redondeo del stock |
| DEC-44 | Dónde van las pruebas y el script | Carpeta `pruebas/` fuera de `src/`, sin JUnit, y `ejecutar.sh` | Asumida, por confirmar |
| DEC-45 | La demo y la cuenta de clases del reporte | 191 clases más `DemoArquitectura`, y 19 clases de apoyo en `demo/` que el reporte no cuenta | Asumida, por confirmar |
| DEC-46 | Tres reglas que el revisor encontró mal en la sesión 4 (S4-1, S4-2, S4-3) | Alarma solo al cruzar el umbral; `completar()` acepta PENDIENTE; auditoría en cada escritura del recorrido, incluidos los fallos | Confirmada por Mauricio; auditoría de los fallos autorizada en la fase 2 (S4-13) |
| DEC-47 | Plan de sprints del Excel (H20) | 1 PHU = 25 h y 9 PHU por sprint; HU-14 = 1 (total 35); HU-17 al sprint 3 (8, 8, 6, 8 y 5 PHU); inicio el lunes 03/08/2026; 5 sprints y 10 semanas en todo el Excel | Aplicada en el Excel (sesión 5); fecha y «EN SEMANAS» decididos por Mauricio; el orden de filas es asumido, por confirmar |
| DEC-48 | Dónde van las páginas 3a, 3b y 6 en el reporte | La 6 en §10, después de la tabla del recorrido; la 3a y la 3b en §14, después del contexto | Asumida, por confirmar (sesión 5) |
| DEC-49 | Qué orden sale de V20 a V22: los mockups y la demo no coinciden | Opción A ampliada: los datos de la demo mandan (COT-018, Proveedor ABC, OC-0012 por $545.000) y se corrigen seis mockups (V17 y V19 a V23) | Decidida por el arquitecto-revisor (2026-11-08), a pedido de Mauricio |
| DEC-50 | Arroz y Pollo al 25 % en V05 | Se sacan de la lista y la tarjeta queda en «3 Stock bajo»; la regla de DEC-43 no cambia | Decidida por el arquitecto-revisor (2026-11-08), a pedido de Mauricio |
| DEC-51 | El pedido guarda qué mesero lo tomó (A4) | `meseroId: Long` en `Pedido`, en la 4b y en el código | Aplicada y aprobada por Mauricio en la fase 1 de la sesión 6: código `22717f7`, 4b `4e4559e`, R-60/R-61; 176/176 |
| DEC-52 | Quién corrige el archivo de casos de uso | Corrección directa del archivo, autorizada por Mauricio, con los 13 problemas de `REVISION_CASOS_DE_USO.md` | Aplicada: RCU-1 a RCU-13; fase 2 aprobada por Mauricio. Precisión de cierre S6-2. Falta el PNG del compañero, señalado según lo acordado |
| DEC-53 | Cómo llega el `meseroId` al pedido (DEC-51) | En el constructor: `Pedido(numero, mesa, meseroId)`; ninguna otra firma cambia | Aplicada y aprobada en la fase 1 de la sesión 6; validada en `VALIDACION_SESION6.md` |
| PD-01 | Alarma WebSocket con varias réplicas de ms-notificaciones (A2) | Difusión por Redis pub/sub; V05 relee las alertas guardadas al abrirse | Decidida por el arquitecto-revisor (2026-11-08); efectos en 1, 3a, 4d y 5, y R-56 |
| PD-02 | Categorías del proveedor | Se mantiene `List<String>`: nombres de las categorías de ms-menu, copiados por valor | Decidida por el arquitecto-revisor (2026-11-08); efecto en CU-09 |
| PD-03 | BAM: periodos fijos frente al rango libre | Sin rango: demanda de 30 días y proveedor de 365; con rango, manda el rango; la sucursal filtra la demanda; solo cotizaciones OPCIONADAS | Decidida por el arquitecto-revisor (2026-11-08); efectos en 4e (`n_kpis`), CU-14 y R-31; código fuera de alcance |
| PD-04 | Promociones: fuente de tendencias y periodo | Job semanal sobre los 7 días anteriores: tendencias de redes y los 3 más pedidos; vigencia de 7 días; supuesto S11 | Decidida por el arquitecto-revisor (2026-11-08); efectos en 4e (`n_promociones`), CU-06 y §17 del reporte |
| PD-05 | T-06: eventos con tipos del dominio de otro servicio | Tipos simples en el contrato (`estado: String`, `diferenciaPorcentual`, `sugerencias: List<String>`); aplicación diferida | Decidida por el arquitecto-revisor (2026-11-08); aplicación diferida, pendiente de autorización de Mauricio (4c, 4e y código) |

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

**Nota (sesión 3, W-36 de `INVENTARIO_SESION3.md`):** la opción B se descartó por claridad, no por un choque técnico. `GET /api/cotizaciones/pendientes` (V16) y `GET /api/cotizaciones/{id}` (H5) tienen la misma forma y funcionan, porque Spring prefiere el segmento literal.

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

## DEC-33 · ¿Dónde se ve qué anotación de Spring lleva cada clase? (K1)

**Por qué hubo que decidir.** El README decía que «las anotaciones de Spring y de JAX-WS van solo en ese comentario», es decir, en el Javadoc. Pero ningún comentario tenía una: el código no usa Spring, para compilar solo con `javac`, y el reporte (p. 26) solo dice en general que «los adaptadores reciben sus anotaciones (@RestController, @Repository, @KafkaListener)».

**Opciones:**
- **A. Cambiar la frase del README** para que diga que el código no usa anotaciones. Es 1 commit, y era la recomendación del revisor.
- **B. Escribir en el Javadoc de cada adaptador la anotación que lleva en el proyecto real**, por ejemplo «En el proyecto real: @RestController». ✅ **Escogida.** Sobre el alcance:
  - **B1. Los 54 adaptadores**, cada uno con su anotación. ✅ **Escogida.**
  - **B2. Solo las 37 clases** de las tres anotaciones que nombra el reporte.

**Qué implica:**
- **Qué anotación lleva cada adaptador:**
  - `@RestController` en los 14 controladores;
  - `@Repository` en los 15 adaptadores JPA;
  - `@Component` en los 8 listeners, con `@KafkaListener` en `alRecibir()`;
  - `@Component` en los 2 jobs, con `@Scheduled` en `ejecutar()`, y desde DEC-36 también `@SchedulerLock`;
  - `@Component` en los otros 15 adaptadores; desde DEC-35, el Proxy de precios lleva además `@Primary`.
- **Sin anotación:**
  - los servicios de aplicación y los 3 decoradores, porque una clase de configuración los arma con sus constructores (DEC-25), y ahí se fija el orden de los decoradores (reporte, p. 22);
  - el cliente SOAP, que lo genera `wsimport`;
  - el endpoint SOAP, que ya decía `@WebService`.
- **Commits:** uno por adaptador (54) y uno para la frase del README.

## DEC-34 · ¿Qué se hace con la historia de `Correo` y `CorreoDecorator` en git? (K4)

**Por qué hubo que decidir.** Antes de moverlos, `Correo.java` y `CorreoDecorator.java` eran esqueletos de 5 líneas. Al cambiarles el `package` quedaron con 30 % y 41 % de similitud. Por eso `git log --follow` no reconoce los movimientos `7cb9e8f` (H25) y `5f50178` (N8), y les asigna la historia de `AlarmaPushPort.java` y `AlarmaPushAdapter.java`. Con `git log --follow -M30%` sí se sigue bien.

**Opciones:**
- **A. Dejar la historia y anotar lo de `-M30%` en el plan.** Era la recomendación del revisor.
- **B. Reescribir la historia** desde el movimiento: primero un commit que solo mueve los archivos y después otro que cambia el `package`. El código queda igual, pero los ~176 commits siguientes cambian de id, y hay que volver a validar.
- **C. Un commit nuevo que lo corrija**, como propuso Mauricio. No es posible: un commit nuevo no cambia cómo git lee los commits viejos.
- **D. Un commit con la nota, en el README de `KonradGourmet` y en el plan.** ✅ **Escogida**, después de explicar por qué C no es posible.

**Qué implica:** la historia no cambia y la validación sigue valiendo. Quien busque de dónde vienen esos dos archivos tiene la nota con el comando.

## DEC-35 · ¿Cómo recibe `ConsultarPrecioService` el Proxy y no el adaptador real? (K9)

**Por qué hubo que decidir.** Desde DEC-33, `PrecioReferenciaCacheProxy` y `MinisterioComercioAdapter` dicen `@Component`, y los dos implementan `PrecioReferenciaPort`. `ConsultarPrecioService` pide un `PrecioReferenciaPort` en su constructor. Con dos candidatos, Spring no arranca (`NoUniqueBeanDefinitionException`), y nada asegura que el servicio reciba el Proxy, que es el que mira primero la caché.

**Opciones:**
- **A. `@Primary` en el Proxy:** cuando hay dos candidatos, Spring entrega el Proxy. Spring no le pasa a una clase su propio objeto, así que el Proxy recibe el adaptador real. Es 1 commit y siguen siendo 54 adaptadores con anotación. ✅ **Escogida**, como recomendaba el revisor.
- **B. El Proxy sin anotación, armado en la clase de configuración** como los decoradores. Se parece más a los decoradores, pero los adaptadores con anotación bajan a 53, y cambian el README, DEC-33 y el seguimiento.

**Qué implica:** el Javadoc del Proxy dice «En el proyecto real: @Component y @Primary: ConsultarPrecioService recibe el Proxy, y el Proxy recibe el adaptador real.», y DEC-33 lo anota. El diagrama y las firmas no cambian.

## DEC-36 · ¿Cómo se asegura que cada job corra en una sola réplica? (K10)

**Por qué hubo que decidir.** `@Scheduled` se ejecuta en cada réplica, y el despliegue (p. 5, `v15`) le da a ms-inventario de 2 a 6 réplicas y a ms-analitica de 2 a 4. Así, el descuento de las 23:59 de `ConsumoDiarioJob` se haría de 2 a 6 veces cada noche; `descontarConsumoDiario()` no recibe la fecha, así que no puede saber si ya se hizo. Las sugerencias semanales de `TendenciasJob` también se repetirían: `notificadaEl` solo evita repetir la notificación. DEC-33 no creó el problema, porque las cajas «Scheduler» ya estaban en el mapa (`ad_inventario_t1`, `ad_analitica_t2`), la 4b y la 4e, pero lo dejó escrito.

**Opciones:**
- **A. `@SchedulerLock` (ShedLock):** antes de correr, la réplica toma un candado en una tabla de su base, y las demás se saltan el turno. Es la solución habitual con `@Scheduled`. Agrega una librería y una tabla en la base de cada servicio: ms-inventario y ms-analitica tienen su PostgreSQL en la 3a (`c3a_db_inventario`, `c3a_db_analitica`). ✅ **Escogida**, como recomendaba el revisor.
- **B. Un CronJob de Kubernetes** en lugar de `@Scheduled`. Cambia lo que dicen las cajas «Scheduler» del mapa, la 4b y la 4e: el mapa ya está aprobado (sesión 2), y la 4b y la 4e no se pueden tocar para esto en esta sesión. Además, el CronJob necesita otra forma de llamar a `ejecutar()`.
- **C. Dejar `@Scheduled` y hacer idempotente el descuento en la sesión 4**, para que repetirlo no haga daño. No evita que dos réplicas corran a la vez: si despiertan en el mismo segundo, las dos ven que falta el descuento.

**Qué implica:** el Javadoc de los dos jobs dice que `ejecutar()` lleva además `@SchedulerLock (ShedLock)`, para que lo ejecute una sola réplica (2 commits), y DEC-33 lo anota. El diagrama y las firmas no cambian. La demo de la sesión 4 corre una sola réplica, así que no necesita nada de esto.

## DEC-37 · ¿Qué ventanas atiende ms-cotizaciones? (K11)

**Por qué hubo que decidir.** K5 cambió el README para que ms-cotizaciones atendiera «V13–V21», porque `CotizacionRestController.detalle()` dice «Atiende la ventana V21 de los mockups». Pero el mapa (`v121` «V13–V20» y `v141` «V21–V23»), la 3a (`c3a_ms_cotizaciones`), el reporte (p. 7) y `ESTADO_PROYECTO_Y_C4.md` le dan V13–V20 y dejan V21 en ms-ordenes. V21 es la ventana donde empieza la orden de compra a partir de una cotización OPCIONADA: ms-ordenes le pide a ms-cotizaciones los datos de la cotización (`GET /api/cotizaciones/{id}`, 3b `c3b_e5`), pero la ventana es de ms-ordenes.

**Opciones:**
- **A. Volver a «V13–V20» en el README**, como los otros cuatro artefactos. El comentario de `detalle()` se queda, porque ese método sí le da datos a V21. ✅ **Escogida**, como recomendaba el revisor, que reconoció que K5 fue una recomendación suya equivocada.
- **B. Dejar «V13–V21» y cambiar el mapa, la 3a y el reporte.** Son páginas ya aprobadas y el reporte no se toca (DEC-03).

**Qué implica:** 1 commit en el README, que revierte el de K5 (`69d8011`). La columna «Ventanas» dice de qué servicio es cada ventana, no a cuáles les da datos.

## DEC-38 · ¿ShedLock y `@Primary` deben aparecer también en el reporte o en algún diagrama? (tercera validación)

**Por qué hubo que decidir.** Después de DEC-35 y DEC-36, `@Primary` y `@SchedulerLock` solo están en el Javadoc del código y en este registro. El reporte (p. 26) nombra `@RestController`, `@Repository` y `@KafkaListener`, pero no estas dos, y ningún diagrama las menciona. El profesor puede preguntar en la sustentación qué pasa con los jobs cuando hay varias réplicas.

**Opciones:**
- **A. Nada ahora, y una frase en el reporte, junto a las anotaciones de la p. 26, si en la sesión 5 aparece el script (DEC-03).** ✅ **Escogida**, como recomendaba el revisor.
- **B. Nada**, ni ahora ni en la sesión 5. La respuesta queda solo en DEC-35 y DEC-36.
- **C. Una nota en la 4b y en la 4e.** Son páginas que en esta sesión no se pueden tocar para esto.

**Qué implica:** el plan de la sesión 5 suma la frase a `CORRECCIONES_REPORTE.md`. El código y el `.drawio` no cambian.

## DEC-39 · ¿Qué métodos de las 191 clases reciben lógica en la sesión 4?

**Por qué hubo que decidir.** Al terminar la sesión 3 había 274 cuerpos que lanzaban `Pendiente de implementar`. El plan pide lógica real en la fachada, la cadena de reglas «y en todo lo que use la demo», pero no dice qué hacer con el resto.

**Opciones:**
- **A. Solo lo que usa el recorrido de compra y los 7 patrones.** Quedan pendientes los adaptadores de tecnología externa (JPA, Kafka, SMTP, OpenPDF, S3, BCrypt, WebSocket, Elasticsearch, clientes REST y SOAP) y los módulos que la demo no toca: autenticación, platos, y promociones (HU-19). ✅ **Asumida.** Queda con lógica lo que el plan y DEC-31 piden, y de los 274 cuerpos quedan 94.
- **B. Los 274.** Los adaptadores de tecnología no se pueden escribir de verdad sin las librerías (el esqueleto compila solo con `javac`), así que habría que inventar versiones falsas dentro de `src/` que parecerían reales.
- **C. Solo el flujo de COT-018.** Es lo que pedía el plan original, pero DEC-31 ya decidió que la demo cubre las 7 salidas.

**Qué implica:** el README tiene la tabla de qué métodos tienen lógica y cuáles no. Para los puertos de salida que la demo necesita, hay adaptadores en memoria en el paquete `demo` (DEC-30), y `Ensamble` los conecta como lo haría la clase de configuración del proyecto real. Si Mauricio prefiere B, el trabajo grande son los 12 `*JpaAdapter` sin atributos, que tendrían que ganar una colección que el diagrama no dibuja.

## DEC-40 · ¿Qué cifras usa la demo cuando los mockups no las fijan?

**Por qué hubo que decidir.** V19 da los totales ($520.000 y $545.000) pero no el reparto por producto, y los mockups se contradicen en el pedido.
- V18 y V19 muestran el valor de la línea (res 20 kg = $400.000, arroz 15 kg = $120.000), pero `ItemCotizacion` tiene `precioEstandar` y `precioProveedor` por producto, y `CotizacionEnviadaEvent.precios` habla del «precio sugerido de cada producto».
- V03 (mesero) muestra un pedido con 2 bandejas paisas, 1 ajiaco y 1 limonada: 4 platos y **$88.000**. V04 dice «Platos: 3». El reporte promete **$82.000**.
- V05 muestra la res con stock 4 kg, que no sirve para el «23 %» que promete el reporte.

**Opciones:**
- **A. Precios por unidad.** Estándar: res $20.000/kg y arroz $8.000/kg. El proveedor cotiza la res a $21.250/kg y el arroz a $8.000/kg (COT-018, $545.000); a $29.000 y $8.000 (COT-019, $700.000, +34,62 %); a $4.000 y $8.000 (COT-020, $200.000, −61,54 %). ✅ **Confirmada por Mauricio**: reproduce los totales de V19 y deja la cantidad en la línea, como el modelo.
- **B. Precios por línea** ($400.000 y $120.000 como en los mockups). El valor de la línea ya no depende de la cantidad, y habría que cambiar lo que significa `precio` en 4c y 4d.
- **C. Otro reparto del $545.000** (por ejemplo res a $21.000 y arroz a $8.333,33). No da cifras exactas por kilo.

Para el pedido: **A. $82.000, con 2 bandejas paisas y 1 ajiaco (3 platos)** ✅ **Confirmada por Mauricio**, porque coincide con el reporte y con «Platos: 3» de V04. **B. $88.000**, con la limonada de V03: contradice el reporte. Para el inventario: la res arranca en 12,5 kg de 50 (25 %) y cada bandeja lleva 0,5 kg, de modo que después de 2 bandejas queda en 11,5 kg (23 %) y la alerta salta con el cierre del día, no antes.

Para el tablero, la demo carga un histórico del mes (Bandeja paisa 22 + 18 = 40, y Proveedor B con el promedio más bajo): el listener que lo armaría con los eventos (`NegocioEventsListener`) sigue pendiente. La alternativa, alimentarlo con `cotizacion.validada`, activaría T-06 y haría ganar a COT-020, que es SOSPECHOSA.

**Qué implica:** los mockups V03 y V18/V19 se comentan en `CORRECCIONES_HU_Y_MOCKUPS.md` (sesión 5): V03 suma $88.000 con 4 platos y V04 dice 3; V18 y V19 muestran el valor de la línea y el modelo guarda el precio por unidad.

## DEC-41 · ¿Cómo cambia una entidad cuando no tiene un método para eso?

**Por qué hubo que decidir.** Tres casos que el diagrama no resuelve con ningún método:
- el número de la orden (OC-0012) lo da la base al guardarla, pero `OrdenCompra.desde()` no lo recibe;
- el `pdfUrl` de la cotización y de la orden se conoce después de guardar el PDF;
- el precio estándar de cada ítem lo completa la fachada, y el precio del proveedor, `registrarValor()`.

**Opciones:**
- **A. Constructores de copia y accesores de lista modificables.** `Cotizacion(base, pdfUrl)`, `OrdenCompra(numero, sinNumero)` y `OrdenCompra(base, pdfUrl)`; `items()` y `cotizacionesAplicadas()` devuelven la lista modificable. ✅ **Asumida**: DEC-25 ya permite constructores y accesores, y no cambia ninguna firma.
- **B. Mutadores** (`asignarNumero()`, `asignarPdfUrl()`). Son métodos que 4a–4e no dibujan; hay que agregarlos a las páginas de clases o dejar el código distinto del diagrama.
- **C. Cambiar las firmas de los puertos** (por ejemplo, que `guardar()` reciba el número). Rompe las páginas aprobadas.

**Qué implica:** el README lo dice. Además, `GenerarCotizacionFacade` tiene un `Logger` privado (para «dejar el error en el log», H18) y algunas clases tienen métodos privados auxiliares (`huella()`, `enviar()`); no se ven desde fuera. `ParametrosSistema.valores()`, `Bitacora.registros()` y `Mensaje.cabeceras()` son accesores.

## DEC-42 · ¿Qué hacer con lo que los contratos entre servicios no alcanzan a llevar?

**Por qué hubo que decidir.** Al conectar los servicios aparecieron tres huecos de diseño:
- **El adjunto.** `Notificacion.enviar(destino, contenido)` solo lleva dos textos, y `Mensaje.adjuntoUrl` no tiene cómo llenarse. El correo certificado al proveedor «con el PDF» no puede llevar la URL en el campo del adjunto.
- **El usuario y la correlación.** Los casos de uso no reciben al usuario autenticado, y no hay cómo pasar el `correlacionId` de un evento al siguiente.
- **T-06.** `TendenciaDetectadaEvent.sugerencias` y `CotizacionValidadaEvent.estado` usan tipos del dominio de otro servicio, y DEC-29 no deja importarlos.

**Opciones para el adjunto:** **A.** El enlace del PDF va en el cuerpo del correo, y `adjuntoUrl` queda vacío (lo que se hizo al principio de la sesión). **B.** Agregar `adjuntoUrl` a `Notificacion.enviar()` y a `CreadorNotificacion.notificar()`: cambia dos contratos de la 4d, reflejados en cuatro declaraciones (`Notificacion`, `AlarmaApp`, `NotificacionCorreo` y `CreadorNotificacion`) ✅ **Escogida por Mauricio el 2026-11-08**, al revisar esta sesión; el código ya la tiene y la página 4d se actualiza en la sesión 5 (hasta entonces el código y el diagrama difieren en esas cuatro declaraciones). **C.** Meter la URL en el contenido con una marca que `NotificacionCorreo` separe: funciona, pero es un truco que nadie lee en el diagrama.

**Opciones para el usuario y la correlación:** **A.** Los eventos llevan `sistema` (o el correo de la solicitud) y cada evento crea su propio `correlacionId` ✅ **Confirmada por Mauricio el 2026-11-08**, como límite de la demo, en la segunda ronda de la sesión 4. **B.** Pasar el usuario y la correlación por parámetro: cambia las firmas de todos los casos de uso.

**Opciones para T-06:** **A.** La demo no consume `cotizacion.validada` ni `tendencia.detectada`: se publica la primera, nadie la lee, y la rama `tendencia.detectada` de `NotificacionService.notificarEvento()` lanza `Pendiente de implementar: … (T-06)` ✅ **Asumida**. **B.** Cambiar `estado` a `String` en la 4c (era la opción B de DEC-29). **C.** Mover los eventos a `compartido.eventos` (opción C de DEC-29).

**Qué implica:** `Notificacion.enviar(destino, contenido, adjuntoUrl)` y `CreadorNotificacion.notificar(destino, contenido, adjuntoUrl)` tienen un parámetro más que la 4d; `AlarmaApp` lo ignora. Los correos a los proveedores llevan el PDF en `Mensaje.adjuntoUrl`. La sesión 5 actualiza la 4d (`Notificacion`, `AlarmaApp`, `NotificacionCorreo` y `CreadorNotificacion`) y lo anota en `PLAN_CORRECCIONES.md`. **Aplicado en la sesión 5 (2026-11-08):** la 4d ya tiene las cuatro firmas con `adjuntoUrl` (`4da992c`) y la nota `n_outbox` de la 4a separa el sistema real del límite de la demo (`f57a89e`); el diagrama y el código vuelven a coincidir.

**Límite confirmado de auditoría y trazabilidad:** el reporte promete usuario en la auditoría (p. 5) y correlación para las trazas (p. 9); la nota `n_outbox` de la 4a dice que `correlacionId` une todos los eventos de un mismo recorrido. La demo usa `sistema` (o el correo de la entidad) y genera una correlación nueva por evento: no identifica siempre al usuario autenticado ni enlaza el recorrido completo. En la sesión 5 se debe distinguir expresamente esa simplificación de lo prometido para el sistema real, tanto en el reporte como en la nota de la 4a. Esta aceptación no elimina la auditoría de escrituras, incluidos los estados pendientes por fallos (S4-13). No se cambian firmas, diagramas ni PDF en esta fase.

## DEC-43 · ¿Qué pasa justo en los límites de la cadena de reglas y del umbral?

**Por qué hubo que decidir.** El diagrama dice «más de 25 % por encima», «más de 50 % por debajo» y «dentro del rango», pero no dice qué pasa con +25,00 % o −50,00 %. Tampoco dice si el 25 % del stock «superado» incluye el 25 % exacto, ni cuándo `descontar()` reactiva la alerta (el Javadoc dice «cuando el insumo vuelve a cruzar el umbral», y el insumo no conoce el umbral).

**Opciones:**
- **A. Lectura literal.** +25,00 % y −50,00 % siguen OPCIONADA (rango −50 a +25, con los dos extremos dentro); +25,01 % es RECHAZADA y −50,01 % es SOSPECHOSA. El stock está «superado» si queda por debajo del 25 % (el 25 % exacto no alerta). `descontar()` reactiva la alerta, y el servicio conserva la atención previa si el insumo ya estaba bajo el umbral (DEC-46). ✅ **Decidida por Mauricio el 2026-11-08**, en la segunda ronda de la sesión 4. Está en `PruebasCotizacion` y en `PruebasFlujoDeCompra`.
- **B. Límites inclusivos hacia afuera** (+25,00 % ya es RECHAZADA, y el 25 % de stock ya alerta). Más cautela, pero no es lo que dice «más de».

**Redondeo — decidido por Mauricio el 2026-11-08.** Se mantiene el redondeo a 2 decimales (`HALF_UP`) antes de comparar, tanto en cotizaciones como en el porcentaje de stock. +25,004 % se ve y se trata como +25,00 % (OPCIONADA). Un stock real de 24,995 % se redondea a 25,00 % y no activa la alarma; 24,994 % se redondea a 24,99 % y sí queda bajo el umbral. La alarma se publica al cruzarlo (DEC-46). Comparar sin redondear sería la alternativa descartada para esta demo (S4-6).

**Qué implica:** no cambia el código ni ninguna firma. En la sesión 5 se corregirá la tabla de eventos del reporte (p. 21), que dice «Existencia ≤ 25 %»: la regla decidida es porcentaje redondeado **< 25 %**, con alarma solo al cruzar el umbral. La descripción de la p. 9 («llega al 25 %») también debe aclararse. El PDF se conserva intacto en esta sesión.

## DEC-44 · ¿Dónde van las pruebas y el script?

**Por qué hubo que decidir.** El plan pide «pruebas simples que no necesiten Maven» y un script. El README y el reporte cuentan 191 clases más la demo, y un paquete de pruebas dentro de `src/` cambiaría la cuenta.

**Opciones:**
- **A. Carpeta `pruebas/` junto a `src/`**, con un marco mínimo propio (`Prueba`) y `ejecutar.sh` ✅ **Asumida.** No cambia los paquetes de los 12 servicios ni la cuenta de clases. 171 verificaciones en 3 clases de prueba.
- **B. Dentro de `src/…/pruebas`.** Cuenta como un paquete más y como más clases en el reporte.
- **C. JUnit con Maven.** Pide Maven, que el plan descarta.

**Qué implica:** `ejecutar.sh` compila el código y las pruebas en `out/` (que `.gitignore` ahora ignora), ejecuta la demo y corre las pruebas. En Windows, el README da las órdenes sueltas.

## DEC-45 · ¿La demo cambia la cuenta de clases del reporte?

**Por qué hubo que decidir.** El plan de la sesión 3 dejó «191 clases más la demo». La demo necesita 19 clases de apoyo (los adaptadores en memoria, el bus y el ensamblador) que no están en 4a–4e.

**Opciones:**
- **A. Dejarlas en `demo/` y no contarlas como clases del diagrama.** Son parte de la demo, como lo era `DemoArquitectura` ✅ **Asumida.** El reporte seguiría diciendo «191 clases» del modelo, más la demo (que ahora son 20 archivos), y la sesión 5 lo anota en `CORRECCIONES_REPORTE.md`.
- **B. Dibujarlas en una página nueva del `.drawio`.** Es mucho trabajo para clases que no son arquitectura.

**Qué implica:** el total de archivos `.java` en `src/` pasa de 192 a 211, y `pruebas/` suma 6 más (fuera de `src/`).

## DEC-46 · Tres reglas que la validación de la sesión 4 encontró mal

**Por qué hubo que decidir.** El revisor (`VALIDACION_SESION4.md`) reprodujo tres comportamientos que contradecían notas del propio diagrama. Cada uno tiene más de una salida correcta, y en las tres se escogió la que no cambia ninguna firma.

**1. ¿Cuándo sale la alarma de stock bajo? (S4-1).** La nota de `InsumoInventario` dice que la alerta sigue activa hasta que llega `solicitud.registrada`, pero el cierre de cada noche volvía a publicar `inventario.stock-bajo` de todos los insumos bajo el umbral, incluso con la alerta atendida.
- **A. Solo cuando el insumo cruza el umbral en esa ejecución.** Una alerta atendida sigue atendida aunque el stock siga bajando. ✅ **Confirmada por Mauricio.**
- **B. Un recordatorio cada noche mientras no haya solicitud.** Es más insistente, pero hay que distinguir «sin atender» de «atendida» en el evento, o el jefe recibe correos de algo que ya pidió.
- **C. Dejarlo como estaba.** La alarma se repite aunque V05 diga 0 alertas.

**2. ¿Qué hace una orden con una solicitud que la cotización cubrió solo en parte? (S4-2).** `completar()` lanzaba «sigue pendiente» y el bucle de `orden.generada` se cortaba: la orden quedaba guardada, pero ni las otras solicitudes se completaban ni la cotización pasaba a CON_ORDEN.
- **A. `completar()` acepta PENDIENTE.** La nota de `GestionarSolicitudUseCase` dice que con `orden.generada` «cada solicitud pasa a COMPLETADA». ✅ **Confirmada por Mauricio.**
- **B. Una solicitud parcial se queda abierta.** Hay que decirle a `completar()` qué productos se compraron, y la firma no tiene dónde llevarlos.
- **C. Obligar a que la cotización cubra todos los productos de cada solicitud.** Cambia V13 (el mockup).

**3. ¿Qué escrituras dejan auditoría? (S4-3).** La p. 6 y la 3a dicen que cada uno de los otros 10 servicios publica `auditoria.evento` después de cada escritura, y solo lo hacían `ProveedorService`, las notificaciones y las consultas de precio.
- **A. Publicarlo en las escrituras del recorrido de compra** (pedido, solicitud, cotización, orden, consumo diario y alertas), incluidas las cotizaciones que quedan pendientes por fallo del precio o del PDF, con su causa (S4-13). ✅ **Confirmada por Mauricio al autorizar la fase 2.** No cambia ninguna firma, porque los servicios ya tienen `EventPublisherPort`.
- **B. Dejarlo como límite anotado en el README.** El reporte seguiría afirmando algo que el código no hace.

**Qué implica:** otros hallazgos del revisor se corrigieron sin decisión de diseño: parámetro nulo (S4-9), número de cotización repetido y fallo del almacén de PDF (S4-10), proveedores de la demo (S4-7), comentarios (S4-8), README (S4-5 y S4-11) y pruebas (S4-4). Los consumidores siguen sin descartar eventos repetidos (S4-5): queda anotado en el README. Las 4 preguntas del revisor para Mauricio están al final de `VALIDACION_SESION4.md`.

## DEC-47 · ¿Cómo queda el plan de sprints del Excel? (H20)

**Por qué hubo que decidir.** El Excel se contradecía con el Story Mapping y consigo mismo (H20): HU-14 valía 2 y no 1 (total 36 y no 35), el PHU valía 30 h en una hoja y 100 h en la otra, «EN SEMANAS 21,43» salía de dividir 3.600 h por una semana de 24 h × 7 días y contradecía las 10 semanas de la otra hoja, y «SOBRAN 2» y «SOBRAN 4» suponían capacidades de 10 y 9 PHU mientras el encabezado decía 8. El profesor pidió «cuántos sprints, cómo lo calcularon y qué fechas tienen», y ninguna fuente da una fecha de inicio. Lo que fija el plan (1 PHU = 25 h, 9 PHU por sprint, HU-14 = 1, HU-17 al sprint 3) no se discutió; sí hubo tres puntos abiertos.

**1. ¿Con qué fecha empieza el sprint 1?** El Story Mapping lleva «Agosto 2026» en la portada final y el reporte dice «Octubre de 2026».
- **A. Lunes 3 de agosto de 2026.** Coincide con el «Agosto 2026» del Story Mapping. Los 5 sprints van del 03/08 al 11/10/2026 (14 días cada uno, de lunes a domingo). ✅ **Decidida por Mauricio.**
- **B. Lunes 16 de noviembre de 2026.** El plan queda hacia adelante (16/11/2026 a 24/01/2027), como la construcción que sigue a esta entrega.
- **C. Celda vacía.** No inventa nada, pero el Excel no muestra fechas hasta que alguien las escriba.

**2. ¿Cómo se muestra «EN SEMANAS»?** Por capacidad, 35 PHU × 25 h = 875 h, y a 224 h por sprint salen 3,9 sprints (4 sprints, 8 semanas). El cronograma recomendado usa 5 sprints (10 semanas), con 10 PHU de holgura (45 de capacidad contra 35). El Story Mapping p. 31 dice 4 sprints y 8 semanas.
- **A. 10 semanas más una fila «Sprints mínimos (capacidad)» = 4.** «EN SEMANAS» = 5 sprints × 2 semanas, que coincide con la otra hoja y con «2 meses y medio»; la fila extra muestra de dónde sale la holgura. Mauricio la escogió primero y se aplicó (commit `21900dd`).
- **B. Solo 10 semanas.** Más simple, pero no explica por qué no bastan 4 sprints si la capacidad los alcanza. ✅ **Decidida por Mauricio el 2026-11-08, al revisar la fase 4:** «acomódalos todos para que sean 5 y 10 semanas». Se quitó la fila de sprints mínimos; la holgura sigue a la vista en la columna «Sobran» del resumen (10 PHU).
- **C. Cambiar el plan a 4 sprints (8 semanas).** Alinea el Excel con la p. 31, pero los 35 PHU caben justos (36 de capacidad), exige repartir distinto a lo que fija el plan y deja el sprint 5 sin contenido.

**3. ¿Dónde queda F17 en la hoja SPRINT?** Las funcionalidades están ordenadas por número y el dibujo en escalera exige que cada sprint ocupe filas seguidas; F17 va en el sprint 3 y F16 en el 4.
- **A. Subir F17 una fila, antes de F16**, y dejar los números F1…F19 como están (siguen siendo los de las HU). El bloque «Sprint 3» (F13, F14, F15, F17) queda contiguo. ✅ **Asumida, por confirmar.**
- **B. Dejar el orden y partir el bloque del sprint 3 en dos tramos.** Una columna «SPRINT» lo diría igual, pero el dibujo quedaría confuso.
- **C. Renumerar las funcionalidades.** Rompe la correspondencia F-n = HU-n.

**Qué implica en el Excel (`Copia de ARQUITECTURA-Supuestos de Capacidad  del equipo.xlsx`):**
- Una sola celda de entrada, «Inicio del Sprint 1» (D15 de la hoja «Supuestos de Capacidad»); las fechas de inicio y fin de los sprints, las duraciones, los PHU por sprint, «SOBRAN», las horas y las semanas son fórmulas. Si el equipo cambia la fecha, todo se recalcula.
- Reparto resultante: sprint 1 = 8 PHU (F1–F8), sprint 2 = 8 (F9–F12), sprint 3 = 6 (F13, F14, F15 y F17), sprint 4 = 8 (F16 y F18) y sprint 5 = 5 (F19); sobran 1, 1, 3, 1 y 4 (10 en total).
- Los entregables del sprint 3 suman «consulta de cotizaciones opcionadas», que antes no aparecía en ningún sprint.
- Las etiquetas «Sprint» → «Duración del sprint (semanas)» y «SPRINT» → «PHU POR SPRINT» se aclararon porque ahora alimentan fórmulas.
- El archivo se guardó con openpyxl: conserva estilos, celdas combinadas y fórmulas, con sus valores calculados, y pierde solo los metadatos propios de Google Sheets. La copia de `../proyecto/contexto/` queda con el contenido anterior.

**Para la fase 5 (`CORRECCIONES_HU_Y_MOCKUPS.md`):** el Story Mapping p. 31 («SPRINT») contradice el Excel: dice 4 sprints, 8 semanas y «2 Meses», con los sprints 2 y 3 «sobrecargados (10 sobre 9)» y los sobrantes de 1 PHU en los sprints 1 y 4. Por decisión de Mauricio, la corrección es que diga 5 sprints, 10 semanas y «2 meses y medio», con el reparto y las fechas del Excel. Hay que anotarlo junto a las etiquetas de H26 (el título «Capacidad … (120)» de la p. 30, y las etiquetas de las pp. 2, 5 y 27). El Story Mapping no se modifica (DEC-03).

## DEC-48 · ¿Dónde van las páginas 3a, 3b y 6 en el reporte?

**Por qué hubo que decidir.** Al escribir `CORRECCIONES_REPORTE.md` (sesión 5) hubo que proponer dónde entran las tres páginas nuevas del `.drawio`. El reporte (p. 4) dice que el C4 es obligatorio en sus cuatro niveles y que «los diagramas dinámico y de despliegue son complementarios», pero solo muestra el contexto (§14), el código (§12 y anexo) y el despliegue (§14). Ninguna fuente dice dónde deben ir.

**Opciones:**
- **A. Cada una junto a lo que explica.** La página 6 en §10, justo después de la tabla «Recorrido completo de una compra», que recorre los mismos 14 pasos; la 3a y la 3b en §14, después del nivel 1, para que el C4 se lea en orden (contexto, contenedores, componentes) antes del despliegue. Las figuras se renumeran del 7 al 14. ✅ **Asumida, por confirmar** (R-02 y R-03).
- **B. Una sección nueva, «El modelo C4»,** con las páginas 2, 3a, 3b y 6 juntas. Ordena mejor el C4, pero mueve el contexto fuera de §14 y cambia la numeración de todas las secciones siguientes.
- **C. En el anexo**, como figuras A6 a A8. Es lo que menos toca el cuerpo del reporte, pero deja los niveles 2 y 3 del C4 fuera del texto, que es justo lo que señaló la revisión.

**Qué implica:** solo cambia `CORRECCIONES_REPORTE.md` (R-02, R-03 y R-05). El PDF no se toca (DEC-03). Si el equipo prefiere B o C, hay que rehacer R-02 y R-03.

## DEC-49 · ¿Qué orden sale de V20 a V22? Los mockups y la demo cuentan historias distintas

**Por qué hubo que decidir.** Al preparar `CORRECCIONES_HU_Y_MOCKUPS.md` (sesión 5) se compararon los mockups V19 a V22 (DISEÑO UXUI, págs. 26 a 29) con la demo, la página 6 y el reporte:
- **V19 (p. 26)** valida **COT-018**, de Proveedor A, con $520.000 contra $545.000: +4,81 %, OPCIONADA. Coincide con todo.
- **V20 (p. 27)** compara tres cotizaciones OPCIONADAS de SOL-031 (Proveedor A $545.000, Proveedor B $510.000, Proveedor C $570.000) y resalta **COT-019**, de Proveedor B (res $395.000 y arroz $115.000, −1,92 %).
- **V21 y V22 (págs. 28 y 29)** generan la orden de **COT-019**, Proveedor B, por **$510.000**, y la orden es **OC-0012**.
- **La demo y la página 6** generan **OC-0012** sobre **COT-018** por **$545.000**. El reporte no une OC-0012 con ninguna cotización, pero en la p. 26 dice «COT-019: +34,62 %, RECHAZADA» (en la demo, COT-019 es de Proveedor C por $700.000), mientras que V20 la muestra OPCIONADA. El proveedor de COT-018 se llama «Proveedor ABC» en la demo y en V13 y V18; V19 y V20 dicen «Proveedor A».

**Opciones:**
- **A. Dejar la demo, la página 6 y el reporte como están, y anotar los mockups V20 a V22 para que se alineen.**
- **B. Cambiar la demo, la página 6 y el reporte a la historia de los mockups.** Toca el código.
- **C. No decidir y dejar ambas versiones.**
- **D. Dejar los mockups y que la demo agregue una segunda opcionada.** (Propuesta del arquitecto.)

**Decisión: A ampliada («A+»).** La tomó el **arquitecto-revisor**, a pedido de Mauricio («actúa como el agente de arquitectura… y toma esas decisiones»), el 2026-11-08. Va como DEC-23 y DEC-24: Mauricio puede revertirla.

**Por qué A+ y no B ni D** (lo midió el arquitecto con datos):
- No hay una «historia de los mockups» coherente que adoptar. La misma historia aparece también en V17 (p. 24), en los fondos de V19, V21 y V22 y en V23 (p. 30, que dice «OC-012» en la lista y «OC-0012» en el detalle), y los mockups se contradicen entre sí: Proveedor C está RECHAZADA en V17 y OPCIONADA con $570.000 en V20; COT-021 es de Proveedor D en V17 y de Proveedor A en el fondo de V22; las fechas de V23 son de 2026 y las demás de 2025; y ninguno trae una cotización SOSPECHOSA, que DEC-31 exige (−61,54 %).
- **B no se puede reproducir con el modelo de precio por unidad** de DEC-40: el arroz de V20 y V21 cuesta $115.000 por 15 kg ($7.666,67/kg), que no es un precio por kilo exacto. Además toca 73 líneas en 6 archivos de código y pruebas, unas 10 etiquetas de la página 6 y la fila de la p. 26 del reporte, y la sesión 5 no modifica el código.
- **D no resuelve el choque:** V21 a V23 seguirían diciendo que OC-0012 es COT-019, de Proveedor B, por $510.000.

**Regla de A+:** un mismo número significa lo mismo en todos los artefactos. Los datos canónicos son los de la demo:

| Número | Proveedor | Total | Diferencia | Estado | Dónde existe |
|---|---|---|---|---|---|
| COT-018 | Proveedor ABC | $545.000 | +4,81 % | OPCIONADA, luego CON ORDEN | demo y mockups |
| COT-019 | Proveedor C | $700.000 | +34,62 % | RECHAZADA | demo y mockups |
| COT-020 | Proveedor D | $200.000 | −61,54 % | SOSPECHOSA | demo y mockups |
| COT-021 | Proveedor B (res $22.000/kg, arroz $8.000/kg) | $560.000 | +7,69 % | OPCIONADA | solo en los mockups, para que V20 compare dos opcionadas (HU-17 pide «dos o más») |
| OC-0012 | COT-018, Proveedor ABC (res $425.000, arroz $120.000) | $545.000 | — | EN PROCESO | demo y mockups |

**Qué implica:** hay que corregir **seis** mockups (V17, V19, V20, V21, V22 y V23; ver M-03 en `CORRECCIONES_HU_Y_MOCKUPS.md`) y unificar «Proveedor A» a «Proveedor ABC». No cambian el código, la demo, la página 6 ni el reporte. **Riesgo para la sustentación:** si Canva no se corrige a tiempo, el choque sigue a la vista; la línea de respaldo es «los datos canónicos son los de la demo; V17–V23 conservan datos ilustrativos anteriores». La demo imprime «Cotizaciones opcionadas (V20): [COT-018]» y V20 mostraría dos: la demo genera un subconjunto de los datos, no contradice ningún número.

## DEC-50 · ¿Qué se hace con Arroz y Pollo al 25 % en V05?

**Por qué hubo que decidir.** V05 (DISEÑO UXUI, p. 10) lista «Alertas de inventario» con Res 23 %, Arroz 25 %, Papa 18 %, Tomate 24 % y Pollo 25 %, y la tarjeta dice «4 Stock bajo». Con DEC-43 (alerta solo si el porcentaje redondeado es estrictamente menor que 25 %), Arroz y Pollo al 25 % no alertan, y la lista tiene 5 filas.

**Opciones:**
- **A. Bajarlos a 24 %.** Crea un choque nuevo: V07 (p. 12) marca Arroz y Pollo como «Normal».
- **B. Sacarlos de la lista de alertas y dejar la tarjeta en «3 Stock bajo».** ✅ **Escogida por el arquitecto-revisor** (a pedido de Mauricio, 2026-11-08).
- **C. Cambiar la regla a «≤ 25 %».** El arquitecto lo probó en una copia: la res arranca en 12,5 kg de 50 (25,00 %), así que ya contaría como baja y nunca cruzaría el umbral; la demo deja de publicar `inventario.stock-bajo`, desaparece la alarma «Res al 23 %» y fallan 2 de las 171 verificaciones. Además revierte DEC-43 (decidida por Mauricio) y toca el código.

**Qué implica:** la lista final de V05 es Res 23 %, Papa 18 % y Tomate 24 % (captura y wireframe), con «3 Stock bajo» (M-06). **Riesgo para la sustentación:** la tarjeta HU-16 dice «llegue al 25 %», el reporte «≤ 25 %» (p. 21) y «llega al 25 %» (p. 9). Se corrigen con R-22 y DEC-43. Respuesta lista: «el umbral se cruza al bajar del 25 %; un insumo en 25,00 % todavía no está bajo; es un parámetro de `UmbralStock`».

## DEC-51 · ¿El pedido guarda qué mesero lo tomó? (A4)

**Por qué hubo que decidir.** La tarjeta HU-01 pide que el pedido quede «asociado a… mi usuario», pero `Pedido` (4b y código) solo guarda número, mesa, fecha, estado y detalles. Lo encontró el arquitecto al revisar los casos de uso (`REVISION_CASOS_DE_USO.md`, problema 9).

**Opciones:**
- **A. Agregar `meseroId: Long` a `Pedido`** en la 4b y en el código. Es pequeño, pero cambia una firma. ✅ **Escogida por Mauricio el 2026-11-08.**
- **B. Dejarlo anotado como límite del diseño**, sin tocar nada. El criterio de HU-01 queda sin cubrir.

**Aplicación:** hecha y aprobada en la fase 1 de la sesión 6: la 4b (`4e4559e`), `Pedido`, `PedidoService.registrar()`, la demo, las pruebas y el README (código `c88c750` a `22717f7`), más R-60/R-61 del reporte. A4 queda resuelta. El cierre confirmó 176/176 y compilación sin avisos; véase `VALIDACION_SESION6.md`.

## DEC-52 · ¿Quién corrige el archivo de casos de uso?

**Por qué hubo que decidir.** `Casos de uso — Konrad Gourmet.md` lo escribió un compañero de Mauricio. El arquitecto lo dio por «bien con correcciones», con 13 problemas, y no se puede entregar sin corregir el estado del prototipo y sin el diagrama.

**Opciones:**
- **A. Se lo pasa Mauricio a su compañero**, que aplica las correcciones.
- **B. Lo corrige Claude directamente sobre el archivo.** ✅ **Escogida por Mauricio el 2026-11-08.**

**Aplicación:** RCU-1 a RCU-13 en `Casos de uso — Konrad Gourmet.md`, con un commit por problema (`689ed75` a `de33625`), más S6-2 (`80b5cbe`) que precisa la postcondición de CU-08 ante fallos. Mauricio aprobó la fase 2 y confirmó que el autor es Carlos Franco. La continuación RCU-11 a RCU-13 y el cierre se hicieron con Codex; la autorización de corrección directa y su alcance se mantienen. El diagrama UML sigue en poder del compañero: queda el enlace a `casos_de_uso.png` y la nota de exportación pendiente; no se presenta como entregado.

## DEC-53 · ¿Cómo llega el `meseroId` al pedido?

**Por qué hubo que decidir.** DEC-51 agrega `meseroId: Long` a `Pedido`, y el plan dice que `PedidoService.registrar()` lo llene con el mesero. Había dos formas de hacerlo, y cada una cambia firmas distintas de la 4b. `pedidosDelTurno(meseroId)` ya existía, pero la demo devolvía todos los pedidos porque el pedido no guardaba al mesero.

**Opciones:**
- **A. En el constructor:** `Pedido(numero, mesa, meseroId)`. El controlador saca el mesero del token JWT al armar el pedido; `validar()` lo exige; `registrar()` lo deja en la auditoría; `delTurno()` filtra por él. En la 4b solo cambia `Pedido` (un atributo). ✅ **Escogida por Mauricio el 2026-11-08.**
- **B. Como parámetro de `registrar()`:** `registrar(p: Pedido, meseroId: Long)` en `RegistrarPedidoUseCase`, `PedidoService` y `PedidoRestController`, y `Pedido.asignarMesero(meseroId)`. Cambian cuatro clases de la 4b y la fila de §15 del reporte.

**Qué implica:** código `c88c750` a `22717f7` (Pedido, servicio, `PedidoEnMemoria`, demo, 5 pruebas nuevas: 176 verificaciones, compila sin avisos, README); `.drawio` `4e4559e` (4b, PNG en `exportes/sesion6/4b.png`); `CORRECCIONES_REPORTE.md` R-60 y R-61. `RegistrarPedidoUseCase`, `PedidoService`, `PedidoRestController` y `PedidoRegistradoEvent` no cambian: inventario no necesita saber quién tomó el pedido.

## PD-01 · ¿Cómo llega la alarma por WebSocket con varias réplicas de ms-notificaciones? (A2)

**Por qué hubo que decidir.** ms-notificaciones tiene de 2 a 4 réplicas (página 5, `v15`) y consume `inventario.stock-bajo` en un solo grupo de Kafka: el evento lo procesa una réplica, pero la conexión WebSocket del jefe de cocina puede estar abierta en otra. HU-16 solo pide una «alarma visible dentro de la aplicación», y V05 ya consulta `GET /api/inventario/alertas`.

**Opciones:**
- **A. Sesión pegajosa en el balanceador.** No resuelve el problema: fija el navegador a una réplica, pero la réplica que procesa el evento la decide Kafka por partición.
- **B. Difusión con Redis pub/sub.** La réplica que consume el evento envía el correo una vez y llama a `empujar()`; el adaptador publica la alarma en un canal Redis y cada réplica la entrega a las conexiones del destinatario que tenga abiertas. ✅ **Escogida por el arquitecto-revisor.**
- **C. Un grupo de Kafka por réplica para la alarma.** Duplica los correos, salvo que se parta el flujo de la 4d.
- **D. Un tópico nuevo de alarmas.** Rompe los 10 tópicos.
- **E. Quitar el WebSocket y consultar periódicamente desde V05.** Deshace el producto `AlarmaApp` del Factory Method.

**Qué implica:** la alerta guardada en ms-inventario sigue siendo la fuente de verdad; V05 la lee al abrirse, así que no se pierde si el jefe no estaba conectado (el cierre corre a las 23:59). El gateway valida el JWT al abrir la conexión. Ninguna firma cambia: todo queda dentro de `AlarmaPushAdapter`. Se pierde independencia: ms-notificaciones depende de Redis (si cae, se pierde el aviso en tiempo real, no la alerta), y Redis queda compartido con la caché de precios, en canal y usuario separados.
- `.drawio`: 3a (Redis con el canal y una flecha desde ms-notificaciones), página 1 (`e231`), página 5 (`v25`) y una nota junto a `AlarmaPushAdapter` en la 4d.
- Reporte: R-56 pasa a «Cambiar», con una fila de riesgo nueva.
- Fuera de alcance: la implementación del adaptador, que sigue pendiente (DEC-39).

## PD-02 · ¿`Proveedor.categorias` sigue siendo `List<String>`?

**Por qué hubo que decidir.** HU-15 pide «asociar una o varias categorías de producto que maneja» el proveedor, sin criterios de filtrado. `Proveedor.categorias: List<String>` (4c y `Proveedor.java`) no apunta a `Categoria`, que vive en ms-menu. Los casos de uso lo dejaban abierto (RCU-10).

**Opciones:**
- **A. Mantener `List<String>`**, con los nombres de las categorías del catálogo de ms-menu, copiados por valor al registrar el proveedor y sin validarlos contra ms-menu. ✅ **Escogida por el arquitecto-revisor.**
- **B. `List<Long> categoriaIds`** que apunten a ms-menu. Obliga a buscar los nombres para mostrarlos y cambia la 4c, el código y las pruebas; entre microservicios tampoco hay clave foránea.
- **C. Una copia local del catálogo alimentada por eventos.** Exige un tópico nuevo o ampliar `menu.actualizado`.

**Qué implica:** no cambia el diagrama ni el código. Se pierde que un cambio de nombre en ms-menu se propague; el dato es informativo. El listado de categorías para los desplegables es el hallazgo N2 (efecto futuro). Se actualiza CU-09 en los casos de uso.

## PD-03 · ¿Cómo conviven los periodos fijos de los indicadores con el filtro libre por fechas? (BAM)

**Por qué hubo que decidir.** HU-18 pide «último mes» y «último año» para los indicadores y, además, «filtrar por rango de fechas y por sucursal». La 4e tiene `kpis(desde, hasta, sucursalId)`; la demo llama a `kpis(null, null, null)`. DEC-40 advertía que, si el tablero se alimentara con `cotizacion.validada`, el «mejor precio» lo ganaría COT-020, que es SOSPECHOSA.

**Opciones:**
- **A. Periodos por defecto que un rango elegido reemplaza.** ✅ **Escogida por el arquitecto-revisor.**
- **B. Periodos fijos siempre, con el rango solo para la demanda.**
- **C. Rango libre siempre, con «último mes» como valor inicial.**

**Regla escogida:**
- Sin rango, el producto y la sucursal con mayor demanda se calculan sobre los últimos 30 días, y el proveedor con mejores precios sobre los últimos 365.
- Con rango, los tres usan ese rango, y la pantalla muestra el periodo aplicado.
- La sucursal filtra solo la demanda: las compras son centrales y `cotizacion.validada` no trae sucursal.
- «Mejores precios» es la menor diferencia promedio contra el precio estándar, contando solo las cotizaciones **OPCIONADAS**.
- Sin hechos en el periodo: «No hay información suficiente para el periodo seleccionado».

**Qué implica:** cumple los dos criterios de HU-18 sin cambiar firmas, y la salida de la demo no cambia (sus hechos tienen la fecha de hoy). Se pierde filtrar el indicador de proveedor por sucursal. Efectos: nota `n_kpis` de la 4e, CU-14 y R-31. Fuera de alcance: que `TableroService.kpis()` resuelva los periodos por defecto y que `NegocioEventsListener` filtre las OPCIONADAS (requiere PD-05).

## PD-04 · ¿De dónde salen las tendencias de las promociones y sobre qué periodo? (HU-19)

**Por qué hubo que decidir.** HU-19 nombra «redes sociales» y «productos más solicitados», pero no fija la red, la ventana ni la cantidad. La 4e ya tiene `TendenciasPort.productosEnTendencia()`, `RedesSocialesAdapter`, `TendenciasJob` («semanal») y `notificadaEl`. CU-06 lo dejaba «por definir».

**Opciones:**
- **A. Las dos fuentes, con una ventana semanal.** ✅ **Escogida por el arquitecto-revisor.**
- **B. Solo redes sociales.**
- **C. Solo la demanda interna.**
- **D. Una ventana diaria.** Reacciona el mismo día, pero no coincide con el job semanal.

**Regla escogida:** el job semanal analiza los **7 días anteriores** y sugiere los productos en tendencia que entrega `TendenciasPort` y los **3 más pedidos** según los hechos DEMANDA_PRODUCTO de ms-analitica. El motivo dice «Tendencia en redes» o «Entre los 3 más pedidos de la semana»; cada sugerencia vale 7 días y se avisa a los jefes de la libreta (DEC-13).

**Supuesto S11 (sin respaldo en las fuentes):** la red social concreta y los valores 7 y 3; son constantes de ms-analitica, como `UmbralStock`. Si el enunciado nombra una red, sustituye el supuesto.

**Qué implica:** usa solo piezas ya dibujadas. Efectos: nota `n_promociones` de la 4e, CU-06 y el supuesto S11 en §17 del reporte. Fuera de alcance: `detectarTendencias()`, que sigue pendiente.

## PD-05 · ¿Cómo se resuelve T-06? (eventos con tipos del dominio de otro servicio)

**Por qué hubo que decidir.** DEC-42 dejaba T-06 asumida: `CotizacionValidadaEvent.estado` es `EstadoCotizacion` y `TendenciaDetectadaEvent.sugerencias` es `List<SugerenciaPromocion>`, tipos del dominio de su servicio, y DEC-29 prohíbe que otro servicio los importe. Además, el evento validado no trae la diferencia ni el precio estándar (hallazgo N1), así que ms-analitica no puede calcular el indicador de PD-03.

**Opciones:**
- **A. Seguir sin consumidores** (lo asumido en DEC-42).
- **B. Tipos simples en el contrato**, ampliada: `CotizacionValidadaEvent` con `estado: String` y `+ diferenciaPorcentual: BigDecimal`; `TendenciaDetectadaEvent` con `sugerencias: List<String>`, líneas «producto · motivo · vigencia» listas para el correo. ✅ **Escogida por el arquitecto-revisor, con aplicación diferida.**
- **C. Mover los eventos a `compartido.eventos`.** Es un cambio grande en 4a–4e y en el mapa.

**Qué implica:** T-06 queda decidida. Mientras no se aplique, A sigue siendo el límite declarado de la demo. Aplicarla cambia a la vez la 4c, la 4e y el código (los dos eventos, la llamada en `CotizacionService.validar()` y la rama T-06 de `NotificacionService`); son unas 10 líneas y cabe en la demo, pero no hace falta: **solo se aplica si Mauricio lo autoriza**, porque el alcance actual es solo la demo (DEC-39). Se pierde el tipado fuerte del estado en el consumidor.

---

## Preguntas abiertas (sin decidir)

| # | Pregunta | Por qué importa | Quién |
|---|---|---|---|
| A1 | ¿Quién tiene el script de ReportLab que generó el reporte? | Sin él no se puede corregir el PDF (DEC-03) | Equipo |
| A2 | Con 2–4 réplicas de ms-notificaciones, ¿cómo llega la alarma por WebSocket a la réplica que tiene abierta la sesión del jefe? | **Resuelta por PD-01** (Redis pub/sub; V05 relee las alertas) | — |
| A3 | Los commits del repo son de la cuenta `mari00aa120-eng`. ¿Es de alguien del equipo? | Conviene saberlo antes de la sustentación | Equipo |
| A4 | `Pedido` no guarda qué mesero lo tomó | **Resuelta por DEC-51 y DEC-53** (aplicada en la sesión 6) | — |
