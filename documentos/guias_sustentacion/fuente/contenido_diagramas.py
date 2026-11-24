"""Contenido de la guía de los diagramas: qué se numera en cada página y qué se dice.

Cada elemento es (id de la celda en el .drawio, posición del número, nombre, explicación).
La posición es 'si' (esquina superior izquierda), 'sd', 'ii', 'id' o 'c'; None = 'si'.
Las cifras salen del .drawio, del reporte v2 y de «Casos de uso — Konrad Gourmet.md».
"""

INTRO_ORDEN = [
    ("casos_de_uso", "Quién hace qué: los 19 casos de uso", "1 a 2 min"),
    ("2", "Nivel 1: el sistema como una caja", "1 a 2 min"),
    ("3a", "Nivel 2: los contenedores", "2 a 3 min"),
    ("1", "El mapa integrado: las tres arquitecturas juntas", "3 a 4 min"),
    ("3b", "Nivel 3: los componentes de ms-cotizaciones", "2 min"),
    ("4a", "Nivel 4: compartido e identidad", "1 a 2 min"),
    ("4b", "Nivel 4: pedidos, menú e inventario", "1 a 2 min"),
    ("4c", "Nivel 4: solicitudes y cotizaciones", "1 a 2 min"),
    ("4d", "Nivel 4: órdenes y notificaciones", "1 a 2 min"),
    ("4e", "Nivel 4: integración, administración y analítica", "1 a 2 min"),
    ("6", "El recorrido de una compra (diagrama dinámico)", "2 a 3 min"),
    ("5", "Despliegue para el 99,7 %", "2 min"),
]

DIAGRAMAS = {}

# ---------------------------------------------------------------- casos de uso
DIAGRAMAS["casos_de_uso"] = dict(
    titulo="Diagrama de casos de uso",
    origen="documentos/casos_de_uso.png (se genera de «Casos de uso — Konrad Gourmet.md»)",
    reporte="§10, figura 7",
    que_es=(
        "Es el diagrama UML de casos de uso: el rectángulo es el sistema y cada óvalo es un caso de uso (CU-01 a "
        "CU-19). A la izquierda y abajo a la derecha están los seis roles que usan el sistema; a la derecha, los cinco "
        "actores externos. No lleva números agregados: se explica con sus propios códigos CU."
    ),
    sin_pines=True,
    tabla_titulo="Los actores y los 19 casos de uso",
    tabla=[
        ("Usuario (abstracto)", "Actor en cursiva. Los seis roles heredan de él (flecha de punta hueca, generalización), por eso todos inician sesión (CU-01) sin dibujar seis líneas."),
        ("Mesero", "CU-02 Registrar pedido (V02 a V04)."),
        ("Jefe de cocina", "CU-03 Registrar plato con ingredientes (V06), CU-04 Registrar solicitud de alimentos (V08 a V10), CU-05 Recibir alerta de inventario bajo (V05, V07) y CU-06 Revisar promociones sugeridas."),
        ("Auxiliar de compras", "CU-07 Consultar solicitudes (V11, V12), CU-08 Enviar solicitud de cotización (V13 a V15) y CU-09 Registrar datos de proveedor (Proveedores)."),
        ("Director de compras", "CU-10 Registrar valores (V18), CU-11 Validar cotización (V16, V17, V19), CU-12 Consultar opcionadas (V20) y CU-13 Generar orden de compra (V21 a V23)."),
        ("Director comercial", "CU-14 Consultar tablero BAM."),
        ("Administrador", "CU-15 Parametrizar el sistema, CU-16 Consultar auditoría, CU-17 Consultar logs de errores y CU-19 Registrar usuario."),
        ("Proveedor", "Recibe el PDF de la cotización (CU-08) y de la orden (CU-13); responde por correo, fuera del sistema."),
        ("Ministerio de Comercio", "Da el precio estándar por producto (CU-08)."),
        ("Servidor de correo certificado", "Entrega los correos con certificado y sello de tiempo (CU-04, CU-05, CU-06, CU-08 y CU-13)."),
        ("Redes sociales", "Dan las señales de tendencia para las promociones (CU-06)."),
        ("Sistemas externos", "Consumen el servicio SOAP del restaurante: CU-18 Consultar precio sugerido."),
    ],
    guion=[
        "Antes de la arquitectura, mostramos quién usa el sistema. Son seis roles: mesero, jefe de cocina, auxiliar de "
        "compras, director de compras, director comercial y administrador. Todos heredan de un actor abstracto, Usuario, "
        "y por eso todos inician sesión con el mismo caso de uso, el CU-01.",
        "Los 19 casos de uso cubren las 19 historias de usuario. El flujo principal es el de abastecimiento: el jefe de "
        "cocina pide alimentos, el auxiliar envía la solicitud de cotización al proveedor con el precio del Ministerio, "
        "y el director de compras valida y genera la orden. Alrededor están el pedido del mesero, la alerta de inventario, "
        "el tablero BAM y las funciones del administrador.",
        "A la derecha están los actores externos: el proveedor, el Ministerio de Comercio, el servidor de correo "
        "certificado, las redes sociales y los sistemas externos que consumen nuestro servicio SOAP, que es el CU-18.",
    ],
    preguntas=[
        ("¿Por qué Usuario es abstracto?", "Porque nadie es solo «usuario»: siempre tiene un rol. La generalización evita repetir la asociación con CU-01 seis veces, y coincide con la clase Usuario «abstract» de la página 4a."),
        ("¿Por qué CU-18 no tiene ventana?", "Porque lo usa otro sistema, no una persona: es el servicio SOAP que exige el requisito de integración."),
    ],
)

# ---------------------------------------------------------------- página 2
DIAGRAMAS["2"] = dict(
    titulo="Nivel 1 del C4 · Diagrama de contexto",
    origen="Página «2 · C4 Contexto» del .drawio",
    reporte="§14.1, figura 12",
    que_es=(
        "Es el primer nivel del modelo C4: el sistema completo es una sola caja azul, y alrededor están las personas "
        "que lo usan y los sistemas externos con los que se integra. Cada flecha dice para qué se usa, qué ventanas "
        "intervienen y con qué protocolo."
    ),
    elementos=[
        ("sys", None, "Sistema de Abastecimiento Konrad Gourmet", "El sistema que construimos, visto desde afuera. Registra menús y pedidos, descuenta el inventario cada día, gestiona solicitudes, cotizaciones y órdenes de compra y da los KPI del negocio."),
        ("v2", None, "Mesero", "Registra pedidos por mesa (V02 a V04) por HTTPS. Cada rol tiene su color, que se repite en todas las páginas."),
        ("v4", None, "Jefe de cocina", "Registra platos, ve las alarmas de inventario, pide alimentos y revisa las promociones sugeridas (V05 a V10)."),
        ("v6", None, "Auxiliar de compras", "Consulta solicitudes, envía solicitudes de cotización y registra proveedores (V11 a V15)."),
        ("v8", None, "Director de compras", "Registra los valores del proveedor, valida cotizaciones y genera órdenes (V16 a V23)."),
        ("v10", None, "Director comercial", "Consulta los KPI del negocio en el tablero BAM."),
        ("v12", None, "Administrador", "Parametriza el sistema, registra usuarios y destinatarios y revisa la auditoría y los logs."),
        ("v14", None, "Ministerio de Comercio", "Sistema externo con un servicio web SOAP que publica el precio estándar de cada producto. Lo consultamos por SOAP/HTTPS."),
        ("v15", None, "Servidor de correo certificado", "Entrega los correos con certificado y estampado cronológico (SMTP/TLS). Por aquí salen los PDF al proveedor."),
        ("v16", None, "Redes sociales", "APIs públicas que dan señales de tendencia de productos para las promociones (HTTPS/REST)."),
        ("v17", None, "Sistemas externos", "Otros sistemas que consumen los servicios que expone el restaurante; la flecha va hacia el sistema (SOAP/HTTPS con OAuth 2.0)."),
        ("v22", "c", "Proveedor", "Persona externa (gris claro). Recibe por correo el PDF de la cotización y de la orden, y responde por correo, fuera del sistema; por eso el director digita sus valores en V18."),
        ("p2_leg_t", None, "Cómo leer el diagrama", "Flecha punteada oscura: relación con propósito, ventanas y protocolo. Flecha punteada gris: lo que ocurre fuera del sistema (el correo entre el servidor, el proveedor y el director)."),
    ],
    guion=[
        "Empezamos por el nivel 1 del C4, el contexto. Aquí el sistema es una sola caja: el Sistema de Abastecimiento "
        "Konrad Gourmet. Arriba están las seis personas que lo usan, cada una con su color, que se mantiene en todos "
        "los diagramas. Cada flecha dice qué hace esa persona, en qué ventanas y por qué protocolo: todas por HTTPS.",
        "Abajo están los cuatro sistemas externos. Del Ministerio de Comercio tomamos el precio estándar por SOAP; "
        "el servidor de correo certificado entrega los correos con sello de tiempo; las redes sociales nos dan "
        "tendencias para las promociones, y otros sistemas consumen nuestros servicios SOAP.",
        "El proveedor es una persona externa: no entra al sistema. Recibe el PDF por correo y responde por correo; "
        "por eso el director de compras digita sus valores en la ventana 18. Esa decisión está en los supuestos del reporte.",
    ],
    preguntas=[
        ("¿Por qué el proveedor no usa el sistema?", "Es el supuesto S2 del reporte: el proveedor responde por correo y el director digita los valores. Evita dar acceso a terceros y mantiene el correo certificado como respaldo legal."),
    ],
)

# ---------------------------------------------------------------- página 3a
DIAGRAMAS["3a"] = dict(
    titulo="Nivel 2 del C4 · Diagrama de contenedores",
    origen="Página «3a · C4 Contenedores» del .drawio",
    reporte="§14.2, figura 13",
    que_es=(
        "Abre la caja del nivel 1 y muestra las aplicaciones y los almacenes de datos que la forman: la SPA, el "
        "servidor web, el API Gateway, el Service Registry, los once microservicios con su base de datos, el broker "
        "de eventos, Redis, el almacén de objetos y los logs. Cada flecha lleva su propósito y su protocolo."
    ),
    elementos=[
        ("c3a_boundary", None, "Límite del sistema", "Todo lo que está dentro del recuadro punteado es nuestro; lo de afuera son personas y sistemas externos."),
        ("c3a_p_mesero", None, "Las seis personas", "Los mismos seis roles del nivel 1, con su color. Todos usan la SPA por HTTPS."),
        ("c3a_p_proveedor", "c", "Proveedor", "Responde por correo, fuera del sistema; el director digita los valores en V18."),
        ("c3a_web", None, "Servidor web (Nginx)", "Entrega los archivos estáticos de la SPA, detrás de una CDN con WAF."),
        ("c3a_spa", None, "SPA Konrad Gourmet", "React + TypeScript, se ejecuta en el navegador y es responsive (web y móvil). El menú lateral de cada rol llega en el JWT."),
        ("c3a_gateway", None, "API Gateway «Facade»", "Spring Cloud Gateway: punto único de entrada. Valida el JWT, limita la tasa y enruta por prefijo (13 prefijos para los 11 servicios)."),
        ("c3a_registry", None, "Service Registry (Eureka)", "Directorio vivo de instancias: cada servicio se registra y late cada 30 s; el gateway le pregunta a qué instancia sana enviar cada petición. Dos réplicas."),
        ("c3a_elk", None, "Logs de error (Elasticsearch)", "Reúne los logs de error de los 11 servicios (RNF de mantenimiento); los consulta el administrador desde ms-administracion."),
        ("c3a_group", None, "Los once microservicios", "Spring Boot 3 con Java 21 en Kubernetes: once contenedores sin estado, uno por subdominio, cada uno dueño de su base. Por dentro son hexagonales (páginas 4a a 4e)."),
        ("c3a_ms_cotizaciones", None, "Un microservicio con su base", "Ejemplo: ms-cotizaciones con su BD cotizaciones [PostgreSQL]. Ningún servicio lee la base de otro: se comunican por REST o por eventos."),
        ("c3a_db_objetos", None, "Almacén de objetos [S3]", "Guarda los PDF de cotizaciones y órdenes y las imágenes de los platos. Kafka no lleva archivos: el evento lleva solo la dirección del PDF (claim check)."),
        ("c3a_ms_integracion", "sd", "ms-integracion", "La frontera SOA: consume el servicio web SOAP del Ministerio y expone nuestros servicios SOAP en /api/integracion/**."),
        ("c3a_db_integracion", None, "Redis", "Tres usos: la caché de precios del Ministerio (Proxy), el canal pub/sub de las alarmas en vivo y los baldes del límite de tasa del gateway."),
        ("c3a_db_integracion_pg", None, "BD integración", "PostgreSQL de ms-integracion, con su tabla outbox para publicar eventos de forma confiable."),
        ("c3a_bus", None, "Broker de eventos (Kafka)", "Tres brokers, factor de réplica 3, retención 7 días, los 10 tópicos y la cola de errores notificaciones.dlq. Coreografía publicar/suscribir, entrega al menos una vez y consumidores idempotentes."),
        ("c3a_x_sistemas", None, "Sistemas externos", "Consumen nuestro servicio SOAP a través del gateway."),
        ("c3a_x_ministerio", None, "Ministerio de Comercio", "Servicio web SOAP con el precio estándar; solo ms-integracion habla con él."),
        ("c3a_x_correo", None, "Servidor de correo certificado", "ms-notificaciones le envía los correos por SMTP con sello de tiempo."),
        ("c3a_x_redes", None, "Redes sociales", "ms-analitica lee las tendencias por HTTPS."),
        ("c3a_leg_t", None, "Cómo leer el diagrama", "Flecha continua: llamada síncrona (REST, SOAP, SMTP o WebSocket). Punteada ámbar: evento hacia o desde Kafka. Gris: acceso a datos y registro en Eureka."),
        ("c3a_notes", None, "Qué se simplificó", "Las rutas del gateway van como una sola flecha; el balanceador, las réplicas y las zonas están en la página 5; el interior de cada servicio, en las páginas 4a a 4e."),
    ],
    guion=[
        "En el nivel 2 abrimos la caja. Las personas usan la SPA, hecha en React y TypeScript, que entrega un servidor "
        "Nginx. Toda petición de la SPA pasa por el API Gateway, que es una fachada: valida el token JWT, limita la tasa "
        "de peticiones y enruta por prefijo al servicio que corresponde. Para saber qué instancia está viva, le pregunta "
        "al Service Registry, Eureka.",
        "En el centro están los once microservicios, uno por subdominio: identidad, pedidos, menú, inventario, "
        "solicitudes, cotizaciones, órdenes, notificaciones, administración, analítica e integración. Cada uno tiene "
        "su propia base PostgreSQL y ninguno lee la base de otro.",
        "Abajo está Kafka, el broker de eventos, con los diez tópicos. Así se comunican los servicios cuando no hace "
        "falta esperar una respuesta: por ejemplo, cuando se registra un pedido, inventario y analítica se enteran por "
        "el evento pedido.registrado.",
        "A los lados están los apoyos: Redis para la caché de precios, las alarmas y el límite de tasa; el almacén de "
        "objetos para los PDF y las imágenes, y Elasticsearch para los logs de error. ms-integracion es la única puerta "
        "al Ministerio por SOAP, y también expone nuestro servicio SOAP a otros sistemas.",
    ],
    preguntas=[
        ("¿Por qué una base por servicio?", "Para que cada servicio se despliegue, escale y falle por separado. El costo es la consistencia eventual, que se maneja con eventos, la outbox y consumidores idempotentes."),
        ("¿Por qué Kafka y no solo REST?", "Porque muchas reacciones no deben bloquear al usuario (correo, KPI, descuento de inventario). Con eventos, el que publica no conoce a los que consumen y un servicio caído no detiene a los demás."),
    ],
)

# ---------------------------------------------------------------- página 1
DIAGRAMAS["1"] = dict(
    titulo="Mapa integrado de las tres arquitecturas",
    origen="Página «1 · Mapa integrado» del .drawio",
    reporte="§9, figuras 1 a 6",
    que_es=(
        "Es la vista que integra las tres arquitecturas en una sola página: microservicios (la estructura), orientada "
        "a eventos (cómo se comunican los roles) y hexagonal (el interior de cada servicio). Se lee de arriba abajo: "
        "personas, ventanas de la SPA, entrada (balanceador, gateway y registro), los once servicios, Kafka y, a la "
        "derecha, los sistemas externos. Es la página más grande: conviene acercarse con los botones."
    ),
    elementos=[
        ("v5", None, "Personas", "Una por rol, con su color, sobre las ventanas que usa."),
        ("v2", None, "SPA Konrad Gourmet", "El contenedor cliente (React + TypeScript). Cada miniatura de ventana indica el servicio y el endpoint que la atiende."),
        ("v3", None, "Acceso · V01", "Inicio de sesión para todos los roles: ms-identidad, POST /api/auth/token."),
        ("v6", None, "Ventanas del mesero", "V02 a V04: inicio, nuevo pedido y pedido registrado (PED-0251), con ms-pedidos."),
        ("v11", None, "Ventanas del jefe de cocina", "V05 a V10: inicio con alarmas, nuevo plato, inventario, nueva solicitud, solicitud registrada (SOL-031) y mis solicitudes."),
        ("v19", None, "Ventanas del auxiliar de compras", "V11 a V15: solicitudes, detalle, generar solicitud de cotización, cotización enviada y cotizaciones."),
        ("v26", None, "Ventanas del director de compras", "V16 a V23: pendientes, todas, registrar valores, validación, opcionadas, generar orden, orden generada (OC-0012) y órdenes de compra."),
        ("v36", None, "Las otras diez ventanas", "Editar y desactivar plato, promociones sugeridas, proveedores, reenviar cotización, tablero BAM, parámetros y tema, auditoría, logs de errores, usuarios y destinatarios de alertas, cada una con su rol, su servicio y sus rutas. Con las 23 del prototipo son 33."),
        ("lb", None, "Balanceador de carga L7", "Activo-activo con TLS: reparte entre las réplicas del gateway y revisa su salud."),
        ("gw", None, "API Gateway «Facade»", "Punto único de entrada: valida el JWT, limita la tasa y enruta a la instancia sana que indica el registro."),
        ("reg", None, "Service Registry (Eureka)", "Directorio vivo de instancias; latido cada 30 s."),
        ("v208", None, "Prefijos del gateway", "Encima de cada servicio, las rutas que el gateway le envía: 13 prefijos para los 11 servicios (por ejemplo /api/pedidos/**)."),
        ("v51", None, "Plataforma de microservicios", "Kubernetes en 3 zonas más un centro de datos alterno; una base de datos por servicio."),
        ("v53", None, "Cómo se lee un servicio (ms-identidad)", "Encabezado azul con el nombre, la tecnología y, a la derecha, las ventanas que atiende (V01). Debajo, una línea con su responsabilidad."),
        ("ad_identidad_t0", "ii", "Adaptadores de arriba", "Cajas verde claro: los adaptadores de entrada (REST) y de salida síncrona (cifrado BCrypt, emisor del JWT). Conducen o usan el núcleo a través de puertos."),
        ("hex_identidad", "sd", "Núcleo hexagonal", "El hexágono verde: dominio y casos de uso, sin Spring, JPA ni Kafka. Dentro va la relación UML principal: aquí, la herencia de Usuario con un menú por rol."),
        ("ad_identidad_b1", None, "Persistencia", "Adaptador JPA que implementa el puerto de repositorio y guarda en la base del servicio."),
        ("db_identidad", "ii", "Base del servicio", "BD identidad [PostgreSQL]: cada servicio tiene la suya."),
        ("ad_identidad_b2", None, "Publicador de eventos", "KafkaEventPublisher: escribe el evento en la outbox y un relé lo publica en Kafka."),
        ("v71", None, "ms-pedidos", "Pedidos por mesa (V02 a V04). Composición Pedido ◆ DetallePedido. Escala en horas pico; recibe el catálogo por menu.actualizado."),
        ("v83", None, "ms-menu", "Platos, recetas, categorías y productos (V06); guarda la imagen del plato en el almacén de objetos."),
        ("v95", None, "ms-inventario", "Existencias por sucursal, descuento diario (ConsumoDiarioJob, 23:59) y umbral del 25 %. Agregación Inventario ◇ InsumoInventario."),
        ("v108", None, "ms-solicitudes", "Solicitudes de alimentos y sus estados (V08 a V12): PENDIENTE, EN TRÁMITE, COMPLETADA."),
        ("v120", None, "ms-cotizaciones", "Solicitud de cotización, PDF, registro de valores, validación y proveedores (V13 a V20)."),
        ("v126", None, "Chain of Responsibility", "Las tres reglas de validación: más de 25 % por encima, RECHAZADA; más de 50 % por debajo, SOSPECHOSA; el resto, OPCIONADA."),
        ("v140", None, "ms-ordenes", "Órdenes de compra desde cotizaciones OPCIONADAS y su PDF (V21 a V23); consulta la cotización por REST a ms-cotizaciones."),
        ("v152", None, "ms-administracion", "Parámetros e imagen corporativa, auditoría y logs."),
        ("v158", None, "Singleton", "ParametrosSistema: una sola fuente de parámetros por réplica (tema, logos)."),
        ("v163", None, "ms-analitica", "Tablero BAM (KPI), tendencias y promociones sugeridas; lee las redes sociales."),
        ("v166", None, "Strategy", "CalculadorKpi con un cálculo intercambiable por KPI: producto y sucursal con mayor demanda y proveedor con mejores precios."),
        ("v180", None, "ms-notificaciones", "Correos certificados con sello de tiempo y la alarma en vivo de V05 (WebSocket)."),
        ("v183", None, "Factory Method + Decorator", "Factory Method crea la notificación (correo o alarma); los decoradores agregan certificado y sello de tiempo al correo SMTP."),
        ("v194", None, "ms-integracion", "Frontera SOA: consume el SOAP del Ministerio y expone servicios SOAP; apoya V13 y V14."),
        ("ad_integracion_b1", None, "Proxy con caché", "PrecioReferenciaCacheProxy: si el precio está en Redis, responde; si no, lo pide al Ministerio y lo guarda un día."),
        ("db_integracion", "ii", "Redis", "Caché de precios compartida por las réplicas."),
        ("db_objetos", None, "Almacén de objetos [S3]", "PDF e imágenes de platos; lo usan ms-menu, ms-cotizaciones, ms-ordenes y ms-notificaciones."),
        ("db_elk", None, "Logs de error [Elasticsearch]", "Los consulta ms-administracion."),
        ("ext_ministerio", None, "Ministerio de Comercio", "Servicio web SOAP con el precio estándar."),
        ("ext_sistemas", None, "Sistemas externos", "Consumen nuestro SOAP."),
        ("ext_correo", None, "Servidor de correo certificado", "SMTP + TLS + sello de tiempo."),
        ("ext_redes", None, "Redes sociales", "Señales de tendencia por HTTPS."),
        ("v225", "c", "Proveedor", "Recibe los PDF por correo."),
        ("bus", None, "Broker de eventos (Kafka)", "La banda ámbar: coreografía publicar/suscribir, eventos inmutables, entrega al menos una vez, consumidores idempotentes y outbox transaccional."),
        ("chip_pedido_registrado", None, "Los diez tópicos", "Cada chip es un tópico. Las flechas punteadas ámbar bajan desde el publicador al tópico y suben del tópico a cada listener que lo consume."),
        ("v260", None, "Leyenda", "Cómo leer el mapa (las cuatro arquitecturas), las relaciones UML y C4 y los elementos. Es la misma notación de la tabla 7 del reporte."),
    ],
    guion=[
        "Esta página junta las tres arquitecturas en una sola vista y se lee de arriba abajo. Arriba están las personas "
        "y, debajo, la SPA con sus ventanas agrupadas por rol: las 23 del prototipo y las otras diez, como proveedores, "
        "el tablero BAM y las del administrador. Cada miniatura dice qué servicio y qué endpoint la atiende, así que "
        "desde una ventana se llega directo a su servicio.",
        "Las peticiones bajan por HTTPS al balanceador y al API Gateway, que valida el token, limita la tasa y enruta "
        "con ayuda de Eureka. Encima de cada servicio están sus prefijos: son 13 para los 11 servicios.",
        "La fila central es la arquitectura de microservicios, y cada caja muestra la hexagonal por dentro. Tomemos "
        "ms-identidad: arriba, los adaptadores de entrada, como el controlador REST; en el centro, el hexágono con el "
        "dominio y los casos de uso, que no conoce Spring ni Kafka; abajo, la persistencia con su propia base y el "
        "publicador de eventos. Todos los servicios siguen la misma forma, y dentro de cada hexágono está el patrón "
        "principal: la cadena de reglas en cotizaciones, el Singleton en administración, Strategy en analítica y "
        "Factory Method con Decorator en notificaciones.",
        "Abajo, la banda ámbar es la arquitectura orientada a eventos: Kafka con sus diez tópicos. Las flechas punteadas "
        "bajan del servicio que publica al tópico y suben a los que consumen. A la derecha está la frontera SOA: "
        "ms-integracion habla SOAP con el Ministerio, con un Proxy que guarda los precios en Redis.",
    ],
    preguntas=[
        ("¿Dónde está cada arquitectura en el mapa?", "Microservicios: la fila de cajas azules. Eventos: la banda ámbar con los tópicos. Hexagonal: el hexágono verde y los adaptadores de cada servicio. Cliente-servidor y SOA: la SPA con el gateway y ms-integracion con el Ministerio."),
        ("¿Por qué el gateway es una fachada?", "Porque esconde los once servicios detrás de un solo punto de entrada: la SPA solo conoce el gateway, que además concentra la validación del token y el límite de tasa."),
    ],
)

# ---------------------------------------------------------------- página 3b
DIAGRAMAS["3b"] = dict(
    titulo="Nivel 3 del C4 · Componentes de ms-cotizaciones",
    origen="Página «3b · C4 Componentes · ms-cotizaciones» del .drawio",
    reporte="§14.3, figura 14",
    que_es=(
        "Hace zoom sobre un solo contenedor, ms-cotizaciones, y muestra sus componentes: los controladores, el núcleo "
        "hexagonal (casos de uso, fachada, reglas y modelo) y los adaptadores hacia la base, el almacén de objetos, "
        "Kafka y ms-integracion. Se eligió este servicio porque atiende el flujo de compra de V13 a V20 y concentra "
        "tres patrones: Facade, Chain of Responsibility y Adapter."
    ),
    elementos=[
        ("c3b_p_auxiliar", "c", "Auxiliar de compras", "Genera y consulta cotizaciones y gestiona proveedores."),
        ("c3b_p_director", "c", "Director de compras", "Registra los valores del proveedor y valida."),
        ("c3b_spa", None, "SPA", "Las ventanas V13 a V20 que usan este servicio."),
        ("c3b_gateway", None, "API Gateway", "Valida el JWT y enruta por prefijo (/api/cotizaciones/** y /api/proveedores/**)."),
        ("c3b_ordenes", None, "ms-ordenes", "Otro contenedor: lee la cotización OPCIONADA por REST para generar la orden."),
        ("c3b_boundary", None, "Límite de ms-cotizaciones", "Todo lo de adentro es un solo contenedor desplegable."),
        ("c3b_controller", None, "CotizacionRestController", "Adaptador de entrada (Spring MVC). Cada endpoint dice qué ventana atiende: POST /api/cotizaciones (V13), valores (V18), validación (V19), reenvío (HU-12)…"),
        ("c3b_prov_controller", None, "ProveedorRestController", "Adaptador de entrada de /api/proveedores/**: registrar, consultar, editar e inactivar."),
        ("c3b_hex", None, "Núcleo hexagonal", "Dominio y aplicación. No importa Spring, JPA, Kafka ni HTTP: todo lo de afuera entra y sale por un puerto."),
        ("c3b_app", None, "Casos de uso de cotización", "El componente de aplicación que implementa GestionarCotizacionUseCase."),
        ("c3b_service", "sd", "CotizacionService", "Registra valores, valida, consulta, entrega el PDF desde el almacén, reenvía y marca CON_ORDEN."),
        ("c3b_facade", None, "GenerarCotizacionFacade «Facade»", "Un clic de V13 coordina todo: precio sugerido por producto, guardar, generar el PDF, subirlo al almacén y publicar cotizacion.enviada. Si el Ministerio no responde, la deja PENDIENTE_PRECIO."),
        ("c3b_prov_service", None, "Gestión de proveedores", "ProveedorService: NIT único, correo válido, inactivar sin borrar."),
        ("c3b_reglas", None, "Reglas de validación «Chain of Responsibility»", "Rechazada (más de +25 %) → Sospechosa (más de −50 %) → Opcionada (el resto). COT-018: +4,81 %, OPCIONADA."),
        ("c3b_modelo", None, "Modelo de cotización", "Cotizacion ◆ ItemCotizacion, Proveedor y EstadoCotizacion. Calcula totales y la diferencia porcentual."),
        ("c3b_ad_jpa", None, "Adaptadores JPA", "Implementan los puertos de repositorio de cotizaciones y proveedores."),
        ("c3b_ad_kafka", None, "KafkaEventPublisher", "De la librería compartido: escribe el evento en la tabla outbox y un relé lo publica en Kafka."),
        ("c3b_ad_listener", None, "OrdenGeneradaListener", "Adaptador de entrada que recibe orden.generada y pasa la cotización a CON_ORDEN."),
        ("c3b_ad_pdf", None, "PdfCotizacionAdapter", "Genera el PDF con OpenPDF (puerto GeneradorPdfPort)."),
        ("c3b_ad_almacen", None, "AlmacenObjetosAdapter", "Guarda el PDF en el almacén S3 (puerto AlmacenArchivosPort)."),
        ("c3b_ad_precio", None, "PrecioSugeridoRestAdapter", "Cliente REST hacia ms-integracion (puerto PrecioSugeridoPort)."),
        ("c3b_bd", None, "BD cotizaciones", "Su base PostgreSQL."),
        ("c3b_broker", None, "Broker de eventos", "Quién consume lo que publica este servicio: notificaciones y solicitudes (cotizacion.enviada), analítica (cotizacion.validada) y administración (auditoria.evento)."),
        ("c3b_objetos", None, "Almacén de objetos", "Donde queda el PDF; el evento solo lleva su dirección (claim check)."),
        ("c3b_integracion", None, "ms-integracion", "Responde el precio desde la caché (Proxy) o consulta el SOAP del Ministerio."),
        ("c3b_notes", None, "Decisiones del diagrama", "Servicio y fachada forman un componente; los puertos se nombran en las flechas que cruzan el hexágono; el PDF viaja como claim check."),
    ],
    guion=[
        "En el nivel 3 entramos a un contenedor. Escogimos ms-cotizaciones porque atiende la parte central del flujo de "
        "compra, de la ventana 13 a la 20, y porque reúne tres patrones.",
        "Las peticiones entran por dos controladores REST, uno de cotizaciones y otro de proveedores. Esos controladores "
        "son adaptadores de entrada: no tienen lógica, llaman a un puerto del núcleo. El núcleo hexagonal, en verde, "
        "no conoce Spring, JPA, Kafka ni HTTP.",
        "Dentro está la fachada GenerarCotizacionFacade: con un solo clic del auxiliar pide el precio sugerido de cada "
        "producto a ms-integracion, guarda la cotización, genera el PDF, lo sube al almacén y publica cotizacion.enviada. "
        "Si el Ministerio no responde, la cotización no se pierde: queda pendiente de precio y se puede reenviar.",
        "Cuando el director valida, actúa la cadena de reglas: primero se pregunta si está más de 25 % por encima, "
        "luego si está más de 50 % por debajo y, si no, queda opcionada. Con COT-018 la diferencia es +4,81 %, así que "
        "queda opcionada. Abajo están los adaptadores de salida, cada uno detrás de su puerto: la base, el PDF, el "
        "almacén, el cliente REST de precios y el publicador de eventos con la outbox.",
    ],
    preguntas=[
        ("¿Por qué una fachada y no el controlador haciendo todo?", "Porque generar una cotización coordina cinco cosas (precio, repositorio, PDF, almacén y evento); la fachada las ordena en un lugar y el controlador hace una sola llamada."),
        ("¿Qué pasa si el Ministerio no responde?", "La fachada guarda la cotización como PENDIENTE_PRECIO, deja el error en el log y avisa; luego se reenvía desde Cotizaciones (HU-12)."),
    ],
)

# ---------------------------------------------------------------- 4a
_LEER4 = ("Las páginas 4a a 4e son el nivel 4 del C4: diagramas de clases UML generados del mismo modelo que el "
          "código Java (paquetes co.edu.konradlorenz.…). En total son 195 clases. Los métodos marcados ‹Vxx› atienden "
          "esa ventana, y en magenta va el patrón de diseño de cada clase. Cada paquete se lee igual: arriba los "
          "adaptadores de entrada, luego el puerto de entrada (UseCase) y el servicio de aplicación, en el centro el "
          "dominio y abajo los puertos de salida con sus adaptadores.")

DIAGRAMAS["4a"] = dict(
    titulo="Nivel 4 · Código · compartido e identidad",
    origen="Página «4a · Código · compartido e identidad» del .drawio",
    reporte="§12 y §13, figura 9",
    que_es=_LEER4 + " Esta página tiene la librería compartido, que usan todos los servicios, y ms-identidad.",
    elementos=[
        ("pkg_compartido", None, "Librería compartido", "Código común a los once servicios: la base de los eventos y cómo se publican y consumen."),
        ("cls_compartido_EventoDominio", None, "EventoDominio «abstract»", "Todo evento hereda id, usuario, correlacionId y fecha. El usuario es quien hizo la acción; la correlación une los eventos de un mismo recorrido."),
        ("cls_compartido_EventPublisherPort", None, "EventPublisherPort", "El puerto de salida para publicar (Publish-Subscribe): el núcleo publica sin saber que existe Kafka."),
        ("cls_compartido_KafkaEventPublisher", None, "KafkaEventPublisher", "El adaptador que implementa ese puerto con Kafka."),
        ("cls_compartido_EventListener", None, "EventListener", "La interfaz de los suscriptores (listeners)."),
        ("n_outbox", None, "Eventos confiables", "Outbox transaccional: el evento se guarda en la tabla outbox en la misma transacción que el cambio y un relé lo publica después. Cada consumidor guarda los id procesados y descarta los repetidos."),
        ("n_infra_compartido", None, "Infraestructura que no se dibuja", "Las clases técnicas que no son del diseño: formato JSON de los eventos, contexto del usuario y la correlación, seguridad del JWT, envío de errores a Elasticsearch, la outbox, el relé y los eventos procesados."),
        ("pkg_identidad", None, "ms-identidad", "Inicio de sesión, usuarios, roles y menú por rol."),
        ("cls_identidad_AuthRestController", None, "AuthRestController", "Adaptador de entrada: POST /api/auth/token (V01)."),
        ("cls_identidad_UsuarioRestController", None, "UsuarioRestController", "Adaptador de entrada: POST /api/usuarios, el alta de usuarios del administrador (CU-19)."),
        ("cls_identidad_IniciarSesionUseCase", None, "IniciarSesionUseCase", "Puerto de entrada: lo que el controlador puede pedirle al núcleo."),
        ("cls_identidad_IniciarSesionService", None, "IniciarSesionService", "Servicio de aplicación: busca el usuario, verifica la clave, arma el menú y emite el token."),
        ("cls_identidad_UsuarioRepositoryPort", None, "Puertos de salida", "UsuarioRepositoryPort, CifradorPort y EmisorTokenPort: lo que el núcleo necesita de afuera."),
        ("cls_identidad_UsuarioJpaAdapter", None, "Adaptadores de salida", "UsuarioJpaAdapter (base), BCryptCifradorAdapter (hash de la clave) y JwtEmisorAdapter: la línea punteada con triángulo hueco es «implementa»."),
        ("cls_identidad_JwtEmisorAdapter", None, "JwtEmisorAdapter «RS256»", "Firma el JWT con una llave RSA; el gateway y cada servicio validan con la llave pública."),
        ("cls_identidad_Usuario", None, "Usuario «abstract» · Factory Method", "Cada rol implementa construirMenu(): la subclase decide qué menú crear."),
        ("cls_identidad_JefeCocina", None, "Los seis roles", "Mesero, JefeCocina, AuxiliarCompras, DirectorCompras, DirectorComercial y Administrador heredan de Usuario."),
        ("cls_identidad_ElementoMenu", None, "ElementoMenu · Composite", "El menú lateral es un árbol: GrupoMenu (compuesto) contiene OpcionMenu (hoja). La SPA solo recorre el árbol."),
        ("cls_identidad_PoliticaContrasena", None, "PoliticaContrasena", "La regla de contraseña del alta de usuarios (8 o más caracteres, mayúscula…)."),
        ("n_altausuarios", None, "Alta de usuarios", "Crea la subclase según el rol, aplica la política, guarda solo el hash BCrypt y publica auditoria.evento."),
        ("ley_t", None, "Cómo leer el diagrama", "La leyenda de las páginas de clases: realización (implementa), herencia, composición ◆, agregación ◇, asociación y dependencia; verde oscuro, dominio y aplicación; verde claro, adaptadores; ámbar, eventos. Debajo, «Dentro de cada clase»: gris arriba el estereotipo y la capa, magenta el patrón y ‹Vxx› la ventana."),
    ],
    guion=[
        "En el nivel 4 vemos las clases. Esta primera página tiene dos paquetes. A la izquierda, la librería compartido: "
        "todos los eventos heredan de EventoDominio, que lleva el id, el usuario, la correlación y la fecha. El núcleo "
        "publica a través de un puerto, EventPublisherPort, y el adaptador de Kafka lo implementa. Además usamos outbox "
        "transaccional: el evento se guarda en la misma transacción que el cambio y un relé lo publica, así no se pierde "
        "aunque Kafka falle en ese momento.",
        "A la derecha está ms-identidad, que muestra bien la hexagonal: el controlador REST de la V01 llama al puerto "
        "IniciarSesionUseCase; el servicio usa tres puertos de salida, el repositorio, el cifrador y el emisor del "
        "token, y cada uno tiene su adaptador: JPA, BCrypt y JWT firmado con RS256.",
        "Aquí hay dos patrones. Factory Method: Usuario es abstracto y cada rol construye su propio menú. Y Composite: "
        "ese menú es un árbol de grupos y opciones, que viaja en el token y la SPA solo lo recorre. Por eso agregar un "
        "rol no obliga a cambiar la SPA.",
    ],
)

DIAGRAMAS["4b"] = dict(
    titulo="Nivel 4 · Código · pedidos, menú e inventario",
    origen="Página «4b · Código · pedidos, menú e inventario» del .drawio",
    reporte="§13",
    que_es=_LEER4 + " Esta página tiene ms-pedidos, ms-menu y ms-inventario: lo que pasa desde el plato hasta el descuento de inventario.",
    elementos=[
        ("pkg_pedidos", None, "ms-pedidos", "Pedidos por mesa (V02 a V04)."),
        ("cls_pedidos_PedidoRestController", None, "PedidoRestController", "Adaptador de entrada de /api/pedidos."),
        ("cls_pedidos_MenuActualizadoListener", None, "MenuActualizadoListener", "Recibe menu.actualizado y actualiza la copia local del catálogo de platos."),
        ("cls_pedidos_RegistrarPedidoUseCase", None, "RegistrarPedidoUseCase", "Puerto de entrada: registrar un pedido y consultar los del turno."),
        ("cls_pedidos_PedidoService", None, "PedidoService", "Valida el pedido contra el catálogo, lo guarda y publica pedido.registrado."),
        ("cls_pedidos_Pedido", None, "Pedido", "Número, mesa y mesero; compone sus líneas (◆)."),
        ("cls_pedidos_DetallePedido", None, "DetallePedido", "Plato y cantidad: no existe sin su pedido (composición)."),
        ("cls_pedidos_PedidoRegistradoEvent", None, "PedidoRegistradoEvent", "El evento con las líneas del pedido; extiende EventoDominio."),
        ("cls_pedidos_PedidoRepositoryPort", None, "PedidoRepositoryPort", "Puerto de salida de persistencia."),
        ("cls_pedidos_PedidoJpaAdapter", None, "PedidoJpaAdapter", "Lo implementa con JPA."),
        ("n_catalogo", None, "Catálogo en la base", "Cada réplica lee el catálogo de su base, no de memoria: cualquier réplica atiende V03."),
        ("pkg_menu", None, "ms-menu", "Platos, recetas, categorías y productos (V06)."),
        ("cls_menu_PlatoRestController", None, "PlatoRestController", "Adaptador de entrada de /api/platos."),
        ("cls_menu_PlatoService", None, "PlatoService", "Registra, edita, desactiva y reactiva platos y publica menu.actualizado."),
        ("cls_menu_Plato", None, "Plato", "Con su sucursal, su creador y su receta."),
        ("cls_menu_Ingrediente", None, "Ingrediente", "Producto, cantidad y unidad de la receta."),
        ("cls_menu_AlmacenArchivosPort", None, "AlmacenArchivosPort", "Puerto para guardar la imagen del plato; lo implementa AlmacenObjetosAdapter (S3)."),
        ("cls_menu_MenuActualizadoEvent", None, "MenuActualizadoEvent", "Avisa a pedidos, inventario y analítica que cambió un plato."),
        ("n_platos", "sd", "Reglas del plato", "Nombre único en la sucursal; editar, desactivar y reactivar publican menu.actualizado."),
        ("pkg_inventario", None, "ms-inventario", "Existencias por sucursal, descuento diario y umbral del 25 % (V05, V07)."),
        ("cls_inventario_ConsumoDiarioJob", None, "ConsumoDiarioJob «Scheduler»", "La tarea de las 23:59: descuenta el consumo del día."),
        ("cls_inventario_PedidoRegistradoListener", None, "PedidoRegistradoListener", "Acumula el consumo de cada pedido con la receta local."),
        ("cls_inventario_InventarioService", None, "InventarioService", "Descuenta, evalúa el umbral y publica inventario.stock-bajo."),
        ("cls_inventario_Inventario", None, "Inventario ◇ InsumoInventario", "Agregación: el inventario agrupa insumos que existen por sí solos."),
        ("cls_inventario_UmbralStock", None, "UmbralStock", "El 25 % es una regla propia de inventario, no un parámetro global."),
        ("cls_inventario_StockBajoEvent", None, "StockBajoEvent", "Lo publica la noche en que un insumo cruza el umbral."),
        ("n_recetas", None, "Recetas locales", "pedido.registrado trae plato y cantidad; inventario los multiplica por su copia de la receta, que recibió por menu.actualizado."),
        ("n_alerta", None, "La alerta", "Sigue activa hasta que llega solicitud.registrada con ese producto (HU-16)."),
        ("ley_t", None, "Cómo leer el diagrama", "La leyenda de las páginas de clases: realización (implementa), herencia, composición ◆, agregación ◇, asociación y dependencia; verde oscuro, dominio y aplicación; verde claro, adaptadores; ámbar, eventos. Debajo, «Dentro de cada clase»: gris arriba el estereotipo y la capa, magenta el patrón y ‹Vxx› la ventana."),
    ],
    guion=[
        "Esta página muestra cómo un plato termina descontando inventario sin que los servicios se llamen entre sí. "
        "En ms-menu, el jefe registra el plato con su receta; el servicio publica menu.actualizado.",
        "ms-pedidos escucha ese evento y guarda su copia del catálogo; así el mesero arma el pedido sin preguntarle a "
        "ms-menu. El pedido compone sus líneas, se guarda y se publica pedido.registrado.",
        "ms-inventario escucha ese evento y acumula el consumo con su copia de la receta. A las 23:59, la tarea diaria "
        "descuenta lo consumido y, si un insumo queda por debajo del 25 %, publica inventario.stock-bajo, que se "
        "convierte en la alarma en vivo y el correo al jefe. Las relaciones UML que se ven son la composición de "
        "Pedido con sus líneas y la agregación de Inventario con sus insumos.",
    ],
)

DIAGRAMAS["4c"] = dict(
    titulo="Nivel 4 · Código · solicitudes y cotizaciones",
    origen="Página «4c · Código · solicitudes y cotizaciones» del .drawio",
    reporte="§12, figura 10",
    que_es=_LEER4 + " Esta página tiene ms-solicitudes y ms-cotizaciones, con Facade y Chain of Responsibility.",
    elementos=[
        ("pkg_solicitudes", None, "ms-solicitudes", "Solicitudes de alimentos (V08 a V12)."),
        ("cls_solicitudes_SolicitudRestController", None, "SolicitudRestController", "Adaptador de entrada de /api/solicitudes."),
        ("cls_solicitudes_CotizacionEventsListener", None, "CotizacionEventsListener", "Escucha cotizacion.enviada y orden.generada para mover el estado de la solicitud."),
        ("cls_solicitudes_GestionarSolicitudUseCase", None, "GestionarSolicitudUseCase", "Puerto de entrada: registrar, consultar con filtros y registrar cotizaciones."),
        ("cls_solicitudes_SolicitudService", None, "SolicitudService", "Aplica las reglas y publica solicitud.registrada y solicitud.en-tramite."),
        ("cls_solicitudes_SolicitudAlimentos", None, "SolicitudAlimentos ◆ ItemSolicitud", "La solicitud compone sus ítems (producto y cantidad)."),
        ("cls_solicitudes_EstadoSolicitud", None, "EstadoSolicitud", "PENDIENTE → EN TRÁMITE → COMPLETADA."),
        ("cls_solicitudes_SolicitudRegistradaEvent", None, "Eventos de la solicitud", "SolicitudRegistradaEvent y SolicitudEnTramiteEvent extienden EventoDominio."),
        ("n_listas", None, "Idempotencia por cotización", "La solicitud pasa a EN TRÁMITE cuando todos sus productos quedan cotizados; un reenvío no suma dos veces."),
        ("pkg_cotizaciones", None, "ms-cotizaciones", "Cotizaciones, PDF, validación y proveedores (V13 a V20)."),
        ("cls_cotizaciones_CotizacionRestController", None, "CotizacionRestController", "Adaptador de entrada de /api/cotizaciones."),
        ("cls_cotizaciones_ProveedorRestController", None, "ProveedorRestController", "Adaptador de entrada de /api/proveedores (HU-15)."),
        ("cls_cotizaciones_OrdenGeneradaListener", None, "OrdenGeneradaListener", "Recibe orden.generada y marca la cotización CON_ORDEN."),
        ("cls_cotizaciones_CotizacionService", None, "CotizacionService", "Registra valores, valida, consulta y reenvía."),
        ("cls_cotizaciones_GenerarCotizacionFacade", None, "GenerarCotizacionFacade «Facade»", "Coordina precio, repositorio, PDF, almacén y evento en un solo clic."),
        ("cls_cotizaciones_ReglaValidacion", None, "ReglaValidacion «abstract» · Chain of Responsibility", "Cada regla decide o pasa a la siguiente."),
        ("cls_cotizaciones_ReglaOpcionada", None, "Las tres reglas", "ReglaRechazada (más de 25 % por encima), ReglaSospechosa (más de 50 % por debajo) y ReglaOpcionada (el resto); heredan de ReglaValidacion."),
        ("cls_cotizaciones_Cotizacion", None, "Cotizacion ◆ ItemCotizacion", "Agrupa una o varias solicitudes, con su proveedor y su estado."),
        ("cls_cotizaciones_EstadoCotizacion", None, "EstadoCotizacion", "PENDIENTE_PRECIO, ENVIADA, RECHAZADA, SOSPECHOSA, OPCIONADA y CON_ORDEN."),
        ("cls_cotizaciones_PrecioSugeridoRestAdapter", None, "PrecioSugeridoRestAdapter", "Cliente REST hacia ms-integracion."),
        ("cls_cotizaciones_PdfCotizacionAdapter", None, "PdfCotizacionAdapter", "Genera el PDF con OpenPDF."),
        ("cls_cotizaciones_AlmacenObjetosAdapter", None, "AlmacenObjetosAdapter", "Guarda el PDF en el almacén S3."),
        ("n_reenvio", None, "Si el Ministerio no responde", "La cotización queda PENDIENTE_PRECIO y se reenvía luego (HU-12)."),
        ("n_claim", None, "Claim check", "Kafka no transporta archivos: cotizacion.enviada lleva solo pdfUrl."),
        ("n_proveedores", None, "Proveedores", "NIT único, correo válido, inactivar sin borrar; cada acción se audita."),
        ("ley_t", None, "Cómo leer el diagrama", "La leyenda de las páginas de clases: realización (implementa), herencia, composición ◆, agregación ◇, asociación y dependencia; verde oscuro, dominio y aplicación; verde claro, adaptadores; ámbar, eventos. Debajo, «Dentro de cada clase»: gris arriba el estereotipo y la capa, magenta el patrón y ‹Vxx› la ventana."),
    ],
    guion=[
        "Aquí está el corazón del flujo de compra. A la izquierda, ms-solicitudes: el jefe registra la solicitud, que "
        "compone sus ítems y empieza PENDIENTE. El jefe no la cambia de estado: lo hace un evento. Cuando llega "
        "cotizacion.enviada, la solicitud pasa a EN TRÁMITE, y con orden.generada, a COMPLETADA.",
        "A la derecha, ms-cotizaciones. La fachada coordina la generación de la cotización, y la validación usa una "
        "cadena de responsabilidad: ReglaValidacion es abstracta y sus tres reglas se evalúan en orden. Agregar una "
        "regla no toca las otras, y el orden se arma en un solo lugar.",
        "Dos decisiones que vale la pena mencionar: el PDF no viaja por Kafka, el evento lleva solo su dirección en el "
        "almacén, que es el patrón claim check; y si el Ministerio no responde, la cotización queda pendiente de precio "
        "y se reenvía después.",
    ],
)

DIAGRAMAS["4d"] = dict(
    titulo="Nivel 4 · Código · órdenes y notificaciones",
    origen="Página «4d · Código · órdenes y notificaciones» del .drawio",
    reporte="§12, figura 11",
    que_es=_LEER4 + " Esta página tiene ms-ordenes y ms-notificaciones, con Factory Method y Decorator.",
    elementos=[
        ("pkg_ordenes", None, "ms-ordenes", "Órdenes de compra (V21 a V23)."),
        ("cls_ordenes_OrdenCompraRestController", None, "OrdenCompraRestController", "Adaptador de entrada de /api/ordenes-compra, incluido «Marcar como recibida» (PATCH …/finalizar)."),
        ("cls_ordenes_GenerarOrdenUseCase", None, "GenerarOrdenUseCase", "Puerto de entrada: generar, consultar y finalizar."),
        ("cls_ordenes_OrdenCompraService", None, "OrdenCompraService", "Genera la orden desde la cotización, su PDF y publica orden.generada."),
        ("cls_ordenes_OrdenCompra", None, "OrdenCompra ◆ ItemOrden", "La orden compone sus ítems."),
        ("cls_ordenes_EstadoOrden", None, "EstadoOrden", "EN_PROCESO y FINALIZADA."),
        ("cls_ordenes_CotizacionConsultaPort", None, "CotizacionConsultaPort", "Puerto para leer la cotización: solo entrega cotizaciones OPCIONADAS."),
        ("cls_ordenes_CotizacionRestClient", None, "CotizacionRestClient", "Lo implementa con REST hacia ms-cotizaciones (síncrono: el director espera el número)."),
        ("cls_ordenes_PdfOrdenAdapter", None, "PdfOrdenAdapter", "Genera el PDF de la orden con OpenPDF; se guarda en el almacén."),
        ("cls_ordenes_OrdenGeneradaEvent", None, "OrdenGeneradaEvent", "Lleva pdfUrl, correo del proveedor y número de cotización."),
        ("n_unaorden", None, "Una sola orden por cotización", "Se rechaza la segunda orden y la base garantiza número de cotización único: un doble clic no crea dos órdenes."),
        ("pkg_notificaciones", None, "ms-notificaciones", "Correos certificados y la alarma en vivo."),
        ("cls_notificaciones_NotificacionListeners", None, "NotificacionListeners", "Escuchan seis tópicos y eligen el canal."),
        ("cls_notificaciones_DestinatarioRestController", None, "DestinatarioRestController", "Destinatarios de alertas que carga el administrador."),
        ("cls_notificaciones_NotificacionService", None, "NotificacionService", "Arma el mensaje y lo envía por el canal que corresponda."),
        ("cls_notificaciones_CreadorNotificacion", None, "CreadorNotificacion «abstract» · Factory Method", "CreadorCorreo y CreadorAlarmaApp deciden qué notificación crear."),
        ("cls_notificaciones_Notificacion", None, "Notificacion", "El producto del Factory Method: NotificacionCorreo o AlarmaApp."),
        ("cls_notificaciones_Correo", None, "Correo · Decorator (componente)", "La interfaz que decoran el certificado y el sello de tiempo."),
        ("cls_notificaciones_CorreoDecorator", None, "CorreoDecorator", "CorreoCertificadoDecorator y EstampaCronologicaDecorator envuelven el correo sin cambiar el envío SMTP."),
        ("cls_notificaciones_CorreoSmtpAdapter", None, "CorreoSmtpAdapter", "El componente concreto que habla SMTP con TLS; adjunta el PDF del almacén."),
        ("cls_notificaciones_AlarmaPushAdapter", None, "AlarmaPushAdapter", "Empuja la alarma a V05 por WebSocket, a través de un canal Redis para que llegue a cualquier réplica."),
        ("n_topicos", None, "Los seis tópicos que consume", "stock-bajo (alarma y correo), solicitud.registrada, solicitud.en-tramite, cotizacion.enviada, orden.generada y tendencia.detectada."),
        ("n_fallos", None, "Si el correo falla", "Deja la traza, reintenta dos veces y pasa el evento a notificaciones.dlq, que se reprocesa cada 5 minutos, hasta tres veces."),
        ("ley_t", None, "Cómo leer el diagrama", "La leyenda de las páginas de clases: realización (implementa), herencia, composición ◆, agregación ◇, asociación y dependencia; verde oscuro, dominio y aplicación; verde claro, adaptadores; ámbar, eventos. Debajo, «Dentro de cada clase»: gris arriba el estereotipo y la capa, magenta el patrón y ‹Vxx› la ventana."),
    ],
    guion=[
        "En ms-ordenes, el director genera la orden sobre una cotización opcionada. Aquí la llamada a ms-cotizaciones "
        "es síncrona, por REST, porque el director espera el número de la orden. Una regla y una restricción de la base "
        "impiden dos órdenes sobre la misma cotización. Después, todo lo demás pasa por el evento orden.generada.",
        "ms-notificaciones escucha seis tópicos y decide el canal. Usa dos patrones: Factory Method, porque un creador "
        "abstracto delega en el creador de correo o en el de alarma qué notificación construir; y Decorator, porque el "
        "certificado y el sello de tiempo se agregan envolviendo el correo SMTP, sin tocar el código que lo envía.",
        "Si el correo falla, no se pierde: queda la traza, se reintenta y pasa a una cola de errores que el servicio "
        "vuelve a procesar.",
    ],
)

DIAGRAMAS["4e"] = dict(
    titulo="Nivel 4 · Código · integración, administración y analítica",
    origen="Página «4e · Código · integración, administración y analítica» del .drawio",
    reporte="§12 y §13",
    que_es=_LEER4 + " Esta página tiene ms-integracion (Proxy y Adapter), ms-administracion (Singleton) y ms-analitica (Strategy).",
    elementos=[
        ("pkg_integracion", None, "ms-integracion", "La frontera SOA."),
        ("cls_integracion_AbastecimientoSoapEndpoint", None, "AbastecimientoSoapEndpoint", "Adaptador de entrada SOAP: el servicio que exponemos a sistemas externos (CU-18)."),
        ("cls_integracion_PrecioRestController", None, "PrecioRestController", "REST interno que usa ms-cotizaciones; no sale por el gateway."),
        ("cls_integracion_ConsultarPrecioService", None, "ConsultarPrecioService", "Pide el precio al puerto PrecioReferenciaPort."),
        ("cls_integracion_PrecioReferenciaPort", None, "PrecioReferenciaPort", "Puerto que implementan el Proxy y el Adapter."),
        ("cls_integracion_PrecioReferenciaCacheProxy", None, "PrecioReferenciaCacheProxy «Proxy»", "Responde desde Redis si el precio tiene menos de un día; si no, pregunta al real."),
        ("cls_integracion_MinisterioComercioAdapter", None, "MinisterioComercioAdapter «Adapter»", "Traduce el SOAP del Ministerio al puerto del núcleo."),
        ("cls_integracion_MinisterioSoapClient", None, "MinisterioSoapClient «Adaptee»", "El cliente SOAP generado del servicio del Ministerio."),
        ("n_cache", None, "La caché es compartida", "Vive en Redis, así la comparten todas las réplicas."),
        ("pkg_administracion", None, "ms-administracion", "Parámetros, auditoría y logs."),
        ("cls_administracion_AdminRestController", None, "AdminRestController", "Adaptador de entrada de /api/admin."),
        ("cls_administracion_AuditoriaListener", None, "AuditoriaListener", "Recibe auditoria.evento de los otros diez servicios."),
        ("cls_administracion_AdministracionService", None, "AdministracionService", "Parametriza y consulta auditoría y logs."),
        ("cls_administracion_ParametrosSistema", None, "ParametrosSistema «Singleton»", "Una instancia por réplica con el tema y los logos; se refresca de la base cada minuto."),
        ("cls_administracion_Bitacora", None, "Bitacora ◇ RegistroAuditoria", "Agregación: la bitácora agrupa los registros de auditoría."),
        ("cls_administracion_ElasticsearchLogsAdapter", None, "ElasticsearchLogsAdapter", "Lee los logs de error del índice de Elasticsearch."),
        ("pkg_analitica", None, "ms-analitica", "Tablero BAM y promociones."),
        ("cls_analitica_NegocioEventsListener", None, "NegocioEventsListener", "Escucha cinco tópicos y guarda hechos en su almacén analítico."),
        ("cls_analitica_TendenciasJob", None, "TendenciasJob «Scheduler»", "Análisis semanal de tendencias para las promociones."),
        ("cls_analitica_TableroRestController", None, "TableroRestController", "GET /api/bam/kpis para el tablero BAM."),
        ("cls_analitica_TableroService", None, "TableroService", "Recorre la lista de estrategias de KPI."),
        ("cls_analitica_CalculadorKpi", None, "CalculadorKpi «Strategy»", "ProductoMayorDemanda, SucursalMayorDemanda y ProveedorMejoresPrecios: un KPI nuevo es una clase nueva."),
        ("cls_analitica_RedesSocialesAdapter", None, "RedesSocialesAdapter", "Lee tendencias de las redes sociales por HTTPS."),
        ("cls_analitica_SugerenciaPromocion", None, "SugerenciaPromocion", "Producto, motivo y vigencia; el jefe la acepta o la descarta."),
        ("n_kpis", None, "Cómo se filtran los KPI", "Por rango de fechas y sucursal (HU-18); sin rango, demanda de 30 días y proveedor de 365."),
        ("ley_t", None, "Cómo leer el diagrama", "La leyenda de las páginas de clases: realización (implementa), herencia, composición ◆, agregación ◇, asociación y dependencia; verde oscuro, dominio y aplicación; verde claro, adaptadores; ámbar, eventos. Debajo, «Dentro de cada clase»: gris arriba el estereotipo y la capa, magenta el patrón y ‹Vxx› la ventana."),
    ],
    guion=[
        "La última página de clases tiene tres servicios. En ms-integracion están Proxy y Adapter juntos: el servicio "
        "pide el precio a un puerto; delante está el Proxy, que responde desde la caché en Redis, y detrás el Adapter, "
        "que traduce el SOAP del Ministerio. Así no llamamos al Ministerio por cada producto de cada cotización. El "
        "mismo servicio expone nuestro endpoint SOAP a otros sistemas.",
        "En ms-administracion está el Singleton: ParametrosSistema guarda el tema y los logos, y la SPA los aplica al "
        "iniciar sesión. Aquí también llega la auditoría de cada operación de los otros diez servicios, por eventos.",
        "En ms-analitica está Strategy: cada KPI del tablero es una clase que implementa CalculadorKpi. Los datos no se "
        "consultan en las bases de otros servicios; llegan por eventos a un almacén propio.",
    ],
)

# ---------------------------------------------------------------- 6
DIAGRAMAS["6"] = dict(
    titulo="Diagrama dinámico · Recorrido completo de una compra",
    origen="Página «6 · C4 Dinámico · recorrido de compra» del .drawio",
    reporte="§10.1, tabla 9 y figura 8",
    que_es=(
        "Es el diagrama dinámico del C4, en forma de secuencia: cómo fluye una compra desde que el jefe pide alimentos "
        "(V08) hasta que el director hace seguimiento a la orden (V23). Arriba y abajo están los mismos contenedores de "
        "la página 3a; cada paso lleva su número a la izquierda, que es el de la tabla 9 del reporte, y x.y numera los "
        "mensajes dentro de un paso. No lleva números agregados: se explica con los suyos. Datos de ejemplo: SOL-031, "
        "COT-018 y OC-0012."
    ),
    sin_pines=True,
    tabla_titulo="Los 14 pasos (números del propio diagrama)",
    tabla=[
        ("1 · V08 → V09 · síncrono", "El jefe registra la solicitud: POST /api/solicitudes, responde 201 SOL-031 PENDIENTE."),
        ("2 · evento", "solicitud.registrada: correo de confirmación (ms-notificaciones), KPI (ms-analitica) y alerta de stock atendida (ms-inventario)."),
        ("3 · V11 → V12 · síncrono", "El auxiliar filtra las solicitudes y abre el detalle."),
        ("4 · V13 → V14 · síncrono", "El auxiliar genera la solicitud de cotización: precio del Ministerio (Proxy, luego SOAP), PDF al almacén, COT-018 ENVIADA (Facade)."),
        ("5 · evento", "cotizacion.enviada: PDF por correo al proveedor y revisión de la solicitud."),
        ("6 · evento · V10", "solicitud.en-tramite: la solicitud del jefe pasa a EN TRÁMITE y se le avisa."),
        ("7 · fuera del sistema", "Días después, el proveedor responde por correo."),
        ("8 · V18 · síncrono", "El director registra los valores del proveedor: $545.000."),
        ("9 · V19 · síncrono", "El director valida: $545.000 frente a $520.000 = +4,81 %, OPCIONADA (Chain of Responsibility)."),
        ("10 · evento", "cotizacion.validada hacia ms-analitica: KPI de proveedor con mejores precios."),
        ("11 · V20 → V22 · síncrono", "El director genera la orden: ms-ordenes consulta la cotización por REST y responde 201 OC-0012."),
        ("12 · evento", "orden.generada: solicitud COMPLETADA, cotización CON ORDEN, PDF al proveedor y KPI."),
        ("13 · V23 · síncrono", "Seguimiento de las órdenes."),
        ("14 · evento · todas", "auditoria.evento: cada uno de los otros diez servicios publica la auditoría de cada escritura hacia ms-administracion."),
    ],
    guion=[
        "Este diagrama muestra en el tiempo lo que las otras páginas muestran en el espacio: el recorrido completo de "
        "una compra. Los números de la izquierda son los mismos de la tabla 9 del reporte, y el color dice si el paso "
        "es síncrono, un evento o algo que pasa fuera del sistema.",
        "El jefe registra la solicitud SOL-031 y recibe la respuesta de inmediato; el correo de confirmación y la "
        "atención de la alerta de stock llegan después, por evento. El auxiliar genera la cotización COT-018 con un "
        "clic: el precio sale del Ministerio a través del Proxy, se genera el PDF y se envía al proveedor por evento, "
        "y la solicitud pasa a EN TRÁMITE sin que nadie la toque.",
        "El proveedor responde por correo, fuera del sistema. El director registra $545.000, valida y la cadena de "
        "reglas da +4,81 %, opcionada. Genera la orden OC-0012, y con orden.generada la solicitud queda completada, la "
        "cotización con orden y el proveedor recibe el PDF. En todo momento, cada escritura deja su auditoría.",
        "La idea que muestra el diagrama: el usuario solo espera lo que necesita ver de inmediato; todo lo demás lo "
        "hace el sistema por eventos.",
    ],
)

# ---------------------------------------------------------------- 5
DIAGRAMAS["5"] = dict(
    titulo="Diagrama de despliegue",
    origen="Página «5 · Despliegue» del .drawio",
    reporte="§14.4, figura 15",
    que_es=(
        "Complemento del C4: cómo se ejecuta la arquitectura para cumplir 200.000 usuarios concurrentes, 1.000 TPS y "
        "una disponibilidad del 99,7 % (unas 26,3 horas de caída al año como máximo), con recuperación ante desastres "
        "en un centro de datos alterno."
    ),
    elementos=[
        ("v2", None, "Dispositivos de usuario", "Navegador web y móvil con la SPA responsive."),
        ("v3", None, "CDN + WAF", "Entrega la SPA estática cerca del usuario y filtra ataques."),
        ("v5", None, "DNS global con conmutación", "Si cae el sitio principal, envía el tráfico al alterno."),
        ("v7", None, "Centro de datos principal", "Región A, en la nube, con infraestructura como código."),
        ("v8", None, "Balanceador de carga L7", "Par activo-activo, revisa la salud cada 5 s."),
        ("v10", None, "Clúster Kubernetes", "Tres zonas de disponibilidad y autoescalado horizontal (HPA) al 65 % de CPU."),
        ("v11", None, "API Gateway ×3", "Valida el JWT y limita la tasa; uno por zona."),
        ("v13", None, "Service Registry ×2", "Eureka, con latido de 30 s."),
        ("v21", None, "SPA ×2", "Nginx con la imagen corporativa parametrizable."),
        ("v15", None, "Los once microservicios", "Réplicas mínimas y máximas: ms-pedidos 4 → 20, ms-identidad 3 → 8, inventario, cotizaciones y solicitudes 2 → 6, los demás 2 → 4."),
        ("v17", None, "Kafka ×3 brokers", "Factor de réplica 3, 10 tópicos, retención 7 días y cola de errores."),
        ("v19", None, "Observabilidad", "Prometheus y Grafana (métricas), OpenTelemetry (trazas) y ELK (logs de error)."),
        ("v37", None, "Salida controlada", "Única salida hacia el Ministerio (SOAP), el correo certificado (SMTP/TLS) y las redes sociales."),
        ("v42", None, "Reparto por zona", "Un gateway y un broker por zona; cada servicio en al menos dos zonas. Si cae una zona, siguen dos de tres."),
        ("v23", None, "Capa de datos", "Servicios administrados con cifrado en reposo."),
        ("v24", None, "PostgreSQL ×11", "Una base por servicio, primario y réplica síncrona en otra zona."),
        ("v25", None, "Redis primario + réplica", "Caché de precios, canal de alarmas y límite de tasa."),
        ("v26", None, "Almacén de objetos", "PDF e imágenes, versionado y multizona; previsto para crecer 200 %."),
        ("v27", None, "Respaldo diario", "Copia completa cada noche más WAL continuo; almacén con copia diaria; retención de 30 días."),
        ("v30", None, "Centro de datos alterno", "Región B en espera tibia."),
        ("v31", None, "Clúster en espera", "Las mismas imágenes con réplicas mínimas encendidas."),
        ("v32", None, "Kafka réplica", "MirrorMaker 2 copia los tópicos."),
        ("v33", None, "PostgreSQL réplicas asíncronas", "Se pierden como máximo 5 minutos de datos (RPO)."),
        ("v39", None, "Almacén réplica", "Replicación entre regiones."),
        ("p8_leg_t", None, "Cómo leer el diagrama", "Las líneas magenta son la replicación y la conmutación al centro alterno."),
    ],
    guion=[
        "El despliegue responde a los requisitos no funcionales: 200.000 usuarios, 1.000 transacciones por segundo y "
        "99,7 % de disponibilidad, que son unas 26 horas de caída al año. La idea es que ninguna pieza sea única.",
        "El usuario llega por un DNS global con conmutación y una CDN con WAF. En el centro principal, un balanceador "
        "activo-activo reparte entre tres gateways, uno por zona, dentro de un clúster Kubernetes en tres zonas. Cada "
        "servicio tiene réplicas mínimas y máximas: pedidos, que es el de más carga, va de 4 a 20. Si se cae una zona, "
        "siguen dos de tres gateways y dos de tres brokers de Kafka.",
        "Los datos están replicados: cada PostgreSQL con réplica síncrona en otra zona, Redis con réplica, y el almacén "
        "de objetos multizona, con respaldo diario y 30 días de retención. A la derecha, el centro de datos alterno, en "
        "otra región, recibe las réplicas y está listo para tomar el tráfico: perdemos como máximo cinco minutos de "
        "datos y recuperamos el servicio en una hora.",
    ],
    preguntas=[
        ("¿Cómo se llega al 99,7 %?", "Con redundancia en cada capa (DNS, CDN, balanceador, tres gateways, tres zonas, réplicas de datos) y un centro alterno; ningún componente es punto único de falla."),
    ],
)
