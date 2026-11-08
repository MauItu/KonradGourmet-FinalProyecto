# Validación de la sesión 3 (código) · 2026-11-08

Validó el agente `arquitecto-revisor`, solo con lectura, sobre la rama `correcciones-revision` de los dos repos:
- `documentos/`: de `20a030c` a `b7ed38b`;
- `KonradGourmet/`: los 176 commits sobre `ad71e69`.

Los hallazgos nuevos se numeran K1–K8 para no confundirlos con:
- H1–H26 (`REVISION_2026-11-07.md`);
- N1–N15 (`VALIDACION_FASE2A.md`);
- S1–S14 (`VALIDACION_SESION2.md`);
- los ítems C, M, R, T, W, P, J y Q de `INVENTARIO_SESION3.md`.

**Veredicto: lista con correcciones.**
- El código es idéntico a 4a–4e y los 176 commits compilan.
- Quedan 2 hallazgos de severidad media, los dos de texto:
  - una frase del README que el código no cumple (K1);
  - una nota del plan que puede dejar una afirmación falsa en el reporte (K2).
- Los otros 6 son bajos.
- La sesión 3 no cierra D12: la demo sigue imprimiendo una sola línea, y eso es de la sesión 4.

## Cómo se validó

- **Modelo del diagrama:** un extractor propio, hecho desde cero sobre el XML **actual** de 4a–4e (`p3`–`p7`). No se usó el esqueleto anterior.
  - Lee el paquete, el estereotipo, la cursiva (abstracto), el subrayado (estático), las marcas ‹Vxx›, los valores de cada enumeración y las flechas de triángulo hueco sin contar la leyenda.
  - Da las mismas cifras que el inventario: 191 clases (134 clases, 45 «interface», 6 «abstract» y 6 «enumeration»), 288 atributos, 403 métodos, 23 valores, 65 flechas (49 + 16) y 17 relaciones escritas.
- **Modelo del código:** un parser de los 192 `.java`, que lee los miembros de primer nivel, los comentarios y los cuerpos.
- **Comparador:** compara clase por clase, por nombre completo. Se probó con 8 mutaciones del modelo y detectó cada cambio real.
- **Compilación:** `git archive` de cada uno de los 176 commits, compilado en un temporal con `javac --release 21 -Xlint:all`. El `javac` instalado es el 27.
- **4e:** renderizada con `drawio -x -p 9 -s 1` y revisada por zonas.

## Lo acordado se respetó

- **Código idéntico a 4a–4e.** Hay 0 diferencias en todo esto:
  - las 191 clases, por nombre completo, con su paquete y su tipo;
  - los 288 atributos: nombre, tipo, visibilidad, `static` y el valor inicial `longitudMinima = 8`;
  - los 403 métodos: nombre, parámetros con su nombre y su tipo, retorno, visibilidad, `static` y `abstract`, en el mismo orden que la caja;
  - los 23 valores de enumeración, en su orden;
  - las 82 relaciones `extends`/`implements`, incluido el genérico de `EventListener<T extends EventoDominio>`.

  Sobra solo `demo.DemoArquitectura`, que se conserva (C-45). Los 4 miembros estáticos y los 7 métodos abstractos coinciden con el subrayado y la cursiva del diagrama.
- **DEC-25 (constructores):** son 56.
  - Uno de inyección en cada clase de `aplicacion` y de adaptadores que tiene atributos. Recibe los atributos en el orden de la caja, los asigna sin más y deja los colaboradores `final`.
  - Uno en cada una de las 4 clases del Factory Method de notificaciones.
  - El de `ParametrosSistema` es `private`, así que ya es un Singleton.
  - `protected CorreoDecorator(Correo envuelto)`, y en los dos decoradores concretos, `super(envuelto)`.
  - No hay clases de aplicación o adaptadores con atributos sin constructor, ni constructores fuera de lo que fija DEC-25.
- **DEC-26 (comentarios de ventana):** hay 29 líneas en 24 métodos, todas con el texto exacto `// Atiende la ventana Vxx de los mockups`. Cubren las 23 ventanas.
- **Rutas:** no hay ninguna inventada. Las 28 líneas de ruta coinciden con W-01 a W-35 o con la cabecera de su caja.
  - Donde ninguna fuente fija la ruta o el verbo (W-06, W-25, W-28 a W-30 y W-33), no hay línea de ruta.
  - El REST interno (W-34) dice que no se publica en el gateway.
  - Hay una omisión: K3.
- **Javadoc:**
  - todas las cabeceras de color de las cajas (23 en magenta y 38 en azul) están en el Javadoc de su clase;
  - las 23 notas `n_*`, incluida la nueva `n_cache`, están en el Javadoc de la clase a la que apunta su conector;
  - están los textos de J-17 (`MinisterioSoapClient`), DEC-30 (los 3 `*JpaAdapter`) y DEC-32 (`AbastecimientoSoapEndpoint`), y los de `{único}`, `{Redis}` y la política de contraseña;
  - ningún comentario cita ids internos ni sesiones; solo los supuestos S3 y S5, que son del reporte.
- **Cuerpos:** los 274 métodos concretos tienen solo `throw new UnsupportedOperationException("Pendiente de implementar");`.
- **Hexagonal:**
  - ningún servicio importa a otro;
  - no hay imports fuera del JDK ni imports sin uso;
  - los puertos no conocen adaptadores ni servicios;
  - los 25 adaptadores de entrada solo tienen un puerto de entrada;
  - todos los adaptadores de salida implementan un puerto, salvo `MinisterioSoapClient` (el Adaptee, P-08);
  - la única dependencia del dominio hacia fuera son las 4 clases de notificaciones que DEC-28 acepta, y el README lo dice.
- **4e (`documentos/`):**
  - cambiaron solo `cls_integracion_PrecioReferenciaCacheProxy` (`{Redis}`) y `cls_integracion_AbastecimientoSoapEndpoint` (`/api/integracion/**`), y se agregaron `n_cache` y `en_cache`;
  - la geometría de las dos cajas no cambió;
  - las otras 10 páginas tienen las mismas celdas que en `20a030c`;
  - el original conserva su SHA-256 (`10a49edd…`);
  - el `.drawio` tiene 1644 celdas, una por línea, y su XML es válido.
- **Render de la 4e:**
  - la nota cabe en 5 líneas y no tapa nada;
  - su conector baja al Proxy sin cruzar ninguna flecha;
  - la línea nueva del endpoint SOAP cabe en la caja.
- **README:**
  - la tabla de patrones es idéntica, carácter por carácter, a la P-13;
  - están las frases de DEC-25, DEC-26, DEC-28, DEC-29, P-08 y P-09;
  - lo que no es cierto frente al código está en K1, K3, K5 y K6.
- **Historia de git:**
  - los 176 commits compilan con `javac --release 21 -Xlint:all`, con 0 errores y 0 avisos (de 156 a 192 archivos);
  - cada commit de código toca exactamente las clases de sus ids M, R y C, sin archivos de más ni de menos;
  - todos los ids M-001–M-191, R-01–R-82 y C-01–C-44 tienen su commit;
  - DEC-25 aparece justo en los commits que agregan un constructor, y DEC-26 en los que agregan comentarios de ventana, con la cantidad correcta;
  - el origen (H, N, D o DEC) de cada clase nueva coincide con la columna «Viene de» del inventario;
  - cada clase pasa como mucho por movimiento → tipo → contenido;
  - los dos commits de movimiento solo cambian el `package`;
  - los 5 commits H8 solo cambian la palabra `class`, `interface`, `abstract`, `extends` o `implements`.
- **Documentos:**
  - DEC-25 a DEC-32 están bien registradas: cada una tiene por qué, opciones, la escogida marcada y qué implica;
  - las dos variantes (DEC-25 A2 y DEC-28 A) están explicadas;
  - las cifras del plan son ciertas: 191 clases + la demo, 82 relaciones, 56 constructores, 29 comentarios, 23 notas y 176 commits = 2 + 5 + 162 + 7.

## Hallazgos nuevos

| # | Sev. | Dónde | Qué está mal | Evidencia | Corrección mínima que propone el revisor |
|---|---|---|---|---|---|
| K1 | Media | `KonradGourmet/README.md` l. 41–43 | Dice que «las anotaciones de Spring y de JAX-WS van solo en ese comentario». Ninguna anotación de Spring está en un comentario | `grep -rn '@[A-Z]' src` solo encuentra `@Override` y `@WebService` (`AbastecimientoSoapEndpoint`, l. 8 y 12). No aparece `@RestController`, `@KafkaListener`, `@Repository` ni `@Scheduled` | «El código no usa anotaciones de Spring ni de JAX-WS, para que compile solo con `javac`; cuando la caja nombra una, como `@WebService`, va en el Javadoc». Se descarta escribir la anotación en el Javadoc de unas 45 clases: son 45 commits y no aportan nada (el reporte, p. 26, ya dice cuáles lleva el proyecto real) |
| K2 | Media | `PLAN_CORRECCIONES.md` l. 148 y l. 160 (sesión 5) | Dice «H1 (ya cierto)». Pero H1 también cubría la p. 22, «el esqueleto Java implementa el mismo comportamiento», que sigue siendo falsa y lo seguirá después de la sesión 4, porque la demo solo implementa una parte. La l. 160 toma de P-10 solo lo de Publish-Subscribe y el Singleton, y se le escapa esta frase | Reporte p. 22, párrafo inicial; los 274 cuerpos lanzan «Pendiente de implementar»; P-10 de `INVENTARIO_SESION3.md` | l. 148: «H1: cierto en p. 26 y p. 30 (métodos, comentarios y relaciones); D12». l. 160: agregar «y cambiar “el esqueleto Java implementa el mismo comportamiento” por “el esqueleto Java declara las mismas clases, firmas y relaciones, y la demo ejecuta los patrones” (P-10)» |
| K3 | Baja | `cotizaciones/adaptadores/entrada/ProveedorRestController.java` l. 23–24 | `consultar(soloActivos)` ‹V13› no tiene la línea de ruta, aunque una fuente la fija. El README (l. 39–40) promete que «la línea siguiente la dice» | Mapa p. 1, `v22`: «V13 … POST /api/cotizaciones · GET /api/proveedores»; W-13 del inventario | Agregar `// GET /api/proveedores` debajo de la línea de V13 (1 commit, W-13) |
| K4 | Baja | Historia de `Correo.java` y `CorreoDecorator.java` | Con el umbral normal, `git log --follow` no sigue estos dos movimientos y les asigna la historia de otro archivo: `AlarmaPushPort.java` (C064) y `AlarmaPushAdapter.java` (C062). Los originales eran esqueletos de 5 líneas, y al cambiar el `package` quedaron con 30 % y 41 % de similitud. Los otros dos decoradores (52 %) sí se siguen | `git log --follow --name-status -- …/puertos/salida/Correo.java` muestra `C064 …AlarmaPushPort.java`. Con `git log --follow -M30%` sale `R030 …/dominio/Correo.java` → `ad71e69` | No reescribir la historia: cuesta rehacer 176 commits y solo se pierde la creación de un archivo vacío. Anotar en el plan (sesión 3, «Cómo se trabaja») que para esos dos archivos se usa `git log --follow -M30%` |
| K5 | Baja | `README.md` l. 56 | La fila de `cotizaciones` dice «V13–V20». Pero `CotizacionRestController.detalle()` también atiende V21 | 4c `cls_cotizaciones_CotizacionRestController` ‹V20 · V21›; código l. 65–67 | «V13–V21». La fila de `ordenes` (V21–V23) queda igual, porque V21 la atienden los dos |
| K6 | Baja | `README.md` l. 33–37 | Al decir qué clases tienen constructor, faltan las 4 del Factory Method de notificaciones, que según DEC-25 reciben su `Correo` o `AlarmaPushPort` por el constructor | `NotificacionCorreo`, `CreadorCorreo`, `AlarmaApp` y `CreadorAlarmaApp` tienen constructor público (56 en total) | Agregar «…y las clases del Factory Method de `notificaciones` reciben el canal que usan» |
| K7 | Baja | `DECISIONES.md` l. 37 y l. 41 (índice) | DEC-25 dice «Aplicada» y DEC-29 «Aplicada (README)», pero a las dos les queda una parte para la sesión 4: los constructores y accesores de entidades y eventos, y T-06. El plan (l. 138) sí lo dice | El «Qué implica» de DEC-25 y DEC-29 | DEC-25: «Aplicada en aplicación y adaptadores; entidades y eventos en la sesión 4». DEC-29: «Aplicada (README); T-06 en la sesión 4, si la demo lo necesita» |
| K8 | Baja | `PLAN_CORRECCIONES.md`, sesión 4 (l. 130–140) | No dice que hay que actualizar el README cuando haya lógica. La l. 44 («Los métodos todavía lanzan…»), la l. 62 («Ejecuta los patrones…») y «Ejecutar la demo» dejarán de ser ciertas o pasarán a serlo | `README.md` l. 44, 62 y 100–105 | Agregar a la sesión 4: «Al terminar, actualizar en el README qué métodos tienen lógica, qué imprime la demo y el comando de las pruebas» |

El revisor no reportó como hallazgo lo que ya se sabía y está planificado:
- D12 (la demo);
- P-10, P-11 y P-12 (reporte p. 22 y 26, sesión 5);
- T-06 (sesión 4);
- la excepción del dominio de notificaciones, que decidió DEC-28.

**Opcional, sin severidad:** `FinalProyecto/exportes/fase2a/4e.png` es de antes de DEC-27 y DEC-32, y ya no coincide con la 4e. Si se quiere una imagen al día, se puede exportar a `exportes/sesion3/4e.png`, a escala 1.

## Lo que el reporte afirma sobre el código

| Página | Afirmación | Estado | Evidencia y dónde se corrige |
|---|---|---|---|
| p. 3 | «Cada uno de los 23 mockups está asignado a un controlador y un método concretos» | **Cierta** | Los 23 métodos de la §15 existen, con su comentario |
| p. 3 | «Una demostración ejecutable reproduce los valores de la ventana 19» | Falsa | La demo imprime solo «Konrad Gourmet - demo de arquitectura». D12, sesión 4 |
| p. 22 | «El esqueleto Java implementa el mismo comportamiento» | Falsa | 274 cuerpos «Pendiente de implementar». K2 → sesión 5 |
| p. 22 | Las filas Facade, Chain of Responsibility (incluido `cadenaPorDefecto`), Composite, Factory Method, Decorator (sobre `CorreoSmtpAdapter`), Adapter, Proxy y Strategy | **Ciertas en estructura** | Clases, tipos y relaciones como en 4a–4e. El Proxy implementa `PrecioReferenciaPort`, como el adaptador real |
| p. 22 | Fila «Observer» | Falsa en el nombre | 4a y el código dicen «Publish-Subscribe (Observer distribuido)». P-10, sesión 5 |
| p. 22 | El Singleton guarda el «umbral del 25 %» | Falsa | El Javadoc de `ParametrosSistema` (nota `n_parametros`) dice que el umbral es de ms-inventario. P-10, sesión 5 |
| p. 26 | Se comprobó que cada clase del mapa existe en el código | **Cierta** | Todos los nombres de clase de la p. 1 existen; las cajas dobles nombran dos clases que existen (DEC-15) |
| p. 26 | Se comprobó que el método de cada una de las 23 ventanas está declarado en su controlador | **Cierta** | 23/23 |
| p. 26 | «Doce paquetes de servicio … y 155 clases, además de la clase DemoArquitectura» | Falsa en la cifra | Son 191 más la demo. P-12, sesión 5 |
| p. 26 | Java 21 sin dependencias externas, compila solo con `javac` | **Cierta** | 0 imports fuera del JDK; compila con `--release 21` sin avisos |
| p. 26 | Los métodos ‹Vxx› llevan «Atiende la ventana Vxx de los mockups» | **Cierta** | 29 líneas, 23 ventanas |
| p. 26 | La tabla de las 7 salidas de la demo | Falsa | D12 y DEC-31, sesión 4. La salida del Singleton se corrige en la sesión 5 (P-11) |
| p. 26 | «javac -d bin … y luego java -cp bin …» | Cierta a medias | Compila y ejecuta, pero imprime una sola línea (D12) |
| p. 30 (§15) | «El método existe en el esqueleto Java con el comentario de la ventana que atiende» | **Cierta** | 23/23 |

**H1:** queda resuelto para la p. 26 (métodos y comentario) y la p. 30. Siguen pendientes:
- la p. 3, con la demo (D12, sesión 4);
- la p. 22, con «implementa el mismo comportamiento» (K2, sesión 5).

## Estado de los pendientes conocidos (E1–E3, D1–D12)

| Id | Estado | Evidencia |
|---|---|---|
| E1 | Resuelto | Mapa `e202`: «Redis · TLS» |
| E2 | Resuelto | Mapa `v51`: «Kubernetes · 3 zonas + centro de datos alterno» |
| E3 | Resuelto | 4d y Javadoc de `NotificacionListeners`: «Listener · 6 tópicos», con los 6 en la nota |
| D1 | Resuelto | `cotizaciones.puertos.salida.AlmacenArchivosPort`; `CotizacionEnviadaEvent.pdfUrl` (l. 15) |
| D2 | Resuelto en el `.drawio` y el código | 13 prefijos (sesión 2); 4e y Javadoc del endpoint SOAP (DEC-32). Falta la p. 7 del reporte (sesión 5) |
| D3 | Resuelto en el diseño y las firmas | `EstadoCotizacion.CON_ORDEN`, `OrdenJpaAdapter.existePorCotizacion()`, `OrdenGeneradaListener`. La lógica es de la sesión 4 |
| D4 | Resuelto | `InventarioEventsListener` («menu.actualizado · solicitud.registrada») y `atenderAlertas()` |
| D5 | Resuelto | `PlatoRestController.editar()` y `desactivar()`, con PUT y PATCH |
| D6 | Resuelto | Correo, en la p. 6 y en la nota `n_topicos` |
| D7 | Resuelto | `Proveedor` con 8 atributos; `ProveedorRestController` y `ProveedorService` |
| D8 | Resuelto | `SugerenciaPromocion`, `PromocionRestController` y `SugerenciaRepositoryPort.notificadaEl()` |
| D9 | Pendiente | Hay que reescribir la tarjeta HU-07 (sesión 5). `CriteriosConsulta` ya tiene sus 5 filtros en el código |
| D10 | Resuelto | `README.md` l. 69: Chain of Responsibility |
| D11 | Pendiente | No aparece el script de ReportLab (A1) |
| D12 | Pendiente | `DemoArquitectura` imprime una línea (sesión 4, DEC-31) |

## Dudas para decidir

| # | Duda | Opciones | Recomendación |
|---|---|---|---|
| K1 | ¿Cómo se corrige la frase de las anotaciones? | **A.** Cambiar la frase del README (1 commit). **B.** Escribir en el Javadoc de cada adaptador la anotación que llevaría en Spring (unas 45 clases) | **A.** El reporte (p. 26) ya nombra las anotaciones del proyecto real, y B suma 45 commits sin valor de diseño |
| K4 | ¿Qué se hace con la historia de `Correo` y `CorreoDecorator`? | **A.** Dejarla y anotar que se usa `-M30%`. **B.** Reescribir la historia desde `7cb9e8f`: cada movimiento llevaría una línea de Javadoc para superar el 50 %, y habría que rehacer los 176 commits | **A.** Solo se pierde la creación de un archivo vacío, y B arriesga lo que ya está validado |

Las demás correcciones (K2, K3 y K5–K8) son mecánicas y no traen duda: la sesión principal puede aplicarlas una por commit, como S12–S14.

## Seguimiento (2026-11-08)

Mauricio decidió los dos hallazgos que traían duda:
- **K1 → DEC-33:** escribir en el Javadoc de cada adaptador la anotación de Spring que lleva en el proyecto real, en los 54 adaptadores, y no cambiar la frase del README como recomendaba el revisor.
- **K4 → DEC-34:** no reescribir la historia y dejar un commit con la nota de `-M30%`. Mauricio propuso primero «un commit solo para corregirlos», pero un commit nuevo no cambia cómo git lee los commits viejos.

Lo demás se aplicó tal como lo propone el revisor.

| # | Estado | Commit |
|---|---|---|
| K1 | Aplicada (DEC-33). Los 54 adaptadores dicen en su Javadoc la anotación que llevan en el proyecto real: `@RestController` (14), `@Repository` (15), `@Component` con `@KafkaListener` (8 listeners), `@Component` con `@Scheduled` (2 jobs) y `@Component` (15). Además, el README explica por qué los servicios, los decoradores, el cliente SOAP y el endpoint SOAP no llevan | `KonradGourmet`: 54 commits, de `ccec468` a `451c69e`, uno por adaptador; README `9b43ed6` |
| K2 | Aplicada: H1 ya es cierto en la p. 26 y la p. 30; la frase de la p. 22 queda anotada para la sesión 5 | `eaa9242` |
| K3 | Aplicada: `// GET /api/proveedores` | `KonradGourmet` `6f19885` |
| K4 | Aplicada (DEC-34): la nota está en el README de `KonradGourmet` y en el plan | `KonradGourmet` `4bb25a0`; `4d12725` |
| K5 | Aplicada: «V13–V21». Revertida por K11 (DEC-37): vuelve a «V13–V20» | `KonradGourmet` `69d8011`; revertida en `aeceef7` |
| K6 | Aplicada | `KonradGourmet` `48989da` |
| K7 | Aplicada | `f901146` |
| K8 | Aplicada | `f918288` |

Cada commit de código compila con `javac --release 21 -Xlint:all`, sin errores ni avisos. Estos commits solo agregan comentarios: el código compilado no cambió frente a `67d9edd`, que la primera validación encontró idéntico a 4a–4e. (Antes decía «las 191 clases son exactamente la salida del generador»; el revisor no pudo comprobarlo, porque el generador no está en los repos, y la segunda validación comprobó lo equivalente.)

**Opcional:** la PNG al día de la 4e está en `FinalProyecto/exportes/sesion3/4e.png` (escala 1, fuera del repo).

## Segunda validación (2026-11-08)

Validó el agente `arquitecto-revisor`, solo con lectura, los commits que aplicaron K1–K8:
- `KonradGourmet/`: los 59 commits de `6f19885` a `4bb25a0` (HEAD), sobre `67d9edd`;
- `documentos/`: de `bc415ba` a `5c9a99c`.

Los hallazgos nuevos siguen la numeración: K9–K13.

**Veredicto del revisor: lista con correcciones.**
- K1–K8 quedaron resueltos, y cada commit cambia solo lo que dice su mensaje:
  - cada uno de los 55 commits de código toca un solo archivo y solo agrega líneas de comentario (122 líneas, ninguna quitada);
  - en los 54 de DEC-33, la clase, el paquete, la página (4a–4e) y la anotación que dice el mensaje coinciden con el archivo y con la caja del diagrama;
  - los 4 commits del README y los 7 de `documentos/` cambian solo el párrafo, la celda o la fila que nombran.
- **K1 (DEC-33):**
  - la nota está en exactamente 54 clases, todas de `adaptadores/`:
    - 14 con `@RestController`;
    - 15 con `@Repository`;
    - 8 listeners con `@KafkaListener` en `alRecibir()`;
    - 2 jobs con `@Scheduled` en `ejecutar()`;
    - 15 con `@Component`;
  - las otras 5 clases de `adaptadores/` son las que DEC-33 excluye: los 3 decoradores, `MinisterioSoapClient` y `AbastecimientoSoapEndpoint`;
  - ninguna clase fuera de `adaptadores/` lleva la nota;
  - las anotaciones coinciden con el reporte y con la 3b:
    - el reporte nombra `@RestController`, `@Repository` y `@KafkaListener` (p. 26) y dice que «el orden de los decoradores se fija en la configuración» (p. 22);
    - la 3b dice «Spring MVC», «Spring Data JPA» y «consumidor Kafka», y no le da tecnología a los casos de uso;
  - quedan dos detalles de Spring (K9 y K10) y uno de redacción en el README (K12).
- **El código sigue idéntico a 4a–4e:**
  - entre `67d9edd` y HEAD, el bytecode de las 192 clases es idéntico (`javap -p -c -s -constants`), y también el código fuente sin comentarios;
  - no cambió ninguna firma.
- **Compilación:**
  - la base y los 59 commits compilan con `javac --release 21 -Xlint:all` (javac 27), con 0 errores, 0 avisos y el log vacío;
  - cada uno tiene 192 archivos;
  - `javadoc -Xdoclint:all,-missing` tampoco da avisos.
- **DEC-33 y DEC-34** están bien registradas: cada una tiene por qué, opciones, la escogida y qué implica. Lo de DEC-34 es cierto:
  - con el umbral normal, `--follow` da `C064` y `C062`;
  - con `-M30%`, da `R030` (`7cb9e8f`) y `R041` (`5f50178`) y llega hasta `ad71e69`.
- **Estados:**
  - son ciertos los de `DECISIONES.md` (DEC-25, DEC-29, DEC-33 y DEC-34) y los del plan (235 commits sobre `ad71e69`; a la sesión 3 le faltan la segunda validación y el OK);
  - el seguimiento también es cierto, salvo una frase de K1 (K12) y la del generador (ver la nota del final).
- **`.drawio`:**
  - no cambió desde `7e92bb9` y sigue con 1644 celdas;
  - el original conserva su SHA-256 (`10a49edd…`).
- **PNG:** `exportes/sesion3/4e.png` es idéntica, byte a byte, a la 4e exportada hoy.

| # | Sev. | Dónde | Qué está mal | Evidencia | Corrección mínima que propone el revisor |
|---|---|---|---|---|---|
| K9 | Media | `integracion/adaptadores/salida/PrecioReferenciaCacheProxy.java` l. 11 y `MinisterioComercioAdapter.java` l. 9 | Los dos dicen `@Component`, y los dos implementan `PrecioReferenciaPort`. `ConsultarPrecioService` recibe un `PrecioReferenciaPort` (l. 14). Con dos candidatos, Spring no arranca (`NoUniqueBeanDefinitionException`), y nada asegura que el servicio reciba el Proxy y no el adaptador real | Es el único puerto con dos implementaciones anotadas en el mismo servicio. Los decoradores también envuelven su interfaz, y el README (l. 47–49) los arma en una clase de configuración; al Proxy no | En el Proxy: «En el proyecto real: @Component y @Primary: ConsultarPrecioService recibe el Proxy, y el Proxy recibe el adaptador real.» (1 commit). En DEC-33 (l. 584), anotar que el Proxy lleva además `@Primary` |
| K10 | Media | `inventario/adaptadores/entrada/ConsumoDiarioJob.java` l. 8 y `analitica/adaptadores/entrada/TendenciasJob.java` l. 8 | `@Scheduled` corre en cada réplica. ms-inventario tiene de 2 a 6, así que el descuento de las 23:59 se haría de 2 a 6 veces cada noche. `descontarConsumoDiario()` no recibe la fecha, y ni el diagrama ni el reporte dicen que el job corra en una sola réplica. En ms-analitica (de 2 a 4 réplicas) se repetirían las sugerencias; `notificadaEl` solo evita repetir la notificación | Despliegue, p. 10 `v15`: «ms-inventario 2 → 6 · … ms-analitica … 2 → 4»; `GestionarInventarioUseCase` l. 13. No lo crea DEC-33: la caja «Scheduler · diario 23:59» ya estaba, pero la nota lo vuelve explícito | En los dos jobs: «En el proyecto real: @Component, y ejecutar() lleva @Scheduled (diario 23:59) y @SchedulerLock (ShedLock), para que lo ejecute una sola réplica.» (2 commits). También en DEC-33 (l. 583) |
| K11 | Baja | `KonradGourmet/README.md` l. 62 (`69d8011`, K5) | Dice que ms-cotizaciones atiende «V13–V21». Cuatro artefactos le dan V13–V20 y dejan V21 en ms-ordenes: el mapa, la 3a, el reporte y `ESTADO_PROYECTO_Y_C4.md`. K5 igualó el README al código, pero lo dejó en contra de esos cuatro. El error fue de la recomendación del revisor | Mapa `v121` «V13–V20» y `v141` «V21–V23»; 3a `c3a_ms_cotizaciones` «V13–V20»; reporte p. 7; `ESTADO_PROYECTO_Y_C4.md` l. 74. Que V21 llama a ms-cotizaciones ya lo dicen la 3b (`c3b_controller`, `c3b_e5`) y el comentario de `detalle()` | Volver a «V13–V20» (1 commit). En el seguimiento, K5 pasa a «Revertida por K11» |
| K12 | Baja | `KonradGourmet/README.md` l. 44–49; `VALIDACION_SESION3.md` l. 176 | «Cada adaptador dice en su Javadoc qué anotación lleva» no es cierto para `MinisterioSoapClient`: está en `adaptadores/salida`, su Javadoc no nombra ninguna anotación y el README no lo excluye. El seguimiento dice que el README explica el cliente SOAP, y no lo explica. Además, «una clase de configuración los arma» se lee como si esa clase estuviera en el código | `MinisterioSoapClient.java` l. 3–8; ninguna clase de configuración en `src/` | «…el endpoint SOAP dice `@WebService`, y el cliente SOAP lo genera `wsimport`. En el proyecto real, los servicios de aplicación y los decoradores no llevan anotación: una clase de configuración los arma…» (1 commit). Con eso, la l. 176 queda cierta |
| K13 | Baja | `DECISIONES.md` l. 601–602 | El «Qué implica» de DEC-34 va pegado al `---`. En Markdown se lee como un título de nivel 2, y la línea separadora no aparece | Es la única decisión sin línea en blanco antes del `---` (`8daca33`) | Una línea en blanco antes de la l. 602 (1 commit) |

**Dudas para decidir:**

| # | Duda | Opciones | Recomendación |
|---|---|---|---|
| K9 | ¿Cómo recibe `ConsultarPrecioService` el Proxy y no el adaptador real? | **A.** `@Primary` en el Proxy: 1 commit, y siguen siendo 54. **B.** El Proxy sin anotación, armado en la configuración como los decoradores: cambian el Proxy, el README, DEC-33 y el seguimiento, que pasan a 53 | **A.** Es una línea y conserva DEC-33. B es más parecido a los decoradores, pero cambia una cuenta ya registrada |
| K10 | ¿Cómo se asegura que el job corra en una sola réplica? | **A.** `@SchedulerLock` (ShedLock), con su tabla en la base de cada servicio. **B.** Un CronJob de Kubernetes en lugar de `@Scheduled`. **C.** Dejar `@Scheduled` y hacer idempotente el descuento en la sesión 4 | **A.** Es la solución habitual con `@Scheduled`, y solo agrega una librería y una tabla. B cambia la caja «Scheduler» y necesita otra forma de llamar a `ejecutar()`. C hará falta igual en la lógica, pero no evita que dos réplicas corran a la vez |
| K11 | ¿Qué ventanas se le dan a ms-cotizaciones? | **A.** Volver a «V13–V20», como el mapa, la 3a, el reporte y ESTADO. **B.** Dejar «V13–V21» y cambiar `v121`, la 3a y la p. 7 | **A.** Es 1 commit. B toca dos páginas ya aprobadas y el reporte |

K12 y K13 son mecánicos y no traen duda.

**Nota:** el revisor no pudo comprobar la frase del seguimiento «las 191 clases son exactamente la salida del generador a partir de 4a–4e» (l. 185), porque el generador no está en ninguno de los dos repos. Comprobó lo equivalente: el código no cambió frente a `67d9edd`, que la primera validación encontró idéntico a 4a–4e.

**Pendientes conocidos:** no cambian frente a la primera validación. E1–E3, D1–D8 y D10 siguen resueltos (D2, salvo la p. 7 del reporte), y D9, D11 y D12 siguen pendientes.

## Seguimiento de la segunda validación (2026-11-08)

Mauricio decidió los tres hallazgos que traían duda, y en los tres siguió la recomendación del revisor:
- **K9 → DEC-35:** el Proxy de precios lleva además `@Primary`.
- **K10 → DEC-36:** los 2 jobs llevan además `@SchedulerLock` (ShedLock).
- **K11 → DEC-37:** el README vuelve a «V13–V20» para ms-cotizaciones.

K12 y K13 se aplicaron como los propone el revisor.

| # | Estado | Commit |
|---|---|---|
| K9 | Aplicada (DEC-35): «En el proyecto real: @Component y @Primary: ConsultarPrecioService recibe el Proxy, y el Proxy recibe el adaptador real.» DEC-33 lo anota | `KonradGourmet` `96e211a`; `ca2402d` |
| K10 | Aplicada (DEC-36): en los dos jobs, `ejecutar()` lleva además «@SchedulerLock (ShedLock), para que lo ejecute una sola réplica». DEC-33 lo anota | `KonradGourmet` `6756be5` (`ConsumoDiarioJob`) y `ab988e9` (`TendenciasJob`); `ca2402d` |
| K11 | Aplicada (DEC-37): «V13–V20». K5 queda revertida | `KonradGourmet` `aeceef7` |
| K12 | Aplicada: el README dice que el cliente SOAP no lleva anotación porque lo genera `wsimport`, y que la clase de configuración es del proyecto real y este código no la incluye. Con eso, la fila de K1 del seguimiento anterior queda cierta | `KonradGourmet` `ef29eed` |
| K13 | Aplicada | `4b98ace` |

- Los 3 commits de código solo cambian una línea de Javadoc por clase, que pasa a ocupar dos.
- Cada uno compila con `javac --release 21 -Xlint:all`, sin errores ni avisos, con 192 archivos.
- `KonradGourmet` tiene 240 commits sobre `ad71e69`.
- El `.drawio` no cambió.

## Tercera validación (2026-11-08)

Validó el agente `arquitecto-revisor`, solo con lectura, los commits que aplicaron K9–K13:
- `KonradGourmet/`: los 5 commits de `4bb25a0..ef29eed`. HEAD es `ef29eed`, con 240 commits sobre `ad71e69`.
- `documentos/`: los 3 commits de `0e8ba0d..9f39317`.

Los hallazgos nuevos siguen la numeración: K14–K16.

**Veredicto del revisor: lista con correcciones.**
- K9–K13 están bien aplicados. El código compilado no cambió, y los 7 estados revisados compilan sin avisos.
- Quedan 3 hallazgos de severidad baja, todos de texto en `documentos/`:
  - K14: un estado del plan;
  - K15: una errata de la segunda validación;
  - K16: una omisión en DEC-36.
- Ninguno toca el código ni el `.drawio`.

**Cómo se validó:**
- **Copias:** `git archive` de `67d9edd`, `4bb25a0` y los 5 commits, hacia un temporal. No se hicieron checkouts ni worktrees.
- **Compilación:** `javac --release 21 -Xlint:all`, con javac 27.0.1.
- **Bytecode:** `javap -p -c -s -constants` de las 192 clases en los 7 estados.
- **Fuente sin comentarios:** un comparador propio. Se probó con dos mutaciones: detecta un cambio de código e ignora un cambio de comentario.
- **Javadoc:** `javadoc -Xdoclint:all,-missing` sobre HEAD.
- **`.drawio`:** su XML, leído página por página.

**Lo comprobado:**

1. **Commits.**
   - Cada commit de `KonradGourmet` toca un solo archivo y cambia lo que dice su mensaje:
     - `96e211a`: el Proxy (4e);
     - `6756be5`: `ConsumoDiarioJob` (4b);
     - `ab988e9`: `TendenciasJob` (4e);
     - `aeceef7`: la fila de `cotizaciones` del README;
     - `ef29eed`: el párrafo de las anotaciones del README.
   - Los 3 commits de código cambian una línea de Javadoc por dos, y nada más.
   - `aeceef7` es el inverso exacto de `69d8011` (K5).
   - En `documentos/`:
     - `4b98ace` agrega solo la línea en blanco;
     - `ca2402d` toca solo `DECISIONES.md`: la cabecera, el índice (con «Por aplicar»), DEC-33 y DEC-35 a DEC-37;
     - `9f39317` pasa el índice a «Aplicada» y actualiza el plan y el seguimiento.
   - De las validaciones anteriores solo cambiaron la fila K5 (l. 180) y la frase del generador (l. 185) del primer seguimiento.

2. **K9 (DEC-35).**
   - El Javadoc del Proxy (l. 11–12) dice exactamente la frase propuesta, partida en dos líneas.
   - En `4bb25a0..HEAD` solo cambian 3 `.java` y el README. `MinisterioComercioAdapter` sigue con `@Component`.
   - El razonamiento técnico es correcto:
     - **El servicio recibe el Proxy.** `ConsultarPrecioService(PrecioReferenciaPort precios, …)` tiene dos candidatos, y el nombre `precios` no coincide con el de ningún bean. Sin `@Primary`, Spring falla con `NoUniqueBeanDefinitionException`; con `@Primary`, recibe el Proxy. Fuera del Proxy, es el único que pide ese puerto.
     - **El Proxy recibe el adaptador real.** Spring descarta la auto-referencia en la primera pasada de `findAutowireCandidates` cuando hay otro candidato. Para el constructor del Proxy queda solo `MinisterioComercioAdapter`, así que Spring ni consulta `@Primary` y no hay ciclo.
     - **La auto-referencia** solo se usa como último recurso, cuando no hay ningún otro candidato. En un constructor fallaría por referencia circular.
     - **La frase de DEC-35.** «Spring no le pasa a una clase su propio objeto» es cierto aquí, porque el adaptador real existe. Si se quiere precisar, se puede agregar «mientras haya otro candidato».

3. **K10 (DEC-36).**
   - Los dos jobs (l. 8–9) dicen la frase propuesta; `TendenciasJob` dice «(semanal)».
   - Ninguna línea empieza con `@`, así que `javadoc` no la lee como etiqueta: da 0 avisos.
   - Lo que DEC-36 dice del `.drawio` es cierto:
     - `v15` de la p. 5 (pestaña 10) dice «ms-inventario 2 → 6» y «ms-analitica … 2 → 4»;
     - en la 3a, `c3a_db_inventario` es «BD inventario [PostgreSQL]» y `c3a_db_analitica` es «Almacén analítico [PostgreSQL]»;
     - las cajas «Scheduler» están en la 4b y en la 4e, y también en el mapa (K16).
   - También es cierto lo que dice del código:
     - `descontarConsumoDiario()` no recibe parámetros (l. 13);
     - `SugerenciaRepositoryPort.notificadaEl(LocalDate)` está en la l. 16.

4. **K11 (DEC-37).**
   - El README dice «V13–V20» para ms-cotizaciones (l. 63) y «V21–V23» para ms-ordenes (l. 64). «V13–V21» ya solo aparece en los registros.
   - Las fuentes de DEC-37 lo confirman:
     - el mapa: `v121` «V13–V20» y `v141` «V21–V23»;
     - la 3a: `c3a_ms_cotizaciones` y `c3a_ms_ordenes`;
     - la 3b: `c3b_e5`;
     - el reporte, p. 7;
     - `ESTADO_PROYECTO_Y_C4.md`, l. 74–75.
   - El comentario V21 de `detalle()` se queda (l. 68), como dice DEC-37.
   - DEC-37 y la l. 180 marcan K5 como revertida.

5. **K12.** La frase del README (l. 44–50) es cierta:
   - no hay imports fuera del JDK, y fuera de los comentarios solo aparece `@Override`;
   - 54 adaptadores llevan la nota:
     - 14 con `@RestController`;
     - 15 con `@Repository`;
     - 8 listeners con `@KafkaListener`;
     - 2 jobs con `@Scheduled` y `@SchedulerLock`;
     - 15 con `@Component`, uno de ellos el Proxy, que lleva además `@Primary`;
   - `MinisterioSoapClient` solo dice que lo genera `wsimport`, sin anotación;
   - el endpoint SOAP dice `@WebService (JAX-WS)`;
   - los 3 decoradores no llevan nota;
   - no hay ninguna clase de configuración en `src/`.

   El texto aplicado va un poco más allá de la propuesta: «que este código no incluye» resuelve también la segunda mitad de K12. Con eso, la fila K1 del primer seguimiento (l. 176) queda cierta: las cifras coinciden, y el README explica las cuatro exclusiones. El endpoint queda explicado porque lleva `@WebService`, que es de JAX-WS.

6. **K13.**
   - El «Qué implica» de DEC-34 está en la l. 605, la l. 606 está en blanco y la l. 607 empieza DEC-35.
   - El `---` ahora cierra DEC-37 (l. 638), con la l. 637 en blanco.
   - En los tres documentos no queda ningún texto pegado a un separador ni a un título.

7. **El código compilado no cambió.**
   - `javap` da lo mismo en los 7 estados.
   - El fuente sin comentarios es idéntico en los 192 archivos.
   - Los `.class` solo difieren en la tabla de números de línea, que se corre una línea:
     - frente a `4bb25a0`, en 3 clases;
     - frente a `67d9edd`, en 54.
   - Entre `67d9edd` y `4bb25a0` se agregaron 122 líneas, todas de comentario, y no se quitó ninguna.

8. **Compilación.** Los 7 estados (`67d9edd`, `4bb25a0` y los 5 commits) compilan: 192 archivos, código de salida 0 y el log vacío. La demo de `ef29eed` sigue imprimiendo una sola línea (D12).

9. **`DECISIONES.md`.**
   - La cabecera (l. 10) y el índice (l. 49–51, «Aplicada») son correctos.
   - DEC-35 a DEC-37 tienen por qué, las opciones de cada duda (A/B, A/B/C y A/B), la escogida marcada y qué implica.
   - DEC-33 anota los dos cambios (l. 587–588).
   - Lo único inexacto es K16.

10. **Plan y seguimiento.**
    - Las cifras son ciertas: 176 commits en `67d9edd`, 235 en `4bb25a0` y 240 en HEAD.
    - El seguimiento de la segunda validación es cierto en todo: los commits de cada fila, «una línea de Javadoc por clase, que pasa a ocupar dos», los 192 archivos, los 240 commits y que el `.drawio` no cambió.
    - La frase nueva de la l. 185 es cierta si «código compilado» se entiende como el bytecode: solo cambian los números de línea.
    - Lo inexacto está en K14.

11. **`.drawio`.**
    - No cambió desde `7e92bb9`, tampoco en el árbol de trabajo.
    - Tiene 1644 celdas y su XML es válido.
    - El original conserva su SHA-256: `10a49edda6378257ef5bff30f2ffa4e2700d9b427fa32d478dfef1351845dc5f`.

| # | Sev. | Dónde | Qué está mal | Evidencia | Corrección mínima que propone el revisor |
|---|---|---|---|---|---|
| K14 | Baja | `PLAN_CORRECCIONES.md` l. 124 y l. 201 (fila 3 de «Avance») | La l. 124 dice que los hallazgos K1–K8 «todos están aplicados», y la fila 3 dice que «K1–K13 están corregidos». Pero K5 ya no está aplicada: se revirtió, porque su recomendación era equivocada | `aeceef7` revierte `69d8011`; DEC-37; seguimiento, l. 180: «Revertida por K11»; el mismo plan, l. 131: «(revierte K5)» | l. 124: «…y todos se aplicaron (K5 se revirtió después, por K11):». Fila 3: «Aplicada y validada por el revisor tres veces (2026-11-08). K1–K13 resueltos (K5, revertida por K11); falta el OK de Mauricio. …». Va en el mismo commit que registra esta validación |
| K15 | Baja | `VALIDACION_SESION3.md` l. 236 (evidencia de K10, texto del revisor) | Dice «Despliegue, p. 10 `v15`». En los documentos, el despliegue es la p. 5; 10 es su número de pestaña. Hay otros cuatro `v15` (en la p. 1, la 2, la 4a y la 4d), así que el número de página importa | `<diagram name="5 · Despliegue" id="p8">` es la pestaña 10. DEC-20 (l. 390) y DEC-36 (l. 619) dicen «p. 5», y DEC-36 está bien | Ninguna en la l. 236, que es el registro de la segunda validación: esta sección deja la errata. Si se prefiere, cambiar a «p. 5» en la l. 236 |
| K16 | Baja | `DECISIONES.md` l. 619 y l. 623 (DEC-36) | Dice que las cajas «Scheduler» están «en la 4b y la 4e», y que la opción B cambia esas dos. Pero también están en el mapa, aprobado en la sesión 2: B costaba una página aprobada más. No cambia la decisión, pero hace ver la opción descartada más barata de lo que era | Mapa (p. 1): `ad_inventario_t1` ««Scheduler» · entrada / ConsumoDiarioJob» y `ad_analitica_t2` ««Scheduler» · entrada / TendenciasJob» | l. 619: «…ya estaban en el mapa, la 4b y la 4e…». l. 623: «Cambia lo que dicen las cajas «Scheduler» del mapa, la 4b y la 4e…» (1 commit) |

**Opcional, sin severidad:** el README (l. 47–50) dice que la clase de configuración arma los servicios y los decoradores. Para que el proyecto real arranque, también tendría que crear:
- el cliente SOAP, `MinisterioSoapClient(String endpoint)`, que no lleva anotación;
- el mapa de Redis del Proxy, `Map<Long, PrecioEstandar>`. Como su clave no es `String`, Spring no arma un mapa de beans: busca un bean de ese tipo;
- las dos fábricas de notificaciones que recibe `NotificacionService`.

El README no dice lo contrario, y ninguna fuente lo exige. Si se quiere completar: «…los arma con sus constructores, igual que al cliente SOAP, al mapa de Redis del Proxy y a las fábricas de notificaciones, y ahí se fija el orden de los decoradores.»

**Dudas para decidir:**

| # | Duda | Opciones | Recomendación |
|---|---|---|---|
| K10 | ¿ShedLock y `@Primary` deben aparecer también en el reporte o en algún diagrama? | **A.** No: quedan en el Javadoc y en DEC-35 y DEC-36. **B.** Una frase en el reporte, junto a las anotaciones de la p. 26, si en la sesión 5 aparece el script (DEC-03). **C.** Una nota en la 4b y en la 4e | **A** ahora, y **B** si se vuelve a generar el reporte. C toca páginas aprobadas. Lo probable es que el profesor lo pregunte en la sustentación, no que lo busque en un diagrama, y DEC-36 ya da la respuesta |

K14 y K16 son mecánicos. K15 queda resuelto con esta sección.

**No tocar:** los 3 Javadoc nuevos, el párrafo del README (l. 44–50), la fila de `cotizaciones` y DEC-35 a DEC-37. Están bien y coinciden con el código y el `.drawio`.

**Pendientes conocidos:** no cambian frente a las validaciones anteriores.
- E1–E3, D1–D8 y D10 siguen resueltos (D2, salvo la p. 7 del reporte).
- D9, D11 y D12 siguen pendientes: la demo de `ef29eed` imprime una sola línea.

## Seguimiento de la tercera validación (2026-11-08)

Mauricio aceptó las dos recomendaciones:
- **La duda → DEC-38:** ShedLock y `@Primary` no van ahora en el reporte ni en los diagramas. El plan de la sesión 5 suma una frase para la p. 26 del reporte.
- **El opcional:** se completa la frase del README sobre la clase de configuración.

| # | Estado | Commit |
|---|---|---|
| K14 | Aplicada: la l. 124 y la fila 3 de «Avance» dicen que K5 se revirtió por K11 | `3804808` |
| K15 | Sin cambio: la errata queda anotada en la tercera validación | — |
| K16 | Aplicada: DEC-36 nombra las cajas «Scheduler» del mapa (`ad_inventario_t1`, `ad_analitica_t2`) | `138ebc0` |
| Duda | Aplicada (DEC-38) | `e5db1c6` |
| Opcional | Aplicada: «…los arma con sus constructores, igual que al cliente SOAP, al mapa de Redis del Proxy y a las fábricas de notificaciones, y ahí se fija el orden de los decoradores.» | `KonradGourmet` `37d655b` |

- `KonradGourmet` tiene 241 commits sobre `ad71e69`.
- Compila con `javac --release 21 -Xlint:all`, sin errores ni avisos, con 192 archivos.
- El código Java y el `.drawio` no cambiaron.

## Cuarta validación (2026-11-08)

La validó el agente `arquitecto-revisor`, solo con lectura, sobre estos commits:
- `KonradGourmet/`: `37d655b` (`ef29eed..HEAD`). HEAD tiene 241 commits sobre `ad71e69`.
- `documentos/`: `3804808`, `138ebc0`, `e5db1c6` y `f6bb233` (`9f39317..HEAD`).

Los hallazgos nuevos siguen la numeración: K17–K18.

**Veredicto del revisor: lista.**
- Los 5 commits cambian lo que dicen sus mensajes, y K14, K16 y DEC-38 quedaron como se propusieron.
- La frase nueva del README es cierta para el código.
- HEAD compila sin avisos. El código Java y el `.drawio` no cambiaron.
- Quedan 2 hallazgos de severidad baja, de redacción. Ninguno impide el OK de Mauricio.

**Cómo se validó:**
- **Copia:** `git archive HEAD` de `KonradGourmet`, hacia un temporal. No se hicieron checkouts ni worktrees, y los dos repos siguen limpios en `correcciones-revision`.
- **Compilación:** `javac --release 21 -Xlint:all`, con javac 27.0.1.
- **Texto de la tercera validación:** se comparó con la respuesta original del revisor, sacada del registro de su sesión.

**Lo comprobado:**

1. **Commits.** Cada uno toca solo lo que dice su mensaje.
   - `37d655b` toca solo el README, l. 49–51 (3 líneas agregadas y 2 quitadas).
   - `3804808`:
     - agrega 146 líneas al final de `VALIDACION_SESION3.md` y no borra ninguna;
     - en el plan, cambia la l. 124 (K14), agrega la viñeta «Tercera validación» y la fila 3 de «Avance».
   - `138ebc0` cambia solo 2 líneas de DEC-36 (K16).
   - `e5db1c6` agrega en `DECISIONES.md` la línea de cabecera, la fila del índice y DEC-38, y en el plan una viñeta.
   - `f6bb233` actualiza 2 líneas del plan y agrega el seguimiento.
   - En `9f39317..HEAD` no se borró ninguna línea de `VALIDACION_SESION3.md`.

2. **La sección «Tercera validación» es fiel.** Pasado `&lt;`/`&gt;` a `<`/`>`, el `diff` contra la respuesta original solo muestra dos diferencias:
   - el párrafo introductorio y el `---`, que no eran parte de la sección;
   - el salto de línea final.

3. **K14 y K16.**
   - **K14, l. 124:** dice el texto propuesto, palabra por palabra.
   - **K14, fila 3 (l. 203):** no es literal, pero lo dice todo. Dice «K1–K13 resueltos (K5, revertida por K11)», y que falta la revisión corta y el OK de Mauricio. La fila además está al día con DEC-38.
   - **K16:** la l. 621 dice «el mapa (`ad_inventario_t1`, `ad_analitica_t2`), la 4b y la 4e», y la l. 625 dice «del mapa, la 4b y la 4e». Las dos celdas están en la p. 1 (`id="p1"`) y dicen «Scheduler».

4. **DEC-38 está bien registrado.**
   - Tiene la cabecera (l. 11), la fila del índice (l. 53) y la sección (l. 640–649), con por qué, opciones A/B/C, la escogida marcada y qué implica.
   - Va cerrada por `---`, con una línea en blanco antes.
   - **Las opciones.** Se reordenaron frente a la tabla de dudas, pero no cambian lo que se decide:
     - la A de DEC-38 junta la A y la B que proponía el revisor, que era su recomendación;
     - la B de DEC-38 es la A del revisor;
     - la C es la misma.
   - **Lo que afirma es cierto:**
     - la p. 26 del reporte nombra `@RestController, @Repository, @KafkaListener` (línea 19 de `pdftotext`);
     - ni el reporte ni el `.drawio` mencionan `Primary`, `SchedulerLock` ni `ShedLock` (0 coincidencias).
   - **La viñeta del plan (l. 179)** no repite la condición «si aparece el script». No hace falta: la hereda de su encabezado, «`CORRECCIONES_REPORTE.md`: … cuando aparezca el script (DEC-03)».

5. **La frase del README es cierta.**
   - `MinisterioSoapClient(String endpoint)` no lleva anotación, porque lo genera `wsimport`.
   - El Proxy recibe `Map<Long, PrecioEstandar> cache`.
   - `NotificacionService(CreadorNotificacion creadorCorreo, CreadorNotificacion creadorAlarma, …)` recibe las dos fábricas. `CreadorCorreo` y `CreadorAlarmaApp` están en `notificaciones/dominio/` y no llevan nota de anotación.
   - No hay ninguna clase de configuración en `src/`.

6. **Las cifras son ciertas.**
   - `git rev-list --count ad71e69..HEAD` da 241 (240 en `ef29eed`).
   - Son 192 archivos `.java`, y salen 192 `.class`.
   - La fila 3 de «Avance» y el «Seguimiento de la tercera validación» coinciden con los commits:
     - K14 en `3804808`;
     - K15 sin cambio: la l. 236 sigue diciendo «p. 10 `v15`»;
     - K16 en `138ebc0`;
     - DEC-38 en `e5db1c6`;
     - el opcional en `37d655b`, y la cita coincide con el README.

7. **Compilación.** HEAD compila: código de salida 0 y el log vacío (0 bytes). La demo sigue imprimiendo una sola línea (D12).

8. **Sin cambios.**
   - `git diff ef29eed HEAD -- src` sale vacío.
   - `git diff 7e92bb9 HEAD -- '*.drawio'` sale vacío, y el árbol de trabajo está limpio.
   - El original conserva su SHA-256: `10a49edda6378257ef5bff30f2ffa4e2700d9b427fa32d478dfef1351845dc5f`.

**Nota:** DEC-38 corrió 2 líneas `DECISIONES.md` desde la l. 11. Por eso las líneas que la tercera validación cita de ese archivo (619, 623, 638…) ahora están 2 más abajo. Es un registro fechado, y no se corrige.

| # | Sev. | Dónde | Qué está mal | Evidencia | Corrección mínima que propone el revisor |
|---|---|---|---|---|---|
| K17 | Baja | `VALIDACION_SESION3.md` l. 413 (tabla de dudas de la tercera validación, texto del revisor) | La duda se numeró «K10», que ya es el hallazgo de los jobs (l. 236). Es un error del revisor, y el registro lo copió fielmente | En la l. 236, K10 es `@Scheduled` en cada réplica. DEC-38 y el seguimiento la llaman «la duda» | Ninguna en la l. 413: esta sección deja la errata, como con K15. Si se prefiere, cambiar «K10» por «Duda» |
| K18 | Baja | `DECISIONES.md` l. 625 (DEC-36, opción B) | La frase encadena dos «y»: «…el mapa ya está aprobado (sesión 2), y la 4b y la 4e no se pueden tocar…, y necesita otra forma de llamar a `ejecutar()`». Así, «necesita» se lee como si tuviera por sujeto «la 4b y la 4e», cuando es el CronJob | `138ebc0` agregó la subordinada en medio de la frase original | Partir la frase: «…en esta sesión. Además, necesita otra forma de llamar a `ejecutar()`.» (1 commit, opcional) |

**No tocar:**
- la frase del README (l. 44–51);
- DEC-36, salvo K18 si se quiere, DEC-38 y su fila del índice;
- la l. 124 y la fila 3 del plan;
- la sección «Tercera validación».

**Pendientes conocidos:** no cambian frente a las validaciones anteriores.
- E1–E3, D1–D8 y D10 siguen resueltos (D2, salvo la p. 7 del reporte).
- D9, D11 y D12 siguen pendientes: la demo de `37d655b` imprime una sola línea.

### Seguimiento de la cuarta validación (2026-11-08)

| # | Estado | Commit |
|---|---|---|
| K17 | Sin cambio: la errata queda anotada aquí, como K15 | — |
| K18 | Aplicada: «…en esta sesión. Además, el CronJob necesita otra forma de llamar a `ejecutar()`.» | `c648c94` |

La sesión 3 queda a la espera del OK de Mauricio.
