# Validación del corte 7 · plataforma completa

Validación del arquitecto revisor sobre la plataforma completa (cortes 1 a 7 de la rama `stack-real`), paso 7c del plan del corte 7 (DEC-95). Fecha: 2026-11-23. Revisó el código (`src/`, `servicios/`, `web/`, `plataforma/`), el `.drawio`, `DECISIONES.md` (DEC-57 a DEC-100), `CAMBIOS_A_DOCUMENTAR.md`, los casos de uso, el Story Mapping corregido, los mockups y el reporte v2.

**Veredicto: lista con correcciones.** La arquitectura y la plataforma coinciden en lo grueso: los 11 servicios, los 13 prefijos del gateway, los 10 tópicos más `notificaciones.dlq`, la hexagonal (el núcleo solo importa `java.*` y cada servicio lo copia al compilar, DEC-61), las 195 clases del diagrama, JWT RS256 validado en el gateway y en cada servicio, `@PreAuthorize` en todos los endpoints, PD-01, el claim check del PDF y ShedLock en los dos jobs. DEC-100 queda bien en lo esencial.

Mauricio decidió los cuatro hallazgos que lo pedían (DEC-101 a DEC-104). La columna «Estado» dice qué se hizo con cada uno.

## Hallazgos

| # | Sev. | Hallazgo | Corrección | Estado |
|---|---|---|---|---|
| C7-1 | Alta | Un jefe de cocina puede editar, desactivar, reactivar o cambiar la imagen de un plato de otra sucursal: `PlatoRestController` busca el plato solo por id | El adaptador comprueba que el plato sea de la sucursal del JWT y responde 404 si no; JUnit con un jefe de otra sucursal | Por aplicar |
| C7-2 | Alta | V11: los mensajes de lista vacía quedaron invertidos (regresión de DEC-97); la prueba solo buscaba «No se encontraron solicitudes» | Sin resultados de búsqueda: «No se encontraron solicitudes con los criterios indicados» (HU-07, 4); sin solicitudes: «No se encontraron solicitudes registradas» (HU-06, 5); la prueba compara el texto completo; CU-07 agrega el flujo alterno de HU-07 | Por aplicar |
| C7-3 | Media | El rango de fechas invertido de ms-solicitudes dice «La fecha «desde» es posterior a «hasta»» | «La fecha inicial no puede ser posterior a la fecha final.» (HU-07, 6; PD-10), con la prueba del texto completo | Por aplicar |
| C7-4 | Alta | `reactivar()` está en el código, la SPA y el mockup, pero no en el diagrama, en CU-03 ni en CAMBIOS | **DEC-101 (Mauricio):** se lleva a la 4b, a la página 1 y a CU-03, con un CD nuevo | Por aplicar |
| C7-5 | Media | ms-inventario no descarta un `pedido.registrado` repetido y el cierre descuenta dos veces | **DEC-104 (Mauricio):** todos los consumidores descartan los eventos repetidos | Por aplicar |
| C7-6 | Media | El diagrama describe outbox transaccional, consumidores idempotentes y límite de tasa en el gateway; la plataforma no los tiene, y la base `ms_integracion` no se usa | **DEC-104 (Mauricio):** se implementan los tres; ms-integracion usa su base para la outbox | Por aplicar |
| C7-7 | Media | HU-16 (4) pide producto, cantidad restante, unidad y sucursal; la fila de V05 solo muestra el nombre y el porcentaje, y el correo no nombra la sucursal | **DEC-102 (Mauricio):** la fila de V05 con la cantidad y la unidad, el correo con la sucursal y CU-05 precisa la alarma en vivo | Por aplicar |
| C7-8 | Media | `CAMBIOS_A_DOCUMENTAR.md`: seis CD apuntan a la «tabla 5» del reporte (solo prefijos) y no a la tabla 13 (§15); faltan rutas (productos por categoría, imagen del plato, reactivar, proveedores, reenvío, destinatarios, alta de usuarios) y un «No aplica» para los accesos de demostración de V01; no se explica el salto de CD-35 a CD-50 | Corregir el destino, agregar los CD que faltan y una línea sobre la numeración | Por aplicar |
| C7-9 | Baja | El cierre diario confirma el inventario y el consumo pendiente en transacciones separadas: si se cae entre las dos, repite el descuento | El job corre su cuerpo en una transacción, dentro del candado de ShedLock | Por aplicar |
| C7-10 | Baja | Página 3a: dos flechas con el id `c3a_e31d` | Renombrar una (`c3a_e31e`) | Por aplicar |
| C7-11 | Baja | Página 5: `v26` y `e47` no nombran las imágenes de los platos (ms-menu, DEC-67) | Agregarlas | Por aplicar |
| C7-12 | Baja | `ReprocesoColaErrores` no está en la nota de infraestructura de ms-notificaciones (4d) | Nombrarla en `n_infra_notif` | Por aplicar |
| C7-13 | Media | CU-05 cita «el código del 8 de noviembre» (contra DEC-57) | Texto nuevo (abajo) | Por aplicar |
| C7-14 | Media | Los casos de uso mencionan IA fuera de la §18 («aprobada por el arquitecto-revisor de Claude»); la línea 3 dice «versión corregida del 8 de noviembre» | «Estado: propuesta de diseño, sin estimar (PD-09); no forma parte del Story Mapping original.» y la fecha de la versión final | Por aplicar |
| C7-15 | Baja | DEC-100: si el nombre del producto no está en el catálogo y el jefe no tiene solicitudes, V08 precarga la solicitud de ejemplo | No precargar cuando viene `nombreProducto` | Por aplicar |
| C7-16 | Baja | Con una URL desconocida, la SPA muestra «se construye en el corte undefined»; `pantallas.ts` aún dice «sin mockup» | «La ventana no existe» con un enlace a Inicio; quitar los datos viejos de `pantallas.ts` | Por aplicar |
| C7-17 | Baja | V11 pagina cada 6 filas; HU-06 (4) y CU-07 dicen 25 | **DEC-103 (Mauricio):** páginas de 25 | Por aplicar |
| C7-18 | Baja | `README.md` dice 398 verificaciones (son 399); `ESTADO_PROYECTO_Y_C4.md` dice «falta el corte 7» y D5 «Sin mockup» | Se corrigen en el 7d | Para el 7d |

Sin hallazgo: las claves de las bases y de S3 fijas en `application.yml` y en el compose (se aceptan en local) y `/plataforma/registro` y `/plataforma/rutas` sin token (solo en local).

## CU-05: texto propuesto (C7-13)

> **Reglas y fuente:** el proceso no debe afectar el desempeño en horario de operación. El 25 % es un parámetro propio de ms-inventario (`UmbralStock`); no se cambia desde la parametrización (DEC-43). El modelo no tiene una entidad Alerta: la alerta es la marca `alertaAtendida` de `InsumoInventario` (página 4b, nota `n_alerta`). `alertas()` devuelve los insumos bajo el umbral con la marca sin atender, `solicitud.registrada` la atiende y `descontar()` la reactiva cuando el insumo vuelve a cruzar el umbral.

## CD pendientes

- Para el 7d (reporte): CD-02, 08, 10, 12, 14, 15, 23, 33, 34, 50, 55 y 60. Ninguno quedó pendiente fuera del reporte.
- Nuevos por esta revisión: `reactivar()` (C7-4), las rutas de C7-8, el «No aplica» de los accesos de V01 y lo que cambien DEC-102 y DEC-104.

## Reglas de la entrega

- IA: el reporte la menciona solo en la §18; la otra mención estaba en los casos de uso (C7-14).
- Ningún texto dice que los mockups de `mockups_faltantes/` son capturas.
- Límites de la demostración: la única violación es la de CU-05 (C7-13).

## Pruebas

Antes de las correcciones, con DEC-100 aplicada, `probar_todo.sh` sobre una instancia limpia: ver la sección «Corridas» al final.
