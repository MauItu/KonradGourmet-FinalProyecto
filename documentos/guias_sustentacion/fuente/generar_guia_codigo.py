"""Genera guias_sustentacion/guia_codigo.html: el código, sus arquitecturas y sus carpetas.

Los fragmentos de código se leen de KonradGourmet/ al generar, así la guía no puede decir otra cosa que el código.
Uso, desde la carpeta FinalProyecto:
    python3 documentos/guias_sustentacion/fuente/generar_guia_codigo.py
"""
import re
import sys
from pathlib import Path

AQUI = Path(__file__).resolve().parent
sys.path.insert(0, str(AQUI))
from estilo import E, pagina   # noqa: E402

GUIAS = AQUI.parent
KG = GUIAS.parent.parent / "KonradGourmet"
NUCLEO = "src/co/edu/konradlorenz"
SERV = "servicios"


def fragmento(ruta, desde, lineas, hasta=None):
    """Las líneas de `ruta` desde la primera que contiene `desde` (y hasta la que contiene `hasta`, si se da)."""
    texto = (KG / ruta).read_text(encoding="utf-8").splitlines()
    i = next(n for n, l in enumerate(texto) if desde in l)
    if hasta:
        j = next(n for n in range(i + 1, len(texto)) if hasta in texto[n])
        trozo = texto[i:j + 1]
    else:
        trozo = texto[i:i + lineas]
    sangria = min((len(l) - len(l.lstrip()) for l in trozo if l.strip()), default=0)
    codigo = "\n".join(l[sangria:] for l in trozo)
    return (f'<p class="ruta">KonradGourmet/{E(ruta)}</p><pre><code>{E(codigo)}</code></pre>')


def tabla(cabeza, filas, anchos=None):
    th = "".join(f'<th{f" style=width:{anchos[i]}" if anchos and anchos[i] else ""}>{E(c)}</th>'
                 for i, c in enumerate(cabeza))
    cuerpo = "".join("<tr>" + "".join(f"<td>{c}</td>" for c in f) + "</tr>" for f in filas)
    return f"<table><thead><tr>{th}</tr></thead><tbody>{cuerpo}</tbody></table>"


def c(t):
    return f"<code>{E(t)}</code>"


def contar():
    clases = {m.name: len(list(m.rglob("*.java"))) for m in (KG / NUCLEO).iterdir() if m.is_dir()}
    del_diagrama = sum(n for m, n in clases.items() if m != "demo")
    return clases, del_diagrama


def secciones():
    clases, del_diagrama = contar()
    s = []

    # 1 ------------------------------------------------------------------
    s.append(("resumen", "El código en una página", f"""
<p>El código está en el repositorio <code>KonradGourmet/</code> (rama <code>stack-real</code>) y tiene cuatro partes
que trabajan juntas:</p>
{tabla(["Parte", "Carpeta", "Qué es"], [
    ["<b>Núcleo</b>", c("src/co/edu/konradlorenz/"), f"Las {del_diagrama} clases del diagrama de clases (páginas 4a a 4e), en Java 21 puro, sin frameworks: dominio, casos de uso y puertos de cada servicio. Es el «hexágono» de la arquitectura."],
    ["<b>Microservicios</b>", c("servicios/"), "Los 11 microservicios Spring Boot, el API Gateway, el Service Registry (Eureka) y la librería compartido. Cada servicio toma su parte del núcleo y le agrega los adaptadores reales: REST, JPA, Kafka, S3, SOAP, SMTP…"],
    ["<b>SPA</b>", c("web/"), "La aplicación web en React y TypeScript: las 33 ventanas, una por ruta, que hablan con el gateway por REST."],
    ["<b>Plataforma</b>", c("plataforma/"), "El Docker Compose que levanta todo junto (servicios, bases, Kafka, Redis, almacén de objetos, Elasticsearch…), los scripts para compilar, levantar, bajar y probar, y las pruebas de punta a punta."],
], ["18%", "26%", None])}
<p>Además están <code>pruebas/</code> (las 400 verificaciones del núcleo) y la versión en memoria del núcleo
(<code>src/…/demo/</code>), que recorre la compra completa sin base de datos ni Kafka.</p>
<div class="aviso"><b>La idea central:</b> la lógica de negocio se escribe una sola vez, en el núcleo, y no conoce
ninguna tecnología. Cada microservicio <em>copia</em> su parte del núcleo al compilar y la conecta con Spring, la base
de datos y Kafka a través de los puertos. Por eso los nombres del diagrama de clases, del núcleo y de los servicios
coinciden uno a uno.</div>
"""))

    # 2 ------------------------------------------------------------------
    s.append(("arquitecturas", "Las arquitecturas y dónde se ven en el código", f"""
<p>El proyecto combina tres arquitecturas principales y varias de apoyo. Esta tabla dice en qué carpeta y en qué
clases se ve cada una, para poder abrirlas si lo piden.</p>
{tabla(["Arquitectura", "Qué decide", "Dónde se ve en el código"], [
    ["<b>Microservicios</b>", "La estructura: un servicio desplegable por subdominio, cada uno con su base de datos.", f"Un módulo Maven por servicio en {c('servicios/')} ({c('ms-pedidos')}, {c('ms-cotizaciones')}…), cada uno con su {c('application.yml')}, sus migraciones Flyway en {c('db/migration')} y su imagen Docker; una base PostgreSQL y un usuario por servicio en {c('plataforma/infra/postgres/01-bases.sh')}."],
    ["<b>Orientada a eventos</b>", "Cómo se comunican los servicios sin esperarse: publicar y suscribirse en Kafka.", f"{c('EventoDominio')} y {c('EventPublisherPort')} en el núcleo; {c('KafkaEventPublisher')}, {c('OutboxEventos')}, {c('ReleOutbox')} y {c('EventosProcesados')} en {c('servicios/compartido')}; los {c('*Listener')} de cada servicio con {c('@KafkaListener')}; los 10 tópicos en {c('plataforma/infra/kafka/crear-topicos.sh')}."],
    ["<b>Hexagonal</b> (puertos y adaptadores)", "El interior de cada servicio: el dominio en el centro y la tecnología afuera.", f"Las carpetas de cada módulo: {c('dominio/')}, {c('puertos/entrada')}, {c('puertos/salida')}, {c('aplicacion/')}, {c('adaptadores/entrada')} y {c('adaptadores/salida')}. El núcleo no importa Spring ni JPA."],
    ["Cliente-servidor y REST", "La SPA es el cliente; los servicios responden JSON.", f"{c('web/src/api/cliente.ts')} y los {c('*RestController')} de cada servicio."],
    ["SOA / SOAP", "La frontera con el Ministerio y con otros sistemas.", f"{c('ms-integracion')}: {c('AbastecimientoSoapEndpoint')} (Apache CXF, {c('@WebService')}) y {c('MinisterioSoapClient')}."],
    ["API Gateway (Facade)", "Un solo punto de entrada.", f"{c('servicios/api-gateway')}: rutas por prefijo en {c('application.yml')}, {c('SeguridadGateway')} (JWT), {c('LimiteTasaGateway')} (límite de tasa en Redis) y {c('CorrelacionGateway')} (X-Correlation-Id)."],
    ["Service Registry", "Descubrir instancias vivas.", f"{c('servicios/service-registry')} (Eureka); cada servicio se registra solo."],
    ["OAuth 2.0 / JWT", "Quién es el usuario y qué rol tiene.", f"{c('ms-identidad')} firma el token con RS256 ({c('JwtEmisorAdapter')}); el gateway y cada servicio lo validan ({c('SeguridadServicio')}) y los controladores piden el rol con {c('@PreAuthorize')}."],
], ["20%", "28%", None])}
"""))

    # 3 ------------------------------------------------------------------
    arbol = """KonradGourmet/
├── src/co/edu/konradlorenz/      núcleo: las 195 clases del diagrama (+ demo/)
│   ├── compartido/               EventoDominio, EventPublisherPort, EventListener…
│   ├── identidad/  pedidos/  menu/  inventario/  solicitudes/  cotizaciones/
│   ├── ordenes/  notificaciones/  integracion/  administracion/  analitica/
│   └── demo/                     versión en memoria: DemoArquitectura, Ensamble y adaptadores en memoria
├── pruebas/                      400 verificaciones del núcleo (8 clases, sin JUnit)
├── servicios/                    Maven multimódulo (Spring Boot 3.5 · Spring Cloud 2025.0)
│   ├── pom.xml                   el padre: versiones y la copia del núcleo
│   ├── Dockerfile                una imagen para cualquier servicio (Java 21)
│   ├── compartido/               librería: Kafka, outbox, relé, seguridad, logs a Elasticsearch
│   ├── service-registry/         Eureka
│   ├── api-gateway/              Spring Cloud Gateway
│   └── ms-identidad/ … ms-analitica/   los 11 microservicios
├── web/                          SPA: React 18 + TypeScript + Vite
│   └── src/  api/ componentes/ paginas/ sesion/ estilos/
├── plataforma/                   todo junto con Docker Compose
│   ├── docker-compose.yml
│   ├── compilar.sh  levantar.sh  bajar.sh  probar_todo.sh
│   ├── infra/                    postgres/ kafka/ nginx/ s3/ ministerio/ redes/
│   └── pruebas/                  recorridos de navegador y verificadores (cortes 3 a 7)
├── ejecutar.sh                   compila el núcleo, corre la demo y las pruebas
└── README.md                     cómo está hecho y cómo se corre"""
    s.append(("carpetas", "Qué es cada carpeta", f"""
<pre><code>{E(arbol)}</code></pre>
<p>Las carpetas {c('out/')}, {c('target/')}, {c('web/dist/')} y {c('web/node_modules/')} se generan al compilar y git
las ignora. En la raíz también hay una interfaz web sencilla ({c('index.html')}, {c('app.html')}, {c('app.js')} y
{c('web.sh')}) que corre sobre la versión en memoria del núcleo; la que se sustenta es la SPA de {c('web/')} sobre la
plataforma.</p>
<h3>Un módulo del núcleo por dentro</h3>
<p>Los doce módulos del núcleo tienen siempre las mismas carpetas. La regla es que la dependencia apunta hacia
adentro: los adaptadores conocen los puertos, nunca al revés.</p>
<div class="capas">
<div><h4>adaptadores/entrada</h4>Controladores REST, listeners de Kafka y tareas programadas. <b>Llaman</b> a un puerto de entrada.</div>
<div><h4>puertos/entrada</h4>Interfaces de los casos de uso (<code>…UseCase</code>): lo que el servicio ofrece.</div>
<div class="nucleo"><h4>aplicacion</h4>Los servicios (<code>…Service</code>, la fachada) que implementan los casos de uso y orquestan el dominio.</div>
<div class="nucleo"><h4>dominio</h4>Entidades, objetos de valor y reglas de negocio. No depende de nada externo.</div>
<div class="nucleo"><h4>eventos</h4>Los eventos que publica el módulo; extienden <code>EventoDominio</code>. Es lo único que otro módulo puede importar.</div>
<div><h4>puertos/salida</h4>Interfaces de lo que el servicio necesita de afuera: repositorio, PDF, precio, correo…</div>
<div><h4>adaptadores/salida</h4>Implementan los puertos de salida con una tecnología: JPA, S3, OpenPDF, SOAP, SMTP, Redis.</div>
</div>
<p>Las {del_diagrama} clases del núcleo, por módulo:
{" ".join(f'<span class="etiqueta">{E(m)} · {n}</span>' for m, n in sorted(clases.items()) if m != "demo")}
<span class="etiqueta">demo · {clases.get("demo", 0)} (fuera del diagrama)</span></p>
"""))

    # 4 ------------------------------------------------------------------
    s.append(("servicio", "Cómo se arma un microservicio", f"""
<p>Cada carpeta de {c('servicios/')} es un proyecto Spring Boot. No reescribe la lógica: al compilar, Maven copia su
parte del núcleo (dominio, eventos, puertos y aplicación, sin los adaptadores del núcleo) y le suma sus adaptadores
Spring, con los mismos nombres del diagrama. Así un solo modelo sirve para el diagrama, la versión en memoria y la
plataforma.</p>
{fragmento(f"{SERV}/ms-cotizaciones/pom.xml", "<id>copiar-nucleo</id>", 0, "</resources>")}
<p>Lo que agrega cada servicio:</p>
{tabla(["Pieza", "Ejemplo en ms-cotizaciones", "Para qué"], [
    ["Clase principal", c("CotizacionesApplication"), "Arranca Spring Boot."],
    ["Configuración", c("configuracion/ConfiguracionCotizaciones"), "Arma los objetos del núcleo con {c} (la fachada, el servicio, la cadena de reglas) y les pasa los adaptadores por el constructor.".format(c=c("@Bean"))],
    ["Adaptadores de entrada", f"{c('CotizacionRestController')}, {c('ProveedorRestController')}, {c('OrdenGeneradaListener')}", "Reciben HTTP o eventos de Kafka y llaman al puerto de entrada."],
    ["Adaptadores de salida", f"{c('CotizacionJpaAdapter')}, {c('PdfCotizacionAdapter')}, {c('AlmacenObjetosAdapter')}, {c('PrecioSugeridoRestAdapter')}", "Implementan los puertos con JPA, OpenPDF, el cliente S3 y REST."],
    ["Entidades JPA", f"{c('CotizacionEntidad')}, {c('ProveedorEntidad')}", "El mapeo a tablas queda en el adaptador; el dominio no tiene anotaciones."],
    ["Migraciones", f"{c('db/migration/V1__esquema.sql')}, {c('V2__outbox.sql')}", "Flyway crea las tablas de su base, incluida la outbox."],
    ["application.yml", "puerto, base, Kafka, Eureka, S3", "La configuración, con variables de entorno que pone el compose."],
    ["Pruebas JUnit", c("src/test/…"), "Corren al compilar (91 en total)."],
], ["20%", "38%", None])}
<p>La configuración conecta el núcleo con la tecnología:</p>
{fragmento(f"{SERV}/ms-cotizaciones/src/main/java/co/edu/konradlorenz/cotizaciones/configuracion/ConfiguracionCotizaciones.java", "public ReglaValidacion reglasValidacion()", 0, "return new CotizacionService(")}
"""))

    # 5 ------------------------------------------------------------------
    pasos = [("SPA", "V19: el director pulsa «Validar»"), ("Nginx", "sirve la SPA y pasa /api/** al gateway"),
             ("API Gateway", "valida el JWT, limita la tasa, pone la correlación y busca la instancia en Eureka"),
             ("CotizacionRestController", "adaptador de entrada; exige el rol DIRECTOR_COMPRAS"),
             ("GestionarCotizacionUseCase", "puerto de entrada"), ("CotizacionService", "aplicación: aplica la cadena de reglas"),
             ("ReglaValidacion", "dominio: +4,81 % → OPCIONADA"), ("CotizacionJpaAdapter", "guarda en la BD de cotizaciones"),
             ("KafkaEventPublisher", "escribe cotizacion.validada en la outbox, en la misma transacción"),
             ("ReleOutbox → Kafka", "publica el evento y lo borra de la outbox"),
             ("ms-analitica", "su listener lo descarta si es repetido y suma el KPI del proveedor")]
    flujo = '<span class="flecha">→</span>'.join(f'<div class="paso"><b>{E(a)}</b>{E(b)}</div>' for a, b in pasos)
    s.append(("recorrido", "Una petición de punta a punta", f"""
<p>Así atraviesa el código una acción de la directora de compras. Sirve para explicar las tres arquitecturas en un
solo ejemplo: entra por cliente-servidor, cruza el hexágono de afuera hacia adentro y sale como evento.</p>
<div class="flujo">{flujo}</div>
<p>El controlador solo traduce HTTP a una llamada al puerto; la regla vive en el dominio; la base y Kafka están detrás
de puertos de salida. Si mañana se cambia PostgreSQL o Kafka, cambia un adaptador, no el núcleo.</p>
{fragmento(f"{SERV}/ms-cotizaciones/src/main/java/co/edu/konradlorenz/cotizaciones/adaptadores/entrada/CotizacionRestController.java", '@PostMapping("/{numero}/validacion")', 0, "    }")}
"""))

    # 6 ------------------------------------------------------------------
    s.append(("patrones", "Los patrones de diseño en el código", f"""
<p>Los diez patrones de la tabla 11 del reporte, con la clase que hay que abrir para mostrarlos.</p>
{tabla(["Patrón", "Clases", "Carpeta"], [
    ["Facade", f"{c('GenerarCotizacionFacade')}; a escala de sistema, el API Gateway", c("cotizaciones/aplicacion")],
    ["Chain of Responsibility", f"{c('ReglaValidacion')} → {c('ReglaRechazada')} → {c('ReglaSospechosa')} → {c('ReglaOpcionada')}", c("cotizaciones/dominio")],
    ["Composite", f"{c('ElementoMenu')}, {c('GrupoMenu')}, {c('OpcionMenu')}", c("identidad/dominio")],
    ["Factory Method", f"{c('Usuario.construirMenu()')} en cada rol; {c('CreadorNotificacion')}, {c('CreadorCorreo')}, {c('CreadorAlarmaApp')}", f"{c('identidad/dominio')}, {c('notificaciones/dominio')}"],
    ["Decorator", f"{c('CorreoDecorator')}, {c('CorreoCertificadoDecorator')}, {c('EstampaCronologicaDecorator')} sobre {c('CorreoSmtpAdapter')}", c("notificaciones/adaptadores/salida")],
    ["Adapter", f"{c('MinisterioComercioAdapter')} (y todos los {c('*Adapter')})", c("integracion/adaptadores/salida")],
    ["Proxy", c("PrecioReferenciaCacheProxy"), c("integracion/adaptadores/salida")],
    ["Publish-Subscribe", f"{c('EventPublisherPort')}, {c('KafkaEventPublisher')}, {c('EventListener')} y los listeners", f"{c('compartido')} y cada servicio"],
    ["Singleton", c("ParametrosSistema.getInstancia()"), c("administracion/dominio")],
    ["Strategy", f"{c('CalculadorKpi')} → {c('ProductoMayorDemanda')}, {c('SucursalMayorDemanda')}, {c('ProveedorMejoresPrecios')}", c("analitica/dominio")],
], ["20%", None, "26%"])}
<h3>Chain of Responsibility: la validación de V19</h3>
<p>Cada regla decide o pasa a la siguiente; el orden se fija en un solo lugar.</p>
{fragmento(f"{NUCLEO}/cotizaciones/dominio/ReglaValidacion.java", "public static ReglaValidacion cadenaPorDefecto()", 0, "return siguiente.evaluar(c);")}
<h3>Facade: un clic de V13</h3>
<p>La fachada coordina precio, repositorio, PDF, almacén y evento; si el Ministerio no responde, la cotización no se pierde.</p>
{fragmento(f"{NUCLEO}/cotizaciones/aplicacion/GenerarCotizacionFacade.java", "public Cotizacion generarYEnviar(Cotizacion c)", 13)}
<h3>Proxy: la caché de precios del Ministerio</h3>
<p>Implementa el mismo puerto que el adaptador real y se pone delante; en la plataforma el mapa está en Redis.</p>
{fragmento(f"{NUCLEO}/integracion/adaptadores/salida/PrecioReferenciaCacheProxy.java", "public PrecioEstandar precioEstandar(Long productoId)", 0, "return nuevo;")}
<h3>Factory Method + Composite: el menú de cada rol</h3>
<p>Cada subclase de {c('Usuario')} decide qué árbol de menú construir; la SPA solo lo recorre.</p>
{fragmento(f"{NUCLEO}/identidad/dominio/DirectorCompras.java", "public ElementoMenu construirMenu()", 13)}
<h3>Singleton y Strategy</h3>
{fragmento(f"{NUCLEO}/administracion/dominio/ParametrosSistema.java", "public static synchronized ParametrosSistema getInstancia()", 6)}
{fragmento(f"{NUCLEO}/analitica/dominio/CalculadorKpi.java", "public interface CalculadorKpi", 0, "ResultadoKpi calcular(")}
"""))

    # 7 ------------------------------------------------------------------
    s.append(("eventos", "Eventos confiables: outbox y consumidores idempotentes", f"""
<p>Es lo que más se pregunta de la arquitectura orientada a eventos: ¿qué pasa si Kafka falla, o si un evento llega dos
veces? El código lo resuelve en la librería {c('compartido')}, igual para los once servicios.</p>
{tabla(["Problema", "Solución", "Clase"], [
    ["Se guarda el cambio pero Kafka no está: el evento se perdería.", "Outbox transaccional: el evento se escribe en la tabla outbox de la base del servicio, en la misma transacción que el cambio.", c("KafkaEventPublisher") + " + " + c("OutboxEventos")],
    ["Alguien tiene que llevar la outbox a Kafka.", "Un relé toma las filas en orden, las publica, espera la confirmación y las borra; con varias réplicas, un candado de PostgreSQL deja publicar a una sola a la vez.", c("ReleOutbox")],
    ["Kafka entrega «al menos una vez»: un evento puede llegar repetido.", "Cada consumidor guarda el id del evento en la misma transacción que su cambio y descarta los repetidos.", c("EventosProcesados")],
    ["Saber quién hizo qué y seguir un recorrido.", f"Cada evento lleva el usuario del JWT y el {c('correlacionId')} del recorrido.", f"{c('EventoDominio')}, {c('ContextoRecorrido')}"],
], ["30%", None, "24%"])}
{fragmento(f"{SERV}/compartido/src/main/java/co/edu/konradlorenz/compartido/adaptadores/KafkaEventPublisher.java", "/** Guarda cada evento en la outbox", 6)}
{fragmento(f"{SERV}/ms-cotizaciones/src/main/java/co/edu/konradlorenz/cotizaciones/adaptadores/entrada/OrdenGeneradaListener.java", "@Transactional", 0, "    }")}
"""))

    # 8 ------------------------------------------------------------------
    s.append(("modulos", "Clases principales de cada servicio", tabla(
        ["Servicio", "Entrada", "Núcleo (aplicación y dominio)", "Salida"], [
            ["ms-identidad", "AuthRestController, UsuarioRestController", "IniciarSesionService, RegistrarUsuarioService, Usuario y sus 6 roles, ElementoMenu, PoliticaContrasena", "UsuarioJpaAdapter, BCryptCifradorAdapter, JwtEmisorAdapter"],
            ["ms-pedidos", "PedidoRestController, MenuActualizadoListener", "PedidoService, Pedido ◆ DetallePedido, Mesa, CatalogoPlatos", "PedidoJpaAdapter, CatalogoJpaAdapter"],
            ["ms-menu", "PlatoRestController", "PlatoService, Plato, Ingrediente, Producto, Categoria", "PlatoJpaAdapter, AlmacenObjetosAdapter"],
            ["ms-inventario", "InventarioRestController, ConsumoDiarioJob, PedidoRegistradoListener, InventarioEventsListener", "InventarioService, Inventario ◇ InsumoInventario, RecetaPlato, UmbralStock", "InventarioJpaAdapter"],
            ["ms-solicitudes", "SolicitudRestController, CotizacionEventsListener", "SolicitudService, SolicitudAlimentos ◆ ItemSolicitud, CriteriosConsulta", "SolicitudJpaAdapter"],
            ["ms-cotizaciones", "CotizacionRestController, ProveedorRestController, OrdenGeneradaListener", "GenerarCotizacionFacade, CotizacionService, ProveedorService, Cotizacion, ReglaValidacion y sus 3 reglas", "CotizacionJpaAdapter, ProveedorJpaAdapter, PdfCotizacionAdapter, AlmacenObjetosAdapter, PrecioSugeridoRestAdapter"],
            ["ms-ordenes", "OrdenCompraRestController", "OrdenCompraService, OrdenCompra ◆ ItemOrden, CotizacionOpcionada", "OrdenJpaAdapter, PdfOrdenAdapter, AlmacenObjetosAdapter, CotizacionRestClient"],
            ["ms-notificaciones", "NotificacionListeners, DestinatarioRestController", "NotificacionService, CreadorNotificacion, Notificacion, Mensaje", "CorreoSmtpAdapter y sus decoradores, AlarmaPushAdapter, EnvioJpaAdapter, DestinatarioJpaAdapter"],
            ["ms-integracion", "AbastecimientoSoapEndpoint, PrecioRestController", "ConsultarPrecioService, PrecioEstandar", "PrecioReferenciaCacheProxy, MinisterioComercioAdapter, MinisterioSoapClient"],
            ["ms-administracion", "AdminRestController, AuditoriaListener", "AdministracionService, ParametrosSistema, Bitacora ◇ RegistroAuditoria, LogError", "AuditoriaJpaAdapter, ParametrosJpaAdapter, ElasticsearchLogsAdapter"],
            ["ms-analitica", "TableroRestController, PromocionRestController, NegocioEventsListener, TendenciasJob", "TableroService, CalculadorKpi y sus 3 estrategias, HechoNegocio, SugerenciaPromocion", "KpiJpaAdapter, SugerenciaJpaAdapter, RedesSocialesAdapter"],
        ], ["15%", "24%", None, "26%"]) +
        "<p>La guía de los microservicios explica cada uno con sus rutas, sus eventos y sus ventanas.</p>"))

    # 9 ------------------------------------------------------------------
    s.append(("spa", "La SPA (web/)", f"""
{tabla(["Carpeta", "Qué tiene"], [
    [c("src/main.tsx") + ", " + c("App.tsx"), "El arranque y las rutas: una por ventana, dentro de un armazón con el menú lateral."],
    [c("src/sesion/"), "El token JWT, el usuario, el menú del rol (llega en el token) y el tema que pone el administrador."],
    [c("src/api/"), f"{c('cliente.ts')}: cada llamada lleva {c('Authorization: Bearer …')}; si el gateway responde 401, vuelve al inicio de sesión. {c('servicios.ts')}: una función por endpoint."],
    [c("src/paginas/"), "Las ventanas, por rol: mesero/, jefe/, auxiliar/, director/, comercial/ y admin/, más Login, Inicio y Perfil."],
    [c("src/componentes/"), "Piezas comunes: encabezado, barra lateral, tarjetas, tablas de solicitudes, cotizaciones y órdenes, y la alarma en vivo (WebSocket)."],
    [c("src/estilos/"), "Los estilos del inicio de sesión y de la aplicación."],
], ["30%", None])}
<p>React 18.3.1, TypeScript 5.6.3, Vite 5.4.11 y React Router 6.28. Se compila dentro de Docker y la sirve Nginx.</p>
"""))

    # 10 -----------------------------------------------------------------
    s.append(("tecnologias", "Tecnologías y versiones", tabla(["Pieza", "Tecnología"], [
        ["Lenguaje y núcleo", "Java 21, sin frameworks"],
        ["Microservicios", "Spring Boot 3.5.0, Spring Cloud 2025.0.0 (Gateway, Eureka, LoadBalancer), Spring Security, Spring Data JPA, Spring Kafka, Flyway"],
        ["Bases de datos", "PostgreSQL 17 (una base y un usuario por servicio)"],
        ["Mensajería", "Apache Kafka 3.9.1 (KRaft)"],
        ["Caché y tasa", "Redis 7.4"],
        ["Almacén de objetos", "S3 compatible (SeaweedFS 3.97), con el SDK de AWS 2.31"],
        ["Logs", "Elasticsearch 8.19.4"],
        ["SOAP", "Apache CXF 4.1.5"],
        ["PDF", "OpenPDF 2.0.3"],
        ["Tareas programadas", "Spring Scheduling con ShedLock 6.9.0 (una sola réplica ejecuta cada tarea)"],
        ["SPA", "React 18.3.1, TypeScript 5.6.3, Vite 5.4.11, servida con Nginx"],
        ["Despliegue local", "Docker Compose; Maven corre dentro de Docker"],
    ], ["28%", None])))

    # 11 -----------------------------------------------------------------
    s.append(("pruebas", "Cómo se prueba y se ejecuta", f"""
{tabla(["Nivel", "Dónde", "Cuántas"], [
    ["Núcleo, sin frameworks", c("pruebas/") + " (8 clases, mini marco propio)", "400 verificaciones"],
    ["Servicios (JUnit)", c("servicios/*/src/test"), "91 pruebas"],
    ["De punta a punta", c("plataforma/pruebas/") + ": recorridos con navegador (Playwright) y verificadores de los cortes 3 a 7", "763 comprobaciones"],
    ["<b>Total</b> (" + c("plataforma/probar_todo.sh") + ", instancia limpia)", "", "<b>1.254, sin fallas</b>"],
], ["30%", None, "20%"])}
<pre><code>./ejecutar.sh                        # núcleo: compila, corre la demo y las 400 verificaciones
./plataforma/levantar.sh             # compila y levanta la plataforma: http://localhost:8080
./plataforma/bajar.sh                # la detiene
PYTHON=/ruta/al/venv/bin/python ./plataforma/probar_todo.sh /tmp/pruebas   # todo (unos 40 minutos)</code></pre>
"""))

    # 12 -----------------------------------------------------------------
    s.append(("guion", "Si piden ver el código: qué abrir y qué decir", """
<div class="guion"><h3>🎤 Guion sugerido (3 a 4 minutos)</h3>
<p><b>1. La estructura.</b> Abrimos <code>KonradGourmet/</code>. Tiene cuatro partes: el núcleo en <code>src/</code>, con
las 195 clases del diagrama de clases; los microservicios en <code>servicios/</code>; la SPA en <code>web/</code>, y la
plataforma en <code>plataforma/</code>, que levanta todo con Docker Compose.</p>
<p><b>2. La hexagonal.</b> Abrimos <code>src/…/cotizaciones/</code>: siempre las mismas carpetas, dominio, puertos de
entrada y de salida, aplicación y adaptadores. El dominio no importa Spring ni JPA: abrimos
<code>ReglaValidacion</code> y se ve que es Java puro, la cadena de responsabilidad de la validación.</p>
<p><b>3. Los microservicios.</b> Abrimos <code>servicios/ms-cotizaciones/</code>. No repite la lógica: el
<code>pom.xml</code> copia el núcleo de cotizaciones al compilar, y la clase de configuración lo arma con sus
adaptadores. El controlador REST solo traduce HTTP a una llamada al puerto y pide el rol con
<code>@PreAuthorize</code>.</p>
<p><b>4. Los eventos.</b> Abrimos <code>KafkaEventPublisher</code> en <code>compartido</code>: el evento no va directo
a Kafka, se guarda en la outbox en la misma transacción y un relé lo publica. Y en <code>OrdenGeneradaListener</code>
se ve que el consumidor descarta los eventos repetidos.</p>
<p><b>5. Las pruebas.</b> Cerramos con los números: 400 verificaciones del núcleo, 91 pruebas JUnit y 763
comprobaciones de punta a punta, 1.254 sin fallas en una instalación limpia.</p>
</div>
<h3>Si preguntan…</h3>
<dl class="preguntas">
<dt>¿Por qué el núcleo no usa Spring?</dt><dd>Para que la lógica no dependa de la tecnología: es lo que pide la hexagonal. Se puede probar sola (las 400 verificaciones corren sin base ni Kafka) y cambiar un adaptador no toca el dominio.</dd>
<dt>¿No es duplicar código copiar el núcleo en cada servicio?</dt><dd>No se duplica a mano: Maven lo copia al compilar desde una sola fuente. Cada servicio lleva solo su paquete y los eventos que consume, así que sigue siendo independiente al desplegarse.</dd>
<dt>¿Qué impide que un servicio lea la base de otro?</dt><dd>Cada servicio tiene su propio usuario de PostgreSQL, que solo puede entrar a su base; entre servicios solo hay REST o eventos.</dd>
<dt>¿Cómo se asegura que el código coincide con el diagrama?</dt><dd>El núcleo se generó de las páginas 4a a 4e, con los mismos paquetes, clases, métodos y relaciones; los métodos que atienden una ventana llevan el comentario «Atiende la ventana Vxx».</dd>
</dl>
"""))
    return s


def main():
    s = secciones()
    indice = '<nav class="indice"><strong>Contenido</strong><ol>' + "".join(
        f'<li><a href="#{i}">{E(t)}</a></li>' for i, t, _ in s) + "</ol></nav>"
    cuerpo = indice + "".join(f'<section class="bloque" id="{i}"><h2>{n}. {E(t)}</h2>{h}</section>'
                              for n, (i, t, h) in enumerate(s, 1))
    sub = ("Cómo está hecho el código de Konrad Gourmet: las arquitecturas que usa, cómo se reparte en carpetas, "
           "la estructura de cada servicio y sus clases principales. Los fragmentos se toman del repositorio "
           "<code>KonradGourmet/</code> al generar la guía.")
    html = pagina("Guía del código · Konrad Gourmet", sub, cuerpo)
    (GUIAS / "guia_codigo.html").write_text(html, encoding="utf-8")
    print(f"guia_codigo.html: {len(s)} secciones")


if __name__ == "__main__":
    main()
