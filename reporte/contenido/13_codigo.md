<!-- §13. Correcciones: R-41, R-42, R-43, R-45, R-46 y R-47; en el 7d, CD-15, CD-50 y las pruebas (§13.4). R-44 ya es cierto (29 comentarios cubren las 23 ventanas; verificado el 2026-11-09 sobre el código 9c4563b, con 211 archivos .java y 176 verificaciones bien). -->
!vertical

# 13. Del diagrama al código

## 13.1 Un solo modelo

El esqueleto Java se generó a partir de las páginas 4a a 4e del .drawio, de modo que los nombres coinciden uno a uno con el diagrama de clases y con el mapa. Antes de entregar se comprobó automáticamente que cada clase que aparece en el mapa (adaptadores, núcleo de dominio y casos de uso) existe en el código y que el método asignado a cada una de las 23 ventanas está declarado en su controlador.

## 13.2 Estructura del esqueleto

El código está en el paquete co.edu.konradlorenz.<servicio>.<capa>, con doce paquetes de servicio (los once microservicios más compartido) y 195 clases, las mismas de las páginas 4a a 4e. Aparte está el paquete demo, con DemoArquitectura y 24 clases de apoyo (adaptadores en memoria, el ensamblador que los conecta y un servidor web local), que no son parte del modelo. El núcleo está escrito en Java 21 sin dependencias externas, para que compile solo con javac y se pueda revisar en cualquier equipo.

Cada paquete de servicio es además un proyecto Spring Boot, en la carpeta servicios: al compilar copia sin cambios el núcleo de su paquete (dominio, eventos, puertos y aplicación) y le agrega sus adaptadores con las anotaciones de Spring (@RestController, @Repository, @KafkaListener) y los mismos nombres del diagrama, que es precisamente lo que promete la arquitectura hexagonal. Dos anotaciones resuelven lo que trae tener varias réplicas: el Proxy de precios lleva además @Primary, para que ConsultarPrecioService reciba el Proxy y no el adaptador real, y los dos jobs programados llevan @SchedulerLock (ShedLock), para que cada uno corra en una sola réplica. Los servicios tienen también piezas de infraestructura que el diagrama de clases no dibuja, como no dibuja los constructores: la configuración de Spring, las entidades JPA, la outbox y su relé, el registro de los eventos ya procesados y el canal de alarmas en Redis, entre otras. Las páginas 4a a 4e las nombran sin caja, en las notas «Infraestructura que no se dibuja», y no cuentan entre las 195 clases. Los métodos marcados en el diagrama con ‹Vxx› llevan en el código el comentario «Atiende la ventana Vxx de los mockups».

## 13.3 Comprobación con la demostración

DemoArquitectura recorre el flujo de compra de punta a punta (pedido, alarma de stock, solicitud SOL-031, cotizaciones COT-018 a COT-020 y orden OC-0012) y ejecuta los patrones sin base de datos ni broker, usando adaptadores en memoria. Su salida coincide con los datos de los mockups, como muestra la tabla 12.

!tabla 12 | Salida de la demostración por patrón | 3.2,7.6,2.8
| Patrón | Salida de la demostración | Ventana |
|---|---|---|
| Composite y Factory Method | Menú DIRECTOR_COMPRAS con Inicio, Cotizaciones (Todas, Pendientes de validar, Opcionadas), Órdenes de compra, Mi perfil y Cerrar sesión | Panel izquierdo de V16 a V23 |
| Hexagonal y Publish-Subscribe | Pedido registrado, evento pedido.registrado publicado, total $82.000 | V03 y V04 |
| Facade y Chain of Responsibility | COT-018: estándar $520.000, proveedor $545.000, diferencia 4,81 %, OPCIONADA. COT-019: +34,62 %, RECHAZADA. COT-020: −61,54 %, SOSPECHOSA | V13, V18 y V19 |
| Factory Method y Decorator | Correo a jefe.centro@konradgourmet.com con encabezados X-Certificado y X-Sello-Tiempo; alarma «Res al 23 %: realice una solicitud» | V05 y correos de V09 |
| Proxy y Adapter | Primer precio de la res desde el servicio web; el segundo, desde la caché del Proxy | V13 |
| Singleton | tema.color-primario = #F26B21, misma instancia en dos llamadas | Parametrización |
| Strategy | Producto con mayor demanda: Bandeja paisa (40); proveedor con mejores precios: Proveedor B | Tablero BAM |

Para compilar, ejecutar la demostración y correr las pruebas basta con ./ejecutar.sh, que necesita un JDK 21. En Windows, las órdenes sueltas están en el README.

## 13.4 Pruebas

Las pruebas cubren tres niveles. El núcleo tiene 400 verificaciones en ocho clases de prueba (carpeta pruebas), que corren sin base de datos ni broker; los servicios tienen 91 pruebas JUnit, que corren al compilar; y 763 comprobaciones de punta a punta recorren las ventanas en un navegador con cada rol, a 1.440 y a 390 píxeles de ancho, y revisan lo que quedó en cada base de datos, en Kafka, en Redis, en el almacén de objetos y en el servidor de correo. Un solo comando, plataforma/probar_todo.sh, las corre todas sobre una instalación nueva de los once microservicios con toda su infraestructura; la última corrida dio 1.254 comprobaciones sin fallas.

La historia final cruza los once servicios y los seis roles: el jefe de cocina crea un plato, el mesero lo pide, el cierre del día dispara la alarma de stock bajo, el jefe pide el insumo, el auxiliar cotiza con dos proveedores, el director de compras valida, genera la orden y la marca como recibida, el director comercial ve el resultado en el tablero y el administrador, en la auditoría. También comprueba que un evento entregado dos veces no se procesa dos veces, que una escritura con el broker detenido conserva su evento en la outbox y lo entrega una sola vez cuando el broker vuelve, y que el gateway responde 429 a una ráfaga de peticiones.
