# Konrad Gourmet — Sistema de Abastecimiento

Sistema de abastecimiento para restaurantes, construido con **arquitectura hexagonal**
(puertos y adaptadores) y organizado en módulos que corresponden a microservicios.

## Arquitectura

Cada módulo se divide siempre en las mismas capas:

| Capa | Contenido | Regla |
|---|---|---|
| `dominio/` | Entidades, objetos de valor y reglas de negocio | No depende de nada externo |
| `eventos/` | Eventos de dominio que publica el módulo | Extienden `EventoDominio` |
| `puertos/entrada/` | Casos de uso que el módulo ofrece | Interfaces |
| `puertos/salida/` | Lo que el módulo necesita del exterior | Interfaces |
| `aplicacion/` | Servicios que implementan los casos de uso | Orquesta el dominio |
| `adaptadores/entrada/` | Controladores REST, listeners, jobs | Llaman a los puertos de entrada |
| `adaptadores/salida/` | Persistencia, clientes HTTP/SOAP, correo | Implementan los puertos de salida, salvo el cliente SOAP generado del WSDL, que es el Adaptee |

La dependencia siempre apunta hacia adentro: los adaptadores conocen los puertos,
nunca al revés. El dominio no importa frameworks ni adaptadores. Hay una sola excepción
a «el dominio no conoce a nadie»: en `notificaciones`, el Factory Method usa las
interfaces `Correo` y `AlarmaPushPort`, que son puertos del mismo núcleo.

Entre microservicios, un módulo solo puede importar el paquete `eventos` de otro, que
es el contrato de su tópico, y nunca su `dominio`. Todos pueden usar `compartido`.

## El código y los diagramas de clases

El código sigue las páginas 4a–4e del diagrama de arquitectura: los mismos paquetes,
clases, atributos, métodos y relaciones `extends`/`implements`.

- Como es habitual en UML, los diagramas omiten los constructores y los métodos de
  acceso. En el código, los servicios, controladores, listeners, jobs y adaptadores
  reciben por el constructor lo que su caja lista como atributos; `ParametrosSistema`
  tiene el constructor privado del Singleton, cada decorador recibe el correo que
  envuelve, y las clases del Factory Method de `notificaciones` reciben el canal que
  usan (`Correo` o `AlarmaPushPort`).
- Cada método marcado ‹Vxx› en el diagrama lleva el comentario
  `// Atiende la ventana Vxx de los mockups`, una línea por ventana. Si alguna fuente
  fija la ruta REST, la línea siguiente la dice, por ejemplo `// POST /api/cotizaciones`.
- El texto de color de cada caja (patrón, ruta, tópico o tecnología) y las notas del
  diagrama van en el Javadoc de la clase.
- El código no usa Spring ni JAX-WS, para que compile solo con `javac`. Cada adaptador
  dice en su Javadoc qué anotación lleva en el proyecto real, por ejemplo
  «En el proyecto real: @RestController.»; el endpoint SOAP dice `@WebService`, y el
  cliente SOAP no dice ninguna, porque lo genera `wsimport`. En el proyecto real, los
  servicios de aplicación y los decoradores no llevan anotación: una clase de
  configuración, que este código no incluye, los arma con sus constructores, igual que
  al cliente SOAP, al mapa de Redis del Proxy y a las fábricas de notificaciones, y ahí
  se fija el orden de los decoradores.
- Además de lo que dibujan los diagramas, el código tiene lo que la demo necesita (DEC-25):
  constructores, accesores al estilo record (`numero()`, no `getNumero()`) y, donde una entidad
  cambia sin tener un método para eso, un constructor que copia: `Cotizacion(base, pdfUrl)` y
  `OrdenCompra(numero, sinNumero)` / `OrdenCompra(base, pdfUrl)`. En las entidades que arma otra
  clase, el accesor de la lista (`items()`, `cotizacionesAplicadas()`) devuelve la lista
  modificable. Fuera de eso, solo hay métodos privados auxiliares y el `Logger` de la fachada.
- Los métodos con lógica real y los que siguen pendientes están en «Qué métodos tienen lógica».
  Los pendientes lanzan `UnsupportedOperationException("Pendiente de implementar")`.

## Módulos

| Módulo | Microservicio | Ventanas | Responsabilidad |
|---|---|---|---|
| `compartido` | — | — | Librería común: publicación de eventos y auditoría |
| `identidad` | ms-identidad | V01 | Usuarios, roles, sucursales y autenticación |
| `pedidos` | ms-pedidos | V02–V04 | Registro y seguimiento de pedidos |
| `menu` | ms-menu | V06 | Platos, ingredientes y categorías |
| `inventario` | ms-inventario | V05, V07 | Stock, umbrales y consumo diario |
| `solicitudes` | ms-solicitudes | V08–V12 | Solicitudes de alimentos |
| `cotizaciones` | ms-cotizaciones | V13–V20 | Cotizaciones, proveedores y reglas de validación |
| `ordenes` | ms-ordenes | V21–V23 | Órdenes de compra |
| `notificaciones` | ms-notificaciones | — | Correos y alarmas en la aplicación |
| `integracion` | ms-integracion | — | Frontera SOAP con el Ministerio de Comercio |
| `administracion` | ms-administracion | — | Parámetros del sistema y auditoría |
| `analitica` | ms-analitica | — | Tablero BAM e indicadores |
| `demo` | — | — | `DemoArquitectura` recorre el flujo de compra; las otras 19 clases son los adaptadores en memoria y el `Ensamble` que lo conecta |

## Patrones de diseño

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

## Estructura

```
KonradGourmet/
├── README.md
├── ejecutar.sh                  compila, ejecuta la demo y corre las pruebas
├── pruebas/co/edu/konradlorenz/pruebas/
└── src/co/edu/konradlorenz/
    ├── compartido/
    ├── identidad/
    ├── pedidos/
    ├── menu/
    ├── inventario/
    ├── solicitudes/
    ├── cotizaciones/
    ├── ordenes/
    ├── notificaciones/
    ├── integracion/
    ├── administracion/
    ├── analitica/
    └── demo/
```

## Ejecutar la demo y las pruebas

Solo hace falta un JDK 21 o más nuevo; no se usa Maven ni ninguna librería.

```bash
./ejecutar.sh             # compila, ejecuta la demo y corre las pruebas
./ejecutar.sh demo        # solo la demo
./ejecutar.sh pruebas     # solo las pruebas (sale con código 1 si alguna falla)
```

El script deja las clases en `out/` (que git ignora) y compila con `javac --release 21 -Xlint:all`,
que no da errores ni avisos. Las dos órdenes de fondo, por si no tienes bash:

```bash
javac --release 21 -d out/clases $(find src -name "*.java")
java -cp out/clases co.edu.konradlorenz.demo.DemoArquitectura
javac --release 21 -cp out/clases -d out/pruebas $(find pruebas -name "*.java")
java -cp out/clases:out/pruebas co.edu.konradlorenz.pruebas.EjecutarPruebas
```

En Windows (PowerShell), cambia `$(find …)` por `(Get-ChildItem -Recurse src -Filter *.java).FullName` y
los `:` del classpath por `;`.

### Qué imprime la demo

`DemoArquitectura` cuenta un día en la sucursal Centro, con adaptadores en memoria (sin base de datos, sin
Kafka y sin frameworks). Las secciones numeradas del 1 al 7 son las 7 salidas que promete el reporte
(§13, p. 26); las demás son los pasos que las conectan:

| Sección | Patrón | Qué se ve |
|---|---|---|
| 0. Carga inicial | Publish-Subscribe | Los datos de partida: 2 platos (por `menu.actualizado`), 4 insumos, 4 proveedores y la libreta de correos |
| 1 | Composite + Factory Method | El menú `DIRECTOR_COMPRAS`: Inicio, Cotizaciones (Todas, Pendientes de validar, Opcionadas), Órdenes de compra, Mi perfil y Cerrar sesión |
| 2 | Hexagonal + Publish-Subscribe | El pedido PED-0251, mesa 5, tomado por el mesero 3, 3 platos, **$82.000**, y el evento `pedido.registrado` |
| 3 | Factory Method + Decorator | El cierre de las 23:59 deja la res al **23 %**: alarma «Res al 23 %: realice una solicitud» y correo al jefe con `X-Certificado` y `X-Sello-Tiempo` |
| 4 | Proxy + Adapter | El precio de la res: la 1.ª consulta llama al servicio web (1 llamada); la 2.ª la responde la caché (sigue en 1) |
| Solicitud | Publish-Subscribe | SOL-031 (res 20 kg y arroz 15 kg); `solicitud.registrada` atiende la alerta de la res |
| 5 | Facade + Chain of Responsibility | **COT-018:** estándar $520.000, proveedor $545.000, **+4,81 % → OPCIONADA**. **COT-019:** +34,62 % → RECHAZADA. **COT-020:** −61,54 % → SOSPECHOSA |
| Orden | Publish-Subscribe | **OC-0012** por $545.000; COT-018 pasa a CON_ORDEN y SOL-031 a COMPLETADA; una segunda orden sobre COT-018 se rechaza (D3) |
| 6 | Singleton | `tema.color-primario` (no el umbral, que es de ms-inventario, H13), y que dos llamadas dan la misma instancia |
| 7 | Strategy | Producto con mayor demanda: **Bandeja paisa (40)**; proveedor con mejores precios: **Proveedor B** |
| Resumen | — | Eventos por tópico, correos, alarmas y registros de auditoría |

Cifras de la demo que no salen de los diagramas (supuestos, anotados en `DECISIONES.md`):
- Los precios son por unidad: la res a $20.000/kg y el arroz a $8.000/kg dan los $520.000 de V19; los
  proveedores cotizan la res a $21.250, $29.000 y $4.000/kg, y el arroz a $8.000/kg.
- El pedido de $82.000 es 2 bandejas paisas ($32.000) y 1 ajiaco ($18.000), como dice el reporte; el mockup
  V03 muestra además una limonada, y con ella el total sería $88.000.
- Los hechos del tablero (demanda y precios de proveedores) son un histórico cargado a mano: el listener
  que los armaría con los eventos (`NegocioEventsListener`) sigue pendiente.

### Qué métodos tienen lógica

De los 274 cuerpos que lanzaban `Pendiente de implementar` al terminar la sesión 3, quedan **94**. Tienen
lógica real todo lo que usa el recorrido de compra, de punta a punta; lo que queda pendiente es lo que
habla con una tecnología externa (la demo pone un adaptador en memoria en su lugar) o lo que la demo no usa.

| Módulo | Con lógica | Pendiente |
|---|---|---|
| `compartido` | `EventoDominio` y `AuditoriaEvent`; los otros 9 eventos están en el paquete `eventos` de su servicio y también tienen constructor, accesores y `tipo()` | `KafkaEventPublisher` |
| `identidad` | Composite (`ElementoMenu`, `GrupoMenu`, `OpcionMenu`), `Usuario` y sus 6 roles (`rol()`), `DirectorCompras.construirMenu()`, `Sucursal` | `construirMenu()` de los otros 5 roles, `PoliticaContrasena`, `IniciarSesionService`, `RegistrarUsuarioService`, los 2 controladores y los 2 adaptadores (BCrypt y usuarios) |
| `pedidos` | `Pedido`, `DetallePedido`, `Mesa`, `PlatoCatalogo`, `CatalogoPlatos`, `PedidoService`, `PedidoRestController`, `MenuActualizadoListener` | `CatalogoJpaAdapter`, `PedidoJpaAdapter` |
| `menu` | `MenuActualizadoEvent` | todo lo demás: `Plato`, `PlatoService`, `PlatoRestController`, `PlatoJpaAdapter` |
| `inventario` | `InsumoInventario`, `Inventario`, `RecetaPlato`, `UmbralStock`, `InventarioService`, `ConsumoDiarioJob`, los 2 listeners y `InventarioRestController` | `InventarioJpaAdapter` |
| `solicitudes` | `SolicitudAlimentos`, `ItemSolicitud`, `CriteriosConsulta`, `SolicitudService`, `CotizacionEventsListener`, `SolicitudRestController` | `SolicitudJpaAdapter` |
| `cotizaciones` | `Cotizacion`, `ItemCotizacion`, `Proveedor`, `ReglaValidacion` y sus 3 reglas, `GenerarCotizacionFacade`, `CotizacionService`, `ProveedorService`, `OrdenGeneradaListener`, los 2 controladores, `CotizacionJpaAdapter` | `ProveedorJpaAdapter`, `PdfCotizacionAdapter`, `AlmacenObjetosAdapter`, `PrecioSugeridoRestAdapter` |
| `ordenes` | `OrdenCompra`, `ItemOrden`, `CotizacionOpcionada`, `OrdenCompraService`, `OrdenCompraRestController` | `OrdenJpaAdapter`, `PdfOrdenAdapter`, `AlmacenObjetosAdapter`, `CotizacionRestClient` |
| `notificaciones` | `CreadorNotificacion` y sus 2 creadores, `NotificacionCorreo`, `AlarmaApp`, `Mensaje`, los 3 decoradores, `NotificacionService`, `NotificacionListeners`, `DestinatarioRestController` | `CorreoSmtpAdapter`, `AlarmaPushAdapter`, `AlmacenObjetosAdapter`, `DestinatarioJpaAdapter`, `EnvioJpaAdapter`; la rama `tendencia.detectada` de `notificarEvento()` (T-06) |
| `integracion` | `PrecioEstandar`, `MinisterioComercioAdapter`, `PrecioReferenciaCacheProxy`, `ConsultarPrecioService`, `PrecioRestController`, `AbastecimientoSoapEndpoint` | `MinisterioSoapClient` (la demo usa `MinisterioSoapSimulado`) |
| `analitica` | `HechoNegocio`, `ResultadoKpi`, las 3 estrategias, `kpis()` y `registrarHecho()` de `TableroService`, `KpiJpaAdapter`, `TableroRestController` | las promociones (HU-19): `SugerenciaPromocion`, `detectarTendencias()`, `sugerencias()`, `aceptarSugerencia()`, `descartarSugerencia()`, `PromocionRestController`, `TendenciasJob`, `NegocioEventsListener`, `SugerenciaJpaAdapter`, `RedesSocialesAdapter` |
| `administracion` | `ParametrosSistema`, `RegistroAuditoria`, `LogError`, `Bitacora`, `AdministracionService`, `AuditoriaListener`, `AdminRestController`, `AuditoriaJpaAdapter` | `ParametrosJpaAdapter`, `ElasticsearchLogsAdapter` |

Las 3 clases `*JpaAdapter` que tienen un atributo en el diagrama (`CotizacionJpaAdapter`, `KpiJpaAdapter`,
`AuditoriaJpaAdapter`) funcionan sobre su colección en memoria (DEC-30). Para los demás puertos de salida, la
demo trae su adaptador en memoria en el paquete `demo`.

Límites que conviene conocer:
- Los casos de uso no reciben al usuario (en el proyecto real sale del token JWT), así que los eventos llevan
  `sistema` o el correo de la entidad. La excepción es el pedido: nace con el `meseroId` de quien lo tomó
  (HU-01, DEC-51), `validar()` lo exige y `pedidosDelTurno()` filtra por él. Cada evento crea su propio `correlacionId`; propagarlo entre
  servicios queda para el proyecto real.
- `Notificacion.enviar(destino, contenido, adjuntoUrl)` y `CreadorNotificacion.notificar(destino, contenido,
  adjuntoUrl)` llevan el enlace del adjunto (DEC-42, decidido por Mauricio): el correo certificado al proveedor
  lleva el PDF en `Mensaje.adjuntoUrl`, y `AlarmaApp` lo ignora. La página 4d tiene las mismas firmas.
- `descontarConsumoDiario()` no repite el descuento si se ejecuta dos veces, porque resta del consumo
  pendiente lo que ya descontó. Aun así, DEC-36 sigue valiendo: con varias réplicas, `@SchedulerLock`
  evita que dos corran a la vez.
- Todos los datos de la demo están en memoria: al terminar se pierden.
- La alarma de stock bajo sale solo la noche en que el insumo cruza el umbral, no cada noche mientras siga bajo
  (DEC-46). Una alerta atendida por una solicitud sigue atendida aunque el stock siga bajando.
- La diferencia porcentual se redondea a 2 decimales antes de compararla con los límites: +25,004 % se ve y se
  trata como +25,00 % (OPCIONADA).
- Los consumidores no descartan los eventos repetidos (la nota de `EventoDominio` habla de guardar los id
  procesados): un `orden.generada` que llega dos veces manda otro correo al proveedor.
- «Mesa activa» se valida con la `Mesa` que llega en el pedido, no con la del repositorio.
- `completar()` acepta una solicitud PENDIENTE: una orden sobre una cotización que cubrió solo parte de los
  productos de una solicitud completa igual a todas las de la cotización (DEC-46).

### Pruebas

`pruebas/` tiene 176 verificaciones en 3 clases de prueba, sobre un mini marco (`Prueba`) que no usa JUnit:
- `PruebasCotizacion`: los valores de V19 y los límites de la cadena de reglas (+25,00 % y −50,00 % siguen
  OPCIONADA; +25,01 % y −50,01 % no);
- `PruebasPatrones`: los 8 patrones, uno por uno;
- `PruebasFlujoDeCompra`: pedido (con su mesero), inventario (incluido el cierre del día siguiente), solicitud, cotización,
  orden OC-0012, una cotización con 2 solicitudes (DEC-05), proveedores y el caso PENDIENTE_PRECIO (H18).

## Historia en git

`Correo.java` y `CorreoDecorator.java` venían de `notificaciones/dominio`. Eran archivos
tan pequeños que, al cambiarles el `package`, `git log --follow` no reconoce el
movimiento y les asigna la historia de `AlarmaPushPort.java` y `AlarmaPushAdapter.java`.
Para ver su historia real:

```bash
git log --follow -M30% -- src/co/edu/konradlorenz/notificaciones/puertos/salida/Correo.java
git log --follow -M30% -- src/co/edu/konradlorenz/notificaciones/adaptadores/salida/CorreoDecorator.java
```

## Interfaz web

La pantalla de inicio de sesión está en la raíz del proyecto:

```
index.html      maquetado
styles.css      estilos
assets/logo/    logo.png
assets/video/   3DVideo.mp4
```

Se abre directamente en el navegador. Si el video no arranca por las
restricciones de `file://`, levanta un servidor local:

```bash
python -m http.server 8000
```
