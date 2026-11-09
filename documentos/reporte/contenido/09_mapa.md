<!-- §9. Correcciones: R-26, R-27 y R-28 (figuras 1 a 6 recapturadas de exportes/post_sesion6; pie de la figura 5 ajustado). -->
# 9. El mapa integrado y su notación

La página 1 del archivo .drawio integra las tres arquitecturas en una sola vista que se lee de arriba abajo. Arriba están las personas del C4, una por rol y con el color del rol. Debajo, el contenedor SPA agrupa las 23 ventanas por rol y cada miniatura indica qué servicio y qué endpoint la atienden. Las peticiones bajan por HTTPS al balanceador y al API Gateway, que se apoya en el Service Registry.

La fila central son los once microservicios: cada caja azul contiene arriba sus adaptadores de entrada y de salida síncrona, al centro el hexágono con el dominio y abajo los listeners, los adaptadores de persistencia con su base y el publicador de eventos. Cada caja verde agrupa los adaptadores de una misma tecnología y destino (por ejemplo, «CotizacionJpaAdapter / ProveedorJpaAdapter»); el detalle por clase está en las páginas 4a a 4e. Junto a las bases aparecen el almacén de objetos [S3 compatible], donde ms-cotizaciones, ms-ordenes y ms-notificaciones guardan y leen los PDF; los logs de error [Elasticsearch, ELK], que consulta ms-administracion; y la base [PostgreSQL] de ms-integracion, que guarda su tabla outbox junto a la caché de precios en Redis. La banda ámbar del fondo es Kafka con sus diez tópicos. A la derecha están los sistemas externos y el proveedor.

La notación combina UML (Object Management Group, 2017) para las relaciones entre clases y C4 (Brown, 2018) para personas, contenedores y sistemas. Las mismas formas y colores se usan en todas las páginas del archivo; la tabla 7 los resume.

!tabla 7 | Notación del diagrama | 3.6,4.4,5.6
| Símbolo | Significado | Ejemplo en el diagrama |
|---|---|---|
| Línea continua con triángulo hueco | Herencia o generalización: la subclase es un tipo de la superclase | Mesero, JefeCocina, AuxiliarCompras y DirectorCompras heredan de Usuario; ReglaRechazada hereda de ReglaValidacion |
| Línea punteada con triángulo hueco | Realización: una clase implementa una interfaz o un puerto | UsuarioJpaAdapter implementa UsuarioRepositoryPort; flechas «implementa puerto» del mapa |
| Línea punteada con punta abierta | Dependencia: un elemento usa a otro sin guardarlo | ConsultarPrecioService «usa» PrecioReferenciaPort; flechas «usa puerto» de los adaptadores de entrada |
| Rombo lleno | Composición: la parte no existe sin el todo | Pedido con DetallePedido 1..\*; Cotizacion con ItemCotizacion 1..\* |
| Rombo vacío | Agregación: el todo agrupa partes que existen por sí solas | Inventario con InsumoInventario; Bitacora con RegistroAuditoria |
| Línea continua con punta abierta | Asociación navegable, con rol y multiplicidad | TableroService hacia CalculadorKpi (estrategias 1..\*) |
| Línea continua con punta llena | Llamada síncrona, con su protocolo en la etiqueta | SPA hacia el balanceador [HTTPS]; ms-integracion hacia el Ministerio [SOAP/HTTPS] |
| Línea punteada ámbar | Evento asíncrono que se publica o se consume | ms-cotizaciones, cotizacion.enviada, ms-notificaciones |
| Línea discontinua gris | Relación C4 entre una persona o sistema externo y el sistema | Mesero hacia la SPA: «Registra pedidos por mesa» |
| Caja azul | Contenedor C4: microservicio desplegable | ms-pedidos [Spring Boot 3, Java 21] |
| Hexágono verde | Núcleo hexagonal: dominio y casos de uso | Uno por microservicio |
| Cilindro | Base de datos, caché o almacén | BD pedidos [PostgreSQL]; caché de precios [Redis]; almacén de objetos [S3 compatible] |
| Caja gris | Sistema externo | Ministerio de Comercio, servidor de correo certificado, redes sociales |

Las figuras 1 a 6 muestran el mapa completo y cada una de sus zonas de cerca.

!figura 1 | figuras/1.png | Mapa integrado de las tres arquitecturas | Página 1 del archivo Arquitectura_Konrad_Gourmet.drawio. De arriba abajo: personas por rol, SPA con las 23 ventanas, balanceador, API Gateway y Service Registry, los once microservicios con su núcleo hexagonal, el broker de eventos y, a la derecha, los sistemas externos. Para leer los detalles conviene ampliar la imagen; las figuras 2 a 6 muestran cada zona de cerca. | horizontal

!figura 2 | figuras/fig02_entrada.png | Clientes, ventanas y punto de entrada | Fragmento de la página 1. Las ventanas se agrupan por rol con el color de su actor y cada miniatura indica el servicio y el endpoint que la atiende. El director comercial y el administrador no tienen mockup, pero ya tienen servicio y ruta. Todas las peticiones bajan por HTTPS al balanceador y al API Gateway «Facade», que descubre instancias en el Service Registry. | horizontal

!figura 3 | figuras/fig03_identidad_inventario.png | ms-identidad, ms-pedidos, ms-menu y ms-inventario | Fragmento de la página 1. Encima de cada hexágono, los adaptadores de entrada (REST, Scheduler) y de salida síncrona (BCrypt); dentro, el dominio con su relación UML principal (herencia de Usuario, composición Pedido–DetallePedido, agregación Inventario–InsumoInventario); debajo, listeners de eventos, adaptadores de persistencia con su base propia y publicador Kafka. | horizontal

!figura 4 | figuras/fig04_solicitudes_administracion.png | ms-solicitudes, ms-cotizaciones, ms-ordenes y ms-administracion | Fragmento de la página 1. En ms-cotizaciones se ve la cadena de reglas de validación (Chain of Responsibility) y los adaptadores de PDF y de precio sugerido; en ms-administracion, la agregación de la bitácora de auditoría y el Singleton de parámetros. | horizontal

!figura 5 | figuras/fig05_analitica_integracion.png | ms-analitica, ms-notificaciones, ms-integracion y sistemas externos | Fragmento de la página 1. ms-analitica aplica Strategy para cada KPI; ms-notificaciones combina Factory Method y Decorator para el correo certificado y lee los PDF del almacén de objetos; ms-integracion es la frontera SOA, con el endpoint SOAP hacia afuera y el Proxy con caché delante del adaptador del Ministerio. | horizontal

!figura 6 | figuras/fig06_broker_leyenda.png | Broker de eventos y leyenda del mapa | Fragmento de la página 1. Arriba, en dos tramos, las flechas punteadas ámbar bajan desde los publicadores hasta cada tópico de Kafka y suben desde el tópico hasta los listeners que lo consumen; también se ven el almacén de objetos y los logs de error. Abajo, la leyenda incluida en el propio archivo: arquitecturas, relaciones UML y C4, y elementos. | horizontal
