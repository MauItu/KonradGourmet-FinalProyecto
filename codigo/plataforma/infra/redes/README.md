# Redes sociales (simuladas)

Sistema externo de la página 2 del `.drawio` («Redes sociales · APIs públicas HTTPS · señales de tendencia de
productos»). En local lo simula WireMock, como el Ministerio de Comercio (DEC-71): no es parte del sistema y no se
registra en Eureka. Solo lo ve ms-analitica, dentro de la red de Docker (`http://redes:8080/v1/tendencias`).

- `mappings/tendencias.json`: con el token de Konrad Gourmet (`Authorization: Bearer konrad-redes-local`) y la
  consulta de `RedesSocialesAdapter` (`?pais=CO&categoria=gastronomia&periodo=semana`), los productos en tendencia
  de la semana, de más a menos menciones: el Ajiaco y la Limonada natural, con 200 ms de demora.
- `mappings/sin-token.json`: sin token, o con otro, responde 401.

La red social concreta no la fija el enunciado (supuesto S11); el contrato de la API es el que asume DEC-91.
WireMock lleva la cuenta de las llamadas (`/__admin/requests`), y `verificar_corte6b.py` le cambia la respuesta por
su API de administración (`/__admin/mappings`) para probar una tendencia nueva y la API caída; al terminar, la
deja como estaba (`/__admin/mappings/reset`).
