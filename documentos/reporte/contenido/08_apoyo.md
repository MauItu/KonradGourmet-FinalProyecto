<!-- §8. Sin cambios de contenido. Se agrega una frase sobre el canal de alarmas en Redis (R-56, PD-01). -->
# 8. Estilos de apoyo

Los tres estilos principales se apoyan en cinco mecanismos que completan el diseño. Ninguno define la forma del sistema, pero sin ellos no se cumplirían varios requerimientos.

## 8.1 Cliente-servidor con REST

La SPA, construida con React y TypeScript y servida por Nginx detrás de una CDN, es el único cliente. Es responsive, así que la misma base de código sirve para escritorio y móvil, y consume REST con JSON sobre HTTPS. Cada ventana corresponde a un endpoint concreto (sección 15). Entre servicios solo hay dos llamadas síncronas: ms-ordenes consulta a ms-cotizaciones al generar la orden y ms-cotizaciones consulta a ms-integracion el precio estándar.

## 8.2 SOA con SOAP en la frontera

ms-integracion es el único servicio que habla SOAP. Hacia afuera publica AbastecimientoSoapEndpoint, con su WSDL, para los sistemas externos que el requerimiento de integración menciona. Hacia el Ministerio de Comercio usa MinisterioComercioAdapter, protegido por PrecioReferenciaCacheProxy con una caché en Redis. El mismo Redis lleva, en un canal separado de la caché, la difusión de las alarmas entre las réplicas de ms-notificaciones (sección 16). El resto del sistema le habla a ms-integracion por REST, de modo que si mañana el Ministerio publica una API REST, solo cambia un adaptador.

## 8.3 Seguridad con OAuth 2.0 y JWT

En la ventana 01 la SPA envía POST /api/auth/token. ms-identidad busca al usuario, compara la contraseña con su hash BCrypt y emite un JWT con el rol y el menú lateral, que arma la subclase de Usuario correspondiente (Factory Method) como un árbol de opciones (Composite). El gateway valida la firma del token en cada petición, cada servicio comprueba el rol en sus endpoints y las llamadas internas usan mTLS.

## 8.4 Balanceador, API Gateway y Service Registry

Un par de balanceadores de capa 7 en activo-activo reparte el tráfico entre tres réplicas del gateway (Spring Cloud Gateway), que actúa como Facade del sistema: la SPA ve una sola dirección aunque detrás haya once servicios. El gateway pregunta al Service Registry (Eureka, con latido cada 30 segundos) qué instancias están vivas, lo que permite que el autoescalado agregue o quite réplicas sin reconfigurar nada.
