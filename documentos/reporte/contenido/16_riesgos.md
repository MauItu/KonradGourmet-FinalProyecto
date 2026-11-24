<!-- §16. Correcciones: R-56 (fila nueva, PD-01); en el 7d, las filas de eventos repetidos y del límite de tasa (DEC-104). R-54 y R-55 ya son ciertos. -->
# 16. Riesgos y mitigaciones

La tabla 14 reúne los riesgos que trae la arquitectura elegida y cómo se mitiga cada uno.

!tabla 14 | Riesgos y mitigaciones | 4.4,3.6,6.4
| Riesgo | Impacto | Mitigación |
|---|---|---|
| El jefe ve PENDIENTE unos segundos después de que el auxiliar envió la cotización | Confusión sobre el estado real de la solicitud | Mostrar la fecha de última actualización en la ventana 10 y vigilar que la propagación de eventos tarde menos de unos segundos |
| El servicio web del Ministerio no responde | No se puede calcular el precio estándar de una cotización | Proxy con caché en Redis; si no hay precio, la cotización queda pendiente de precio y se reintenta sin bloquear al auxiliar |
| Un servicio guarda en su base pero falla al publicar el evento | Otros servicios no se enteran del cambio | Patrón outbox transaccional: el evento se guarda en la misma transacción que el cambio y un relé lo publica después; si Kafka no responde, el evento espera en la tabla y el relé reintenta |
| Un evento llega dos veces (Kafka y el relé entregan al menos una vez) | Un pedido se descontaría dos veces del inventario | Consumidores idempotentes: cada uno guarda el identificador de los eventos que procesó, en la misma transacción que su cambio, y descarta los repetidos. ms-notificaciones marca el evento justo después de enviar el correo, porque el envío no cabe en una transacción: un fallo entre el envío y la marca puede repetir un correo (al menos una vez, sección 11) |
| Un cliente o un sistema externo envía una ráfaga de peticiones | Los demás usuarios esperan o el servicio se satura | Límite de tasa en el gateway, por usuario o por IP, con los contadores en Redis: pasado el cupo responde 429 |
| Operación compleja de once servicios | Errores de despliegue y diagnóstico lento | Kubernetes, infraestructura como código, trazas con identificador de correlación y tableros de observabilidad |
| Procesos que cruzan servicios (orden generada, solicitud que avanza) | Datos inconsistentes si un paso falla a mitad | Saga coreografiada con eventos y estados explícitos que permiten compensar o reintentar (Richardson, 2018) |
| Costo de once bases de datos | Más infraestructura que un monolito | Servicios administrados; las bases pequeñas pueden compartir servidor con esquemas separados, sin compartir tablas |
| Seguridad entre servicios | Un servicio comprometido podría llamar a otros | mTLS entre servicios, propagación del JWT y verificación del rol en cada endpoint |
| La alarma la procesa una réplica de ms-notificaciones distinta de la que tiene abierta la conexión del jefe | El jefe no la ve en tiempo real | La réplica que consume el evento la difunde por Redis pub/sub a todas las réplicas, y cada una la entrega a sus conexiones; además, V05 consulta las alertas activas al abrirse, así que la alarma no se pierde |
