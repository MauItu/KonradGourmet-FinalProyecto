# Validación del corte 7 · plataforma completa

Validación del arquitecto revisor sobre la plataforma completa (cortes 1 a 7 de la rama `stack-real`), paso 7c del plan del corte 7 (DEC-95). Fecha: 2026-11-23. Revisó el código (`src/`, `servicios/`, `web/`, `plataforma/`), el `.drawio`, `DECISIONES.md` (DEC-57 a DEC-100), `CAMBIOS_A_DOCUMENTAR.md`, los casos de uso, el Story Mapping corregido, los mockups y el reporte v2.

**Veredicto: lista con correcciones.** La arquitectura y la plataforma coinciden en lo grueso: los 11 servicios, los 13 prefijos del gateway, los 10 tópicos más `notificaciones.dlq`, la hexagonal (el núcleo solo importa `java.*` y cada servicio lo copia al compilar, DEC-61), las 195 clases del diagrama, JWT RS256 validado en el gateway y en cada servicio, `@PreAuthorize` en todos los endpoints, PD-01, el claim check del PDF y ShedLock en los dos jobs. DEC-100 queda bien en lo esencial.

Mauricio decidió los cuatro hallazgos que lo pedían (DEC-101 a DEC-104); los detalles que escogió Claude están en DEC-105. La columna «Estado» dice qué se hizo con cada uno.

## Hallazgos

| # | Sev. | Hallazgo | Corrección | Estado |
|---|---|---|---|---|
| C7-1 | Alta | Un jefe de cocina puede editar, desactivar, reactivar o cambiar la imagen de un plato de otra sucursal: `PlatoRestController` busca el plato solo por id | El adaptador comprueba que el plato sea de la sucursal del JWT y responde 404 si no; JUnit con un jefe de otra sucursal | Corregido (código 0bc5fa4; `PlatoSucursalTest`) |
| C7-2 | Alta | V11: los mensajes de lista vacía quedaron invertidos (regresión de DEC-97); la prueba solo buscaba «No se encontraron solicitudes» | Sin resultados de búsqueda: «No se encontraron solicitudes con los criterios indicados» (HU-07, 4); sin solicitudes: «No se encontraron solicitudes registradas» (HU-06, 5); la prueba compara el texto completo; CU-07 agrega el flujo alterno de HU-07 | Corregido (código a928986; CU-07 0e9db38) |
| C7-3 | Media | El rango de fechas invertido de ms-solicitudes dice «La fecha «desde» es posterior a «hasta»» | «La fecha inicial no puede ser posterior a la fecha final.» (HU-07, 6; PD-10), con la prueba del texto completo | Corregido (código 9159973) |
| C7-4 | Alta | `reactivar()` está en el código, la SPA y el mockup, pero no en el diagrama, en CU-03 ni en CAMBIOS | **DEC-101 (Mauricio):** se lleva a la 4b, a la página 1 y a CU-03, con un CD nuevo | Corregido: DEC-101 (`.drawio` e65149b, CU-03 51f09ea, CD-62) |
| C7-5 | Media | ms-inventario no descarta un `pedido.registrado` repetido y el cierre descuenta dos veces | **DEC-104 (Mauricio):** todos los consumidores descartan los eventos repetidos | Corregido: DEC-104 (código d4962fc; `PedidoRepetidoTest` y `verificar_corte7`) |
| C7-6 | Media | El diagrama describe outbox transaccional, consumidores idempotentes y límite de tasa en el gateway; la plataforma no los tiene, y la base `ms_integracion` no se usa | **DEC-104 (Mauricio):** se implementan los tres; ms-integracion usa su base para la outbox | Corregido: DEC-104 (código 9f0370f, d0b858d, d4962fc, e165371) y DEC-105 |
| C7-7 | Media | HU-16 (4) pide producto, cantidad restante, unidad y sucursal; la fila de V05 solo muestra el nombre y el porcentaje, y el correo no nombra la sucursal | **DEC-102 (Mauricio):** la fila de V05 con la cantidad y la unidad, el correo con la sucursal y CU-05 precisa la alarma en vivo | Corregido: DEC-102 (código 4b7fe7e y 4c9f198, 4d 86f0431, CU-05 bde7f29; el mockup de V05 queda en CD-65) |
| C7-8 | Media | `CAMBIOS_A_DOCUMENTAR.md`: seis CD apuntan a la «tabla 5» del reporte (solo prefijos) y no a la tabla 13 (§15); faltan rutas (productos por categoría, imagen del plato, reactivar, proveedores, reenvío, destinatarios, alta de usuarios) y un «No aplica» para los accesos de demostración de V01; no se explica el salto de CD-35 a CD-50 | Corregir el destino, agregar los CD que faltan y una línea sobre la numeración | Corregido (CAMBIOS c463830 y b5109bc, página 1 1cccf49; la tabla 13 del reporte en el 7d) |
| C7-9 | Baja | El cierre diario confirma el inventario y el consumo pendiente en transacciones separadas: si se cae entre las dos, repite el descuento | El job corre su cuerpo en una transacción, dentro del candado de ShedLock | Corregido (código f38bb0f) |
| C7-10 | Baja | Página 3a: dos flechas con el id `c3a_e31d` | Renombrar una (`c3a_e31e`) | Corregido (d62722c) |
| C7-11 | Baja | Página 5: `v26` y `e47` no nombran las imágenes de los platos (ms-menu, DEC-67) | Agregarlas | Corregido (4c21458) |
| C7-12 | Baja | `ReprocesoColaErrores` no está en la nota de infraestructura de ms-notificaciones (4d) | Nombrarla en `n_infra_notif` | Corregido (b36fe99) |
| C7-13 | Media | CU-05 cita «el código del 8 de noviembre» (contra DEC-57) | Texto nuevo (abajo) | Corregido (ff95985) |
| C7-14 | Media | Los casos de uso mencionan IA fuera de la §18 («aprobada por el arquitecto-revisor de Claude»); la línea 3 dice «versión corregida del 8 de noviembre» | «Estado: propuesta de diseño, sin estimar (PD-09); no forma parte del Story Mapping original.» y la fecha de la versión final | Corregido (7511666) |
| C7-15 | Baja | DEC-100: si el nombre del producto no está en el catálogo y el jefe no tiene solicitudes, V08 precarga la solicitud de ejemplo | No precargar cuando viene `nombreProducto` | Corregido (código 5c250f5) |
| C7-16 | Baja | Con una URL desconocida, la SPA muestra «se construye en el corte undefined»; `pantallas.ts` aún dice «sin mockup» | «La ventana no existe» con un enlace a Inicio; quitar los datos viejos de `pantallas.ts` | Corregido (código 1eb658e y c96c8ff: una dirección fuera del menú lleva a la primera opción del rol) |
| C7-17 | Baja | V11 pagina cada 6 filas; HU-06 (4) y CU-07 dicen 25 | **DEC-103 (Mauricio):** páginas de 25 | Corregido: DEC-103 (código 4408638) |
| C7-18 | Baja | `README.md` dice 398 verificaciones (son 399); `ESTADO_PROYECTO_Y_C4.md` dice «falta el corte 7» y D5 «Sin mockup» | Se corrigen en el 7d | Corregido en el 7d (código 4eaff2c: 400 verificaciones; documentos 72e6da1) |

Sin hallazgo: las claves de las bases y de S3 fijas en `application.yml` y en el compose (se aceptan en local) y `/plataforma/registro` y `/plataforma/rutas` sin token (solo en local).

## CU-05: texto propuesto (C7-13)

> **Reglas y fuente:** el proceso no debe afectar el desempeño en horario de operación. El 25 % es un parámetro propio de ms-inventario (`UmbralStock`); no se cambia desde la parametrización (DEC-43). El modelo no tiene una entidad Alerta: la alerta es la marca `alertaAtendida` de `InsumoInventario` (página 4b, nota `n_alerta`). `alertas()` devuelve los insumos bajo el umbral con la marca sin atender, `solicitud.registrada` la atiende y `descontar()` la reactiva cuando el insumo vuelve a cruzar el umbral.

## CD pendientes

- Para el 7d (reporte): CD-02, 08, 10, 12, 14, 15, 23, 33, 34, 50, 55 y 60. Ninguno quedó pendiente fuera del reporte. Aplicados al regenerar el reporte en el 7d, con CD-62 y CD-63 (tabla 13) y CD-70 a CD-73.
- Nuevos por esta revisión: `reactivar()` (C7-4), las rutas de C7-8, el «No aplica» de los accesos de V01 y lo que cambien DEC-102 y DEC-104.

## Reglas de la entrega

- IA: el reporte la menciona solo en la §18; la otra mención estaba en los casos de uso (C7-14).
- Ningún texto dice que los mockups de `mockups_faltantes/` son capturas.
- Límites de la demostración: la única violación es la de CU-05 (C7-13).

## Segunda ronda (sobre las correcciones)

**Veredicto: lista con correcciones.** C7-1 a C7-17 quedan bien resueltos; las 195 clases y las firmas tocadas coinciden entre el `.drawio` y `src/`; no quedan ids repetidos. Hallazgos nuevos:

| # | Sev. | Hallazgo | Corrección | Estado |
|---|---|---|---|---|
| C7-19 | Media | Sin sesión, el límite de tasa tomaba la primera IP de `X-Forwarded-For`, que el cliente puede inventar | Nginx pone `X-Real-IP` y el gateway la usa; prueba con 600 inicios de sesión con IP inventadas | Corregido (código 0e830f3; DEC-105) |
| C7-20 | Media | El gateway depende de Redis para el límite de tasa y el diagrama no lo mostraba | Flecha `c3a_e31f` y etiqueta de Redis en la 3a; `v11` y `v25` en la página 5; CD-69 | Corregido (676ae08, ffafb35, ff8448a) |
| C7-21 | Media | El relé esperaba evento por evento (tope de unos cientos por segundo) y con Kafka caído podía bloquearse 60 s | Lote entero, una espera por lote y un `DELETE … ANY`; productor con 10 s de máximo; DEC-105 dice «orden por clave» | Corregido (código 74869b5) |
| C7-22 | Baja | `AuditoriaJpaAdapter` atrapaba la violación del índice dentro de la transacción | Se quita el `catch`: la transacción se deshace y Kafka reintenta | Corregido (código 94774ec) |
| C7-23 | Baja | El comentario de `NotificacionListeners.CONSUMIDOR` decía que `ReprocesoColaErrores` usa el mismo nombre | Comentario corregido | Corregido (código 13d7719) |
| C7-24 | Baja | Comentarios de la SPA con «sin mockup» | Nombran la carpeta de su mockup | Corregido (código 287db28) |
| C7-25 | Baja | `README.md` dice que los consumidores no descartan repetidos y que `registrarDestinatario()` no audita | Se corrige con C7-18 | Corregido en el 7d (código 668f587) |
| C7-26 | Baja | La copia vieja de los casos de uso en la raíz de `FinalProyecto/` cita «el código del 8 de noviembre» y enlaza a claude.ai | Moverla a `archivo_desactualizado/` (recomendado) o dejarla y avisar al equipo | Movida a `FinalProyecto/archivo_desactualizado/` (Mauricio, 2026-11-23; fuera de git) |
| C7-27 | Baja | La transacción de `generar` (cotizaciones) abarca las llamadas remotas; si se deshace, el PDF queda huérfano en S3 | Aceptarlo (recomendado) o partir la fachada, que cambia la 4c | Aceptado (Mauricio, 2026-11-23): no cambia nada |

Observación sin hallazgo: `deLaSucursal()` de ms-menu lee los platos de la sucursal en cada llamada; un `detalle(id)` lo evitaría, pero cambia la 4b.

## Tercera ronda (7d: README, LEEME y reporte regenerado)

**Veredicto: lista con correcciones.** Lo pedido para el 7d está hecho: README y LEEME finales, los CD del reporte, la figura 7 y la renumeración, DEC-104 en el texto. Las figuras de página son idénticas al píxel a una exportación nueva del `.drawio`, y las cifras (195 clases, 61 pendientes, 24 clases de apoyo, 400, 91, 763) se cumplen.

| # | Sev. | Hallazgo | Corrección | Estado |
|---|---|---|---|---|
| C7-28 | Alta | La cifra de 1.254 sumaba dos corridas: la última usó `--sin-compilar` (JUnit 0) | Correr `probar_todo.sh` completo sobre HEAD | Corregido: corrida completa con compilación, 1.254 sin fallas (tabla de abajo); ningún texto cambia |
| C7-29 | Media | §16 y §6.2 decían que la idempotencia evita correos repetidos; ms-notificaciones marca después de enviar | La fila de §16 y §6.2 dicen que el correo se marca justo después de enviarlo y que puede repetirse | Corregido (7f2ae84) |
| C7-30 | Media | La §18 no nombra los mockups de las ventanas nuevas entre lo hecho con IA | Agregar «los mockups de las ventanas nuevas» (sin decir «capturas») | Pregunta para Mauricio |
| C7-31 | Baja | README, `DemoArquitectura` y ESTADO citaban páginas del reporte original | Citar por sección | Corregido (código a28386f; documentos con el mismo id) |
| C7-32 | Baja | La tabla 13 nombra `EnviosController` y el canal de alarmas, que no están entre las 195 clases | Señalarlos como infraestructura que no se dibuja | Corregido (df3154f) |
| C7-33 | Baja | La tabla 8 no cubría Proveedores ni Promociones sugeridas | «V11 a V15 y Proveedores»; fila «Analítica: BAM y promociones» | Corregido (bf049c0) |
| C7-34 | Baja | La tabla «Módulos» del README no nombraba las ventanas nuevas | Copiar las de la tabla 5 del reporte | Corregido (código a28386f) |
| C7-35 | Baja | README y LEEME dicen «aprobado» para el corte 7; PLAN y ESTADO, «esperando el OK» | Se resuelve con el OK de Mauricio al 7d (DEC-106) | Espera el OK |
| C7-36 | Baja | «inventario.stock-bajo» y «/api/ordenes-compra» se partían en el guion | `generar_reporte.py` une el guion de los nombres en minúscula (U+2060) | Corregido (b06ef8d; reporte 25d3573, 70 páginas) |
| C7-37 | Baja | El comentario de `PlatoRestController.consultar()` decía `?sucursal={id}` | `GET /api/platos (la sucursal sale del JWT)` | Corregido (código d933c7f) |

Preguntas que deja: C7-30 (la §18 y los mockups), si la §1 nombra la plataforma como tercer entregable (el revisor recomienda dejarlo, por DEC-57) y DEC-106.

## Pruebas

`probar_todo.sh` sobre una instancia limpia (`kg-limpio`):

| Corrida | Núcleo | JUnit | Recorridos y verificadores | Total | Resultado |
|---|---|---|---|---|---|
| Con DEC-100, antes de las correcciones (código 9aaf10b) | 399 | 86 | 750 | 1.235 | Sin fallas |
| Con todas las correcciones (código aa5af5b) | 400 | 91 | 763 | 1.254 | Sin fallas |
| Final del 7d, completa (con compilación; código d933c7f) | 400 | 91 | 763 | 1.254 | Sin fallas |

En la segunda fila, los JUnit son los de la corrida que compiló el mismo código (287db28); después solo cambió el compose (PostgreSQL con 200 conexiones, aa5af5b), y la corrida final usó `--sin-compilar`. Dos corridas intermedias fallaron: la primera, por dos pruebas que había que ajustar (`verificar_corte6b` contaba también las marcas de `NombresNegocio`, y una dirección fuera del menú lleva a la primera opción del rol, no a «La ventana no existe»; c96c8ff); la segunda no alcanzó a levantar porque, con ms-integracion usando su base, 11 servicios con 10 conexiones cada uno pasaban las 100 de PostgreSQL (aa5af5b).

Lo nuevo que comprueban: V08 llega con el producto desde la alarma (DEC-100), la fila de V05 con la cantidad y el correo con la sucursal (DEC-102), los textos de HU-06, HU-07 y PD-10 (C7-2, C7-3), las 11 outbox vacías, un `pedido.registrado` repetido que no cambia el inventario ni el tablero (C7-5), una escritura con Kafka detenido que responde enseguida y llega una sola vez a la bitácora cuando Kafka vuelve, y el 429 del gateway con sesión y sin ella, aunque se cambie `X-Forwarded-For` (C7-19).
