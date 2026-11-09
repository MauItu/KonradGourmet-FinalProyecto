<!-- §5. Correcciones: R-15, R-16, R-17 y R-20. R-18 ya es cierto. R-19 es opcional y no se aplica. -->
# 5. Microservicios

La primera arquitectura define cómo se parte y se despliega el sistema. Vive en el nivel 2 del modelo C4 (contenedores) y responde a los requerimientos de desempeño, disponibilidad y almacenamiento (Newman, 2021).

## 5.1 Esquema

La SPA envía todas las peticiones por HTTPS al balanceador de carga, que las reparte entre tres réplicas del API Gateway. El gateway valida el JWT emitido por ms-identidad, consulta el Service Registry y enruta por prefijo (/api/pedidos/\*\*, /api/cotizaciones/\*\*, entre otros) a una instancia sana del servicio. Cada uno de los once microservicios es un contenedor Spring Boot con su propia base PostgreSQL, que ningún otro servicio lee directamente. En el mapa integrado es la fila de once cajas azules debajo del gateway.

## 5.2 Lineamientos, políticas y reglas

El lineamiento es que cada servicio sea dueño de un subdominio del negocio y de sus datos (Richardson, 2018). Las políticas son una base de datos por servicio, servicios sin estado (la sesión viaja en el JWT) para poder replicarlos, comunicación síncrona solo por REST a través del gateway o de un cliente declarado, y despliegue independiente con versión de API. Las reglas concretas: ningún servicio consulta las tablas de otro; ninguna petición del usuario encadena más de dos llamadas síncronas entre servicios; cada servicio expone un chequeo de salud para el balanceador y se registra en Eureka al arrancar; y cada servicio publica un evento de auditoría en cada CRUD, salvo ms-administracion, que es quien los guarda y registra los suyos directo en la bitácora.

## 5.3 Aplicabilidad en Konrad Gourmet

Los cortes siguen el ciclo de vida de los datos y los roles que los tocan: el pedido lo crea el mesero, el plato y el inventario los maneja el jefe de cocina, la solicitud pasa del jefe al auxiliar, la cotización del auxiliar al director y la orden la emite el director. A eso se suman cinco servicios transversales (identidad, notificaciones, administración, analítica e integración). La carga no es pareja: los meseros de todas las sucursales registran pedidos todo el día y las 1.000 TPS de compras se concentran en pedidos y cotizaciones, mientras que el registro de platos es esporádico. Con microservicios, ms-pedidos escala de 4 a 20 réplicas, mientras que ms-menu se mueve entre 2 y 4.

El gateway enruta 13 prefijos a los once servicios: ms-identidad y ms-cotizaciones tienen dos cada uno. La tabla 5 resume la responsabilidad, las ventanas y las rutas de cada servicio.

!tabla 5 | Los once microservicios | 3.5,4.2,2.9,3.6
| Servicio | Responsabilidad | Ventanas | Ruta en el gateway |
|---|---|---|---|
| ms-identidad | OAuth 2.0 / OIDC, usuarios, roles, permisos y menú por rol; alta de usuarios por el administrador | V01 | /api/auth/\*\* y /api/usuarios/\*\* |
| ms-pedidos | Pedidos por mesa; escala horizontal en horas pico | V02 a V04 | /api/pedidos/\*\* |
| ms-menu | Platos, recetas, categorías y productos | V06 | /api/platos/\*\* |
| ms-inventario | Existencias por sucursal, descuento diario y umbral del 25 % | V05 y V07 | /api/inventario/\*\* |
| ms-solicitudes | Solicitudes de alimentos y sus estados | V08 a V12 | /api/solicitudes/\*\* |
| ms-cotizaciones | Solicitud de cotización, PDF, registro de valores y validación; proveedores (HU-15) | V13 a V20 | /api/cotizaciones/\*\* y /api/proveedores/\*\* |
| ms-ordenes | Órdenes de compra desde cotizaciones OPCIONADAS, y su PDF | V21 a V23 | /api/ordenes-compra/\*\* |
| ms-administracion | Parametrización, imagen corporativa, auditoría y logs | Parametrización y auditoría (sin mockup) | /api/admin/\*\* |
| ms-analitica | Tablero BAM (KPI), detección de tendencias y promociones sugeridas (HU-19) | Tablero BAM (sin mockup) | /api/bam/\*\* |
| ms-notificaciones | Correos certificados con sello de tiempo y alarmas en la app | Correos de V09, V14 y V22; alarma de V05 | /api/notificaciones/\*\* |
| ms-integracion | Frontera SOA: consume el servicio web del Ministerio y expone servicios SOAP | Precio estándar en V13 | /api/integracion/\*\* |

## 5.4 Ventajas

Escalado selectivo de los servicios con más carga; aislamiento de fallas (si ms-analitica cae, el mesero sigue registrando pedidos); despliegue independiente, de modo que un cambio en la validación de cotizaciones no exige redesplegar pedidos; cada base de datos puede cambiar de motor o de tamaño por separado, lo que también responde al crecimiento del 200 %; y equipos que pueden trabajar en paralelo sobre servicios distintos.

## 5.5 Desventajas y cómo se mitigan

La operación se vuelve más compleja (once despliegues), lo que se mitiga con Kubernetes, infraestructura como código y observabilidad centralizada. Aparece la consistencia eventual entre servicios, que se maneja con estados explícitos (PENDIENTE, EN TRÁMITE, ENVIADA, OPCIONADA) y eventos idempotentes. La red añade latencia, pero ninguna ventana necesita más de una llamada síncrona entre servicios. Las consultas que cruzan datos de varios servicios, como los KPI, no leen bases ajenas: ms-analitica arma su propio almacén con los eventos.

## 5.6 Ejemplo en las ventanas

En la ventana 21 el director pulsa «Generar orden de compra». La SPA envía POST /api/ordenes-compra; el gateway lo enruta a ms-ordenes, que pide por REST los datos de la cotización OPCIONADA a ms-cotizaciones (CotizacionRestClient con GET /api/cotizaciones/{id}, síncrono porque el director espera la respuesta), guarda la orden en su propia base, genera su PDF, lo deja en el almacén de objetos y responde 201 con OC-0012, que aparece en la ventana 22. Si ms-analitica o ms-notificaciones estuvieran caídos en ese momento, la orden se genera igual.
