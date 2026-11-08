# Konrad Gourmet — Sistema de Abastecimiento

Sistema de abastecimiento para restaurantes, construido con **arquitectura hexagonal**
(puertos y adaptadores) y organizado en módulos que corresponden a microservicios.

## Arquitectura

Cada módulo se divide siempre en las mismas capas:

| Capa | Contenido | Regla |
|---|---|---|
| `dominio/` | Entidades, objetos de valor y reglas de negocio | No depende de nada externo |
| `eventos/` | Eventos de dominio que publica el módulo | Extienden `EventoDominio` |
| `puertos/entrada/` | Casos de uso que el módulo ofrece | Interfaces |
| `puertos/salida/` | Lo que el módulo necesita del exterior | Interfaces |
| `aplicacion/` | Servicios que implementan los casos de uso | Orquesta el dominio |
| `adaptadores/entrada/` | Controladores REST, listeners, jobs | Llaman a los puertos de entrada |
| `adaptadores/salida/` | Persistencia, clientes HTTP/SOAP, correo | Implementan los puertos de salida, salvo el cliente SOAP generado del WSDL, que es el Adaptee |

La dependencia siempre apunta hacia adentro: los adaptadores conocen los puertos,
nunca al revés. El dominio no importa frameworks ni adaptadores. Hay una sola excepción
a «el dominio no conoce a nadie»: en `notificaciones`, el Factory Method usa las
interfaces `Correo` y `AlarmaPushPort`, que son puertos del mismo núcleo.

Entre microservicios, un módulo solo puede importar el paquete `eventos` de otro, que
es el contrato de su tópico, y nunca su `dominio`. Todos pueden usar `compartido`.

## El código y los diagramas de clases

El código sigue las páginas 4a–4e del diagrama de arquitectura: los mismos paquetes,
clases, atributos, métodos y relaciones `extends`/`implements`.

- Como es habitual en UML, los diagramas omiten los constructores y los métodos de
  acceso. En el código, los servicios, controladores, listeners, jobs y adaptadores
  reciben por el constructor lo que su caja lista como atributos; `ParametrosSistema`
  tiene el constructor privado del Singleton, y cada decorador recibe el correo que
  envuelve.
- Cada método marcado ‹Vxx› en el diagrama lleva el comentario
  `// Atiende la ventana Vxx de los mockups`, una línea por ventana. Si alguna fuente
  fija la ruta REST, la línea siguiente la dice, por ejemplo `// POST /api/cotizaciones`.
- El texto de color de cada caja (patrón, ruta, tópico o tecnología) y las notas del
  diagrama van en el Javadoc de la clase. Las anotaciones de Spring y de JAX-WS van
  solo en ese comentario, para que el código compile solo con `javac`.
- Los métodos todavía lanzan `UnsupportedOperationException("Pendiente de implementar")`.

## Módulos

| Módulo | Microservicio | Historias | Responsabilidad |
|---|---|---|---|
| `compartido` | — | — | Librería común: publicación de eventos y auditoría |
| `identidad` | ms-identidad | V01 | Usuarios, roles, sucursales y autenticación |
| `pedidos` | ms-pedidos | V02–V04 | Registro y seguimiento de pedidos |
| `menu` | ms-menu | V06 | Platos, ingredientes y categorías |
| `inventario` | ms-inventario | V05, V07 | Stock, umbrales y consumo diario |
| `solicitudes` | ms-solicitudes | V08–V12 | Solicitudes de alimentos |
| `cotizaciones` | ms-cotizaciones | V13–V20 | Cotizaciones, proveedores y reglas de validación |
| `ordenes` | ms-ordenes | V21–V23 | Órdenes de compra |
| `notificaciones` | ms-notificaciones | — | Correos y alarmas en la aplicación |
| `integracion` | ms-integracion | — | Frontera SOAP con el Ministerio de Comercio |
| `administracion` | ms-administracion | — | Parámetros del sistema y auditoría |
| `analitica` | ms-analitica | — | Tablero BAM e indicadores |
| `demo` | — | — | Ejecuta los patrones sin base de datos ni Kafka |

## Patrones de diseño

| Patrón | Dónde | Clases |
|---|---|---|
| Facade | `cotizaciones/aplicacion` | `GenerarCotizacionFacade`; a escala de sistema, el API Gateway |
| Chain of Responsibility | `cotizaciones/dominio` | `ReglaValidacion` → `ReglaRechazada` → `ReglaSospechosa` → `ReglaOpcionada`; la cadena se arma en `ReglaValidacion.cadenaPorDefecto()` |
| Composite | `identidad/dominio` | `ElementoMenu` (componente), `GrupoMenu` (compuesto), `OpcionMenu` (hoja) |
| Factory Method | `identidad/dominio`, `notificaciones/dominio` | `Usuario.construirMenu()` en cada rol; `CreadorNotificacion` → `CreadorCorreo`, `CreadorAlarmaApp`, que crean una `Notificacion` |
| Decorator | `notificaciones/puertos/salida`, `notificaciones/adaptadores/salida` | `Correo` (componente), `CorreoSmtpAdapter` (componente concreto), `CorreoDecorator`, `CorreoCertificadoDecorator`, `EstampaCronologicaDecorator` |
| Adapter | `integracion/adaptadores/salida` | `MinisterioComercioAdapter` adapta `MinisterioSoapClient` a `PrecioReferenciaPort`; además, todos los `*Adapter` de la hexagonal |
| Proxy | `integracion/adaptadores/salida` | `PrecioReferenciaCacheProxy`: implementa `PrecioReferenciaPort`, como el adaptador real, y guarda la caché en Redis |
| Publish-Subscribe (Observer distribuido) | `compartido/puertos`, `compartido/eventos`, `compartido/adaptadores` y los listeners de cada servicio | `EventPublisherPort`, `KafkaEventPublisher`, `EventListener<T>`, `EventoDominio` y los `*Listener` |
| Singleton | `administracion/dominio` | `ParametrosSistema.getInstancia()`: tema, logos y parámetros generales |
| Strategy | `analitica/dominio` | `CalculadorKpi` → `ProductoMayorDemanda`, `SucursalMayorDemanda`, `ProveedorMejoresPrecios` |

## Estructura

```
KonradGourmet/
├── README.md
└── src/co/edu/konradlorenz/
    ├── compartido/
    ├── identidad/
    ├── pedidos/
    ├── menu/
    ├── inventario/
    ├── solicitudes/
    ├── cotizaciones/
    ├── ordenes/
    ├── notificaciones/
    ├── integracion/
    ├── administracion/
    ├── analitica/
    └── demo/
```

## Ejecutar la demo

```bash
javac -d out $(find src -name "*.java")
java -cp out co.edu.konradlorenz.demo.DemoArquitectura
```

## Interfaz web

La pantalla de inicio de sesión está en la raíz del proyecto:

```
index.html      maquetado
styles.css      estilos
assets/logo/    logo.png
assets/video/   3DVideo.mp4
```

Se abre directamente en el navegador. Si el video no arranca por las
restricciones de `file://`, levanta un servidor local:

```bash
python -m http.server 8000
```
