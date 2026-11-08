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
