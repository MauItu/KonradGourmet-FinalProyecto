<!-- §4. Sin correcciones de contenido; solo cambia la redacción de los veredictos. -->
# 4. Estilos evaluados

En las exposiciones de clase se presentaron quince estilos, mecanismos y componentes. Todos se evaluaron contra las restricciones del enunciado (Richards y Ford, 2020): tres se eligieron como arquitectura principal, seis quedaron como apoyo y seis se descartaron o se absorbieron en otro (tabla 4).

!tabla 4 | Estilos y mecanismos evaluados | 3,2.8,7.5
| Estilo o mecanismo | Veredicto | Razón para Konrad Gourmet |
|---|---|---|
| Microservicios | Elegida: estructura | Permite escalar solo lo que recibe carga, aislar fallas entre roles y desplegar cada parte por separado. Es la única opción vista que responde a 1.000 TPS y al 99,7 % sin escalar todo el sistema a la vez. |
| Orientada a eventos | Elegida: comunicación | El enunciado está lleno de efectos secundarios (correos, alarma del 25 %, auditoría, paso a EN TRÁMITE, KPI, tendencias) que no deben bloquear al usuario ni acoplar servicios. |
| Hexagonal (puertos y adaptadores) | Elegida: interior | Aísla el dominio de la base de datos, del SOAP del Ministerio y del SMTP, que son justamente los puntos que el enunciado pide poder cambiar o integrar. |
| Limpia (Clean Architecture) | Absorbida en la hexagonal | Persigue lo mismo: dependencias hacia el dominio (Martin, 2017). Aplicar ambas duplicaría capas; se conserva su regla de dependencia y la separación entre casos de uso y entidades. |
| Capas (monolito) | Descartada como estilo global | Un solo despliegue obliga a escalar todo para atender el pico de pedidos, y una falla detiene a los cuatro roles. La idea de capas sigue viva dentro de cada hexágono. |
| Cliente-servidor | Apoyo | La SPA es el cliente y el API Gateway el servidor; es la relación natural de una aplicación web responsive. |
| REST | Apoyo | Contrato síncrono para las 23 ventanas y para las dos consultas entre servicios que necesitan respuesta inmediata. |
| SOA con SOAP | Apoyo en la frontera | El requerimiento de integración obliga a exponer servicios por SOAP y el Ministerio publica un servicio web. Se limita a ms-integracion para que el resto del sistema no dependa de SOAP. |
| ESB central | Descartado | Un bus de integración central orquestaría a todos los servicios y se volvería cuello de botella y punto único de falla, riesgo que señaló el propio grupo expositor (Hohpe y Woolf, 2003). Kafka da el desacople sin lógica central. |
| Peer-to-peer (P2P) | Descartado | No hay pares equivalentes: pedidos, inventario y compras deben estar centralizados, auditados y protegidos por roles. |
| Polling | Descartado entre servicios | Preguntar cada cierto tiempo si algo cambió consume transacciones sin aportar. Solo se conserva la tarea programada diaria de inventario, que el enunciado pide así. |
| Webhooks | Descartado internamente | Sirven para avisar a terceros por HTTP, pero dentro del sistema Kafka ofrece persistencia, reintentos y varios consumidores. Queda como opción futura para avisar a proveedores. |
| Balanceador de carga | Apoyo | Par activo-activo de capa 7 delante del gateway, con chequeos de salud cada 5 segundos. |
| Service Registry | Apoyo | Eureka mantiene la lista de instancias vivas para que el gateway enrute aunque el autoescalado cambie las réplicas. |
| API Gateway | Apoyo: Facade del sistema | Punto único de entrada: valida el JWT y enruta por prefijo de URL hacia cada servicio, ocultando a la SPA cuántos servicios hay detrás. |
