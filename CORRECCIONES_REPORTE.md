# Correcciones al reporte PDF · Konrad Gourmet

`Reporte_Arquitectura_Konrad_Gourmet.pdf` (38 págs., generado con ReportLab el 29-oct) **no se modifica** mientras no aparezca el script que lo generó (DEC-03; pregunta abierta A1). Este archivo reúne todo lo que hay que cambiar cuando aparezca el script, o si el equipo decide rehacer el reporte a mano.

Las correcciones salen de la revisión del 2026-11-07 (H1–H26 y D1–D12, en `REVISION_2026-11-07.md`), de las decisiones de `DECISIONES.md` y de los inventarios y validaciones de las sesiones 2 a 4. Cada cambio cita la página del PDF, el texto actual (sacado con `pdftotext -layout`, con los saltos de línea del PDF unidos) y el texto nuevo.

**Cómo leerlo**
- Cada corrección tiene un id **R-xx** para poder citarla.
- **Cambiar:** el texto actual es falso o quedó desactualizado.
- **Ya es cierto:** el texto se puede dejar como está, porque las sesiones 3 y 4 lo volvieron verdad. Se anota para que nadie lo «corrija» por error.
- **Opcional:** mejora el reporte, pero no es necesaria.
- **Por confirmar:** el texto depende de algo que el equipo no ha decidido.
- Los textos nuevos van en *cursiva* dentro de comillas «…». Lo que va entre [corchetes] es una instrucción, no texto del reporte.

**Cifras que el reporte debe usar** (reverificadas al cierre de la sesión 6 el 2026-11-08: código `22717f7`, `.drawio` `4e4559e`; ver `VALIDACION_SESION6.md`):

| Dato | Valor | Fuente |
|---|---|---|
| Páginas del `.drawio` | 11: 1 Mapa integrado, 2 Contexto, 3a Contenedores, 3b Componentes de ms-cotizaciones, 4a–4e Código, 5 Despliegue, 6 Dinámico | `.drawio` |
| Clases del modelo (4a–4e) | 191 | DEC-45, P-12 |
| Paquete `demo` | `DemoArquitectura` más 19 clases de apoyo (adaptadores en memoria y `Ensamble`): 20 archivos | DEC-45 |
| Archivos `.java` en `src/` | 211 (191 + 20) | `find src -name "*.java"` |
| Pruebas | 176 verificaciones en `pruebas/` (6 archivos, fuera de `src/`), sin JUnit; eran 171 hasta que la sesión 6 agregó las del mesero (DEC-51) | DEC-44, DEC-51 |
| Microservicios, tópicos y ventanas | 11, 10 y 23 (sin cambio) | Reglas del plan |
| Prefijos del gateway | 13 | DEC-12 |
| Servicios que publican `auditoria.evento` | 10 (todos menos ms-administracion, que escribe directo en su bitácora) | H10 |
| Comentarios «Atiende la ventana Vxx» | 29 líneas en el código, que cubren las 23 ventanas | `grep` |

---

## 1. Cambios de conjunto

### R-01 · Volver a capturar todas las figuras · Cambiar
**Dónde:** figuras 1 a 11 (p. 14–19, 23–25, 28 y 29) y figuras A1 a A5 del anexo (p. 34–38).
**Por qué:** las figuras son capturas del `.drawio` del 29-oct. Desde entonces cambiaron todas las páginas que muestran: el mapa (p. 1), el contexto (p. 2), las cinco de código (4a–4e) y el despliegue (p. 5).
**Qué hacer:** recortar las mismas zonas de las PNG actuales. Las 11 páginas del `.drawio` de hoy (`ae8dc83`) están exportadas en `FinalProyecto/exportes/sesion5/` (`1.png` … `6.png`). Si el `.drawio` vuelve a cambiar, hay que exportarlas otra vez a **escala 1**: en este equipo, a escala mayor la PNG sale en blanco.

### R-02 · Agregar las páginas 3a, 3b y 6 · Cambiar
**Por qué:** el reporte (p. 4) dice que el modelo C4 es obligatorio en sus cuatro niveles y que «los diagramas dinámico y de despliegue son complementarios», pero no muestra el nivel 2, el nivel 3 ni el dinámico (REVISION, estado de los entregables).
**Dónde van y con qué texto** (las figuras se renumeran: ver R-03):

1. **Página 6, en §10**, después de la tabla «Recorrido completo de una compra» (p. 21):

   > *«Figura 7 · Diagrama dinámico del recorrido de compra. Página 6 del .drawio. Los 14 pasos de la tabla anterior en forma de secuencia, con cada mensaje numerado (4.1, 4.2…) y su protocolo. En cada evento se dibujan todos sus consumidores. El ejemplo es el de los mockups: SOL-031 → COT-018 → OC-0012, con los valores de V19.»*

2. **Página 3a, en §14**, como subtítulo nuevo después de «Nivel 1 del C4» (p. 27):

   > *«Nivel 2 del C4. El diagrama de contenedores abre la caja del sistema: la SPA, el servidor web (Nginx), el API Gateway, el Service Registry, los once microservicios con su base de datos, el broker de eventos, la caché de precios en Redis, el almacén de objetos y los logs en ELK. Cada flecha lleva su propósito y su protocolo. El gateway enruta 13 prefijos a los once servicios.»*
   >
   > *«Figura 12 · Nivel 2 del C4, diagrama de contenedores. Página 3a del .drawio.»*

3. **Página 3b, en §14**, a continuación:

   > *«Nivel 3 del C4. El diagrama de componentes hace zoom sobre ms-cotizaciones, el servicio con más lógica: los controladores de cotizaciones y de proveedores, el núcleo hexagonal (casos de uso, fachada, cadena de reglas y modelo) y los adaptadores hacia su base PostgreSQL, el almacén de objetos, ms-integracion y Kafka.»*
   >
   > *«Figura 13 · Nivel 3 del C4, componentes de ms-cotizaciones. Página 3b del .drawio.»*

### R-03 · Renumerar las figuras · Cambiar

| Actual | Nueva | Página del `.drawio` |
|---|---|---|
| Figuras 1 a 6 | 1 a 6 | 1 |
| — | **7** (nueva) | 6 |
| Figuras 7, 8 y 9 | 8, 9 y 10 | fragmentos de 4a, 4c y 4d |
| Figura 10 (contexto) | 11 | 2 |
| — | **12** (nueva) | 3a |
| — | **13** (nueva) | 3b |
| Figura 11 (despliegue) | 14 | 5 |

Las figuras del anexo (A1–A5) no cambian de número. Hay que revisar cualquier frase que cite un número de figura; «las figuras 2 a 6 muestran cada zona de cerca» (p. 14) sigue siendo correcta.

### R-04 · «Ocho páginas» → 11 · Cambiar
- **p. 1 (portada):** «Acompaña a Arquitectura_Konrad_Gourmet.drawio (8 páginas) y al esqueleto Java del paquete co.edu.konradlorenz.» → *«Acompaña a Arquitectura_Konrad_Gourmet.drawio (11 páginas) y al esqueleto Java del paquete co.edu.konradlorenz.»*
- **p. 2:** «el archivo Arquitectura_Konrad_Gourmet.drawio, con ocho páginas (mapa integrado, contexto C4, cinco páginas de nivel de código y despliegue), y el esqueleto Java que implementa las mismas clases.» → *«el archivo Arquitectura_Konrad_Gourmet.drawio, con once páginas (mapa integrado, los cuatro niveles del C4 —contexto, contenedores, componentes de ms-cotizaciones y cinco páginas de nivel de código—, despliegue y el diagrama dinámico del recorrido de compra), y el esqueleto Java que declara las mismas clases.»*

### R-05 · Fecha e índice · Cambiar
- **p. 1:** «Octubre de 2026» → la fecha de la nueva versión (por ejemplo, *«Noviembre de 2026»*).
- **p. 2 (contenido):** regenerar el índice. R-02 agrega texto en §10 y §14, así que los números de página cambian desde la p. 21.

---

## 2. Cambios página por página

### p. 3 · §1 Resumen de la decisión

**R-06 · Ya es cierto (H1, D12).** «cada uno de los 23 mockups está asignado a un controlador y un método concretos, y una demostración ejecutable reproduce los valores de la ventana 19 (cotización COT-018, diferencia de +4,81 % frente al precio del Ministerio, estado OPCIONADA).» Desde las sesiones 3 y 4, los 23 métodos existen con su comentario y la demo imprime «Valor estándar total $520.000 · valor cotizado total $545.000 · diferencia +4,81 % → OPCIONADA». **Opcional:** agregar al final *«…, y sigue el recorrido hasta la orden de compra OC-0012»*, que la demo también muestra.

**R-07 · Cambiar (K2, P-10).** «y las secciones 12 y 13 muestran cómo el diagrama de clases y el código implementan los patrones de diseño.» → *«y las secciones 12 y 13 muestran dónde están los patrones de diseño en el diagrama de clases y en el código, y cómo los ejecuta la demostración.»*

### p. 5 · §3 Requerimientos no funcionales y decisiones

**R-08 · Cambiar (H21).** Fila «200.000 usuarios concurrentes y 1.000 TPS de compras», columna Decisión: «Servicios sin estado detrás de un balanceador; solo los que reciben más carga (pedidos, cotizaciones, identidad) escalan en horizontal» → *«Servicios sin estado detrás de un balanceador; todos escalan en horizontal entre un mínimo y un máximo de réplicas, y los que reciben más carga (pedidos, identidad, cotizaciones, solicitudes e inventario) tienen los máximos más altos»*. Así lo dice el despliegue (p. 5 del `.drawio`, `v15`): ms-pedidos 4 → 20, ms-identidad 3 → 8, ms-inventario, ms-cotizaciones y ms-solicitudes 2 → 6, y los demás 2 → 4.

**R-09 · Cambiar (H15, DEC-10).** Fila «Contraseña de 8 o más caracteres…», columna Decisión: «Regla en el dominio de identidad; hash BCrypt detrás del puerto CifradorPort» → *«Regla en el dominio de identidad (PoliticaContrasena); el alta de usuarios la hace el administrador (POST /api/usuarios), que valida la regla y guarda el hash BCrypt con CifradorPort.cifrar()»*. Columna «Dónde se ve»: *«Página 4a; adaptador BCrypt de ms-identidad»*.

**R-10 · Ya es cierto (H25).** Fila «Correo certificado…»: «Decoradores sobre el puerto Correo». Desde la fase 2a, `Correo` está en `puertos.salida` (4d y código), así que la frase quedó correcta.

**R-11 · Cambiar (H13).** Fila «Imagen corporativa parametrizable»: «Tema y logos en ParametrosSistema (ms-administracion); la SPA los carga al iniciar, sin redesplegar» → *«Tema y logos en ParametrosSistema (ms-administracion); la SPA los carga al iniciar con GET /api/admin/parametros/tema, sin redesplegar»*.

**R-12 · Cambiar (H16).** Fila «Backup diario»: «Copia completa nocturna más registros WAL continuos, con retención de 30 días» → *«Bases: copia completa nocturna más registros WAL continuos. Almacén de objetos: copia diaria y réplica en la región alterna. Retención de 30 días»*.

**R-13 · Cambiar (H10, DEC-42).** Fila «Auditoría de cada CRUD con acción, usuario, fecha y hora»: «Cada servicio publica auditoria.evento; ms-administracion lo consume y lo guarda en su bitácora» → *«Cada uno de los otros diez servicios publica auditoria.evento después de cada escritura; ms-administracion lo consume y lo guarda en su bitácora, y sus propias escrituras las registra directo. En el sistema real, el usuario sale del JWT; la demostración del esqueleto registra «sistema» (o el correo de la entidad), como se explica en la sección 13»*.

**R-14 · Opcional (H14).** Fila «Registro de cada error en un log»: «Logs centralizados (ELK) con trazas OpenTelemetry; consulta desde ms-administracion» → *«…; ms-administracion los consulta en Elasticsearch por el puerto LogsPort»*.

### p. 6 · §4 Estilos evaluados
Sin cambios.

### p. 7 · §5 Microservicios

**R-15 · Cambiar (H10).** Lineamientos: «y cada servicio publica un evento de auditoría en cada CRUD.» → *«y cada servicio publica un evento de auditoría en cada CRUD, salvo ms-administracion, que es quien los guarda y registra los suyos directo en la bitácora.»*

**R-16 · Cambiar (H21).** Aplicabilidad: «Con microservicios, ms-pedidos escala de 4 a 20 réplicas y ms-menu se queda en 2.» → *«Con microservicios, ms-pedidos escala de 4 a 20 réplicas, mientras que ms-menu se mueve entre 2 y 4.»*

**R-17 · Cambiar (DEC-12, DEC-07, DEC-10, H4).** Tabla de servicios, p. 7–8. Agregar antes de la tabla: *«El gateway enruta 13 prefijos a los once servicios: ms-identidad y ms-cotizaciones tienen dos cada uno.»* Las filas que cambian (las demás quedan igual):

| Servicio | Responsabilidad | Ventanas | Ruta en el gateway |
|---|---|---|---|
| ms-identidad | OAuth 2.0 / OIDC, usuarios, roles, permisos y menú por rol; *alta de usuarios por el administrador* | V01 | /api/auth/\*\* · */api/usuarios/\*\** |
| ms-cotizaciones | Solicitud de cotización, PDF, registro de valores y validación; *proveedores (HU-15)* | V13–V20 | /api/cotizaciones/\*\* · */api/proveedores/\*\** |
| ms-ordenes | Órdenes de compra desde cotizaciones OPCIONADAS, *y su PDF* | V21–V23 | /api/ordenes-compra/\*\* |
| ms-analitica | Tablero BAM (KPI), detección de tendencias *y promociones sugeridas (HU-19)* | tablero BAM (sin mockup) | /api/bam/\*\* |

**R-18 · Ya es cierto (D2).** Fila ms-integracion, p. 8: «/api/integracion/\*\*». El endpoint SOAP se publica bajo ese prefijo (DEC-32).

### p. 8 · §5, continuación

**R-19 · Opcional (D3, H18, H12).** Desventajas: «estados explícitos (PENDIENTE, EN TRÁMITE, ENVIADA, OPCIONADA)» → *«estados explícitos (PENDIENTE, EN TRÁMITE y COMPLETADA en la solicitud; PENDIENTE_PRECIO, ENVIADA, OPCIONADA y CON ORDEN en la cotización)»*.

**R-20 · Cambiar (H4, H5, D1).** Ejemplo en las ventanas: «ms-ordenes, que pide por REST los datos de la cotización OPCIONADA a ms-cotizaciones (CotizacionRestClient, síncrono porque el director espera la respuesta), guarda la orden en su propia base y responde 201 con OC-0012» → *«ms-ordenes, que pide por REST los datos de la cotización OPCIONADA a ms-cotizaciones (CotizacionRestClient con GET /api/cotizaciones/{id}, síncrono porque el director espera la respuesta), guarda la orden en su propia base, genera su PDF, lo deja en el almacén de objetos y responde 201 con OC-0012»*.

### p. 9 · §6 Arquitectura orientada a eventos

**R-21 · Cambiar (DEC-42, H11).** Lineamientos: «Las políticas son eventos inmutables con identificador, tipo, fecha y usuario;» se deja, y se agrega al final del párrafo: *«En el sistema real, el usuario sale del JWT de quien hizo la acción. La demostración del esqueleto simplifica: los casos de uso no reciben al usuario, así que los eventos llevan «sistema» (o el correo de la entidad).»*

**R-22 · Cambiar (DEC-43, DEC-46).** Aplicabilidad: «Cuando una existencia llega al 25 % hay que mostrar una alarma y enviar un correo» → *«Cuando una existencia baja del 25 % de su stock de referencia hay que mostrar una alarma y enviar un correo, una sola vez: el aviso sale la noche en que el insumo cruza el umbral, no cada noche mientras siga bajo»*.

**R-23 · Cambiar (DEC-42).** Desventajas: «La depuración es más difícil, así que cada evento lleva un identificador de correlación y se traza con OpenTelemetry.» → *«La depuración es más difícil, así que cada evento lleva un identificador de correlación y se traza con OpenTelemetry. En el sistema real, ese identificador pasa de un evento al siguiente, de modo que una traza reúne todo el recorrido de una compra. La demostración del esqueleto no lo propaga: cada evento crea su propia correlación.»*

**R-24 · Cambiar (D1, DEC-05).** Ejemplo: «ms-cotizaciones guarda la cotización en estado ENVIADA, genera el PDF, publica cotizacion.enviada y responde 201» → *«ms-cotizaciones guarda la cotización en estado ENVIADA, genera el PDF, lo deja en el almacén de objetos, publica cotizacion.enviada con la referencia del PDF y responde 201»*. Y «ms-solicitudes lo consume y, si todos los productos de la solicitud ya tienen cotización, la pasa a EN TRÁMITE» → *«ms-solicitudes lo consume y pasa a EN TRÁMITE cada solicitud incluida (una o varias, HU-09) cuyos productos ya tengan todos cotización»*. La condición se conserva: así lo hace el código (`SolicitudService`, `todoCotizado()`) y lo pide HU-12.

### p. 10 y 11 · §7 Arquitectura hexagonal

**R-25 · Ya es cierto (H25, D12).** «El correo certificado pasa por el puerto Correo» y «la demostración del esqueleto Java corre sin base de datos ni Kafka» son ciertos.

### p. 12 · §8 Estilos de apoyo
Sin cambios. Las dos llamadas síncronas entre servicios siguen siendo las únicas.

### p. 13 · §9 El mapa integrado y su notación

**R-26 · Cambiar (DEC-15, DEC-16, DEC-17).** Primer párrafo: «La fila central son los once microservicios: cada caja azul contiene arriba sus adaptadores de entrada y de salida síncrona, al centro el hexágono con el dominio y abajo los listeners, el adaptador JPA con su base y el publicador de eventos. La banda ámbar del fondo es Kafka con sus diez tópicos. A la derecha están los sistemas externos y el proveedor.» → *«La fila central son los once microservicios: cada caja azul contiene arriba sus adaptadores de entrada y de salida síncrona, al centro el hexágono con el dominio y abajo los listeners, los adaptadores de persistencia con su base y el publicador de eventos. Cada caja verde agrupa los adaptadores de una misma tecnología y destino (por ejemplo, «CotizacionJpaAdapter / ProveedorJpaAdapter»); el detalle por clase está en las páginas 4a a 4e. Junto a las bases aparecen el almacén de objetos [S3 compatible], donde ms-cotizaciones, ms-ordenes y ms-notificaciones guardan y leen los PDF; los logs de error [Elasticsearch · ELK], que consulta ms-administracion; y la base [PostgreSQL] de ms-integracion, que guarda su tabla outbox junto a la caché de precios en Redis. La banda ámbar del fondo es Kafka con sus diez tópicos. A la derecha están los sistemas externos y el proveedor.»*

**R-27 · Cambiar (DEC-16).** Tabla de notación, fila «Base de datos o caché»: «BD pedidos [PostgreSQL]; caché de precios [Redis]» → símbolo *«Base de datos, caché o almacén»*, ejemplo *«BD pedidos [PostgreSQL]; caché de precios [Redis]; almacén de objetos [S3 compatible]»*. La leyenda del mapa (`v312`) ya dice «Base de datos (una por servicio) o almacén compartido».

### p. 14 a 19 · Figuras 1 a 6

**R-28 · Cambiar (R-01).** Volver a capturarlas. Los pies de figura siguen siendo correctos, con un ajuste en la Figura 5 (p. 18) si se quiere ser preciso: *«ms-notificaciones combina Factory Method y Decorator para el correo certificado y lee los PDF del almacén de objetos»*.

### p. 20 y 21 · §10 Arquitectura por flujo de ventanas

**R-29 · Cambiar (DEC-43, DEC-46).** Fila «Jefe · inventario y alarmas»: «La tarea diaria descuenta el consumo, evalúa el umbral del 25 % y publica inventario.stock-bajo, que se convierte en alarma en la app (WebSocket) y en correo.» → *«La tarea diaria descuenta el consumo, evalúa el umbral del 25 % y publica inventario.stock-bajo cuando un insumo baja de ese umbral, que se convierte en alarma en la app (WebSocket) y en correo. La alerta queda atendida cuando el jefe registra una solicitud (solicitud.registrada).»*

**R-30 · Cambiar (DEC-43).** Fila «Director · validación»: «más de 25 % por encima, RECHAZADA; más de 50 % por debajo, SOSPECHOSA; el resto, OPCIONADA (Chain of Responsibility).» → *«más de 25 % por encima, RECHAZADA; más de 50 % por debajo, SOSPECHOSA; el resto, OPCIONADA, incluidos +25,00 % y −50,00 % (Chain of Responsibility). La diferencia se redondea a 2 decimales antes de comparar.»*

**R-31 · Cambiar (H7, PD-03).** Fila «Director comercial · BAM»: agregar *«Sin filtro, la demanda se mide sobre el último mes y el proveedor con mejores precios sobre el último año; si el director elige un rango de fechas, los tres indicadores usan ese rango, y la sucursal filtra la demanda (HU-18). El proveedor se mide con la diferencia promedio de sus cotizaciones OPCIONADAS frente al precio estándar.»*

**R-32 · Cambiar (DEC-18, D4, DEC-19, H5, D3, H10).** Tabla «Recorrido completo de una compra». Las filas que cambian, columna Mensaje:

| # | Actual | Nuevo |
|---|---|---|
| 2 | solicitud.registrada → ms-notificaciones | *solicitud.registrada → ms-notificaciones (correo), ms-analitica (KPI) y ms-inventario (atiende la alerta de stock bajo)* |
| 4 | POST /api/cotizaciones → precio del Ministerio (REST → SOAP), PDF, ENVIADA | *POST /api/cotizaciones con una o varias solicitudes (aquí SOL-031) → precio del Ministerio (REST → SOAP), PDF al almacén, ENVIADA* |
| 11 | POST /api/ordenes-compra (consulta REST a ms-cotizaciones) → 201 OC-0012 | *POST /api/ordenes-compra (GET /api/cotizaciones/{id} a ms-cotizaciones) → 201 OC-0012* |
| 12 | orden.generada → ms-solicitudes, ms-notificaciones, ms-analitica | *orden.generada → ms-solicitudes (cada solicitud a COMPLETADA), ms-cotizaciones (CON ORDEN), ms-notificaciones (PDF al proveedor) y ms-analitica* |
| 14 | auditoria.evento → ms-administracion (Ventana: «todas») | *auditoria.evento de los otros diez servicios → ms-administracion* |

En la fila 2, columna «Qué pasa»: «Confirmación al jefe por correo» → *«Confirmación al jefe por correo y alerta de stock atendida»*.

### p. 21 · §11 Eventos del sistema

**R-33 · Cambiar (H10).** Introducción: «Cada uno tiene un único productor (salvo la auditoría, que publican todos) y uno o más consumidores.» → *«Cada uno tiene un único productor (salvo la auditoría, que publican los otros diez servicios) y uno o más consumidores.»*

**R-34 · Cambiar (H2, D4, D3, H4, H10, DEC-43, DEC-46).** Tabla de tópicos. Filas que cambian:

| Tópico | Publica | Consumen | Qué lo dispara y para qué |
|---|---|---|---|
| menu.actualizado | ms-menu | ms-pedidos, *ms-inventario* | Plato creado o editado (06): actualiza el catálogo del mesero (03) *y la copia local de las recetas con la que ms-inventario calcula el consumo* |
| pedido.registrado | ms-pedidos | ms-inventario, ms-analitica | Pedido creado (03/04), *con sus líneas (plato y cantidad)*: consumo para el descuento diario y KPI de demanda |
| inventario.stock-bajo | ms-inventario | ms-notificaciones, ms-analitica | *Existencia por debajo del 25 % (porcentaje redondeado a 2 decimales), publicado la noche en que el insumo cruza el umbral*: alarma en la app (05) y correo al jefe |
| solicitud.registrada | ms-solicitudes | ms-notificaciones, ms-analitica, *ms-inventario* | Solicitud PENDIENTE (08/09): correo de confirmación, KPI *y alerta de stock atendida (HU-16)* |
| orden.generada | ms-ordenes | ms-solicitudes, *ms-cotizaciones*, ms-notificaciones, ms-analitica | Orden de compra (21/22): *cada solicitud pasa a COMPLETADA, la cotización a CON ORDEN* y se avisa al proveedor *con el PDF de la orden* |
| auditoria.evento | *los otros 10 servicios* | ms-administracion | *Cada CRUD de los otros 10 servicios* (ms-administracion registra los suyos directo): acción, usuario, fecha y hora |

Las filas solicitud.en-tramite, cotizacion.enviada, cotizacion.validada y tendencia.detectada no cambian. **Opcional (DEC-07):** en tendencia.detectada, agregar *«como máximo un aviso por día»*.

### p. 22 · §12 Patrones de diseño en el nivel de código

**R-35 · Cambiar (H1, K2, P-10).** Introducción: «En las páginas 4a a 4e del .drawio cada clase con patrón lleva su estereotipo en magenta, y el esqueleto Java implementa el mismo comportamiento.» → *«En las páginas 4a a 4e del .drawio cada clase con patrón lleva su estereotipo en magenta; el esqueleto Java declara las mismas clases, firmas y relaciones, y la demostración ejecuta los patrones.»*

**R-36 · Ya es cierto (H8).** Fila Chain of Responsibility: «(cadenaPorDefecto)». Desde la sesión 3, `ReglaValidacion` es una clase abstracta con `cadenaPorDefecto()`, en la 4c y en el código.

**R-37 · Cambiar (H9, P-10).** Fila «Observer»: el nombre del patrón pasa a *«Publish-Subscribe (Observer distribuido)»*, como en la 4a. Columna «Por qué aquí»: «Publicar hechos sin conocer a los suscriptores» → *«Publicar hechos sin conocer a los suscriptores. A diferencia del Observer de GoF, el publicador no guarda la lista de observadores: Kafka hace de intermediario»*. Las demás columnas quedan igual.

**R-38 · Cambiar (H13, P-10).** Fila «Singleton», columna «Por qué aquí»: «Una sola fuente de parámetros por instancia: umbral del 25 %, tema, logos» → *«Una sola fuente de parámetros por instancia: tema, logos e imagen corporativa»*. Columna «Consecuencia»: «Estado global: de solo lectura para el resto, recargado cuando cambia» → *«Estado global: de solo lectura para el resto, recargado cuando cambia. La SPA lo lee con GET /api/admin/parametros/tema. El umbral del 25 % no vive aquí: es una regla de ms-inventario»*.

**R-39 · Opcional (D1).** Fila «Facade», columna «Por qué aquí»: «Un clic coordina precio sugerido, repositorio, PDF y evento» → *«Un clic coordina precio sugerido, repositorio, PDF, almacén de objetos y evento»*.

### p. 23 a 25 · Figuras 7 a 9 (pasan a 8 a 10)

**R-40 · Cambiar (R-01, R-03).** Volver a capturar los fragmentos de 4a, 4c y 4d y renumerar. Ajustes a los pies:
- Figura 8 (antes 7), p. 23: correcto como está.
- Figura 9 (antes 8), p. 24: «GenerarCotizacionFacade depende de los puertos de repositorio, precio sugerido y PDF.» → *«GenerarCotizacionFacade depende de los puertos de repositorio, precio sugerido, PDF, almacén y eventos.»*
- Figura 10 (antes 9), p. 25: correcto. La captura nueva muestra `adjuntoUrl` en `Notificacion.enviar()` y `CreadorNotificacion.notificar()` (DEC-42).

### p. 26 · §13 Del diagrama al código

**R-41 · Cambiar (H1).** «Un solo modelo, tres salidas»: «El mapa, el diagrama de clases, el esqueleto Java y las tablas de este documento se generan desde el mismo modelo, de modo que los nombres coinciden uno a uno.» → *«El esqueleto Java se generó a partir de las páginas 4a a 4e del .drawio, de modo que los nombres coinciden uno a uno con el diagrama de clases y con el mapa.»* La frase que sigue, «Antes de entregar se comprobó automáticamente que cada clase que aparece en el mapa (…) existe en el código y que el método asignado a cada una de las 23 ventanas está declarado en su controlador», **ya es cierta** (sesión 3: 191 clases, 403 métodos y las 23 ventanas). Antes de regenerar el reporte hay que volver a correr esa comprobación.

**R-42 · Cambiar (P-12, DEC-45).** Estructura del esqueleto: «con doce paquetes de servicio (los once microservicios más compartido) y 155 clases, además de la clase DemoArquitectura.» → *«con doce paquetes de servicio (los once microservicios más compartido) y 191 clases, las mismas de las páginas 4a a 4e. Aparte está el paquete demo, con DemoArquitectura y 19 clases de apoyo (adaptadores en memoria y el ensamblador que los conecta), que no son parte del modelo; y fuera de src, la carpeta pruebas, con 176 verificaciones.»*

**R-43 · Cambiar (DEC-38, DEC-35, DEC-36).** Después de «los adaptadores reciben sus anotaciones (@RestController, @Repository, @KafkaListener) sin tocar el dominio, que es precisamente lo que promete la arquitectura hexagonal.», agregar: *«Dos anotaciones resuelven lo que trae tener varias réplicas: el Proxy de precios lleva además @Primary, para que ConsultarPrecioService reciba el Proxy y no el adaptador real, y los dos jobs programados llevan @SchedulerLock (ShedLock), para que cada uno corra en una sola réplica. El Javadoc de cada adaptador dice qué anotación lleva en el proyecto real.»*

**R-44 · Ya es cierto (H1).** «Los métodos marcados en el diagrama con ‹Vxx› llevan en el código el comentario «Atiende la ventana Vxx de los mockups».» Hay 29 líneas de ese comentario, que cubren las 23 ventanas.

**R-45 · Cambiar (D12, DEC-42).** «Comprobación con la demostración»: «DemoArquitectura ejecuta los patrones sin base de datos ni broker, usando adaptadores en memoria. Su salida coincide con los datos de los mockups:» → *«DemoArquitectura recorre el flujo de compra de punta a punta (pedido, alarma de stock, solicitud SOL-031, cotizaciones COT-018 a COT-020 y orden OC-0012) y ejecuta los patrones sin base de datos ni broker, usando adaptadores en memoria. Su salida coincide con los datos de los mockups. Para mantenerla simple, los eventos llevan «sistema» (o el correo de la entidad) como usuario y cada uno crea su propia correlación, y los consumidores no descartan los eventos repetidos; en el sistema real, el usuario sale del JWT, la correlación une todo el recorrido y cada consumidor recuerda los identificadores ya procesados.»*

**R-46 · Cambiar (P-11, DEC-31, H9).** Tabla de la demostración. Filas que cambian:

| Patrón | Salida de la demostración (actual → nueva) | Ventana |
|---|---|---|
| Hexagonal + Observer → *Hexagonal + Publish-Subscribe* | Sin cambio: «Pedido registrado, evento pedido.registrado publicado, total $82.000» | V03 y V04 |
| Facade + Chain of Responsibility | **Opcional.** El PDF ya dice «COT-018: estándar $520.000, proveedor $545.000, diferencia 4,81 %, OPCIONADA. COT-019: +34,62 %, RECHAZADA. COT-020: −61,54 %, SOSPECHOSA», que es lo que imprime la demo. Solo se puede agregar el signo («+4,81 %») y los totales de proveedor ($700.000 y $200.000) | V13, V18 y V19 |
| Singleton | «umbral.stock = 25 %, misma instancia en dos llamadas» → *«tema.color-primario = #F26B21, misma instancia en dos llamadas»* | Parametrización |

Las filas Facade + Chain, Composite + Factory Method, Factory Method + Decorator, Proxy + Adapter y Strategy coinciden con la salida actual y no cambian. **Nota:** la demo ya imprime «Hexagonal + Publish-Subscribe» (cambio hecho por pedido de Mauricio, solo en el título; ver el commit H9 del código).

**R-47 · Cambiar.** «Para compilar y ejecutar: javac -d bin $(find src -name "*.java") y luego java -cp bin co.edu.konradlorenz.demo.DemoArquitectura.» → *«Para compilar, ejecutar la demostración y correr las pruebas: ./ejecutar.sh (necesita un JDK 21). En Windows, las órdenes sueltas están en el README.»*

### p. 27 · §14 Contexto y despliegue

**R-48 · Sin cambio.** «seis tipos de persona y cuatro sistemas externos… El proveedor aparece como persona externa» coincide con la p. 2 del `.drawio`.

**R-49 · Cambiar (H24, DEC-20).** Despliegue: después de «en un clúster Kubernetes repartido en tres zonas,» agregar: *«con un gateway y un broker de Kafka en cada zona y las réplicas de cada servicio repartidas en al menos dos zonas, de modo que si cae una siguen funcionando dos de tres gateways y brokers; el Service Registry y la SPA tienen dos réplicas en zonas distintas;»*. El reparto de Eureka y la SPA lo confirmó el arquitecto-revisor (DEC-20, 2026-11-08): dos réplicas en zonas distintas, con A y B como etiquetas ilustrativas.

**R-50 · Cambiar (H16).** «El centro de datos alterno, en otra región, mantiene las mismas imágenes con réplicas mínimas encendidas, recibe los tópicos con MirrorMaker 2 y las bases por réplica asíncrona;» → *«…, recibe los tópicos con MirrorMaker 2, las bases por réplica asíncrona y el almacén de objetos por replicación entre regiones;»*. Y «El respaldo diario combina copia nocturna y registros continuos con retención de 30 días.» → *«El respaldo diario combina copia nocturna y registros continuos de las bases, más una copia diaria del almacén de objetos, con retención de 30 días.»*

### p. 30 · §15 Trazabilidad de las 23 ventanas

**R-51 · Ya es cierto (H1).** «el método existe en el esqueleto Java con el comentario de la ventana que atiende.»

**R-52 · Cambiar (W-03, W-06, W-13, W-20, W-21; H5, H6, DEC-07).** Agregar estas filas o líneas a la tabla. Las rutas son las que fijan el diagrama y el comentario del código; donde ninguna fuente fija la ruta, se dice así:

| V | Ventana | Servicio | Controlador y método | Endpoint |
|---|---|---|---|---|
| 03 | Nuevo pedido | ms-pedidos | *PedidoRestController.catalogo(sucursalId)* | *GET /api/pedidos/catalogo* |
| 03 | Nuevo pedido | ms-pedidos | *PedidoRestController.mesas(sucursalId)* | *GET /api/pedidos/mesas* |
| 06 | Nuevo plato | ms-menu | *PlatoRestController.productosPorCategoria(categoriaId)* | *bajo /api/platos (ruta sin fijar)* |
| 13 | Generar solicitud de cotización | ms-cotizaciones | *ProveedorRestController.consultar(soloActivos)*: el desplegable de proveedores activos | *GET /api/proveedores* |
| 20 y 21 | Cotizaciones opcionadas · Generar orden de compra | ms-cotizaciones | *CotizacionRestController.detalle(numero)* | *GET /api/cotizaciones/{id}* |

En V21, la fila de `OrdenCompraRestController.generar()` se queda; la nueva línea dice de dónde salen los datos de la cotización que la ventana muestra (ms-ordenes los pide por el mismo GET).

**R-53 · Opcional (W-24 a W-35).** Agregar después de la tabla una segunda tabla, *«Métodos de controlador que no atienden una ventana»*, para que el lector vea las rutas sin mockup (supuesto S5):

| Controlador y método | Servicio | Endpoint | Origen |
|---|---|---|---|
| UsuarioRestController.registrar(…) | ms-identidad | POST /api/usuarios (administrador) | DEC-10 |
| PlatoRestController.consultar(sucursalId) | ms-menu | bajo /api/platos (ruta sin fijar) | D5 |
| PlatoRestController.editar(id, p) y desactivar(id) | ms-menu | PUT /api/platos/{id} · PATCH /api/platos/{id}/desactivar | D5 |
| CotizacionRestController.reenviar(numero) | ms-cotizaciones | POST /api/cotizaciones/{id}/reenvio | H18 |
| ProveedorRestController.registrar, detalle, editar e inactivar | ms-cotizaciones | /api/proveedores/\*\* | DEC-07 (HU-15) |
| DestinatarioRestController.registrar y consultar | ms-notificaciones | /api/notificaciones/destinatarios | DEC-13 |
| AdminRestController.parametrizar, auditoria y logs | ms-administracion | /api/admin/\*\* | S5, H14 |
| AdminRestController.tema() | ms-administracion | GET /api/admin/parametros/tema | H13 |
| TableroRestController.kpis(desde, hasta, sucursalId) | ms-analitica | GET /api/bam/kpis?desde&hasta&sucursalId | H7 (HU-18) |
| PromocionRestController.sugerencias, aceptar y descartar | ms-analitica | /api/bam/promociones | DEC-07 (HU-19) |
| PrecioRestController.precioSugerido(productoId) | ms-integracion | interna, sin ruta en el gateway (REST/JSON con mTLS desde ms-cotizaciones) | — |
| AbastecimientoSoapEndpoint.consultarPrecioSugerido(productoId) | ms-integracion | SOAP bajo /api/integracion/\*\* | D2, DEC-32 |

Donde dice «sin fijar» o no da verbo, ninguna fuente fija la ruta exacta (`INVENTARIO_SESION3.md`, W-24 a W-35); no se inventa.

**R-60 · Cambiar (DEC-51, sesión 6).** Fila de la ventana 03, columna Endpoint: «POST /api/pedidos» → *«POST /api/pedidos (el pedido guarda el meseroId del token JWT, HU-01)»*. Desde la sesión 6, `Pedido` tiene `meseroId: Long` (4b y código): el pedido nace con el mesero que lo tomó, `validar()` lo exige y `pedidosDelTurno()` (V02) filtra por él. Si se recapturan las figuras de la 4b (R-59), la clase `Pedido` ya muestra el atributo.

### p. 31 · §16 Riesgos y §17 Supuestos

**R-54 · Ya es cierto (H18).** Riesgo «El servicio web del Ministerio no responde»: «si no hay precio, la cotización queda pendiente de precio y se reintenta sin bloquear al auxiliar». Existe el estado `PENDIENTE_PRECIO` y `POST /api/cotizaciones/{id}/reenvio`. **Opcional:** nombrarlos.

**R-55 · Ya es cierto (H11).** Riesgo del outbox: la 4a ahora lo dibuja (nota `n_outbox`).

**R-56 · Cambiar (A2, PD-01).** Agregar a la tabla de riesgos: *«La alarma la procesa una réplica de ms-notificaciones distinta de la que tiene abierta la conexión del jefe · El jefe no la ve en tiempo real · La réplica que consume el evento la difunde por Redis pub/sub a todas las réplicas, y cada una la entrega a sus conexiones; además, V05 consulta las alertas activas al abrirse, así que la alarma no se pierde.»* La sesión pegajosa en el balanceador no basta: la réplica que procesa el evento la asigna Kafka por partición (PD-01). Si se describe la caché en Redis (p. 13 o §14), agregar que el mismo Redis lleva el canal de alarmas, separado de la caché.

**R-57 · Cambiar (DEC-10, DEC-07).** Supuesto S5: «El director comercial y el administrador no tienen mockup; sus servicios y rutas quedan listos» → *«El director comercial y el administrador no tienen mockup; sus servicios y rutas quedan listos, incluida el alta de usuarios por el administrador (POST /api/usuarios). Tampoco tienen mockup el registro de proveedores (HU-15, /api/proveedores) ni el histórico de promociones sugeridas (HU-19, /api/bam/promociones).»* Agregar al final: *«Los seis roles son fijos y el administrador asigna uno al registrar cada usuario.»* (PD-06). Columna «Por qué»: queda igual.

**R-61 · Opcional (DEC-51, sesión 6).** Agregar el supuesto S9: *«El mesero que registra un pedido es el usuario del token JWT; la SPA no lo envía»*, con «Por qué»: *«Así nadie registra un pedido a nombre de otro mesero, y el pedido queda asociado al usuario que lo tomó, como pide HU-01»*.

### p. 32 · §18 Declaración de uso de inteligencia artificial

**R-58 · Cambiar (PD-11, hallazgo N4).** El reporte declara solo la generación inicial. El texto que se proponía antes decía que «las decisiones de diseño las tomó el equipo», lo que es falso para las decisiones delegadas al agente revisor. Agregar al final de «Herramienta»: *«Las correcciones posteriores al 7 de noviembre de 2026 se hicieron con IA: Claude Code (Anthropic, [modelos]) para el .drawio, el código, la demostración, las pruebas, el Excel y los documentos, con un agente revisor de Claude que validó cada sesión; Codex (OpenAI, [modelo]) continuó la sesión 6 (RCU-11 a RCU-13 y su validación), y el trabajo posterior a la sesión 6 se hizo de nuevo con Claude Code. Las instrucciones y cada decisión con sus alternativas están en DECISIONES.md y en el historial de git (un commit por corrección). Mauricio Iturriza tomó la mayoría de las decisiones; DEC-23, DEC-24, DEC-49, DEC-50 y PD-01 a PD-11 las tomó el agente revisor por delegación expresa suya.»* Los modelos y versiones (A6) y la autoría los confirma el equipo antes de publicarlo; no se inventan.

### p. 33 a 38 · Referencias y anexo
- Referencias: sin cambios.
- **R-59 · Cambiar (R-01).** Anexo A: volver a capturar las páginas 4a a 4e. El texto del anexo sigue siendo correcto.

---

## 3. De dónde sale cada corrección

| Hallazgo o decisión | Correcciones |
|---|---|
| H1 (p. 3, 22, 26 y 30) | R-06, R-07, R-35, R-41, R-44, R-51 |
| H2 | R-34 |
| H4, D1 | R-17, R-20, R-24, R-34, R-39 |
| H5 | R-20, R-32, R-52 |
| H6 | R-52 |
| H7 | R-31, R-53 |
| H8 | R-36 |
| H9 | R-37, R-46 |
| H10 | R-13, R-15, R-32, R-33, R-34 |
| H11 | R-21, R-55 |
| H13 | R-11, R-38, R-46 |
| H14 | R-14, R-53 |
| H15, DEC-10 | R-09, R-17, R-53, R-57 |
| H16 | R-12, R-50 |
| H18 | R-19, R-53, R-54 |
| H21 | R-08, R-16 |
| H24, DEC-20 | R-49 |
| H25 | R-10, R-25 |
| D2, DEC-12, DEC-32 | R-17, R-18, R-53 |
| D3, D4 | R-19, R-32, R-34 |
| D12, DEC-31, P-11 | R-06, R-45, R-46 |
| DEC-05, DEC-19 | R-24, R-32 |
| DEC-07 | R-17, R-34, R-52, R-53, R-57 |
| DEC-15, DEC-16, DEC-17 | R-26, R-27 |
| DEC-18 | R-32 |
| DEC-35, DEC-36, DEC-38 | R-43 |
| DEC-42 | R-13, R-21, R-23, R-40, R-45 |
| DEC-43, DEC-46 | R-22, R-29, R-30, R-34 |
| DEC-45, P-12 | R-42 |
| P-10, K2 | R-07, R-35, R-37, R-38 |
| W-03, W-06, W-13, W-20, W-21 | R-52 |
| W-24 a W-35 | R-53 |
| DEC-51 (A4) | R-60, R-61 |
| «Ocho páginas», páginas 3a, 3b y 6 | R-01 a R-05, R-28, R-40, R-59 |

Los hallazgos H3, H12, H17, H19, H20, H22, H23 y H26 no tocan el texto del reporte: se corrigieron en el `.drawio`, el código, el README o el Excel, o van en `CORRECCIONES_HU_Y_MOCKUPS.md` (H26).

## 4. Lo que queda para el equipo

- **A1:** quién tiene el script de ReportLab. Sin él, nada de esto se aplica (DEC-03).
- **T-06:** la demo no consume `cotizacion.validada` ni `tendencia.detectada` (DEC-42). El reporte describe la arquitectura, que sí los consume, así que no hace falta cambiar el texto; si alguien pregunta en la sustentación, la respuesta está en DEC-29 y DEC-42.
- **R-58:** los modelos y versiones de Claude Code y Codex (A6), y la confirmación de la autoría, antes de publicar la declaración (PD-11).
