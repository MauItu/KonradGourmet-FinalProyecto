# Ministerio de Comercio (simulado)

Sistema externo de la página 2 del `.drawio`: el servicio web SOAP de precios estándar. En local lo
simula WireMock (DEC-71), como Mailpit simula el servidor de correo: no es parte del sistema y no se
registra en Eureka. Solo lo ve ms-integracion, dentro de la red de Docker (`http://ministerio:8080/precios`).

- `__files/precios.wsdl`: el contrato que sigue `MinisterioSoapClient` (`consultarPrecioEstandar`).
- `mappings/precio-N.json`: el precio por kilo de la res (1), el arroz (2), la papa (3) y el pollo (4),
  los de la demo, con 300 ms de demora, para que se note cuándo responde la caché.
- `mappings/sin-precio.json`: cualquier otro producto recibe un SOAP Fault.

WireMock lleva la cuenta de las llamadas (`/__admin/requests`): así `verificar_corte4a.py` comprueba que
la segunda consulta la respondió Redis y no el Ministerio.
