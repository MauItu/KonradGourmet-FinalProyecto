# Inventario previo de la sesión 3 · código frente a los diagramas de clases 4a–4e

Hecho por el agente `arquitecto-revisor` el 2026-11-08, solo con lectura. Compara clase por clase, por nombre completo (paquete + clase), el código de `KonradGourmet/` (rama `correcciones-revision`, HEAD `ad71e69`) con las páginas 4a–4e de `documentos/Arquitectura_Konrad_Gourmet.drawio` (rama `correcciones-revision`, HEAD `20a030c`; ids `p3`–`p7`).

**Regla de la sesión:** el código queda idéntico a 4a–4e. Donde no está claro qué está bien, hay una duda Q-xx para Mauricio.

**Ids de este documento:** C (clases), M (miembros), R (relaciones), T (tipos), W (§15 del reporte frente a 4a–4e), P (patrones y README), J (detalles de Java que el diagrama no fija) y Q (dudas para decidir).

## 0. Cifras

| Concepto | Cifra |
|---|---|
| Clases en 4a–4e | **191**: 134 clases, 45 «interface», 6 «abstract» y 6 «enumeration» (4a 28 · 4b 39 · 4c 40 · 4d 40 · 4e 44) |
| Clases en el código | 156 = 155 + `demo/DemoArquitectura`. Todas solo con la declaración; compila con `javac --release 21` |
| Coinciden por nombre completo | 151 |
| Faltan por nombre completo | **40** = 36 nuevas (C-01 a C-36) + 4 que hoy están en otro paquete (C-37 a C-40) |
| Sobran por nombre completo | 5 = esas 4 en su paquete viejo + la demo, que se conserva (C-45) |
| Tipo de clase distinto (H8) | **5**: `Correo` (C-37) y C-41 a C-44 |
| Miembros por declarar | **714** = 288 atributos + 403 métodos + 23 valores de enumeración. Hoy el código no tiene ninguno |
| Métodos con ‹Vxx› | 24 métodos con 29 marcas (5 métodos atienden dos ventanas); cubren las 23 ventanas |
| Relaciones `implements`/`extends` esperadas | **82** = 65 flechas (49 realizaciones + 16 herencias) + **17** escritas dentro de las cajas |
| … ya bien en el código | **19** |
| … bien escritas, pero en el paquete viejo | 2 (se arreglan al mover los decoradores) |
| … al revés en el código | 8 (`implements` donde va `extends`, o al contrario) |
| … que faltan del todo | **53** |
| Relaciones que hoy tiene el código | 29 = 19 correctas + 10 que cambian |

**Dos cifras del plan cambian:**
- **17 relaciones escritas, no 18.** Son 9 «extends EventoDominio» (los eventos de 4b–4e) y 8 «implements EventListener<EventoDominio>» (los listeners). La 18.ª que se contó era la cota genérica de `EventListener<T extends EventoDominio>` (`cls_compartido_EventListener`, 4a), que no es una relación entre clases. `AuditoriaEvent` hereda por flecha (`e7`), no por texto.
- **36 clases nuevas, pero 40 que faltan por nombre completo.** Las 4 de diferencia son `Correo` y los 3 decoradores, que existen en `notificaciones.dominio` y en 4d están en otro paquete.

**Prueba de que 4a–4e es Java compilable tal cual.** Generé en un temporal un esqueleto con las 191 clases exactamente como están en 4a–4e: firmas, `extends`/`implements`, `static`, `abstract`, el genérico de `EventListener` e imports. Compila con `javac --release 21 -Xlint:all` sin errores ni avisos (192 `.class` con la demo). No hay `List` ni `Map` sin parámetro, ni tipos que no existan, ni `Money`. Está en `/tmp/tmp.EtxiAb77TO/gen/` por si sirve de referencia: **no es la entrega**, porque no tiene Javadoc, ni comentarios ‹Vxx›, ni constructores.

**Sobre el extractor `modelo.py` (scratchpad).** Coincide con mi lectura del XML en las 191 clases y en el texto de todos los atributos y métodos. Tiene cuatro limitaciones:
- pierde la cursiva y el subrayado de cada línea, así que no distingue los 7 métodos abstractos ni los 4 miembros estáticos (J-07 y J-08);
- mete en `aristas` las 35 flechas de la leyenda (`ley_l0` a `ley_l6` en cada página). Si se cuentan, salen 54 realizaciones y 21 herencias (75) en vez de 49 y 16 (65);
- clasifica los conectores de nota como `otra(none,1,none,d)`. Es inofensivo;
- deja cada miembro como texto: no separa nombre, parámetros y retorno, ni las restricciones (`{único}`, `= 8`).

## 1. Clases (C)

### 1.1 Faltan en el código: 36 clases nuevas

Todos los paquetes ya existen como carpetas en `KonradGourmet/src/`.

| Id | Clase (nombre completo, sin `co.edu.konradlorenz.`) | Pág. | Celda | Tipo | Archivo a crear | Viene de |
|---|---|---|---|---|---|---|
| C-01 | `identidad.adaptadores.entrada.UsuarioRestController` | 4a | `cls_identidad_UsuarioRestController` | clase | `KonradGourmet/src/co/edu/konradlorenz/identidad/adaptadores/entrada/UsuarioRestController.java` | DEC-10 |
| C-02 | `identidad.aplicacion.RegistrarUsuarioService` | 4a | `cls_identidad_RegistrarUsuarioService` | clase | `KonradGourmet/src/co/edu/konradlorenz/identidad/aplicacion/RegistrarUsuarioService.java` | DEC-10 |
| C-03 | `identidad.dominio.PoliticaContrasena` | 4a | `cls_identidad_PoliticaContrasena` | clase | `KonradGourmet/src/co/edu/konradlorenz/identidad/dominio/PoliticaContrasena.java` | DEC-10 (H15) |
| C-04 | `identidad.puertos.entrada.RegistrarUsuarioUseCase` | 4a | `cls_identidad_RegistrarUsuarioUseCase` | «interface» | `KonradGourmet/src/co/edu/konradlorenz/identidad/puertos/entrada/RegistrarUsuarioUseCase.java` | DEC-10 |
| C-05 | `inventario.adaptadores.entrada.InventarioEventsListener` | 4b | `cls_inventario_InventarioEventsListener` | clase | `KonradGourmet/src/co/edu/konradlorenz/inventario/adaptadores/entrada/InventarioEventsListener.java` | H2 / D4 |
| C-06 | `inventario.dominio.RecetaPlato` | 4b | `cls_inventario_RecetaPlato` | clase | `KonradGourmet/src/co/edu/konradlorenz/inventario/dominio/RecetaPlato.java` | H2 / N2 |
| C-07 | `pedidos.adaptadores.salida.CatalogoJpaAdapter` | 4b | `cls_pedidos_CatalogoJpaAdapter` | clase | `KonradGourmet/src/co/edu/konradlorenz/pedidos/adaptadores/salida/CatalogoJpaAdapter.java` | H3 |
| C-08 | `pedidos.dominio.Mesa` | 4b | `cls_pedidos_Mesa` | clase | `KonradGourmet/src/co/edu/konradlorenz/pedidos/dominio/Mesa.java` | H6 |
| C-09 | `pedidos.dominio.PlatoCatalogo` | 4b | `cls_pedidos_PlatoCatalogo` | clase | `KonradGourmet/src/co/edu/konradlorenz/pedidos/dominio/PlatoCatalogo.java` | H3 / N1 |
| C-10 | `pedidos.puertos.salida.CatalogoRepositoryPort` | 4b | `cls_pedidos_CatalogoRepositoryPort` | «interface» | `KonradGourmet/src/co/edu/konradlorenz/pedidos/puertos/salida/CatalogoRepositoryPort.java` | H3 |
| C-11 | `cotizaciones.adaptadores.entrada.OrdenGeneradaListener` | 4c | `cls_cotizaciones_OrdenGeneradaListener` | clase | `KonradGourmet/src/co/edu/konradlorenz/cotizaciones/adaptadores/entrada/OrdenGeneradaListener.java` | D3 |
| C-12 | `cotizaciones.adaptadores.entrada.ProveedorRestController` | 4c | `cls_cotizaciones_ProveedorRestController` | clase | `KonradGourmet/src/co/edu/konradlorenz/cotizaciones/adaptadores/entrada/ProveedorRestController.java` | DEC-07 (D7) |
| C-13 | `cotizaciones.adaptadores.salida.AlmacenObjetosAdapter` | 4c | `cls_cotizaciones_AlmacenObjetosAdapter` | clase | `KonradGourmet/src/co/edu/konradlorenz/cotizaciones/adaptadores/salida/AlmacenObjetosAdapter.java` | D1 |
| C-14 | `cotizaciones.adaptadores.salida.ProveedorJpaAdapter` | 4c | `cls_cotizaciones_ProveedorJpaAdapter` | clase | `KonradGourmet/src/co/edu/konradlorenz/cotizaciones/adaptadores/salida/ProveedorJpaAdapter.java` | DEC-07 |
| C-15 | `cotizaciones.aplicacion.ProveedorService` | 4c | `cls_cotizaciones_ProveedorService` | clase | `KonradGourmet/src/co/edu/konradlorenz/cotizaciones/aplicacion/ProveedorService.java` | DEC-07 |
| C-16 | `cotizaciones.puertos.entrada.GestionarProveedorUseCase` | 4c | `cls_cotizaciones_GestionarProveedorUseCase` | «interface» | `KonradGourmet/src/co/edu/konradlorenz/cotizaciones/puertos/entrada/GestionarProveedorUseCase.java` | DEC-07 |
| C-17 | `cotizaciones.puertos.salida.AlmacenArchivosPort` | 4c | `cls_cotizaciones_AlmacenArchivosPort` | «interface» | `KonradGourmet/src/co/edu/konradlorenz/cotizaciones/puertos/salida/AlmacenArchivosPort.java` | D1 |
| C-18 | `cotizaciones.puertos.salida.ProveedorRepositoryPort` | 4c | `cls_cotizaciones_ProveedorRepositoryPort` | «interface» | `KonradGourmet/src/co/edu/konradlorenz/cotizaciones/puertos/salida/ProveedorRepositoryPort.java` | DEC-07 / N14 |
| C-19 | `notificaciones.adaptadores.entrada.DestinatarioRestController` | 4d | `cls_notificaciones_DestinatarioRestController` | clase | `KonradGourmet/src/co/edu/konradlorenz/notificaciones/adaptadores/entrada/DestinatarioRestController.java` | DEC-13 (N5) |
| C-20 | `notificaciones.adaptadores.salida.AlmacenObjetosAdapter` | 4d | `cls_notificaciones_AlmacenObjetosAdapter` | clase | `KonradGourmet/src/co/edu/konradlorenz/notificaciones/adaptadores/salida/AlmacenObjetosAdapter.java` | N4 |
| C-21 | `notificaciones.adaptadores.salida.DestinatarioJpaAdapter` | 4d | `cls_notificaciones_DestinatarioJpaAdapter` | clase | `KonradGourmet/src/co/edu/konradlorenz/notificaciones/adaptadores/salida/DestinatarioJpaAdapter.java` | DEC-13 |
| C-22 | `notificaciones.puertos.salida.AlmacenArchivosPort` | 4d | `cls_notificaciones_AlmacenArchivosPort` | «interface» | `KonradGourmet/src/co/edu/konradlorenz/notificaciones/puertos/salida/AlmacenArchivosPort.java` | N4 |
| C-23 | `notificaciones.puertos.salida.DestinatarioRepositoryPort` | 4d | `cls_notificaciones_DestinatarioRepositoryPort` | «interface» | `KonradGourmet/src/co/edu/konradlorenz/notificaciones/puertos/salida/DestinatarioRepositoryPort.java` | DEC-13 |
| C-24 | `ordenes.adaptadores.salida.AlmacenObjetosAdapter` | 4d | `cls_ordenes_AlmacenObjetosAdapter` | clase | `KonradGourmet/src/co/edu/konradlorenz/ordenes/adaptadores/salida/AlmacenObjetosAdapter.java` | H4 |
| C-25 | `ordenes.adaptadores.salida.PdfOrdenAdapter` | 4d | `cls_ordenes_PdfOrdenAdapter` | clase | `KonradGourmet/src/co/edu/konradlorenz/ordenes/adaptadores/salida/PdfOrdenAdapter.java` | H4 |
| C-26 | `ordenes.puertos.salida.AlmacenArchivosPort` | 4d | `cls_ordenes_AlmacenArchivosPort` | «interface» | `KonradGourmet/src/co/edu/konradlorenz/ordenes/puertos/salida/AlmacenArchivosPort.java` | H4 |
| C-27 | `ordenes.puertos.salida.GeneradorPdfPort` | 4d | `cls_ordenes_GeneradorPdfPort` | «interface» | `KonradGourmet/src/co/edu/konradlorenz/ordenes/puertos/salida/GeneradorPdfPort.java` | H4 |
| C-28 | `administracion.adaptadores.salida.ElasticsearchLogsAdapter` | 4e | `cls_administracion_ElasticsearchLogsAdapter` | clase | `KonradGourmet/src/co/edu/konradlorenz/administracion/adaptadores/salida/ElasticsearchLogsAdapter.java` | H14 |
| C-29 | `administracion.adaptadores.salida.ParametrosJpaAdapter` | 4e | `cls_administracion_ParametrosJpaAdapter` | clase | `KonradGourmet/src/co/edu/konradlorenz/administracion/adaptadores/salida/ParametrosJpaAdapter.java` | N7 |
| C-30 | `administracion.puertos.salida.LogsPort` | 4e | `cls_administracion_LogsPort` | «interface» | `KonradGourmet/src/co/edu/konradlorenz/administracion/puertos/salida/LogsPort.java` | H14 |
| C-31 | `administracion.puertos.salida.ParametrosRepositoryPort` | 4e | `cls_administracion_ParametrosRepositoryPort` | «interface» | `KonradGourmet/src/co/edu/konradlorenz/administracion/puertos/salida/ParametrosRepositoryPort.java` | N7 |
| C-32 | `analitica.adaptadores.entrada.PromocionRestController` | 4e | `cls_analitica_PromocionRestController` | clase | `KonradGourmet/src/co/edu/konradlorenz/analitica/adaptadores/entrada/PromocionRestController.java` | DEC-07 (D8) |
| C-33 | `analitica.adaptadores.salida.SugerenciaJpaAdapter` | 4e | `cls_analitica_SugerenciaJpaAdapter` | clase | `KonradGourmet/src/co/edu/konradlorenz/analitica/adaptadores/salida/SugerenciaJpaAdapter.java` | DEC-07 (D8) |
| C-34 | `analitica.dominio.EstadoSugerencia` | 4e | `cls_analitica_EstadoSugerencia` | «enumeration» | `KonradGourmet/src/co/edu/konradlorenz/analitica/dominio/EstadoSugerencia.java` | DEC-07 (D8) |
| C-35 | `analitica.dominio.SugerenciaPromocion` | 4e | `cls_analitica_SugerenciaPromocion` | clase | `KonradGourmet/src/co/edu/konradlorenz/analitica/dominio/SugerenciaPromocion.java` | DEC-07 (D8) |
| C-36 | `analitica.puertos.salida.SugerenciaRepositoryPort` | 4e | `cls_analitica_SugerenciaRepositoryPort` | «interface» | `KonradGourmet/src/co/edu/konradlorenz/analitica/puertos/salida/SugerenciaRepositoryPort.java` | DEC-07 (D8) |

Los tres nombres repetidos son clases distintas y cada una va en su servicio. Ninguna se comparte:
- `AlmacenArchivosPort`: la de cotizaciones y la de ordenes tienen `guardar()` y `obtener()`; la de notificaciones solo `obtener()`.
- `AlmacenObjetosAdapter`: una por servicio (cotizaciones, ordenes y notificaciones).
- `GeneradorPdfPort`: la de cotizaciones ya existe (`generar(c: Cotizacion)`); la de ordenes es nueva (`generar(o: OrdenCompra)`).

### 1.2 En otro paquete: mover 4 clases

Con `git mv` para conservar la historia: un commit H25 para `Correo` y uno N8 para los tres decoradores.

| Id | Hoy | Debe quedar | Pág. | Celda | Nota |
|---|---|---|---|---|---|
| C-37 | `KonradGourmet/src/co/edu/konradlorenz/notificaciones/dominio/Correo.java` (clase) | `KonradGourmet/src/co/edu/konradlorenz/notificaciones/puertos/salida/Correo.java` («interface») | 4d | `cls_notificaciones_Correo` | H25 + H8: pasa de clase a «interface» |
| C-38 | `KonradGourmet/src/co/edu/konradlorenz/notificaciones/dominio/CorreoDecorator.java` («abstract») | `KonradGourmet/src/co/edu/konradlorenz/notificaciones/adaptadores/salida/CorreoDecorator.java` («abstract») | 4d | `cls_notificaciones_CorreoDecorator` | N8; sigue «abstract» |
| C-39 | `KonradGourmet/src/co/edu/konradlorenz/notificaciones/dominio/CorreoCertificadoDecorator.java` (clase) | `KonradGourmet/src/co/edu/konradlorenz/notificaciones/adaptadores/salida/CorreoCertificadoDecorator.java` (clase) | 4d | `cls_notificaciones_CorreoCertificadoDecorator` | N8 |
| C-40 | `KonradGourmet/src/co/edu/konradlorenz/notificaciones/dominio/EstampaCronologicaDecorator.java` (clase) | `KonradGourmet/src/co/edu/konradlorenz/notificaciones/adaptadores/salida/EstampaCronologicaDecorator.java` (clase) | 4d | `cls_notificaciones_EstampaCronologicaDecorator` | N8 |

### 1.3 Tipo de clase distinto (H8)

Son 5: `Correo` (C-37, que además se mueve) y estas 4, que se quedan en su paquete. `EventoDominio` ya coincide: es abstracta en el código y en la 4a (DEC-09).

| Id | Clase | Pág. | Celda | Hoy en el código | Debe quedar | Arrastra |
|---|---|---|---|---|---|---|
| C-41 | `cotizaciones.dominio.ReglaValidacion` | 4c | `cls_cotizaciones_ReglaValidacion` | «interface» | «abstract» | H8: «abstract» con `# siguiente`, `enlazar()` y `cadenaPorDefecto()`; arrastra R de las 3 reglas (`implements` → `extends`) |
| C-42 | `identidad.dominio.ElementoMenu` | 4a | `cls_identidad_ElementoMenu` | clase | «abstract» | H8: «abstract» (Composite · componente), con `esHoja()` abstracto |
| C-43 | `notificaciones.dominio.Notificacion` | 4d | `cls_notificaciones_Notificacion` | «abstract» | «interface» | H8: «interface» (Factory Method · producto); arrastra R de `NotificacionCorreo` y `AlarmaApp` (`extends` → `implements`) |
| C-44 | `analitica.dominio.CalculadorKpi` | 4e | `cls_analitica_CalculadorKpi` | «abstract» | «interface» | H8: «interface» (Strategy); arrastra R de los 3 KPI (`extends` → `implements`) |

### 1.4 Sobra

| Id | Clase | Qué hacer |
|---|---|---|
| C-45 | `demo.DemoArquitectura` (`KonradGourmet/src/co/edu/konradlorenz/demo/DemoArquitectura.java`) | No está en 4a–4e. Se conserva tal cual; es la sesión 4 (D12) |

Total después de la sesión 3: **191 clases + la demo = 192 archivos** (193 + demo si se escoge la opción C de DEC-22).

## 2. Miembros (M)

Hoy ninguna de las 155 clases tiene atributos ni métodos, así que en **cada clase faltan todos los miembros** de su celda. El único caso que además "no coincide" es `EventListener`, que en el código no tiene parámetro de tipo (M-002).

Cómo leer cada bloque:
- se copia la notación UML de la celda: `+` público, `-` privado, `#` protegido;
- **(abstracto)** = en cursiva en el diagrama; **(estático)** = subrayado;
- ‹Vxx› = el método lleva el comentario de la ventana (ver J-05 y Q2);
- «Cabecera» = el texto magenta (patrón) o azul (tecnología, ruta o tópico) de la caja, que va al Javadoc de la clase (J-04);
- el archivo es el que existe hoy o, si la clase es nueva o se mueve, el que hay que crear.

### 2.1 Página 4a (`p3`)

**M-001** · `compartido.eventos.EventoDominio` · `cls_compartido_EventoDominio` · «abstract» · `KonradGourmet/src/co/edu/konradlorenz/compartido/eventos/EventoDominio.java`
- Atributos (4): `# id: UUID`, `# usuario: String`, `# correlacionId: UUID`, `# ocurridoEn: LocalDateTime`
- Métodos (5): `+ id(): UUID`, `+ usuario(): String`, `+ correlacionId(): UUID`, `+ ocurridoEn(): LocalDateTime`, `+ tipo(): String` (abstracto)

**M-002** · `compartido.eventos.EventListener` · `cls_compartido_EventListener` · «interface» · `KonradGourmet/src/co/edu/konradlorenz/compartido/eventos/EventListener.java`
- Cabecera: «Publish-Subscribe · suscriptor» · (Observer distribuido, vía Kafka)
- Atributos (0): —
- Métodos (1): `+ alRecibir(evento: T): void`
- **No coincide:** hoy es `public interface EventListener {}`, sin parámetro de tipo. Debe ser `EventListener<T extends EventoDominio>`.

**M-003** · `compartido.puertos.EventPublisherPort` · `cls_compartido_EventPublisherPort` · «interface» · `KonradGourmet/src/co/edu/konradlorenz/compartido/puertos/EventPublisherPort.java`
- Cabecera: «Publish-Subscribe · publicador» · (Observer distribuido, vía Kafka)
- Atributos (0): —
- Métodos (1): `+ publicar(evento: EventoDominio): void`

**M-004** · `compartido.adaptadores.KafkaEventPublisher` · `cls_compartido_KafkaEventPublisher` · clase · `KonradGourmet/src/co/edu/konradlorenz/compartido/adaptadores/KafkaEventPublisher.java`
- Atributos (1): `- prefijoTopico: String`
- Métodos (1): `+ publicar(evento: EventoDominio): void`

**M-005** · `compartido.eventos.AuditoriaEvent` · `cls_compartido_AuditoriaEvent` · clase · `KonradGourmet/src/co/edu/konradlorenz/compartido/eventos/AuditoriaEvent.java`
- Atributos (1): `- accion: String`
- Métodos (1): `+ tipo(): String`

**M-006** · `identidad.dominio.Usuario` · `cls_identidad_Usuario` · «abstract» · `KonradGourmet/src/co/edu/konradlorenz/identidad/dominio/Usuario.java`
- Cabecera: «Factory Method · creador»
- Atributos (5): `# id: Long`, `# nombre: String`, `# correo: String`, `# passwordHash: String`, `# sucursal: Sucursal`
- Métodos (3): `+ construirMenu(): ElementoMenu` (abstracto), `+ rol(): String` (abstracto), `+ asignarPasswordHash(hash: String): void`

**M-007** · `identidad.dominio.Mesero` · `cls_identidad_Mesero` · clase · `KonradGourmet/src/co/edu/konradlorenz/identidad/dominio/Mesero.java`
- Atributos (0): —
- Métodos (2): `+ construirMenu(): ElementoMenu`, `+ rol(): String`

**M-008** · `identidad.dominio.JefeCocina` · `cls_identidad_JefeCocina` · clase · `KonradGourmet/src/co/edu/konradlorenz/identidad/dominio/JefeCocina.java`
- Atributos (0): —
- Métodos (2): `+ construirMenu(): ElementoMenu`, `+ rol(): String`

**M-009** · `identidad.dominio.AuxiliarCompras` · `cls_identidad_AuxiliarCompras` · clase · `KonradGourmet/src/co/edu/konradlorenz/identidad/dominio/AuxiliarCompras.java`
- Atributos (0): —
- Métodos (2): `+ construirMenu(): ElementoMenu`, `+ rol(): String`

**M-010** · `identidad.dominio.DirectorCompras` · `cls_identidad_DirectorCompras` · clase · `KonradGourmet/src/co/edu/konradlorenz/identidad/dominio/DirectorCompras.java`
- Atributos (0): —
- Métodos (2): `+ construirMenu(): ElementoMenu`, `+ rol(): String`

**M-011** · `identidad.dominio.DirectorComercial` · `cls_identidad_DirectorComercial` · clase · `KonradGourmet/src/co/edu/konradlorenz/identidad/dominio/DirectorComercial.java`
- Atributos (0): —
- Métodos (2): `+ construirMenu(): ElementoMenu`, `+ rol(): String`

**M-012** · `identidad.dominio.Administrador` · `cls_identidad_Administrador` · clase · `KonradGourmet/src/co/edu/konradlorenz/identidad/dominio/Administrador.java`
- Atributos (0): —
- Métodos (2): `+ construirMenu(): ElementoMenu`, `+ rol(): String`

**M-013** · `identidad.dominio.Sucursal` · `cls_identidad_Sucursal` · clase · `KonradGourmet/src/co/edu/konradlorenz/identidad/dominio/Sucursal.java`
- Atributos (3): `- id: Long`, `- nombre: String`, `- usuarios: List<Usuario>`
- Métodos (0): —

**M-014** · `identidad.dominio.ElementoMenu` · `cls_identidad_ElementoMenu` · «abstract» · `KonradGourmet/src/co/edu/konradlorenz/identidad/dominio/ElementoMenu.java` · ver C-42
- Cabecera: «Composite · componente»
- Atributos (2): `# titulo: String`, `# ruta: String`
- Métodos (3): `+ agregar(e: ElementoMenu): void`, `+ hijos(): List<ElementoMenu>`, `+ esHoja(): boolean` (abstracto)

**M-015** · `identidad.dominio.OpcionMenu` · `cls_identidad_OpcionMenu` · clase · `KonradGourmet/src/co/edu/konradlorenz/identidad/dominio/OpcionMenu.java`
- Cabecera: «Composite · hoja»
- Atributos (0): —
- Métodos (1): `+ esHoja(): boolean`

**M-016** · `identidad.dominio.GrupoMenu` · `cls_identidad_GrupoMenu` · clase · `KonradGourmet/src/co/edu/konradlorenz/identidad/dominio/GrupoMenu.java`
- Cabecera: «Composite · compuesto»
- Atributos (1): `- hijos: List<ElementoMenu>`
- Métodos (3): `+ agregar(e: ElementoMenu): void`, `+ hijos(): List<ElementoMenu>`, `+ esHoja(): boolean`

**M-017** · `identidad.dominio.TokenAcceso` · `cls_identidad_TokenAcceso` · clase · `KonradGourmet/src/co/edu/konradlorenz/identidad/dominio/TokenAcceso.java`
- Atributos (2): `- jwt: String`, `- menu: ElementoMenu`
- Métodos (0): —

**M-018** · `identidad.puertos.entrada.IniciarSesionUseCase` · `cls_identidad_IniciarSesionUseCase` · «interface» · `KonradGourmet/src/co/edu/konradlorenz/identidad/puertos/entrada/IniciarSesionUseCase.java`
- Atributos (0): —
- Métodos (1): `+ iniciarSesion(correo: String, clave: String): TokenAcceso`

**M-019** · `identidad.aplicacion.IniciarSesionService` · `cls_identidad_IniciarSesionService` · clase · `KonradGourmet/src/co/edu/konradlorenz/identidad/aplicacion/IniciarSesionService.java`
- Atributos (3): `- usuarios: UsuarioRepositoryPort`, `- cifrador: CifradorPort`, `- eventos: EventPublisherPort`
- Métodos (1): `+ iniciarSesion(correo: String, clave: String): TokenAcceso`

**M-020** · `identidad.puertos.salida.UsuarioRepositoryPort` · `cls_identidad_UsuarioRepositoryPort` · «interface» · `KonradGourmet/src/co/edu/konradlorenz/identidad/puertos/salida/UsuarioRepositoryPort.java`
- Atributos (0): —
- Métodos (2): `+ buscarPorCorreo(correo: String): Usuario`, `+ guardar(u: Usuario): Usuario`

**M-021** · `identidad.puertos.salida.CifradorPort` · `cls_identidad_CifradorPort` · «interface» · `KonradGourmet/src/co/edu/konradlorenz/identidad/puertos/salida/CifradorPort.java`
- Atributos (0): —
- Métodos (2): `+ cifrar(clave: String): String`, `+ coincide(clave: String, hash: String): boolean`

**M-022** · `identidad.adaptadores.entrada.AuthRestController` · `cls_identidad_AuthRestController` · clase · `KonradGourmet/src/co/edu/konradlorenz/identidad/adaptadores/entrada/AuthRestController.java`
- Cabecera: «REST · POST /api/auth/token»
- Atributos (1): `- casoDeUso: IniciarSesionUseCase`
- Métodos (1): `+ iniciarSesion(correo: String, clave: String): TokenAcceso` ‹V01›

**M-023** · `identidad.adaptadores.salida.UsuarioJpaAdapter` · `cls_identidad_UsuarioJpaAdapter` · clase · `KonradGourmet/src/co/edu/konradlorenz/identidad/adaptadores/salida/UsuarioJpaAdapter.java`
- Atributos (0): —
- Métodos (2): `+ buscarPorCorreo(correo: String): Usuario`, `+ guardar(u: Usuario): Usuario`

**M-024** · `identidad.adaptadores.salida.BCryptCifradorAdapter` · `cls_identidad_BCryptCifradorAdapter` · clase · `KonradGourmet/src/co/edu/konradlorenz/identidad/adaptadores/salida/BCryptCifradorAdapter.java`
- Atributos (0): —
- Métodos (2): `+ cifrar(clave: String): String`, `+ coincide(clave: String, hash: String): boolean`

**M-025** · `identidad.dominio.PoliticaContrasena` · `cls_identidad_PoliticaContrasena` · clase · `KonradGourmet/src/co/edu/konradlorenz/identidad/dominio/PoliticaContrasena.java` · ver C-03
- Cabecera: {8+ caracteres, con mayúscula, minúscula y número}
- Atributos (1): `- longitudMinima: int = 8`
- Métodos (1): `+ validar(clave: String): void`

**M-026** · `identidad.adaptadores.entrada.UsuarioRestController` · `cls_identidad_UsuarioRestController` · clase · `KonradGourmet/src/co/edu/konradlorenz/identidad/adaptadores/entrada/UsuarioRestController.java` · ver C-01
- Cabecera: «REST · POST /api/usuarios (Administrador)»
- Atributos (1): `- casoDeUso: RegistrarUsuarioUseCase`
- Métodos (1): `+ registrar(nombre: String, correo: String, rol: String, sucursalId: Long, clave: String): Long`

**M-027** · `identidad.puertos.entrada.RegistrarUsuarioUseCase` · `cls_identidad_RegistrarUsuarioUseCase` · «interface» · `KonradGourmet/src/co/edu/konradlorenz/identidad/puertos/entrada/RegistrarUsuarioUseCase.java` · ver C-04
- Atributos (0): —
- Métodos (1): `+ registrar(nombre: String, correo: String, rol: String, sucursalId: Long, clave: String): Long`

**M-028** · `identidad.aplicacion.RegistrarUsuarioService` · `cls_identidad_RegistrarUsuarioService` · clase · `KonradGourmet/src/co/edu/konradlorenz/identidad/aplicacion/RegistrarUsuarioService.java` · ver C-02
- Atributos (4): `- usuarios: UsuarioRepositoryPort`, `- cifrador: CifradorPort`, `- politica: PoliticaContrasena`, `- eventos: EventPublisherPort`
- Métodos (1): `+ registrar(nombre: String, correo: String, rol: String, sucursalId: Long, clave: String): Long`

### 2.2 Página 4b (`p4`)

**M-029** · `pedidos.dominio.Pedido` · `cls_pedidos_Pedido` · clase · `KonradGourmet/src/co/edu/konradlorenz/pedidos/dominio/Pedido.java`
- Atributos (5): `- numero: String`, `- mesa: Mesa`, `- fecha: LocalDateTime`, `- estado: EstadoPedido`, `- detalles: List<DetallePedido>`
- Métodos (3): `+ agregarPlato(platoId: Long, cantidad: int, nota: String, precio: BigDecimal): void`, `+ total(): BigDecimal`, `+ validar(): void`

**M-030** · `pedidos.dominio.DetallePedido` · `cls_pedidos_DetallePedido` · clase · `KonradGourmet/src/co/edu/konradlorenz/pedidos/dominio/DetallePedido.java`
- Atributos (4): `- platoId: Long`, `- cantidad: int`, `- nota: String`, `- precioUnitario: BigDecimal`
- Métodos (1): `+ subtotal(): BigDecimal`

**M-031** · `pedidos.dominio.EstadoPedido` · `cls_pedidos_EstadoPedido` · «enumeration» · `KonradGourmet/src/co/edu/konradlorenz/pedidos/dominio/EstadoPedido.java`
- Valores, en este orden: `EN_PREPARACION`, `LISTO`, `SERVIDO`, `ENTREGADO`

**M-032** · `pedidos.dominio.CatalogoPlatos` · `cls_pedidos_CatalogoPlatos` · clase · `KonradGourmet/src/co/edu/konradlorenz/pedidos/dominio/CatalogoPlatos.java`
- Atributos (2): `- sucursalId: Long`, `- platos: Map<Long, PlatoCatalogo>`
- Métodos (3): `+ actualizar(p: PlatoCatalogo): void`, `+ precioDe(platoId: Long): BigDecimal`, `+ vigentes(): List<PlatoCatalogo>`

**M-033** · `pedidos.eventos.PedidoRegistradoEvent` · `cls_pedidos_PedidoRegistradoEvent` · clase · `KonradGourmet/src/co/edu/konradlorenz/pedidos/eventos/PedidoRegistradoEvent.java`
- Atributos (3): `- numero: String`, `- sucursalId: Long`, `- lineas: Map<Long, Integer>`
- Métodos (1): `+ tipo(): String`

**M-034** · `pedidos.puertos.entrada.RegistrarPedidoUseCase` · `cls_pedidos_RegistrarPedidoUseCase` · «interface» · `KonradGourmet/src/co/edu/konradlorenz/pedidos/puertos/entrada/RegistrarPedidoUseCase.java`
- Atributos (0): —
- Métodos (5): `+ registrar(p: Pedido): Pedido`, `+ pedidosDelTurno(meseroId: Long): List<Pedido>`, `+ catalogo(sucursalId: Long): List<PlatoCatalogo>`, `+ mesas(sucursalId: Long): List<Mesa>`, `+ actualizarCatalogo(p: PlatoCatalogo): void`

**M-035** · `pedidos.aplicacion.PedidoService` · `cls_pedidos_PedidoService` · clase · `KonradGourmet/src/co/edu/konradlorenz/pedidos/aplicacion/PedidoService.java`
- Atributos (3): `- repositorio: PedidoRepositoryPort`, `- catalogo: CatalogoRepositoryPort`, `- eventos: EventPublisherPort`
- Métodos (5): `+ registrar(p: Pedido): Pedido`, `+ pedidosDelTurno(meseroId: Long): List<Pedido>`, `+ catalogo(sucursalId: Long): List<PlatoCatalogo>`, `+ mesas(sucursalId: Long): List<Mesa>`, `+ actualizarCatalogo(p: PlatoCatalogo): void`

**M-036** · `pedidos.puertos.salida.PedidoRepositoryPort` · `cls_pedidos_PedidoRepositoryPort` · «interface» · `KonradGourmet/src/co/edu/konradlorenz/pedidos/puertos/salida/PedidoRepositoryPort.java`
- Atributos (0): —
- Métodos (3): `+ guardar(p: Pedido): Pedido`, `+ delTurno(meseroId: Long): List<Pedido>`, `+ mesasDe(sucursalId: Long): List<Mesa>`

**M-037** · `pedidos.adaptadores.entrada.PedidoRestController` · `cls_pedidos_PedidoRestController` · clase · `KonradGourmet/src/co/edu/konradlorenz/pedidos/adaptadores/entrada/PedidoRestController.java`
- Cabecera: «REST · /api/pedidos · GET …/catalogo · GET …/mesas»
- Atributos (1): `- casoDeUso: RegistrarPedidoUseCase`
- Métodos (4): `+ registrar(p: Pedido): Pedido` ‹V03 · V04›, `+ pedidosDelTurno(meseroId: Long): List<Pedido>` ‹V02›, `+ catalogo(sucursalId: Long): List<PlatoCatalogo>` ‹V03›, `+ mesas(sucursalId: Long): List<Mesa>` ‹V03›

**M-038** · `pedidos.adaptadores.entrada.MenuActualizadoListener` · `cls_pedidos_MenuActualizadoListener` · clase · `KonradGourmet/src/co/edu/konradlorenz/pedidos/adaptadores/entrada/MenuActualizadoListener.java`
- Cabecera: «Listener · menu.actualizado»
- Atributos (1): `- casoDeUso: RegistrarPedidoUseCase`
- Métodos (1): `+ alRecibir(evento: EventoDominio): void`

**M-039** · `pedidos.adaptadores.salida.PedidoJpaAdapter` · `cls_pedidos_PedidoJpaAdapter` · clase · `KonradGourmet/src/co/edu/konradlorenz/pedidos/adaptadores/salida/PedidoJpaAdapter.java`
- Atributos (0): —
- Métodos (3): `+ guardar(p: Pedido): Pedido`, `+ delTurno(meseroId: Long): List<Pedido>`, `+ mesasDe(sucursalId: Long): List<Mesa>`

**M-040** · `menu.dominio.Plato` · `cls_menu_Plato` · clase · `KonradGourmet/src/co/edu/konradlorenz/menu/dominio/Plato.java`
- Atributos (8): `- id: Long`, `- sucursalId: Long`, `- creadoPor: Long`, `- nombre: String`, `- descripcion: String`, `- precioVenta: BigDecimal`, `- activo: boolean`, `- ingredientes: List<Ingrediente>`
- Métodos (4): `+ agregarIngrediente(i: Ingrediente): void`, `+ validar(): void`, `+ editar(datos: Plato): void`, `+ desactivar(): void`

**M-041** · `menu.dominio.Ingrediente` · `cls_menu_Ingrediente` · clase · `KonradGourmet/src/co/edu/konradlorenz/menu/dominio/Ingrediente.java`
- Atributos (3): `- producto: Producto`, `- cantidad: BigDecimal`, `- unidad: UnidadMedida`
- Métodos (0): —

**M-042** · `menu.dominio.Producto` · `cls_menu_Producto` · clase · `KonradGourmet/src/co/edu/konradlorenz/menu/dominio/Producto.java`
- Atributos (3): `- id: Long`, `- nombre: String`, `- categoria: Categoria`
- Métodos (0): —

**M-043** · `menu.dominio.Categoria` · `cls_menu_Categoria` · clase · `KonradGourmet/src/co/edu/konradlorenz/menu/dominio/Categoria.java`
- Atributos (3): `- id: Long`, `- nombre: String`, `- productos: List<Producto>`
- Métodos (0): —

**M-044** · `menu.dominio.UnidadMedida` · `cls_menu_UnidadMedida` · «enumeration» · `KonradGourmet/src/co/edu/konradlorenz/menu/dominio/UnidadMedida.java`
- Valores, en este orden: `KG`, `GRAMO`, `LIBRA`, `LITRO`, `UNIDAD`

**M-045** · `menu.eventos.MenuActualizadoEvent` · `cls_menu_MenuActualizadoEvent` · clase · `KonradGourmet/src/co/edu/konradlorenz/menu/eventos/MenuActualizadoEvent.java`
- Atributos (6): `- platoId: Long`, `- sucursalId: Long`, `- nombre: String`, `- precioVenta: BigDecimal`, `- activo: boolean`, `- receta: Map<Long, BigDecimal>`
- Métodos (1): `+ tipo(): String`

**M-046** · `menu.puertos.entrada.RegistrarPlatoUseCase` · `cls_menu_RegistrarPlatoUseCase` · «interface» · `KonradGourmet/src/co/edu/konradlorenz/menu/puertos/entrada/RegistrarPlatoUseCase.java`
- Atributos (0): —
- Métodos (5): `+ registrar(p: Plato): Plato`, `+ consultar(sucursalId: Long): List<Plato>`, `+ editar(id: Long, p: Plato): Plato`, `+ desactivar(id: Long): void`, `+ productosPorCategoria(categoriaId: Long): List<Producto>`

**M-047** · `menu.aplicacion.PlatoService` · `cls_menu_PlatoService` · clase · `KonradGourmet/src/co/edu/konradlorenz/menu/aplicacion/PlatoService.java`
- Atributos (2): `- repositorio: PlatoRepositoryPort`, `- eventos: EventPublisherPort`
- Métodos (5): `+ registrar(p: Plato): Plato`, `+ consultar(sucursalId: Long): List<Plato>`, `+ editar(id: Long, p: Plato): Plato`, `+ desactivar(id: Long): void`, `+ productosPorCategoria(categoriaId: Long): List<Producto>`

**M-048** · `menu.puertos.salida.PlatoRepositoryPort` · `cls_menu_PlatoRepositoryPort` · «interface» · `KonradGourmet/src/co/edu/konradlorenz/menu/puertos/salida/PlatoRepositoryPort.java`
- Atributos (0): —
- Métodos (5): `+ guardar(p: Plato): Plato`, `+ productosPorCategoria(categoriaId: Long): List<Producto>`, `+ porId(id: Long): Plato`, `+ porSucursal(sucursalId: Long): List<Plato>`, `+ existeNombre(sucursalId: Long, nombre: String): boolean`

**M-049** · `menu.adaptadores.entrada.PlatoRestController` · `cls_menu_PlatoRestController` · clase · `KonradGourmet/src/co/edu/konradlorenz/menu/adaptadores/entrada/PlatoRestController.java`
- Cabecera: «REST · /api/platos · PUT …/{id} · PATCH …/{id}/desactivar»
- Atributos (1): `- casoDeUso: RegistrarPlatoUseCase`
- Métodos (5): `+ crear(p: Plato): Plato` ‹V06›, `+ consultar(sucursalId: Long): List<Plato>`, `+ editar(id: Long, p: Plato): Plato`, `+ desactivar(id: Long): void`, `+ productosPorCategoria(categoriaId: Long): List<Producto>` ‹V06›

**M-050** · `menu.adaptadores.salida.PlatoJpaAdapter` · `cls_menu_PlatoJpaAdapter` · clase · `KonradGourmet/src/co/edu/konradlorenz/menu/adaptadores/salida/PlatoJpaAdapter.java`
- Atributos (0): —
- Métodos (5): `+ guardar(p: Plato): Plato`, `+ productosPorCategoria(categoriaId: Long): List<Producto>`, `+ porId(id: Long): Plato`, `+ porSucursal(sucursalId: Long): List<Plato>`, `+ existeNombre(sucursalId: Long, nombre: String): boolean`

**M-051** · `inventario.dominio.InsumoInventario` · `cls_inventario_InsumoInventario` · clase · `KonradGourmet/src/co/edu/konradlorenz/inventario/dominio/InsumoInventario.java`
- Atributos (6): `- productoId: Long`, `- nombre: String`, `- stockActual: BigDecimal`, `- stockReferencia: BigDecimal`, `- unidad: String`, `- alertaAtendida: boolean`
- Métodos (3): `+ descontar(cantidad: BigDecimal): void`, `+ porcentajeExistencia(): BigDecimal`, `+ atenderAlerta(): void`

**M-052** · `inventario.dominio.Inventario` · `cls_inventario_Inventario` · clase · `KonradGourmet/src/co/edu/konradlorenz/inventario/dominio/Inventario.java`
- Atributos (2): `- sucursalId: Long`, `- insumos: List<InsumoInventario>`
- Métodos (1): `+ insumosBajoUmbral(umbral: UmbralStock): List<InsumoInventario>`

**M-053** · `inventario.dominio.UmbralStock` · `cls_inventario_UmbralStock` · clase · `KonradGourmet/src/co/edu/konradlorenz/inventario/dominio/UmbralStock.java`
- Atributos (1): `- porcentaje: BigDecimal`
- Métodos (1): `+ superado(insumo: InsumoInventario): boolean`

**M-054** · `inventario.eventos.StockBajoEvent` · `cls_inventario_StockBajoEvent` · clase · `KonradGourmet/src/co/edu/konradlorenz/inventario/eventos/StockBajoEvent.java`
- Atributos (6): `- sucursalId: Long`, `- productoId: Long`, `- nombreProducto: String`, `- stockActual: BigDecimal`, `- unidad: String`, `- porcentaje: BigDecimal`
- Métodos (1): `+ tipo(): String`

**M-055** · `inventario.puertos.entrada.GestionarInventarioUseCase` · `cls_inventario_GestionarInventarioUseCase` · «interface» · `KonradGourmet/src/co/edu/konradlorenz/inventario/puertos/entrada/GestionarInventarioUseCase.java`
- Atributos (0): —
- Métodos (6): `+ acumularConsumo(sucursalId: Long, lineas: Map<Long, Integer>): void`, `+ descontarConsumoDiario(): void`, `+ alertas(sucursalId: Long): List<InsumoInventario>`, `+ insumos(sucursalId: Long): List<InsumoInventario>`, `+ actualizarReceta(r: RecetaPlato): void`, `+ atenderAlertas(sucursalId: Long, productoIds: List<Long>): void`

**M-056** · `inventario.aplicacion.InventarioService` · `cls_inventario_InventarioService` · clase · `KonradGourmet/src/co/edu/konradlorenz/inventario/aplicacion/InventarioService.java`
- Atributos (3): `- repositorio: InventarioRepositoryPort`, `- umbral: UmbralStock`, `- eventos: EventPublisherPort`
- Métodos (6): `+ acumularConsumo(sucursalId: Long, lineas: Map<Long, Integer>): void`, `+ descontarConsumoDiario(): void`, `+ alertas(sucursalId: Long): List<InsumoInventario>`, `+ insumos(sucursalId: Long): List<InsumoInventario>`, `+ actualizarReceta(r: RecetaPlato): void`, `+ atenderAlertas(sucursalId: Long, productoIds: List<Long>): void`

**M-057** · `inventario.puertos.salida.InventarioRepositoryPort` · `cls_inventario_InventarioRepositoryPort` · «interface» · `KonradGourmet/src/co/edu/konradlorenz/inventario/puertos/salida/InventarioRepositoryPort.java`
- Atributos (0): —
- Métodos (7): `+ inventarioDe(sucursalId: Long): Inventario`, `+ guardar(i: Inventario): void`, `+ acumular(sucursalId: Long, consumo: Map<Long, BigDecimal>): void`, `+ sucursalesConConsumo(): List<Long>`, `+ consumoPendiente(sucursalId: Long): Map<Long, BigDecimal>`, `+ guardarReceta(r: RecetaPlato): void`, `+ recetaDe(platoId: Long): RecetaPlato`

**M-058** · `inventario.adaptadores.entrada.InventarioRestController` · `cls_inventario_InventarioRestController` · clase · `KonradGourmet/src/co/edu/konradlorenz/inventario/adaptadores/entrada/InventarioRestController.java`
- Cabecera: «REST · /api/inventario»
- Atributos (1): `- casoDeUso: GestionarInventarioUseCase`
- Métodos (2): `+ alertas(sucursalId: Long): List<InsumoInventario>` ‹V05›, `+ insumos(sucursalId: Long): List<InsumoInventario>` ‹V07›

**M-059** · `inventario.adaptadores.entrada.ConsumoDiarioJob` · `cls_inventario_ConsumoDiarioJob` · clase · `KonradGourmet/src/co/edu/konradlorenz/inventario/adaptadores/entrada/ConsumoDiarioJob.java`
- Cabecera: «Scheduler · diario 23:59»
- Atributos (1): `- casoDeUso: GestionarInventarioUseCase`
- Métodos (1): `+ ejecutar(): void`

**M-060** · `inventario.adaptadores.entrada.PedidoRegistradoListener` · `cls_inventario_PedidoRegistradoListener` · clase · `KonradGourmet/src/co/edu/konradlorenz/inventario/adaptadores/entrada/PedidoRegistradoListener.java`
- Cabecera: «Listener · pedido.registrado»
- Atributos (1): `- casoDeUso: GestionarInventarioUseCase`
- Métodos (1): `+ alRecibir(evento: EventoDominio): void`

**M-061** · `inventario.adaptadores.salida.InventarioJpaAdapter` · `cls_inventario_InventarioJpaAdapter` · clase · `KonradGourmet/src/co/edu/konradlorenz/inventario/adaptadores/salida/InventarioJpaAdapter.java`
- Atributos (0): —
- Métodos (7): `+ inventarioDe(sucursalId: Long): Inventario`, `+ guardar(i: Inventario): void`, `+ acumular(sucursalId: Long, consumo: Map<Long, BigDecimal>): void`, `+ sucursalesConConsumo(): List<Long>`, `+ consumoPendiente(sucursalId: Long): Map<Long, BigDecimal>`, `+ guardarReceta(r: RecetaPlato): void`, `+ recetaDe(platoId: Long): RecetaPlato`

**M-062** · `inventario.dominio.RecetaPlato` · `cls_inventario_RecetaPlato` · clase · `KonradGourmet/src/co/edu/konradlorenz/inventario/dominio/RecetaPlato.java` · ver C-06
- Atributos (2): `- platoId: Long`, `- insumos: Map<Long, BigDecimal>`
- Métodos (1): `+ consumoPara(cantidad: int): Map<Long, BigDecimal>`

**M-063** · `inventario.adaptadores.entrada.InventarioEventsListener` · `cls_inventario_InventarioEventsListener` · clase · `KonradGourmet/src/co/edu/konradlorenz/inventario/adaptadores/entrada/InventarioEventsListener.java` · ver C-05
- Cabecera: «Listener · menu.actualizado · solicitud.registrada»
- Atributos (1): `- casoDeUso: GestionarInventarioUseCase`
- Métodos (1): `+ alRecibir(evento: EventoDominio): void`

**M-064** · `pedidos.puertos.salida.CatalogoRepositoryPort` · `cls_pedidos_CatalogoRepositoryPort` · «interface» · `KonradGourmet/src/co/edu/konradlorenz/pedidos/puertos/salida/CatalogoRepositoryPort.java` · ver C-10
- Atributos (0): —
- Métodos (2): `+ catalogo(sucursalId: Long): CatalogoPlatos`, `+ guardar(p: PlatoCatalogo): void`

**M-065** · `pedidos.adaptadores.salida.CatalogoJpaAdapter` · `cls_pedidos_CatalogoJpaAdapter` · clase · `KonradGourmet/src/co/edu/konradlorenz/pedidos/adaptadores/salida/CatalogoJpaAdapter.java` · ver C-07
- Atributos (0): —
- Métodos (2): `+ catalogo(sucursalId: Long): CatalogoPlatos`, `+ guardar(p: PlatoCatalogo): void`

**M-066** · `pedidos.dominio.PlatoCatalogo` · `cls_pedidos_PlatoCatalogo` · clase · `KonradGourmet/src/co/edu/konradlorenz/pedidos/dominio/PlatoCatalogo.java` · ver C-09
- Atributos (5): `- platoId: Long`, `- sucursalId: Long`, `- nombre: String`, `- precio: BigDecimal`, `- activo: boolean`
- Métodos (0): —

**M-067** · `pedidos.dominio.Mesa` · `cls_pedidos_Mesa` · clase · `KonradGourmet/src/co/edu/konradlorenz/pedidos/dominio/Mesa.java` · ver C-08
- Atributos (4): `- numero: int`, `- sucursalId: Long`, `- capacidad: int`, `- activa: boolean`
- Métodos (0): —

### 2.3 Página 4c (`p5`)

**M-068** · `solicitudes.dominio.SolicitudAlimentos` · `cls_solicitudes_SolicitudAlimentos` · clase · `KonradGourmet/src/co/edu/konradlorenz/solicitudes/dominio/SolicitudAlimentos.java`
- Atributos (9): `- numero: String`, `- sucursalId: Long`, `- solicitanteId: Long`, `- correoSolicitante: String`, `- fecha: LocalDateTime`, `- estado: EstadoSolicitud`, `- valorSugeridoTotal: BigDecimal`, `- cotizacionesAplicadas: Set<String>`, `- items: List<ItemSolicitud>`
- Métodos (7): `+ agregarItem(i: ItemSolicitud): void`, `+ validarFormato(): void`, `+ marcarCotizado(productoId: Long, precio: BigDecimal): void`, `+ yaAplicada(cotizacion: String): boolean`, `+ todoCotizado(): boolean`, `+ pasarAEnTramite(): void`, `+ completar(): void`

**M-069** · `solicitudes.dominio.ItemSolicitud` · `cls_solicitudes_ItemSolicitud` · clase · `KonradGourmet/src/co/edu/konradlorenz/solicitudes/dominio/ItemSolicitud.java`
- Atributos (6): `- productoId: Long`, `- cantidad: BigDecimal`, `- marca: String`, `- unidad: String`, `- cotizado: boolean`, `- precioSugerido: BigDecimal`
- Métodos (0): —

**M-070** · `solicitudes.dominio.EstadoSolicitud` · `cls_solicitudes_EstadoSolicitud` · «enumeration» · `KonradGourmet/src/co/edu/konradlorenz/solicitudes/dominio/EstadoSolicitud.java`
- Valores, en este orden: `PENDIENTE`, `EN_TRAMITE`, `COMPLETADA`

**M-071** · `solicitudes.dominio.CriteriosConsulta` · `cls_solicitudes_CriteriosConsulta` · clase · `KonradGourmet/src/co/edu/konradlorenz/solicitudes/dominio/CriteriosConsulta.java`
- Atributos (5): `- desde: LocalDate`, `- hasta: LocalDate`, `- productoId: Long`, `- marca: String`, `- estado: EstadoSolicitud`
- Métodos (0): —

**M-072** · `solicitudes.eventos.SolicitudRegistradaEvent` · `cls_solicitudes_SolicitudRegistradaEvent` · clase · `KonradGourmet/src/co/edu/konradlorenz/solicitudes/eventos/SolicitudRegistradaEvent.java`
- Atributos (5): `- numero: String`, `- sucursalId: Long`, `- solicitanteId: Long`, `- correoSolicitante: String`, `- productoIds: List<Long>`
- Métodos (1): `+ tipo(): String`

**M-073** · `solicitudes.eventos.SolicitudEnTramiteEvent` · `cls_solicitudes_SolicitudEnTramiteEvent` · clase · `KonradGourmet/src/co/edu/konradlorenz/solicitudes/eventos/SolicitudEnTramiteEvent.java`
- Atributos (2): `- numero: String`, `- correoSolicitante: String`
- Métodos (1): `+ tipo(): String`

**M-074** · `solicitudes.puertos.entrada.GestionarSolicitudUseCase` · `cls_solicitudes_GestionarSolicitudUseCase` · «interface» · `KonradGourmet/src/co/edu/konradlorenz/solicitudes/puertos/entrada/GestionarSolicitudUseCase.java`
- Atributos (0): —
- Métodos (5): `+ registrar(s: SolicitudAlimentos): SolicitudAlimentos`, `+ consultar(c: CriteriosConsulta): List<SolicitudAlimentos>`, `+ detalle(numero: String): SolicitudAlimentos`, `+ registrarCotizacion(numero: String, cotizacion: String, precios: Map<Long, BigDecimal>): void`, `+ completar(numero: String): void`

**M-075** · `solicitudes.aplicacion.SolicitudService` · `cls_solicitudes_SolicitudService` · clase · `KonradGourmet/src/co/edu/konradlorenz/solicitudes/aplicacion/SolicitudService.java`
- Atributos (2): `- repositorio: SolicitudRepositoryPort`, `- eventos: EventPublisherPort`
- Métodos (5): `+ registrar(s: SolicitudAlimentos): SolicitudAlimentos`, `+ consultar(c: CriteriosConsulta): List<SolicitudAlimentos>`, `+ detalle(numero: String): SolicitudAlimentos`, `+ registrarCotizacion(numero: String, cotizacion: String, precios: Map<Long, BigDecimal>): void`, `+ completar(numero: String): void`

**M-076** · `solicitudes.puertos.salida.SolicitudRepositoryPort` · `cls_solicitudes_SolicitudRepositoryPort` · «interface» · `KonradGourmet/src/co/edu/konradlorenz/solicitudes/puertos/salida/SolicitudRepositoryPort.java`
- Atributos (0): —
- Métodos (3): `+ guardar(s: SolicitudAlimentos): SolicitudAlimentos`, `+ buscar(c: CriteriosConsulta): List<SolicitudAlimentos>`, `+ porNumero(numero: String): SolicitudAlimentos`

**M-077** · `solicitudes.adaptadores.entrada.SolicitudRestController` · `cls_solicitudes_SolicitudRestController` · clase · `KonradGourmet/src/co/edu/konradlorenz/solicitudes/adaptadores/entrada/SolicitudRestController.java`
- Cabecera: «REST · /api/solicitudes»
- Atributos (1): `- casoDeUso: GestionarSolicitudUseCase`
- Métodos (4): `+ registrar(s: SolicitudAlimentos): SolicitudAlimentos` ‹V08 · V09›, `+ misSolicitudes(c: CriteriosConsulta): List<SolicitudAlimentos>` ‹V10›, `+ consultar(c: CriteriosConsulta): List<SolicitudAlimentos>` ‹V11›, `+ detalle(numero: String): SolicitudAlimentos` ‹V12›

**M-078** · `solicitudes.adaptadores.entrada.CotizacionEventsListener` · `cls_solicitudes_CotizacionEventsListener` · clase · `KonradGourmet/src/co/edu/konradlorenz/solicitudes/adaptadores/entrada/CotizacionEventsListener.java`
- Cabecera: «Listener · cotizacion.enviada · orden.generada»
- Atributos (1): `- casoDeUso: GestionarSolicitudUseCase`
- Métodos (1): `+ alRecibir(evento: EventoDominio): void`

**M-079** · `solicitudes.adaptadores.salida.SolicitudJpaAdapter` · `cls_solicitudes_SolicitudJpaAdapter` · clase · `KonradGourmet/src/co/edu/konradlorenz/solicitudes/adaptadores/salida/SolicitudJpaAdapter.java`
- Atributos (0): —
- Métodos (3): `+ guardar(s: SolicitudAlimentos): SolicitudAlimentos`, `+ buscar(c: CriteriosConsulta): List<SolicitudAlimentos>`, `+ porNumero(numero: String): SolicitudAlimentos`

**M-080** · `cotizaciones.dominio.Cotizacion` · `cls_cotizaciones_Cotizacion` · clase · `KonradGourmet/src/co/edu/konradlorenz/cotizaciones/dominio/Cotizacion.java`
- Atributos (8): `- numero: String`, `- solicitudNumeros: List<String>`, `- proveedor: Proveedor`, `- fechaEnvio: LocalDate`, `- fechaLimite: LocalDate`, `- pdfUrl: String`, `- estado: EstadoCotizacion`, `- items: List<ItemCotizacion>`
- Métodos (6): `+ validarSolicitudes(): void`, `+ valorEstandarTotal(): BigDecimal`, `+ valorCotizadoTotal(): BigDecimal`, `+ diferenciaPorcentual(): BigDecimal`, `+ registrarValor(productoId: Long, precio: BigDecimal): void`, `+ cambiarEstado(e: EstadoCotizacion): void`

**M-081** · `cotizaciones.dominio.ItemCotizacion` · `cls_cotizaciones_ItemCotizacion` · clase · `KonradGourmet/src/co/edu/konradlorenz/cotizaciones/dominio/ItemCotizacion.java`
- Atributos (5): `- productoId: Long`, `- cantidad: BigDecimal`, `- unidad: String`, `- precioEstandar: BigDecimal`, `- precioProveedor: BigDecimal`
- Métodos (0): —

**M-082** · `cotizaciones.dominio.Proveedor` · `cls_cotizaciones_Proveedor` · clase · `KonradGourmet/src/co/edu/konradlorenz/cotizaciones/dominio/Proveedor.java`
- Atributos (8): `- id: Long`, `- razonSocial: String`, `- nit: String`, `- correo: String`, `- telefono: String`, `- direccion: String`, `- categorias: List<String>`, `- activo: boolean`
- Métodos (3): `+ validar(): void`, `+ editar(datos: Proveedor): void`, `+ inactivar(): void`

**M-083** · `cotizaciones.dominio.EstadoCotizacion` · `cls_cotizaciones_EstadoCotizacion` · «enumeration» · `KonradGourmet/src/co/edu/konradlorenz/cotizaciones/dominio/EstadoCotizacion.java`
- Valores, en este orden: `PENDIENTE_PRECIO`, `ENVIADA`, `RECHAZADA`, `SOSPECHOSA`, `OPCIONADA`, `CON_ORDEN`

**M-084** · `cotizaciones.dominio.ReglaValidacion` · `cls_cotizaciones_ReglaValidacion` · «abstract» · `KonradGourmet/src/co/edu/konradlorenz/cotizaciones/dominio/ReglaValidacion.java` · ver C-41
- Cabecera: «Chain of Responsibility · manejador»
- Atributos (1): `# siguiente: ReglaValidacion`
- Métodos (5): `+ cadenaPorDefecto(): ReglaValidacion` (estático), `+ enlazar(siguiente: ReglaValidacion): ReglaValidacion`, `+ evaluar(c: Cotizacion): EstadoCotizacion`, `# aplica(diferencia: BigDecimal): boolean` (abstracto), `# estado(): EstadoCotizacion` (abstracto)

**M-085** · `cotizaciones.dominio.ReglaRechazada` · `cls_cotizaciones_ReglaRechazada` · clase · `KonradGourmet/src/co/edu/konradlorenz/cotizaciones/dominio/ReglaRechazada.java`
- Cabecera: «más de 25 % por encima»
- Atributos (0): —
- Métodos (2): `# aplica(diferencia: BigDecimal): boolean`, `# estado(): EstadoCotizacion`

**M-086** · `cotizaciones.dominio.ReglaSospechosa` · `cls_cotizaciones_ReglaSospechosa` · clase · `KonradGourmet/src/co/edu/konradlorenz/cotizaciones/dominio/ReglaSospechosa.java`
- Cabecera: «más de 50 % por debajo»
- Atributos (0): —
- Métodos (2): `# aplica(diferencia: BigDecimal): boolean`, `# estado(): EstadoCotizacion`

**M-087** · `cotizaciones.dominio.ReglaOpcionada` · `cls_cotizaciones_ReglaOpcionada` · clase · `KonradGourmet/src/co/edu/konradlorenz/cotizaciones/dominio/ReglaOpcionada.java`
- Cabecera: «dentro del rango»
- Atributos (0): —
- Métodos (2): `# aplica(diferencia: BigDecimal): boolean`, `# estado(): EstadoCotizacion`

**M-088** · `cotizaciones.eventos.CotizacionEnviadaEvent` · `cls_cotizaciones_CotizacionEnviadaEvent` · clase · `KonradGourmet/src/co/edu/konradlorenz/cotizaciones/eventos/CotizacionEnviadaEvent.java`
- Atributos (5): `- numero: String`, `- solicitudNumeros: List<String>`, `- precios: Map<Long, BigDecimal>`, `- correoProveedor: String`, `- pdfUrl: String`
- Métodos (1): `+ tipo(): String`

**M-089** · `cotizaciones.eventos.CotizacionValidadaEvent` · `cls_cotizaciones_CotizacionValidadaEvent` · clase · `KonradGourmet/src/co/edu/konradlorenz/cotizaciones/eventos/CotizacionValidadaEvent.java`
- Atributos (4): `- numero: String`, `- proveedorId: Long`, `- estado: EstadoCotizacion`, `- valorCotizado: BigDecimal`
- Métodos (1): `+ tipo(): String`

**M-090** · `cotizaciones.puertos.entrada.GestionarCotizacionUseCase` · `cls_cotizaciones_GestionarCotizacionUseCase` · «interface» · `KonradGourmet/src/co/edu/konradlorenz/cotizaciones/puertos/entrada/GestionarCotizacionUseCase.java`
- Atributos (0): —
- Métodos (8): `+ generar(c: Cotizacion): Cotizacion`, `+ registrarValores(numero: String, precios: Map<Long, BigDecimal>): Cotizacion`, `+ validar(numero: String): Cotizacion`, `+ consultar(estado: EstadoCotizacion): List<Cotizacion>`, `+ pdf(numero: String): byte[]`, `+ detalle(numero: String): Cotizacion`, `+ reenviar(numero: String): void`, `+ marcarConOrden(numero: String): void`

**M-091** · `cotizaciones.aplicacion.GenerarCotizacionFacade` · `cls_cotizaciones_GenerarCotizacionFacade` · clase · `KonradGourmet/src/co/edu/konradlorenz/cotizaciones/aplicacion/GenerarCotizacionFacade.java`
- Cabecera: «Facade»
- Atributos (5): `- repositorio: CotizacionRepositoryPort`, `- precios: PrecioSugeridoPort`, `- pdf: GeneradorPdfPort`, `- almacen: AlmacenArchivosPort`, `- eventos: EventPublisherPort`
- Métodos (1): `+ generarYEnviar(c: Cotizacion): Cotizacion`

**M-092** · `cotizaciones.aplicacion.CotizacionService` · `cls_cotizaciones_CotizacionService` · clase · `KonradGourmet/src/co/edu/konradlorenz/cotizaciones/aplicacion/CotizacionService.java`
- Atributos (5): `- fachada: GenerarCotizacionFacade`, `- repositorio: CotizacionRepositoryPort`, `- reglas: ReglaValidacion`, `- almacen: AlmacenArchivosPort`, `- eventos: EventPublisherPort`
- Métodos (8): `+ generar(c: Cotizacion): Cotizacion`, `+ registrarValores(numero: String, precios: Map<Long, BigDecimal>): Cotizacion`, `+ validar(numero: String): Cotizacion`, `+ consultar(estado: EstadoCotizacion): List<Cotizacion>`, `+ pdf(numero: String): byte[]`, `+ detalle(numero: String): Cotizacion`, `+ reenviar(numero: String): void`, `+ marcarConOrden(numero: String): void`

**M-093** · `cotizaciones.puertos.salida.CotizacionRepositoryPort` · `cls_cotizaciones_CotizacionRepositoryPort` · «interface» · `KonradGourmet/src/co/edu/konradlorenz/cotizaciones/puertos/salida/CotizacionRepositoryPort.java`
- Atributos (0): —
- Métodos (3): `+ guardar(c: Cotizacion): Cotizacion`, `+ porNumero(numero: String): Cotizacion`, `+ porEstado(e: EstadoCotizacion): List<Cotizacion>`

**M-094** · `cotizaciones.puertos.salida.PrecioSugeridoPort` · `cls_cotizaciones_PrecioSugeridoPort` · «interface» · `KonradGourmet/src/co/edu/konradlorenz/cotizaciones/puertos/salida/PrecioSugeridoPort.java`
- Atributos (0): —
- Métodos (1): `+ precioSugerido(productoId: Long): BigDecimal`

**M-095** · `cotizaciones.puertos.salida.GeneradorPdfPort` · `cls_cotizaciones_GeneradorPdfPort` · «interface» · `KonradGourmet/src/co/edu/konradlorenz/cotizaciones/puertos/salida/GeneradorPdfPort.java`
- Atributos (0): —
- Métodos (1): `+ generar(c: Cotizacion): byte[]`

**M-096** · `cotizaciones.adaptadores.entrada.CotizacionRestController` · `cls_cotizaciones_CotizacionRestController` · clase · `KonradGourmet/src/co/edu/konradlorenz/cotizaciones/adaptadores/entrada/CotizacionRestController.java`
- Cabecera: «REST · /api/cotizaciones · GET …/{id} · POST …/{id}/reenvio»
- Atributos (1): `- casoDeUso: GestionarCotizacionUseCase`
- Métodos (9): `+ generar(c: Cotizacion): Cotizacion` ‹V13›, `+ pdf(numero: String): byte[]` ‹V14›, `+ consultar(estado: EstadoCotizacion): List<Cotizacion>` ‹V15 · V17›, `+ pendientesDeAtencion(): List<Cotizacion>` ‹V16›, `+ registrarValores(numero: String, precios: Map<Long, BigDecimal>): Cotizacion` ‹V18›, `+ validar(numero: String): Cotizacion` ‹V19›, `+ opcionadas(): List<Cotizacion>` ‹V20›, `+ detalle(numero: String): Cotizacion` ‹V20 · V21›, `+ reenviar(numero: String): void`

**M-097** · `cotizaciones.adaptadores.salida.CotizacionJpaAdapter` · `cls_cotizaciones_CotizacionJpaAdapter` · clase · `KonradGourmet/src/co/edu/konradlorenz/cotizaciones/adaptadores/salida/CotizacionJpaAdapter.java`
- Atributos (1): `- datos: Map<String, Cotizacion>`
- Métodos (3): `+ guardar(c: Cotizacion): Cotizacion`, `+ porNumero(numero: String): Cotizacion`, `+ porEstado(e: EstadoCotizacion): List<Cotizacion>`

**M-098** · `cotizaciones.adaptadores.salida.PrecioSugeridoRestAdapter` · `cls_cotizaciones_PrecioSugeridoRestAdapter` · clase · `KonradGourmet/src/co/edu/konradlorenz/cotizaciones/adaptadores/salida/PrecioSugeridoRestAdapter.java`
- Cabecera: «REST cliente → ms-integracion»
- Atributos (1): `- urlIntegracion: String`
- Métodos (1): `+ precioSugerido(productoId: Long): BigDecimal`

**M-099** · `cotizaciones.adaptadores.salida.PdfCotizacionAdapter` · `cls_cotizaciones_PdfCotizacionAdapter` · clase · `KonradGourmet/src/co/edu/konradlorenz/cotizaciones/adaptadores/salida/PdfCotizacionAdapter.java`
- Cabecera: «OpenPDF»
- Atributos (0): —
- Métodos (1): `+ generar(c: Cotizacion): byte[]`

**M-100** · `cotizaciones.puertos.salida.AlmacenArchivosPort` · `cls_cotizaciones_AlmacenArchivosPort` · «interface» · `KonradGourmet/src/co/edu/konradlorenz/cotizaciones/puertos/salida/AlmacenArchivosPort.java` · ver C-17
- Atributos (0): —
- Métodos (2): `+ guardar(nombre: String, contenido: byte[]): String`, `+ obtener(url: String): byte[]`

**M-101** · `cotizaciones.adaptadores.salida.AlmacenObjetosAdapter` · `cls_cotizaciones_AlmacenObjetosAdapter` · clase · `KonradGourmet/src/co/edu/konradlorenz/cotizaciones/adaptadores/salida/AlmacenObjetosAdapter.java` · ver C-13
- Cabecera: «almacén de objetos (S3 compatible) · HTTPS»
- Atributos (0): —
- Métodos (2): `+ guardar(nombre: String, contenido: byte[]): String`, `+ obtener(url: String): byte[]`

**M-102** · `cotizaciones.adaptadores.entrada.OrdenGeneradaListener` · `cls_cotizaciones_OrdenGeneradaListener` · clase · `KonradGourmet/src/co/edu/konradlorenz/cotizaciones/adaptadores/entrada/OrdenGeneradaListener.java` · ver C-11
- Cabecera: «Listener · orden.generada»
- Atributos (1): `- casoDeUso: GestionarCotizacionUseCase`
- Métodos (1): `+ alRecibir(evento: EventoDominio): void`

**M-103** · `cotizaciones.adaptadores.entrada.ProveedorRestController` · `cls_cotizaciones_ProveedorRestController` · clase · `KonradGourmet/src/co/edu/konradlorenz/cotizaciones/adaptadores/entrada/ProveedorRestController.java` · ver C-12
- Cabecera: «REST · /api/proveedores (Auxiliar de compras)»
- Atributos (1): `- casoDeUso: GestionarProveedorUseCase`
- Métodos (5): `+ registrar(p: Proveedor): Proveedor`, `+ consultar(soloActivos: boolean): List<Proveedor>` ‹V13›, `+ detalle(id: Long): Proveedor`, `+ editar(id: Long, p: Proveedor): Proveedor`, `+ inactivar(id: Long): void`

**M-104** · `cotizaciones.puertos.entrada.GestionarProveedorUseCase` · `cls_cotizaciones_GestionarProveedorUseCase` · «interface» · `KonradGourmet/src/co/edu/konradlorenz/cotizaciones/puertos/entrada/GestionarProveedorUseCase.java` · ver C-16
- Atributos (0): —
- Métodos (5): `+ registrar(p: Proveedor): Proveedor`, `+ consultar(soloActivos: boolean): List<Proveedor>`, `+ detalle(id: Long): Proveedor`, `+ editar(id: Long, p: Proveedor): Proveedor`, `+ inactivar(id: Long): void`

**M-105** · `cotizaciones.aplicacion.ProveedorService` · `cls_cotizaciones_ProveedorService` · clase · `KonradGourmet/src/co/edu/konradlorenz/cotizaciones/aplicacion/ProveedorService.java` · ver C-15
- Atributos (2): `- repositorio: ProveedorRepositoryPort`, `- eventos: EventPublisherPort`
- Métodos (5): `+ registrar(p: Proveedor): Proveedor`, `+ consultar(soloActivos: boolean): List<Proveedor>`, `+ detalle(id: Long): Proveedor`, `+ editar(id: Long, p: Proveedor): Proveedor`, `+ inactivar(id: Long): void`

**M-106** · `cotizaciones.puertos.salida.ProveedorRepositoryPort` · `cls_cotizaciones_ProveedorRepositoryPort` · «interface» · `KonradGourmet/src/co/edu/konradlorenz/cotizaciones/puertos/salida/ProveedorRepositoryPort.java` · ver C-18
- Atributos (0): —
- Métodos (5): `+ guardar(p: Proveedor): Proveedor`, `+ porId(id: Long): Proveedor`, `+ existeNit(nit: String): boolean`, `+ activos(): List<Proveedor>`, `+ todos(): List<Proveedor>`

**M-107** · `cotizaciones.adaptadores.salida.ProveedorJpaAdapter` · `cls_cotizaciones_ProveedorJpaAdapter` · clase · `KonradGourmet/src/co/edu/konradlorenz/cotizaciones/adaptadores/salida/ProveedorJpaAdapter.java` · ver C-14
- Atributos (0): —
- Métodos (5): `+ guardar(p: Proveedor): Proveedor`, `+ porId(id: Long): Proveedor`, `+ existeNit(nit: String): boolean`, `+ activos(): List<Proveedor>`, `+ todos(): List<Proveedor>`

### 2.4 Página 4d (`p6`)

**M-108** · `ordenes.dominio.OrdenCompra` · `cls_ordenes_OrdenCompra` · clase · `KonradGourmet/src/co/edu/konradlorenz/ordenes/dominio/OrdenCompra.java`
- Atributos (9): `- numero: String`, `- fecha: LocalDate`, `- cotizacionNumero: String {único}`, `- solicitudNumeros: List<String>`, `- proveedor: String`, `- correoProveedor: String`, `- pdfUrl: String`, `- estado: EstadoOrden`, `- items: List<ItemOrden>`
- Métodos (2): `+ desde(c: CotizacionOpcionada): OrdenCompra` (estático), `+ total(): BigDecimal`

**M-109** · `ordenes.dominio.ItemOrden` · `cls_ordenes_ItemOrden` · clase · `KonradGourmet/src/co/edu/konradlorenz/ordenes/dominio/ItemOrden.java`
- Atributos (4): `- productoId: Long`, `- cantidad: BigDecimal`, `- unidad: String`, `- precio: BigDecimal`
- Métodos (0): —

**M-110** · `ordenes.dominio.EstadoOrden` · `cls_ordenes_EstadoOrden` · «enumeration» · `KonradGourmet/src/co/edu/konradlorenz/ordenes/dominio/EstadoOrden.java`
- Valores, en este orden: `EN_PROCESO`, `FINALIZADA`

**M-111** · `ordenes.dominio.CotizacionOpcionada` · `cls_ordenes_CotizacionOpcionada` · clase · `KonradGourmet/src/co/edu/konradlorenz/ordenes/dominio/CotizacionOpcionada.java`
- Atributos (5): `- numero: String`, `- solicitudNumeros: List<String>`, `- proveedor: String`, `- correoProveedor: String`, `- items: List<ItemOrden>`
- Métodos (0): —

**M-112** · `ordenes.eventos.OrdenGeneradaEvent` · `cls_ordenes_OrdenGeneradaEvent` · clase · `KonradGourmet/src/co/edu/konradlorenz/ordenes/eventos/OrdenGeneradaEvent.java`
- Atributos (7): `- numero: String`, `- cotizacionNumero: String`, `- solicitudNumeros: List<String>`, `- proveedor: String`, `- correoProveedor: String`, `- pdfUrl: String`, `- total: BigDecimal`
- Métodos (1): `+ tipo(): String`

**M-113** · `ordenes.puertos.entrada.GenerarOrdenUseCase` · `cls_ordenes_GenerarOrdenUseCase` · «interface» · `KonradGourmet/src/co/edu/konradlorenz/ordenes/puertos/entrada/GenerarOrdenUseCase.java`
- Atributos (0): —
- Métodos (2): `+ generar(cotizacionNumero: String): OrdenCompra`, `+ consultar(estado: EstadoOrden): List<OrdenCompra>`

**M-114** · `ordenes.aplicacion.OrdenCompraService` · `cls_ordenes_OrdenCompraService` · clase · `KonradGourmet/src/co/edu/konradlorenz/ordenes/aplicacion/OrdenCompraService.java`
- Atributos (5): `- repositorio: OrdenRepositoryPort`, `- cotizaciones: CotizacionConsultaPort`, `- pdf: GeneradorPdfPort`, `- almacen: AlmacenArchivosPort`, `- eventos: EventPublisherPort`
- Métodos (2): `+ generar(cotizacionNumero: String): OrdenCompra`, `+ consultar(estado: EstadoOrden): List<OrdenCompra>`

**M-115** · `ordenes.puertos.salida.OrdenRepositoryPort` · `cls_ordenes_OrdenRepositoryPort` · «interface» · `KonradGourmet/src/co/edu/konradlorenz/ordenes/puertos/salida/OrdenRepositoryPort.java`
- Atributos (0): —
- Métodos (3): `+ guardar(o: OrdenCompra): OrdenCompra`, `+ porEstado(e: EstadoOrden): List<OrdenCompra>`, `+ existePorCotizacion(numero: String): boolean`

**M-116** · `ordenes.puertos.salida.CotizacionConsultaPort` · `cls_ordenes_CotizacionConsultaPort` · «interface» · `KonradGourmet/src/co/edu/konradlorenz/ordenes/puertos/salida/CotizacionConsultaPort.java`
- Atributos (0): —
- Métodos (1): `+ opcionada(numero: String): CotizacionOpcionada`

**M-117** · `ordenes.adaptadores.entrada.OrdenCompraRestController` · `cls_ordenes_OrdenCompraRestController` · clase · `KonradGourmet/src/co/edu/konradlorenz/ordenes/adaptadores/entrada/OrdenCompraRestController.java`
- Cabecera: «REST · /api/ordenes-compra»
- Atributos (1): `- casoDeUso: GenerarOrdenUseCase`
- Métodos (2): `+ generar(cotizacionNumero: String): OrdenCompra` ‹V21 · V22›, `+ consultar(estado: EstadoOrden): List<OrdenCompra>` ‹V23›

**M-118** · `ordenes.adaptadores.salida.OrdenJpaAdapter` · `cls_ordenes_OrdenJpaAdapter` · clase · `KonradGourmet/src/co/edu/konradlorenz/ordenes/adaptadores/salida/OrdenJpaAdapter.java`
- Atributos (0): —
- Métodos (3): `+ guardar(o: OrdenCompra): OrdenCompra`, `+ porEstado(e: EstadoOrden): List<OrdenCompra>`, `+ existePorCotizacion(numero: String): boolean`

**M-119** · `ordenes.adaptadores.salida.CotizacionRestClient` · `cls_ordenes_CotizacionRestClient` · clase · `KonradGourmet/src/co/edu/konradlorenz/ordenes/adaptadores/salida/CotizacionRestClient.java`
- Cabecera: «REST cliente → GET /api/cotizaciones/{id}»
- Atributos (0): —
- Métodos (1): `+ opcionada(numero: String): CotizacionOpcionada`

**M-120** · `notificaciones.dominio.Notificacion` · `cls_notificaciones_Notificacion` · «interface» · `KonradGourmet/src/co/edu/konradlorenz/notificaciones/dominio/Notificacion.java` · ver C-43
- Cabecera: «Factory Method · producto»
- Atributos (0): —
- Métodos (1): `+ enviar(destino: String, contenido: String): void`

**M-121** · `notificaciones.dominio.NotificacionCorreo` · `cls_notificaciones_NotificacionCorreo` · clase · `KonradGourmet/src/co/edu/konradlorenz/notificaciones/dominio/NotificacionCorreo.java`
- Atributos (1): `- correo: Correo`
- Métodos (1): `+ enviar(destino: String, contenido: String): void`

**M-122** · `notificaciones.dominio.AlarmaApp` · `cls_notificaciones_AlarmaApp` · clase · `KonradGourmet/src/co/edu/konradlorenz/notificaciones/dominio/AlarmaApp.java`
- Atributos (1): `- canal: AlarmaPushPort`
- Métodos (1): `+ enviar(destino: String, contenido: String): void`

**M-123** · `notificaciones.dominio.CreadorNotificacion` · `cls_notificaciones_CreadorNotificacion` · «abstract» · `KonradGourmet/src/co/edu/konradlorenz/notificaciones/dominio/CreadorNotificacion.java`
- Cabecera: «Factory Method · creador»
- Atributos (0): —
- Métodos (2): `+ notificar(destino: String, contenido: String): void`, `# crearNotificacion(): Notificacion` (abstracto)

**M-124** · `notificaciones.dominio.CreadorCorreo` · `cls_notificaciones_CreadorCorreo` · clase · `KonradGourmet/src/co/edu/konradlorenz/notificaciones/dominio/CreadorCorreo.java`
- Atributos (1): `- correo: Correo`
- Métodos (1): `# crearNotificacion(): Notificacion`

**M-125** · `notificaciones.dominio.CreadorAlarmaApp` · `cls_notificaciones_CreadorAlarmaApp` · clase · `KonradGourmet/src/co/edu/konradlorenz/notificaciones/dominio/CreadorAlarmaApp.java`
- Atributos (1): `- canal: AlarmaPushPort`
- Métodos (1): `# crearNotificacion(): Notificacion`

**M-126** · `notificaciones.puertos.salida.Correo` · `cls_notificaciones_Correo` · «interface» · `KonradGourmet/src/co/edu/konradlorenz/notificaciones/puertos/salida/Correo.java` · ver C-37
- Cabecera: «Decorator · componente»
- Atributos (0): —
- Métodos (1): `+ enviar(m: Mensaje): void`

**M-127** · `notificaciones.dominio.Mensaje` · `cls_notificaciones_Mensaje` · clase · `KonradGourmet/src/co/edu/konradlorenz/notificaciones/dominio/Mensaje.java`
- Atributos (5): `- para: String`, `- asunto: String`, `- cuerpo: String`, `- cabeceras: Map<String, String>`, `- adjuntoUrl: String`
- Métodos (0): —

**M-128** · `notificaciones.adaptadores.salida.CorreoDecorator` · `cls_notificaciones_CorreoDecorator` · «abstract» · `KonradGourmet/src/co/edu/konradlorenz/notificaciones/adaptadores/salida/CorreoDecorator.java` · ver C-38
- Cabecera: «Decorator»
- Atributos (1): `# envuelto: Correo`
- Métodos (1): `+ enviar(m: Mensaje): void`

**M-129** · `notificaciones.adaptadores.salida.CorreoCertificadoDecorator` · `cls_notificaciones_CorreoCertificadoDecorator` · clase · `KonradGourmet/src/co/edu/konradlorenz/notificaciones/adaptadores/salida/CorreoCertificadoDecorator.java` · ver C-39
- Cabecera: «Decorator concreto»
- Atributos (0): —
- Métodos (1): `+ enviar(m: Mensaje): void`

**M-130** · `notificaciones.adaptadores.salida.EstampaCronologicaDecorator` · `cls_notificaciones_EstampaCronologicaDecorator` · clase · `KonradGourmet/src/co/edu/konradlorenz/notificaciones/adaptadores/salida/EstampaCronologicaDecorator.java` · ver C-40
- Cabecera: «Decorator concreto»
- Atributos (0): —
- Métodos (1): `+ enviar(m: Mensaje): void`

**M-131** · `notificaciones.puertos.entrada.NotificarUseCase` · `cls_notificaciones_NotificarUseCase` · «interface» · `KonradGourmet/src/co/edu/konradlorenz/notificaciones/puertos/entrada/NotificarUseCase.java`
- Atributos (0): —
- Métodos (3): `+ notificarEvento(evento: EventoDominio): void`, `+ registrarDestinatario(sucursalId: Long, correo: String): void`, `+ destinatarios(sucursalId: Long): List<String>`

**M-132** · `notificaciones.aplicacion.NotificacionService` · `cls_notificaciones_NotificacionService` · clase · `KonradGourmet/src/co/edu/konradlorenz/notificaciones/aplicacion/NotificacionService.java`
- Atributos (5): `- creadorCorreo: CreadorNotificacion`, `- creadorAlarma: CreadorNotificacion`, `- envios: EnvioRepositoryPort`, `- destinatarios: DestinatarioRepositoryPort`, `- eventos: EventPublisherPort`
- Métodos (3): `+ notificarEvento(evento: EventoDominio): void`, `+ registrarDestinatario(sucursalId: Long, correo: String): void`, `+ destinatarios(sucursalId: Long): List<String>`

**M-133** · `notificaciones.puertos.salida.AlarmaPushPort` · `cls_notificaciones_AlarmaPushPort` · «interface» · `KonradGourmet/src/co/edu/konradlorenz/notificaciones/puertos/salida/AlarmaPushPort.java`
- Atributos (0): —
- Métodos (1): `+ empujar(destino: String, contenido: String): void`

**M-134** · `notificaciones.puertos.salida.EnvioRepositoryPort` · `cls_notificaciones_EnvioRepositoryPort` · «interface» · `KonradGourmet/src/co/edu/konradlorenz/notificaciones/puertos/salida/EnvioRepositoryPort.java`
- Atributos (0): —
- Métodos (2): `+ registrar(destino: String, tipo: String, fechaHora: LocalDateTime): void`, `+ registrarFallo(destino: String, tipo: String, error: String): void`

**M-135** · `notificaciones.adaptadores.entrada.NotificacionListeners` · `cls_notificaciones_NotificacionListeners` · clase · `KonradGourmet/src/co/edu/konradlorenz/notificaciones/adaptadores/entrada/NotificacionListeners.java`
- Cabecera: «Listener · 6 tópicos»
- Atributos (1): `- casoDeUso: NotificarUseCase`
- Métodos (1): `+ alRecibir(evento: EventoDominio): void`

**M-136** · `notificaciones.adaptadores.salida.CorreoSmtpAdapter` · `cls_notificaciones_CorreoSmtpAdapter` · clase · `KonradGourmet/src/co/edu/konradlorenz/notificaciones/adaptadores/salida/CorreoSmtpAdapter.java`
- Cabecera: «Decorator · componente concreto (SMTP/TLS)»
- Atributos (1): `- almacen: AlmacenArchivosPort`
- Métodos (1): `+ enviar(m: Mensaje): void`

**M-137** · `notificaciones.adaptadores.salida.AlarmaPushAdapter` · `cls_notificaciones_AlarmaPushAdapter` · clase · `KonradGourmet/src/co/edu/konradlorenz/notificaciones/adaptadores/salida/AlarmaPushAdapter.java`
- Cabecera: «WebSocket → V05»
- Atributos (0): —
- Métodos (1): `+ empujar(destino: String, contenido: String): void`

**M-138** · `notificaciones.adaptadores.salida.EnvioJpaAdapter` · `cls_notificaciones_EnvioJpaAdapter` · clase · `KonradGourmet/src/co/edu/konradlorenz/notificaciones/adaptadores/salida/EnvioJpaAdapter.java`
- Atributos (0): —
- Métodos (2): `+ registrar(destino: String, tipo: String, fechaHora: LocalDateTime): void`, `+ registrarFallo(destino: String, tipo: String, error: String): void`

**M-139** · `ordenes.puertos.salida.GeneradorPdfPort` · `cls_ordenes_GeneradorPdfPort` · «interface» · `KonradGourmet/src/co/edu/konradlorenz/ordenes/puertos/salida/GeneradorPdfPort.java` · ver C-27
- Atributos (0): —
- Métodos (1): `+ generar(o: OrdenCompra): byte[]`

**M-140** · `ordenes.adaptadores.salida.PdfOrdenAdapter` · `cls_ordenes_PdfOrdenAdapter` · clase · `KonradGourmet/src/co/edu/konradlorenz/ordenes/adaptadores/salida/PdfOrdenAdapter.java` · ver C-25
- Cabecera: «OpenPDF»
- Atributos (0): —
- Métodos (1): `+ generar(o: OrdenCompra): byte[]`

**M-141** · `ordenes.puertos.salida.AlmacenArchivosPort` · `cls_ordenes_AlmacenArchivosPort` · «interface» · `KonradGourmet/src/co/edu/konradlorenz/ordenes/puertos/salida/AlmacenArchivosPort.java` · ver C-26
- Atributos (0): —
- Métodos (2): `+ guardar(nombre: String, contenido: byte[]): String`, `+ obtener(url: String): byte[]`

**M-142** · `ordenes.adaptadores.salida.AlmacenObjetosAdapter` · `cls_ordenes_AlmacenObjetosAdapter` · clase · `KonradGourmet/src/co/edu/konradlorenz/ordenes/adaptadores/salida/AlmacenObjetosAdapter.java` · ver C-24
- Cabecera: «almacén de objetos (S3 compatible) · HTTPS»
- Atributos (0): —
- Métodos (2): `+ guardar(nombre: String, contenido: byte[]): String`, `+ obtener(url: String): byte[]`

**M-143** · `notificaciones.puertos.salida.AlmacenArchivosPort` · `cls_notificaciones_AlmacenArchivosPort` · «interface» · `KonradGourmet/src/co/edu/konradlorenz/notificaciones/puertos/salida/AlmacenArchivosPort.java` · ver C-22
- Atributos (0): —
- Métodos (1): `+ obtener(url: String): byte[]`

**M-144** · `notificaciones.adaptadores.salida.AlmacenObjetosAdapter` · `cls_notificaciones_AlmacenObjetosAdapter` · clase · `KonradGourmet/src/co/edu/konradlorenz/notificaciones/adaptadores/salida/AlmacenObjetosAdapter.java` · ver C-20
- Cabecera: «almacén de objetos (S3 compatible) · HTTPS»
- Atributos (0): —
- Métodos (1): `+ obtener(url: String): byte[]`

**M-145** · `notificaciones.adaptadores.entrada.DestinatarioRestController` · `cls_notificaciones_DestinatarioRestController` · clase · `KonradGourmet/src/co/edu/konradlorenz/notificaciones/adaptadores/entrada/DestinatarioRestController.java` · ver C-19
- Cabecera: «REST · /api/notificaciones/destinatarios (Administrador)»
- Atributos (1): `- casoDeUso: NotificarUseCase`
- Métodos (2): `+ registrar(sucursalId: Long, correo: String): void`, `+ consultar(sucursalId: Long): List<String>`

**M-146** · `notificaciones.puertos.salida.DestinatarioRepositoryPort` · `cls_notificaciones_DestinatarioRepositoryPort` · «interface» · `KonradGourmet/src/co/edu/konradlorenz/notificaciones/puertos/salida/DestinatarioRepositoryPort.java` · ver C-23
- Atributos (0): —
- Métodos (3): `+ jefesDe(sucursalId: Long): List<String>`, `+ jefes(): List<String>`, `+ guardar(sucursalId: Long, correo: String): void`

**M-147** · `notificaciones.adaptadores.salida.DestinatarioJpaAdapter` · `cls_notificaciones_DestinatarioJpaAdapter` · clase · `KonradGourmet/src/co/edu/konradlorenz/notificaciones/adaptadores/salida/DestinatarioJpaAdapter.java` · ver C-21
- Atributos (0): —
- Métodos (3): `+ jefesDe(sucursalId: Long): List<String>`, `+ jefes(): List<String>`, `+ guardar(sucursalId: Long, correo: String): void`

### 2.5 Página 4e (`p7`)

**M-148** · `integracion.dominio.PrecioEstandar` · `cls_integracion_PrecioEstandar` · clase · `KonradGourmet/src/co/edu/konradlorenz/integracion/dominio/PrecioEstandar.java`
- Atributos (3): `- productoId: Long`, `- valor: BigDecimal`, `- consultadoEn: LocalDateTime`
- Métodos (0): —

**M-149** · `integracion.puertos.entrada.ConsultarPrecioUseCase` · `cls_integracion_ConsultarPrecioUseCase` · «interface» · `KonradGourmet/src/co/edu/konradlorenz/integracion/puertos/entrada/ConsultarPrecioUseCase.java`
- Atributos (0): —
- Métodos (1): `+ precioSugerido(productoId: Long): BigDecimal`

**M-150** · `integracion.aplicacion.ConsultarPrecioService` · `cls_integracion_ConsultarPrecioService` · clase · `KonradGourmet/src/co/edu/konradlorenz/integracion/aplicacion/ConsultarPrecioService.java`
- Atributos (2): `- precios: PrecioReferenciaPort`, `- eventos: EventPublisherPort`
- Métodos (1): `+ precioSugerido(productoId: Long): BigDecimal`

**M-151** · `integracion.puertos.salida.PrecioReferenciaPort` · `cls_integracion_PrecioReferenciaPort` · «interface» · `KonradGourmet/src/co/edu/konradlorenz/integracion/puertos/salida/PrecioReferenciaPort.java`
- Cabecera: «Proxy · sujeto / Adapter · objetivo»
- Atributos (0): —
- Métodos (1): `+ precioEstandar(productoId: Long): PrecioEstandar`

**M-152** · `integracion.adaptadores.entrada.PrecioRestController` · `cls_integracion_PrecioRestController` · clase · `KonradGourmet/src/co/edu/konradlorenz/integracion/adaptadores/entrada/PrecioRestController.java`
- Cabecera: «REST interno · apoya V13/V14»
- Atributos (1): `- casoDeUso: ConsultarPrecioUseCase`
- Métodos (1): `+ precioSugerido(productoId: Long): BigDecimal`

**M-153** · `integracion.adaptadores.entrada.AbastecimientoSoapEndpoint` · `cls_integracion_AbastecimientoSoapEndpoint` · clase · `KonradGourmet/src/co/edu/konradlorenz/integracion/adaptadores/entrada/AbastecimientoSoapEndpoint.java`
- Cabecera: «SOAP · @WebService (RNF integración)»
- Atributos (1): `- casoDeUso: ConsultarPrecioUseCase`
- Métodos (1): `+ consultarPrecioSugerido(productoId: Long): BigDecimal`

**M-154** · `integracion.adaptadores.salida.PrecioReferenciaCacheProxy` · `cls_integracion_PrecioReferenciaCacheProxy` · clase · `KonradGourmet/src/co/edu/konradlorenz/integracion/adaptadores/salida/PrecioReferenciaCacheProxy.java`
- Cabecera: «Proxy»
- Atributos (2): `- real: PrecioReferenciaPort`, `- cache: Map<Long, PrecioEstandar>`
- Métodos (1): `+ precioEstandar(productoId: Long): PrecioEstandar`

**M-155** · `integracion.adaptadores.salida.MinisterioComercioAdapter` · `cls_integracion_MinisterioComercioAdapter` · clase · `KonradGourmet/src/co/edu/konradlorenz/integracion/adaptadores/salida/MinisterioComercioAdapter.java`
- Cabecera: «Adapter»
- Atributos (1): `- cliente: MinisterioSoapClient`
- Métodos (1): `+ precioEstandar(productoId: Long): PrecioEstandar`

**M-156** · `integracion.adaptadores.salida.MinisterioSoapClient` · `cls_integracion_MinisterioSoapClient` · clase · `KonradGourmet/src/co/edu/konradlorenz/integracion/adaptadores/salida/MinisterioSoapClient.java`
- Cabecera: «Adaptee · generado del WSDL»
- Atributos (1): `- endpoint: String`
- Métodos (1): `+ consultarPrecioEstandar(codigoProducto: String): double`

**M-157** · `administracion.dominio.ParametrosSistema` · `cls_administracion_ParametrosSistema` · clase · `KonradGourmet/src/co/edu/konradlorenz/administracion/dominio/ParametrosSistema.java`
- Cabecera: «Singleton»
- Atributos (2): `- instancia: ParametrosSistema` (estático), `- valores: Map<String, String>`
- Métodos (3): `+ getInstancia(): ParametrosSistema` (estático), `+ valor(clave: String): String`, `+ actualizar(clave: String, valor: String): void`

**M-158** · `administracion.dominio.RegistroAuditoria` · `cls_administracion_RegistroAuditoria` · clase · `KonradGourmet/src/co/edu/konradlorenz/administracion/dominio/RegistroAuditoria.java`
- Atributos (4): `- accion: String`, `- usuario: String`, `- fecha: LocalDate`, `- hora: LocalTime`
- Métodos (0): —

**M-159** · `administracion.dominio.Bitacora` · `cls_administracion_Bitacora` · clase · `KonradGourmet/src/co/edu/konradlorenz/administracion/dominio/Bitacora.java`
- Atributos (1): `- registros: List<RegistroAuditoria>`
- Métodos (1): `+ registrar(r: RegistroAuditoria): void`

**M-160** · `administracion.dominio.LogError` · `cls_administracion_LogError` · clase · `KonradGourmet/src/co/edu/konradlorenz/administracion/dominio/LogError.java`
- Atributos (3): `- servicio: String`, `- mensaje: String`, `- fechaHora: LocalDateTime`
- Métodos (0): —

**M-161** · `administracion.puertos.entrada.AdministracionUseCase` · `cls_administracion_AdministracionUseCase` · «interface» · `KonradGourmet/src/co/edu/konradlorenz/administracion/puertos/entrada/AdministracionUseCase.java`
- Atributos (0): —
- Métodos (5): `+ parametrizar(clave: String, valor: String): void`, `+ registrarAuditoria(r: RegistroAuditoria): void`, `+ auditoria(desde: LocalDate, hasta: LocalDate): List<RegistroAuditoria>`, `+ logs(servicio: String): List<LogError>`, `+ tema(): Map<String, String>`

**M-162** · `administracion.aplicacion.AdministracionService` · `cls_administracion_AdministracionService` · clase · `KonradGourmet/src/co/edu/konradlorenz/administracion/aplicacion/AdministracionService.java`
- Atributos (3): `- repositorio: AuditoriaRepositoryPort`, `- logs: LogsPort`, `- parametros: ParametrosRepositoryPort`
- Métodos (5): `+ parametrizar(clave: String, valor: String): void`, `+ registrarAuditoria(r: RegistroAuditoria): void`, `+ auditoria(desde: LocalDate, hasta: LocalDate): List<RegistroAuditoria>`, `+ logs(servicio: String): List<LogError>`, `+ tema(): Map<String, String>`

**M-163** · `administracion.puertos.salida.AuditoriaRepositoryPort` · `cls_administracion_AuditoriaRepositoryPort` · «interface» · `KonradGourmet/src/co/edu/konradlorenz/administracion/puertos/salida/AuditoriaRepositoryPort.java`
- Atributos (0): —
- Métodos (2): `+ guardar(r: RegistroAuditoria): void`, `+ entre(desde: LocalDate, hasta: LocalDate): List<RegistroAuditoria>`

**M-164** · `administracion.adaptadores.entrada.AdminRestController` · `cls_administracion_AdminRestController` · clase · `KonradGourmet/src/co/edu/konradlorenz/administracion/adaptadores/entrada/AdminRestController.java`
- Cabecera: «REST · /api/admin · GET …/parametros/tema»
- Atributos (1): `- casoDeUso: AdministracionUseCase`
- Métodos (4): `+ parametrizar(clave: String, valor: String): void`, `+ auditoria(desde: LocalDate, hasta: LocalDate): List<RegistroAuditoria>`, `+ logs(servicio: String): List<LogError>`, `+ tema(): Map<String, String>`

**M-165** · `administracion.adaptadores.entrada.AuditoriaListener` · `cls_administracion_AuditoriaListener` · clase · `KonradGourmet/src/co/edu/konradlorenz/administracion/adaptadores/entrada/AuditoriaListener.java`
- Cabecera: «Listener · auditoria.evento»
- Atributos (1): `- casoDeUso: AdministracionUseCase`
- Métodos (1): `+ alRecibir(evento: EventoDominio): void`

**M-166** · `administracion.adaptadores.salida.AuditoriaJpaAdapter` · `cls_administracion_AuditoriaJpaAdapter` · clase · `KonradGourmet/src/co/edu/konradlorenz/administracion/adaptadores/salida/AuditoriaJpaAdapter.java`
- Atributos (1): `- bitacora: Bitacora`
- Métodos (2): `+ guardar(r: RegistroAuditoria): void`, `+ entre(desde: LocalDate, hasta: LocalDate): List<RegistroAuditoria>`

**M-167** · `analitica.dominio.CalculadorKpi` · `cls_analitica_CalculadorKpi` · «interface» · `KonradGourmet/src/co/edu/konradlorenz/analitica/dominio/CalculadorKpi.java` · ver C-44
- Cabecera: «Strategy»
- Atributos (0): —
- Métodos (2): `+ nombre(): String`, `+ calcular(hechos: List<HechoNegocio>): ResultadoKpi`

**M-168** · `analitica.dominio.ProductoMayorDemanda` · `cls_analitica_ProductoMayorDemanda` · clase · `KonradGourmet/src/co/edu/konradlorenz/analitica/dominio/ProductoMayorDemanda.java`
- Atributos (0): —
- Métodos (2): `+ nombre(): String`, `+ calcular(hechos: List<HechoNegocio>): ResultadoKpi`

**M-169** · `analitica.dominio.SucursalMayorDemanda` · `cls_analitica_SucursalMayorDemanda` · clase · `KonradGourmet/src/co/edu/konradlorenz/analitica/dominio/SucursalMayorDemanda.java`
- Atributos (0): —
- Métodos (2): `+ nombre(): String`, `+ calcular(hechos: List<HechoNegocio>): ResultadoKpi`

**M-170** · `analitica.dominio.ProveedorMejoresPrecios` · `cls_analitica_ProveedorMejoresPrecios` · clase · `KonradGourmet/src/co/edu/konradlorenz/analitica/dominio/ProveedorMejoresPrecios.java`
- Atributos (0): —
- Métodos (2): `+ nombre(): String`, `+ calcular(hechos: List<HechoNegocio>): ResultadoKpi`

**M-171** · `analitica.dominio.HechoNegocio` · `cls_analitica_HechoNegocio` · clase · `KonradGourmet/src/co/edu/konradlorenz/analitica/dominio/HechoNegocio.java`
- Atributos (5): `- tipo: String`, `- sucursalId: Long`, `- clave: String`, `- valor: BigDecimal`, `- fecha: LocalDate`
- Métodos (0): —

**M-172** · `analitica.dominio.ResultadoKpi` · `cls_analitica_ResultadoKpi` · clase · `KonradGourmet/src/co/edu/konradlorenz/analitica/dominio/ResultadoKpi.java`
- Atributos (3): `- kpi: String`, `- ganador: String`, `- valor: BigDecimal`
- Métodos (0): —

**M-173** · `analitica.eventos.TendenciaDetectadaEvent` · `cls_analitica_TendenciaDetectadaEvent` · clase · `KonradGourmet/src/co/edu/konradlorenz/analitica/eventos/TendenciaDetectadaEvent.java`
- Atributos (2): `- sugerencias: List<SugerenciaPromocion>`, `- fuente: String`
- Métodos (1): `+ tipo(): String`

**M-174** · `analitica.puertos.entrada.TableroUseCase` · `cls_analitica_TableroUseCase` · «interface» · `KonradGourmet/src/co/edu/konradlorenz/analitica/puertos/entrada/TableroUseCase.java`
- Atributos (0): —
- Métodos (6): `+ kpis(desde: LocalDate, hasta: LocalDate, sucursalId: Long): List<ResultadoKpi>`, `+ registrarHecho(h: HechoNegocio): void`, `+ detectarTendencias(): void`, `+ sugerencias(desde: LocalDate, hasta: LocalDate): List<SugerenciaPromocion>`, `+ aceptarSugerencia(id: Long): void`, `+ descartarSugerencia(id: Long): void`

**M-175** · `analitica.aplicacion.TableroService` · `cls_analitica_TableroService` · clase · `KonradGourmet/src/co/edu/konradlorenz/analitica/aplicacion/TableroService.java`
- Atributos (5): `- estrategias: List<CalculadorKpi>`, `- repositorio: KpiRepositoryPort`, `- redes: TendenciasPort`, `- eventos: EventPublisherPort`, `- sugerencias: SugerenciaRepositoryPort`
- Métodos (6): `+ kpis(desde: LocalDate, hasta: LocalDate, sucursalId: Long): List<ResultadoKpi>`, `+ registrarHecho(h: HechoNegocio): void`, `+ detectarTendencias(): void`, `+ sugerencias(desde: LocalDate, hasta: LocalDate): List<SugerenciaPromocion>`, `+ aceptarSugerencia(id: Long): void`, `+ descartarSugerencia(id: Long): void`

**M-176** · `analitica.puertos.salida.KpiRepositoryPort` · `cls_analitica_KpiRepositoryPort` · «interface» · `KonradGourmet/src/co/edu/konradlorenz/analitica/puertos/salida/KpiRepositoryPort.java`
- Atributos (0): —
- Métodos (2): `+ guardar(h: HechoNegocio): void`, `+ hechos(desde: LocalDate, hasta: LocalDate, sucursalId: Long): List<HechoNegocio>`

**M-177** · `analitica.puertos.salida.TendenciasPort` · `cls_analitica_TendenciasPort` · «interface» · `KonradGourmet/src/co/edu/konradlorenz/analitica/puertos/salida/TendenciasPort.java`
- Atributos (0): —
- Métodos (1): `+ productosEnTendencia(): List<String>`

**M-178** · `analitica.adaptadores.entrada.TableroRestController` · `cls_analitica_TableroRestController` · clase · `KonradGourmet/src/co/edu/konradlorenz/analitica/adaptadores/entrada/TableroRestController.java`
- Cabecera: «REST · /api/bam (Director comercial)»
- Atributos (1): `- casoDeUso: TableroUseCase`
- Métodos (1): `+ kpis(desde: LocalDate, hasta: LocalDate, sucursalId: Long): List<ResultadoKpi>`

**M-179** · `analitica.adaptadores.entrada.NegocioEventsListener` · `cls_analitica_NegocioEventsListener` · clase · `KonradGourmet/src/co/edu/konradlorenz/analitica/adaptadores/entrada/NegocioEventsListener.java`
- Cabecera: «Listener · 5 tópicos»
- Atributos (1): `- casoDeUso: TableroUseCase`
- Métodos (1): `+ alRecibir(evento: EventoDominio): void`

**M-180** · `analitica.adaptadores.entrada.TendenciasJob` · `cls_analitica_TendenciasJob` · clase · `KonradGourmet/src/co/edu/konradlorenz/analitica/adaptadores/entrada/TendenciasJob.java`
- Cabecera: «Scheduler · semanal»
- Atributos (1): `- casoDeUso: TableroUseCase`
- Métodos (1): `+ ejecutar(): void`

**M-181** · `analitica.adaptadores.salida.KpiJpaAdapter` · `cls_analitica_KpiJpaAdapter` · clase · `KonradGourmet/src/co/edu/konradlorenz/analitica/adaptadores/salida/KpiJpaAdapter.java`
- Atributos (1): `- hechos: List<HechoNegocio>`
- Métodos (2): `+ guardar(h: HechoNegocio): void`, `+ hechos(desde: LocalDate, hasta: LocalDate, sucursalId: Long): List<HechoNegocio>`

**M-182** · `analitica.adaptadores.salida.RedesSocialesAdapter` · `cls_analitica_RedesSocialesAdapter` · clase · `KonradGourmet/src/co/edu/konradlorenz/analitica/adaptadores/salida/RedesSocialesAdapter.java`
- Cabecera: «HTTPS → APIs de redes»
- Atributos (0): —
- Métodos (1): `+ productosEnTendencia(): List<String>`

**M-183** · `administracion.puertos.salida.LogsPort` · `cls_administracion_LogsPort` · «interface» · `KonradGourmet/src/co/edu/konradlorenz/administracion/puertos/salida/LogsPort.java` · ver C-30
- Atributos (0): —
- Métodos (1): `+ logs(servicio: String): List<LogError>`

**M-184** · `administracion.adaptadores.salida.ElasticsearchLogsAdapter` · `cls_administracion_ElasticsearchLogsAdapter` · clase · `KonradGourmet/src/co/edu/konradlorenz/administracion/adaptadores/salida/ElasticsearchLogsAdapter.java` · ver C-28
- Cabecera: «ELK · Elasticsearch · HTTPS»
- Atributos (0): —
- Métodos (1): `+ logs(servicio: String): List<LogError>`

**M-185** · `analitica.dominio.SugerenciaPromocion` · `cls_analitica_SugerenciaPromocion` · clase · `KonradGourmet/src/co/edu/konradlorenz/analitica/dominio/SugerenciaPromocion.java` · ver C-35
- Atributos (6): `- id: Long`, `- producto: String`, `- motivo: String`, `- vigenciaDesde: LocalDate`, `- vigenciaHasta: LocalDate`, `- estado: EstadoSugerencia`
- Métodos (2): `+ aceptar(): void`, `+ descartar(): void`

**M-186** · `analitica.dominio.EstadoSugerencia` · `cls_analitica_EstadoSugerencia` · «enumeration» · `KonradGourmet/src/co/edu/konradlorenz/analitica/dominio/EstadoSugerencia.java` · ver C-34
- Valores, en este orden: `SUGERIDA`, `ACEPTADA`, `DESCARTADA`

**M-187** · `analitica.puertos.salida.SugerenciaRepositoryPort` · `cls_analitica_SugerenciaRepositoryPort` · «interface» · `KonradGourmet/src/co/edu/konradlorenz/analitica/puertos/salida/SugerenciaRepositoryPort.java` · ver C-36
- Atributos (0): —
- Métodos (4): `+ guardar(s: SugerenciaPromocion): void`, `+ porId(id: Long): SugerenciaPromocion`, `+ historico(desde: LocalDate, hasta: LocalDate): List<SugerenciaPromocion>`, `+ notificadaEl(fecha: LocalDate): boolean`

**M-188** · `analitica.adaptadores.salida.SugerenciaJpaAdapter` · `cls_analitica_SugerenciaJpaAdapter` · clase · `KonradGourmet/src/co/edu/konradlorenz/analitica/adaptadores/salida/SugerenciaJpaAdapter.java` · ver C-33
- Atributos (0): —
- Métodos (4): `+ guardar(s: SugerenciaPromocion): void`, `+ porId(id: Long): SugerenciaPromocion`, `+ historico(desde: LocalDate, hasta: LocalDate): List<SugerenciaPromocion>`, `+ notificadaEl(fecha: LocalDate): boolean`

**M-189** · `analitica.adaptadores.entrada.PromocionRestController` · `cls_analitica_PromocionRestController` · clase · `KonradGourmet/src/co/edu/konradlorenz/analitica/adaptadores/entrada/PromocionRestController.java` · ver C-32
- Cabecera: «REST · /api/bam/promociones (Jefe de cocina)»
- Atributos (1): `- casoDeUso: TableroUseCase`
- Métodos (3): `+ sugerencias(desde: LocalDate, hasta: LocalDate): List<SugerenciaPromocion>`, `+ aceptar(id: Long): void`, `+ descartar(id: Long): void`

**M-190** · `administracion.puertos.salida.ParametrosRepositoryPort` · `cls_administracion_ParametrosRepositoryPort` · «interface» · `KonradGourmet/src/co/edu/konradlorenz/administracion/puertos/salida/ParametrosRepositoryPort.java` · ver C-31
- Atributos (0): —
- Métodos (2): `+ cargar(): Map<String, String>`, `+ guardar(clave: String, valor: String): void`

**M-191** · `administracion.adaptadores.salida.ParametrosJpaAdapter` · `cls_administracion_ParametrosJpaAdapter` · clase · `KonradGourmet/src/co/edu/konradlorenz/administracion/adaptadores/salida/ParametrosJpaAdapter.java` · ver C-29
- Atributos (0): —
- Métodos (2): `+ cargar(): Map<String, String>`, `+ guardar(clave: String, valor: String): void`


## 3. Relaciones `implements` / `extends` (R)

Esperadas: **82**. Las 65 flechas son las de triángulo hueco de 4a–4e sin contar la leyenda (`ley_l0` y `ley_l1` de cada página). Las 17 escritas están en la cabecera de la caja, y su destino está en la 4a (`compartido.eventos`).

Comprobado: cada clase que implementa o hereda declara en su caja exactamente los métodos que le exige su supertipo, con el mismo nombre, parámetros, retorno y visibilidad. Ninguna relación deja el código sin compilar.

| Id | Pág. | Flecha o celda | Clase | Relación | Supertipo | Estado hoy |
|---|---|---|---|---|---|---|
| R-01 | 4a | flecha `e6` | `compartido.adaptadores.KafkaEventPublisher` | `implements` | `compartido.puertos.EventPublisherPort` | presente |
| R-02 | 4a | flecha `e7` | `compartido.eventos.AuditoriaEvent` | `extends` | `compartido.eventos.EventoDominio` | presente |
| R-03 | 4a | flecha `e22` | `identidad.dominio.Mesero` | `extends` | `identidad.dominio.Usuario` | presente |
| R-04 | 4a | flecha `e23` | `identidad.dominio.JefeCocina` | `extends` | `identidad.dominio.Usuario` | presente |
| R-05 | 4a | flecha `e24` | `identidad.dominio.AuxiliarCompras` | `extends` | `identidad.dominio.Usuario` | presente |
| R-06 | 4a | flecha `e25` | `identidad.dominio.DirectorCompras` | `extends` | `identidad.dominio.Usuario` | presente |
| R-07 | 4a | flecha `e26` | `identidad.dominio.DirectorComercial` | `extends` | `identidad.dominio.Usuario` | presente |
| R-08 | 4a | flecha `e27` | `identidad.dominio.Administrador` | `extends` | `identidad.dominio.Usuario` | presente |
| R-09 | 4a | flecha `e28` | `identidad.dominio.OpcionMenu` | `extends` | `identidad.dominio.ElementoMenu` | falta |
| R-10 | 4a | flecha `e29` | `identidad.dominio.GrupoMenu` | `extends` | `identidad.dominio.ElementoMenu` | falta |
| R-11 | 4a | flecha `e30` | `identidad.aplicacion.IniciarSesionService` | `implements` | `identidad.puertos.entrada.IniciarSesionUseCase` | falta |
| R-12 | 4a | flecha `e31` | `identidad.adaptadores.salida.UsuarioJpaAdapter` | `implements` | `identidad.puertos.salida.UsuarioRepositoryPort` | falta |
| R-13 | 4a | flecha `e32` | `identidad.adaptadores.salida.BCryptCifradorAdapter` | `implements` | `identidad.puertos.salida.CifradorPort` | falta |
| R-14 | 4a | flecha `e_h15_impl` | `identidad.aplicacion.RegistrarUsuarioService` | `implements` | `identidad.puertos.entrada.RegistrarUsuarioUseCase` | falta |
| R-15 | 4b | flecha `e17` | `pedidos.aplicacion.PedidoService` | `implements` | `pedidos.puertos.entrada.RegistrarPedidoUseCase` | falta |
| R-16 | 4b | flecha `e18` | `pedidos.adaptadores.salida.PedidoJpaAdapter` | `implements` | `pedidos.puertos.salida.PedidoRepositoryPort` | falta |
| R-17 | 4b | flecha `e34` | `menu.aplicacion.PlatoService` | `implements` | `menu.puertos.entrada.RegistrarPlatoUseCase` | falta |
| R-18 | 4b | flecha `e35` | `menu.adaptadores.salida.PlatoJpaAdapter` | `implements` | `menu.puertos.salida.PlatoRepositoryPort` | falta |
| R-19 | 4b | flecha `e51` | `inventario.aplicacion.InventarioService` | `implements` | `inventario.puertos.entrada.GestionarInventarioUseCase` | falta |
| R-20 | 4b | flecha `e52` | `inventario.adaptadores.salida.InventarioJpaAdapter` | `implements` | `inventario.puertos.salida.InventarioRepositoryPort` | falta |
| R-21 | 4b | flecha `e_h3_impl` | `pedidos.adaptadores.salida.CatalogoJpaAdapter` | `implements` | `pedidos.puertos.salida.CatalogoRepositoryPort` | falta |
| R-22 | 4b | escrita en `cls_inventario_InventarioEventsListener` | `inventario.adaptadores.entrada.InventarioEventsListener` | `implements` | `compartido.eventos.EventListener<EventoDominio>` | falta |
| R-23 | 4b | escrita en `cls_inventario_PedidoRegistradoListener` | `inventario.adaptadores.entrada.PedidoRegistradoListener` | `implements` | `compartido.eventos.EventListener<EventoDominio>` | falta |
| R-24 | 4b | escrita en `cls_inventario_StockBajoEvent` | `inventario.eventos.StockBajoEvent` | `extends` | `compartido.eventos.EventoDominio` | presente |
| R-25 | 4b | escrita en `cls_menu_MenuActualizadoEvent` | `menu.eventos.MenuActualizadoEvent` | `extends` | `compartido.eventos.EventoDominio` | presente |
| R-26 | 4b | escrita en `cls_pedidos_MenuActualizadoListener` | `pedidos.adaptadores.entrada.MenuActualizadoListener` | `implements` | `compartido.eventos.EventListener<EventoDominio>` | falta |
| R-27 | 4b | escrita en `cls_pedidos_PedidoRegistradoEvent` | `pedidos.eventos.PedidoRegistradoEvent` | `extends` | `compartido.eventos.EventoDominio` | presente |
| R-28 | 4c | flecha `e19` | `solicitudes.aplicacion.SolicitudService` | `implements` | `solicitudes.puertos.entrada.GestionarSolicitudUseCase` | falta |
| R-29 | 4c | flecha `e20` | `solicitudes.adaptadores.salida.SolicitudJpaAdapter` | `implements` | `solicitudes.puertos.salida.SolicitudRepositoryPort` | falta |
| R-30 | 4c | flecha `e42` | `cotizaciones.dominio.ReglaRechazada` | `extends` | `cotizaciones.dominio.ReglaValidacion` | **al revés**: el código tiene `implements` |
| R-31 | 4c | flecha `e43` | `cotizaciones.dominio.ReglaSospechosa` | `extends` | `cotizaciones.dominio.ReglaValidacion` | **al revés**: el código tiene `implements` |
| R-32 | 4c | flecha `e44` | `cotizaciones.dominio.ReglaOpcionada` | `extends` | `cotizaciones.dominio.ReglaValidacion` | **al revés**: el código tiene `implements` |
| R-33 | 4c | flecha `e45` | `cotizaciones.aplicacion.CotizacionService` | `implements` | `cotizaciones.puertos.entrada.GestionarCotizacionUseCase` | falta |
| R-34 | 4c | flecha `e46` | `cotizaciones.adaptadores.salida.CotizacionJpaAdapter` | `implements` | `cotizaciones.puertos.salida.CotizacionRepositoryPort` | falta |
| R-35 | 4c | flecha `e47` | `cotizaciones.adaptadores.salida.PrecioSugeridoRestAdapter` | `implements` | `cotizaciones.puertos.salida.PrecioSugeridoPort` | falta |
| R-36 | 4c | flecha `e48` | `cotizaciones.adaptadores.salida.PdfCotizacionAdapter` | `implements` | `cotizaciones.puertos.salida.GeneradorPdfPort` | falta |
| R-37 | 4c | flecha `e_d1_impl` | `cotizaciones.adaptadores.salida.AlmacenObjetosAdapter` | `implements` | `cotizaciones.puertos.salida.AlmacenArchivosPort` | falta |
| R-38 | 4c | flecha `e_dec07_impl` | `cotizaciones.aplicacion.ProveedorService` | `implements` | `cotizaciones.puertos.entrada.GestionarProveedorUseCase` | falta |
| R-39 | 4c | flecha `e_dec07_jpa` | `cotizaciones.adaptadores.salida.ProveedorJpaAdapter` | `implements` | `cotizaciones.puertos.salida.ProveedorRepositoryPort` | falta |
| R-40 | 4c | escrita en `cls_cotizaciones_CotizacionEnviadaEvent` | `cotizaciones.eventos.CotizacionEnviadaEvent` | `extends` | `compartido.eventos.EventoDominio` | presente |
| R-41 | 4c | escrita en `cls_cotizaciones_CotizacionValidadaEvent` | `cotizaciones.eventos.CotizacionValidadaEvent` | `extends` | `compartido.eventos.EventoDominio` | presente |
| R-42 | 4c | escrita en `cls_cotizaciones_OrdenGeneradaListener` | `cotizaciones.adaptadores.entrada.OrdenGeneradaListener` | `implements` | `compartido.eventos.EventListener<EventoDominio>` | falta |
| R-43 | 4c | escrita en `cls_solicitudes_CotizacionEventsListener` | `solicitudes.adaptadores.entrada.CotizacionEventsListener` | `implements` | `compartido.eventos.EventListener<EventoDominio>` | falta |
| R-44 | 4c | escrita en `cls_solicitudes_SolicitudEnTramiteEvent` | `solicitudes.eventos.SolicitudEnTramiteEvent` | `extends` | `compartido.eventos.EventoDominio` | presente |
| R-45 | 4c | escrita en `cls_solicitudes_SolicitudRegistradaEvent` | `solicitudes.eventos.SolicitudRegistradaEvent` | `extends` | `compartido.eventos.EventoDominio` | presente |
| R-46 | 4d | flecha `e18` | `ordenes.aplicacion.OrdenCompraService` | `implements` | `ordenes.puertos.entrada.GenerarOrdenUseCase` | falta |
| R-47 | 4d | flecha `e19` | `ordenes.adaptadores.salida.OrdenJpaAdapter` | `implements` | `ordenes.puertos.salida.OrdenRepositoryPort` | falta |
| R-48 | 4d | flecha `e20` | `ordenes.adaptadores.salida.CotizacionRestClient` | `implements` | `ordenes.puertos.salida.CotizacionConsultaPort` | falta |
| R-49 | 4d | flecha `e36` | `notificaciones.dominio.NotificacionCorreo` | `implements` | `notificaciones.dominio.Notificacion` | **al revés**: el código tiene `extends` |
| R-50 | 4d | flecha `e37` | `notificaciones.dominio.AlarmaApp` | `implements` | `notificaciones.dominio.Notificacion` | **al revés**: el código tiene `extends` |
| R-51 | 4d | flecha `e38` | `notificaciones.dominio.CreadorCorreo` | `extends` | `notificaciones.dominio.CreadorNotificacion` | presente |
| R-52 | 4d | flecha `e39` | `notificaciones.dominio.CreadorAlarmaApp` | `extends` | `notificaciones.dominio.CreadorNotificacion` | presente |
| R-53 | 4d | flecha `e40` | `notificaciones.adaptadores.salida.CorreoDecorator` | `implements` | `notificaciones.puertos.salida.Correo` | falta |
| R-54 | 4d | flecha `e41` | `notificaciones.adaptadores.salida.CorreoCertificadoDecorator` | `extends` | `notificaciones.adaptadores.salida.CorreoDecorator` | presente en el paquete viejo: se arregla al mover C-39 |
| R-55 | 4d | flecha `e42` | `notificaciones.adaptadores.salida.EstampaCronologicaDecorator` | `extends` | `notificaciones.adaptadores.salida.CorreoDecorator` | presente en el paquete viejo: se arregla al mover C-40 |
| R-56 | 4d | flecha `e43` | `notificaciones.aplicacion.NotificacionService` | `implements` | `notificaciones.puertos.entrada.NotificarUseCase` | falta |
| R-57 | 4d | flecha `e44` | `notificaciones.adaptadores.salida.CorreoSmtpAdapter` | `implements` | `notificaciones.puertos.salida.Correo` | falta |
| R-58 | 4d | flecha `e45` | `notificaciones.adaptadores.salida.AlarmaPushAdapter` | `implements` | `notificaciones.puertos.salida.AlarmaPushPort` | falta |
| R-59 | 4d | flecha `e46` | `notificaciones.adaptadores.salida.EnvioJpaAdapter` | `implements` | `notificaciones.puertos.salida.EnvioRepositoryPort` | falta |
| R-60 | 4d | flecha `e_h4_almimpl` | `ordenes.adaptadores.salida.AlmacenObjetosAdapter` | `implements` | `ordenes.puertos.salida.AlmacenArchivosPort` | falta |
| R-61 | 4d | flecha `e_h4_pdfimpl` | `ordenes.adaptadores.salida.PdfOrdenAdapter` | `implements` | `ordenes.puertos.salida.GeneradorPdfPort` | falta |
| R-62 | 4d | flecha `e_n4_impl` | `notificaciones.adaptadores.salida.AlmacenObjetosAdapter` | `implements` | `notificaciones.puertos.salida.AlmacenArchivosPort` | falta |
| R-63 | 4d | flecha `e_n5_impl` | `notificaciones.adaptadores.salida.DestinatarioJpaAdapter` | `implements` | `notificaciones.puertos.salida.DestinatarioRepositoryPort` | falta |
| R-64 | 4d | escrita en `cls_notificaciones_NotificacionListeners` | `notificaciones.adaptadores.entrada.NotificacionListeners` | `implements` | `compartido.eventos.EventListener<EventoDominio>` | falta |
| R-65 | 4d | escrita en `cls_ordenes_OrdenGeneradaEvent` | `ordenes.eventos.OrdenGeneradaEvent` | `extends` | `compartido.eventos.EventoDominio` | presente |
| R-66 | 4e | flecha `e13` | `integracion.aplicacion.ConsultarPrecioService` | `implements` | `integracion.puertos.entrada.ConsultarPrecioUseCase` | falta |
| R-67 | 4e | flecha `e14` | `integracion.adaptadores.salida.PrecioReferenciaCacheProxy` | `implements` | `integracion.puertos.salida.PrecioReferenciaPort` | falta |
| R-68 | 4e | flecha `e15` | `integracion.adaptadores.salida.MinisterioComercioAdapter` | `implements` | `integracion.puertos.salida.PrecioReferenciaPort` | falta |
| R-69 | 4e | flecha `e28` | `administracion.aplicacion.AdministracionService` | `implements` | `administracion.puertos.entrada.AdministracionUseCase` | falta |
| R-70 | 4e | flecha `e29` | `administracion.adaptadores.salida.AuditoriaJpaAdapter` | `implements` | `administracion.puertos.salida.AuditoriaRepositoryPort` | falta |
| R-71 | 4e | flecha `e46` | `analitica.dominio.ProductoMayorDemanda` | `implements` | `analitica.dominio.CalculadorKpi` | **al revés**: el código tiene `extends` |
| R-72 | 4e | flecha `e47` | `analitica.dominio.SucursalMayorDemanda` | `implements` | `analitica.dominio.CalculadorKpi` | **al revés**: el código tiene `extends` |
| R-73 | 4e | flecha `e48` | `analitica.dominio.ProveedorMejoresPrecios` | `implements` | `analitica.dominio.CalculadorKpi` | **al revés**: el código tiene `extends` |
| R-74 | 4e | flecha `e49` | `analitica.aplicacion.TableroService` | `implements` | `analitica.puertos.entrada.TableroUseCase` | falta |
| R-75 | 4e | flecha `e50` | `analitica.adaptadores.salida.KpiJpaAdapter` | `implements` | `analitica.puertos.salida.KpiRepositoryPort` | falta |
| R-76 | 4e | flecha `e51` | `analitica.adaptadores.salida.RedesSocialesAdapter` | `implements` | `analitica.puertos.salida.TendenciasPort` | falta |
| R-77 | 4e | flecha `e_d8_impl` | `analitica.adaptadores.salida.SugerenciaJpaAdapter` | `implements` | `analitica.puertos.salida.SugerenciaRepositoryPort` | falta |
| R-78 | 4e | flecha `e_h14_impl` | `administracion.adaptadores.salida.ElasticsearchLogsAdapter` | `implements` | `administracion.puertos.salida.LogsPort` | falta |
| R-79 | 4e | flecha `e_n7_impl` | `administracion.adaptadores.salida.ParametrosJpaAdapter` | `implements` | `administracion.puertos.salida.ParametrosRepositoryPort` | falta |
| R-80 | 4e | escrita en `cls_administracion_AuditoriaListener` | `administracion.adaptadores.entrada.AuditoriaListener` | `implements` | `compartido.eventos.EventListener<EventoDominio>` | falta |
| R-81 | 4e | escrita en `cls_analitica_NegocioEventsListener` | `analitica.adaptadores.entrada.NegocioEventsListener` | `implements` | `compartido.eventos.EventListener<EventoDominio>` | falta |
| R-82 | 4e | escrita en `cls_analitica_TendenciaDetectadaEvent` | `analitica.eventos.TendenciaDetectadaEvent` | `extends` | `compartido.eventos.EventoDominio` | presente |

Recuento: {'presente': 19, 'falta': 53, 'al revés': 8, 'paquete viejo': 2}.

## 4. Tipos (T)

| Id | Qué | Detalle | Acción |
|---|---|---|---|
| T-01 | Todos los tipos de 4a–4e existen | Cada tipo que nombra un atributo, parámetro o retorno es del JDK o es una clase del mismo servicio o de `compartido`. No hay `List`/`Map`/`Set` sin parámetro, ni `Money`, ni tipos huérfanos. El esqueleto de prueba compila con `-Xlint:all` sin avisos | Ninguna |
| T-02 | Primitivos y `java.lang` | `int`, `boolean`, `double`, `byte[]`, `String`, `Long`, `Integer`, `void` | Sin import |
| T-03 | Imports del JDK | 8 tipos; la lista de clases que usa cada uno está debajo de la tabla | Import explícito, nunca `java.util.*` (T-09) |
| T-04 | Imports de `compartido` (librería común, permitidos) | `compartido.puertos.EventPublisherPort` en los 13 servicios de aplicación que publican; `compartido.eventos.EventoDominio` en los 9 eventos, los 8 listeners, `NotificarUseCase` y `NotificacionService`; `compartido.eventos.EventListener` en los 8 listeners | Ninguna |
| T-05 | Dependencias entre microservicios | **Ninguna firma de 4a–4e obliga a importar otro servicio.** Comprobado con los imports del esqueleto de prueba. Los «REST cliente» (`PrecioSugeridoRestAdapter`, `CotizacionRestClient`) usan solo tipos propios (`BigDecimal`, `CotizacionOpcionada`) | Ninguna en la sesión 3. En la sesión 4, los listeners que lean datos de un evento ajeno tendrán que importar su clase (Q5) |
| T-06 | Eventos con tipos del dominio | `CotizacionValidadaEvent.estado: EstadoCotizacion` (`cotizaciones.dominio`), que consume ms-analitica, y `TendenciaDetectadaEvent.sugerencias: List<SugerenciaPromocion>` (`analitica.dominio`), que consume ms-notificaciones. Si el consumidor lee el evento, importa el dominio de otro servicio | Q5 |
| T-07 | El dominio importa puertos | En ms-notificaciones, `NotificacionCorreo` y `CreadorCorreo` tienen `- correo: Correo`, y `AlarmaApp` y `CreadorAlarmaApp` tienen `- canal: AlarmaPushPort`. Los dos tipos están en `puertos.salida`, así que 4 clases de `dominio` importan `puertos.salida`. El README (línea 21) dice "El dominio no conoce a nadie". Desde H25, también `Correo` es un puerto | Q4 |
| T-08 | Nombres repetidos entre servicios | `AlmacenArchivosPort` ×3, `AlmacenObjetosAdapter` ×3 y `GeneradorPdfPort` ×2. Cada uno se usa solo dentro de su servicio, así que no hay ambigüedad de import | Un archivo por servicio (C-xx) |
| T-09 | Choque con el JDK | `EventListener` se llama igual que `java.util.EventListener` | No usar `import java.util.*` |
| T-10 | Anotaciones que no están en el JDK | `@WebService` (JAX-WS salió del JDK en Java 11) y las de Spring (`@RestController`, `@KafkaListener`…). El reporte (p. 26) promete compilar «solo con javac» | Van en Javadoc, no como anotación (J-04) |
| T-11 | Colecciones en memoria en adaptadores de persistencia | `CotizacionJpaAdapter.datos: Map<String, Cotizacion>`, `KpiJpaAdapter.hechos: List<HechoNegocio>` y `AuditoriaJpaAdapter.bitacora: Bitacora` vienen del original. Compilan; los otros 12 `*JpaAdapter` (de 15) no tienen atributos | Copiar tal cual (Q6) |
| T-12 | Caché del Proxy en memoria | `PrecioReferenciaCacheProxy.cache: Map<Long, PrecioEstandar>` | DEC-22 (sección 7) |
| T-13 | La unidad cambia de tipo entre servicios | `UnidadMedida` es una «enumeration» en ms-menu y un `String` en solicitudes, cotizaciones, ordenes e inventario | Ninguna: así nadie importa el enum de ms-menu, y cuadra con `n_recetas` |
| T-14 | El Adaptee usa otros tipos | `MinisterioSoapClient.consultarPrecioEstandar(codigoProducto: String): double` frente a `Long` y `BigDecimal` del puerto | Ninguna: es lo que traduce el Adapter |
| T-15 | Versión de Java | El reporte (p. 26) y la 3a dicen Java 21; el `javac` instalado es el 27 | Compilar con `--release 21` (J-12) |

**Imports del JDK que necesita cada clase (T-03):**

- `java.math.BigDecimal` (40): `HechoNegocio`, `ResultadoKpi`, `CotizacionRestController`, `PrecioSugeridoRestAdapter`, `CotizacionService`, `Cotizacion`, `ItemCotizacion`, `ReglaOpcionada`, `ReglaRechazada`, `ReglaSospechosa`, `ReglaValidacion`, `CotizacionEnviadaEvent`, `CotizacionValidadaEvent`, `GestionarCotizacionUseCase`, `PrecioSugeridoPort`, `AbastecimientoSoapEndpoint`, `PrecioRestController`, `ConsultarPrecioService`, `PrecioEstandar`, `ConsultarPrecioUseCase`, `InventarioJpaAdapter`, `InsumoInventario`, `RecetaPlato`, `UmbralStock`, `StockBajoEvent`, `InventarioRepositoryPort`, `Ingrediente`, `Plato`, `MenuActualizadoEvent`, `ItemOrden`, `OrdenCompra`, `OrdenGeneradaEvent`, `CatalogoPlatos`, `DetallePedido`, `Pedido`, `PlatoCatalogo`, `SolicitudService`, `ItemSolicitud`, `SolicitudAlimentos`, `GestionarSolicitudUseCase`
- `java.time.LocalDate` (19): `AdminRestController`, `AuditoriaJpaAdapter`, `AdministracionService`, `RegistroAuditoria`, `AdministracionUseCase`, `AuditoriaRepositoryPort`, `PromocionRestController`, `TableroRestController`, `KpiJpaAdapter`, `SugerenciaJpaAdapter`, `TableroService`, `HechoNegocio`, `SugerenciaPromocion`, `TableroUseCase`, `KpiRepositoryPort`, `SugerenciaRepositoryPort`, `Cotizacion`, `OrdenCompra`, `CriteriosConsulta`
- `java.time.LocalDateTime` (7): `LogError`, `EventoDominio`, `PrecioEstandar`, `EnvioJpaAdapter`, `EnvioRepositoryPort`, `Pedido`, `SolicitudAlimentos`
- `java.time.LocalTime` (1): `RegistroAuditoria`
- `java.util.List` (79): `AdminRestController`, `AuditoriaJpaAdapter`, `ElasticsearchLogsAdapter`, `AdministracionService`, `Bitacora`, `AdministracionUseCase`, `AuditoriaRepositoryPort`, `LogsPort`, `PromocionRestController`, `TableroRestController`, `KpiJpaAdapter`, `RedesSocialesAdapter`, `SugerenciaJpaAdapter`, `TableroService`, `CalculadorKpi`, `ProductoMayorDemanda`, `ProveedorMejoresPrecios`, `SucursalMayorDemanda`, `TendenciaDetectadaEvent`, `TableroUseCase`, `KpiRepositoryPort`, `SugerenciaRepositoryPort`, `TendenciasPort`, `CotizacionRestController`, `ProveedorRestController`, `CotizacionJpaAdapter`, `ProveedorJpaAdapter`, `CotizacionService`, `ProveedorService`, `Cotizacion`, `Proveedor`, `CotizacionEnviadaEvent`, `GestionarCotizacionUseCase`, `GestionarProveedorUseCase`, `CotizacionRepositoryPort`, `ProveedorRepositoryPort`, `ElementoMenu`, `GrupoMenu`, `Sucursal`, `InventarioRestController`, `InventarioJpaAdapter`, `InventarioService`, `Inventario`, `GestionarInventarioUseCase`, `InventarioRepositoryPort`, `PlatoRestController`, `PlatoJpaAdapter`, `PlatoService`, `Categoria`, `Plato`, `RegistrarPlatoUseCase`, `PlatoRepositoryPort`, `DestinatarioRestController`, `DestinatarioJpaAdapter`, `NotificacionService`, `NotificarUseCase`, `DestinatarioRepositoryPort`, `OrdenCompraRestController`, `OrdenJpaAdapter`, `OrdenCompraService`, `CotizacionOpcionada`, `OrdenCompra`, `OrdenGeneradaEvent`, `GenerarOrdenUseCase`, `OrdenRepositoryPort`, `PedidoRestController`, `PedidoJpaAdapter`, `PedidoService`, `CatalogoPlatos`, `Pedido`, `RegistrarPedidoUseCase`, `PedidoRepositoryPort`, `SolicitudRestController`, `SolicitudJpaAdapter`, `SolicitudService`, `SolicitudAlimentos`, `SolicitudRegistradaEvent`, `GestionarSolicitudUseCase`, `SolicitudRepositoryPort`
- `java.util.Map` (23): `AdminRestController`, `ParametrosJpaAdapter`, `AdministracionService`, `ParametrosSistema`, `AdministracionUseCase`, `ParametrosRepositoryPort`, `CotizacionRestController`, `CotizacionJpaAdapter`, `CotizacionService`, `CotizacionEnviadaEvent`, `GestionarCotizacionUseCase`, `PrecioReferenciaCacheProxy`, `InventarioJpaAdapter`, `InventarioService`, `RecetaPlato`, `GestionarInventarioUseCase`, `InventarioRepositoryPort`, `MenuActualizadoEvent`, `Mensaje`, `CatalogoPlatos`, `PedidoRegistradoEvent`, `SolicitudService`, `GestionarSolicitudUseCase`
- `java.util.Set` (1): `SolicitudAlimentos`
- `java.util.UUID` (1): `EventoDominio`

## 5. §15 del reporte frente a 4a–4e (W)

Manda 4a–4e. **Ningún nombre de método de la §15 es distinto del de 4a–4e.** Las diferencias son métodos ‹Vxx› de 4a–4e que la §15 no lista, más los métodos que no tienen ventana. Ninguna trae duda: se anotan para `CORRECCIONES_REPORTE.md` (sesión 5).

| Id | V | §15: método y ruta | 4a–4e: método ‹V› | Estado |
|---|---|---|---|---|
| W-01 | V01 | `AuthRestController.iniciarSesion()` · POST /api/auth/token | `iniciarSesion(correo, clave)` ‹V01› | Coincide |
| W-02 | V02 | `PedidoRestController.pedidosDelTurno()` · GET /api/pedidos?turno=actual | `pedidosDelTurno(meseroId)` ‹V02› | Coincide. El `meseroId` sale del JWT, no de la URL (anotarlo en el Javadoc) |
| W-03 | V03 | `PedidoRestController.registrar()` · POST /api/pedidos | `registrar(p)` ‹V03 · V04›, `catalogo(sucursalId)` ‹V03›, `mesas(sucursalId)` ‹V03› | **Faltan en la §15** `catalogo()` (GET /api/pedidos/catalogo) y `mesas()` (GET /api/pedidos/mesas), por H6 (mapa `v8`) |
| W-04 | V04 | `registrar()` → 201 · PED-0251 | `registrar(p)` ‹V03 · V04› | Coincide |
| W-05 | V05 | `InventarioRestController.alertas()` · GET /api/inventario/alertas | `alertas(sucursalId)` ‹V05› | Coincide. `AlarmaPushAdapter` también cita V05 en su cabecera («WebSocket → V05»), sin ‹V› |
| W-06 | V06 | `PlatoRestController.crear()` · POST /api/platos | `crear(p)` ‹V06›, `productosPorCategoria(categoriaId)` ‹V06› | **Falta en la §15** `productosPorCategoria()`. Ninguna fuente fija su ruta |
| W-07 | V07 | `InventarioRestController.insumos()` · GET /api/inventario/insumos | `insumos(sucursalId)` ‹V07› | Coincide |
| W-08 | V08 | `SolicitudRestController.registrar()` · POST /api/solicitudes | `registrar(s)` ‹V08 · V09› | Coincide |
| W-09 | V09 | `registrar()` → 201 · SOL-031 | `registrar(s)` ‹V08 · V09› | Coincide |
| W-10 | V10 | `misSolicitudes()` · GET /api/solicitudes?solicitante=yo | `misSolicitudes(c)` ‹V10› | Coincide |
| W-11 | V11 | `consultar()` · GET /api/solicitudes?estado=PENDIENTE | `consultar(c)` ‹V11› | Coincide |
| W-12 | V12 | `detalle()` · GET /api/solicitudes/{id} | `detalle(numero)` ‹V12› | Coincide: `{id}` es el número (SOL-031) |
| W-13 | V13 | `CotizacionRestController.generar()` · POST /api/cotizaciones | `generar(c)` ‹V13›; `ProveedorRestController.consultar(soloActivos)` ‹V13› | **Falta en la §15** el desplegable de proveedores: GET /api/proveedores (DEC-07, mapa `v22`) |
| W-14 | V14 | `pdf()` · GET /api/cotizaciones/{id}/pdf | `pdf(numero)` ‹V14› | Coincide |
| W-15 | V15 | `consultar()` · GET /api/cotizaciones | `consultar(estado)` ‹V15 · V17› | Coincide |
| W-16 | V16 | `pendientesDeAtencion()` · GET /api/cotizaciones/pendientes | `pendientesDeAtencion()` ‹V16› | Coincide. No está en `GestionarCotizacionUseCase`: el cuerpo delegará en `consultar(estado)` (sesión 4) |
| W-17 | V17 | `consultar()` · GET /api/cotizaciones | `consultar(estado)` ‹V15 · V17› | Coincide |
| W-18 | V18 | `registrarValores()` · PUT /api/cotizaciones/{id}/valores | `registrarValores(numero, precios)` ‹V18› | Coincide |
| W-19 | V19 | `validar()` · POST /api/cotizaciones/{id}/validacion | `validar(numero)` ‹V19› | Coincide |
| W-20 | V20 | `opcionadas()` · GET /api/cotizaciones?estado=OPCIONADA | `opcionadas()` ‹V20›, `detalle(numero)` ‹V20 · V21› | **Falta en la §15** `detalle()`: GET /api/cotizaciones/{id} (H5) |
| W-21 | V21 | `OrdenCompraRestController.generar()` · POST /api/ordenes-compra | `generar(cotizacionNumero)` ‹V21 · V22›; `CotizacionRestController.detalle(numero)` ‹V20 · V21› | La §15 solo cita la orden; falta el detalle de la cotización (mapa `v32`) |
| W-22 | V22 | `generar()` → 201 · OC-0012 | `generar(cotizacionNumero)` ‹V21 · V22› | Coincide |
| W-23 | V23 | `OrdenCompraRestController.consultar()` · GET /api/ordenes-compra | `consultar(estado)` ‹V23› | Coincide |

**Métodos de controlador sin ventana.** La §15 solo traza ventanas, así que no los tiene. La sesión 5 puede sumarlos en una tabla aparte. La ruta sale de 4a–4e o del mapa (p. 1); donde ninguna fuente fija el verbo, el Javadoc lleva solo el prefijo (J-04).

| Id | Método | Ruta según las fuentes |
|---|---|---|
| W-24 | `UsuarioRestController.registrar(...)` | POST /api/usuarios (4a, mapa `v38b`) |
| W-25 | `PlatoRestController.consultar(sucursalId)` | Sin ruta fija; prefijo /api/platos |
| W-26 | `PlatoRestController.editar(id, p)` y `desactivar(id)` | PUT /api/platos/{id} · PATCH /api/platos/{id}/desactivar (4b, mapa `v38d`) |
| W-27 | `CotizacionRestController.reenviar(numero)` | POST /api/cotizaciones/{id}/reenvio (4c, mapa `v38g`) |
| W-28 | `ProveedorRestController.registrar`, `detalle`, `editar` e `inactivar` | /api/proveedores/**, sin verbo fijo (4c, mapa `v38f`) |
| W-29 | `DestinatarioRestController.registrar` y `consultar` | /api/notificaciones/destinatarios, sin verbo fijo (4d, mapa `v38c`) |
| W-30 | `AdminRestController.parametrizar`, `auditoria` y `logs` | /api/admin/**, sin verbo fijo (4e) |
| W-31 | `AdminRestController.tema()` | GET /api/admin/parametros/tema (4e, mapa `v38`) |
| W-32 | `TableroRestController.kpis(desde, hasta, sucursalId)` | GET /api/bam/kpis?desde&hasta&sucursalId (mapa `v37`; la 4e solo dice /api/bam) |
| W-33 | `PromocionRestController.sugerencias`, `aceptar` y `descartar` | /api/bam/promociones, sin verbo fijo (4e, mapa `v38e`) |
| W-34 | `PrecioRestController.precioSugerido(productoId)` | Interna, sin ruta en el gateway: la llama ms-cotizaciones con `[REST/JSON · mTLS]` (3a `c3a_e13`) |
| W-35 | `AbastecimientoSoapEndpoint.consultarPrecioSugerido(productoId)` | SOAP bajo /api/integracion/** (D2, sección 8) |

Dos observaciones de severidad baja, sin acción en la sesión 3:
- **W-36.** `GET /api/cotizaciones/pendientes` (V16) y `GET /api/cotizaciones/{id}` (H5) tienen la misma forma. Spring prefiere el segmento literal, así que funciona. Pero DEC-12 descartó `/api/cotizaciones/proveedores` justo por esa confusión: conviene anotar en DEC-12 que el motivo fue la claridad, no un choque técnico.
- **W-37.** `opcionadas()` y `consultar(estado)` atienden la misma ruta (GET /api/cotizaciones, una con `?estado=OPCIONADA`). En Spring se distinguen con `params`. No cambia firmas.

## 6. Tabla de patrones: README, reporte p. 22 y 4a–4e (P)

El plan pide que el README quede «igual a la p. 22». Pero la p. 22 tiene dos errores que la fase 2a ya corrigió en 4a–4e: H9 (Observer) y H13 (el umbral en el Singleton). Como manda 4a–4e, **el README debe seguir a 4a–4e, con el orden y las clases de la p. 22**. La p. 22 se corrige en la sesión 5.

| Id | Dónde | Qué está mal | Fuente que lo contradice | Corrección |
|---|---|---|---|---|
| P-01 | README l. 47 | Llama «Strategy» a la cadena de reglas (D10) | 4c `cls_cotizaciones_ReglaValidacion` «Chain of Responsibility · manejador»; p. 22 | Fila Chain of Responsibility (P-13) |
| P-02 | README l. 43–50 | Tiene 6 de los 10 patrones: faltan Composite, Singleton, Adapter y la Strategy real (H19) | p. 22; 4a, 4e | Tabla de P-13 |
| P-03 | README l. 46 | Decorator en `notificaciones/dominio` | 4d: `Correo` en `puertos.salida` (H25) y los decoradores en `adaptadores.salida` (N8) | Dos paquetes en la fila Decorator |
| P-04 | README l. 45 | Factory Method sin `Usuario.construirMenu()` | 4a `cls_identidad_Usuario` «Factory Method · creador»; p. 22 | Sumar ms-identidad |
| P-05 | README l. 50 | «Observer» en `compartido/eventos`. `EventPublisherPort` está en `compartido/puertos` | 4a: «Publish-Subscribe · publicador/suscriptor (Observer distribuido, vía Kafka)» (H9) | «Publish-Subscribe (Observer distribuido)», con sus 3 paquetes |
| P-06 | README l. 49 | Proxy sin decir que comparte la interfaz ni dónde vive la caché | 4e `e14`/`e15` (los dos implementan `PrecioReferenciaPort`); DEC-22 | Lo dice la fila de P-13 |
| P-07 | README l. 21 | «El dominio no conoce a nadie» | T-07: 4 clases de `notificaciones.dominio` usan puertos | Según Q4 |
| P-08 | README l. 18 | «`adaptadores/salida/` … Implementan los puertos de salida». `MinisterioSoapClient` (el Adaptee) no implementa ninguno | 4e `cls_integracion_MinisterioSoapClient` | Opcional: «…salvo el cliente SOAP generado del WSDL, que es el Adaptee» |
| P-09 | README l. 25 y 76 | La columna «Historias» lista ventanas (V01…), no HU. El comando de compilación no fija la versión | — | Opcional: «Ventanas» y `javac --release 21 -d out …` (J-12) |
| P-10 | Reporte p. 22 (sesión 5) | «Observer» (H9); el Singleton guarda el «umbral del 25 %» (H13); «el esqueleto Java implementa el mismo comportamiento» (H1, D12). «(cadenaPorDefecto)» pasa a ser cierto con la sesión 3 | 4a; 4e `n_parametros` | Anotar en `CORRECCIONES_REPORTE.md` |
| P-11 | Reporte §13, p. 26 (sesión 4 y 5) | La tabla de la demo promete `umbral.stock = 25 %` desde el Singleton, contra 4e `n_parametros` (H13). Además promete **7 salidas**, y el plan de la sesión 4 (D12) solo cubre la de COT-018 | 4e `n_parametros`; `PLAN_CORRECCIONES.md`, sesión 4 | Q7 |
| P-12 | Reporte p. 26 (sesión 5) | «155 clases, además de la clase DemoArquitectura» | Sección 0 | 191 (193 si DEC-22 = C) |

**P-13 · Tabla propuesta para el README** (reemplaza las líneas 43–50; mismo orden que la p. 22):

```markdown
| Patrón | Dónde | Clases |
|---|---|---|
| Facade | `cotizaciones/aplicacion` | `GenerarCotizacionFacade`; a escala de sistema, el API Gateway |
| Chain of Responsibility | `cotizaciones/dominio` | `ReglaValidacion` → `ReglaRechazada` → `ReglaSospechosa` → `ReglaOpcionada`; la cadena se arma en `ReglaValidacion.cadenaPorDefecto()` |
| Composite | `identidad/dominio` | `ElementoMenu` (componente), `GrupoMenu` (compuesto), `OpcionMenu` (hoja) |
| Factory Method | `identidad/dominio`, `notificaciones/dominio` | `Usuario.construirMenu()` en cada rol; `CreadorNotificacion` → `CreadorCorreo`, `CreadorAlarmaApp`, que crean una `Notificacion` |
| Decorator | `notificaciones/puertos/salida`, `notificaciones/adaptadores/salida` | `Correo` (componente), `CorreoSmtpAdapter` (componente concreto), `CorreoDecorator`, `CorreoCertificadoDecorator`, `EstampaCronologicaDecorator` |
| Adapter | `integracion/adaptadores/salida` | `MinisterioComercioAdapter` adapta `MinisterioSoapClient` a `PrecioReferenciaPort`; además, todos los `*Adapter` de la hexagonal |
| Proxy | `integracion/adaptadores/salida` | `PrecioReferenciaCacheProxy`: implementa `PrecioReferenciaPort`, como el adaptador real, y guarda la caché en Redis |
| Publish-Subscribe (Observer distribuido) | `compartido/puertos`, `compartido/eventos`, `compartido/adaptadores` y los listeners de cada servicio | `EventPublisherPort`, `KafkaEventPublisher`, `EventListener<T>`, `EventoDominio` y los `*Listener` |
| Singleton | `administracion/dominio` | `ParametrosSistema.getInstancia()`: tema, logos y parámetros generales |
| Strategy | `analitica/dominio` | `CalculadorKpi` → `ProductoMayorDemanda`, `SucursalMayorDemanda`, `ProveedorMejoresPrecios` |
```

Si Q4 se resuelve con la opción B, en la fila Factory Method `notificaciones/dominio` pasa a `notificaciones/aplicacion`.

## 7. DEC-22 · la caché del Proxy (4e)

**Situación.** En la 4e, `cls_integracion_PrecioReferenciaCacheProxy` tiene `- cache: Map<Long, PrecioEstandar>`. Eso es una caché en la memoria de cada réplica. Viene así desde `main`. En cambio, varias fuentes dicen que la caché está en Redis:
- la 3a: `c3a_db_integracion` «Caché de precios [Redis]» y `c3a_e20` «[Redis · TLS]»;
- el mapa, el despliegue y la p. 6 (`c6_v49`);
- el reporte: p. 12, «protegido por PrecioReferenciaCacheProxy con una caché en Redis», y p. 31, «Proxy con caché en Redis».

**Dos restricciones que pesan:**
- El código no usa frameworks y compila solo con `javac`, así que no puede declarar un tipo de cliente Redis.
- El reporte (§13, p. 26) promete que la demo lee «el primer precio desde el servicio web; el segundo, desde la caché del Proxy». Así que cualquier opción necesita una caché que funcione sin Redis en la demo.

| Opción | Cambios en la 4e | Código | Lo que cuesta |
|---|---|---|---|
| **A. Solo una nota** | Nota nueva `n_cache` con un conector punteado al Proxy (texto abajo). El atributo no cambia | Campo `Map<Long, PrecioEstandar> cache` que llega por el constructor, con el mismo texto en Javadoc. La demo le pasa un `HashMap` | 1 nota y 1 conector. Siguen 191 clases y 65 flechas. Quien solo mire la caja sigue viendo un `Map` en memoria |
| **B. Restricción en el atributo + la nota** ✅ | La línea pasa a `- cache: Map<Long, PrecioEstandar> {Redis}`, la misma notación que `{único}` en `OrdenCompra` (4d). Más la nota de A | Igual que A; el Javadoc del campo dice `{Redis}` | 1 línea, 1 nota y 1 conector. Siguen 191 clases y 65 flechas. La caja ya dice Redis |
| **C. Puerto de caché + adaptador Redis** | `PrecioCachePort` «interface» · puertos.salida (`+ obtener(productoId: Long): PrecioEstandar`, `+ guardar(p: PrecioEstandar): void`) y `RedisPrecioCacheAdapter` · adaptadores.salida «Redis · TLS», que lo implementa. El atributo pasa a `- cache: PrecioCachePort` | 2 clases más. La demo necesita su propio adaptador en memoria para el puerto | 193 clases, 66 flechas (83 relaciones), 1 asociación más. En el mapa, la caja del Proxy pasa a «PrecioReferenciaCacheProxy / RedisPrecioCacheAdapter» (DEC-15). Cambia la cifra de clases del reporte (sesión 5). Es lo más hexagonal, y la vigencia la maneja Redis |
| D. Un tipo de cliente Redis en el atributo | — | No compila sin dependencias | Descartada: rompe «solo javac» |

**Recomendación: B.**
- Es honesta: `Map` es una interfaz y la implementación que se inyecta decide dónde viven los datos. En producción sería un mapa respaldado por Redis; por ejemplo, el `RMap` de Redisson implementa `java.util.Map`. En la demo, un `HashMap`.
- No toca la cuenta de clases ni de flechas, y la caja dice Redis sin tener que leer la nota.
- C también se defiende, porque N4 ya aceptó que un adaptador use un puerto. Pero suma 2 clases y toca el mapa por una caché.

**Texto propuesto para la nota `n_cache`** (en el hueco libre sobre el Proxy, más o menos en x=60, y=300, 320×100; mismo estilo que `n_parametros`; el conector baja hasta el borde de arriba del Proxy sin cruzar nada):

> La caché vive en Redis y la comparten todas las réplicas de ms-integracion: `cache` es un mapa respaldado por Redis, no la memoria de la réplica. Cada precio vale un día según `consultadoEn` (supuesto S3): si venció o no está, el Proxy le pregunta a `real` y guarda la respuesta.

## 8. D2 · el `@WebService` se publica bajo `/api/integracion/**` (4e)

**Celda:** `cls_integracion_AbastecimientoSoapEndpoint` (4e, `p7`). Es la tercera línea de la cabecera, en azul `#2E6F8E` y a 8 px, y hoy dice «SOAP · @WebService (RNF integración)». La caja mide 349 px de ancho (unos 337 útiles) y esa línea, de 38 caracteres, ocupa unos 150.

- **Opción 1, recomendada (una línea de 60 caracteres, unos 240 px: cabe en el ancho actual):** `«SOAP · @WebService · /api/integracion/** (RNF integración)»`
- **Opción 2 (dos líneas, si se quiere decir «gateway»):** se deja la línea actual y se agrega debajo, con el mismo estilo, `«publicado por el API Gateway en /api/integracion/**»`. La caja crece unos 10 px; hay espacio hasta y=433.

**Javadoc de la clase en el código:**

```java
/**
 * «SOAP · @WebService · /api/integracion/** (RNF integración)»
 * Endpoint SOAP que ms-integracion expone, con su WSDL, a los sistemas externos.
 * Se publica a través del API Gateway bajo /api/integracion/**.
 * En el proyecto real lleva @WebService (JAX-WS); aquí va solo en este comentario,
 * porque JAX-WS no está en el JDK y el esqueleto compila solo con javac.
 */
```

No se debe escribir el número del prefijo («prefijo 13») en el diagrama: S12 quitó de las páginas los ids internos.

**Opcional, relacionado:** `cls_integracion_PrecioRestController` dice «REST interno · apoya V13/V14». Con `/api/integracion/**` publicado, conviene que el Javadoc de esa clase diga «No se publica en el gateway; lo llama ms-cotizaciones con mTLS (3a `c3a_e13`)». Así nadie entiende que el prefijo también expone el REST interno. En el diagrama no hace falta tocarla.

## 9. Detalles de Java que el diagrama no fija (J)

«Técnico» quiere decir que la sesión principal lo puede aplicar sin preguntar. «Decisión» quiere decir que va a Q.

| Id | Tema | Recomendación | Tipo |
|---|---|---|---|
| J-01 | Constructores | 4a–4e no dibuja constructores. Hacen falta para que algunos patrones estén bien construidos en el código: sin constructor privado, `ParametrosSistema` no es un Singleton; sin constructor que reciba el `Correo`, el Decorator no puede envolver nada. Propuesta: constructores de inyección en las 46 clases de aplicación o adaptadores que tienen colaboradores (servicios, controladores, listeners, jobs, fachada, Proxy, Adapter, decoradores) y en las 4 del Factory Method de ms-notificaciones; constructor `private` en `ParametrosSistema`; constructor `protected CorreoDecorator(Correo envuelto)`, con los de sus 2 subclases. Las entidades y los eventos, en la sesión 4, según lo que use la demo | Decisión (Q1) |
| J-02 | Accesores (getters) | Los atributos son privados y casi ninguna caja tiene accesores. Sin ellos, un consumidor no puede leer, por ejemplo, `OrdenGeneradaEvent.solicitudNumeros`. Si se agregan, que sigan el estilo de la 4a (`id()`, `usuario()`, como en un record) y no `getId()`. `getInstancia()` se queda como está, por ser el nombre habitual del Singleton | Decisión (Q1) |
| J-03 | Genéricos de `EventListener` | Copiar literal: `public interface EventListener<T extends EventoDominio> { void alRecibir(T evento); }`, y en los 8 listeners `implements EventListener<EventoDominio>` con `public void alRecibir(EventoDominio evento)` | Técnico |
| J-04 | Ruta, tecnología y patrón sin Spring | En el Javadoc de la clase va el texto literal de la cabecera, entre « ». Por ejemplo, «REST · /api/pedidos · GET …/catalogo · GET …/mesas» o «Chain of Responsibility · manejador». En el Javadoc de cada método va el verbo y la ruta **solo si una fuente la fija** (W-01 a W-35); si no, solo el prefijo de la clase. No se inventan rutas. Sin anotaciones (T-10) | Técnico |
| J-05 | Comentario de ventana | Una línea por ventana, con la frase exacta que el reporte dice que existe (p. 26): `// Atiende la ventana V03 de los mockups`. Si el método tiene ‹V03 · V04›, van dos líneas. Así, `grep "Atiende la ventana V04"` encuentra cada una de las 23. Son 29 líneas en 24 métodos, todos en controladores | Técnico, pero cambia la frase del plan (Q2) |
| J-06 | Enumeraciones | Los valores, en el orden de la celda (sección 2). Sin atributos ni constructor: el diagrama no tiene | Técnico |
| J-07 | Miembros estáticos (subrayados) | `private static ParametrosSistema instancia;`, `public static ParametrosSistema getInstancia()`, `public static ReglaValidacion cadenaPorDefecto()` y `public static OrdenCompra desde(CotizacionOpcionada c)`. Son los únicos 4 | Técnico |
| J-08 | Métodos abstractos (en cursiva) | Son 7: `EventoDominio.tipo()`, `Usuario.construirMenu()` y `rol()`, `ElementoMenu.esHoja()`, `ReglaValidacion.aplica()` y `estado()` (los dos `protected`) y `CreadorNotificacion.crearNotificacion()` (`protected`). Llevan `abstract`. El resto de los métodos de las clases abstractas, por ejemplo `ElementoMenu.agregar()` o `CorreoDecorator.enviar()`, llevan cuerpo | Técnico |
| J-09 | Visibilidad y `final` | `+` es `public`, `-` es `private` y `#` es `protected`. En las interfaces, sin modificador. `final` solo en los colaboradores que lleguen por constructor (si Q1 = A); los datos de las entidades, sin `final` | Técnico |
| J-10 | Restricciones y valores iniciales | `private int longitudMinima = 8;` literal. `{único}` va en el Javadoc del campo `OrdenCompra.cotizacionNumero`. `{8+ caracteres, con mayúscula, minúscula y número}` va en el Javadoc de `PoliticaContrasena`. `{Redis}`, según DEC-22 | Técnico |
| J-11 | Cuerpo de los métodos | `throw new UnsupportedOperationException("Pendiente de implementar");`. Mejor sin decir «sesión 4»: el profesor lee el código entregado y las sesiones son internas | Técnico |
| J-12 | Versión de Java | Compilar con `javac --release 21`, porque el reporte (p. 26) y la 3a dicen Java 21 y aquí está el `javac` 27. Llevar ese comando al README (P-09) | Técnico |
| J-13 | Imports por dependencias «publica» o «usa» | Las flechas de dependencia hacia eventos (por ejemplo, `PedidoService` → `PedidoRegistradoEvent`) no aparecen en ninguna firma. Con cuerpos que lanzan excepción, no se importan: llegan en la sesión 4 | Técnico |
| J-14 | Atributos que exige una asociación | Comprobado: las 62 flechas de asociación, agregación y composición (49 + 7 + 6) tienen su atributo escrito en la caja de origen. No hay que inventar ninguno. Algunos atributos tipados con clases no tienen flecha, como `KpiJpaAdapter.hechos`: es válido en UML | Técnico |
| J-15 | Multiplicidades y notas | Las etiquetas como `detalles · 1..*` y las notas `n_*` pueden ir como Javadoc en la clase o en el campo. Es opcional; ayuda en la sustentación | Técnico |
| J-16 | Nombres de parámetros | Literales, aunque cambien entre capas. Por ejemplo, `cotizacion` en `registrarCotizacion` y `yaAplicada`, o `c`, `p`, `s`. El diagrama ya los unificó en N3 | Técnico |
| J-17 | Clases «generadas» | `MinisterioSoapClient` («Adaptee · generado del WSDL») se escribe a mano como esqueleto. Su Javadoc dice que en el proyecto real lo genera `wsimport` o CXF a partir del WSDL del Ministerio | Técnico |
| J-18 | Movimientos | `git mv` para C-37 a C-40, en commits separados de los de contenido, para que el diff se lea bien (H25 y N8) | Técnico |
| J-19 | Javadoc en español | Como el resto del proyecto | Técnico |

## 10. Dudas para decidir (Q)

### Q1 · Constructores y accesores que 4a–4e no dibuja (J-01 y J-02)

**Qué pasa:** La regla dice «idéntico a 4a–4e», pero sin constructor privado el Singleton no lo es. Sin constructor, el Decorator y el Proxy no reciben lo que envuelven. Y sin accesores, ningún listener puede leer un evento.

- **A.** En el código: los constructores de J-01 ya en la sesión 3, y los accesores al estilo record solo donde los use la sesión 4. En 4a–4e, una línea en la leyenda `ley_conv` de las 5 páginas: «Se omiten constructores y métodos de acceso». Cuesta 5 líneas de leyenda y unos 53 constructores. Los patrones quedan bien construidos y la regla «idéntico» queda explícita.
- **B.** Dibujarlos en 4a–4e. Son cientos de líneas y cajas más grandes.
- **C.** Nada en la sesión 3; todo en la 4. Mientras tanto, el Singleton del código no es un Singleton, y si la sesión 4 no toca esa clase, se entrega así.

**Recomendación:** A.

### Q2 · Texto del comentario de ventana

**Qué pasa:** El plan dice `// Atiende la ventana Vxx`. El reporte (p. 26) dice que el código lleva «Atiende la ventana Vxx de los mockups», y el reporte no se toca (DEC-03). Además, 5 métodos atienden dos ventanas.

- **A.** La frase del reporte, una línea por ventana (29 líneas). Lo que dice el reporte queda literalmente cierto, y `grep` encuentra cada ventana.
- **B.** La frase del plan. Es más corta, pero no coincide con la que cita el reporte.
- **C.** Una línea con varias ventanas («Atiende las ventanas V03 y V04»). Un `grep` de «la ventana V04» no la encuentra.

**Recomendación:** A.

### Q3 · DEC-22: la caché del Proxy

**Qué pasa:** Sección 7.

- **A.** Solo una nota (191 clases).
- **B.** `{Redis}` en el atributo y la nota (191 clases).
- **C.** Puerto y adaptador Redis (193 clases, toca el mapa).

**Recomendación:** B.

### Q4 · En ms-notificaciones, el dominio depende de puertos (T-07)

**Qué pasa:** `NotificacionCorreo`, `CreadorCorreo`, `AlarmaApp` y `CreadorAlarmaApp` (en `dominio`) tienen atributos de tipo `Correo` y `AlarmaPushPort` (en `puertos.salida`). Con H25 esto creció. Contradice el README («El dominio no conoce a nadie») y es lo que encuentra un `grep import` por capa. No usa ningún framework.

- **A.** No cambiar 4d y reescribir la línea 21 del README: «El dominio no importa frameworks; en ms-notificaciones, el Factory Method usa las interfaces `Correo` y `AlarmaPushPort` del núcleo». Cuesta 1 línea. Se puede defender (en DDD, el dominio puede conocer interfaces del núcleo), pero un revisor estricto lo marca.
- **B.** Pasar las 6 clases del Factory Method de notificaciones (`Notificacion`, `NotificacionCorreo`, `AlarmaApp`, `CreadorNotificacion`, `CreadorCorreo` y `CreadorAlarmaApp`) de «dominio» a «aplicacion» en 4d. Es la primera línea de 6 cabeceras; el color es el mismo, porque la leyenda agrupa dominio y aplicación. En el código son 6 `git mv`, y la fila Factory Method del README cambia. El mapa no cambia: su núcleo dice «dominio + casos de uso». La regla «las dependencias apuntan hacia adentro» se cumple sin excepciones.
- **C.** Volver a poner `Correo` en el dominio. Deshace H25, y `AlarmaPushPort` sigue igual.

**Recomendación:** B. Es barata ahora, porque la sesión 3 reescribe esos archivos de todos modos, y deja el `grep` por capas limpio. Si Mauricio prefiere no tocar la 4d aprobada, A es aceptable.

### Q5 · Eventos que cruzan servicios (para la sesión 4; T-05 y T-06)

**Qué pasa:** Para leer los datos de un evento ajeno, un listener tiene que importar su clase. Por ejemplo, `solicitudes.CotizacionEventsListener` importaría `cotizaciones.eventos.CotizacionEnviadaEvent` y `ordenes.eventos.OrdenGeneradaEvent`. Y dos eventos llevan tipos del dominio de su servicio.

- **A.** Regla: un servicio solo puede importar el paquete `eventos` de otro, que es el contrato del tópico, y nunca su `dominio`. Los dos atributos de T-06 se dejan como están y se resuelven en la sesión 4, solo si la demo consume esos eventos (el recorrido de compra no consume `cotizacion.validada` ni `tendencia.detectada`). Cuesta 0 ahora.
- **B.** Lo mismo que A, más `CotizacionValidadaEvent.estado: String` en la 4c ya, que es 1 línea. `TendenciaDetectadaEvent` necesitaría otro tipo y se deja.
- **C.** Pasar los 10 eventos a `compartido.eventos`. Es un cambio grande en 4a–4e y en el mapa.

**Recomendación:** A. Conviene escribir la regla en el README.

### Q6 · Adaptadores de persistencia con colecciones en memoria (T-11; puede esperar a la sesión 4)

**Qué pasa:** `CotizacionJpaAdapter.datos`, `KpiJpaAdapter.hechos` y `AuditoriaJpaAdapter.bitacora` simulan la tabla en memoria; los otros 12 `*JpaAdapter` (de 15) no tienen atributos. La sesión 4 necesita adaptadores en memoria para la demo.

- **A.** Copiarlos tal cual. El Javadoc dice que en el esqueleto simulan la tabla y que en Spring Boot serían un `JpaRepository`. Los demás adaptadores en memoria de la demo van en `demo`. Cuesta 0.
- **B.** Quitar los 3 atributos y la flecha `e25` en 4c y 4e, para que los 15 `*JpaAdapter` queden iguales. Toca 2 páginas aprobadas por algo que no está mal.

**Recomendación:** A.

### Q7 · Alcance de la demo frente a lo que promete el reporte (para la sesión 4; P-11)

**Qué pasa:** La §13 (p. 26) promete 7 salidas: Composite y Factory Method (menú del director de compras), Observer (pedido de $82.000), Facade y Chain (COT-018/019/020), Factory Method y Decorator (correo con X-Certificado), Proxy y Adapter (segundo precio desde la caché), Singleton (`umbral.stock = 25 %`) y Strategy (Bandeja paisa, Proveedor B). El plan de la sesión 4 solo cubre COT-018/019/020. Con el reporte sin tocar (DEC-03), cada salida que falte es una afirmación falsa. Además, la del Singleton contradice `n_parametros` (H13).

- **A.** La sesión 4 cubre las 7 salidas. La del Singleton muestra un parámetro del tema en lugar del umbral, y la frase se corrige en `CORRECCIONES_REPORTE.md`.
- **B.** Solo el flujo de compra, y anotar las otras 6 como falsas en `CORRECCIONES_REPORTE.md`.
- **C.** Las 7, con `umbral.stock` en el Singleton. Contradice la 4b y la 4e.

**Recomendación:** A. Es más trabajo en la sesión 4, pero el reporte queda cierto. Hay que decidirlo antes de la sesión 4, porque cambia su alcance.

