<!-- §6. Correcciones: R-21, R-22, R-23 y R-24. -->
# 6. Arquitectura orientada a eventos

La segunda arquitectura define cómo se avisan los servicios entre sí sin esperarse. Vive entre los contenedores y responde a los requerimientos de correo certificado, alarma de inventario, auditoría y disponibilidad (Fowler, 2017).

## 6.1 Esquema

Apache Kafka con tres brokers y diez tópicos. Cada servicio publica hechos de negocio en pasado (pedido.registrado, cotizacion.validada) a través del puerto EventPublisherPort, y los servicios interesados se suscriben con listeners. Es coreografía, no orquestación: nadie dirige el proceso y cada consumidor decide qué hacer con el evento. En el mapa es la banda ámbar inferior con sus flechas punteadas.

## 6.2 Lineamientos, políticas y reglas

El lineamiento es el desacoplamiento temporal: el productor no sabe quién consume ni espera respuesta. Las políticas son eventos inmutables con identificador, tipo, fecha y usuario; entrega al menos una vez; consumidores idempotentes que recuerdan los identificadores ya procesados; orden garantizado por clave (número de solicitud o de cotización) dentro de una partición; y una cola de errores (DLQ) para los mensajes que fallan varias veces (Kleppmann, 2017). El usuario sale del JWT de quien hizo la acción.

Las reglas son un tópico por hecho de negocio con el nombre entidad.verbo-en-pasado, ningún evento lleva contraseñas, la retención es de siete días y el esquema de cada evento se versiona.

## 6.3 Aplicabilidad en Konrad Gourmet

El enunciado describe muchos efectos que ocurren después de la acción del usuario. Al generar una cotización hay que enviar el PDF por correo al proveedor y revisar si la solicitud del jefe ya pasa a EN TRÁMITE. Después de cada CRUD hay que auditar. Cada pedido alimenta el descuento de inventario y los KPI. Cuando una existencia baja del 25 % de su stock de referencia hay que mostrar una alarma y enviar un correo, una sola vez: el aviso sale la noche en que el insumo cruza el umbral, no cada noche mientras siga bajo. Las tendencias generan correos de promociones. Si todo eso fuera síncrono, la ventana 13 esperaría al servidor de correo y una caída del correo impediría cotizar.

## 6.4 Ventajas

El usuario recibe respuesta inmediata porque los efectos secundarios ocurren en segundo plano. Un servicio caído no arrastra a los demás: sus eventos esperan en Kafka hasta que vuelve. Agregar un consumidor nuevo, como ms-analitica, no obliga a tocar al productor. Y el propio flujo de eventos es una bitácora que facilita la auditoría pedida por el enunciado.

## 6.5 Desventajas y cómo se mitigan

La consistencia es eventual: el jefe puede ver PENDIENTE durante unos segundos después de que el auxiliar envió la cotización; por eso la ventana 10 muestra la fecha de la última actualización. Puede haber mensajes duplicados, que se neutralizan con idempotencia. La depuración es más difícil, así que cada evento lleva un identificador de correlación y se traza con OpenTelemetry. Ese identificador pasa de un evento al siguiente, de modo que una traza reúne todo el recorrido de una compra. Kafka es una pieza más que debe estar disponible, y por eso corre con tres brokers y factor de réplica 3.

## 6.6 Ejemplo en las ventanas

En la ventana 13 el auxiliar pulsa «Enviar solicitud de cotización». ms-cotizaciones guarda la cotización en estado ENVIADA, genera el PDF, lo deja en el almacén de objetos, publica cotizacion.enviada con la referencia del PDF y responde 201, con lo que aparece la ventana 14. En paralelo, ms-notificaciones consume el evento y envía el PDF al proveedor por correo certificado, y ms-solicitudes lo consume y pasa a EN TRÁMITE cada solicitud incluida (una o varias, HU-09) cuyos productos ya tengan todos cotización, y publica solicitud.en-tramite, que el jefe ve en la ventana 10. ms-cotizaciones no conoce a ninguno de los dos consumidores.
