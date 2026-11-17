#!/bin/bash
# Crea los 10 tópicos del sistema (sección 6 del reporte). La creación automática está apagada
# en el broker, así que ningún servicio puede publicar en un tópico que no esté aquí.
set -e
B=kafka:9092
until /opt/kafka/bin/kafka-topics.sh --bootstrap-server $B --list >/dev/null 2>&1; do sleep 2; done
for t in pedido.registrado menu.actualizado inventario.stock-bajo solicitud.registrada solicitud.en-tramite \
         cotizacion.enviada cotizacion.validada orden.generada tendencia.detectada auditoria.evento; do
  /opt/kafka/bin/kafka-topics.sh --bootstrap-server $B --create --if-not-exists --topic $t --partitions 3 --replication-factor 1
done
/opt/kafka/bin/kafka-topics.sh --bootstrap-server $B --list
