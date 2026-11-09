<!-- §3. Correcciones: R-08, R-09, R-11, R-12, R-13 y R-63. R-10 ya es cierto. R-14 es opcional y no se aplica. -->
# 3. Requerimientos no funcionales y decisiones

La tabla 3 recorre los requerimientos no funcionales del enunciado (algunos agrupados) y muestra qué decisión produce cada uno, qué arquitectura la asume y dónde se ve en el diagrama.

!tabla 3 | Requerimientos no funcionales y decisiones de diseño | 3.2,5.6,2.4,2.6
| Requerimiento | Decisión | Arquitectura | Dónde se ve |
|---|---|---|---|
| 200.000 usuarios concurrentes y 1.000 TPS de compras | Servicios sin estado detrás de un balanceador; todos escalan en horizontal entre un mínimo y un máximo de réplicas, y los que reciben más carga (pedidos, identidad, cotizaciones, solicitudes e inventario) tienen los máximos más altos | Microservicios | Despliegue: ms-pedidos de 4 a 20 réplicas |
| Computación distribuida | Once servicios independientes que se registran en un Service Registry y se descubren en tiempo de ejecución | Microservicios | Mapa: Service Registry [Eureka] |
| Alta disponibilidad del 99,7 % (unas 26 horas de caída al año) | Sin punto único de falla: balanceador activo-activo, tres réplicas del gateway, Kafka con factor de réplica 3, clúster en tres zonas. Si un servicio cae, los demás siguen y sus eventos esperan en el broker | Microservicios y eventos | Despliegue |
| Centro de datos alterno | Región B en espera tibia con réplica asíncrona de bases y tópicos; conmutación por DNS | Apoyo (despliegue) | Despliegue, borde magenta |
| Autenticación, autorización, roles y permisos | ms-identidad emite un JWT (OAuth 2.0); el gateway lo valida en cada petición; el menú lateral se arma por rol | Microservicios y OAuth 2.0 | V01; página 4a (Composite) |
| HTTPS en toda comunicación | TLS 1.3 hacia el usuario, mTLS entre servicios y JDBC con TLS | Apoyo | Protocolo en cada flecha |
| Contraseña de 8 o más caracteres con mayúscula, minúscula y número, almacenada cifrada | Regla en el dominio de identidad (PoliticaContrasena); el alta de usuarios la hace el administrador (POST /api/usuarios), que valida la regla y guarda el hash BCrypt con CifradorPort.cifrar() | Hexagonal | Página 4a; adaptador BCrypt de ms-identidad |
| Correo certificado con estampado cronológico | Decoradores sobre el puerto Correo: el certificado y el sello de tiempo se apilan sobre el adaptador SMTP; los correos se disparan por eventos | Hexagonal y eventos | Página 4d (Decorator) |
| Imagen corporativa parametrizable | Tema y logos en ParametrosSistema (ms-administracion); la SPA los carga al iniciar con GET /api/admin/parametros/tema, sin redesplegar | Microservicios | ms-administracion (Singleton) |
| Diseño responsive para móviles | Una sola SPA React y TypeScript responsive para web y móvil | Cliente-servidor | Contenedor SPA del mapa |
| Crecimiento del 200 % en documentos, archivos y datos | PDF y archivos de carga en un almacén de objetos; cada base crece y se dimensiona por separado | Microservicios | Despliegue, capa de datos |
| Backup diario | Bases: copia completa nocturna más registros WAL continuos. Almacén de objetos: copia diaria y réplica en la región alterna. Retención de 30 días | Apoyo (despliegue) | Despliegue, respaldo diario |
| Cambio fácil de motor de base de datos | Cada repositorio es un puerto; el motor vive en un adaptador JPA que se reemplaza sin tocar el dominio | Hexagonal | Adaptadores \*JpaAdapter |
| Auditoría de cada CRUD con acción, usuario, fecha y hora | Cada uno de los otros diez servicios publica auditoria.evento después de cada escritura; ms-administracion lo consume y lo guarda en su bitácora, y sus propias escrituras las registra directo. En el sistema real, el usuario sale del JWT; la demostración del esqueleto registra «sistema» (o el correo de la entidad), como se explica en la sección 13 | Eventos | Tópico auditoria.evento |
| Registro de cada error en un log | Logs centralizados (ELK) con trazas OpenTelemetry; consulta desde ms-administracion | Apoyo | Despliegue, observabilidad |
| Tecnología libre de licenciamiento y versiones estables | Java 21, Spring Boot 3, PostgreSQL, Apache Kafka, Redis, React, Kubernetes: todo de código abierto | Todas | Etiquetas [tecnología] del mapa |
| Exponer servicios a sistemas externos por SOAP | ms-integracion concentra la frontera SOA: publica un endpoint SOAP y consume el servicio web del Ministerio de Comercio; los sistemas externos se autentican con OAuth 2.0 client credentials y el gateway valida su JWT, que solo da acceso a /api/integracion/\*\* | SOA (apoyo) y hexagonal | Endpoint SOAP de ms-integracion |
