<!-- §13. Correcciones: R-41, R-42, R-43, R-45, R-46 y R-47. R-44 ya es cierto (29 comentarios cubren las 23 ventanas; verificado el 2026-11-09 sobre el código 9c4563b, con 211 archivos .java y 176 verificaciones bien). -->
!vertical

# 13. Del diagrama al código

## 13.1 Un solo modelo

El esqueleto Java se generó a partir de las páginas 4a a 4e del .drawio, de modo que los nombres coinciden uno a uno con el diagrama de clases y con el mapa. Antes de entregar se comprobó automáticamente que cada clase que aparece en el mapa (adaptadores, núcleo de dominio y casos de uso) existe en el código y que el método asignado a cada una de las 23 ventanas está declarado en su controlador.

## 13.2 Estructura del esqueleto

El código está en el paquete co.edu.konradlorenz.<servicio>.<capa>, con doce paquetes de servicio (los once microservicios más compartido) y 191 clases, las mismas de las páginas 4a a 4e. Aparte está el paquete demo, con DemoArquitectura y 19 clases de apoyo (adaptadores en memoria y el ensamblador que los conecta), que no son parte del modelo; y fuera de src, la carpeta pruebas, con 176 verificaciones. Está escrito en Java 21 sin dependencias externas para que compile solo con javac y se pueda revisar en cualquier equipo.

Al implementarse, cada paquete de servicio se convierte en un proyecto Spring Boot y los adaptadores reciben sus anotaciones (@RestController, @Repository, @KafkaListener) sin tocar el dominio, que es precisamente lo que promete la arquitectura hexagonal. Dos anotaciones resuelven lo que trae tener varias réplicas: el Proxy de precios lleva además @Primary, para que ConsultarPrecioService reciba el Proxy y no el adaptador real, y los dos jobs programados llevan @SchedulerLock (ShedLock), para que cada uno corra en una sola réplica. El Javadoc de cada adaptador dice qué anotación lleva en el proyecto real. Los métodos marcados en el diagrama con ‹Vxx› llevan en el código el comentario «Atiende la ventana Vxx de los mockups».

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
