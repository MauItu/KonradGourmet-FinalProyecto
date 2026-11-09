<!-- §12. Correcciones: R-35, R-37, R-38 y R-40 (figuras 8 a 10 recapturadas y renumeradas, R-03). R-36 ya es cierto. R-39 es opcional y no se aplica. -->
!horizontal

# 12. Patrones de diseño en el nivel de código

El profesor pidió que en el nivel 4 del C4 se vea dónde va cada patrón. En las páginas 4a a 4e del .drawio cada clase con patrón lleva su estereotipo en magenta; el esqueleto Java declara las mismas clases, firmas y relaciones, y la demostración ejecuta los patrones. La tabla 11 resume los diez patrones (Gamma et al., 1994), dónde están y qué costo tienen; las figuras 8 a 10 muestran de cerca los tres fragmentos del diagrama de clases donde se concentran.

!tabla 11 | Patrones de diseño del nivel de código | 3,4.6,3.4,5,5
| Patrón | Clases | Servicio y ventanas | Por qué aquí | Consecuencia |
|---|---|---|---|---|
| Facade | GenerarCotizacionFacade; a escala de sistema, el API Gateway | ms-cotizaciones, V13 y V14 | Un clic coordina precio sugerido, repositorio, PDF y evento | El controlador hace una sola llamada; la fachada solo coordina, no decide |
| Chain of Responsibility | ReglaValidacion, ReglaRechazada, ReglaSospechosa y ReglaOpcionada | ms-cotizaciones, V19 | Tres reglas excluyentes que se evalúan en orden; una regla nueva no toca las demás | El orden importa, así que la cadena se arma en un solo lugar (cadenaPorDefecto) |
| Composite | ElementoMenu, GrupoMenu, OpcionMenu | ms-identidad, panel izquierdo de las 23 ventanas | El menú lateral es un árbol de grupos y opciones | Un rol nuevo no exige cambiar la SPA, que solo recorre el árbol |
| Factory Method | Usuario.construirMenu() en cada rol; CreadorNotificacion, CreadorCorreo y CreadorAlarmaApp | ms-identidad y ms-notificaciones, V01 y V05 | La subclase decide qué producto crear: el menú del rol o el tipo de notificación | Una clase creadora por variante |
| Decorator | CorreoDecorator, CorreoCertificadoDecorator y EstampaCronologica​Decorator sobre CorreoSmtpAdapter | ms-notificaciones, correos de V09, V14 y V22 | Certificado y sello de tiempo se suman al correo sin modificar el envío SMTP | El orden de los decoradores se fija en la configuración |
| Adapter | MinisterioComercioAdapter y todos los \*Adapter de la hexagonal | ms-integracion, V13 | Traduce el servicio SOAP del Ministerio al puerto PrecioReferenciaPort | Un adaptador por cada tecnología externa |
| Proxy | PrecioReferenciaCacheProxy | ms-integracion, V13 | Evita llamar al servicio web por cada producto de cada cotización | Un precio puede quedar desactualizado hasta que vence la caché |
| Publish-Subscribe (Observer distribuido) | EventPublisherPort, EventListener y los listeners de Kafka | compartido y todos los servicios | Publicar hechos sin conocer a los suscriptores. A diferencia del Observer de GoF, el publicador no guarda la lista de observadores: Kafka hace de intermediario | Consistencia eventual entre servicios |
| Singleton | ParametrosSistema | ms-administracion, imagen corporativa | Una sola fuente de parámetros por instancia: tema, logos e imagen corporativa | Estado global: de solo lectura para el resto, recargado cuando cambia. La SPA lo lee con GET /api/admin/parametros/tema. El umbral del 25 % no vive aquí: es una regla de ms-inventario |
| Strategy | CalculadorKpi, con ProductoMayorDemanda, SucursalMayorDemanda y ProveedorMejoresPrecios | ms-analitica, tablero BAM | Cada KPI es un algoritmo intercambiable sobre los mismos hechos | Un KPI nuevo es una clase nueva |


!figura 8 | figuras/fig08_4a_identidad.png | Composite y Factory Method en ms-identidad | Fragmento de la página 4a. Usuario «abstract» es el creador del Factory Method: cada rol implementa construirMenu() y devuelve un árbol ElementoMenu (Composite) con GrupoMenu y OpcionMenu. A la derecha, IniciarSesionService usa los puertos UsuarioRepositoryPort y CifradorPort, que implementan UsuarioJpaAdapter y BCryptCifradorAdapter (líneas punteadas de realización). | horizontal

!figura 9 | figuras/fig09_4c_cotizaciones.png | Chain of Responsibility y Facade en ms-cotizaciones | Fragmento de la página 4c. ReglaValidacion enlaza ReglaRechazada, ReglaSospechosa y ReglaOpcionada (herencia con triángulo hueco). GenerarCotizacionFacade depende de los puertos de repositorio, precio sugerido, PDF, almacén y eventos. Cotizacion compone sus ItemCotizacion (rombo lleno) y se asocia al Proveedor y a su EstadoCotizacion. | horizontal

!figura 10 | figuras/fig10_4d_notificaciones.png | Factory Method y Decorator en ms-notificaciones | Fragmento de la página 4d. CreadorNotificacion «abstract» delega en CreadorCorreo y CreadorAlarmaApp la creación de la Notificacion. CorreoDecorator agrega (rombo vacío) un Correo y sus subclases añaden el certificado y el sello de tiempo; CorreoSmtpAdapter es el componente concreto que habla SMTP con TLS. Notificacion.enviar() y CreadorNotificacion.notificar() reciben la referencia del adjunto (adjuntoUrl).
