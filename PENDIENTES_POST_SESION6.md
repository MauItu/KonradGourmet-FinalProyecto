# Pendientes después de la sesión 6 · Plan de trabajo

Distribución indicada por Mauricio después del cierre de la sesión 6. Este documento organiza el trabajo por realizar; no marca como terminados los entregables ni las decisiones que siguen pendientes.

Base: sesión 6 publicada en `MauItu/KonradGourmet-FinalProyecto`, `main` `b90a108`; código local `22717f7`, con 176/176 verificaciones y compilación sin avisos. Los números 1 a 6 corresponden a la lista de pendientes presentada a Mauricio.

| Punto | Instrucción de Mauricio | Responsable | Estado |
|---|---|---|---|
| 1 · Documentos de entrega | Hacerlo aquí | Asistente y Mauricio, con los insumos del equipo | Por realizar |
| 2 · Mockups | Dejarlos pendientes; Mauricio los hace | Mauricio | Pendiente externo |
| 3 · Story Mapping | Crear un documento nuevo con todo corregido | Asistente, aquí | Por realizar; conservar el original |
| 4 · Preguntas de arquitectura y requisitos | Que decida el agente de Claude de arquitectura de software | `arquitecto-revisor` de Claude | Hecho el 2026-11-08, falta el OK de Mauricio: PD-01 a PD-11 (`REVISION_ARQUITECTO_POST_S6.md`); quedan A1, A3, A5, A6 y A7, que son factuales |
| 5 · Supuestos asumidos | Que decida el mismo agente | `arquitecto-revisor` de Claude | Hecho el 2026-11-08, falta el OK de Mauricio: los siete supuestos confirmados (tres con ajuste) |
| 6 · Implementación | Por ahora, solo la demo; explicar qué contiene | Asistente | Alcance fijado por Mauricio; descripción abajo |

## Reglas para continuar

- Trabajar en `correcciones-revision` de `documentos/` y `KonradGourmet/`, con un commit por corrección y su id al inicio.
- Conservar los originales del `.drawio`, reporte PDF, mockups y Story Mapping. El nuevo documento del punto 3 no sustituye el original sin indicación de Mauricio.
- El perfil del agente solicitado está en `../.claude/agents/arquitecto-revisor.md`. La revisión de los puntos 4 y 5 debe quedar atribuida a quien realmente la ejecute; no presentar una revisión de Codex como una ejecución de Claude.
- La delegación autoriza al arquitecto a escoger entre alternativas de diseño y dejar su justificación. No autoriza a inventar hechos del equipo, como quién posee un archivo o una cuenta.
- Registrar las decisiones del arquitecto en `DECISIONES.md`, distinguir nuevas decisiones de confirmaciones y señalar sus efectos en documentos, diagramas y código. No cambiar silenciosamente una decisión ya confirmada por Mauricio.
- Mantener el alcance de demo del punto 6. Una recomendación de arquitectura no abre automáticamente la implementación de los 94 cuerpos pendientes.
- El push de la sesión 6 ya está realizado. La publicación de los nuevos entregables se registra aparte cuando corresponda.

## 1. Documentos de entrega: hacer aquí

Fuentes: [CORRECCIONES_REPORTE.md](CORRECCIONES_REPORTE.md), [VALIDACION_SESION6.md](VALIDACION_SESION6.md) y [Casos de uso — Konrad Gourmet.md](Casos%20de%20uso%20%E2%80%94%20Konrad%20Gourmet.md).

- [ ] Obtener el diagrama UML de Carlos, o preparar aquí una versión nueva a partir de los casos corregidos si no está disponible, dejando explícito su origen. Revisarlo contra CU-01 a CU-19, los 6 roles y los 5 actores externos.
- [ ] Exportar el resultado a `documentos/casos_de_uso.png`, comprobar que se vea al abrir el Markdown y retirar la nota de ausencia solo cuando el archivo exista y esté validado.
- [ ] Localizar el script de ReportLab del reporte (A1). Si no aparece, definir la reconstrucción en un archivo nuevo conservando el PDF original, conforme al límite vigente de DEC-03.
- [ ] Aplicar al reporte las instrucciones pertinentes R-01 a R-61: distinguir «Cambiar», «Ya es cierto», «Opcional» y «Por confirmar». No tratar las 61 entradas como 61 cambios obligatorios.
- [ ] Sustituir las figuras antiguas e incorporar las páginas 3a, 3b y 6 según la resolución de DEC-48. Usar la 4b actual de `exportes/sesion6/4b.png`, junto con las otras páginas vigentes del `.drawio`.
- [ ] Reflejar 191 clases del modelo, 20 archivos de demo, 176 verificaciones, `meseroId` y los límites reales del prototipo.
- [ ] Revisar texto, figuras, referencias y coherencia con código, casos de uso y el nuevo documento del punto 3. Cuando Mauricio termine los mockups, contrastar también sus archivos finales.

**Criterio de terminado:** entregables nuevos o regenerados legibles, sin marcadores de imágenes ausentes, con fuentes y decisiones trazables; originales preservados y validación registrada.

## 2. Mockups: pendientes a cargo de Mauricio

No hacer estos cambios desde aquí. La especificación detallada está en [CORRECCIONES_HU_Y_MOCKUPS.md](CORRECCIONES_HU_Y_MOCKUPS.md).

| Id | Trabajo que queda para Mauricio | Prioridad |
|---|---|---|
| M-01 | V13: seleccionar una o varias solicitudes y resumir sus productos y cantidades | Obligatoria |
| M-02 | V21: mostrar «Solicitudes» con la lista de números | Obligatoria |
| M-03 | V17 y V19–V23: datos acordados de cotizaciones y orden; COT-018, Proveedor ABC, OC-0012 por $545.000, estados, números y fechas coherentes | Obligatoria; DEC-49 |
| M-04 | Resolver V03/V04: recomendación quitar la limonada de V03 para mantener tres platos y $82.000. La alternativa de $88.000 cambia también demo y reporte | Obligatoria; elección de Mauricio |
| M-06, V05 | Sacar Arroz y Pollo al 25 % de las alertas y dejar «3 Stock bajo» | Obligatoria; DEC-50 |
| M-05 | V18/V19: separar precio unitario y valor de la línea | Opcional |
| M-06, V07 | Hacer coincidir contador y filas; res 11,5 kg de 50 kg (23 %) | Opcional |
| M-07 | Mostrar las solicitudes que comparten cotización en el detalle | Opcional |
| M-08 | Diseñar pantallas de proveedores y promociones (HU-15 y HU-19) | Opcional |
| M-09 | Corregir el saludo de V03 para que corresponda al mesero | Registrada como opcional |
| Nombre del director | Revisar Laura Gómez en la demo frente a Isabella Castro en V16–V23; no se cambia mientras Mauricio no lo resuelva | Pendiente de Mauricio |

- [ ] Recibir los mockups corregidos de Mauricio y verificar su coherencia con los demás entregables.

## 3. Crear un documento nuevo con el Story Mapping corregido

**Archivo de trabajo previsto:** `documentos/STORY_MAPPING_CORREGIDO.md`. Debe contener la versión corregida, no solamente una lista de cambios. El PDF original se conserva intacto.

- [ ] Leer y transcribir las 19 HU del original, incluidos sus criterios de aceptación, actores y MVP; las tarjetas son imágenes. Mantener lo que no requiere corrección y dejar trazabilidad a la tarjeta original.
- [ ] Reescribir HU-07 con filtros desde/hasta, producto, marca y estado (S-01); distinguir el criterio propuesto de validar el orden de las fechas de los criterios ya establecidos.
- [ ] Mantener HU-09 con «una o varias solicitudes» (S-02: no requiere cambiar su regla).
- [ ] Alinear HU-16 con la regla aprobada: porcentaje redondeado estrictamente menor que 25 % y alarma al cruzar el umbral.
- [ ] Mostrar capacidad de 224 horas por sprint, no 120 (S-03).
- [ ] Corregir cartas del Planning Poker: 25/50/75/125 horas, equivalentes a 1/2/3/5 PHU; HU-14 = 1 PHU, total 35 PHU (S-04).
- [ ] Ubicar «Ver KPIs» bajo el director comercial en actores, actividades y MVP (S-05).
- [ ] Incluir la planificación completa del Excel: 5 sprints, 10 semanas, 2 meses y medio, del 03/08 al 11/10/2026; 8, 8, 6, 8 y 5 PHU por sprint, con 10 PHU de holgura (S-06).
- [ ] Eliminar referencias contradictorias a 4 sprints/8 semanas y la nota «Un detalle para tu sustentación…» (S-07).
- [ ] Incluir la correspondencia de historias y ventanas; S-08 es informativa.
- [ ] Mantener HU-20 a HU-22 separadas como propuestas hasta que el arquitecto resuelva su tratamiento. No convertirlas en historias aprobadas del Story Mapping original ni asignarles estimaciones inventadas.
- [ ] Revisar el documento nuevo contra casos de uso, decisiones y Excel; registrar cualquier dato pendiente de una fuente o decisión.

**Criterio de terminado:** un documento nuevo que se pueda leer de principio a fin como versión corregida de las historias y la planificación, con la procedencia y las propuestas claramente identificadas.

## 4. Decisiones de arquitectura y requisitos: agente de Claude

Encargar esta revisión al `arquitecto-revisor` de Claude, con la autorización de Mauricio para escoger las alternativas de diseño. Pedir evidencia, decisión, justificación, consecuencias y pendientes de información.

| Tema | Resolución que se le solicita |
|---|---|
| A2 | Cómo entregar alarmas WebSocket con varias réplicas de notificaciones |
| DEC-20 | Reparto de Eureka y SPA entre zonas; justificar el supuesto de A/B |
| DEC-48 | Ubicación de las páginas 3a, 3b y 6 dentro del reporte |
| Categorías de proveedor | Mantener `List<String>` o usar referencias a categorías |
| BAM | Relación entre periodos fijos de indicadores y filtros libres por fechas |
| Promociones | Fuente de tendencias y periodo de análisis; declarar como supuesto lo que no tenga respaldo |
| Administración | Quién administra roles y permisos y con qué alcance |
| Auditoría | Si incluir filtros por usuario y acción, además de fechas |
| SOAP externo | Cómo autenticar consumidores externos |
| HU-20 a HU-22 | Aprobar como propuesta de diseño, ajustar o descartar; indicar cómo incorporar su estimación por el equipo |
| Mensajes de interfaz | Completar los mensajes aún «por definir» con criterios coherentes |

Los siguientes puntos requieren información factual, no una elección de arquitectura:

- **A1:** averiguar quién tiene el script del reporte; el arquitecto puede recomendar cómo reconstruirlo, pero no atribuir su posesión sin evidencia.
- **A3:** identificar al titular de `mari00aa120-eng`; debe confirmarlo alguien del equipo.
- **Enunciado original:** localizarlo si existe. Sin él, citar las fuentes locales y marcar los requisitos cuya literalidad no pudo verificarse.

- [x] Ejecutar la revisión con el agente solicitado y conservar su informe: la ejecutó el `arquitecto-revisor` de Claude (Claude Opus 5.5) el 2026-11-08, y su informe está en `REVISION_ARQUITECTO_POST_S6.md`.
- [x] Registrar sus resoluciones y las preguntas factuales que continúen abiertas:
  - PD-01 a PD-11 están en `DECISIONES.md`;
  - sus efectos están en el `.drawio` (páginas 1, 2, 3a, 4d, 4e y 5; PNG en `exportes/post_sesion6/`), en los casos de uso, en `CORRECCIONES_REPORTE.md` (R-31, R-49, R-56 a R-58, R-61 y las nuevas R-62 a R-66), en `ESTADO_PROYECTO_Y_C4.md` y en el README del código (N5);
  - A2 queda cerrada por PD-01;
  - A1 queda en manos de Mauricio, y si el script no aparece se rehace el reporte (DEC-54); A3 está descartada; A5 (enunciado) queda para revisar, con prioridad baja; A7 (estimación de HU-20 a HU-22) sigue abierta;
  - PD-11 (ampliar la declaración de IA) la descartó Mauricio, y R-58 queda sin cambio.
- [ ] PD-05 (T-06) está decidida, pero su aplicación (4c, 4e y código) espera la autorización de Mauricio.

## 5. Supuestos: agente de Claude

| Decisión | Qué debe revisar o confirmar |
|---|---|
| DEC-08 | Recomendaciones asumidas del revisor; resolver considerando las decisiones posteriores, sin restaurar cifras o reglas superadas |
| DEC-39 | Alcance de la lógica implementada. La instrucción actual de Mauricio fija **solo la demo**; registrar esa prioridad |
| DEC-40 | Inventario inicial e histórico manual del BAM; no reabrir los precios unitarios y el pedido de $82.000 ya confirmados |
| DEC-41 | Constructores de copia y accesores de listas modificables |
| DEC-44 | Pruebas fuera de `src/`, sin JUnit, con `ejecutar.sh` |
| DEC-45 | Separar las 191 clases del modelo de los 20 archivos de demo |
| DEC-47 | Orden de filas: F17 antes de F16. Fechas, cinco sprints y diez semanas ya están decididos |

- [x] Pedir al mismo agente que resuelva estos supuestos junto con el punto 4.
- [x] Registrar qué queda confirmado, qué cambia y por qué, con un commit por corrección:
  - confirmados: DEC-39, DEC-40, DEC-45 y DEC-47 (orden de filas);
  - confirmados con ajuste: DEC-08 (D2 reemplazada por DEC-12), DEC-41 (listas modificables como simplificación declarada) y DEC-44 (176 verificaciones en 6 archivos);
  - DEC-20 y DEC-48 se resolvieron con el punto 4.

## 6. Alcance actual: solo la demo

Mauricio indicó mantener por ahora la demo. No se abre la implementación del sistema completo, ni de los 94 cuerpos pendientes. La siguiente descripción se contrastó con `DemoArquitectura.java`, el README y la ejecución de cierre de la sesión 6 sobre código `22717f7`.

### Qué ejecuta

Es una **demo de consola en Java 21**, que recorre una compra en la sucursal Centro con lógica de dominio y adaptadores en memoria.

| Paso | Lo que muestra |
|---|---|
| Carga inicial | Una mesa, cuatro insumos, dos platos con recetas, cuatro proveedores y un jefe en la libreta de correos. Los eventos de menú alimentan catálogo y recetas |
| Menú del director | Árbol de opciones de DIRECTOR_COMPRAS: Inicio, Cotizaciones, Órdenes, Mi perfil y Cerrar sesión. Demuestra Composite y Factory Method |
| Pedido | PED-0251, mesa 5, mesero 3, dos bandejas paisas y un ajiaco, total $82.000. Publica el evento y acumula el consumo de ingredientes |
| Cierre diario | Invoca el cierre que representa las 23:59. La res queda en 11,5 kg de 50 kg (23 %), genera alarma y correo simulados |
| Precio del Ministerio | Dos consultas de res a $20.000/kg: la primera llama al servicio simulado y la segunda usa la caché del Proxy |
| Solicitud | SOL-031, con res 20 kg y arroz 15 kg; queda PENDIENTE, registra confirmación y atiende la alerta de stock |
| Cotizaciones | COT-018: $545.000 frente a $520.000, +4,81 %, OPCIONADA. COT-019: $700.000, +34,62 %, RECHAZADA. COT-020: $200.000, −61,54 %, SOSPECHOSA |
| Cambios por eventos | El envío de cotización actualiza la solicitud a EN TRÁMITE cuando todos sus productos están cotizados y registra los avisos correspondientes |
| Orden | OC-0012 sobre COT-018, por $545.000. Cotización a CON ORDEN y solicitud a COMPLETADA. Rechaza una orden repetida y una orden sobre una cotización rechazada |
| Parámetros | Tema y color corporativo mediante Singleton; verifica que dos accesos obtengan la misma instancia |
| Indicadores BAM | Bandeja paisa: 40; sucursal Centro: 120; proveedor B: −2,40 % frente al estándar. Se calculan sobre un histórico precargado manualmente |
| Auditoría y resumen | Muestra eventos, mensajes y registros. La ejecución de referencia termina con 7 correos simulados, 1 alarma y 41 registros de auditoría |

### Qué comprueban las pruebas

`./ejecutar.sh` compila, ejecuta la demo y corre **176 verificaciones**. Estas cubren los patrones, límites y redondeo de precios/stock, mesero del pedido, flujo de compra, varias solicitudes, proveedores, orden repetida y recuperación tras fallos simulados del Ministerio o del almacén de PDF. Estos escenarios adicionales de prueba no son todos pasos impresos por el recorrido principal.

### Qué simula o no implementa

- No hay aplicación web operativa integrada con esta demo. El menú se imprime por consola y las llamadas a controladores son llamadas Java, no un servidor HTTP desplegado.
- No usa base de datos, Kafka, SMTP, WebSocket, almacenamiento de objetos ni servicio real del Ministerio. Los adaptadores de demo sustituyen esas tecnologías.
- Generación/almacenamiento de documentos, adjuntos y entrega de correos se representan con adaptadores de demostración; las cabeceras de certificado y sello de tiempo no equivalen a un servicio externo de correo certificado.
- No hay login ni JWT real. El mesero se entrega explícitamente al constructor; la auditoría usa `sistema` o el correo de la entidad y menciona al mesero en la acción del pedido.
- Los KPI parten de hechos precargados; el listener que los alimentaría automáticamente sigue pendiente. Tampoco está implementado el motor de promociones.
- Los datos se pierden al terminar. La correlación no une todo el recorrido y no existe descarte general de eventos repetidos.
- El filtro de pedidos distingue meseros, pero no delimita turnos reales. La validación de mesa usa el objeto recibido. El número PED-0251 se carga explícitamente; no se demuestra una regla para incluir la mesa en el consecutivo.
- Alta de usuarios, gestión completa de platos, otros menús por rol y adaptadores externos siguen fuera de este alcance.

Para ejecutarla desde `FinalProyecto/`:

```bash
cd KonradGourmet
./ejecutar.sh
```

Para ejecutar solo la demo: `./ejecutar.sh demo`. En el repositorio público, la carpeta equivalente es `codigo/`.

## Orden de trabajo previsto

1. Obtener las resoluciones del arquitecto de Claude (puntos 4 y 5), porque afectan al reporte y a las propuestas de historias.
2. Crear el documento nuevo del Story Mapping (punto 3), avanzando desde las fuentes y decisiones ya confirmadas.
3. Completar aquí el diagrama de casos de uso y el reporte (punto 1), conservando los originales.
4. Mauricio corrige los mockups (punto 2) en paralelo.
5. Revisar los entregables juntos y preparar su publicación, manteniendo el alcance de demo del punto 6.
