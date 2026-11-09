<!-- §1. Correcciones: R-04 (once páginas) y R-07. R-06 queda como estaba: ya es cierto. -->
# 1. Resumen de la decisión

Konrad Gourmet necesita que cuatro roles operativos (mesero, jefe de cocina, auxiliar de compras y director de compras) trabajen sobre los mismos datos desde todas las sucursales, con 200.000 usuarios concurrentes, 1.000 transacciones por segundo y una disponibilidad del 99,7 %. Ningún estilo arquitectónico cumple solo esas restricciones, así que la propuesta combina tres, cada uno en el nivel donde resuelve un problema distinto.

!tabla 1 | Las tres arquitecturas de la propuesta | 3,2.9,3.2,3.3,2.5
| Arquitectura | Dónde vive | Qué decide | Requerimientos que la exigen | Dónde se ve |
|---|---|---|---|---|
| Microservicios | Nivel 2 del C4 (contenedores) | Once servicios desplegables, cada uno con su base de datos, detrás de un API Gateway | 200.000 usuarios, 1.000 TPS, 99,7 %, computación distribuida, crecimiento del 200 % | Mapa (cajas azules) y página de despliegue |
| Orientada a eventos | Entre contenedores | Comunicación asíncrona entre roles y procesos automáticos por medio de diez tópicos | Correo certificado, alarma del 25 %, auditoría de cada CRUD, tendencias, desacople ante caídas | Mapa (banda ámbar y flechas punteadas) |
| Hexagonal | Niveles 3 y 4 del C4, dentro de cada servicio | Dominio aislado de la base de datos, del SOAP, del SMTP y del PDF | Cambio fácil de motor de base de datos, integración SOAP, tecnología libre, mantenimiento | Mapa (hexágonos) y páginas 4a a 4e |

Las tres se leen juntas en la página 1 del archivo .drawio (figura 1): las cajas azules son los microservicios, la banda ámbar es el broker de eventos y los hexágonos verdes son el interior de cada servicio. A su alrededor aparecen cinco estilos y mecanismos de apoyo que completan a los tres principales: cliente-servidor con REST entre la SPA y el API Gateway, SOA con SOAP en la frontera con los sistemas externos, OAuth 2.0 con JWT para la seguridad, un Service Registry para descubrir instancias y un balanceador de carga activo-activo.

El diagrama no es un dibujo aparte del código. Las clases del nivel 4 y el esqueleto Java salen del mismo modelo, cada uno de los 23 mockups está asignado a un controlador y un método concretos, y una demostración ejecutable reproduce los valores de la ventana 19 (cotización COT-018, diferencia de +4,81 % frente al precio del Ministerio, estado OPCIONADA).

El profesor insistió en que «un diseño no es bueno o malo en sí mismo, sino con respecto a las restricciones que se le imponen» (López, 2026b, sesión 10). Por eso cada decisión de este documento se justifica contra un requerimiento no funcional del enunciado.

La sección 3 recorre los requerimientos no funcionales uno por uno; la 4 descarta los estilos vistos en clase que no encajan; las secciones 5 a 7 desarrollan cada arquitectura con la plantilla de clase (esquema, lineamientos, aplicabilidad, ventajas y desventajas, ejemplo); la 9 explica el mapa y su notación; la 10 asigna una arquitectura dominante a cada flujo de ventanas; y las secciones 12 y 13 muestran dónde están los patrones de diseño en el diagrama de clases y en el código, y cómo los ejecuta la demostración.

Este documento acompaña dos entregables: el archivo Arquitectura_Konrad_Gourmet.drawio, con once páginas (mapa integrado, los cuatro niveles del C4, es decir, contexto, contenedores, componentes de ms-cotizaciones y cinco páginas de nivel de código, despliegue y el diagrama dinámico del recorrido de compra), y el esqueleto Java del paquete co.edu.konradlorenz, que declara las mismas clases. Las figuras son capturas de ese archivo; para ampliarlas, se puede abrir en draw.io.
