# Validación de la sesión 6 · 2026-11-08

Revisión crítica realizada por Codex siguiendo los criterios del perfil local `arquitecto-revisor`: contrastar con las fuentes, separar diseño de implementación y proponer cambios mínimos. No se atribuye esta revisión a un agente independiente. Base revisada: código `22717f7`; documentos hasta RCU-13 (`de33625`), con los ajustes de cierre S6-1 a S6-5. Rama local `correcciones-revision` en ambos repos.

**Veredicto: cierre local conforme al alcance autorizado, con un entregable pendiente.** El pedido conserva el mesero, los casos de uso incorporan las 13 correcciones y el código compila sin avisos con 176/176. El diagrama UML de casos de uso **no está entregado**: RCU-2 cumple la alternativa autorizada de dejar el enlace y una nota. El documento no debe presentarse como completo para la entrega académica mientras falte esa imagen. El reporte PDF también sigue pendiente de regeneración, como ya establece DEC-03.

Mauricio aprobó las fases 1 y 2 y autorizó el cierre y el push a `MauItu/KonradGourmet-FinalProyecto`, rama `main`. No se cambian firmas ni decisiones en esta fase.

## Código y pedido con mesero

Se ejecutó `./ejecutar.sh` sobre `KonradGourmet/` en `22717f7`, con resultado 0:

```text
== Compilando (javac --release 21 -Xlint:all) ==
Compiló sin errores ni avisos.
Pedido PED-0251 registrado · Mesa 5 · mesero 3 · 3 platos · total $82.000 · EN_PREPARACION
176 verificaciones: 176 bien, 0 mal.
```

Los avisos `WARNING` durante las pruebas de Ministerio y almacén de PDF son fallos provocados para verificar la recuperación; no son avisos del compilador ni pruebas fallidas.

Evidencias contrastadas en `codigo/src/` del repo público (`KonradGourmet/src/` local):

- `pedidos/dominio/Pedido.java`: atributo y accesor `meseroId`, constructor `Pedido(String numero, Mesa mesa, Long meseroId)` y rechazo de `null` en `validar()`.
- `pedidos/aplicacion/PedidoService.java`, `registrar()`: valida antes de guardar y escribe «Pedido … registrado por el mesero …» en la acción de `AuditoriaEvent`. El campo `usuario` del evento continúa siendo `sistema`, según DEC-42.
- `demo/PedidoEnMemoria.java`, `delTurno()`: filtra por `meseroId`. No implementa una delimitación temporal de turnos; es el adaptador en memoria de la demo.
- `pruebas/…/PruebasFlujoDeCompra.java`: las cinco verificaciones nuevas cubren mesero guardado, consulta del mismo mesero, exclusión de otro, acción auditada y rechazo del pedido sin mesero. Se ejecutaron junto con las 171 anteriores.
- El controlador REST, el puerto de entrada y `PedidoRegistradoEvent` conservan sus contratos. `PedidoRestController.registrar(Pedido)` delega; **no extrae un JWT real**. La extracción del usuario autenticado es el diseño del adaptador real; la demo entrega el id explícitamente. No se afirma que haya autenticación funcionando.
- Recuento: 211 archivos Java en `src/`, 20 en `demo/`, 191 del modelo; 94 cuerpos pendientes en 45 archivos. El conteo incluye `Pendiente de implementar: tendencia.detectada (T-06)`, además de los 93 con el mensaje corto. El alta de usuarios, login, menú y promociones conservan los límites declarados en el README.

No se modificó código en el cierre; su HEAD sigue en `22717f7`.

## 4b, PNG y correcciones del reporte

- XML válido, 11 páginas. Comparado con `f57a89e` (cierre de la sesión 5), solo cambió la celda `cls_pedidos_Pedido` de la página `p4`: añade `- meseroId: Long` y amplía la altura de 158 a 171. Las otras páginas y celdas no cambiaron.
- La 4b conserva 88 cajas, 48 flechas y 39 clases. No cambia el total de 191 clases de 4a–4e.
- Se inspeccionó `exportes/sesion6/4b.png` (4111 × 2010) y el recorte de `Pedido`: el atributo se ve y los métodos no quedan cortados. El PNG permanece fuera de los repos, según la organización existente; la fuente `.drawio` sí se publica.
- **R-60:** la fila V03 debe explicar el mesero asociado al pedido y su origen previsto en el JWT. Es coherente con DEC-51/53 y `Pedido`; no prueba una integración JWT implementada.
- **R-61:** conserva como opcional el supuesto S9: la SPA no decide el mesero, el adaptador lo obtiene del JWT. No añade una firma ni una decisión nueva.
- **R-42:** el texto para regenerar el reporte aún decía 171 aunque la tabla inicial decía 176. Se corrigió en S6-1 (`5e607ef`) y se actualizó la referencia de la comprobación de cifras.
- Los R-60/R-61 son instrucciones para el reporte futuro. No se modificó el PDF, el `.drawio` original, los PDF de mockups/flujo ni el Story Mapping.

## Los 13 problemas, uno por uno

Las referencias RCU identifican los commits del repo `documentos/`. La revisión original se conserva como registro histórico en `REVISION_CASOS_DE_USO.md`.

| Problema | Evidencia y comprobación | Resultado |
|---|---|---|
| 1 · Estado desactualizado | RCU-1 `689ed75`: el encabezado cita código `22717f7`, 176 verificaciones, 94 cuerpos pendientes y distingue casos con lógica de los pendientes. Las carpetas locales y la carpeta pública `codigo/` se explican | Corregido; cifras verificadas de nuevo |
| 2 · Diagrama y enlace privado | RCU-2 `87245d9`: desaparece `claude.ai`; queda `![Diagrama de casos de uso](casos_de_uso.png)` con la nota que exige 19 CU, 6 roles y 5 externos | Alternativa autorizada aplicada; **PNG aún pendiente** del compañero |
| 3 · Umbral de inventario | RCU-3 `cbeace2`: CU-05 dice porcentaje redondeado, estrictamente menor que 25 % y una alarma al cruzar. CU-15 y HU-20 no administran ese umbral. `UmbralStock.superado()` usa `< 0` al comparar y `InventarioService` detecta el cruce | Corregido, coherente con DEC-43/46 |
| 4 · Validación de cotizaciones | RCU-4 `7bf87af`: precondición ENVIADA y límites +25,00/−50,00 incluidos en OPCIONADA; las reglas `ReglaRechazada`, `ReglaSospechosa` y `ReglaOpcionada` coinciden | Corregido; las pruebas de límites pasan |
| 5 · Orden y fallos de CU-08 | RCU-5 `be66dcf`: precio antes del PDF, almacenamiento, envío y efecto en solicitudes. `GenerarCotizacionFacade.generarYEnviar()` deja PENDIENTE_PRECIO y audita los fallos; permite reenvío. S6-2 `80b5cbe` precisa que el fallo no publica `cotizacion.enviada` ni aplica valores a solicitudes | Corregido y precisado durante el cierre |
| 6 · Usuarios y destinatarios | RCU-6 `c8ab0f1`: CU-19 figura en ambos cuadros, catálogo y especificación; CU-15 incluye destinatarios por sucursal. Contratos de `UsuarioRestController`, `RegistrarUsuarioUseCase`, `PoliticaContrasena` y `CifradorPort` coinciden | Corregido como especificación; `RegistrarUsuarioService` sigue pendiente, declarado en el cuadro |
| 7 · V10, V15 y V16 | RCU-7 `510ab5d`: flujos alternos de CU-04, CU-08 y CU-11 con sus rutas y ventanas en el catálogo | Corregido; el catálogo contiene V01 a V23 |
| 8 · Orden repetida y solicitudes | RCU-8 `bd26752`: el alterno reproduce el rechazo de `OrdenCompraService.generar()`. Postcondición CON ORDEN y solicitudes COMPLETADAS coincide con listeners y DEC-46 | Corregido; no queda «que extraje» |
| 9 · Pedido sin usuario | RCU-9 `1ac557b`, DEC-51/53: CU-02 conserva sucursal por la mesa y mesero por `meseroId`. Las cinco verificaciones nuevas confirman el comportamiento de la demo | Resuelto; JWT real sigue fuera de la demo |
| 10 · Decisiones desactualizadas | RCU-10 `2765368`: documenta mesero, ShedLock, `@Primary`, `cotizacionesAplicadas` y dueño del umbral; mantiene categorías de proveedor como texto | Corregido; no se decide transformar `List<String>` |
| 11 · Referencias sin definición | RCU-11 `97b1d5d`: define RF-01 a RF-12 por su uso, enlaza fuentes locales, elimina B/H/K/D sin definir y conserva S2/S3 con referencia a §17. Aclara que §3 es no funcional y que falta el enunciado original | Corregido sin inventar una matriz ni códigos DEC |
| 12 · Efecto en la alerta | RCU-12 `c14d003`: CU-04 añade la atención de la alerta al consumir `solicitud.registrada`. `InventarioEventsListener` llama a `atenderAlertas(sucursalId, productoIds)` | Corregido; el efecto depende del consumo del evento |
| 13 · Forma | RCU-13 `de33625`: Carlos Franco y fecha confirmados, HU-03 menciona la mesa en el consecutivo y CU-16 explica `sistema`/JWT (DEC-42) | Corregido; Laura/Isabella se conserva como pendiente de Mauricio |

Comprobación del archivo: 19 especificaciones CU-01 a CU-19, 12 filas RF y cobertura de las 23 ventanas. No quedan `claude.ai`, `726c4b0`, `274 métodos`, «sesión 4 sigue pendiente», «18 casos», «cinco restantes» ni códigos internos B/H/D/K sin definir. Los enlaces a fuentes locales existen; la única imagen enlazada ausente es `casos_de_uso.png`, declarada pendiente. `git diff --check` no encontró errores de espacios. La copia del documento fuera de `documentos/` no se editó.

## Hallazgos del cierre y límites de la conclusión

| Id | Hallazgo | Tratamiento |
|---|---|---|
| S6-1 | R-42 conservaba 171 verificaciones y el encabezado citaba una comprobación anterior | Corregido en `5e607ef`: 176 y referencia a esta validación |
| S6-2 | CU-08 garantizaba «valor sugerido registrado» incluso en el alterno de fallo; la fachada retorna sin publicar el evento | Corregido en `80b5cbe`: postcondiciones separadas por resultado, sin cambiar código |
| S6-3 | `ESTADO_PROYECTO_Y_C4.md` aún describía 171 pruebas, 18 CU, A4 abierta y sesión 5 en curso | Actualizado con el estado comprobado, 61 correcciones del reporte y pendientes de entrega |
| S6-4 | DEC-51/52 seguían pendientes de aplicación; DEC-53 no reflejaba su validación | Estados actualizados, conservando las opciones decididas y sus límites |
| S6-5 | Sesión 6 y fila de Avance seguían «por hacer» | Fases aprobadas, cierre local y publicación autorizada registrados |

**Pendientes externos al cierre:**

- Carlos Franco/equipo: exportar el diagrama UML y contrastarlo con los 19 casos antes de entregar. No se validaron sus relaciones UML, porque la imagen no existe.
- Mauricio: decidir si unifica Laura Gómez (demo) e Isabella Castro (V16–V23). No se cambiaron nombres ni mockups.
- Equipo: aplicar R-01 a R-61 al reporte cuando aparezca el script (A1); confirmar los supuestos que siguen abiertos. R-58 también deberá reflejar el uso de Codex en esta continuación cuando el equipo apruebe la declaración de IA.
- No se revalidaron las 19 tarjetas del Story Mapping ni todas las capturas de UX. CU-02 expresa el criterio de HU-03 sobre mesa/consecutivo, pero el prototipo recibe `PED-0251` explícitamente y no implementa una regla para codificar la mesa en el número. La especificación no equivale a afirmar que toda la interfaz esté construida.
- El filtrado por mesero no constituye por sí solo autorización. La integración real con JWT, persistencia, Kafka y los restantes 94 cuerpos sigue fuera del alcance de la demo.

## Publicación y trazabilidad

La integración se prepara en un clon temporal de `https://github.com/MauItu/KonradGourmet-FinalProyecto.git`, cuyo `main` inicial es `2af02d0`. Los remotos `documentos` y `codigo` apuntan a los dos repos locales; sus ramas `correcciones-revision` se integran mediante `git merge -X subtree=documentos/` y `git merge -X subtree=codigo/`, conservando los commits por corrección. Se exige comparar los árboles integrados con los HEAD locales antes del push HTTPS y comprobar después el HEAD de `refs/heads/main` en el servidor. El resultado de esa comprobación se informa en el cierre de la conversación. No se publica al remoto de `iCastro117` ni se cambian los `main` locales.
