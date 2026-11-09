<!-- §7. Sin cambios de contenido: R-25 ya es cierto. -->
# 7. Arquitectura hexagonal

La tercera arquitectura define cómo se construye cada microservicio por dentro. Vive en los niveles 3 y 4 del modelo C4 (componentes y código) y responde a los requerimientos de cambio de motor de base de datos, integración SOAP, correo certificado y mantenimiento (Cockburn, 2005).

## 7.1 Esquema

Cada servicio pone el dominio y los casos de uso en el centro (el hexágono verde del mapa), los puertos como interfaces en el borde y los adaptadores afuera. Arriba están los adaptadores de entrada, que conducen el núcleo (controlador REST, tarea programada, listener de Kafka); abajo, los de salida, que implementan puertos (JPA, publicador de Kafka, SMTP, SOAP, PDF). En el código, cada capa es un paquete, como muestra la tabla 6.

!tabla 6 | Paquetes de cada servicio, con ejemplos de ms-cotizaciones | 2.8,5.6,5.2
| Paquete | Contiene | Ejemplo en ms-cotizaciones |
|---|---|---|
| dominio | Entidades, objetos de valor y reglas de negocio, sin frameworks | Cotizacion, ItemCotizacion, ReglaValidacion y sus tres reglas |
| puertos.entrada | Interfaces de los casos de uso que el exterior invoca | GestionarCotizacionUseCase |
| aplicacion | Implementación de los casos de uso; coordina dominio y puertos | CotizacionService, GenerarCotizacionFacade |
| puertos.salida | Interfaces que el dominio necesita del exterior | CotizacionRepositoryPort, PrecioSugeridoPort, GeneradorPdfPort |
| adaptadores.entrada | Conducen el núcleo: REST, listeners de Kafka, tareas programadas | CotizacionRestController |
| adaptadores.salida | Implementan los puertos con una tecnología concreta | CotizacionJpaAdapter, PdfCotizacionAdapter, PrecioSugeridoRestAdapter |
| eventos | Hechos de negocio que el servicio publica | CotizacionEnviadaEvent, CotizacionValidadaEvent |

## 7.2 Lineamientos, políticas y reglas

El lineamiento es que las dependencias apunten siempre hacia el dominio (Martin, 2017). Las políticas: el dominio no importa Spring, JPA, Kafka ni SOAP; toda salida al mundo exterior pasa por un puerto que el propio núcleo define; y los adaptadores se conectan al arrancar, por inyección de dependencias. Las reglas: un caso de uso por puerto de entrada; los controladores no contienen reglas de negocio; y el dominio se prueba con adaptadores falsos en memoria, sin base de datos ni broker.

## 7.3 Aplicabilidad en Konrad Gourmet

Tres requerimientos piden aislar tecnología. El cambio fácil de motor de base de datos se resuelve con un puerto de repositorio y un adaptador JPA por servicio. La integración con el Ministerio pasa por el puerto PrecioReferenciaPort, cuyo adaptador habla SOAP. El correo certificado pasa por el puerto Correo, cuyo adaptador habla SMTP. Además, el profesor pidió decidir la tecnología lo más tarde posible, y la hexagonal deja esa decisión encerrada en los adaptadores.

## 7.4 Ventajas

El dominio se prueba sin infraestructura: la demostración del esqueleto Java corre sin base de datos ni Kafka. Cambiar PostgreSQL por otro motor afecta un solo adaptador por servicio. Las reglas del negocio (el 25 % del inventario, el +25 % y el −50 % de las cotizaciones) quedan en un único lugar. Y el mismo caso de uso puede invocarlo un controlador REST, un listener o la tarea diaria.

## 7.5 Desventajas y cómo se mitigan

Hay más clases e interfaces que en un CRUD directo; para no repetir trabajo, la librería común compartido trae EventPublisherPort, EventoDominio y AuditoriaEvent. Se necesita traducir entre entidades de dominio y de persistencia, trabajo que solo existe en los adaptadores JPA. Y existe el riesgo de sobrediseñar servicios pequeños como ms-menu; se acepta a cambio de que los once servicios tengan la misma estructura y cualquier desarrollador sepa dónde buscar.

## 7.6 Ejemplo en las ventanas

En la ventana 13, CotizacionRestController (adaptador de entrada) invoca el caso de uso, que delega en GenerarCotizacionFacade. La fachada pide el precio estándar de cada producto por PrecioSugeridoPort; su adaptador llega por REST a ms-integracion, donde PrecioReferenciaCacheProxy responde desde la caché o delega en MinisterioComercioAdapter, que habla SOAP con el Ministerio. En la ventana 19, CotizacionService aplica la cadena de reglas del dominio a esos valores y guarda por CotizacionRepositoryPort. Para COT-018 el precio estándar es $520.000 y el del proveedor $545.000: la diferencia es +4,81 %, dentro del rango, y la cotización queda OPCIONADA, como en el mockup.
