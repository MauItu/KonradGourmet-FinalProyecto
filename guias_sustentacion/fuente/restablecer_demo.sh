#!/bin/bash
# Deja la instancia kg-demo con los datos de la demostración: copia tres volúmenes de kg-mockups (bases, PDF e
# imágenes, logs) sobre los de kg-demo (DEC-111) y vacía su Kafka y su Redis (DEC-113, DEC-114). kg-mockups no cambia.
# Uso, desde FinalProyecto:  documentos/guias_sustentacion/fuente/restablecer_demo.sh
# Después:  COMPOSE_PROJECT_NAME=kg-demo KonradGourmet/plataforma/levantar.sh --sin-compilar
#           y esperar ~75 s a que Eureka propague antes de entrar.
set -e
ORIGEN=kg-mockups
DESTINO=kg-demo
COMPOSE=$(cd "$(dirname "$0")/../../../KonradGourmet/plataforma" && pwd)/docker-compose.yml
IMAGEN=postgres:17-alpine   # cualquier imagen con sh y cp; esta ya está en el equipo

# Las dos instancias tienen que estar detenidas para que la copia sea coherente.
for p in $ORIGEN $DESTINO; do
  if [ -n "$(docker ps -q --filter label=com.docker.compose.project=$p)" ]; then
    echo "Deteniendo $p…"
    COMPOSE_PROJECT_NAME=$p docker compose -f "$COMPOSE" stop >/dev/null 2>&1
  fi
done
for v in datos-postgres datos-s3 datos-elasticsearch; do
  docker volume inspect "${ORIGEN}_$v" >/dev/null
  docker volume inspect "${DESTINO}_$v" >/dev/null 2>&1 || docker volume create \
    --label com.docker.compose.project=$DESTINO --label com.docker.compose.volume=$v "${DESTINO}_$v" >/dev/null
  docker run --rm -v "${ORIGEN}_$v":/de:ro -v "${DESTINO}_$v":/a $IMAGEN \
    sh -c 'find /a -mindepth 1 -delete && cp -a /de/. /a/'
  echo "  ${DESTINO}_$v ← ${ORIGEN}_$v"
done
# Kafka guarda los tópicos en datos-kafka (DEC-114). No se copia de kg-mockups: se vacía, para que los tópicos solo
# tengan los eventos de esta demo y no los de ensayos anteriores; levantar.sh crea otra vez los tópicos. Queda del
# usuario de la imagen (1000), que es quien escribe ahí.
docker volume inspect "${DESTINO}_datos-kafka" >/dev/null 2>&1 || docker volume create \
  --label com.docker.compose.project=$DESTINO --label com.docker.compose.volume=datos-kafka "${DESTINO}_datos-kafka" >/dev/null
docker run --rm -v "${DESTINO}_datos-kafka":/a $IMAGEN sh -c 'find /a -mindepth 1 -delete && chown 1000:1000 /a'
echo "  ${DESTINO}_datos-kafka vacío"
# Redis guarda la caché en un volumen anónimo (/data) que sobrevive a «stop»: se quita su contenedor con ese volumen
# para que la caché de precios empiece vacía y la primera cotización consulte al Ministerio (DEC-113). El de Kafka
# también se quita, para que arranque de cero sobre el volumen vacío. levantar.sh vuelve a crear los dos.
COMPOSE_PROJECT_NAME=$DESTINO docker compose -f "$COMPOSE" rm -f -v redis kafka >/dev/null 2>&1 || true
echo "  ${DESTINO}-redis y ${DESTINO}-kafka: la caché y los tópicos empiezan vacíos"
echo "kg-demo restablecida. Para encenderla:"
echo "  COMPOSE_PROJECT_NAME=$DESTINO KonradGourmet/plataforma/levantar.sh --sin-compilar"
