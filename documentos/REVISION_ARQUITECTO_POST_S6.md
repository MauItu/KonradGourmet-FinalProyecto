# Revisión del arquitecto · puntos 4 y 5 de los pendientes posteriores a la sesión 6

**Quién la ejecutó:** el agente `arquitecto-revisor` de Claude Code (perfil `../.claude/agents/arquitecto-revisor.md`, modelo Claude Opus 5.5), el 2026-11-08. La sesión principal le encargó la revisión y el registro de sus resultados, también con Claude Code. No es una revisión de Codex.

**Autorización:** Mauricio Iturriza autorizó al arquitecto a resolver las decisiones de diseño y los supuestos pendientes de `PENDIENTES_POST_SESION6.md` (puntos 4 y 5), respetando sus decisiones ya confirmadas, y le pidió no inventar información factual del equipo.

**Base revisada:** documentos `779d86c`; código `22717f7`. El revisor solo leyó; sus temporales quedaron fuera del proyecto. Las decisiones se registran en `DECISIONES.md` como PD-01 a PD-11, y sus efectos se aplican en commits aparte, uno por corrección.

**Veredicto:** todas las decisiones se pueden cerrar sin tocar el código de la demo. Lo que sí tiene efecto en código (PD-03, PD-05 y PD-08, y parte de PD-01) queda como efecto futuro, fuera de alcance. Antes de publicar R-58 hay que corregirla: su texto actual diría algo falso.

**Comprobaciones del revisor:**
- `javac --release 21 -Xlint:all` compila sin avisos, en un temporal.
- La demo termina bien (rc 0) y pasan las 176 verificaciones.
- Hay 211 archivos `.java`: 191 del modelo y 20 de la demo (`DemoArquitectura` y 19 de apoyo). Quedan 94 cuerpos pendientes en 45 archivos.
- En la hoja SPRINT del Excel, F17 va entre F15 y F16.
- Se leyeron las tarjetas HU-11, HU-12, HU-15, HU-16 y HU-19 del Story Mapping, y HU-18 en `principalesHU.md`.

---

## Punto 4 · Decisiones de diseño

### PD-01 · A2: alarmas por WebSocket con varias réplicas de ms-notificaciones

- **Evidencia:**
  - el mapa dibuja `e231` «WebSocket · alarma en la app (V05)», y la 3a, `c3a_e16`;
  - en la 4d, `AlarmaPushAdapter.empujar(destino, contenido)` lleva el estereotipo «WebSocket → V05»;
  - el despliegue (`v15`) da a ms-notificaciones de 2 a 4 réplicas;
  - la tarjeta HU-16 solo pide una «alarma visible dentro de la aplicación», y V05 ya consulta `GET /api/inventario/alertas`;
  - `NotificacionListeners` consume en un solo grupo de Kafka: cada evento lo procesa una sola réplica.
- **Opciones:**
  - a) sesión pegajosa en el balanceador;
  - b) difusión con Redis pub/sub;
  - c) un grupo de Kafka por réplica para la alarma;
  - d) un tópico nuevo de alarmas;
  - e) quitar el WebSocket y que V05 consulte periódicamente.
- **Decisión (nueva): b.**
  - La réplica que consume `inventario.stock-bajo` envía el correo una vez y llama a `empujar()`.
  - El adaptador publica la alarma en un canal Redis. Cada réplica la recibe y la entrega a las conexiones que tenga abiertas el destinatario.
  - El gateway valida el JWT al abrir la conexión.
  - La fuente de verdad sigue siendo la alerta guardada en ms-inventario. V05 la lee al abrirse, así que la alarma no se pierde si el jefe no estaba conectado; la tarea corre a las 23:59.
- **Por qué descarta las demás:**
  - a) no resuelve el problema. La sesión pegajosa fija el navegador a una réplica, pero el evento lo procesa la réplica a la que Kafka asignó la partición, que puede ser otra.
  - c) duplica los correos, salvo que se parta el flujo de la 4d.
  - d) rompe los 10 tópicos.
  - e) obliga a deshacer el producto `AlarmaApp` del Factory Method.
- **Qué se pierde:**
  - ms-notificaciones pasa a depender de Redis. Si Redis cae, se pierde el aviso en tiempo real, pero no la alerta.
  - Redis queda compartido con la caché de precios, en un canal y con un usuario separados.
  - El cambio queda dentro del adaptador: no cambia ninguna firma.
- **Efectos:**
  - **`.drawio`:**
    - 3a: «Redis [caché de precios · canal pub/sub de alarmas]», más una flecha desde ms-notificaciones;
    - página 1: `e231`;
    - página 5: `v25`;
    - 4d: una nota junto a `AlarmaPushAdapter`.
  - **Reporte:** R-56 pasa a «Cambiar», más una fila de riesgo.
  - **Fuera de alcance:** el adaptador, que sigue pendiente.
- **Pendiente:** nada.

### DEC-20 · Eureka y la SPA en las zonas

- **Evidencia:**
  - página 5: `v43` y `v44` (Eureka ×1² y SPA ×1² en las zonas A y B), `v45` (la zona C no los tiene) y `v46` (²: asumido);
  - `v13` dice Eureka ×2, y `v21`, SPA ×2;
  - reporte, p. 27: «dos del Service Registry».
- **Opciones:**
  - a) confirmar 2 réplicas en dos zonas distintas;
  - b) ×3, una por zona;
  - c) dejarlo como supuesto.
- **Decisión: confirmación de (a).** «A» y «B» son etiquetas ilustrativas; la regla es la antiafinidad entre zonas.
- **Por qué:**
  - Si cae una zona, queda una réplica de cada uno.
  - Los clientes de Eureka conservan su copia del registro y la renuevan cada 30 s.
  - La CDN sigue sirviendo la SPA.
  - Con ×3 habría que cambiar `v13`, `v21`, la 3a y la p. 27, a cambio de poco.
- **Qué se pierde:** la redundancia si caen dos zonas a la vez, que ya cubre el centro de datos alterno.
- **Efectos:**
  - **`.drawio`:** el texto ² de `v46`.
  - **Documentos:** quitar el «por confirmar» de DEC-20.
  - **Reporte:** R-49 deja de estar «Por confirmar».

### DEC-48 · Páginas 3a, 3b y 6 en el reporte

- **Evidencia:**
  - reporte §2 (p. 4): «el modelo C4 es obligatorio en sus cuatro niveles…»;
  - `General1.pdf`, líneas 270–283: los cuatro niveles y los tres complementarios.
- **Opciones:**
  - A) cada página junto a lo que explica;
  - B) una sección «Modelo C4»;
  - C) en el anexo.
- **Decisión: confirmación de A, con un ajuste.**
  - En §2 va una tabla «Dónde está cada nivel del C4».
  - §14 se renombra «Modelo C4: contexto, contenedores, componentes y despliegue».
- **Por qué:** se gana lo de B sin mover secciones. C esconde justo los niveles 2 y 3.
- **Qué se pierde:** el C4 no queda todo junto en una sola sección.
- **Efectos:** R-62, nueva, con la tabla de abajo, y el título de §14 en R-05.

| Nivel | Página del .drawio | Figura | Sección |
|---|---|---|---|
| 1 Contexto | 2 | 11 | §14 |
| 2 Contenedores | 3a | 12 | §14 |
| 3 Componentes | 3b | 13 | §14 |
| 4 Código | 4a–4e | 8–10 y A1–A5 | §12 y anexo |
| Dinámico | 6 | 7 | §10 |
| Despliegue | 5 | 14 | §14 |

### PD-02 · Categorías del proveedor

- **Evidencia:**
  - `Proveedor.categorias: List<String>`, en la 4c y en `Proveedor.java:13`;
  - HU-15: «asociar una o varias categorías de producto que maneja», sin ningún criterio de filtrado;
  - `Categoria` vive en ms-menu, y ningún servicio lista las categorías (N2).
- **Opciones:**
  - a) mantener `List<String>`;
  - b) `List<Long> categoriaIds`;
  - c) una copia local del catálogo, alimentada por eventos.
- **Decisión (nueva): a.** La lista guarda los nombres de las categorías del catálogo de ms-menu, copiados por valor al registrar el proveedor, sin validarlos contra ms-menu.
- **Por qué:**
  - En microservicios, ninguna de las dos opciones se garantiza con una clave foránea.
  - b) obliga a buscar los nombres y cambia la 4c, el código y las pruebas.
  - c) exige un tópico nuevo.
- **Qué se pierde:** un cambio de nombre en ms-menu no se propaga. El dato es informativo.
- **Efectos:** CU-09 en los casos de uso. La pregunta abierta de la sección de decisiones se cierra.

### PD-03 · BAM: periodos fijos frente al filtro libre por fechas

- **Evidencia:**
  - HU-18 pide «último mes», «último año» y «filtrar por rango de fechas y por sucursal»;
  - en la 4e, `kpis(desde, hasta, sucursalId)` y `n_kpis`;
  - `TableroService.kpis()` pasa los mismos hechos a las tres estrategias;
  - PRECIO_PROVEEDOR es una diferencia porcentual, y la demo llama a `kpis(null, null, null)`;
  - DEC-40 advierte que ganaría COT-020, que es SOSPECHOSA.
- **Opciones:**
  - a) periodos por defecto, que un rango elegido reemplaza;
  - b) periodos fijos, con el rango solo para la demanda;
  - c) rango libre siempre.
- **Decisión (nueva): a.**
  - Sin rango, la demanda usa los últimos 30 días y el proveedor los últimos 365.
  - Con rango, los tres indicadores usan ese rango, y la pantalla muestra el periodo aplicado.
  - La sucursal filtra solo la demanda: las compras son centrales y `cotizacion.validada` no trae sucursal.
  - «Mejores precios» es la menor diferencia promedio contra el precio estándar, contando solo las cotizaciones OPCIONADAS.
  - Sin hechos, muestra «No hay información suficiente para el periodo seleccionado».
- **Por qué:**
  - Cumple los dos criterios sin cambiar firmas.
  - Resuelve la duda de DEC-40.
  - La salida de la demo no cambia.
- **Qué se pierde:** filtrar el indicador de proveedor por sucursal.
- **Efectos:**
  - **`.drawio`:** `n_kpis` en la 4e.
  - **Documentos:** CU-14.
  - **Reporte:** R-31 pasa a «Cambiar».
  - **Fuera de alcance:** `TableroService.kpis()` con los periodos por defecto, y `NegocioEventsListener` filtrando las OPCIONADAS (requiere PD-05).

### PD-04 · Promociones: fuente de las tendencias y periodo de análisis

- **Evidencia:**
  - HU-19 nombra «redes sociales» y «productos más solicitados», pero no fija la red, la ventana ni la cantidad;
  - en la 4e están `TendenciasPort.productosEnTendencia()`, `RedesSocialesAdapter`, `TendenciasJob` («semanal») y `notificadaEl`;
  - CU-06 lo dice «por definir».
- **Opciones:**
  - a) las dos fuentes, con una ventana semanal;
  - b) solo las redes;
  - c) solo la demanda interna;
  - d) una ventana diaria.
- **Decisión (nueva): a.**
  - El job semanal analiza los 7 días anteriores y sugiere los productos en tendencia de `TendenciasPort` y los 3 más pedidos (hechos DEMANDA_PRODUCTO).
  - El motivo dice «Tendencia en redes» o «Entre los 3 más pedidos de la semana».
  - La vigencia es de 7 días, y avisa a los jefes de la libreta (DEC-13).
  - **Supuesto S11:** la red concreta y los valores 7 y 3, que ninguna fuente respalda.
- **Por qué:** usa solo piezas ya dibujadas, y la ventana coincide con la del job.
- **Qué se pierde:** reaccionar el mismo día.
- **Efectos:**
  - **`.drawio`:** `n_promociones` en la 4e.
  - **Documentos:** CU-06.
  - **Reporte:** el supuesto S11 en §17.
  - **Fuera de alcance:** `detectarTendencias()`.
- **Pendiente:** si el enunciado nombra una red, sustituye el supuesto.

### PD-05 · T-06: eventos con tipos del dominio de otro servicio

- **Evidencia:**
  - `CotizacionValidadaEvent.estado: EstadoCotizacion` y `TendenciaDetectadaEvent.sugerencias: List<SugerenciaPromocion>`;
  - DEC-29 prohíbe importar el dominio de otro servicio;
  - el evento validado no trae la diferencia ni el precio estándar (N1).
- **Opciones:**
  - A) seguir sin consumidores;
  - B) tipos simples en el contrato;
  - C) mover los eventos a `compartido.eventos`.
- **Decisión (nueva, cierra T-06): B ampliada, con aplicación diferida.**
  - `CotizacionValidadaEvent` pasa a `estado: String`, con un campo nuevo, `diferenciaPorcentual: BigDecimal`.
  - `TendenciaDetectadaEvent` pasa a `sugerencias: List<String>`, con líneas «producto · motivo · vigencia».
  - Mientras no se aplique, A sigue siendo el límite declarado de la demo.
- **Por qué:**
  - Un contrato de tópico no debe exponer tipos del dominio.
  - Sin la diferencia, ms-analitica no puede calcular el indicador de PD-03.
  - C es un cambio grande.
- **Qué se pierde:** el tipado fuerte del estado.
- **Efectos (fuera de alcance):** la 4c, la 4e y el código, juntos. Cabe en unas 10 líneas, pero solo se aplica si Mauricio lo autoriza.

### PD-06 · Administración: roles, permisos y alcance

- **Evidencia:**
  - las 6 subclases de `Usuario` y el menú por rol (4a);
  - `RegistrarUsuarioUseCase.registrar(nombre, correo, rol, sucursalId, clave)`;
  - reporte §16: «verificación del rol en cada endpoint»;
  - `parametrizar(clave, valor)`;
  - CU-15 lo deja abierto.
- **Opciones:**
  - a) roles fijos, permisos fijos y un administrador global;
  - b) gestión de roles en ms-identidad;
  - c) administradores por sucursal.
- **Decisión (nueva): a.**
  - Los 6 roles son un conjunto cerrado. Un rol nuevo es un cambio de código.
  - Los permisos son una matriz fija de rol a rutas: el gateway valida el JWT, cada servicio verifica el rol y el menú sale del rol.
  - El Administrador tiene alcance global: registra usuarios con un rol y una sucursal (CU-19); mantiene parámetros, tema, logos y destinatarios (CU-15); y consulta la auditoría y los logs.
  - **Supuesto S10:** las listas maestras (categorías, productos, unidades, marcas y mesas) y el primer administrador se cargan con la migración inicial del servicio dueño de cada una.
  - Fuera de alcance: cambiar el rol o desactivar usuarios.
- **Por qué:** es lo que el diseño ya construye, sin firmas nuevas.
- **Qué se pierde:** dar de baja a un usuario sin un despliegue. La evolución sería `PATCH /api/usuarios/{id}`.
- **Efectos:**
  - **Documentos:** CU-15.
  - **Reporte:** R-57, y el supuesto S10 en §17.
- **Pendiente:** si el enunciado exige que el administrador mantenga las listas, se reconsidera S10.

### PD-07 · Auditoría: filtros por usuario y acción

- **Evidencia:**
  - `AuditoriaRepositoryPort.entre(desde, hasta)` y `auditoria(desde, hasta)`, que ya tiene lógica;
  - la demo audita a «sistema» (DEC-42);
  - CU-16 lo deja «por definir».
- **Opciones:**
  - a) solo fechas;
  - b) fechas, más usuario y acción opcionales;
  - c) filtrar en la SPA.
- **Decisión (nueva): a, en esta versión.**
  - El rango es obligatorio, y «desde» no puede ser posterior a «hasta».
  - b) queda como la primera evolución.
- **Por qué:**
  - Ninguna fuente lo exige.
  - b) cambia cinco firmas de la 4e y un método que ya tiene lógica.
  - Con «sistema» como usuario, el filtro no demostraría nada.
  - c) no escala.
- **Qué se pierde:** encontrar rápido lo que hizo un usuario.
- **Efectos:** CU-16, y los criterios en HU-21 (PD-09).

### PD-08 · Autenticación de los consumidores externos del SOAP

- **Evidencia:**
  - página 2, `e21`; 3a, `c3a_e15`; página 1, `e233`;
  - el SOAP se publica bajo `/api/integracion/**`;
  - reporte §3: el gateway valida el JWT;
  - CU-18 lo deja «por definir».
- **Opciones:**
  - a) OAuth 2.0 client credentials (JWT);
  - b) WS-Security UsernameToken;
  - c) mTLS por socio;
  - d) API key.
- **Decisión (nueva): a.**
  - Cada sistema externo se registra como cliente en ms-identidad y obtiene el token en `POST /api/auth/token`.
  - El gateway lo valida como cualquier JWT y solo permite `/api/integracion/**` a ese tipo de cliente.
  - El `client_id` queda como usuario en la auditoría.
  - La conexión va por TLS 1.3.
- **Por qué:** es un solo mecanismo, ya validado en el gateway, que además da una identidad para auditar.
  - b) sería un segundo mecanismo.
  - c) obliga a gestionar certificados por socio.
  - d) no expira.
- **Qué se pierde:** la compatibilidad directa con clientes que solo saben WS-Security; para ellos se puede agregar mTLS en el borde.
- **Efectos:**
  - **`.drawio`:** `e21` (página 2), `c3a_e15` (3a) y `e233` (página 1).
  - **Documentos:** CU-18.
  - **Reporte:** el supuesto S12 y una frase en §3.
  - **Fuera de alcance:** el registro de clientes en ms-identidad, que no existe en la 4a.

### PD-09 · HU-20 a HU-22

- **Evidencia:**
  - casos de uso, sección de propuestas;
  - el criterio 1 de HU-20 promete mantener categorías, productos, unidades, marcas y mesas, y ningún servicio lo expone (N3);
  - HU-21 y HU-22 coinciden con `auditoria(desde, hasta)` y `logs(servicio)`.
- **Opciones:**
  - a) aprobarlas tal cual;
  - b) aprobarlas como propuesta, con ajustes;
  - c) descartarlas.
- **Decisión (nueva): b.** Se marcan «Propuesta de diseño aprobada por el arquitecto-revisor, sin estimar; no forma parte del Story Mapping original».
  - **HU-20 «Parametrizar el sistema»:**
    1. Consultar y cambiar los parámetros, el tema y los logos; la SPA los toma al iniciar, sin redespliegue.
    2. Registrar y consultar los correos de los jefes de cocina por sucursal.
    3. Validaciones y mensajes de PD-10.
    4. Cada cambio queda en la auditoría.
    5. Solo para el Administrador.
    6. Un error al guardar queda en el log.
    7. Las listas maestras quedan fuera (S10).
  - **HU-21 «Consultar la auditoría»:**
    1. Acción, usuario, fecha y hora.
    2. Rango obligatorio, con «desde» ≤ «hasta».
    3. Solo lectura y solo para el administrador.
    4. Mensajes de PD-10.
  - **HU-22 «Consultar logs de errores»:**
    1. Servicio, mensaje, fecha y hora.
    2. El servicio es opcional; sin él, salen todos.
    3. Solo lectura y solo para el administrador.
    4. Mensajes de PD-10.
- **Por qué:** cubren el rol que el Story Mapping no tiene, sin prometer lo que la arquitectura no hace.
- **Qué se pierde:** el mantenimiento de las listas en pantalla.
- **Estimación:** la hace el equipo, con planning poker y las mismas cartas (25/50/75/125 h = 1/2/3/5 PHU).
  - Hasta entonces, van al Excel como F20–F22 en un bloque «Propuestas (sin sprint)».
  - Solo se ubican dentro de la holgura: 1, 1, 3, 1 y 4 PHU por sprint, 10 en total.
  - Si no caben, decide Mauricio, porque agregar un sprint cambia las 10 semanas que él confirmó.
- **Pendiente:** la estimación del equipo.

### PD-10 · Mensajes de interfaz por definir

**Criterio:**
- El mismo estilo que las tarjetas: una frase completa, sin códigos técnicos, que diga qué pasó y qué hacer.
- El mismo texto para el mismo caso en todas las pantallas.
- Los mensajes literales de las HU son el contrato de la interfaz. Los de excepción del dominio son internos, y el adaptador REST los traduce (efecto futuro; N6).

| Dónde | Caso | Texto propuesto |
|---|---|---|
| CU-01 / V01 | Credenciales inválidas | «Correo o contraseña incorrectos.» (no dice cuál falló) |
| CU-01 | Rol sin acceso | «No tiene permiso para acceder a esta opción.» |
| CU-08 / HU-12 | El Ministerio no responde | «No se pudo consultar el precio del Ministerio de Comercio. La cotización {número} quedó pendiente de precio; puede reenviarla desde Cotizaciones.» |
| CU-08 / HU-12 | Falla el correo | «No se pudo enviar el correo al proveedor. La cotización {número} está guardada; puede reintentar el envío.» |
| CU-10 / HU-11 | Cotización ya validada | «La cotización ya fue validada; sus valores no se pueden modificar.» |
| CU-10 / HU-11 | Valor no válido | «El valor unitario debe ser un número mayor a cero.» |
| CU-19 | Contraseña | «La contraseña debe tener al menos 8 caracteres, con una mayúscula, una minúscula y un número.» |
| CU-19 | Correo repetido | «Ya existe un usuario registrado con ese correo.» |
| CU-19, HU-20 | Correo mal formado | «El formato del correo electrónico no es válido.» (el de HU-15) |
| HU-20 | Clave o valor vacío | «La clave del parámetro es obligatoria.» / «El valor del parámetro {clave} es obligatorio.» |
| HU-20 | Guardado | «Parámetro actualizado.» |
| HU-07, HU-18, HU-19, HU-21 | Rango invertido | «La fecha inicial no puede ser posterior a la fecha final.» |
| HU-21 | Sin resultados | «No hay movimientos de auditoría en el periodo seleccionado.» |
| HU-22 | Sin resultados | «No hay errores registrados para el servicio seleccionado.» |
| HU-19 | Sin sugerencias / ya resuelta | «No hay promociones sugeridas para el periodo seleccionado.» / «Esta sugerencia ya fue aceptada o descartada.» |
| CU-18 | Producto sin precio (SOAP Fault) | «No hay precio estándar disponible para el producto {id}.» Un fallo de autenticación responde 401 en el gateway |

**Efectos:**
- **Documentos:** los casos de uso, las HU-20 a HU-22 y la tarjeta HU-07 del nuevo Story Mapping.
- **Código:** nada ahora, porque las pruebas comprueban los textos actuales.

### Preguntas factuales: no se deciden

- **A1, el script del reporte:**
  - **Evidencia:**
    - según `pdfinfo`, el PDF lo creó ReportLab el 2026-10-29 a las 12:13:59 (-05), con las fuentes Onest, Bricolage Grotesque, JetBrains Mono y DejaVu Sans;
    - §18 dice que la aplicación de Claude generó el `.drawio`, el esqueleto y el reporte en la misma sesión;
    - el esqueleto se subió 8 minutos después del PDF (`ad71e69`).
  - **Lo que se infiere, sin atribuirlo a nadie:** el script probablemente está en el historial de claude.ai de quien escribió esos prompts.
  - **Recomendación si no aparece:**
    - Mauricio cambia DEC-03;
    - se escribe un script nuevo (por ejemplo `documentos/reporte/generar_reporte.py`) con las mismas fuentes, el texto de `pdftotext -layout`, R-01 a R-62 y las figuras de `exportes/`;
    - el resultado se compara página por página y sale a `…_v2.pdf`, conservando el original.
- **A3, la cuenta `mari00aa120-eng`:**
  - tiene 3 commits del 29-oct, entre las 12:00 y las 12:22 (`379fb64`, `b56abab` y `ad71e69`), y el remoto es `iCastro117/KonradGourmet`;
  - el correo de autor del commit no coincide con ningún integrante, pero eso no prueba nada;
  - lo confirma el equipo.
- **Enunciado original:** no está en ninguna carpeta (`TALLER1_GRUPO_1_20262.pdf` es otro taller). El reporte lo cita como [1] López (2026), «Proyecto #1».
  - **Respaldado por las tarjetas HU:**
    - la auditoría con acción, usuario, fecha y hora;
    - el correo certificado con estampado cronológico;
    - el umbral del 25 % con verificación diaria;
    - las reglas +25/−50;
    - los periodos de los indicadores, la respuesta en menos de 5 s, la interfaz responsive y la actualización diaria;
    - una notificación de promociones por día;
    - el log de errores.
  - **Solo en el reporte, sin poder verificar su redacción literal:**
    - 200.000 usuarios y 1.000 TPS;
    - 99,7 % y el centro de datos alterno;
    - el crecimiento del 200 % y el backup diario;
    - la regla de contraseña y el cifrado, y HTTPS;
    - roles y permisos, y la imagen parametrizable;
    - el cambio fácil de motor de base de datos y la tecnología libre;
    - el SOAP para los servicios expuestos y la computación distribuida;
    - «diecinueve requerimientos no funcionales», y los supuestos S2 y S4.

---

## Punto 5 · Supuestos

| DEC | Resolución | Evidencia y ajuste |
|---|---|---|
| DEC-08 | Confirmada con ajuste | D2 (11 rutas) queda reemplazada por DEC-12 (13 prefijos) y no se restaura. Se confirman D1 (`pdfUrl`, más `adjuntoUrl`), D3 (CON_ORDEN, `existePorCotizacion` en `OrdenCompraService:36`), D4, D5 (pendiente fuera de la demo), D6 (aviso por correo), D7 y D8, estas dos por DEC-07 |
| DEC-39 | Confirmada | 94 cuerpos en 45 archivos. Mauricio fija el 2026-11-08 que el alcance es solo la demo; ninguna PD abre cuerpos pendientes |
| DEC-40 | Confirmada | La res arranca en 12,5 de 50 kg (25,00 %, sin alerta) y queda en 11,5 kg (23 %). El histórico manual es un límite de la demo, y PD-03 elimina el riesgo de COT-020. No se tocan precios ni los $82.000 |
| DEC-41 | Confirmada con ajuste | Los constructores de copia se confirman. Las listas modificables son una simplificación declarada: `GenerarCotizacionFacade:66-67` hace `items().clear()/addAll()` y `SolicitudService:60` hace `cotizacionesAplicadas().add()`. En el sistema real irían `List.copyOf` y otro constructor de copia. Fuera de alcance |
| DEC-44 | Confirmada con ajuste del texto | Hoy dice «171 verificaciones en 3 clases»; debe decir 176 verificaciones en 6 archivos (3 de prueba más `Prueba`, `Datos` y `EjecutarPruebas`) |
| DEC-45 | Confirmada | 211 = 191 + 20 (`DemoArquitectura` y 19 de apoyo) |
| DEC-47 | Confirmada (orden de filas) | En SPRINT, F15 → F17 → F16, con la etiqueta MVP4 en F17. Las dependencias F13 → F17 → F14 quedan dentro del sprint 3 |
| T-06 (DEC-42) | Resuelta por PD-05 | Aplicación diferida |
| R-61 | Pasa a «Cambiar» | S9 (el mesero sale del JWT) defiende DEC-51, que está confirmada |
| R-58 → PD-11 | Redacción corregida, con datos por confirmar | Ver N4. Declarar Claude Code (con el agente revisor) y la continuación con Codex en la sesión 6; las decisiones delegadas al agente; modelos por confirmar |

## Hallazgos colaterales

| # | Severidad | Dónde | Qué está mal | Corrección mínima |
|---|---|---|---|---|
| N1 | Media | 4c, `CotizacionValidadaEvent` | No trae la diferencia ni el precio estándar: ms-analitica no puede calcular «mejores precios» | PD-05 |
| N2 | Media | 4b, ms-menu | No hay un endpoint que liste categorías ni marcas para V06, V08 y el proveedor (HU-10 pide una lista desplegable) | Efecto futuro: `categorias()` en la 4b; las marcas, por S10 |
| N3 | Media | Casos de uso, HU-20 | Promete mantener listas que ningún servicio expone | PD-09 |
| N4 | Alta | R-58 | Dice «las decisiones de diseño las tomó el equipo», lo que es falso para las decisiones delegadas | PD-11 |
| N5 | Media | `NotificacionService.registrarDestinatario()` | No publica `auditoria.evento`, aunque CU-15 lo afirma | Declararlo como límite en CU-15 y en el README; el código, fuera de alcance |
| N6 | Baja | Código frente a las HU | Cinco textos distintos (NIT, correo del proveedor, cantidad, mesa y «no tiene valores registrados») | Política de PD-10; no cambiar ahora |
| N7 | Baja | `ESTADO_PROYECTO_Y_C4.md` §1 | Atribuye las promociones al director comercial; son del jefe de cocina | Corregir el texto |

**Estado de E1–E3 y D1–D12:**
- **Resueltos:** E1, E2 y E3, y D1 a D8, D10 y D12.
- **Pendientes:** D9 (la tarjeta HU-07, S-01) y D11 (A1).

**Lo que está bien:**
- la demo es reproducible;
- la alarma sale una sola vez, al cruzar el umbral;
- el claim check del PDF con `adjuntoUrl`;
- las 13 rutas y los 10 tópicos son coherentes en el mapa, la 3a y la 6;
- los 29 comentarios de ventana.

## Resumen

| Id | Tema | Decisión | Tipo |
|---|---|---|---|
| PD-01 | A2 WebSocket | Redis pub/sub; V05 relee las alertas | Nueva |
| DEC-20 | Eureka y SPA | ×2 en zonas distintas | Confirmación |
| DEC-48 | Páginas 3a, 3b y 6 | A, más la tabla de niveles C4 | Confirmación con ajuste |
| PD-02 | Categorías del proveedor | `List<String>` con nombres | Nueva |
| PD-03 | BAM | Periodos por defecto; el rango manda | Nueva |
| PD-04 | Promociones | 7 días, redes y los 3 más pedidos (S11) | Nueva |
| PD-05 | T-06 | Tipos simples y diferencia; aplicación diferida | Nueva |
| PD-06 | Administración | Roles y permisos fijos; administrador global (S10) | Nueva |
| PD-07 | Auditoría | Solo fechas | Nueva |
| PD-08 | SOAP externo | OAuth 2.0 client credentials (S12) | Nueva |
| PD-09 | HU-20 a HU-22 | Propuesta aprobada, ajustada y sin estimar | Nueva |
| PD-10 | Mensajes | 16 textos | Nueva |
| PD-11 | R-58 | Declaración fiel | Ajuste |
| DEC-08/39/40/41/44/45/47 | Supuestos | Ver el punto 5 | Confirmación (08, 41 y 44 con ajuste) |

**Preguntas factuales abiertas:**
- A1: el script del reporte.
- A3: de quién es la cuenta `mari00aa120-eng`.
- El enunciado «Proyecto #1».
- Los modelos y versiones de Claude Code y Codex que se usaron (para R-58).
- La estimación de HU-20 a HU-22.
